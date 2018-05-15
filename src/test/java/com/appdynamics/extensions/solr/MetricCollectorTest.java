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
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
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

        server.put("host","localhost");
        server.put("port","8983");
        server.put("name", "Server1");
        server.put("collectionName", "techproducts");

        metricCollector = Mockito.spy(new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser, metricWriter, metricReplacer));


        PowerMockito.mockStatic(HttpClientUtils.class);

        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).then(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String url = (String) invocationOnMock.getArguments()[1];
                        String var="ArrayNode";
                        String file = null;
                        if (url.contains("/mbeans")) {
                            file = "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/json/mbeans.json";
                        } else if (url.contains("/system")) {
                            file = "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/json/system.json";
                        }
                        logger.info("Returning the mocked data for the api " + file);

                        return mapper.readValue(getClass().getResourceAsStream(file), ArrayNode.class);

                    }
                });

    }
    @Test
    public void testWithSystemMetrics() throws TaskExecutionException {

        expectedValueMap = new HashMap<String, String>();
        initExpectedSystemandMemoryMetrics();
        initExpectedSystemAndSystemMetrics();

        metricCollector.run();
        int a  =  3;


    }

    private void validateMetrics(){
        for(Metric metric: metricCollector.getMetricsList()){

            String actualValue = metric.getMetricValue();
            String metricName = metric.getMetricName();
            if(expectedValueMap.containsKey(metricName)){
                String expectedValue = expectedValueMap.get(metricName);
                Assert.assertEquals("The value of metric " + metricName + " failed", expectedValue, actualValue);
                expectedValueMap.remove(metricName);
            }
            else {
                System.out.println("\"" + metricName + "\",\"" + actualValue + "\"");
                Assert.fail("Unknown Metric " + metricName);
            }

        }
    }

    private void initExpectedMBeanCACHEperSegFilterMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Evictions", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Size", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Warmup Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Evictions", "0");
    }

    private void initExpectedMBeanCACHEqueryResultCacheMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Evictions", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Size", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Warmup Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Evictions", "0");

    }

    private void initExpectedMBeanCACHEdocumentCacheMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Evictions", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Size", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Warmup Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Evictions", "0");
    }

    private void initExpectedMBeanCACHEfilterCacheMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Evictions", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Size", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Warmup Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Lookups", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Inserts", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hits", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hit Ratio", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Evictions", "0");
    }

    private void initExpectedMBeanCACHEfieldCacheMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|fieldCache|Entries Count", "0");
    }

    private void initExpectedMBeanCOREandCoreMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Total Space", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Size In Bytes", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|SEARCHER New Errors", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Ref Count", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Usable Space", "0");
    }

    private void initExpectedMBeanCOREsearcherMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Max Docs", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Deleted Docs", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Warmup Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Index Version", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Number Of Docs", "0");
    }

    private void initExpectedMBeanQUERYandSQLMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Total Time", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Client Error Count", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Timeout", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Server Errors", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Request Times Mean", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Handlet Start", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Error Count", "0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Requests", "0");
    }



    private void initExpectedSystemAndSystemMetrics(){
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Available Processors", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|System Load Average", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Committed Virtual Memory Size", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Physical Memory Size", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Swap Space Size", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process Cpu Time", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Physical Memory Size", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Swap Space Size", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Max File Descriptor Count", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Open File Descriptor Count", "6.576627712E9");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process CPU Load", "6.576627712E9");
    }

    private void initExpectedSystemandMemoryMetrics() {
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Free MB", "411.0");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Total MB", "490.7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Max MB", "490.7");
        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Used MB", "79.6");

    }

}
