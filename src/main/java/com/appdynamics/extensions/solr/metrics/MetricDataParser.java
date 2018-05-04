package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.solr.utils.MetricUtils.convertMemoryStringToDouble;
import static com.appdynamics.extensions.solr.utils.Constants.JSONMAP;
import static com.appdynamics.extensions.solr.utils.Constants.JSONLIST;

/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

public class MetricDataParser {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private List<Metric> metrics = new ArrayList<Metric>();
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();

    public MetricDataParser(MonitorContextConfiguration monitorContextConfiguration) {
        this.monitorContextConfiguration = monitorContextConfiguration;
    }

    Map<String, Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper objectMapper, String serverName, List<Map<String, String>> metricReplacer) {
        if (nodes != null) {
            if (stat.getStructure() != null) {
                if (stat.getStructure().toString().equals(JSONMAP)) {
                    ArrayList<?> arrayOfNodes = (ArrayList<?>) objectMapper.convertValue(nodes, List.class);
                    Map<String, Object> mapOfNodes = MetricUtils.mapOfArrayList(arrayOfNodes);

                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(getMetricFromMap(mapOfNodes, metricConfig, stat, serverName, objectMapper, metricReplacer));
                    }
                } else if (stat.getStructure().toString().equals(JSONLIST)) {
                    JsonNode newNode = MetricUtils.getJsonNode(stat, nodes);
                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(getMetricFromJson(metricConfig, stat, newNode, objectMapper, serverName, metricReplacer));
                    }

                }
            } else {
                logger.debug("No structure defined in the stat. ");
            }
        } else {

            logger.debug("Empty JSON Node returned for server: {} and alias: {}", serverName, stat.getAlias());
        }
        return allMetrics;
    }


    private Metric getMetricFromMap(Map<String, Object> mapOfNodes, MetricConfig metricConfig, Stat stat, String serverName, ObjectMapper objectMapper, List<Map<String, String>> metricReplacer) {
        Metric metric = null;
        if (!MetricUtils.checkForEmptyAttribute(metricConfig)) {
            String metricValue = getValueFromMap(mapOfNodes, metricConfig, stat);
            Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
            String metricPrefix = getMetricPrefix(metricConfig, stat, serverName, metricReplacer);
            metric = new Metric(metricConfig.getAlias(), metricValue, metricPrefix, propertiesMap);
        }

        allMetrics.put(metric.getMetricPath(), metric);
        return metric;

    }

    private String getValueFromMap(Map<String, Object> mapOfNodes, MetricConfig metricConfig, Stat stat) {
        String value = "";
        if (stat.getCategory() != null) {
            if (mapOfNodes.get(stat.getCategory()) != null) {
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getCategory());
            }
        }

        if (stat.getSubcategory() != null) {
            if (mapOfNodes.get(stat.getSubcategory()) != null) {
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getSubcategory());
            }
        }

        if (stat.getMetricSection() != null) {
            if (mapOfNodes.get(stat.getMetricSection()) != null) {
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getMetricSection());
            }
        }

        if (metricConfig.getAttr() != null) {
            if (mapOfNodes.get(metricConfig.getAttr()) != null) {
                value = mapOfNodes.get(metricConfig.getAttr()).toString();
            }
        }
        return value;
    }

    private Metric getMetricFromJson(MetricConfig metricConfig, Stat stat, JsonNode currentNode, ObjectMapper objectMapper, String serverName, List<Map<String, String>> metricReplacer) {
        Metric metric = null;
        String metricValue;

        String metricPrefix = getMetricPrefix(metricConfig, stat, serverName, metricReplacer);

        if (currentNode.has(metricConfig.getAttr())) {
            metricValue = currentNode.findValue(metricConfig.getAttr()).asText();

            metricValue = convertMemoryStringToDouble(metricValue).toString();

            if (metricValue != null) {
                Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue), metricPrefix, propertiesMap);
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());
            }
        }
        allMetrics.put(metric.getMetricPath(), metric);

        return metric;
    }

    private String getMetricPrefix(MetricConfig metricConfig, Stat stat, String serverName, List<Map<String, String>> metricReplacer) {
        String metricPrefix = "";
        if (monitorContextConfiguration.getMetricPrefix() != null) {
            metricPrefix += monitorContextConfiguration.getMetricPrefix();
        }
        if (serverName != null) {
            metricPrefix += "|" + serverName;
        }
        if (stat.getAlias() != null) {
            metricPrefix += "|" + stat.getAlias();
        }
        if (stat.getCategory() != null) {
            metricPrefix += "|" + stat.getCategory();
        }
        if (stat.getSubcategory() != null) {
            metricPrefix += "|" + stat.getSubcategory();
        }
        if (metricConfig.getAlias() != null) {
            metricPrefix += "|" + metricConfig.getAlias();
        } else {
            metricPrefix += "|" + metricConfig.getAttr();
        }

        metricPrefix = MetricUtils.replaceCharacter(metricPrefix, metricReplacer);

        return metricPrefix;
    }


}
