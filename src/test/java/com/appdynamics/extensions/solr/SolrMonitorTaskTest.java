package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.solr.config.Core;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.fasterxml.jackson.databind.JsonNode;
import com.singularity.ee.agent.systemagent.api.MetricWriter;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

/**
 * Created by adityajagtiani on 10/27/16.
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest(SolrHelper.class)
public class SolrMonitorTaskTest {
    private Map server;
    private MonitorConfiguration configuration;
    private SolrMonitorTask solrMonitorTask;
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private SolrHelper solrHelper;
    private Map<String, ?> config;
    private StatusLine statusLine;
    private JsonNode jsonNode;
    private MetricWriteHelper metricWriter;
    private SolrHelper helper;
    private JsonNode jsonNodeTemp;
    private Map<String, JsonNode> solrMBeanHandler;

    @Before
    public void setup () throws IOException {
        server = new HashMap<String, String>();
        server.put("uri", "http://localhost:8983");
        configuration = mock(MonitorConfiguration.class);
        helper = mock(SolrHelper.class);
        solrMonitorTask = new SolrMonitorTask(configuration, server, helper);
        httpClient = mock(CloseableHttpClient.class);
        solrHelper = new SolrHelper(httpClient);
        config = mock(Map.class);
        httpResponse = mock(CloseableHttpResponse.class);
        statusLine = mock(StatusLine.class);
        PowerMockito.mockStatic(SolrHelper.class);
        jsonNode = mock(JsonNode.class);
        metricWriter = mock(MetricWriteHelper.class);
        jsonNodeTemp = mock(JsonNode.class);
        solrMBeanHandler = mock(Map.class);

        Core core = new Core();
        core.setName("TestCore");
        core.setPingHandler("TestPingHandler");
        core.setQueryHandlers(new ArrayList<String>());
        List<Core> cores = new ArrayList<Core>();
        cores.add(core);

        doReturn(config).when(configuration).getConfigYml();
        when(helper.getCores(config)).thenReturn(cores);
        when(helper.getHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(SolrHelper.getJsonNode(httpResponse)).thenReturn(jsonNode);
        when(configuration.getMetricWriter()).thenReturn(metricWriter);
        when(jsonNode.path(Mockito.anyString())).thenReturn(jsonNodeTemp);
        when(configuration.getMetricPrefix()).thenReturn("prefix");
    }

    @Test
    public void runTest_isNotAPingHandler () throws IOException {
        when(jsonNodeTemp.asText()).thenReturn("Not OK");
        solrMonitorTask.run();
        Mockito.verify(metricWriter, times(1)).printMetric(Mockito.anyString(), Mockito.anyString(), eq(MetricWriter
                .METRIC_AGGREGATION_TYPE_AVERAGE), eq(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE), eq(MetricWriter
                .METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL));
    }

    @Test
    public void runTest_isAPingHandler_MBeanHandlerNotSupported () throws IOException {
        when(jsonNodeTemp.asText()).thenReturn("OK");
        when(helper.checkIfMBeanHandlerSupported(Mockito.anyString())).thenReturn(false);
        solrMonitorTask.run();
        Mockito.verify(metricWriter, times(1)).printMetric(Mockito.anyString(), Mockito.anyString(), eq(MetricWriter
                .METRIC_AGGREGATION_TYPE_AVERAGE), eq(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE), eq(MetricWriter
                .METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL));
    }

    @Test
    public void runTest_isAPingHandler_MBeanHandlerSupported () throws IOException {
        when(jsonNodeTemp.asText()).thenReturn("OK");
        when(helper.checkIfMBeanHandlerSupported(Mockito.anyString())).thenReturn(true);
        when(helper.getSolrMBeansHandlersMap(Mockito.anyString(), Mockito.anyString())).thenReturn(solrMBeanHandler);
        solrMonitorTask.run();
        Mockito.verify(metricWriter, times(1)).printMetric(Mockito.anyString(), Mockito.anyString(), eq(MetricWriter
                .METRIC_AGGREGATION_TYPE_AVERAGE), eq(MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE), eq(MetricWriter
                .METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL));
    }
}