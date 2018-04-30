package com.appdynamics.extensions.solr.metrics;
/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.SolrMonitorTask;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.core.CoreContext;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.solr.input.Stat;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.Phaser;

import java.math.BigInteger;
import java.util.List;

public class MetricCollector implements Runnable{

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
                           Phaser phaser, MetricWriteHelper metricWriteHelper , List<Map<String, String>> metricReplacer){
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());
        this.metricReplacer = metricReplacer;
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + "/solr/" + server.get("collectionName")+ statEndpoint;
    }

    public void run(){
        try {

            serverName = server.get("name").toString();
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonNode = null;
            try {
                 jsonNode = HttpClientUtils.getResponseAsJson(monitorContextConfiguration.getContext().getHttpClient(), endpoint, JsonNode.class);
            } catch (Exception e){
                logger.error("Unable to establish connection and get data from endpoint: {}", endpoint);
                metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ZERO),
                        monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));

                String prefix = monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat";
                Metric heartBeat = new Metric("Heart Beat", String.valueOf(BigInteger.ZERO), prefix);
                allMetrics.put(prefix,heartBeat);

            }
            processStats(stat,jsonNode);

            printMetrics();

        } catch (Exception e){
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, e.getMessage());
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ZERO),
                    monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }

    }

    private void printMetrics() {
        metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ONE), monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));
        String prefix = monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat";
        Metric heartBeat = new Metric("Heart Beat", String.valueOf(BigInteger.ONE), prefix);
        allMetrics.put(prefix,heartBeat);

        // #TODO instead of doing the following method, move it to use the map, get the list and send it to transform and print metrics.
//        List<Metric> metricList = MetricUtils.getListMetrics(allMetrics);

        if (metrics != null && metrics.size() > 0) {
            logger.debug("Printing {} metrics for stat: {}", metrics.size(), stat.getAlias());
            metricWriteHelper.transformAndPrintMetrics(metrics);
        }
    }


    private void processStats(Stat stat, JsonNode jsonNode){

        if(!childStatNull(stat.getStats())){
            collectChildStats(stat, jsonNode);
        }else {
            collectStats(stat,jsonNode);
        }

    }
    private void collectStats(Stat stat, JsonNode jsonNode){
        if(stat.getMetricConfig()!= null) {
            //#TODO this overrides the current data in the all metrics
            allMetrics = metricDataParser.parseNodeData(stat, jsonNode, new ObjectMapper(), serverName, metricReplacer);
//            metrics.addAll(metricDataParser.parseNodeData(stat, jsonNode, new ObjectMapper(), serverName, metricReplacer));

        }
    }


    private boolean childStatNull(Stat[] stat){
        if(stat == null){
            return true;
        }
        else {
            return false;
        }
    }

    private void collectChildStats(Stat stat, JsonNode jsonNode){
        for(Stat childStat : stat.getStats()){
            if(childStat != null){
                if(stat.getRootElement() != null){
                    if(jsonNode.get(stat.getRootElement()) != null){
                         jsonNode = jsonNode.get(stat.getRootElement());
                    }
                }
                processStats(childStat, jsonNode);
            }
            else{
                collectStats(stat, jsonNode);
            }
        }
    }


}
