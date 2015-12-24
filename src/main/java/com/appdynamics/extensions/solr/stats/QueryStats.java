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

package com.appdynamics.extensions.solr.stats;

import com.appdynamics.extensions.solr.SolrHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.log4j.Logger;

import java.util.Map;

public class QueryStats {

    private static Logger logger = Logger.getLogger(QueryStats.class);

    private Double requests;
    private Double errors;
    private Double timeouts;
    private Double avgRequests;
    private Double avgTimePerRequest;
    private Double fiveMinRateRequests;

    public void populateStats(Map<String, JsonNode> solrMBeansHandlersMap, String handler) {

        JsonNode node = solrMBeansHandlersMap.get("QUERYHANDLER");
        if (node != null) {
            JsonNode searchStats = node.path(handler).path("stats");
            if (!searchStats.isMissingNode()) {
                this.setRequests(searchStats.path("requests").asDouble());
                this.setErrors(searchStats.path("errors").asDouble());
                this.setTimeouts(searchStats.path("timeouts").asDouble());
                this.setAvgRequests(searchStats.path("avgRequestsPerSecond").asDouble());
                this.setFiveMinRateRequests(SolrHelper.multipyBy(searchStats.path("5minRateReqsPerSecond").asDouble(), 60));
                this.setAvgTimePerRequest(searchStats.path("avgTimePerRequest").asDouble());
            } else {
                logger.warn("Missing Handler " + handler + " in this Solr");
            }
        }
    }

    public Double getRequests() {
        return requests;
    }

    public void setRequests(Double requests) {
        this.requests = requests;
    }

    public Double getErrors() {
        return errors;
    }

    public void setErrors(Double errors) {
        this.errors = errors;
    }

    public Double getTimeouts() {
        return timeouts;
    }

    public void setTimeouts(Double timeouts) {
        this.timeouts = timeouts;
    }

    public Double getAvgRequests() {
        return avgRequests;
    }

    public void setAvgRequests(Double avgRequests) {
        this.avgRequests = avgRequests;
    }

    public Double getAvgTimePerRequest() {
        return avgTimePerRequest;
    }

    public void setAvgTimePerRequest(Double avgTimePerRequest) {
        this.avgTimePerRequest = avgTimePerRequest;
    }

    public Double getFiveMinRateRequests() {
        return fiveMinRateRequests;
    }

    public void setFiveMinRateRequests(Double fiveMinRateRequests) {
        this.fiveMinRateRequests = fiveMinRateRequests;
    }
}
