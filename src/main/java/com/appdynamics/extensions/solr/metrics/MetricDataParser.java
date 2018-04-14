package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.util.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */
public class MetricDataParser {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private List<Metric> metrics = new ArrayList<Metric>();

    MetricDataParser(MonitorContextConfiguration monitorContextConfiguration){
        this.monitorContextConfiguration = monitorContextConfiguration;
    }

    List<Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper objectMapper, String serverName){
//        JsonNode currentNode;
        String root = stat.getRootElement();
        if(nodes != null){
            JsonNode newNode = nodes.get(root);

            if(newNode != null){
                String mbean = stat.getMbeanGroup().getName();

            }

//            if(newNode != null)
//            {
//                if(!newNode.isArray()){
//                    for(MetricConfig metricConfig : stat.getMetricConfig()){
//                        metrics.add(parseAndRetrieveMetric(metricConfig, stat, newNode, objectMapper, serverName));
//
//                    }
//                } else {
//                    for(JsonNode node: newNode){
//                        for(MetricConfig metricConfig: stat.getMetricConfig()){
//                            metrics.add(parseAndRetrieveMetric(metricConfig, stat, node, objectMapper, serverName));
//                        }
//                    }
//                }
//            }
//            for(JsonNode currentNode: nodes){
//                if(currentNode != null){
//                    if(!currentNode.isArray()){
//                        for(MetricConfig metricConfig : stat.getMetricConfig()){
//                            metrics.add(parseAndRetrieveMetric(metricConfig, stat, currentNode, objectMapper, serverName));
//
//                        }
//                    } else {
//                        for(JsonNode node: currentNode){
//                            for(MetricConfig metricConfig: stat.getMetricConfig()){
//                                metrics.add(parseAndRetrieveMetric(metricConfig, stat, node, objectMapper, serverName));
//                            }
//                        }
//                    }
//                }
//            }
        }

//        if(nodes != null){
//            currentNode = nodes.get(stat.getRootElement());
//            if(currentNode != null){
//                if(!currentNode.isArray()){
//                    for(MetricConfig metricConfig : stat.getMetricConfig()){
//                        metrics.add(parseAndRetrieveMetric(metricConfig, stat, currentNode, objectMapper, serverName));
//
//                    }
//                } else {
//                    for(JsonNode node: currentNode){
//                        for(MetricConfig metricConfig: stat.getMetricConfig()){
//                            metrics.add(parseAndRetrieveMetric(metricConfig, stat, node, objectMapper, serverName));
//                        }
//                    }
//                }
//            }
//        }else {
//            logger.debug("{} metrics are not available for server: {}. Skipping.", stat.getRootElement(), serverName);
//        }
        return metrics;
    }


    private Metric parseAndRetrieveMetric(MetricConfig metricConfig, Stat stat, JsonNode currentNode, ObjectMapper objectMapper, String serverName){
        Metric metric = null;
        String metricValue;
        if(currentNode.has(metricConfig.getAttr())){
            metricValue = currentNode.findValue(metricConfig.getAttr()).asText();
            if(metricValue != null){
                String prefix = StringUtils.trim(stat.getAlias(), "|");
                String name = (currentNode.has("name")) ? currentNode.get("name").asText() + "|" : "";
                Map<String, String > propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue), monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|" + prefix + "|" + name + metricConfig.getAlias(), propertiesMap);
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());
            }
        }
        return metric;
    }



}
