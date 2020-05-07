/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.metrics;
/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import com.appdynamics.extensions.util.AssertUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.extensions.solr.utils.Constants.HEART_BEAT;
import static com.appdynamics.extensions.solr.utils.Constants.METRIC_SEPARATOR;
import static com.appdynamics.extensions.solr.utils.Constants.NAME;

public class MetricCollector implements Runnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricCollector.class);
    private Stat stat;
    private Phaser phaser;
    private Map server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;
    private String endpoint;
    private String serverName;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();
    private MetricDataParser metricDataParser;
    private String collectionName;

    public MetricCollector(Stat stat, MonitorContextConfiguration monitorContextConfiguration, Map<String, String> server,
                           Phaser phaser, MetricWriteHelper metricWriteHelper, String endpoint, String collectionName) {
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.phaser.register();
        this.metricWriteHelper = metricWriteHelper;
        this.endpoint = endpoint;
        this.collectionName = collectionName;
        metricDataParser = new MetricDataParser(monitorContextConfiguration, collectionName);
    }

    public Map<String, Metric> getMetricsMap() {
        return allMetrics;
    }

    public void run() {
        try {
            serverName = server.get(NAME).toString();
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonNode = HttpClientUtils.getResponseAsJson(monitorContextConfiguration.getContext().getHttpClient(),
                    endpoint, JsonNode.class);
            AssertUtils.assertNotNull(jsonNode, "The output returned from \"" + endpoint + "\" is NULL");
            logger.debug("Received Json Node and starting processing.");
            addMetricsFromJson(jsonNode, stat);
            printMetrics();
        } catch (Exception e) {
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, e.getMessage());
            String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName +
                    METRIC_SEPARATOR + collectionName + METRIC_SEPARATOR + HEART_BEAT;
            Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ZERO), prefix);
            allMetrics.put(prefix, heartBeat);

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }
    }

    private void printMetrics() {
        String prefix = monitorContextConfiguration.getMetricPrefix() + METRIC_SEPARATOR + serverName + METRIC_SEPARATOR
                + collectionName + METRIC_SEPARATOR + HEART_BEAT;
        Metric heartBeat = new Metric(HEART_BEAT, String.valueOf(BigInteger.ONE), prefix);
        allMetrics.put(prefix, heartBeat);
        List<Metric> metricList = MetricUtils.getListMetrics(allMetrics);
        if (metricList.size() > 0) {
            logger.debug("Printing {} metrics for stat: {}", metricList.size(), stat.getAlias());
            metricWriteHelper.transformAndPrintMetrics(metricList);
        }
    }

    private Map<String, ?> getMapOfJson(boolean isJsonMap, JsonNode jsonNode) {
        Map<String, ?> jsonMap = new HashMap<String, Object>();
        if (isJsonMap) {
            jsonMap = MetricUtils.getMapOfArrayNodes(jsonNode.get(stat.getRootElement()));
        }
        return jsonMap;
    }

    private void addMetricsFromJson(JsonNode childNode, Stat stats) {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        boolean isArray = MetricUtils.isJsonArray(stat);
        Map<String, ?> jsonMap = getMapOfJson(isArray, childNode);

        if (stats.getStats() != null) {
            for (Stat childStat : stats.getStats()) {
                childNode = MetricUtils.getJsonNodeFromMap(childNode, jsonMap, childStat);
                if (childStat.getMetricSection() != null) {
                    childNode = childNode.get(childStat.getMetricSection());
                }
                properties.put(childStat.getRootElement(), childStat.getAlias());
                processStats(childStat, childNode, properties);
                properties.remove(childStat.getRootElement());
            }
        }
    }

    private void processStats(Stat stat, JsonNode jsonNode, Map<String, String> properties) {
        JsonNode node = jsonNode;
        if (!isChildStatNull(stat.getStats())) {
            node = MetricUtils.getJsonNode(stat, node);
            node = MetricUtils.getMetricSectionMetrics(stat, node);
            collectChildStats(stat, node, properties);
        } else {
            collectStats(stat, node, properties);
        }
    }

    private void collectStats(Stat stat, JsonNode jsonNode, Map<String, String> properties) {
        if (stat.getMetricConfig() != null) {
            allMetrics.putAll(metricDataParser.parseNodeData(stat, jsonNode, serverName, properties));
        }
    }

    private boolean isChildStatNull(Stat[] stat) {
        return stat == null;
    }

    private void collectChildStats(Stat stat, JsonNode node, Map<String, String> properties) {
        JsonNode jsonNode = node;
        for (Stat childStat : stat.getStats()) {
            if (childStat != null) {
                properties.put(childStat.getRootElement(), childStat.getAlias());
                jsonNode = MetricUtils.getJsonNode(childStat, node);
                jsonNode = MetricUtils.getMetricSectionMetrics(childStat, jsonNode);
                processStats(childStat, jsonNode, properties);
            } else {
                collectStats(stat, jsonNode, properties);
            }
            if (childStat != null) {
                if (childStat.getRootElement() != null) {
                    properties.remove(childStat.getRootElement());
                }
            }
        }
    }
}