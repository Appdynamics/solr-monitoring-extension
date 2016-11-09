package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.solr.config.Core;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.appdynamics.extensions.util.MetricWriteHelper;
import com.appdynamics.extensions.util.MetricWriteHelperFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.ee.agent.systemagent.api.AManagedMonitor;
import com.singularity.ee.agent.systemagent.api.TaskExecutionContext;
import com.singularity.ee.agent.systemagent.api.TaskOutput;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.Mockito.*;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(SolrUtils.class)
public class CoreContextStatsTest {

    @Test
    public void getCores_whenNoCoresPresent() throws IOException {
        MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(new AManagedMonitor() {
            public TaskOutput execute (Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
                return null;
            }
        });
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        MonitorConfiguration configuration = new MonitorConfiguration("Prefix", new TaskRunner(), metricWriteHelper);
        configuration.setConfigYml("src/test/resources/conf/config_with_no_cores.yml");
        CoreContextStats coreContextStats = new CoreContextStats();
        MonitorConfiguration newConfigurationForMocking = mock(MonitorConfiguration.class);
        CloseableHttpResponse response = mock(CloseableHttpResponse.class);
        when(newConfigurationForMocking.getHttpClient()).thenReturn(httpClient);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        PowerMockito.mockStatic(SolrUtils.class);
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/DefaultCoreJSON"), JsonNode.class);
        when(SolrUtils.getJsonNode(response)).thenReturn(jsonNode);
        List<Core> cores = coreContextStats.getCores(configuration.getConfigYml(), httpClient, "dummy_uri");
        Assert.assertTrue(cores.get(0).getName().equals(""));
        Assert.assertTrue(cores.get(0).getQueryHandlers().size() == 0);
        Assert.assertTrue(cores.get(0).getPingHandler() == null);
    }

    @Test
    public void getCores_whenCoreIsPresent() throws IOException {
        MetricWriteHelper metricWriteHelper = MetricWriteHelperFactory.create(new AManagedMonitor() {
            public TaskOutput execute (Map<String, String> map, TaskExecutionContext taskExecutionContext) throws TaskExecutionException {
                return null;
            }
        });
        CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        MonitorConfiguration configuration = new MonitorConfiguration("Prefix", new TaskRunner(), metricWriteHelper);
        configuration.setConfigYml("src/test/resources/conf/config_with_core.yml");
        CoreContextStats coreContextStats = new CoreContextStats();
        List<Core> cores = coreContextStats.getCores(configuration.getConfigYml(), httpClient, "");
        Assert.assertTrue(cores.get(0).getName().equals("gettingstarted"));
        Assert.assertTrue(cores.get(0).getPingHandler().equals("/admin/ping"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().get(0).equals("/select"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().get(1).equals("/update"));
    }

    private class TaskRunner implements Runnable {
        public void run () {}
    }
}
