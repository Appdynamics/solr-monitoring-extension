package com.appdynamics.extensions.solr.memory;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class JVMMemoryMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final Logger logger = LoggerFactory.getLogger(JVMMemoryMetrics.class);

    JVMMemoryMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, BigDecimal> populateStats(JsonNode jsonNode) {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "MEMORY"
                + METRIC_SEPARATOR;
        String jvmPath = metricPath + "JVM" + METRIC_SEPARATOR;
        Map<String, BigDecimal> jvmMemoryMetrics = new HashMap<String, BigDecimal>();

        if (jsonNode != null) {
            JsonNode jvmMBeansNode = jsonNode.path("jvm").path("memory");
            if (!jvmMBeansNode.isMissingNode()) {
                jvmMemoryMetrics.put(jvmPath + "Used (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("used").asText())));
                jvmMemoryMetrics.put(jvmPath + "Free (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("free").asText())));
                jvmMemoryMetrics.put(jvmPath + "Total (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertMemoryStringToDouble(jvmMBeansNode.path("total").asText())));
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Used (MB)"));
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Free (MB)"));
            }
        }
        return jvmMemoryMetrics;
    }
}
