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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by adityajagtiani on 10/17/16.
 */

public class SolrMonitorTask implements Runnable {
    public static String context_root = "/solr";
    public static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";
    private static String plugins_uri = "/%s/admin/plugins?wt=json";
    private static String memory_uri = "/%s/admin/system?stats=true&wt=json";
    private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";
    private static final String METRIC_SEPARATOR = "|";
    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private MonitorConfiguration configuration;
    private Map server;
    private SolrHelper helper;

    public SolrMonitorTask (MonitorConfiguration configuration, Map server, SolrHelper helper) {
        this.configuration = configuration;
        this.server = server;
        this.helper = helper;
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
            CloseableHttpClient httpClient = helper.getHttpClient();
            List<Core> cores = helper.getCores(configuration.getConfigYml());
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
            if (!isPingHandler(coreConfig, httpClient)) {
                printMetric(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR, core + " Ping Status", 0);
            } else {
                printMetric(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR, core + " Ping Status", 1);
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
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CORE" + METRIC_SEPARATOR;
        printMetric(metricPath, "Number of Docs", stats.getNumDocs());
        printMetric(metricPath, "Max Docs", stats.getMaxDocs());
        printMetric(metricPath, "Deleted Docs", stats.getDeletedDocs());
    }

    private void printQueryMetrics (String collection, QueryStats stats, String handler) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "QUERYHANDLER|";
        String searchMetricPath = metricPath + handler + METRIC_SEPARATOR;
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
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "CACHE" + METRIC_SEPARATOR;
        String queryCachePath = metricPath + "QueryResultCache" + METRIC_SEPARATOR;
        String documentCachePath = metricPath + "DocumentCache" + METRIC_SEPARATOR;
        String fieldCachePath = metricPath + "FieldValueCache" + METRIC_SEPARATOR;
        String filterCachePath = metricPath + "FilterCache" + METRIC_SEPARATOR;

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
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + collection + METRIC_SEPARATOR + "MEMORY" + METRIC_SEPARATOR;
        String jvmPath = metricPath + "JVM" + METRIC_SEPARATOR;
        String systemPath = metricPath + "System" + METRIC_SEPARATOR;

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


    private boolean isPingHandler (Core core, CloseableHttpClient httpClient) throws IOException {
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
        printMetric(configuration.getMetricPrefix() + METRIC_SEPARATOR + server.get("name") + metricPath, metricName, metricValue, MetricWriter
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
