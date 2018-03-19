/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.mbeans;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class CoreMetrics {

    private static final Logger logger = LoggerFactory.getLogger(CoreMetrics.class);
    private static final String METRIC_SEPARATOR = "|";
    private String coreName;

    CoreMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, BigDecimal> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        Map<String, BigDecimal> coreMetrics = new HashMap<String, BigDecimal>();
        try {
            JsonNode node = solrMBeansHandlersMap.get("CORE");
            if (node != null) {
                JsonNode coreNode = node.path("searcher").path("stats");
                String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "CORE" + METRIC_SEPARATOR;
                if (!coreNode.isMissingNode()) {
                    coreMetrics.put(metricPath + "Number of Docs", SolrUtils.convertDoubleToBigDecimal(coreNode.path("numDocs").asDouble()));
                    coreMetrics.put(metricPath + "Max Docs", SolrUtils.convertDoubleToBigDecimal(coreNode.path("maxDocs").asDouble()));
                    coreMetrics.put(metricPath + "Deleted Docs", SolrUtils.convertDoubleToBigDecimal(coreNode.path("deletedDocs").asDouble()));
                    if (logger.isDebugEnabled()) {
                        logger.debug("Docs=" + coreMetrics.get("Number of Docs"));
                        logger.debug("Max Docs=" + coreMetrics.get("Max Docs"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error Retrieving core Stats for " + coreName, e);
        }
        return coreMetrics;
    }
}