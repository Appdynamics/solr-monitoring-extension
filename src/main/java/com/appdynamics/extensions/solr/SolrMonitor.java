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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

import com.appdynamics.extensions.solr.stats.CacheStats;
import com.appdynamics.extensions.solr.stats.CoreStats;
import com.appdynamics.extensions.solr.stats.MemoryStats;
import com.appdynamics.extensions.solr.stats.QueryStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import com.singularity.ee.util.httpclient.HttpClientWrapper;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.httpclient.SimpleHttpClientWrapper;

public class SolrMonitor extends AManagedMonitor {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.SolrMonitor");

	private static String metric_path_prefix = "Custom Metrics|Solr|";

	private static final String SOLR_URI = "/solr";
	private static final String CORE_URI = "/solr/admin/cores?action=STATUS&wt=json";
	private static String plugins_uri = "/solr/%s/admin/plugins?wt=json";
	private static final String MEMORY_URI = "/solr/admin/system?stats=true&wt=json";

	private String host;
	private String port;

	private IHttpClientWrapper httpClient;

	private SolrHelper helper;

	public SolrMonitor() {
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		LOG.info(msg);
		System.out.println(msg);
	}

	/*
	 * Main execution method that uploads the metrics to AppDynamics Controller
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.singularity.ee.agent.systemagent.api.ITask#execute(java.util.Map,
	 * com.singularity.ee.agent.systemagent.api.TaskExecutionContext)
	 */
	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {

		checkTaskArgs(taskArguments);

		if (httpClient == null) {
			if (Boolean.getBoolean("com.appdynamics.extensions.solr.useproxy")) {
				httpClient = HttpClientWrapper.getInstance();
			} else {
				HttpClient client = HttpClientWrapper.getDefaultHttpClient();
				client.getHostConfiguration().setProxyHost(null);
				httpClient = new SimpleHttpClientWrapper(client);
			}
		}

		if (helper == null) {
			helper = new SolrHelper(host, port, httpClient);
		}
		// check for solr status
		try {
			helper.getHttpResponse(buildURL(SOLR_URI));
		} catch (Exception e) {
			LOG.error("Connection to Solr failed", e);
			return new TaskOutput("Connection to Solr failed");
		}

		// Monitor multiple cores
		List<String> cores = new ArrayList<String>();
		try {
			cores = helper.getCores(buildURL(CORE_URI));
			if (cores.isEmpty()) {
				LOG.error("There are no SolrCores running. Using this Solr Extension requires at least one SolrCore.");
				return new TaskOutput("There are no SolrCores running. Using this Solr Extension requires at least one SolrCore.");
			}

			for (String core : cores) {
				if ("".equals(core)) {
					plugins_uri = "/solr/admin/plugins?wt=json";
				}
				if (helper.checkIfMBeanHandlerSupported(buildURL(String.format(plugins_uri, core)))) {
					Map<String, JsonNode> solrMBeansHandlersMap = new HashMap<String, JsonNode>();
					try {
						solrMBeansHandlersMap = helper.getSolrMBeansHandlersMap(core);
					} catch (Exception e) {
						LOG.error(e.getMessage());
						break;
					}

					try {
						CoreStats coreStats = new CoreStats();
						coreStats.populateStats(solrMBeansHandlersMap);
						printMetrics(core, coreStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Core Stats", e);
					}

					try {
						QueryStats queryStats = new QueryStats();
						queryStats.populateStats(solrMBeansHandlersMap);
						printMetrics(core, queryStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Query Stats", e);
					}

					try {
						CacheStats cacheStats = new CacheStats();
						cacheStats.populateStats(solrMBeansHandlersMap);
						printMetrics(core, cacheStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Cache Stats", e);
					}
				} else {
					LOG.error("Stats are collected through an HTTP Request to SolrInfoMBeanHandler");
					LOG.error("SolrInfoMbeanHandler (/admin/mbeans) or /admin request handler is disabled in solrconfig.xml for this " + core);
				}
			}

			// Fetches JVM Memory and System Memory Stats
			try {
				MemoryStats memoryStats = new MemoryStats();
				String jsonString = helper.getHttpResponse(buildURL(MEMORY_URI)).getResponseBody();
				memoryStats.populateStats(jsonString);
				printMetrics(memoryStats);
			} catch (Exception e) {
				LOG.error("Error Retrieving Memory Stats: It is possible that defaultCoreName is missing in solr.xml", e);
			}

		} catch (Exception e) {
			LOG.error(e.getMessage());
		}

		return new TaskOutput("End of execute method");
	}

	private void checkTaskArgs(Map<String, String> taskArguments) {
		host = taskArguments.get("host");
		port = taskArguments.get("port");
		if (taskArguments.get("metric-path") != null && taskArguments.get("metric-path") != "") {
			metric_path_prefix = taskArguments.get("metric-path");
			LOG.debug("Metric path: " + metric_path_prefix);
			if (!metric_path_prefix.endsWith("|")) {
				metric_path_prefix += "|";
			}
		}
	}

	private void printMetrics(String collection, CoreStats stats) {
		if ("".equals(collection)) {
			collection = "Collection";
		}
		String metricPath = "Collections |" + collection + "|" + "Core|";
		printMetric(metricPath, "Number of Docs", stats.getNumDocs());
		printMetric(metricPath, "Max Docs", stats.getMaxDocs());
		printMetric(metricPath, "Deleted Docs", stats.getDeletedDocs());
	}

	private void printMetrics(String collection, CacheStats cacheStats) {
		if ("".equals(collection)) {
			collection = "Collection";
		}
		String metricPath = "Collections |" + collection + "|" + "Cache|";
		String queryCachePath = metricPath + "QueryResultCache|";
		String documentCachePath = metricPath + "DocumentCache|";
		String fieldCachePath = metricPath + "FieldValueCache|";
		String filterCachePath = metricPath + "FilterCache|";

		printMetric(queryCachePath, "HitRatio", cacheStats.getQueryResultCacheHitRatio());
		printMetric(queryCachePath, "HitRatioCumulative", cacheStats.getQueryResultCacheHitRatioCumulative());
		printMetric(queryCachePath, "CacheSize (Bytes)", cacheStats.getQueryResultCacheSize());
		printMetric(documentCachePath, "HitRatio", cacheStats.getDocumentCacheHitRatio());
		printMetric(documentCachePath, "HitRatioCumulative", cacheStats.getDocumentCacheHitRatioCumulative());
		printMetric(documentCachePath, "CacheSize (Bytes)", cacheStats.getDocumentCacheSize());
		printMetric(fieldCachePath, "HitRatio", cacheStats.getFieldValueCacheHitRatio());
		printMetric(fieldCachePath, "HitRatioCumulative", cacheStats.getFieldValueCacheHitRatioCumulative());
		printMetric(fieldCachePath, "CacheSize (Bytes)", cacheStats.getFieldValueCacheSize());
		printMetric(filterCachePath, "HitRatio", cacheStats.getFilterCacheHitRatio());
		printMetric(filterCachePath, "HitRatioCumulative", cacheStats.getFilterCacheHitRatioCumulative());
		printMetric(filterCachePath, "CacheSize (Bytes)", cacheStats.getFilterCacheSize());
	}

	private void printMetrics(MemoryStats memoryStats) {
		String metricPath = "Memory|";
		String jvmPath = metricPath + "JVMMemory|";
		String systemPath = metricPath + "SystemMemory|";

		printMetric(jvmPath, "Used (MB)", memoryStats.getJvmMemoryUsed());
		printMetric(jvmPath, "Free (MB)", memoryStats.getJvmMemoryFree());
		printMetric(jvmPath, "Total (MB)", memoryStats.getJvmMemoryTotal());
		printMetric(systemPath, "Free Physical Memory(MB)", memoryStats.getFreePhysicalMemorySize());
		printMetric(systemPath, "Total Physical Memory(MB)", memoryStats.getTotalPhysicalMemorySize());
		printMetric(systemPath, "Committed Virtual Memory(MB)", memoryStats.getCommittedVirtualMemorySize());
		printMetric(systemPath, "Free Swap Size (MB)", memoryStats.getFreeSwapSpaceSize());
		printMetric(systemPath, "Total Swap Size (MB)", memoryStats.getTotalSwapSpaceSize());
		printMetric(systemPath, "Open File Descriptor Count", memoryStats.getOpenFileDescriptorCount());
		printMetric(systemPath, "Max File Descriptor Count", memoryStats.getMaxFileDescriptorCount());

	}

	private void printMetrics(String collection, QueryStats stats) {
		if ("".equals(collection)) {
			collection = "Collection";
		}
		String metricPath = "Collections |" + collection + "|" + "Query|";
		printMetric(metricPath, "Average Rate (requests per second)", stats.getAvgRate());
		printMetric(metricPath, "5 Minute Rate (requests per second)", stats.getRate5min());
		printMetric(metricPath, "15 Minute Rate (requests per second)", stats.getRate15min());
		printMetric(metricPath, "Average Time Per Request (milliseconds)", stats.getAvgTimePerRequest());
		printMetric(metricPath, "Median Request Time (milliseconds)", stats.getMedianRequestTime());
		printMetric(metricPath, "95th Percentile Request Time (milliseconds)", stats.getPcRequestTime95th());

	}

	/**
	 * Prints Metrics to AppDynamics Metric Browser
	 * 
	 * @param metricPath
	 * @param metricName
	 * @param metricValue
	 */
	private void printMetric(String metricPath, String metricName, Object metricValue) {
		printMetric(getMetricPrefix() + metricPath, metricName, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
				MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_COLLECTIVE);
	}

	private void printMetric(String metricPath, String metricName, Object metricValue, String aggregation, String timeRollup, String cluster) {
		MetricWriter metricWriter = super.getMetricWriter(metricPath + metricName, aggregation, timeRollup, cluster);
		if (metricValue instanceof Double) {
			metricWriter.printMetric(String.valueOf(Math.round((Double) metricValue)));
		} else if (metricValue instanceof Float) {
			metricWriter.printMetric(String.valueOf(Math.round((Float) metricValue)));
		} else {
			metricWriter.printMetric(String.valueOf(metricValue));
		}
	}

	private String getMetricPrefix() {
		return metric_path_prefix;
	}

	private static String getImplementationVersion() {
		return SolrMonitor.class.getPackage().getImplementationTitle();
	}

	private String buildURL(String uri) {
		return "http://" + host + ":" + port + uri;
	}

	public static void main(String[] args) throws TaskExecutionException {
		Map<String, String> taskArguments = new HashMap<String, String>();
		taskArguments.put("host", "localhost");
		taskArguments.put("port", "8983");
		SolrMonitor monitor = new SolrMonitor();
		monitor.execute(taskArguments, null);
	}
}
