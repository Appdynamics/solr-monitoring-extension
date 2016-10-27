package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.config.Core;
import com.appdynamics.extensions.solr.stats.CacheStats;
import com.appdynamics.extensions.solr.stats.CoreStats;
import com.appdynamics.extensions.solr.stats.MemoryStats;
import com.appdynamics.extensions.solr.stats.QueryStats;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.util.shared.bounded.collections.SharedBoundedArrayList;
import com.singularity.ee.util.collections.ArrayStack;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by adityajagtiani on 10/17/16.
 */

public class SolrMonitorTask implements Runnable {
    private static String context_root = "/solr";
    private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";
    private static String plugins_uri = "/%s/admin/plugins?wt=json";
    private static String memory_uri = "/%s/admin/system?stats=true&wt=json";
    private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";
    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private MonitorConfiguration configuration;
    private Map server;

    public SolrMonitorTask (MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run () {
        try {
            runTask();
            logger.info("Solr Metric Upload Complete");
        } catch (Exception ex) {
            configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            logger.error("Error while running the task", ex);
        }
    }

    private void runTask () {
        try {
            CloseableHttpClient httpClient = configuration.getHttpClient();
            SolrHelper helper = new SolrHelper(httpClient);
            List<Core> cores = getCores(helper, configuration.getConfigYml());
            populateAndPrintStats(httpClient, helper, cores);
            logger.info("Solr monitoring task completed successfully.");
        } catch (Exception e) {
            logger.error("Exception while running Solr Monitor Task ", e);
        }
    }

    private void populateAndPrintStats (CloseableHttpClient httpClient, SolrHelper helper, List<Core> coresConfig)
            throws IOException {
        for (Core coreConfig : coresConfig) {
            String core = coreConfig.getName();
            if (!isPingHandler(coreConfig, httpClient, helper)) {
                printMetric("|Cores|" + core + "|", core + " PingStatus", 0);
            } else {
                printMetric("|Cores|" + core + "|", core + " PingStatus", 1);
                if (helper.checkIfMBeanHandlerSupported(generateURI(String.format(context_root + plugins_uri, core)))) {
                    Map<String, JsonNode> solrMBeansHandlersMap;
                    try {
                        solrMBeansHandlersMap = helper.getSolrMBeansHandlersMap(core, generateURI(context_root + mbeansUri));
                    } catch (Exception e) {
                        logger.error("Error in retrieving mbeans info for " + core);
                        break;
                    }

                    try {
                        CoreStats coreStats = new CoreStats();
                        coreStats.populateStats(solrMBeansHandlersMap);
                        printCoreMetrics(core, coreStats);
                    } catch (Exception e) {
                        logger.error("Error Retrieving Core Stats for " + core, e);
                    }
                    try {
                        for (String handler : coreConfig.getQueryHandlers()) {
                            QueryStats queryStats = new QueryStats();
                            queryStats.populateStats(solrMBeansHandlersMap, handler);
                            printQueryMetrics(core, queryStats, handler);
                        }

                    } catch (Exception e) {
                        logger.error("Error Retrieving Query Stats for " + core, e);
                    }

                    try {
                        CacheStats cacheStats = new CacheStats();
                        cacheStats.populateStats(solrMBeansHandlersMap);
                        printCacheMetrics(core, cacheStats);
                    } catch (Exception e) {
                        logger.error("Error Retrieving Cache Stats for " + core, e);
                    }
                }
                CloseableHttpResponse response = null;
                try {
                    MemoryStats memoryStats = new MemoryStats();
                    String uri = generateURI(context_root + String.format(memory_uri, core));
                    HttpGet get = new HttpGet(uri);
                    response = httpClient.execute(get);
                    memoryStats.populateStats(response);
                    printMemoryMetrics(core, memoryStats);
                } catch (Exception e) {
                    logger.error("Error retrieving memory stats for " + core, e);
                } finally {
                    closeResponse(response);
                }
            }
        }
    }

    private void printCoreMetrics (String collection, CoreStats stats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "|Cores|" + collection + "|" + "CORE|";
        printMetric(metricPath, "Number of Docs", stats.getNumDocs());
        printMetric(metricPath, "Max Docs", stats.getMaxDocs());
        printMetric(metricPath, "Deleted Docs", stats.getDeletedDocs());
    }

    private void printQueryMetrics (String collection, QueryStats stats, String handler) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "|Cores|" + collection + "|" + "QUERYHANDLER|";
        String searchMetricPath = metricPath + handler + "|";
        printMetric(searchMetricPath, "Requests", stats.getRequests());
        printMetric(searchMetricPath, "Errors", stats.getErrors());
        printMetric(searchMetricPath, "Timeouts", stats.getTimeouts());
        printMetric(searchMetricPath, "Average Requests Per Minute", SolrHelper.multipyBy(stats.getAvgRequests(), 60));
        printMetric(searchMetricPath, "Average Requests Per Second", stats.getAvgRequests());
        printMetric(searchMetricPath, "5 min Rate Requests Per Minute", stats.getFiveMinRateRequests());
        printMetric(searchMetricPath, "Average Time Per Request (milliseconds)", stats.getAvgTimePerRequest());
    }

