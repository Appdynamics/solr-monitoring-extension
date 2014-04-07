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

public class CacheStats {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.CacheStats");

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

	public void populateStats(Map<String, JsonNode> solrMBeansHandlersMap) throws Exception {

		JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
		JsonNode queryResultCacheStats = cacheNode.path("queryResultCache").path("stats");

		if (!queryResultCacheStats.isMissingNode()) {
			this.setQueryResultCacheHitRatio(queryResultCacheStats.path("hitratio").asDouble());
			this.setQueryResultCacheHitRatioCumulative(queryResultCacheStats.path("cumulative_hitratio").asDouble());
			this.setQueryResultCacheSize(queryResultCacheStats.path("size").asDouble());

			if (LOG.isDebugEnabled()) {
				LOG.debug("hitratio = " + getQueryResultCacheHitRatio());
				LOG.debug("cumulative_hitratio = " + getQueryResultCacheHitRatioCumulative());
				LOG.debug("size= " + getQueryResultCacheSize());
			}
		} else {
			LOG.error("queryResultCache is disabled in solrconfig.xml");
		}

		JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");

		if (!documentCacheStats.isMissingNode()) {
			this.setDocumentCacheHitRatio(documentCacheStats.path("hitratio").asDouble());
			this.setDocumentCacheHitRatioCumulative(documentCacheStats.path("cumulative_hitratio").asDouble());
			this.setDocumentCacheSize(documentCacheStats.path("size").asDouble());
		} else {
			LOG.error("documentCache is disabled in solrconfig.xml");
		}

		JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");

		if (!fieldValueCacheStats.isMissingNode()) {
			this.setFieldValueCacheHitRatio(fieldValueCacheStats.path("hitratio").asDouble());
			this.setFieldValueCacheHitRatioCumulative(fieldValueCacheStats.path("cumulative_hitratio").asDouble());
			this.setFieldValueCacheSize(fieldValueCacheStats.path("size").asDouble());
		} else {
			LOG.error("fieldValueCache is disabled in solrconfig.xml");
		}

		JsonNode filterCacheStats = cacheNode.path("filterCache").path("stats");

		if (!filterCacheStats.isMissingNode()) {
			this.setFilterCacheHitRatio(filterCacheStats.path("hitratio").asDouble());
			this.setFilterCacheHitRatioCumulative(filterCacheStats.path("cumulative_hitratio").asDouble());
			this.setFilterCacheSize(filterCacheStats.path("size").asDouble());
		} else {
			LOG.error("filterCache is disabled in solrconfig.xml");
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
