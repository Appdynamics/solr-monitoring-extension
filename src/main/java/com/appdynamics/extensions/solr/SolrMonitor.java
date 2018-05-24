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

    public static void main(String[] args) throws TaskExecutionException, IOException {

        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level
                .DEBUG);
        org.apache.log4j.Logger.getRootLogger().addAppender(ca);


//        FileAppender fa = new FileAppender(new PatternLayout("%-5p [%t]: %m%n"), "cache.log");
//        fa.setThreshold(Level.DEBUG);
//        logger.getRootLogger().addAppender(fa);


        SolrMonitor solrMonitor = new SolrMonitor();
        Map<String, String> argsMap = new HashMap<String, String>();
        argsMap.put("config-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
        argsMap.put("metric-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/NewMetrics.xml");
//        argsMap.put("metric-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/SystemMetrics.xml");

        solrMonitor.execute(argsMap, null);
    }

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
        AssertUtils.assertNotNull(getContextConfiguration().getMetricsXml(), "The metrics.xml has been not been created.");
        for (Map<String, String> server : servers) {
            logger.debug("Starting the Solr Monitoring Task for server : " + server.get(NAME));
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
        getContextConfiguration().setMetricXml(args.get("metric-file"), Stat.Stats.class);

    }


}


