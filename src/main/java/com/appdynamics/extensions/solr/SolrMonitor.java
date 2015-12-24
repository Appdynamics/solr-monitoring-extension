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

import com.appdynamics.TaskInputArgs;
import com.appdynamics.extensions.PathResolver;
import com.appdynamics.extensions.http.Response;
import com.appdynamics.extensions.http.SimpleHttpClient;
import com.appdynamics.extensions.solr.config.Configuration;
import com.appdynamics.extensions.solr.config.Core;
import com.appdynamics.extensions.solr.config.Server;
import com.appdynamics.extensions.solr.stats.CacheStats;
import com.appdynamics.extensions.solr.stats.CoreStats;
import com.appdynamics.extensions.solr.stats.MemoryStats;
import com.appdynamics.extensions.solr.stats.QueryStats;
import com.appdynamics.extensions.yml.YmlReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class SolrMonitor extends AManagedMonitor {

    public static final String CONFIG_ARG = "config-file";
    public static final String METRIC_SEPARATOR = "|";
    private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";
    private static Logger logger = Logger.getLogger(SolrMonitor.class);
    private static String metric_path_prefix = "Custom Metrics|Solr|";
    private static String context_root = "/solr";
    public static final Map<String, String> DEFAULT_ARGS = new HashMap<String, String>() {
        {
            put("context-root", context_root);
            put(TaskInputArgs.METRIC_PREFIX, metric_path_prefix);
        }
    };
    private static String plugins_uri = "/%s/admin/plugins?wt=json";
    private static String memory_uri = "/%s/admin/system?stats=true&wt=json";
    private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";

    public SolrMonitor() {
        System.out.println(logVersion());
    }

    public static String getContextRootPath() {
        return context_root;
    }

    private static String getImplementationVersion() {
        return SolrMonitor.class.getPackage().getImplementationTitle();
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
        if (taskArguments != null) {
            logger.info(logVersion());
            String configFilename = getConfigFilename(taskArguments.get(CONFIG_ARG));
            try {
                Configuration config = YmlReader.readFromFile(configFilename, Configuration.class);

                taskArguments = buildArgumentsFromConfig(config);
                taskArguments = validateArguments(taskArguments, DEFAULT_ARGS);
                metric_path_prefix = taskArguments.get(TaskInputArgs.METRIC_PREFIX);
                context_root = taskArguments.get("context-root");

                SimpleHttpClient httpClient = SimpleHttpClient.builder(taskArguments).build();
                SolrHelper helper = new SolrHelper(httpClient);

                List<Core> cores = getCores(helper, config);
                populateAndPrintStats(httpClient, helper, cores);
                logger.info("Solr monitoring task completed successfully.");
                return new TaskOutput("Solr monitoring task completed successfully.");
            } catch (Exception e) {
                logger.error("Exception while running Solr Monitor Task ", e);
            }
        }
        throw new TaskExecutionException("Solr monitoring task completed with failures.");
    }

    private Map<String, String> buildArgumentsFromConfig(Configuration config) {
        Map<String, String> taskArguments = Maps.newHashMap();
        if (config != null) {
            Server server = config.getServer();
            taskArguments.put(TaskInputArgs.HOST, server.getHost());
            taskArguments.put(TaskInputArgs.PORT, String.valueOf(server.getPort()));
            taskArguments.put(TaskInputArgs.USER, server.getUsername());
            taskArguments.put(TaskInputArgs.PASSWORD, server.getPassword());
            taskArguments.put("context-root", server.getContextRoot());
            taskArguments.put(TaskInputArgs.USE_SSL, server.getUsessl());
            taskArguments.put(TaskInputArgs.PROXY_HOST, server.getProxyHost());
            taskArguments.put(TaskInputArgs.PROXY_PORT, server.getProxyPort());
            taskArguments.put(TaskInputArgs.PROXY_USER, server.getProxyUsername());
            taskArguments.put(TaskInputArgs.PROXY_PASSWORD, server.getProxyPassword());
            taskArguments.put(TaskInputArgs.METRIC_PREFIX, config.getMetricPrefix());
        }
        return taskArguments;
    }

    private Map<String, String> validateArguments(Map<String, String> taskArguments, Map<String, String> defaultArgs) {
        for (String defaultKey : defaultArgs.keySet()) {
            if (Strings.isNullOrEmpty(taskArguments.get(defaultKey))) {
                String value = defaultArgs.get(defaultKey);
                taskArguments.put(defaultKey, value);
            }
        }
        return taskArguments;
    }

    /**
     * Gets list of cores. First tries to retrieve from config file. If no cores
     * are configured in config file, then gets the default core.
     *
     * @param helper
     * @param config
     * @return
     */
    public List<Core> getCores(SolrHelper helper, Configuration config) {
        List<Core> cores = new ArrayList<Core>();
        if (config != null && config.getCores() != null) {
            cores = config.getCores();
        }
        Iterator<Core> iterator = cores.iterator();
        while (iterator.hasNext()) {
            if (Strings.isNullOrEmpty(iterator.next().getName())) {
                iterator.remove();
            }
        }
        if (cores.size() == 0) {
            String defaultCore = helper.getDefaultCore(context_root + CORE_URI);
            logger.info("Cores not configured in config.yml, default core " + defaultCore + " to be used for stats");
            Core core = new Core();
            core.setName(defaultCore);
            core.setQueryHandlers(new ArrayList<String>());
            cores.add(core);
        }
        return cores;
    }

    private void populateAndPrintStats(SimpleHttpClient httpClient, SolrHelper helper, List<Core> coresConfig) throws IOException {
        for (Core coreConfig : coresConfig) {
            String core = coreConfig.getName();
            if (!isPingHandler(coreConfig, httpClient, helper)) {
                printMetric("Cores |" + core + "|", core + " PingStatus", 0);
            } else {
                printMetric("Cores |" + core + "|", core + " PingStatus", 1);
                if (helper.checkIfMBeanHandlerSupported(String.format(context_root + plugins_uri, core))) {
                    Map<String, JsonNode> solrMBeansHandlersMap = new HashMap<String, JsonNode>();
                    try {
                        solrMBeansHandlersMap = helper.getSolrMBeansHandlersMap(core, context_root + mbeansUri);
                    } catch (Exception e) {
                        logger.error("Error in retrieving mbeans info for " + core);
                        break;
                    }

                    try {
                        CoreStats coreStats = new CoreStats();
                        coreStats.populateStats(solrMBeansHandlersMap);
                        printMetrics(core, coreStats);
                    } catch (Exception e) {
                        logger.error("Error Retrieving Core Stats for " + core, e);
                    }

                    try {
                        for (String handler : coreConfig.getQueryHandlers()) {
                            QueryStats queryStats = new QueryStats();
                            queryStats.populateStats(solrMBeansHandlersMap, handler);
                            printMetrics(core, queryStats, handler);
                        }

                    } catch (Exception e) {
                        logger.error("Error Retrieving Query Stats for " + core, e);
                    }

                    try {
                        CacheStats cacheStats = new CacheStats();
                        cacheStats.populateStats(solrMBeansHandlersMap);
                        printMetrics(core, cacheStats);
                    } catch (Exception e) {
                        logger.error("Error Retrieving Cache Stats for " + core, e);
                    }
                }
                Response response = null;
                try {
                    MemoryStats memoryStats = new MemoryStats();
                    String uri = context_root + String.format(memory_uri, core);
                    response = httpClient.target().path(uri).get();
                    memoryStats.populateStats(response);
                    printMetrics(core, memoryStats);
                } catch (Exception e) {
                    logger.error("Error retrieving memory stats for " + core, e);
                } finally {
                    helper.closeResponse(response);
                }
            }
        }
    }

    private boolean isPingHandler(Core core, SimpleHttpClient httpClient, SolrHelper helper) {
        Response response = null;
        String pingHandler = core.getPingHandler();
        if (!Strings.isNullOrEmpty(pingHandler)) {
            String uri = context_root + "/" + core.getName() + pingHandler + "?wt=json";
            try {
                response = httpClient.target().path(uri).get();
                if(response.getStatus() == 200) {
                    JsonNode pingResponseNode = SolrHelper.getJsonNode(response);
                    if (pingResponseNode != null && "OK".equals(pingResponseNode.path("status").asText())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            } finally {
                helper.closeResponse(response);
            }
        }
        return false;
    }

    private void checkSolrStatus(SimpleHttpClient httpClient) throws TaskExecutionException {
        Response response = null;
        try {
            response = httpClient.target().path(context_root).get();
        } catch (Exception e) {
            logger.error("Connection to Solr failed", e);
            throw new TaskExecutionException("Connection to Solr failed", e);
        } finally {
            if (response != null)
                response.close();
        }
    }

    private void printMetrics(String collection, CoreStats stats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "Cores |" + collection + "|" + "CORE|";
        printMetric(metricPath, "Number of Docs", stats.getNumDocs());
        printMetric(metricPath, "Max Docs", stats.getMaxDocs());
        printMetric(metricPath, "Deleted Docs", stats.getDeletedDocs());
    }

    private void printMetrics(String collection, QueryStats stats, String handler) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "Cores |" + collection + "|" + "QUERYHANDLER|";
        String searchMetricPath = metricPath + handler + "|";
        printMetric(searchMetricPath, "Requests", stats.getRequests());
        printMetric(searchMetricPath, "Errors", stats.getErrors());
        printMetric(searchMetricPath, "Timeouts", stats.getTimeouts());
        printMetric(searchMetricPath, "Average Requests Per Minute", SolrHelper.multipyBy(stats.getAvgRequests(), 60));
        printMetric(searchMetricPath, "Average Requests Per Second", stats.getAvgRequests());
        printMetric(searchMetricPath, "5 min Rate Requests Per Minute", stats.getFiveMinRateRequests());
        printMetric(searchMetricPath, "Average Time Per Request (milliseconds)", stats.getAvgTimePerRequest());
    }

    private void printMetrics(String collection, CacheStats cacheStats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "Cores |" + collection + "|" + "CACHE|";
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

    private void printMetrics(String collection, MemoryStats memoryStats) {
        if ("".equals(collection)) {
            collection = "Collection";
        }
        String metricPath = "Cores |" + collection + "|" + "MEMORY|";
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

    /**
     * Prints Metrics to AppDynamics Metric Browser
     *
     * @param metricPath
     * @param metricName
     * @param metricValue
     */
    private void printMetric(String metricPath, String metricName, Object metricValue) {
        printMetric(getMetricPrefix() + metricPath, metricName, metricValue, MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE,
                MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE, MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL);
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

    private String getConfigFilename(String filename) {
        if (filename == null) {
            return "";
        }
        // for absolute paths
        if (new File(filename).exists()) {
            return filename;
        }
        // for relative paths
        File jarPath = PathResolver.resolveDirectory(AManagedMonitor.class);
        String configFileName = "";
        if (!Strings.isNullOrEmpty(filename)) {
            configFileName = jarPath + File.separator + filename;
        }
        return configFileName;
    }

    private String getMetricPrefix() {
        if (!metric_path_prefix.endsWith("|")) {
            metric_path_prefix += METRIC_SEPARATOR;
        }
        return metric_path_prefix;
    }

    private String logVersion() {
        String msg = "Using Monitor Version [" + getImplementationVersion() + "]";
        return msg;
    }
}
