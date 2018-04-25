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

import java.util.ArrayList;
import java.util.HashMap;
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
        if(nodes != null) {
//            JsonNode newNode = nodes.findValue(stat.getCategory());
//            JsonNode subNewNode = newNode.findValue(stat.getSubcategory());

            if (stat.getStructure().toString().equals("jsonMap")) {
                ArrayList<?> arrayOfNodes = (ArrayList<?>) objectMapper.convertValue(nodes, List.class);
                Map<String, Object> mapOfNodes = mapOfArrayList(arrayOfNodes);

                for(MetricConfig metricConfig: stat.getMetricConfig()){
                    metrics.add(getMetricFromMap(mapOfNodes, metricConfig, stat, serverName, objectMapper));
                }
            } else if(stat.getStructure().toString().equals("jsonList")) {
                JsonNode newNode = nodes.get(stat.getRootElement());
                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(parseAndRetrieveMetric(metricConfig, stat, newNode, objectMapper, serverName));

                }

            }

        }
        return metrics;
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

    private Metric getMetricFromMap(Map<String, Object> mapOfNodes, MetricConfig metricConfig, Stat stat, String serverName, ObjectMapper objectMapper){
        Metric metric = null;

//        Map<String, ?> catergoryMap = (Map<String, ?>) mapOfNodes.get(stat.getCategory());
//        Map<String, ?> subCategoryMap = ((Map<String, ?>) catergoryMap.get(stat.getSubcategory()));
//        Map<String, ?> metricSectionMap = (Map<String, ?>) subCategoryMap.get(stat.getMetricSection());
//        String metricValue = metricSectionMap.get(metricConfig.getAttr()).toString();

        String value = (((Map<String, ?>) (((Map<String, ?>)(((Map<String, ?>) mapOfNodes.get(stat.getCategory())).get(stat.getSubcategory()))).get(stat.getMetricSection()))).get(metricConfig.getAttr())).toString();
        Map<String, String > propertiesMap = objectMapper.convertValue(metricConfig, Map.class);

        String metricPrefix = getMetricPrefix(metricConfig, stat, serverName);
        metric = new Metric(metricConfig.getAlias(), value, metricPrefix, propertiesMap);
        return metric;

    }


    private Metric parseAndRetrieveMetric(MetricConfig metricConfig, Stat stat, JsonNode currentNode, ObjectMapper objectMapper, String serverName){
        Metric metric = null;
        String metricValue;

        String metricPrefix = getMetricPrefix(metricConfig, stat, serverName);

        if(currentNode.has(metricConfig.getAttr())){
            metricValue = currentNode.findValue(metricConfig.getAttr()).asText();
            if(metricValue != null){


//                String prefix = StringUtils.trim(stat.getAlias(), "|");
//                if(stat.getCategory() != null && stat.getSubcategory()!=null) {
//                    prefix += stat.getCategory()+"|"+stat.getSubcategory();
//                }
//                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue), monitorContextConfiguration.getMetricPrefix() + "|" + serverName + "|" + prefix + "|" +
//                        metricConfig.getAlias(), propertiesMap);


                Map<String, String > propertiesMap = objectMapper.convertValue(metricConfig, Map.class);
                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue), metricPrefix, propertiesMap);
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());

            }
        }

        return metric;
    }

    private String getMetricPrefix(MetricConfig metricConfig, Stat stat, String serverName) {
        String metricPrefix = "";

        if(monitorContextConfiguration.getMetricPrefix() != null){
            metricPrefix += monitorContextConfiguration.getMetricPrefix();
        }

        if(serverName != null){
            metricPrefix += "|" + serverName ;
        }

        if(stat.getAlias() != null){
            metricPrefix += "|" + stat.getAlias();
        }

        if(stat.getCategory() != null){
            metricPrefix += "|" + stat.getCategory();

        }

        if(stat.getSubcategory() != null){
            metricPrefix += "|" + stat.getSubcategory();

        }

        if(metricConfig.getAlias() != null){
            metricPrefix += "|" + metricConfig.getAlias();

        }
        return metricPrefix;
    }


}
