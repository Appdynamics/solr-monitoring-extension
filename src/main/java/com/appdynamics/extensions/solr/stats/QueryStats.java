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

import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.solr.SolrHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;

public class QueryStats {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.QueryStats");

	private String handler = "/select";
	
	private Double searchRequests;
	private Double searchErrors;
	private Double searchTimeouts;
	private Double searchAvgRequests;
	private Double searchAvgTimePerRequest;
	private Double search5minRateRequests;
	
	private Double updateRequests;
	private Double updateErrors;
	private Double updateTimeouts;
	private Double updateAvgRequests;
	private Double updateAvgTimePerRequest;
	private Double update5minRateRequests;

	public void populateStats(Map<String, String> taskArguments, Map<String, JsonNode> solrMBeansHandlersMap) {

		if (!Strings.isNullOrEmpty(taskArguments.get("search-handler"))) {
			handler = taskArguments.get("search-handler");
		}

		JsonNode node = solrMBeansHandlersMap.get("QUERYHANDLER");
		if (node != null) {
			JsonNode searchStats = node.path(handler).path("stats");
			if (!searchStats.isMissingNode()) {
				this.setSearchRequests(searchStats.path("requests").asDouble());
				this.setSearchErrors(searchStats.path("errors").asDouble());
				this.setSearchTimeouts(searchStats.path("timeouts").asDouble());
				this.setSearchAvgRequests(searchStats.path("avgRequestsPerSecond").asDouble());
				this.setSearch5minRateRequests(SolrHelper.multipyBy(searchStats.path("5minRateReqsPerSecond").asDouble(), 60));
				this.setSearchAvgTimePerRequest(searchStats.path("avgTimePerRequest").asDouble());
			} else {
				LOG.warn("Missing Handler " + handler + " in this Solr");
			}
			
			JsonNode updateStats = node.path("/update").path("stats");
			if (!searchStats.isMissingNode()) {
				this.setUpdateRequests(updateStats.path("requests").asDouble());
				this.setUpdateErrors(updateStats.path("errors").asDouble());
				this.setUpdateTimeouts(updateStats.path("timeouts").asDouble());
				this.setUpdateAvgRequests(updateStats.path("avgRequestsPerSecond").asDouble());
				this.setUpdate5minRateRequests(SolrHelper.multipyBy(searchStats.path("5minRateReqsPerSecond").asDouble(), 60));
				this.setUpdateAvgTimePerRequest(updateStats.path("avgTimePerRequest").asDouble());
			} else {
				LOG.warn("Missing Handler /update in this Solr");
			}
		}
	}

	public Double getSearchRequests() {
		return searchRequests;
	}

	public void setSearchRequests(Double searchRequests) {
		this.searchRequests = searchRequests;
	}

	public Double getSearchErrors() {
		return searchErrors;
	}

	public void setSearchErrors(Double searchErrors) {
		this.searchErrors = searchErrors;
	}

	public Double getSearchTimeouts() {
		return searchTimeouts;
	}

	public void setSearchTimeouts(Double searchTimeouts) {
		this.searchTimeouts = searchTimeouts;
	}

	public Double getSearchAvgRequests() {
		return searchAvgRequests;
	}

	public void setSearchAvgRequests(Double searchAvgRequests) {
		this.searchAvgRequests = searchAvgRequests;
	}

	public Double getSearchAvgTimePerRequest() {
		return searchAvgTimePerRequest;
	}

	public void setSearchAvgTimePerRequest(Double searchAvgTimePerRequest) {
		this.searchAvgTimePerRequest = searchAvgTimePerRequest;
	}

	public Double getUpdateRequests() {
		return updateRequests;
	}

	public void setUpdateRequests(Double updateRequests) {
		this.updateRequests = updateRequests;
	}

	public Double getUpdateErrors() {
		return updateErrors;
	}

	public void setUpdateErrors(Double updateErrors) {
		this.updateErrors = updateErrors;
	}

	public Double getUpdateTimeouts() {
		return updateTimeouts;
	}

	public void setUpdateTimeouts(Double updateTimeouts) {
		this.updateTimeouts = updateTimeouts;
	}

	public Double getUpdateAvgRequests() {
		return updateAvgRequests;
	}

	public void setUpdateAvgRequests(Double updateAvgRequests) {
		this.updateAvgRequests = updateAvgRequests;
	}

	public Double getUpdateAvgTimePerRequest() {
		return updateAvgTimePerRequest;
	}

	public void setUpdateAvgTimePerRequest(Double updateAvgTimePerRequest) {
		this.updateAvgTimePerRequest = updateAvgTimePerRequest;
	}

	public Double getSearch5minRateRequests() {
		return search5minRateRequests;
	}

	public void setSearch5minRateRequests(Double search5minRateReqsPerSecond) {
		this.search5minRateRequests = search5minRateReqsPerSecond;
	}

	public Double getUpdate5minRateRequests() {
		return update5minRateRequests;
	}

	public void setUpdate5minRateRequests(Double update5minRateReqsPerSecond) {
		this.update5minRateRequests = update5minRateReqsPerSecond;
	}
}