    private void printCacheMetrics (String collection, CacheStats cacheStats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "|Cores|" + collection + "|" + "CACHE|";
        String queryCachePath = metricPath + "QueryResultCache|";
        String documentCachePath = metricPath + "DocumentCache|";
        String fieldCachePath = metricPath + "FieldValueCache|";
        String filterCachePath = metricPath + "FilterCache|";

        printMetric(queryCachePath, "HitRatio %", cacheStats.getQueryResultCacheHitRatio());
        printMetric(queryCachePath, "HitRatioCumulative %", cacheStats.getQueryResultCacheHitRatioCumulative());
        printMetric(queryCachePath, "CacheSize (Bytes)", cacheStats.getQueryResultCacheSize());
        printMetric(documentCachePath, "HitRatio %", cacheStats.getDocumentCacheHitRatio());
        printMetric(documentCachePath, "HitRatioCumulative %", cacheStats.getDocumentCacheHitRatioCumulative());
        printMetric(documentCachePath, "CacheSize (Bytes)", cacheStats.getDocumentCacheSize());
        printMetric(fieldCachePath, "HitRatio %", cacheStats.getFieldValueCacheHitRatio());
        printMetric(fieldCachePath, "HitRatioCumulative %", cacheStats.getFieldValueCacheHitRatioCumulative());
        printMetric(fieldCachePath, "CacheSize (Bytes)", cacheStats.getFieldValueCacheSize());
        printMetric(filterCachePath, "HitRatio %", cacheStats.getFilterCacheHitRatio());
        printMetric(filterCachePath, "HitRatioCumulative %", cacheStats.getFilterCacheHitRatioCumulative());
        printMetric(filterCachePath, "CacheSize (Bytes)", cacheStats.getFilterCacheSize());
    }

    private void printMemoryMetrics (String collection, MemoryStats memoryStats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "|Cores|" + collection + "|" + "MEMORY|";
        String jvmPath = metricPath + "JVM|";
        String systemPath = metricPath + "System|";

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


    private boolean isPingHandler (Core core, CloseableHttpClient httpClient, SolrHelper helper) throws IOException {
        CloseableHttpResponse response = null;
        String pingHandler = core.getPingHandler();
        if (!Strings.isNullOrEmpty(pingHandler)) {
            String uri =  generateURI("/solr/" + core.getName() + pingHandler + "?wt=json");
            try {
                HttpGet get = new HttpGet(uri);
                response = httpClient.execute(get);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonNode pingResponseNode = SolrHelper.getJsonNode(response);
                    if (pingResponseNode != null && "OK".equals(pingResponseNode.path("status").asText())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error("Could not connect to Core", e.getMessage());
            } finally {
                closeResponse(response);
            }
        }
        return false;
    }

    private List<Core> getCores (SolrHelper helper, Map<String, ?> config) {
        List<Core> cores = new ArrayList<Core>();
        if (config != null) {
            List<Map<String, ?>> coresFromCfg = (List) config.get("cores");
            for (Map<String, ?> map : coresFromCfg) {
                Core core = new Core();
                for (Map.Entry<String, ?> entry : map.entrySet()) {
                    if (entry.getKey().equals("name")) {
                        core.setName((String) entry.getValue());
                    } else if (entry.getKey().equals("pingHandler")) {
                        core.setPingHandler((String) entry.getValue());
                    } else if (entry.getKey().equals("queryHandlers")) {
                        core.setQueryHandlers((List<String>) entry.getValue());
                    }
                }
                cores.add(core);
            }
        }

        if (cores.size() == 0) {
            String defaultCore = helper.getDefaultCore(context_root + CORE_URI);
            logger.info("Cores not configured in config.yml, default core " + defaultCore + " to be used for " +
                    "stats");

            Core core = new Core();
            core.setName(defaultCore);
            core.setQueryHandlers(new ArrayList<String>());
            cores.add(core);
        }
        return cores;
    }

    private void closeResponse (CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (Exception e) {
            logger.error("Error while closing input stream", e);
        }
    }

    /**
     * Prints Metrics to AppDynamics Metric Browser
     *
     * @param metricPath
     * @param metricName
     * @param metricValue
     */
    private void printMetric (String metricPath, String metricName, Object metricValue) {
        printMetric(configuration.getMetricPrefix() + metricPath, metricName, metricValue, MetricWriter
                .METRIC_AGGREGATION_TYPE_AVERAGE, MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter
                .METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
    }

    private void printMetric (String metricPath, String metricName, Object metricValue, String aggregation, String
            timeRollup, String cluster) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        metricPath = metricPath + metricName;
        if (metricValue != null) {
            if (metricValue instanceof Double) {
                metricWriter.printMetric(metricPath, String.valueOf(Math.round((Double) metricValue)), aggregation,
                        timeRollup, cluster);
            }
            else if (metricValue instanceof Float) {
                metricWriter.printMetric(metricPath, String.valueOf(Math.round((Float) metricValue)), aggregation, timeRollup, cluster);
            } else {
                metricWriter.printMetric(metricPath, String.valueOf(metricValue), aggregation, timeRollup, cluster);
            }
        }
    }

    private String generateURI(String resource) {
        return UrlBuilder.fromYmlServerConfig(server).build() + resource;
    }
}
