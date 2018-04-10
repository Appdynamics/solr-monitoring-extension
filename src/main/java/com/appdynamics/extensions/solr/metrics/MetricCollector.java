package com.appdynamics.extensions.solr.metrics;
/**
 * Created by bhuvnesh.kumar on 4/10/18.
 */

import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.SolrMonitorTask;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.core.CoreContext;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.solr.input.Stat;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;


public class MetricCollector {

    private static final Logger logger = LoggerFactory.getLogger(MetricCollector.class);
    private Stat stat;
    private Phaser phaser;
    private Map server;
    private MetricWriteHelper metricWriteHelper;
    private MonitorContextConfiguration monitorContextConfiguration;
    private MetricDataParser metricDataParser;
    private String endpoint;
    private String serverName;

    public MetricCollector(Stat stat, MonitorContextConfiguration monitorContextConfiguration, Map<String, String> server,
                           Phaser phaser, MetricWriteHelper metricWriteHelper){
        this.stat = stat;
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.metricDataParser = new MetricDataParser(monitorContextConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + statEndpoint;
    }


}
