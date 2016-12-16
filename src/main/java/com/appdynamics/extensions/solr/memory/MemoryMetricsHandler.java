package com.appdynamics.extensions.solr.memory;

import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.helpers.HttpHelper;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MemoryMetricsHandler {

    CloseableHttpClient httpClient;
    private static String MEMORY_PATH = "/%s/admin/system?stats=true&wt=json";
    private static final Logger logger = LoggerFactory.getLogger(MemoryMetricsHandler.class);

    public MemoryMetricsHandler(CloseableHttpClient httpClient){
        this.httpClient = httpClient;
    }


    public Map<String, Long> populateStats(Core core, String contextRoot, String serverUrl) {
        Map<String, Long> memoryMetrics = Maps.newHashMap();
        try{
            String url = buildUrl(core,contextRoot,serverUrl);
            JsonNode jsonNode = getData(core,url);
            memoryMetrics.putAll(new JVMMemoryMetrics(core.getName()).populateStats(jsonNode));
            memoryMetrics.putAll(new SystemMemoryMetrics(core.getName()).populateStats(jsonNode));
        }
        catch (Exception e) {
            logger.error("Error retrieving memory stats for " + core.getName(), e);
        }
        return memoryMetrics;
    }

    private JsonNode getData(Core core,String url){
        CloseableHttpResponse response = null;
        JsonNode jsonNode = null;
        try {
            logger.debug("fetching memory metrics from {}",url);
            response = HttpHelper.doGet(httpClient,url);
            if(response != null){
                jsonNode = SolrUtils.getJsonNode(response);
            }
        } catch (Exception e) {
            logger.error("Unable to fetch memory metrics for core " + core.getName(), e.getMessage());
        } finally {
            HttpHelper.closeHttpResponse(response);
        }
        return jsonNode;
    }

    private String buildUrl(Core core, String contextRoot, String serverUrl){
        StringBuilder url = new StringBuilder(serverUrl);
        url.append(contextRoot).append(String.format(MEMORY_PATH, core.getName()));
        return url.toString();
    }

}
