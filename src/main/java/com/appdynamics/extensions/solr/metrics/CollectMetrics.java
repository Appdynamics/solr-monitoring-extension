package com.appdynamics.extensions.solr.metrics;
/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.extensions.solr.utils.Constants.*;

public class CollectMetrics implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CollectMetrics.class);
    private Stat stat;
    private Phaser phaser;
    private Map server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;
//    private MetricDataParser metricDataParser;
    private String endpoint;
    private String serverName;
    private List<Map<String, String>> metricReplacer;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();


    public CollectMetrics(Stat stat, MonitorContextConfiguration monitorContextConfiguration, Map<String, String> server,
                          Phaser phaser, MetricWriteHelper metricWriteHelper, List<Map<String, String>> metricReplacer) {
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
//        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());

        //#TODO remove metric replacer and get from monitor context configuration
        //#TODO Change name for ProcessChildStats

        this.metricReplacer = metricReplacer;
    }

    public Map<String, Metric> getMetricsMap() {
        return allMetrics;
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + SOLR_WITH_SLASH + server.get(COLLECTIONNAME) + statEndpoint;
    }

    public void run() {
        try {

            serverName = server.get(NAME).toString();
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonNode = HttpClientUtils.getResponseAsJson(monitorContextConfiguration.getContext().getHttpClient(), endpoint, JsonNode.class);

//            logger.debug("Received Json Node and starting processing.");

            boolean isJsonMap = MetricUtils.isJsonMap(stat);

//            if(stat.getStructure().equals("jsonMAp")) {
//                // define a method in utils that ocnverts 32 to 16
//                // return a map of Map<String, JsonNode>
//            }

            ProcessChildStats processChildStats = new ProcessChildStats( monitorContextConfiguration, serverName, metricReplacer, isJsonMap);
            allMetrics.putAll(processChildStats.startProcessingStats(stat, jsonNode ));

//            processStats(stat, jsonNode);

            printMetrics();

        } catch (Exception e) {
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, e.getMessage());
            String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT;
            Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ZERO), prefix);
            allMetrics.put(prefix, heartBeat);

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }

    }

    private void printMetrics() {
        String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT;
        Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ONE), prefix);
        allMetrics.put(prefix, heartBeat);

        List<Metric> metricList = MetricUtils.getListMetrics(allMetrics);
        if ( metricList.size() > 0) {
            logger.debug("Printing {} metrics for stat: {}", metricList.size(), stat.getAlias());
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }



}
