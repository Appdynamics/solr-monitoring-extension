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
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.util.AssertUtils;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
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
        AssertUtils.assertNotNull(getContextConfiguration().getMetricsXml(), "The metrics-v7.xml has been not been created.");
        for (Map<String, String> server : servers) {
            logger.debug("Starting the Solr Monitoring Task for server : " + server.get(NAME));
            AssertUtils.assertNotNull(server.get("host"), "The host field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("port"), "The port field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("name"), "The name field can not be empty in the config.yml");
            AssertUtils.assertNotNull(server.get("collectionName"), "The collectionName field can not be empty in the config.yml");
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

        //TODO make a system call and get the value for lucene-> version
        // select the metrics xml file to the one that works
        // for versions 6 and below, give metrics-v5, for more, give metrics-v7

        String url =buildUrl();
        JsonNode jsonNode = HttpClientUtils.getResponseAsJson(getContextConfiguration().getContext().getHttpClient(), url, JsonNode.class);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ?> jsonMap = objectMapper.convertValue(jsonNode, Map.class);
        Map<String, ?> luceneMap = (Map)jsonMap.get("lucene");
        String versionValue = luceneMap.get("solr-spec-version").toString();
        String[] charArray = versionValue.split("\\.");
        int version = Integer.valueOf(charArray[0]);
        boolean isVersion7orMore = version > 7 ? true : false;
        if(isVersion7orMore){
            getContextConfiguration().setMetricXml(args.get("metric-file-v7"), Stat.Stats.class);
        } else {
            getContextConfiguration().setMetricXml(args.get("metric-file-v5"), Stat.Stats.class);

        }

    }

    private String buildUrl( ) {
        List<Map<String, String>> servers = (List<Map<String, String>>) getContextConfiguration().getConfigYml().get("servers");
        Map server = servers.get(0);
        // checking only the first server assuming all servers are on the same version of solr
        return UrlBuilder.fromYmlServerConfig(server).build() + SOLR_WITH_SLASH + server.get(COLLECTIONNAME) + "/admin/system?stats=true&wt=json";
    }

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
//        argsMap.put("metric-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/SystemMetricsForProps.xml");
        argsMap.put("metric-file-v5", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/main/resources/config/metrics-v5.xml");
        argsMap.put("metric-file-v7", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/main/resources/config/metrics-v7.xml");

        //        argsMap.put("metric-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/SystemMetrics.xml");

        solrMonitor.execute(argsMap, null);
    }

}


