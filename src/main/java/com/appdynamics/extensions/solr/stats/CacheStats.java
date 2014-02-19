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

public class CacheStats extends Stats {

	private static Logger logger = Logger.getLogger(CacheStats.class.getName());

	private Number queryResultCacheHitRatio;

	private Number documentCacheHitRatio;

	private Number fieldValueCacheHitRatio;

	private Number filterCacheHitRatio;

	private Number queryResultCacheHitRatioCumulative;

	private Number documentCacheHitRatioCumulative;

	private Number fieldValueCacheHitRatioCumulative;

	private Number filterCacheHitRatioCumulative;

	private Number queryResultCacheSize;

	private Number documentCacheSize;

	private Number fieldValueCacheSize;

	private Number filterCacheSize;

	public CacheStats(String host, String port) {
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
		JsonNode cacheNode = solrStatsMap.get("CACHE");
		JsonNode queryResultCacheStats = cacheNode.path("queryResultCache").path("stats");

		if (!queryResultCacheStats.isMissingNode()) {
			this.setQueryResultCacheHitRatio(queryResultCacheStats.path("hitratio").asInt());
			this.setQueryResultCacheHitRatioCumulative(queryResultCacheStats.path("cumulative_hitratio").asInt());
			this.setQueryResultCacheSize(queryResultCacheStats.path("size").asInt());
		} else {
			logger.error("Error in query result cache stats");
		}

		JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");

		if (!documentCacheStats.isMissingNode()) {
			this.setDocumentCacheHitRatio(documentCacheStats.path("hitratio").asInt());
			this.setDocumentCacheHitRatioCumulative(documentCacheStats.path("cumulative_hitratio").asInt());
			this.setDocumentCacheSize(documentCacheStats.path("size").asInt());
		} else {
			logger.error("Error in document cache stats");
		}

		JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");

		if (!fieldValueCacheStats.isMissingNode()) {
			this.setFieldValueCacheHitRatio(fieldValueCacheStats.path("hitratio").asInt());
			this.setFieldValueCacheHitRatioCumulative(fieldValueCacheStats.path("cumulative_hitratio").asInt());
			this.setFieldValueCacheSize(fieldValueCacheStats.path("size").asInt());
		} else {
			logger.error("Error in field value cache stats");
		}

		JsonNode filterCacheStats = cacheNode.path("filterCache").path("stats");

		if (!filterCacheStats.isMissingNode()) {
			this.setFilterCacheHitRatio(filterCacheStats.path("hitratio").asInt());
			this.setFilterCacheHitRatioCumulative(filterCacheStats.path("cumulative_hitratio").asInt());
			this.setFilterCacheSize(filterCacheStats.path("size").asInt());
		} else {
			logger.error("Error in filter cache stats");
		}
	}

	public Number getQueryResultCacheHitRatio() {
		return queryResultCacheHitRatio;
	}

	public void setQueryResultCacheHitRatio(Number queryResultCacheHitRatio) {
		this.queryResultCacheHitRatio = queryResultCacheHitRatio;
	}

	public Number getDocumentCacheHitRatio() {
		return documentCacheHitRatio;
	}

	public void setDocumentCacheHitRatio(Number documentCacheHitRatio) {
		this.documentCacheHitRatio = documentCacheHitRatio;
	}

	public Number getFieldValueCacheHitRatio() {
		return fieldValueCacheHitRatio;
	}

	public void setFieldValueCacheHitRatio(Number fieldValueCacheHitRatio) {
		this.fieldValueCacheHitRatio = fieldValueCacheHitRatio;
	}

	public Number getFilterCacheHitRatio() {
		return filterCacheHitRatio;
	}

	public void setFilterCacheHitRatio(Number filterCacheHitRatio) {
		this.filterCacheHitRatio = filterCacheHitRatio;
	}

	public Number getQueryResultCacheHitRatioCumulative() {
		return queryResultCacheHitRatioCumulative;
	}

	public void setQueryResultCacheHitRatioCumulative(Number queryResultCacheHitRatioCumulative) {
		this.queryResultCacheHitRatioCumulative = queryResultCacheHitRatioCumulative;
	}

	public Number getDocumentCacheHitRatioCumulative() {
		return documentCacheHitRatioCumulative;
	}

	public void setDocumentCacheHitRatioCumulative(Number documentCacheHitRatioCumulative) {
		this.documentCacheHitRatioCumulative = documentCacheHitRatioCumulative;
	}

	public Number getFieldValueCacheHitRatioCumulative() {
		return fieldValueCacheHitRatioCumulative;
	}

	public void setFieldValueCacheHitRatioCumulative(Number fieldValueCacheHitRatioCumulative) {
		this.fieldValueCacheHitRatioCumulative = fieldValueCacheHitRatioCumulative;
	}

	public Number getFilterCacheHitRatioCumulative() {
		return filterCacheHitRatioCumulative;
	}

	public void setFilterCacheHitRatioCumulative(Number filterCacheHitRatioCumulative) {
		this.filterCacheHitRatioCumulative = filterCacheHitRatioCumulative;
	}

	public Number getQueryResultCacheSize() {
		return queryResultCacheSize;
	}

	public void setQueryResultCacheSize(Number queryResultCacheSize) {
		this.queryResultCacheSize = queryResultCacheSize;
	}

	public Number getDocumentCacheSize() {
		return documentCacheSize;
	}

	public void setDocumentCacheSize(Number documentCacheSize) {
		this.documentCacheSize = documentCacheSize;
	}

	public Number getFieldValueCacheSize() {
		return fieldValueCacheSize;
	}

	public void setFieldValueCacheSize(Number fieldValueCacheSize) {
		this.fieldValueCacheSize = fieldValueCacheSize;
	}

	public Number getFilterCacheSize() {
		return filterCacheSize;
	}

	public void setFilterCacheSize(Number filterCacheSize) {
		this.filterCacheSize = filterCacheSize;
	}
}
