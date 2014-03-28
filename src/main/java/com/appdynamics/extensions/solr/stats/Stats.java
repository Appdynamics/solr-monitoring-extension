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

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

public abstract class Stats {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.Stats");

	private IHttpClientWrapper httpClient;

	private String host;

	private String port;

	public Stats(final String host, final String port, IHttpClientWrapper httpClient) {
		this.host = host;
		this.port = port;
		this.httpClient = httpClient;
	}

	/**
	 * Fetches the solr-mbeans node from JsonResponse and puts it into a map
	 * with key as Handler name and its values as JsonNode
	 * 
	 * @param resource
	 * @return
	 */
	public Map<String, JsonNode> getSolrMBeansHandlersMap(String resource) {
		String jsonString = getJsonResponseString(resource);
		Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode solrMBeansNode = null;
		try {
			solrMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("solr-mbeans");
		} catch (JsonParseException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}
		if (solrMBeansNode.isMissingNode()) {
			throw new RuntimeException("Missing node while parsing solr-mbeans node json string " + resource);
		}
		for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
			solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
		}
		return solrStatsMap;

	}

	/**
	 * Connects to specified resource and returns response as JsonString. If the
	 * resource is not found, an exception is thrown.
	 * 
	 * @param resource
	 * @return
	 */
	public String getJsonResponseString(String resource) {
		HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
		HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(LOG));
		if (response.getStatusCode() == 404) {
			throw new RuntimeException("Error accessing " + resource);
		}
		return response.getResponseBody();
	}

	/**
	 * Populates desired metrics from the JsonNode
	 * 
	 * @throws Exception
	 */
	public abstract void populateStats() throws Exception;

	public abstract String constructURL();

	public IHttpClientWrapper getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(IHttpClientWrapper httpClient) {
		this.httpClient = httpClient;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
