/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.utils.MetricUtils;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.solr.utils.Constants.*;


public class SolrMonitor extends ABaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);

    @Override
    public String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    public void doRun(TasksExecutionServiceProvider taskExecutor) {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialized");
        for (Map<String, String> server : servers) {
            AssertUtils.assertNotNull(server.get("host"), "The host field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("port"), "The port field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("name"), "The name field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("collectionName"), "The collectionName field can not be empty in the config.yml");
            AssertUtils.assertNotNull(getContextConfiguration().getMetricsXml(), "The metrics.xml has been not been created.");
            logger.debug("Starting the Solr Monitoring Task for server : " + server.get(NAME));
            SolrMonitorTask task = new SolrMonitorTask(getContextConfiguration(), taskExecutor.getMetricWriteHelper(), server);
            taskExecutor.submit(server.get(NAME), task);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List<Map<String, ?>>) getContextConfiguration().getConfigYml().get("servers");
    }

//    @Override
//    protected int getTaskCount() {
//        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
//        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialized");
//        return servers.size();
//    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
        if (servers.get(0) != null) {
            Map<String, String> firstServer = servers.get(0);
            setMetricsXmlBasedOnVersion(firstServer, args);
        } else {
            logger.error("The 'Servers' section cannot be empty. Please add servers to monitor.");
        }
    }

    private void setMetricsXmlBasedOnVersion(Map<String, ?> server, Map<String, String> args) {
        if (MetricUtils.isVersion7OrHigher(server, getContextConfiguration().getContext().getHttpClient())) {
            logger.info("The Solr Version is greater than V7 for server: {}", server.get("name").toString());
            getContextConfiguration().setMetricXml(args.get("metric-file-v7"), Stat.Stats.class);
        } else {
            logger.info("The Solr Version is less than V7 for server: {}", server.get("name").toString());
            getContextConfiguration().setMetricXml(args.get("metric-file-v5"), Stat.Stats.class);
        }
    }
}