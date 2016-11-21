package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.core.CoreContext;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class SolrMonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SolrMonitorTask.class);
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
            CoreContext coreContext = new CoreContext(configuration.getHttpClient(),server);
            //TODO refactor the get cores method..the url building should be part of the CoreContext class
            List<Core> cores = coreContext.getCores(configuration.getConfigYml(), generateURIFromConfig());
            populateAndPrintStats(cores,coreContext.getContextRoot());
            logger.info("Solr monitoring task completed successfully.");
        } catch (Exception e) {
            logger.error("Exception while running Solr Monitor Task ", e);
        }
    }



    private void populateAndPrintStats (List<Core> coresConfig,String contextRoot)
            throws IOException {
        NewSolrStats solrStats = new NewSolrStats(server,contextRoot,configuration.getHttpClient());
        for (Core coreConfig : coresConfig) {
            Map<String, Long> metrics = solrStats.populateStats(coreConfig);
            printMetrics(metrics);
        }

    }

    void printMetrics (Map<String, Long> solrMetrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        String cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        String timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;

        for (Map.Entry<String, Long> entry : solrMetrics.entrySet()) {
            String metricPath = metricPrefix + "|" + server.get("name").toString() + entry.getKey();
            String metricValue = String.valueOf(entry.getValue());
            metricWriter.printMetric(metricPath, metricValue, aggregation, timeRollup, cluster);
        }
    }

    //TODO move this code inside CoreContext as this method doesn't belong to this class.
    private String generateURIFromConfig() {
        return UrlBuilder.fromYmlServerConfig(server).build();
    }
}
