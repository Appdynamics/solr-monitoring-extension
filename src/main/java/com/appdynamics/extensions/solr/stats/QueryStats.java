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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class QueryStats extends Stats {

	private static Logger logger = Logger.getLogger(QueryStats.class.getName());

	private String handler = "/select";

	private Number numDocs;

	private Number maxDocs;

	private Number avgRate;

	private Number rate5min;

	private Number rate15min;

	private Number avgTimePerRequest;

	private Number medianRequestTime;

	private Number pcRequestTime95th;

	public QueryStats(String host, String port) {
		super(host, port);
		logger.setLevel(Level.INFO);
	}

	public void populateStats() {
		String jsonString = getJsonResponseString(getUrl() + getResourceAppender() + getQueryString());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode solrMBeansNode = null;
		try {
			solrMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("solr-mbeans");
		} catch (JsonParseException e) {
			logger.error("JsonParseException in " + e.getClass());
			throw new RuntimeException("JsonParseException in " + e.getClass());
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException in " + e.getClass());
			throw new RuntimeException("JsonMappingException in " + e.getClass());
		} catch (IOException e) {
			logger.error("IOException in " + e.getClass());
			throw new RuntimeException("IOException in " + e.getClass());
		}
		Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
		for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
			solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));

		}
		JsonNode coreNode = solrStatsMap.get("CORE");
		this.setNumDocs(coreNode.path("searcher").path("stats").path("numDocs").asInt());
		this.setMaxDocs(coreNode.path("searcher").path("stats").path("maxDoc").asInt());

		JsonNode hstats = solrStatsMap.get("QUERYHANDLER").path(handler).path("stats");

		if (!hstats.isMissingNode()) {
			this.setAvgRate(hstats.path("avgRequestsPerSecond").asInt());
			this.setRate5min(hstats.path("5minRateReqsPerSecond").asInt());
			this.setRate15min(hstats.path("15minRateReqsPerSecond").asInt());
			this.setAvgTimePerRequest(hstats.path("avgTimePerRequest").asInt());
			this.setMedianRequestTime(hstats.path("medianRequestTime").asInt());
			this.setPcRequestTime95th(hstats.path("95thPcRequestTime").asInt());
			logger.info("Number of Docs: " + getNumDocs());
		} else {
			logger.error("Handler " + handler + " is not supported in this version");
		}
	}

	public Number getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(Number numDocs) {
		this.numDocs = numDocs;
	}

	public Number getMaxDocs() {
		return maxDocs;
	}

	public void setMaxDocs(Number maxDocs) {
		this.maxDocs = maxDocs;
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

}
