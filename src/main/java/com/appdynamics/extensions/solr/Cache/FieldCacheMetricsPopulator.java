package com.appdynamics.extensions.solr.Cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class FieldCacheMetricsPopulator {
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;

    private Map<String, JsonNode> solrMBeansHandlersMap;
    private String collection;
    public static final Logger logger = LoggerFactory.getLogger(FieldCacheMetricsPopulator.class);

    public FieldCacheMetricsPopulator (Map<String, JsonNode> solrMBeansHandlersMap, String collection) {
        this.collection = collection;
        this.solrMBeansHandlersMap = solrMBeansHandlersMap;
    }

    public Map<String, String> populate () {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String fieldCachePath = metricPath + "FieldValueCache" + METRIC_SEPARATOR;

        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");
        Map<String, String> fieldCacheMetrics = new HashMap<String, String>();

        if (!fieldValueCacheStats.isMissingNode()) {
            fieldCacheMetrics.put(fieldCachePath + "HitRatio %", String.valueOf(Math.round(SolrUtils.multipyBy
                    (fieldValueCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER))));
            fieldCacheMetrics.put(fieldCachePath + "HitRatioCumulative %", String.valueOf(Math.round(SolrUtils
                    .multipyBy(fieldValueCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER))));
            fieldCacheMetrics.put(fieldCachePath + "CacheSize (Bytes)", String.valueOf(Math.round
                    (fieldValueCacheStats.path("size").asDouble())));

        } else {
            logger.info("fieldValueCache is disabled in solrconfig.xml");
        }
        return fieldCacheMetrics;
    }
}
