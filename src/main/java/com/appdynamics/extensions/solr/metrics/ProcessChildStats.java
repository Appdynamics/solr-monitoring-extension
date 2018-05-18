package com.appdynamics.extensions.solr.metrics;

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 5/17/18.
 */
public class ProcessChildStats {
    private static final Logger logger = LoggerFactory.getLogger(ProcessChildStats.class);
    private MetricDataParser metricDataParser;
    private MonitorContextConfiguration monitorContextConfiguration;
    private Map<String, Metric> allMetrics = new HashMap<String, Metric>();
    private String serverName;
    private List<Map<String, String>> metricReplacer;


    ProcessChildStats(MonitorContextConfiguration monitorContextConfiguration, String serverName, List<Map<String, String>> metricReplacer){
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.serverName = serverName;
        this.metricReplacer = metricReplacer;
    }


    protected Map<String, Metric> processStats(Stat stat, JsonNode jsonNode) {

        if (!isChildStatNull(stat.getStats())) {
            collectChildStats(stat, jsonNode);
        } else {
            collectStats(stat, jsonNode);
        }

        return allMetrics;

    }

    private void collectStats(Stat stat, JsonNode jsonNode) {
        if (stat.getMetricConfig() != null) {
            allMetrics.putAll(metricDataParser.parseNodeData(stat, jsonNode, new ObjectMapper(), serverName, metricReplacer));
        }
    }


    private boolean isChildStatNull(Stat[] stat) {
        return stat == null;
    }

    private void collectChildStats(Stat stat, JsonNode jsonNode) {
        for (Stat childStat : stat.getStats()) {
            if (childStat != null) {
                if (stat.getRootElement() != null) {
                    if (jsonNode.get(stat.getRootElement()) != null) {
                        jsonNode = jsonNode.get(stat.getRootElement());
                    } else{
                        logger.debug("no root element found for {}", childStat.getAlias());
                    }
                }
                processStats(childStat, jsonNode);
            } else {
                collectStats(stat, jsonNode);
            }
        }
    }
}
