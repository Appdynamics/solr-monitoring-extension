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

import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;

public class QueryStats extends Stats {

	private static Logger LOG = Logger.getLogger(QueryStats.class.getName());

	// https://cwiki.apache.org/confluence/display/solr/MBean+Request+Handler
	private static final String URL_QUERY_STRING = "/solr/admin/mbeans?stats=true&cat=QUERYHANDLER&key=/select&wt=json";

	private String handler = "/select";

	private Number avgRate;

	private Number rate5min;

	private Number rate15min;

	private Number avgTimePerRequest;

	private Number medianRequestTime;

	private Number pcRequestTime95th;

	public QueryStats(String host, String port, IHttpClientWrapper httpClient) {
		super(host, port, httpClient);
	}

	@Override
	public void populateStats() throws Exception {
		Map<String, JsonNode> solrMBeansHandlersMap = getSolrMBeansHandlersMap(constructURL());

		JsonNode hstats = solrMBeansHandlersMap.get("QUERYHANDLER").path(handler).path("stats");

		if (!hstats.isMissingNode()) {
			this.setAvgRate(hstats.path("avgRequestsPerSecond").asDouble());
			this.setRate5min(hstats.path("5minRateReqsPerSecond").asDouble());
			this.setRate15min(hstats.path("15minRateReqsPerSecond").asDouble());
			this.setAvgTimePerRequest(hstats.path("avgTimePerRequest").asDouble());
			this.setMedianRequestTime(hstats.path("medianRequestTime").asDouble());
			this.setPcRequestTime95th(hstats.path("95thPcRequestTime").asDouble());

			if (LOG.isDebugEnabled()) {
				LOG.debug("avgRequestsPerSecond=" + getAvgRate());
				LOG.debug("avgTimePerRequest=" + getAvgTimePerRequest());
			}
		} else {
			throw new RuntimeException("Handler " + handler + " is not supported in this version");
		}
	}

	public Number getAvgRate() {
		return avgRate;
	}

	public void setAvgRate(Number avgRate) {
		this.avgRate = avgRate;
	}

	public Number getRate5min() {
		return rate5min;
	}

	public void setRate5min(Number rate5min) {
		this.rate5min = rate5min;
	}

	public Number getRate15min() {
		return rate15min;
	}

	public void setRate15min(Number rate15min) {
		this.rate15min = rate15min;
	}

	public Number getAvgTimePerRequest() {
		return avgTimePerRequest;
	}

	public void setAvgTimePerRequest(Number avgTimePerRequest) {
		this.avgTimePerRequest = avgTimePerRequest;
	}

	public Number getMedianRequestTime() {
		return medianRequestTime;
	}

	public void setMedianRequestTime(Number medianRequestTime) {
		this.medianRequestTime = medianRequestTime;
	}

	public Number getPcRequestTime95th() {
		return pcRequestTime95th;
	}

	public void setPcRequestTime95th(Number pcRequestTime95th) {
		this.pcRequestTime95th = pcRequestTime95th;
	}

	@Override
	public String constructURL() {
		return "http://" + getHost() + ":" + getPort() + URL_QUERY_STRING;
	}

}
