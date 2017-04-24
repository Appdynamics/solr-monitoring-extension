/**
 * Copyright 2013 AppDynamics, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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