package com.appdynamics.extensions.solr.Memory;

import com.appdynamics.extensions.solr.Cache.QueryCacheMetricsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/3/16.
 */
public class JVMMemoryMetricsPopulator  {
    private JsonNode jsonNode;
    private String collection;
    private static final String METRIC_SEPARATOR = "|";
    private static final Logger logger = LoggerFactory.getLogger(QueryCacheMetricsPopulator.class);

    public JVMMemoryMetricsPopulator (JsonNode jsonNode, String collection) {
        this.jsonNode = jsonNode;
        this.collection = collection;
    }

    public Map<String, String> populate () throws IOException {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "MEMORY"
                + METRIC_SEPARATOR;
        String jvmPath = metricPath + "JVM" + METRIC_SEPARATOR;
        Map<String, String> jvmMemoryMetrics = new HashMap<String, String>();

        if (jsonNode != null) {
            JsonNode jvmMBeansNode = jsonNode.path("jvm").path("memory");
            if (!jvmMBeansNode.isMissingNode()) {
                jvmMemoryMetrics.put(jvmPath + "Used (MB)", jvmMBeansNode.path("used").asText());
                jvmMemoryMetrics.put(jvmPath + "Free (MB)", jvmMBeansNode.path("free").asText());
                jvmMemoryMetrics.put(jvmPath + "Total (MB)", jvmMBeansNode.path("total").asText());
            }

            if (logger.isDebugEnabled()) {
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Used (MB)"));
                logger.debug("Used = " + jvmMemoryMetrics.get(jvmPath + "Free (MB)"));
            }
        }
        return jvmMemoryMetrics;
    }
}
