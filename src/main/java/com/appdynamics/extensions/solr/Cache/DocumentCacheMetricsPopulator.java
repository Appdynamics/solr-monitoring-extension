package com.appdynamics.extensions.solr.Cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashMap;
import java.util.Map;

public class DocumentCacheMetricsPopulator {
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    public static final Logger logger = LoggerFactory.getLogger(QueryCacheMetricsPopulator.class);
    private Map<String, JsonNode> solrMBeansHandlersMap;
    private String collection;

    public DocumentCacheMetricsPopulator (Map<String, JsonNode> solrMBeansHandlersMap, String collection) {
        this.solrMBeansHandlersMap = solrMBeansHandlersMap;
        this.collection = collection;
    }

    public Map<String, String> populate () {
        Map<String, String> documentCacheMetrics = new HashMap<String, String>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String documentCachePath = metricPath + "DocumentCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");

        if (!documentCacheStats.isMissingNode()) {
            documentCacheMetrics.put(documentCachePath + "HitRatio %", String.valueOf(Math.round(SolrUtils.multipyBy
                    (documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER))));
            documentCacheMetrics.put(documentCachePath + "HitRatioCumulative %", String.valueOf(Math.round(SolrUtils
                    .multipyBy(documentCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER))));
            documentCacheMetrics.put(documentCachePath + "CacheSize (Bytes)", String.valueOf(Math.round
                    (documentCacheStats.path("size").asDouble())));
        } else {
            logger.info("documentCache is disabled in solrconfig.xml");
        }
        return documentCacheMetrics;
    }
}
