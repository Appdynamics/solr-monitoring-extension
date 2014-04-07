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

package com.appdynamics.extensions.solr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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

public class SolrHelper {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.SolrHelper");

	private IHttpClientWrapper httpClient;

	private String host;

	private String port;

	private String mbeansUri = "/solr/%s/admin/mbeans?stats=true&wt=json";

	public SolrHelper(String host, String port, IHttpClientWrapper httpClient) {
		this.host = host;
		this.port = port;
		this.httpClient = httpClient;
	}

	/**
	 * Fetches the solr-mbeans node from JsonResponse and puts it into a map
	 * with key as Category name and its values as JsonNode
	 * 
	 * @param resource
	 * @return
	 */
	public Map<String, JsonNode> getSolrMBeansHandlersMap(String core) {
		if ("".equals(core)) {
			mbeansUri = "/solr/admin/mbeans?stats=true&wt=json";
		}
		String url = buildURL(String.format(mbeansUri, core));
		String jsonString = getHttpResponse(url).getResponseBody();
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
			throw new RuntimeException("Missing node while parsing solr-mbeans node json string for " + core + url);
		}
		for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
			solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
		}
		return solrStatsMap;

	}

	/**
	 * Connects to specified url and returns response. If the resource is not
	 * found, an exception is thrown.
	 * 
	 * @param resource
	 * @return
	 */
	public HttpExecutionResponse getHttpResponse(String url) {
		HttpExecutionRequest request = new HttpExecutionRequest(url, "", HttpOperation.GET);
		HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(LOG));
		if (response.getStatusCode() == 200) {
			if (LOG.isDebugEnabled()) {
				LOG.debug("HTTP Request to " + url + " executed successfully");
			}
		} else {
			LOG.error("Failed to execute HTTP Request to " + url + " with HTTP status code " + response.getStatusCode());
			throw new RuntimeException("HTTP Request Failed");
		}
		return response;
	}

	public List<String> getCores(String url) {
		List<String> cores = new ArrayList<String>();
		HttpExecutionResponse response = getHttpResponse(url);
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = null;
		try {
			node = mapper.readValue(response.getResponseBody().getBytes(), JsonNode.class).path("status");
		} catch (JsonParseException e) {
			LOG.error("Error parsing json response from " + url);
			throw new RuntimeException(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error("Error mapping json response from " + url);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			LOG.error("Error mapping json response from " + url);
			throw new RuntimeException(e.getMessage());
		}

		Iterator<String> fieldNames = node.fieldNames();
		while (fieldNames.hasNext()) {
			cores.add(fieldNames.next());
		}
		if (LOG.isDebugEnabled())
			LOG.debug("Cores / Collections size is " + cores.size());
		return cores;
	}

	public boolean checkIfMBeanHandlerSupported(String resource) {
		HttpExecutionResponse response = null;
		JsonNode node = null;
		boolean hasMBeans = false;
		try {
			response = getHttpResponse(resource);
			ObjectMapper mapper = new ObjectMapper();
			node = mapper.readValue(response.getResponseBody().getBytes(), JsonNode.class).path("plugins").path("QUERYHANDLER");
			hasMBeans = node.has("/admin/mbeans");
		} catch (Exception e) {
			LOG.error(e.getMessage());
		}
		return hasMBeans;
	}

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

	private String buildURL(String uri) {
		return "http://" + getHost() + ":" + getPort() + uri;
	}

}
