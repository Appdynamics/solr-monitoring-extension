package com.appdynamics.extensions.solr.memory;

import com.appdynamics.extensions.solr.cache.QueryCacheMetricsPopulator;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JVMMemoryMetricsPopulator  {
    private JsonNode jsonNode;
    private String collection;
    private static final String METRIC_SEPARATOR = "|";
    private static final Logger logger = LoggerFactory.getLogger(JVMMemoryMetricsPopulator.class);

    public JVMMemoryMetricsPopulator (JsonNode jsonNode, String collection) {
        this.jsonNode = jsonNode;
        this.collection = collection;
    }

    public Map<String, Long> populate () throws IOException {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "MEMORY"
                + METRIC_SEPARATOR;
        String jvmPath = metricPath + "JVM" + METRIC_SEPARATOR;
        Map<String, Long> jvmMemoryMetrics = new HashMap<String, Long>();

        if (jsonNode != null) {
            JsonNode jvmMBeansNode = jsonNode.path("jvm").path("memory");
            if (!jvmMBeansNode.isMissingNode()) {
                jvmMemoryMetrics.put(jvmPath + "Used (MB)", SolrUtils.convertDoubleToLong(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("used").asText())));
                jvmMemoryMetrics.put(jvmPath + "Free (MB)",  SolrUtils.convertDoubleToLong(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("free").asText())));
                jvmMemoryMetrics.put(jvmPath + "Total (MB)", SolrUtils.convertDoubleToLong(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("total").asText())));
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Used (MB)"));
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Free (MB)"));
            }
        }
        return jvmMemoryMetrics;
    }
}
