package com.appdynamics.extensions.solr.Memory;

import com.appdynamics.extensions.solr.Cache.QueryCacheMetricsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SystemMemoryMetricsPopulator {
    private String collection;
    private static final String METRIC_SEPARATOR = "|";
    public static final Logger logger = LoggerFactory.getLogger(QueryCacheMetricsPopulator.class);
    private JsonNode jsonNode;

    public SystemMemoryMetricsPopulator (JsonNode jsonNode, String collection) {
        this.collection = collection;
        this.jsonNode = jsonNode;
    }

    public Map<String, String> populate () throws IOException {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "MEMORY"
                + METRIC_SEPARATOR;
        String systemPath = metricPath + "System" + METRIC_SEPARATOR;
        Map<String, String> systemMemoryMetrics = new HashMap<String, String>();

        if (jsonNode != null) {
            JsonNode memoryMBeansNode = jsonNode.path("system");
            if (!memoryMBeansNode.isMissingNode()) {
                systemMemoryMetrics.put(systemPath + "Free Physical Memory(MB)", memoryMBeansNode.path
                        ("freePhysicalMemorySize").asText());
                systemMemoryMetrics.put(systemPath + "Total Physical Memory (MB)", memoryMBeansNode.path
                        ("totalPhysicalMemorySize").asText());
                systemMemoryMetrics.put(systemPath + "Committed Virtual Memory (MB)", memoryMBeansNode.path
                        ("committedVirtualMemorySize").asText());
                systemMemoryMetrics.put(systemPath + "Free Swap Size (MB)", memoryMBeansNode.path
                        ("freeSwapSpaceSize").asText());
                systemMemoryMetrics.put(systemPath + "Total Swap Size (MB)", memoryMBeansNode.path
                        ("totalSwapSpaceSize").asText());
                systemMemoryMetrics.put(systemPath + "Open File Descriptor Count", memoryMBeansNode.path
                        ("openFileDescriptorCount").asText());
                systemMemoryMetrics.put(systemPath + "Max File Descriptor Count", memoryMBeansNode.path
                        ("maxFileDescriptorCount").asText());
            } else {
                logger.error("Missing json node while retrieving system memory stats");
            }
        }
        return systemMemoryMetrics;
    }
}

