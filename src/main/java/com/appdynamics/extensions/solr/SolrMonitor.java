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

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.solr.stats.CacheStats;
import com.appdynamics.extensions.solr.stats.CoreStats;
import com.appdynamics.extensions.solr.stats.MemoryStats;
import com.appdynamics.extensions.solr.stats.QueryStats;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class SolrMonitor extends AManagedMonitor {

	private static Logger LOG = Logger.getLogger("com.singularity.extensions.SolrMonitor");

	private static String metric_path_prefix = "Custom Metrics|Solr|";

	private static String context_root_path = "/solr";
	private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";
	private static String plugins_uri = "/%s/admin/plugins?wt=json";
	private static final String MEMORY_URI = "/admin/system?stats=true&wt=json";
	private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";

	private SimpleHttpClient httpClient;

	private SolrHelper helper;

	public SolrMonitor() {
		String msg = String.format("Using Monitor Version [ %s ]", getImplementationVersion());
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
		LOG.info("Starting Solr Monitoring Task");

		checkTaskArgs(taskArguments);
		httpClient = SimpleHttpClient.builder(taskArguments).build();
		helper = new SolrHelper(httpClient);

		checkSolrStatus();

		try {
			List<String> cores = helper.getCores(context_root_path + CORE_URI);
			for (String core : cores) {
				if ("".equals(core)) {
					plugins_uri = "/admin/plugins?wt=json";
				}
				if (helper.checkIfMBeanHandlerSupported(String.format(context_root_path + plugins_uri, core))) {
					Map<String, JsonNode> solrMBeansHandlersMap = new HashMap<String, JsonNode>();
					try {
						solrMBeansHandlersMap = helper.getSolrMBeansHandlersMap(core, context_root_path + mbeansUri);
					} catch (Exception e) {
						LOG.error("Error in retrieving mbeans info for " + core);
						break;
					}

					try {
						CoreStats coreStats = new CoreStats();
						coreStats.populateStats(solrMBeansHandlersMap);
						printMetrics(core, coreStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Core Stats for " + core, e);
					}

					try {
						QueryStats queryStats = new QueryStats();
						queryStats.populateStats(taskArguments, solrMBeansHandlersMap);
						printMetrics(core, queryStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Query Stats for " + core, e);
					}

					try {
						CacheStats cacheStats = new CacheStats();
						cacheStats.populateStats(solrMBeansHandlersMap);
						printMetrics(core, cacheStats);
					} catch (Exception e) {
						LOG.error("Error Retrieving Cache Stats for " + core, e);
					}
				}
			}

			// Fetches JVM Memory and System Memory Stats
			try {
				MemoryStats memoryStats = new MemoryStats();
				InputStream inputStream = httpClient.target().path(context_root_path + MEMORY_URI).get().inputStream();
				memoryStats.populateStats(inputStream);
				printMetrics(memoryStats);
			} catch (Exception e) {
				LOG.error("Error retrieving memory stats", e);
			}
		} catch (Exception e) {
			LOG.error("Exception while running Solr Monitor Task ", e);
		}
		LOG.info("Completed Solr Monitor task successfully");
		return new TaskOutput("End of execute method");
	}

	private void checkSolrStatus() throws TaskExecutionException {
		Response response = null;
		try {
			response = httpClient.target().path(context_root_path).get();
		} catch (Exception e) {
			LOG.error("Connection to Solr failed", e);
			throw new TaskExecutionException("Connection to Solr failed", e);
		} finally {
			if (response != null)
				response.close();
		}
	}

	private void checkTaskArgs(Map<String, String> taskArguments) {
		if (!Strings.isNullOrEmpty(taskArguments.get("context-root"))) {
			context_root_path = taskArguments.get("context-root");
		}

		if (!Strings.isNullOrEmpty(taskArguments.get("metric-prefix"))) {
			metric_path_prefix = taskArguments.get("metric-prefix");
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
		if (metricValue != null) {
			if (metricValue instanceof Double) {
				metricWriter.printMetric(String.valueOf(Math.round((Double) metricValue)));
			} else if (metricValue instanceof Float) {
				metricWriter.printMetric(String.valueOf(Math.round((Float) metricValue)));
			} else {
				metricWriter.printMetric(String.valueOf(metricValue));
			}
		}
	}

	private String getMetricPrefix() {
		return metric_path_prefix;
	}

	public static String getContextRootPath() {
		return context_root_path;
	}

	private static String getImplementationVersion() {
		return SolrMonitor.class.getPackage().getImplementationTitle();
	}
}
