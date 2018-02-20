/*
 * Copyright 2014 AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class DocumentCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(DocumentCacheMetrics.class);

    public DocumentCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    public Map<String, BigDecimal> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, BigDecimal> documentCacheMetrics = new HashMap<String, BigDecimal>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String documentCachePath = metricPath + "DocumentCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");
        if (!documentCacheStats.isMissingNode()) {
            documentCacheMetrics.put(documentCachePath + "HitRatio %", SolrUtils.convertDoubleToBigDecimal(SolrUtils.multipyBy(documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "HitRatio %", SolrUtils.convertDoubleToBigDecimal(SolrUtils.multipyBy
                    (documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToBigDecimal(SolrUtils
                    .multipyBy(documentCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            documentCacheMetrics.put(documentCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToBigDecimal(documentCacheStats.path("size").asDouble()));
        } else {
            logger.info("documentCache is disabled in solrconfig.xml");
        }
        return documentCacheMetrics;
    }
}
