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

package com.appdynamics.extensions.solr.Query;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.appdynamics.extensions.solr.SolrMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class QueryStatsPopulator {
    private static final String METRIC_SEPARATOR = "|";
    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private Map<String, String> queryMetrics;
    private String collection;

    public QueryStatsPopulator(String collection) {
        this.collection = collection;
    }
    public Map<String, String> populateStats (Map<String, JsonNode> solrMBeansHandlersMap, String handler) {

        JsonNode node = solrMBeansHandlersMap.get("QUERYHANDLER");
        if (node != null) {
            queryMetrics = new HashMap<String, String>();
            JsonNode searchStats = node.path(handler).path("stats");
            String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "QUERYHANDLER|" + handler + METRIC_SEPARATOR;
            if (!searchStats.isMissingNode()) {
                queryMetrics.put(metricPath + "Requests", String.valueOf(Math.round(searchStats.path("requests").asDouble())));
                queryMetrics.put(metricPath + "Errors", String.valueOf(Math.round(searchStats.path("errors").asDouble())));
                queryMetrics.put(metricPath + "Timeouts", String.valueOf(Math.round(searchStats.path("timeouts").asDouble())));
                queryMetrics.put(metricPath + "Average Requests Per Minute", String.valueOf(Math.round(SolrUtils.multipyBy(searchStats.path("avgRequestsPerMinute").asDouble(), 60))));
                queryMetrics.put(metricPath + "Average Requests Per Second", String.valueOf(Math.round(searchStats.path("avgRequestsPerSecond").asDouble())));
                queryMetrics.put(metricPath + "5 min Rate Requests Per Minute", String.valueOf(Math.round(SolrUtils.multipyBy(searchStats.path("5minRateReqsPerSecond").asDouble(), 60))));
                queryMetrics.put(metricPath + "Average Time Per Request (milliseconds)", String.valueOf(Math.round(searchStats.path("avgTimePerRequest").asDouble())));

            } else {
                logger.warn("Missing Handler " + handler + " in this Solr");
            }
        }
        return queryMetrics;
    }
}