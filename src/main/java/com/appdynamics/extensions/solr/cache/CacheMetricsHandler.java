package com.appdynamics.extensions.solr.cache;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;

public class CacheMetricsHandler {
    private Map<String, JsonNode> solrMBeansHandlersMap;
    private String core;

    public CacheMetricsHandler(Map<String, JsonNode> solrMBeansHandlersMap, String core) {
        this.core = core;
        this.solrMBeansHandlersMap = solrMBeansHandlersMap;
    }

    public Map<String, Long> populate() {
        Map<String, Long> cacheMetrics = new HashMap<String, Long>();
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
