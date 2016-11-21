package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class DocumentCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheMetrics.class);


    DocumentCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, Long> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, Long> documentCacheMetrics = new HashMap<String, Long>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String documentCachePath = metricPath + "DocumentCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");
        if (!documentCacheStats.isMissingNode()) {
            documentCacheMetrics.put(documentCachePath + "HitRatio %", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy(documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "HitRatio %", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy
                            (documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToLong(SolrUtils
                            .multipyBy(documentCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToLong(documentCacheStats.path("size").asDouble()));
        } else {
            logger.info("documentCache is disabled in solrconfig.xml");
        }
        return documentCacheMetrics;
    }
}
