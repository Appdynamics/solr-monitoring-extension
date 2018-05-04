package com.appdynamics.extensions.solr;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricCollector;
import com.appdynamics.extensions.solr.metrics.MetricDataParser;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.yml.YmlReader;
import org.junit.runner.RunWith;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
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

public class MetricCollectorTest {


    @Mock
    private TasksExecutionServiceProvider serviceProvider;

    @Mock
    private MetricWriteHelper metricWriter;

    @Mock
    private MetricDataParser dataParser;


    @Mock
    private Phaser phaser;

    private Stat.Stats stat;

    private MetricCollector metricCollector;

    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor", "Custom Metrics|Solr|",Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));

    public static final Logger logger = Logger.getLogger(MetricCollectorTest.class);

    private Map<String, String> expectedValueMap = new HashMap<String, String>();

    private List<Metric> metrics = new ArrayList<Metric>();

    private Map<String, String> server = new HashMap<String, String>();
    private List<Map<String, String>> metricReplacer = new ArrayList<Map<String, String>>();

    @Before
    public void before() {

        monitorContextConfiguration.setConfigYml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
        monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/metrics.xml", Stat.Stats.class);

        Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

        stat = (Stat.Stats) monitorContextConfiguration.getMetricsXml();

        dataParser = Mockito.spy(new MetricDataParser(monitorContextConfiguration));

        metricCollector = Mockito.spy(new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser, metricWriter, metricReplacer));


        PowerMockito.mockStatic(HttpClientUtils.class);

        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String url = (String) invocationOnMock.getArguments()[1];
                        String var="ArrayNode";
                        String file = null;
                        if (url.contains("/nodes")) {
                            file = "/json/nodes.json";
                        } else if (url.contains("/channels")) {
                            file = "/json/channels.json";
                        } else if (url.contains("/queues")) {
                            file = "/json/queues.json";
                        } else if (url.contains("/overview")) {
                            file = "/json/overview.json";
                        }
                        logger.info("Returning the mocked data for the api " + file);
                        if(url.contains("/overview")){
                            return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);
                        }else {
                            return mapper.readValue(getClass().getResourceAsStream(file), ArrayNode.class);
                        }
                    }
                });

    }

    }
