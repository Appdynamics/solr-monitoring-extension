/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricDataParser;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 5/23/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")

public class MetricDataParserTest {

    private String collectionName = "techproducts";
    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor", "Custom Metrics|Solr|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));

    @Test
    public void testSystemMetricsWithoutAlias() throws Exception {
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("src/test/resources/xml/system-no-alias.xml", Stat.Stats.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("src/test/resources/json/system-exact.json"), JsonNode.class);
        MetricDataParser metricDataParser = new MetricDataParser(monitorContextConfiguration, collectionName);
        String serverName = "Server 1";
        Map<String, Metric> result = metricDataParser.parseNodeData(getStat(), node, serverName, getPropertiesMap());
        Map<String, String> expectedValueMap = initExpectedSystemMetricsWithoutAlias();
        validateMetricsList(result, expectedValueMap);
    }

    @Test
    public void testSystemMetricsWithAlias() throws Exception {
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("src/test/resources/xml/system-exact.xml", Stat.Stats.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("src/test/resources/json/system-exact.json"), JsonNode.class);
        MetricDataParser metricDataParser = new MetricDataParser(monitorContextConfiguration, collectionName);
        String serverName = "Server 1";
        Map<String, Metric> result = metricDataParser.parseNodeData(getStat(), node, serverName, getPropertiesMap());
        Map<String, String> expectedValueMap = initExpectedSystemMetrics();
        validateMetricsList(result, expectedValueMap);
    }

    private void validateMetricsList(Map<String, Metric> mapOfMetrics, Map<String, String> expectedValueMap) {
        for (String prefix : mapOfMetrics.keySet()) {
            String actualValue = mapOfMetrics.get(prefix).getMetricValue();
            String metricPath = mapOfMetrics.get(prefix).getMetricPath();
            if (expectedValueMap.containsKey(metricPath)) {
                String expectedValue = expectedValueMap.get(metricPath);
                Assert.assertEquals("The value of metric " + metricPath + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricPath);
            } else {
                Assert.fail("Unknown Metric " + metricPath);
            }
        }
    }

    private Map<String, String> getPropertiesMap() {
        Map<String, String> properties = new LinkedHashMap<String, String>();
        properties.put("system", "System");
        return properties;
    }

    private Stat getStat() {
        return ((Stat.Stats) monitorContextConfiguration.getMetricsXml()).getStats()[0];
    }

    private Map<String, String> initExpectedSystemMetrics() {
        Map<String, String> expectedValueMap = new HashMap<String, String>();
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Committed Virtual Memory Size", "6.57563648E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Process CPU Load", "0.4583333333333333");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Total Physical Memory Size", "1.7179869184E10");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Process CPU Time", "4.52325456E11");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Total Swap Space Size", "5.36870912E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Available Processors", "8.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Free Physical Memory Size", "2.1737472E7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Free Swap Space Size", "1.200881664E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Max File Descriptor Count", "10240.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Open File Descriptor Count", "205.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|System Load Average", "2.455078125");
        return expectedValueMap;
    }

    private Map<String, String> initExpectedSystemMetricsWithoutAlias() {
        Map<String, String> expectedValueMap = new HashMap<String, String>();
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|openFileDescriptorCount", "205.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|totalPhysicalMemorySize", "1.7179869184E10");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|availableProcessors", "8.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|systemLoadAverage", "2.455078125");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|freePhysicalMemorySize", "2.1737472E7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|processCpuLoad", "0.4583333333333333");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|maxFileDescriptorCount", "10240.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|committedVirtualMemorySize", "6.57563648E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|totalSwapSpaceSize", "5.36870912E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|freeSwapSpaceSize", "1.200881664E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|processCpuTime", "4.52325456E11");
        return expectedValueMap;
    }
}