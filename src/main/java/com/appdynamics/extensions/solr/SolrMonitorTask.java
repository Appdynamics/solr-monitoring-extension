package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.core.CoreContextStats;
import org.apache.http.impl.client.CloseableHttpClient;
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
            CloseableHttpClient httpClient = configuration.getHttpClient();
            CoreContextStats coreContextStats = new CoreContextStats();
            List<Core> cores = coreContextStats.getCores(configuration.getConfigYml(), httpClient, generateURIFromConfig());
            populateAndPrintStats(cores);
            logger.info("Solr monitoring task completed successfully.");
        } catch (Exception e) {
            logger.error("Exception while running Solr Monitor Task ", e);
        }
    }

    private void populateAndPrintStats (List<Core> coresConfig)
            throws IOException {
        SolrStats stats = new SolrStats(configuration, (String) server.get("name"), generateURIFromConfig());
        for (Core coreConfig : coresConfig) {
            Map<String, Long> metrics = stats.populateStats(coreConfig);
            stats.printMetrics(metrics);
        }
    }

    private String generateURIFromConfig() {
        return UrlBuilder.fromYmlServerConfig(server).build();
    }
}
