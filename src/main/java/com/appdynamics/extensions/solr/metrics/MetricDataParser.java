package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.appdynamics.extensions.solr.metrics.MetricUtils;

import static com.appdynamics.extensions.solr.metrics.MetricUtils.*;

/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */
public class MetricDataParser {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private List<Metric> metrics = new ArrayList<Metric>();

    MetricDataParser(MonitorContextConfiguration monitorContextConfiguration) {
        this.monitorContextConfiguration = monitorContextConfiguration;
    }

    List<Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper objectMapper, String serverName, List<Map<String, String>> metricReplacer) {
        if (nodes != null) {
            if(stat.getStructure() != null) {
                if (stat.getStructure().toString().equals("jsonMap")) {
                    ArrayList<?> arrayOfNodes = (ArrayList<?>) objectMapper.convertValue(nodes, List.class);
                    Map<String, Object> mapOfNodes = MetricUtils.mapOfArrayList(arrayOfNodes);

                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(getMetricFromMap(mapOfNodes, metricConfig, stat, serverName, objectMapper, metricReplacer));
                    }
                } else if (stat.getStructure().toString().equals("jsonList")) {
                    JsonNode newNode = nodes;

                    if(stat.getRootElement() != null){
                         newNode = nodes.get(stat.getRootElement());
                    }

                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(getMetricFromJson(metricConfig, stat, newNode, objectMapper, serverName, metricReplacer));
                    }

                }
            } else{
                logger.error("No structure defined in the stat. ");
            }
        }
        return metrics;
    }



    private Metric getMetricFromMap(Map<String, Object> mapOfNodes, MetricConfig metricConfig, Stat stat, String serverName, ObjectMapper objectMapper, List<Map<String, String>> metricReplacer) {
        Metric metric = null;
        boolean check = false;
        if(!MetricUtils.checkForEmptyAttribute(metricConfig)) {

            String metricValue = getValueFromMap(mapOfNodes, metricConfig, stat);


//            String value = (((Map<String, ?>) (((Map<String, ?>) (((Map<String, ?>) mapOfNodes.get(stat.getCategory())).get(stat.getSubcategory()))).get(stat.getMetricSection()))).get(metricConfig.getAttr())).toString();
//
//            if(metricValue.equals(value)){
//                check = true;
//            }

            Map<String, String> propertiesMap = objectMapper.convertValue(metricConfig, Map.class);

            String metricPrefix = getMetricPrefix(metricConfig, stat, serverName, metricReplacer);
            metric = new Metric(metricConfig.getAlias(), metricValue, metricPrefix, propertiesMap);
        }
        return metric;

    }

    private String getValueFromMap(Map<String, Object> mapOfNodes, MetricConfig metricConfig, Stat stat) {
        String value = "";
        if(stat.getCategory()!= null){
            if(mapOfNodes.get(stat.getCategory()) != null){
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getCategory());
            }
        }

        if(stat.getSubcategory()!= null){
            if (mapOfNodes.get(stat.getSubcategory()) != null){
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getSubcategory());
            }
        }

        if(stat.getMetricSection()!= null){
            if (mapOfNodes.get(stat.getMetricSection()) != null){
                mapOfNodes = (Map<String, Object>) mapOfNodes.get(stat.getMetricSection());
            }
        }

//        Map<String, ?> categoryMap = (Map<String, ?>) mapOfNodes.get(stat.getCategory());
//        Map<String, ?> subCategoryMap = (Map<String, ?>) categoryMap.get(stat.getSubcategory());
//        Map<String, ?> metricSectionMap = (Map<String, ?>) subCategoryMap.get(stat.getMetricSection());
//        return metricSectionMap.get(metricConfig.getAttr()).toString();

        if(metricConfig.getAttr() != null){
            if(mapOfNodes.get(metricConfig.getAttr()) != null){
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
        } else{
            metricPrefix += "|" + metricConfig.getAttr();
        }

        metricPrefix = MetricUtils.replaceCharacter(metricPrefix, metricReplacer);

        return metricPrefix;
    }


}
