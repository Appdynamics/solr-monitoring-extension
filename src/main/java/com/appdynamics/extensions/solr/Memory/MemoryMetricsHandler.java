package com.appdynamics.extensions.solr.Memory;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/3/16.
 */
public class MemoryMetricsHandler {
    private CloseableHttpResponse response;
    private String collection;

    public MemoryMetricsHandler(CloseableHttpResponse response, String collection) {
        this.response = response;
        this.collection = collection;
    }

    public Map<String, String> populate() throws IOException {
        Map<String, String> memoryMetrics = new HashMap<String, String>();
        JsonNode jsonNode = SolrUtils.getJsonNode(response);
        JVMMemoryMetricsPopulator jvmMemoryMetricsPopulator = new JVMMemoryMetricsPopulator(jsonNode, collection);
        SystemMemoryMetricsPopulator systemMemoryMetricsPopulator = new SystemMemoryMetricsPopulator(jsonNode, collection);
        memoryMetrics.putAll(jvmMemoryMetricsPopulator.populate());
        memoryMetrics.putAll(systemMemoryMetricsPopulator.populate());
        return memoryMetrics;
    }
}
