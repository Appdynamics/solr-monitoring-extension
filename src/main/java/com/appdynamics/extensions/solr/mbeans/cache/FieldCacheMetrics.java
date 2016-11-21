package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class FieldCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(FieldCacheMetrics.class);

    FieldCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, Long> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String fieldCachePath = metricPath + "FieldValueCache" + METRIC_SEPARATOR;

        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");
        Map<String, Long> fieldCacheMetrics = new HashMap<String, Long>();

        if (!fieldValueCacheStats.isMissingNode()) {
            fieldCacheMetrics.put(fieldCachePath + "HitRatio %", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy
                            (fieldValueCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            fieldCacheMetrics.put(fieldCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToLong(SolrUtils
                            .multipyBy(fieldValueCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            fieldCacheMetrics.put(fieldCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToLong(fieldValueCacheStats.path("size").asDouble()));

        } else {
            logger.info("fieldValueCache is disabled in solrconfig.xml");
        }
        return fieldCacheMetrics;
    }
}
