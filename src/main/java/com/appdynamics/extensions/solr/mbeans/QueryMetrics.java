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

package com.appdynamics.extensions.solr.mbeans;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

class QueryMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final Logger logger = LoggerFactory.getLogger(QueryMetrics.class);

    QueryMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, Long> populateStats (Map<String, JsonNode> solrMBeansHandlersMap, String handler) {
        Map<String, Long> queryMetrics = new HashMap<String, Long>();
        try {
            JsonNode node = solrMBeansHandlersMap.get("QUERYHANDLER");
            if (node != null) {
                JsonNode searchStats = node.path(handler).path("stats");
                String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "QUERYHANDLER" + METRIC_SEPARATOR + handler + METRIC_SEPARATOR;
                if (!searchStats.isMissingNode()) {
                    queryMetrics.put(metricPath + "Requests", SolrUtils.convertDoubleToLong(searchStats.path("requests").asDouble()));
                    queryMetrics.put(metricPath + "Errors", SolrUtils.convertDoubleToLong(searchStats.path("errors").asDouble()));
                    queryMetrics.put(metricPath + "Timeouts", SolrUtils.convertDoubleToLong(searchStats.path("timeouts").asDouble()));
                    queryMetrics.put(metricPath + "Average Requests Per Minute", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy(searchStats.path("avgRequestsPerMinute").asDouble(), 60)));
                    queryMetrics.put(metricPath + "Average Requests Per Second", SolrUtils.convertDoubleToLong(searchStats.path("avgRequestsPerSecond").asDouble()));
                    queryMetrics.put(metricPath + "5 min Rate Requests Per Minute", SolrUtils.convertDoubleToLong(SolrUtils.multipyBy(searchStats.path("5minRateReqsPerSecond").asDouble(), 60)));
                    queryMetrics.put(metricPath + "Average Time Per Request (milliseconds)", SolrUtils.convertDoubleToLong(searchStats.path("avgTimePerRequest").asDouble()));
                } else {
                    logger.warn("Missing Handler " + handler + " in this Solr");
                }
            }
        }
        catch (Exception e) {
            logger.error("Error Retrieving query Stats for " + coreName + " and handler " + handler, e);
        }
        return queryMetrics;
    }
}