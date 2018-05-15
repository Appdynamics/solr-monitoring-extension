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
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;
import static com.appdynamics.extensions.solr.utils.Constants.NAME;
import static com.appdynamics.extensions.solr.utils.Constants.SOLR_WITH_SLASH;
import static com.appdynamics.extensions.solr.utils.Constants.COLLECTIONNAME;
import static com.appdynamics.extensions.solr.utils.Constants.METRIC_SEPARATOR;
import static com.appdynamics.extensions.solr.utils.Constants.HEART_BEAT;

public class MetricCollector implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private Stat stat;
    private Phaser phaser;
    private Map server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;
    private MetricDataParser metricDataParser;
    private String endpoint;
    private String serverName;
    private List<Metric> metrics = new ArrayList<Metric>();
    private List<Map<String, String>> metricReplacer;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();


    public MetricCollector(Stat stat, MonitorContextConfiguration monitorContextConfiguration, Map<String, String> server,
                           Phaser phaser, MetricWriteHelper metricWriteHelper, List<Map<String, String>> metricReplacer) {
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());
        this.metricReplacer = metricReplacer;
    }
    public Map<String, Metric> getMetricsMap() {
        return allMetrics;
    }

    public List<Metric> getMetricsList() {
        return metrics;
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + SOLR_WITH_SLASH + server.get(COLLECTIONNAME) + statEndpoint;
    }

    public void run() {
        try {

            serverName = server.get(NAME).toString();
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonNode = null;
            try {
                jsonNode = HttpClientUtils.getResponseAsJson(monitorContextConfiguration.getContext().getHttpClient(), endpoint, JsonNode.class);
            } catch (Exception e) {
                logger.error("Unable to establish connection and get data from endpoint: {}", endpoint);
                metrics.add(new Metric(HEART_BEAT, String.valueOf(BigInteger.ZERO),
                        monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT));

                String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT;
                Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ZERO), prefix);
                allMetrics.put(prefix, heartBeat);

            }
            processStats(stat, jsonNode);

            printMetrics();

        } catch (Exception e) {
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, e.getMessage());
            metrics.add(new Metric(HEART_BEAT, String.valueOf(BigInteger.ZERO),
                    monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT));

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }

    }

    private void printMetrics() {
        metrics.add(new Metric(HEART_BEAT, String.valueOf(BigInteger.ONE), monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT));


        String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR + HEART_BEAT;
        Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ONE), prefix);
        allMetrics.put(prefix, heartBeat);

        List<Metric> metricList = MetricUtils.getListMetrics(allMetrics);


        if (metricList != null && metricList.size() > 0) {
            logger.debug("Printing {} metrics for stat: {}", metricList.size(), stat.getAlias());
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }


    private void processStats(Stat stat, JsonNode jsonNode) {

        if (!childStatNull(stat.getStats())) {
            collectChildStats(stat, jsonNode);
        } else {
            collectStats(stat, jsonNode);
        }

    }

    private void collectStats(Stat stat, JsonNode jsonNode) {
        if (stat.getMetricConfig() != null) {
            allMetrics.putAll(metricDataParser.parseNodeData(stat, jsonNode, new ObjectMapper(), serverName, metricReplacer));

        }
    }


    private boolean childStatNull(Stat[] stat) {
        if (stat == null) {
            return true;
        } else {
            return false;
        }
    }

    private void collectChildStats(Stat stat, JsonNode jsonNode) {
        for (Stat childStat : stat.getStats()) {
            if (childStat != null) {
                if (stat.getRootElement() != null) {
                    if (jsonNode.get(stat.getRootElement()) != null) {
                        jsonNode = jsonNode.get(stat.getRootElement());
                    }
                }
                processStats(childStat, jsonNode);
            } else {
                collectStats(stat, jsonNode);
            }
        }
    }


}
