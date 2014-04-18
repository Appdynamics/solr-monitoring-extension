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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.SimpleHttpClient;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SolrHelper {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.SolrHelper");

	private SimpleHttpClient httpClient;

	public SolrHelper(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Fetches the solr-mbeans node from JsonResponse and puts it into a map
	 * with key as Category name and its values as JsonNode
	 * 
	 * @param mbeansUri
	 * 
	 * @param resource
	 * @return
	 * @throws IOException
	 */
	public Map<String, JsonNode> getSolrMBeansHandlersMap(String core, String mbeansUri) throws IOException {
		if ("".equals(core)) {
			mbeansUri = SolrMonitor.getContextRootPath() + "/admin/mbeans?stats=true&wt=json";
		}
		String uri = String.format(mbeansUri, core);
		InputStream inputStream = httpClient.target().path(uri).get().inputStream();
		Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
		JsonNode solrMBeansNode = getJsonNode(inputStream).path("solr-mbeans");
		if (solrMBeansNode.isMissingNode()) {
			throw new IllegalArgumentException("Missing node while parsing solr-mbeans node json string for " + core + uri);
		}
		for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
			solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
		}
		return solrStatsMap;

	}

	public List<String> getCores(String uri) {
		List<String> cores = new ArrayList<String>();
		try {
			InputStream inputStream = httpClient.target().path(uri).get().inputStream();
			JsonNode node = getJsonNode(inputStream).path("status");
			Iterator<String> fieldNames = node.fieldNames();
			while (fieldNames.hasNext()) {
				cores.add(fieldNames.next());
			}
		} catch (IOException e) {
			LOG.error("Exception in getCores Method");
			throw new RuntimeException(e);
		} catch (Exception e) {
			LOG.error("Exception in getCores Method");
			throw new RuntimeException(e);
		}
		if (LOG.isDebugEnabled())
			LOG.debug("Cores / Collections size is " + cores.size());
		return cores;
	}

	public static JsonNode getJsonNode(InputStream inputStream) {
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = null;
		try {
			jsonNode = mapper.readValue(inputStream, JsonNode.class);
		} catch (JsonParseException e) {
			LOG.error("JsonParsing error in getJsonNode()");
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			LOG.error("JsonMapping error in getJsonNode()");
			throw new RuntimeException(e);
		} catch (IOException e) {
			LOG.error("Error in getJsonNode()");
			throw new RuntimeException(e);
		}
		return jsonNode;
	}

	public boolean checkIfMBeanHandlerSupported(String resource) throws IOException {
		InputStream inputStream = httpClient.target().path(resource).get().inputStream();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode node = mapper.readValue(inputStream, JsonNode.class).path("plugins").path("QUERYHANDLER");
		return node.has("/admin/mbeans");
	}

	/**
	 * Converts Bytes to MegaBytes
	 * 
	 * @param d
	 * @return
	 */
	public static double convertBytesToMB(Number d) {
		return (double) Math.round(d.doubleValue() / (1024.0 * 1024.0));
	}

	/**
	 * Converts from String form with Units("224 MB") to a number(224)
	 * 
	 * @param value
	 * @return
	 */
	public static Double convertMemoryStringToDouble(String value) {
		if (value.contains("KB"))
			return Double.valueOf(value.split("KB")[0].trim()) / 1024.0;
		else if (value.contains("MB"))
			return Double.valueOf(value.split("MB")[0].trim());
		else if (value.contains("GB"))
			return Double.valueOf(value.split("GB")[0].trim()) * 1024.0;
		else
			throw new NumberFormatException("Unrecognized string format: " + value);
	}

	public SimpleHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
