package com.appdynamics.extensions.solr;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.appdynamics.extensions.solr.metrics.MetricCollector;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import org.junit.runner.RunWith;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Before;
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

        @Mock
        private MetricDataParser dataParser;


        @Mock
        private Phaser phaser;

        private Stat.Stats stat;

        private MetricCollector metricCollector;

        private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor", "Custom Metrics|Solr|",Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));

        public static final Logger logger = Logger.getLogger(com.appdynamics.extensions.solr.MetricCollectorSystemMetricsTest.class);

        private Map<String, String> expectedValueMap = new HashMap<String, String>();

        private List<Metric> metrics = new ArrayList<Metric>();

        private Map<String, String> server = new HashMap<String, String>();
        private List<Map<String, String>> metricReplacer = new ArrayList<Map<String, String>>();

        @Before
        public void before() {

            monitorContextConfiguration.setConfigYml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
//            monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/MbeansMetricsTest.xml", Stat.Stats.class);
            monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/metrics.xml", Stat.Stats.class);


            Mockito.when(serviceProvider.getMetricWriteHelper()).thenReturn(metricWriter);

            stat = (Stat.Stats) monitorContextConfiguration.getMetricsXml();

//            dataParser = Mockito.spy(new MetricDataParser(monitorContextConfiguration));

            server.put("host","localhost");
            server.put("port","8983");
            server.put("name", "Server 1");
            server.put("collectionName", "techproducts");

            metricCollector = Mockito.spy(new MetricCollector(stat.getStats()[0], monitorContextConfiguration, server, phaser, metricWriter, metricReplacer));


            PowerMockito.mockStatic(HttpClientUtils.class);

            PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                    new Answer() {
                        public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                            ObjectMapper mapper = new ObjectMapper();
                            String url = (String) invocationOnMock.getArguments()[1];
                            String file = null;
                            if (url.contains("/mbeans")) {
                                file = "/json/mbeans.json";
                            } else if (url.contains("/system")) {
                                file = "/json/system.json";
                            }
                            logger.info("Returning the mocked data for the api " + file);

                            return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);

                        }
                    });

        }
//        @Test
//        public void testWithMbeanMetrics() throws TaskExecutionException {
//
//            expectedValueMap = new HashMap<String, String>();
//            initExpectedMBeanCACHEdocumentCacheMetrics();
//            initExpectedMBeanCACHEfieldCacheMetrics();
//            initExpectedMBeanCACHEfilterCacheMetrics();
//            initExpectedMBeanCACHEperSegFilterMetrics();
//            initExpectedMBeanCACHEqueryResultCacheMetrics();
//            initExpectedMBeanCOREandCoreMetrics();
//            initExpectedMBeanCOREsearcherMetrics();
//            initExpectedMBeanQUERYandSQLMetrics();
//            addHeartBeatMetricOne();
//            metricCollector.run();
//            validateMetricsList();
//        }

    private void validateMetricsList(){
        Map<String, Metric> mapOfMetrics = metricCollector.getMetricsMap();
        for(String prefix: mapOfMetrics.keySet()){

            String actualValue = mapOfMetrics.get(prefix).getMetricValue();
            String metricPath = mapOfMetrics.get(prefix).getMetricPath();
            System.out.println("expectedValueMap.put(\"" + metricPath + "\",\"" + actualValue + "\");");

//            if(expectedValueMap.containsKey(metricPath)){
//                String expectedValue = expectedValueMap.get(metricPath);
//                Assert.assertEquals("The value of metric " + metricPath + " failed", expectedValue, actualValue);
//                expectedValueMap.remove(metricPath);
//            }
//            else {
//                System.out.println("expectedValueMap.put(\"" + metricPath + "\",\"" + actualValue + "\")");
//                Assert.fail("Unknown Metric " + metricPath);
//            }

        }

        mapOfMetrics.remove("blue");
    }

        private void initExpectedMBeanCACHEperSegFilterMetrics() {
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Inserts","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Warmup Time","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Cumulative Inserts","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Evictions","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Hits","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Size","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Cumulative Hit Ratio","0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Cumulative Lookups","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Hit Ratio","0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Cumulative Hits","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Lookups","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|stats|Cumulative Evictions","0");
        }

        private void initExpectedMBeanCACHEqueryResultCacheMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Cumulative Lookups","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Warmup Time","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Lookups","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Cumulative Inserts","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Hit Ratio","0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Size","1");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Cumulative Evictions","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Evictions","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Hits","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Inserts","1");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Cumulative Hits","0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|stats|Cumulative Hit Ratio","0.0");

    }

        private void initExpectedMBeanCACHEdocumentCacheMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Evictions", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Size", "4");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hit Ratio", "0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Inserts", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hits", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Inserts", "4");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hits", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Lookups", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Lookups", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hit Ratio", "0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Warmup Time", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Evictions", "0");
        }

        private void initExpectedMBeanCACHEfilterCacheMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hit Ratio", "0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Inserts", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Inserts", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Size", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Warmup Time", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Lookups", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hit Ratio", "0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Lookups", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hits", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Evictions", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Evictions", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hits", "0");
        }

        private void initExpectedMBeanCACHEfieldCacheMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|fieldCache|Entries Count", "0");
        }

        private void initExpectedMBeanCOREandCoreMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Usable Space", "116875075584");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Size In Bytes", "26907");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Total Space", "499963170816");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|SEARCHER New Errors", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Ref Count", "2");
        }

        private void initExpectedMBeanCOREsearcherMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Warmup Time", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Index Version", "6");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Deleted Docs", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Number Of Docs", "28");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Max Docs", "28");
        }

        private void initExpectedMBeanQUERYandSQLMetrics(){
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Client Error Count", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Requests", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Error Count", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Request Times Mean", "0.0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Total Time", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Handlet Start", "1524505067595");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Timeout", "0");
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Server Errors", "0");
        }


        private void addHeartBeatMetricOne() {
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|HeartBeat", "1");
        }
        private void addHeartBeatMetricZero() {
            expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|HeartBeat", "0");
        }

    }
