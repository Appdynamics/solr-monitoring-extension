/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.mbeans.cache;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class FilterCacheMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final int PERCENT_MULTIPLIER = 100;
    private static final Logger logger = LoggerFactory.getLogger(FilterCacheMetrics.class);

    public FilterCacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    public Map<String, BigDecimal> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, BigDecimal> filterCacheMetrics = new HashMap<String, BigDecimal>();
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CACHE" +
                METRIC_SEPARATOR;
        String filterCachePath = metricPath + "FilterCache" + METRIC_SEPARATOR;
        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode filterCacheStats = cacheNode.path("filterCache").path("stats");

        if (!filterCacheStats.isMissingNode()) {
            filterCacheMetrics.put(filterCachePath + "HitRatio %", SolrUtils.convertDoubleToBigDecimal(SolrUtils.multipyBy
                    (filterCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER)));
            filterCacheMetrics.put(filterCachePath + "HitRatioCumulative %", SolrUtils.convertDoubleToBigDecimal(SolrUtils
                    .multipyBy(filterCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER)));
            filterCacheMetrics.put(filterCachePath + "CacheSize (Bytes)", SolrUtils.convertDoubleToBigDecimal(filterCacheStats.path("size").asDouble()));
        } else {
            logger.info("filterCache is disabled in solrconfig.xml");
        }
        return filterCacheMetrics;
    }
}
