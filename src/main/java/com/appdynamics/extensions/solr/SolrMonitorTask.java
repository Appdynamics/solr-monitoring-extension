/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricCollector;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Phaser;

import static com.appdynamics.extensions.solr.utils.Constants.NAME;

public class SolrMonitorTask implements AMonitorTaskRunnable {
    private static final Logger logger = LoggerFactory.getLogger(SolrMonitorTask.class);
    private Map server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;

    public SolrMonitorTask(MonitorContextConfiguration monitorContextConfiguration, MetricWriteHelper metricWriteHelper,
                           Map<String, String> server) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.metricWriteHelper = metricWriteHelper;
    }

    public void onTaskComplete() {
        logger.info("Completed the Solr Monitoring Task for server {}", server.get(NAME));
    }


    public void run() {
        try {
            logger.debug("Created Task and starting work for Server: {}", server.get(NAME));
            Phaser phaser = new Phaser();
            Stat.Stats metricConfiguration = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
            AssertUtils.assertNotNull(metricConfiguration.getStats(), "The stat inside of stats are empty.");
            for (Stat stat : metricConfiguration.getStats()) {
                MetricCollector metricCollector = new MetricCollector(stat, monitorContextConfiguration, server, phaser, metricWriteHelper);
                monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask-"+stat.getAlias(), metricCollector);
                logger.debug("Registering MetricCollectorTask phaser for {}", server.get(NAME));
            }
            phaser.arriveAndAwaitAdvance();
            logger.info("Completed the Solr Metric Monitoring task");
        } catch (Exception e) {
            logger.error("An error was encountered during the Solr Monitoring Task for server : " + server.get(NAME), e.getMessage());
        }
    }


}
