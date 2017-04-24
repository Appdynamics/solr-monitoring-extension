package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.core.CoreContext;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class SolrMonitorTask implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SolrMonitorTask.class);
    private MonitorConfiguration configuration;
    private Map server;

    public SolrMonitorTask(MonitorConfiguration configuration, Map server) {
        this.configuration = configuration;
        this.server = server;
    }

    public void run() {
        try {
            runTask();
            logger.info("Solr Metric Upload Complete");
        } catch (Exception ex) {
            configuration.getMetricWriter().registerError(ex.getMessage(), ex);
            logger.error("Error while running the task", ex);
        }
    }

    private void runTask() {
        try {
            CoreContext coreContext = new CoreContext(configuration.getHttpClient(), server);
            List<Core> cores = coreContext.getCores(configuration.getConfigYml());
            populateAndPrintStats(cores, coreContext.getContextRoot());
            logger.info("Solr monitoring task completed successfully.");
        } catch (Exception e) {
            logger.error("Exception while running Solr Monitor Task ", e);
        }
    }

    private void populateAndPrintStats(List<Core> coresConfig, String contextRoot) throws IOException {
        SolrStats solrStats = new SolrStats(server, contextRoot, configuration.getHttpClient());
        for (Core coreConfig : coresConfig) {
            Map<String, BigDecimal> metrics = solrStats.populateStats(coreConfig);
            printMetrics(metrics);
        }

    }

    private void printMetrics(Map<String, BigDecimal> solrMetrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        String cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        String timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;

        for (Map.Entry<String, BigDecimal> entry : solrMetrics.entrySet()) {
            String metricPath = metricPrefix + "|" + server.get("name").toString() + entry.getKey();
            String metricValue = String.valueOf(entry.getValue());
            metricWriter.printMetric(metricPath, metricValue, aggregation, timeRollup, cluster);
        }
    }
}
