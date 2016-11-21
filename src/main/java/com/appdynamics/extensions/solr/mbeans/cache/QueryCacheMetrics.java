package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class QueryCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(QueryCacheMetrics.class);

    QueryCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, Long> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, Long> queryCacheMetrics = new HashMap<String, Long>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String queryCachePath = metricPath + "QueryResultCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode queryResultCacheStats = cacheNode.path("queryResultCache").path("stats");

        if (!queryResultCacheStats.isMissingNode()) {
            queryCacheMetrics.put(queryCachePath + "HitRatio %", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy
                    (queryResultCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            queryCacheMetrics.put(queryCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToLong(SolrUtils
                    .multipyBy(queryResultCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            queryCacheMetrics.put(queryCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToLong(queryResultCacheStats.path("size").asDouble()));
        } else {
            logger.info("queryResultCache is disabled in solrconfig.xml");
        }
        return queryCacheMetrics;
    }
}
