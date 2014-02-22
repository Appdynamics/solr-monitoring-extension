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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;

public class CoreStats extends Stats {

	private static Logger LOG = Logger.getLogger(CoreStats.class.getName());

	private static final String URI_QUERY_STRING = "/solr/admin/mbeans?stats=true&cat=CORE&wt=json";

	private Number numDocs;

	private Number maxDocs;

	private Number deletedDocs;

	public CoreStats(String host, String port, IHttpClientWrapper httpClient) {
		super(host, port, httpClient);
		LOG.setLevel(Level.INFO);
	}

	@Override
	public void populateStats() throws Exception {
		Map<String, JsonNode> solrMBeansHandlersMap = getSolrMBeansHandlersMap(constructURL());
		JsonNode coreNode = solrMBeansHandlersMap.get("CORE");
		if (!coreNode.isMissingNode()) {
			this.setNumDocs(coreNode.path("searcher").path("stats").path("numDocs").asInt());
			this.setMaxDocs(coreNode.path("searcher").path("stats").path("maxDoc").asInt());
			this.setDeletedDocs(coreNode.path("searcher").path("stats").path("deletedDocs").asInt());
			if (LOG.isDebugEnabled()) {
				LOG.debug("Docs=" + getNumDocs());
				LOG.debug("Max Docs=" + getMaxDocs());
			}
		} else {
			throw new RuntimeException("Error accessing when CORE handler");
		}
	}

	@Override
	public String constructURL() {
		return "http://" + getHost() + ":" + getPort() + URI_QUERY_STRING;
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

	public Number getDeletedDocs() {
		return deletedDocs;
	}

	public void setDeletedDocs(Number deletedDocs) {
		this.deletedDocs = deletedDocs;
	}
}
