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
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.SimpleHttpClient;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;

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
		InputStream inputStream = null;
		Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
		try {
			inputStream = httpClient.target().path(uri).get().inputStream();
			JsonNode jsonNode = getJsonNode(inputStream);
			if (jsonNode != null) {
				JsonNode solrMBeansNode = jsonNode.path("solr-mbeans");
				if (solrMBeansNode.isMissingNode()) {
					throw new IllegalArgumentException("Missing node while parsing solr-mbeans node json string for " + core + uri);
				}
				for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
					solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
				}
			}
		} catch (Exception e) {
			LOG.error(e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}

		return solrStatsMap;

	}

	public List<String> getCores(String uri) {
		List<String> cores = new ArrayList<String>();
		InputStream inputStream = null;
		try {
			inputStream = httpClient.target().path(uri).get().inputStream();
			JsonNode node = getJsonNode(inputStream);
			if (node != null) {
				Iterator<String> fieldNames = node.path("status").fieldNames();
				while (fieldNames.hasNext()) {
					cores.add(fieldNames.next());
				}
				if (cores.isEmpty()) {
					LOG.error("There are no SolrCores running. Using this Solr Extension requires at least one SolrCore.");
					throw new RuntimeException();
				}
				if (LOG.isDebugEnabled())
					LOG.debug("Cores / Collections size is " + cores.size());
			}
		} catch (Exception e) {
			LOG.error("Error while fetching cores " + uri, e);
			throw new RuntimeException();
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}
		return cores;
	}

	public static JsonNode getJsonNode(InputStream inputStream) throws IOException {
		if (inputStream == null) {
			return null;
		}
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(inputStream, JsonNode.class);
	}

	public boolean checkIfMBeanHandlerSupported(String resource) throws IOException {
		InputStream inputStream = null;
		try {
			inputStream = httpClient.target().path(resource).get().inputStream();
			JsonNode jsonNode = getJsonNode(inputStream);
			if (jsonNode != null) {
				JsonNode node = jsonNode.findValue("QUERYHANDLER");
				if (node == null) {
					LOG.error("Missing 'QUERYHANDLER' when checking for mbeans " + resource);
					return false;
				}
				boolean mbeanSupport = node.has("/admin/mbeans");
				if (!mbeanSupport) {
					LOG.error("Stats are collected through an HTTP Request to SolrInfoMBeanHandler");
					LOG.error("SolrInfoMbeanHandler (/admin/mbeans) or /admin request handler is disabled in solrconfig.xml " + resource);
				}
				return mbeanSupport;
			} else {
				LOG.error("Response null when accessing " + resource);
				return false;
			}
		} catch (Exception e) {
			LOG.error("Exception when mbean handler check " + resource, e);
			return false;
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
			} catch (Exception e) {
				// Ignore
			}
		}
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
	 * @param valueStr
	 * @return
	 */
	public static Double convertMemoryStringToDouble(String valueStr) {
		if (!Strings.isNullOrEmpty(valueStr)) {
			String strippedValueStr = null;
			try {
				if (valueStr.contains("KB")) {
					strippedValueStr = valueStr.split("KB")[0].trim();
					return unLocalizeStrValue(strippedValueStr) / 1024.0;
				} else if (valueStr.contains("MB")) {
					strippedValueStr = valueStr.split("MB")[0].trim();
					return unLocalizeStrValue(strippedValueStr);
				} else if (valueStr.contains("GB")) {
					strippedValueStr = valueStr.split("GB")[0].trim();
					return unLocalizeStrValue(strippedValueStr) * 1024.0;
				}
			} catch (Exception e) {
				// ignore
			}
			LOG.error("Unrecognized string format: " + valueStr);
		}
		return null;
	}

	private static Double unLocalizeStrValue(String valueStr) {
		try {
			Locale loc = Locale.getDefault();
			return Double.valueOf(NumberFormat.getInstance(loc).parse(valueStr).doubleValue());
		} catch (ParseException e) {
			LOG.error("Exception while unlocalizing number string "+ valueStr, e);
		}
		return null;
	}

	public SimpleHttpClient getHttpClient() {
		return httpClient;
	}

	public void setHttpClient(SimpleHttpClient httpClient) {
		this.httpClient = httpClient;
	}
}
