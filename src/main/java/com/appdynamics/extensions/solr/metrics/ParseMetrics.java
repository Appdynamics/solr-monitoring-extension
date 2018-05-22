package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.solr.utils.Constants.JSONLIST;
import static com.appdynamics.extensions.solr.utils.Constants.JSONMAP;
import static com.appdynamics.extensions.solr.utils.MetricUtils.convertMemoryStringToDouble;

/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

public class ParseMetrics {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();

    public ParseMetrics(MonitorContextConfiguration monitorContextConfiguration) {
        this.monitorContextConfiguration = monitorContextConfiguration;
    }



    public Map<String, Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper objectMapper, String serverName, List<Map<String, String>> metricReplacer, Boolean isJsonMap, Map<String, String> properties) {
        if (nodes != null) {

                    JsonNode newNode = MetricUtils.getJsonNode(stat, nodes);
                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        getMetricValueFromJson(metricConfig, newNode, objectMapper, serverName, metricReplacer, properties);

                }

        } else {

            logger.debug("Empty JSON Node returned for server: {} and alias: {}", serverName, stat.getAlias());
        }
        return allMetrics;
    }

    private String getMetricPrefixUsingProperties(MetricConfig metricConfig, String serverName, List<Map<String, String>> metricReplacer, Map<String, String> properties){
        String metricPrefix = "";
        if (monitorContextConfiguration.getMetricPrefix() != null) {
            metricPrefix += monitorContextConfiguration.getMetricPrefix() + "|";
        }
        if (serverName != null) {
            metricPrefix += serverName + "|" ;
        }
        for( String prop: properties.keySet()){
            if(properties.get(prop) != null){
                metricPrefix+= properties.get(prop).toString() + "|";
            } else
                metricPrefix+= prop + "|";
        }
        if (metricConfig.getAlias() != null) {
            metricPrefix +=  metricConfig.getAlias();
        } else {
            metricPrefix +=  metricConfig.getAttr();
        }

        metricPrefix = MetricUtils.replaceCharacter(metricPrefix, metricReplacer);

        return metricPrefix;
    }


    private void getMetricValueFromJson(MetricConfig metricConfig, JsonNode currentNode, ObjectMapper objectMapper, String serverName, List<Map<String, String>> metricReplacer, Map<String, String> properties) {
        Metric metric = null;
        String metricValue;

        String metricPrefix = getMetricPrefixUsingProperties(metricConfig,  serverName, metricReplacer, properties);

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
    }

}
