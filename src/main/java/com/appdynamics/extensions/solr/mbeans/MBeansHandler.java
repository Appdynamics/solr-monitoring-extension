package com.appdynamics.extensions.solr.mbeans;

import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.helpers.HttpHelper;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.appdynamics.extensions.solr.mbeans.cache.CacheMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Maps;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Collects metrics for Core,Cache and Query
 */

public class MBeansHandler {

    private CloseableHttpClient httpClient;
    private static String MBEANS_PATH = "/%s/admin/mbeans?stats=true&wt=json";
    private static final Logger logger = LoggerFactory.getLogger(MBeansHandler.class);

    public MBeansHandler(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public Map<String, Long> populateStats(Core core, String contextRoot, String serverUrl) {
        Map<String,Long> mBeansMetrics = Maps.newHashMap();
        String url = buildUrl(core, contextRoot, serverUrl);
        Map<String, JsonNode> solrStatsMap = buildSolrMBeansMap(core, url);
        //core metrics
        mBeansMetrics.putAll(new CoreMetrics(core.getName()).populateStats(solrStatsMap));
        //query metrics
        for (String handler : core.getQueryHandlers()) {
            mBeansMetrics.putAll(new QueryMetrics(core.getName()).populateStats(solrStatsMap,handler));
        }
        //cache metrics
        mBeansMetrics.putAll(new CacheMetrics(core.getName()).populateStats(solrStatsMap));
        return mBeansMetrics;
    }

    private Map<String,JsonNode> buildSolrMBeansMap(Core core,String url){
        CloseableHttpResponse response = null;
        Map<String, JsonNode> solrStatsMap = null;
        try {
            logger.debug("fetching solr mbean handler map from {}",url);
            response = HttpHelper.doGet(httpClient, url);
            solrStatsMap = parseResponse(core, response);
        } catch (Exception e) {
            logger.error("Unable to get solr mbean handler map.", e.getMessage());
        } finally {
            HttpHelper.closeHttpResponse(response);
        }
        return solrStatsMap;
    }

    /**
     * The different metrics for cache, core, query are found in the json object following the name of the mBean handler.
     * For eg.
     * [
     *   "Core",
     *   {},
     *   "Cache",
     *   {}
     * ]
     * This method parses the response.
     * @param core
     * @param response
     * @return
     * @throws IOException
     */
    private Map<String, JsonNode> parseResponse(Core core, CloseableHttpResponse response) throws IOException {
        Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
        JsonNode jsonNode = SolrUtils.getJsonNode(response);
        if (jsonNode != null) {
            JsonNode solrMBeansNode = jsonNode.path("solr-mbeans");
            if (solrMBeansNode.isMissingNode()) {
                throw new IllegalArgumentException("Missing context while parsing solr-mbeans context json string for core " + core.getName());
            }
            for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
                solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
            }
        }
        return solrStatsMap;
    }

    private String buildUrl(Core core, String contextRoot, String serverUrl){
        StringBuilder url = new StringBuilder(serverUrl);
        url.append(contextRoot).append(String.format(MBEANS_PATH, core.getName()));
        return url.toString();
    }
}
