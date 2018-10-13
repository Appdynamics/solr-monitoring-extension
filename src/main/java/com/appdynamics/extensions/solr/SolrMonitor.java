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
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.solr.utils.Constants.*;


public class SolrMonitor extends ABaseMonitor {
    private static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private Map<String, String> args;

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
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        for (Map<String, String> server : servers) {
            AssertUtils.assertNotNull(server.get("host"), "The host field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("port"), "The port field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("name"), "The name field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("collectionName"), "The collectionName field can not be empty in the config.yml");
            logger.debug("Starting the Solr Monitoring Task for server : " + server.get(NAME));

            setMetricsXmlBasedOnVersion(server);
            AssertUtils.assertNotNull(getContextConfiguration().getMetricsXml(), "The metrics-v7.xml has been not been created."); // todo: there should be a similar statement for metrics-v5.xml
            SolrMonitorTask task = new SolrMonitorTask(getContextConfiguration(), taskExecutor.getMetricWriteHelper(), server);
            taskExecutor.submit(server.get(NAME), task);
        }
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        this.args = args;
    }

    private void setMetricsXmlBasedOnVersion(Map<String, ?> server) {
        if (MetricUtils.isVersion7orMore(server, getContextConfiguration().getContext().getHttpClient())) {
            logger.debug("The Solr Version is greater than V7 for server: {}", server.get("name").toString());
            getContextConfiguration().setMetricXml(args.get("metric-file-v7"), Stat.Stats.class);
        } else {
            logger.debug("The Solr Version is less than V7 for server: {}", server.get("name").toString());
            getContextConfiguration().setMetricXml(args.get("metric-file-v5"), Stat.Stats.class);
        }
    }

    // todo: don't forget to remove the main method before merging to Master
    public static void main(String[] args) throws TaskExecutionException, IOException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level
                .DEBUG);
        org.apache.log4j.Logger.getRootLogger().addAppender(ca);

        SolrMonitor solrMonitor = new SolrMonitor();
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("config-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/main/resources/config/config.yml");
        argsMap.put("metric-file-v5", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/main/resources/config/metrics-v5.xml");
        argsMap.put("metric-file-v7", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/main/resources/config/metrics-v7.xml");
        solrMonitor.execute(argsMap, null);
    }
}
