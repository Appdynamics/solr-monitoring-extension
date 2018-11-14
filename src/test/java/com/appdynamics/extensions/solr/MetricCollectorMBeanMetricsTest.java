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
import com.appdynamics.extensions.solr.metrics.MetricDataParser;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
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
 * Created by bhuvnesh.kumar on 5/16/18.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)

@PowerMockIgnore("javax.net.ssl.*")


public class MetricCollectorMBeanMetricsTest {

    @Mock
    private TasksExecutionServiceProvider serviceProvider;

    @Mock
    private MetricWriteHelper metricWriter;

    //todo - make this local to before()
    @Mock
    private MetricDataParser dataParser;

    @Mock
    private Phaser phaser;

    private Stat.Stats stat;

    private MetricCollector metricCollector;

    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor", "Custom Metrics|Solr|", Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));

    private Map<String, String> expectedValueMap = new HashMap<String, String>();

    private Map server = new HashMap();

    //TODO - make these Strings local to before
    private String endpoint = "testEndpoint";
    private String collectionName = "techproducts";

    @Before
    public void before() {

        monitorContextConfiguration.setConfigYml("src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("src/test/resources/xml/MbeansMetricsTest.xml", Stat.Stats.class);
        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) monitorContextConfiguration.getMetricsXml();

        dataParser = Mockito.spy(new MetricDataParser(monitorContextConfiguration, collectionName));

        server.put("host", "localhost");
        server.put("port", "8983");
        server.put("name", "Server 1");
        List<String> collections = new ArrayList<String>();
        collections.add(collectionName);
        server.put("collectionName", collections);

        metricCollector = Mockito.spy(new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser, metricWriter, endpoint, collectionName));


        PowerMockito.mockStatic(HttpClientUtils.class);

        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String file = "/json/mbeans.json";
                        return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);
                    }
                });
    }

    @Test
    public void testWithMbeanMetrics() throws TaskExecutionException {
        expectedValueMap = new HashMap<String, String>();
        initExpectedMBeanCACHEdocumentCacheMetrics();
        initExpectedMBeanCACHEfieldCacheMetrics();
        initExpectedMBeanCACHEfilterCacheMetrics();
        initExpectedMBeanCACHEperSegFilterMetrics();
        initExpectedMBeanCACHEqueryResultCacheMetrics();
        initExpectedMBeanCOREandCoreMetrics();
        initExpectedMBeanCOREsearcherMetrics();
        initExpectedMBeanQUERYandSQLMetrics();
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
                Assert.assertEquals("The value of metric " + metricPath + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricPath);
            } else {
                System.out.println("expectedValueMap.put(\"" + metricPath + "\",\"" + actualValue + "\")");
                Assert.fail("Unknown Metric " + metricPath);
            }

        }
    }

    private void initExpectedMBeanCACHEperSegFilterMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Cumulative Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Warmup Time", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Cumulative Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Cumulative Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Inserts", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Size", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Cumulative Inserts", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Cumulative Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Per Seg Filter|Hits", "0.0");
    }

    private void initExpectedMBeanCACHEqueryResultCacheMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Size", "1.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Cumulative Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Cumulative Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Inserts", "1.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Cumulative Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Cumulative Inserts", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Cumulative Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Query Result Cache|Warmup Time", "0.0");
    }

    private void initExpectedMBeanCACHEdocumentCacheMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Cumulative Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Cumulative Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Warmup Time", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Cumulative Inserts", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Cumulative Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Inserts", "4.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Size", "4.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Document Cache|Cumulative Lookups", "0.0");
    }

    private void initExpectedMBeanCACHEfilterCacheMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Cumulative Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Inserts", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Cumulative Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Hit Ratio", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Cumulative Hits", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Evictions", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Size", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Cumulative Lookups", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Warmup Time", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Filter Cache|Cumulative Inserts", "0.0");
    }

    private void initExpectedMBeanCACHEfieldCacheMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Cache|Field Cache|Entries Count", "0.0");
    }

    private void initExpectedMBeanCOREandCoreMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Core|Size In Bytes", "26907.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Core|SEARCHER New Errors", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Core|Usable Space", "1.16875075584E11");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Core|Total Space", "4.99963170816E11");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Core|Ref Count", "2.0");
    }

    private void initExpectedMBeanCOREsearcherMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Searcher|Deleted Docs", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Searcher|Index Version", "6.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Searcher|Max Docs", "28.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Searcher|Warmup Time", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Core|Searcher|Number Of Docs", "28.0");
    }

    private void initExpectedMBeanQUERYandSQLMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Timeout", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Handlet Start", "1.524505067595E12");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Server Errors", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Requests", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Client Error Count", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Error Count", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Total Time", "0.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|Query|SQL|Request Times Mean", "0.0");
    }


    private void addHeartBeatMetricOne() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|techproducts|HeartBeat", "1");
    }
}
