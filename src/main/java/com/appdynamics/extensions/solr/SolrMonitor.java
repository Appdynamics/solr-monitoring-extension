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

import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.appdynamics.extensions.solr.stats.CacheStats;
import com.appdynamics.extensions.solr.stats.MemoryStats;
import com.appdynamics.extensions.solr.stats.QueryStats;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;

public class SolrMonitor extends AManagedMonitor {

	private static Logger logger = Logger.getLogger(SolrMonitor.class.getName());

	private static final String metricPathPrefix = "Custom Metrics|Solr|";

	private String host;
	private String port;

	public SolrMonitor() {
		logger.setLevel(Level.INFO);
		String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
		logger.info(msg);
		System.out.println(msg);
	}

	public TaskOutput execute(Map<String, String> taskArguments, TaskExecutionContext arg1) throws TaskExecutionException {
		try {

			host = taskArguments.get("host");
			port = taskArguments.get("port");

			QueryStats solrStats = new QueryStats(host, port);
			solrStats.populateStats();
			printMetrics(solrStats);

			MemoryStats memoryStats = new MemoryStats(host, port);
			memoryStats.populateStats();
			printMetrics(memoryStats);

			CacheStats cacheStats = new CacheStats(host, port);
			cacheStats.populateStats();
			printMetrics(cacheStats);

			return new TaskOutput("Solr Metric Upload Complete");
		} catch (Exception e) {
			logger.error("Solr Metric upload failed");
			return new TaskOutput("Solr Metric upload failed");
		}
	}

	private void printMetrics(CacheStats cacheStats) {
		String metricPath = "Cache|";
		printMetric(metricPath + "QueryResultCache|", "queryResultCacheHitRatio", cacheStats.getQueryResultCacheHitRatio());
		printMetric(metricPath + "QueryResultCache|", "queryResultCacheHitRatioCumulative", cacheStats.getQueryResultCacheHitRatioCumulative());
		printMetric(metricPath + "QueryResultCache|", "queryResultCacheSize", cacheStats.getQueryResultCacheSize());
		printMetric(metricPath + "DocumentCache|", "documentCacheHitRatio", cacheStats.getDocumentCacheHitRatio());
		printMetric(metricPath + "DocumentCache|", "documentCacheHitRatioCumulative", cacheStats.getDocumentCacheHitRatioCumulative());
		printMetric(metricPath + "DocumentCache|", "documentCacheSize", cacheStats.getDocumentCacheSize());
		printMetric(metricPath + "FieldValueCache|", "fieldValueCacheHitRatio", cacheStats.getFieldValueCacheHitRatio());
		printMetric(metricPath + "FieldValueCache|", "fieldValueCacheHitRatioCumulative", cacheStats.getFieldValueCacheHitRatioCumulative());
		printMetric(metricPath + "FieldValueCache|", "fieldValueCacheSize", cacheStats.getFieldValueCacheSize());
		printMetric(metricPath + "FilterCache|", "filterCacheHitRatio", cacheStats.getFilterCacheHitRatio());
		printMetric(metricPath + "FilterCache|", "filterCacheHitRatioCumulative", cacheStats.getFilterCacheHitRatioCumulative());
		printMetric(metricPath + "FilterCache|", "filterCacheSize", cacheStats.getFilterCacheSize());
	}

	private void printMetrics(MemoryStats memoryStats) {
		// String metricPath = "JVMMemoryStats|";
		// printMetric(metricPath, "JVM Memory Used (MB)",
		// memoryStats.getJvmMemoryUsed());
		// printMetric(metricPath, "JVM Memory Free (MB)",
		// memoryStats.getJvmMemoryFree());
		// printMetric(metricPath, "JVM Memory Total (MB)",
		// memoryStats.getJvmMemoryTotal());
		printMetric("SystemMemoryStats|", "Free Physical Memory Size (Bytes)", memoryStats.getFreePhysicalMemorySize());
		printMetric("SystemMemoryStats|", "Total Physical Memory Size (Bytes)", memoryStats.getTotalPhysicalMemorySize());
		printMetric("SystemMemoryStats|", "Committed Virtual Memory Size (Bytes)", memoryStats.getCommittedVirtualMemorySize());

	}

	private void printMetrics(QueryStats stats) {
		String metricPath = "QueryStats|";
		printMetric("", "Number of Docs", stats.getNumDocs());
		printMetric("", "Max Docs", stats.getMaxDocs());
		printMetric(metricPath, "Average Rate (requests per second)", stats.getAvgRate());
		printMetric(metricPath, "5 Minute Rate (requests per second)", stats.getRate5min());
		printMetric(metricPath, "15 Minute Rate (requests per second)", stats.getRate15min());
		printMetric(metricPath, "Average Timer Per Request (milliseconds)", stats.getAvgTimePerRequest());
		printMetric(metricPath, "Median Request Time (milliseconds)", stats.getMedianRequestTime());
		printMetric(metricPath, "95th Percentile Request Time (milliseconds)", stats.getPcRequestTime95th());

	}

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
		return metricPathPrefix;
	}

	public static String getImplementationVersion() {
		return SolrMonitor.class.getPackage().getImplementationTitle();
	}

	public static void main(String[] args) {
		QueryStats solrStats = new QueryStats("localhost", "8983");
		solrStats.populateStats();

		MemoryStats memoryStats = new MemoryStats("localhost", "8983");
		memoryStats.populateStats();

		CacheStats cacheStats = new CacheStats("localhost", "8983");
		cacheStats.populateStats();
	}
}
