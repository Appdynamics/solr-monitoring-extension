package com.appdynamics.extensions.solr.Cache;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/3/16.
 */
public class CacheMetricsHandler {
    private Map<String, JsonNode> solrMBeansHandlersMap;
    private String core;

    public CacheMetricsHandler(Map<String, JsonNode> solrMBeansHandlersMap, String core) {
        this.core = core;
        this.solrMBeansHandlersMap = solrMBeansHandlersMap;
    }

    public Map<String, String> populate() {
        Map<String, String> cacheMetrics = new HashMap<String, String>();
        DocumentCacheMetricsPopulator documentCacheStatsPopulator = new DocumentCacheMetricsPopulator(solrMBeansHandlersMap, core);
        QueryCacheMetricsPopulator queryCacheMetricsPopulator = new QueryCacheMetricsPopulator(solrMBeansHandlersMap, core);
        FilterCacheMetricsPopulator filterCacheMetricsPopulator = new FilterCacheMetricsPopulator(solrMBeansHandlersMap, core);
        FieldCacheMetricsPopulator fieldCacheMetricsPopulator = new FieldCacheMetricsPopulator(solrMBeansHandlersMap, core);
        cacheMetrics.putAll(documentCacheStatsPopulator.populate());
        cacheMetrics.putAll(queryCacheMetricsPopulator.populate());
        cacheMetrics.putAll(filterCacheMetricsPopulator.populate());
        cacheMetrics.putAll(fieldCacheMetricsPopulator.populate());
        return cacheMetrics;
    }
}
