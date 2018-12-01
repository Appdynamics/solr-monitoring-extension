/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricCollector;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * Created by bhuvnesh.kumar on 5/2/18.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class MetricCollectorSystemMetricsTest {
    @Mock
    private TasksExecutionServiceProvider serviceProvider;
    @Mock
    private MetricWriteHelper metricWriter;
    @Mock
    private Phaser phaser;
    private Stat.Stats stat;
    private MetricCollector metricCollector;
    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor",
            "Custom Metrics|Solr|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
    private Map<String, String> expectedValueMap = new HashMap<String, String>();
    private Map server = new HashMap();

    @Before
    public void before() {
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("src/test/resources/xml/SystemMetrics.xml", Stat.Stats.class);
        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);
        stat = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
        server.put("host", "localhost");
        server.put("port", "8983");
        server.put("name", "Server 1");

        String endpoint = "testEndpoint";
        String collectionName = "techproducts";
        List<String> collections = new ArrayList<String>();
        collections.add(collectionName);
        server.put("collectionName", collections);
        metricCollector = new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser, metricWriter, endpoint, collectionName);
        PowerMockito.mockStatic(HttpClientUtils.class);
        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String file = "/json/system.json";
                        return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);
                    }
                });
    }
    @Test
    public void testMetricCollectorForSystemMetrics() {
        expectedValueMap = new HashMap<String, String>();
        initExpectedSystemandMemoryMetrics();
        initExpectedSystemAndSystemMetrics();
        addHeartBeatMetricOne();
        metricCollector.run();
        validateMetricsList();
    }

    private void validateMetricsList() {
        Map<String, Metric> mapOfMetrics = metricCollector.getMetricsMap();
        for (String prefix : mapOfMetrics.keySet()) {
            String actualValue = mapOfMetrics.get(prefix).getMetricValue();
            String metricPath = mapOfMetrics.get(prefix).getMetricPath();
            if (expectedValueMap.containsKey(metricPath)) {
                String expectedValue = expectedValueMap.get(metricPath);
                Assert.assertEquals(expectedValue, actualValue);
            }
        }
    }

    private void initExpectedSystemAndSystemMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Available Processors", "8.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|System Load Average", "2.455078125");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Committed Virtual Memory Size", "6.57563648E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Free Physical Memory Size", "2.1737472E7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Free Swap Space Size", "1.200881664E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Process CPU Time", "4.52325456E11");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Total Physical Memory Size", "1.7179869184E10");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Total Swap Space Size", "5.36870912E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Max File Descriptor Count", "10240.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Open File Descriptor Count", "205.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|System|Process CPU Load", "0.4583333333333333");
    }

    private void initExpectedSystemandMemoryMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|Free MB", "430.6");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|Total MB", "490.7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|Max MB", "490.7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|Used MB", "60.1");
    }

    private void addHeartBeatMetricOne() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|HeartBeat", "1");
    }
}