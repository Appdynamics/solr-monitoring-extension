package com.appdynamics.extensions.solr.Cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/3/16.
 */
public class QueryCacheMetricsPopulator {
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    public static final Logger logger = LoggerFactory.getLogger(QueryCacheMetricsPopulator.class);
    private Map<String, JsonNode> solrMBeansHandlersMap;
    private String collection;

    public QueryCacheMetricsPopulator (Map<String, JsonNode> solrMBeansHandlersMap, String collection) {
        this.solrMBeansHandlersMap = solrMBeansHandlersMap;
        this.collection = collection;
    }

    public Map<String, String> populate () {
        Map<String, String> queryCacheMetrics = new HashMap<String, String>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String queryCachePath = metricPath + "QueryResultCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode queryResultCacheStats = cacheNode.path("queryResultCache").path("stats");

        if (!queryResultCacheStats.isMissingNode()) {
            queryCacheMetrics.put(queryCachePath + "HitRatio %", String.valueOf(Math.round(SolrUtils.multipyBy
                    (queryResultCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER))));
            queryCacheMetrics.put(queryCachePath + "HitRatioCumulative %", String.valueOf(Math.round(SolrUtils
                    .multipyBy(queryResultCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER))));
            queryCacheMetrics.put(queryCachePath + "CacheSize (Bytes)", String.valueOf(Math.round
                    (queryResultCacheStats.path("size").asDouble())));
        } else {
            logger.info("queryResultCache is disabled in solrconfig.xml");
        }
        return queryCacheMetrics;
    }
}
