package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by bhuvnesh.kumar on 5/17/18.
 */
public class ProcessChildStats {
//
//    private static final Logger logger = LoggerFactory.getLogger(ProcessChildStats.class);
//    private MonitorContextConfiguration monitorContextConfiguration;
//    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();
//    private String serverName;
//    private List<Map<String, String>> metricReplacer;
//    private boolean isJsonMap;
//    private MetricDataParser metricParser;
//
//    ProcessChildStats(MonitorContextConfiguration monitorContextConfiguration, String serverName, List<Map<String, String>> metricReplacer, Boolean isJsonMap){
//        this.monitorContextConfiguration = monitorContextConfiguration;
//        this.metricParser = new MetricDataParser(monitorContextConfiguration);
//        this.serverName = serverName;
//        this.metricReplacer = metricReplacer;
//        this.isJsonMap = isJsonMap;
//    }
//
//     Map<String, Metric> startProcessingStats(Stat stat, JsonNode jsonNode) {
////        List<String> properties = new ArrayList<String>();
//        Map<String, String> propertiesMap = new LinkedHashMap<String, String>();
//        processStats(stat,jsonNode, propertiesMap);
//        return allMetrics;
//
//    }
//
//
//    private void processStats(Stat stat, JsonNode jsonNode, Map<String, String> properties ) {
//        if (!isChildStatNull(stat.getStats())) {
//            collectChildStats(stat, jsonNode, properties);
//        } else {
//            collectStats(stat, jsonNode,properties);
//        }
//
//
//    }
//
//    private void collectStats(Stat stat, JsonNode jsonNode,Map<String, String> properties) {
//        if (stat.getMetricConfig() != null) {
//            allMetrics.putAll(metricParser.parseNodeData(stat, jsonNode,  new ObjectMapper(), serverName, properties));
//        }
//    }
//
//
//    private boolean isChildStatNull(Stat[] stat) {
//        return stat == null;
//    }
//
//
//    private void collectChildStats(Stat stat, JsonNode jsonNode, Map<String, String> properties) {
//
//        for (Stat childStat : stat.getStats()) {
//            if (childStat != null ) {
//                properties.put(childStat.getRootElement(),childStat.getAlias());
//                if (stat.getRootElement() != null) {
//                    if (jsonNode.get(stat.getRootElement()) != null) {
//                        jsonNode = jsonNode.get(stat.getRootElement());
//                    }
//                }
//                processStats(childStat, jsonNode, properties);
//            } else {
//                collectStats(stat, jsonNode, properties);
//            }
//
//            properties.remove(childStat.getRootElement());
//        }
//    }
}
