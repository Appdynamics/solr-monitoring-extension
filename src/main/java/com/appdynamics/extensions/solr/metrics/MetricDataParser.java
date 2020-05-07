/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.Constants;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.solr.utils.MetricUtils.convertMemoryStringToDouble;

/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

public class MetricDataParser {
    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricDataParser.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();
    private String collectionName;

    public MetricDataParser(MonitorContextConfiguration monitorContextConfiguration, String collectionName) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.collectionName = collectionName;
    }

    public Map<String, Metric> parseNodeData(Stat stat, JsonNode nodes, String serverName, Map<String, String> properties) {
        if (nodes != null) {
            JsonNode newNode = MetricUtils.getJsonNode(stat, nodes);
            for (MetricConfig metricConfig : stat.getMetricConfig()) {
                getMetricValueFromJson(metricConfig, newNode, serverName, properties);
            }
        } else {
            logger.debug("Empty JSON Node returned for server: {} and stat: {}", serverName, stat.getAlias());
        }
        return allMetrics;
    }

    private String getMetricPrefixUsingProperties(MetricConfig metricConfig, String serverName, Map<String, String> properties) {
        StringBuilder prefix = new StringBuilder();
        if (!Strings.isNullOrEmpty(monitorContextConfiguration.getMetricPrefix())) {
            prefix.append(monitorContextConfiguration.getMetricPrefix()).append(Constants.METRIC_SEPARATOR);
        }
        if (!Strings.isNullOrEmpty(serverName)) {
            prefix.append(serverName).append(Constants.METRIC_SEPARATOR);
        }
        if (!Strings.isNullOrEmpty(collectionName)) {
            prefix.append(collectionName).append(Constants.METRIC_SEPARATOR);
        }
        for (String prop : properties.keySet()) {
            if (properties.get(prop) != null) {
                prefix.append(properties.get(prop)).append(Constants.METRIC_SEPARATOR);

            } else
                prefix.append(prop).append(Constants.METRIC_SEPARATOR);
        }
        if (metricConfig.getAlias() != null) {
            prefix.append(metricConfig.getAlias());
        } else {
            prefix.append(metricConfig.getAttr());
        }
        return MetricUtils.getMetricPathAfterCharacterReplacement(prefix.toString(), getMetricReplacer());
    }

    private List<Map<String, String>> getMetricReplacer() {
        return (List<Map<String, String>>) monitorContextConfiguration.getConfigYml().get("metricCharacterReplacer");
    }

    private void getMetricValueFromJson(MetricConfig metricConfig, JsonNode currentNode, String serverName, Map<String, String> properties) {
        Metric metric = null;
        String metricValue;
        ObjectMapper objectMapper = new ObjectMapper();
        String metricPrefix = getMetricPrefixUsingProperties(metricConfig, serverName, properties);

        if (currentNode.has(metricConfig.getAttr())) {
            metricValue = currentNode.findValue(metricConfig.getAttr()).asText();

            metricValue = convertMemoryStringToDouble(metricValue).toString();
            if (metricValue != null) {
                Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                if (metricConfig.getAlias() != null) {
                    metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue), metricPrefix, propertiesMap);
                } else {
                    metric = new Metric(metricConfig.getAttr(), String.valueOf(metricValue), metricPrefix, propertiesMap);
                }
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());
            }
        }
        if (metric != null && metric.getMetricPath() != null) {
            allMetrics.put(metric.getMetricPath(), metric);
        }
    }
}