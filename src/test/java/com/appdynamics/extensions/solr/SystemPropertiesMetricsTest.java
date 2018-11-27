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
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.verify;

/**
 * Created by bhuvnesh.kumar on 5/2/18.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")
public class SystemPropertiesMetricsTest {
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
    private Map server = new HashMap();


    @Before
    public void before() {
        String endpoint = "testEndpoint";
        String collectionName = "techproducts";
        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("src/test/resources/xml/SystemMetricsForProps.xml", Stat.Stats.class);
        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);
        stat = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
        server.put("host", "localhost");
        server.put("port", "8983");
        server.put("name", "Server 1");
        List<String> collections = new ArrayList<String>();
        collections.add(collectionName);
        server.put("collectionName", collections);
        metricCollector = Mockito.spy(new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser,
                metricWriter, endpoint, collectionName));
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
    //TODO: rename this test to something more meaningful
    //TODO: add a test for a failure case (throwing an exception)
    public void testWithSystemMetrics() {
        metricCollector.run();
        validateProperties();
    }

    private void validateProperties() {
        ArgumentCaptor<List> pathCaptor = ArgumentCaptor.forClass(List.class);

        verify(metricWriter).transformAndPrintMetrics(pathCaptor.capture());
        List<String> metricPathsList = Lists.newArrayList();
        metricPathsList.add("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|Free Normal");
        metricPathsList.add("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|RAW|Free RAW");
        metricPathsList.add("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|RAW|Free Multiplied");
        metricPathsList.add("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|HeartBeat");
        for (Metric metric : (List<Metric>) pathCaptor.getValue()) {
            if (metric.getMetricName().equals("Free Multiplied")) {
                /* TODO: it is a better practice to use assertEquals instead of assertTrue, as the former will provide the JUnit framework with better information about the components being tested. This will also give you a more sensible error message in case of a failure. Something to keep in mind moving forward.
                 */
                Assert.assertTrue(metric.getMetricPath().equals("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|JVM|Memory|RAW|Free Multiplied"));
                Assert.assertTrue(metric.getMetricProperties().getClusterRollUpType().equals("COLLECTIVE"));
                Assert.assertTrue(metric.getMetricProperties().getTimeRollUpType().equals("SUM"));
                Assert.assertTrue(metric.getMetricProperties().getAggregationType().equals("AVERAGE"));
                Assert.assertTrue(metric.getMetricProperties().getMultiplier().toString().equals("0.001"));
                Assert.assertTrue(metric.getMetricValue().equals("4.51509616E8"));
            }

            if (metric.getMetricName().equals("HeartBeat")) {
                Assert.assertTrue(metric.getMetricPath().equals("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|HeartBeat"));
                Assert.assertTrue(metric.getMetricProperties().getClusterRollUpType().equals("INDIVIDUAL"));
                Assert.assertTrue(metric.getMetricProperties().getTimeRollUpType().equals("AVERAGE"));
                Assert.assertTrue(metric.getMetricProperties().getAggregationType().equals("AVERAGE"));
                Assert.assertTrue(metric.getMetricProperties().getMultiplier().toString().equals("1"));
                Assert.assertTrue(metric.getMetricValue().equals("1"));
            }
        }
    }
}