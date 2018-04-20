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

    public MetricCollector(Stat stat, MonitorContextConfiguration monitorContextConfiguration, Map<String, String> server,
                           Phaser phaser, MetricWriteHelper metricWriteHelper){
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + statEndpoint;
    }

    public void run(){
        try {

            serverName = server.get("name").toString();
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonNode = HttpClientUtils.getResponseAsJson(monitorContextConfiguration.getContext().getHttpClient(), endpoint, JsonNode.class);

            processStats(stat,jsonNode);

            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ONE), monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));

            if (metrics != null && metrics.size() > 0) {
                logger.debug("Printing {} metrics for stat: {}", metrics.size(), stat.getAlias());
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }

        } catch (Exception e){
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, e.getMessage());
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ZERO),
                    monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }

    }


    private void processStats(Stat stat, JsonNode jsonNode){
        if(!childStatNull(stat.getStats())){
//            Stat stat1 = stat.getStats();
            collectChildStats(stat, jsonNode);
        }else {
            collectStats(stat,jsonNode);
        }

    }
    private void collectStats(Stat stat, JsonNode jsonNode){
        if(stat.getMetricConfig()!= null) {
            metrics.addAll(metricDataParser.parseNodeData(stat, jsonNode, new ObjectMapper(), serverName));
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
                //#TODO Implement this
                // get the root element from childstat and convert json node for that and then go from there
                collectStats(stat,jsonNode);
                collectChildStats(childStat, jsonNode);

            } else {
                collectStats(stat, jsonNode);
            }
        }
    }

    private Map mapOfArrayList (ArrayList<?> arrayOfNodes ){
        Map<String, Object> map = new HashMap<String, Object>();

        for(int i= 0; i< arrayOfNodes.size(); i=i+2){
            String name= (String) arrayOfNodes.get(i);
            if(arrayOfNodes.get(i+1) != null){
                map.put(name, arrayOfNodes.get(i+1));
            }
        }

        return map;
    }




}
