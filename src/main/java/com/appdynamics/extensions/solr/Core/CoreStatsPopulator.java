/**
 * Copyright 2013 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.solr.Core;

import com.appdynamics.extensions.solr.SolrMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CoreStatsPopulator {

    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private static final String METRIC_SEPARATOR = "|";
    private Map<String, String> coreMetrics;
    private String collection;

    public CoreStatsPopulator(String collection) {
        this.collection = collection;
    }


    public Map<String, String> populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        JsonNode node = solrMBeansHandlersMap.get("CORE");
        if (node != null) {
            coreMetrics = new HashMap<String, String>();
            JsonNode coreNode = node.path("searcher").path("stats");
            String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CORE" + METRIC_SEPARATOR;
            if (!coreNode.isMissingNode()) {
                coreMetrics.put(metricPath + "Number of Docs", String.valueOf(Math.round(coreNode.path("numDocs").asDouble())));
                coreMetrics.put(metricPath + "Max Docs", String.valueOf(Math.round(coreNode.path("maxDocs").asDouble())));
                coreMetrics.put(metricPath + "Deleted Docs", String.valueOf(Math.round(coreNode.path("deletedDocs").asDouble())));
                if (logger.isDebugEnabled()) {
                    logger.debug("Docs=" + coreMetrics.get("Number of Docs"));
                    logger.debug("Max Docs=" + coreMetrics.get("Max Docs"));
                }
            }
        }
        return coreMetrics;
    }
}