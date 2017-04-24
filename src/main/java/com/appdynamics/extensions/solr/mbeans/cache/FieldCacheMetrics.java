package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FieldCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(FieldCacheMetrics.class);

    public FieldCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    public Map<String, BigDecimal> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String fieldCachePath = metricPath + "FieldValueCache" + METRIC_SEPARATOR;

        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");
        Map<String, BigDecimal> fieldCacheMetrics = new HashMap<String, BigDecimal>();

        if (!fieldValueCacheStats.isMissingNode()) {
            fieldCacheMetrics.put(fieldCachePath + "HitRatio %", SolrUtils.convertDoubleToBigDecimal(SolrUtils.multipyBy
                    (fieldValueCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            fieldCacheMetrics.put(fieldCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToBigDecimal(SolrUtils
                    .multipyBy(fieldValueCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            fieldCacheMetrics.put(fieldCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToBigDecimal(fieldValueCacheStats.path("size").asDouble()));

        } else {
            logger.info("fieldValueCache is disabled in solrconfig.xml");
        }
        return fieldCacheMetrics;
    }
}
