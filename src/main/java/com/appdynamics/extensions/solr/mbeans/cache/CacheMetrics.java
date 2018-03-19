/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.mbeans.cache;

import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CacheMetrics {
    private String coreName;
    private static final Logger logger = LoggerFactory.getLogger(CacheMetrics.class);

    public CacheMetrics(String coreName) {
        this.coreName = coreName;
    }

    public Map<String, BigDecimal> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, BigDecimal> cacheMetrics = new HashMap<String, BigDecimal>();
        try {
            cacheMetrics.putAll(new DocumentCacheMetrics(coreName).populateStats(solrMBeansHandlersMap));
            cacheMetrics.putAll(new QueryCacheMetrics(coreName).populateStats(solrMBeansHandlersMap));
            cacheMetrics.putAll(new FieldCacheMetrics(coreName).populateStats(solrMBeansHandlersMap));
            cacheMetrics.putAll(new FilterCacheMetrics(coreName).populateStats(solrMBeansHandlersMap));
        } catch (Exception e) {
            logger.error("Error Retrieving cache Stats for " + coreName, e);
        }
        return cacheMetrics;
    }
}
