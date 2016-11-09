package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.solr.Memory.MemoryMetricsHandler;
import com.appdynamics.extensions.solr.helpers.PingHandlerHelper;
import com.appdynamics.extensions.solr.helpers.SolrHelper;
import com.appdynamics.extensions.solr.config.Core;
import com.appdynamics.extensions.solr.Cache.CacheMetricsHandler;
import com.appdynamics.extensions.solr.Core.CoreStatsPopulator;
import com.appdynamics.extensions.solr.Query.QueryStatsPopulator;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SolrStats {

    private static final Logger logger = LoggerFactory.getLogger(SolrHelper.class);
    private static final String METRIC_SEPARATOR = "|";
    private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";
    private static String context_root = "/solr";
    private static String plugins_uri = "/%s/admin/plugins?wt=json";
    private static String memory_uri = "/%s/admin/system?stats=true&wt=json";
    private MonitorConfiguration configuration;
    private String serverName;
    private String uri;

    public SolrStats (MonitorConfiguration configuration, String serverName, String uri) {
        this.configuration = configuration;
        this.serverName = serverName;
        this.uri = uri;
    }

    public Map<String, String> populateStats (Core coreConfig) throws IOException {
        Map<String, String> solrMetrics = new HashMap<String, String>();
        Map<String, JsonNode> solrMBeansHandlersMap = new HashMap<String, JsonNode>();
        String core = coreConfig.getName();
        CloseableHttpClient httpClient = configuration.getHttpClient();
        SolrHelper helper = new SolrHelper(httpClient);
        PingHandlerHelper pingHandlerHelper = new PingHandlerHelper();

        if (!pingHandlerHelper.isPingHandler(coreConfig, httpClient, uri)) {
            solrMetrics.put(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR + " " +
                    "Ping Status", String.valueOf(0));
        } else {
            solrMetrics.put(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR + " " +
                    "Ping Status", String.valueOf(1));
            if (helper.checkIfMBeanHandlerSupported(uri + "/" + String.format(context_root + plugins_uri, core))) {
                try {
                    solrMBeansHandlersMap = helper.getSolrMBeansHandlersMap(core, uri + "/" + context_root +
                            mbeansUri);
                } catch (Exception e) {
                    logger.error("Error in retrieving mbeans info for " + core);
                }

                try {
                    CoreStatsPopulator coreStatsPopulator = new CoreStatsPopulator(core);
                    solrMetrics.putAll(coreStatsPopulator.populateStats(solrMBeansHandlersMap));
                } catch (Exception e) {
                    logger.error("Error Retrieving Core Stats for " + core, e);
                }

                try {
                    for (String handler : coreConfig.getQueryHandlers()) {
                        QueryStatsPopulator queryStatsPopulator = new QueryStatsPopulator(core);
                        solrMetrics.putAll(queryStatsPopulator.populateStats(solrMBeansHandlersMap,
                                handler));
                    }
                } catch (Exception e) {
                    logger.error("Error Retrieving Query Stats for " + core, e);
                }

                try {
                    CacheMetricsHandler cacheMetricsHandler = new CacheMetricsHandler(solrMBeansHandlersMap, core);
                    solrMetrics.putAll(cacheMetricsHandler.populate());
                } catch (Exception e) {
                    logger.error("Error Retrieving Cache Stats for " + core, e);
                }
            }

            CloseableHttpResponse response = null;
            try {
                uri += "/" + context_root + String.format(memory_uri, core);
                HttpGet get = new HttpGet(uri);
                response = httpClient.execute(get);
                MemoryMetricsHandler memoryMetricsHandler = new MemoryMetricsHandler(response, core);
                solrMetrics.putAll(memoryMetricsHandler.populate());
            } catch (Exception e) {
                logger.error("Error retrieving memory stats for " + core, e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
        return solrMetrics;
    }

    public void printMetrics (Map<String, String> solrMetrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        String cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        String timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;

        for (Map.Entry<String, String> entry : solrMetrics.entrySet()) {
            String metricPath = metricPrefix + METRIC_SEPARATOR + serverName + entry.getKey();
            String metricValue = entry.getValue();
            metricWriter.printMetric(metricPath, metricValue, aggregation, timeRollup, cluster);
        }
    }
}
