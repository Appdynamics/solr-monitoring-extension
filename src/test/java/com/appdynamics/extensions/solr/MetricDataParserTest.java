package com.appdynamics.extensions.solr;

/**
 * Created by bhuvnesh.kumar on 5/2/18.
 */
public class MetricDataParserTest {

//    private List<Map<String, String>> metricReplacer = new ArrayList<Map<String, String>>();
//
//    private MonitorContextConfiguration monitorContextConfiguration = new MonitorContextConfiguration("SolrMonitor",
//            "Custom Metrics|Solr|",Mockito.mock(File.class), Mockito.mock(AMonitorJob.class));
//
//    @Test
//    public void parseNodeDataTestSystemMetrics() throws Exception{
//        monitorContextConfiguration.setConfigYml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
//        monitorContextConfiguration.setMetricXml("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/xml/SystemMetrics.xml", Stat.Stats.class);
//        Stat.Stats metricConfiguration = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
//        ObjectMapper mapper = new ObjectMapper();
//        JsonNode node = mapper.readValue(new FileInputStream("/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/json/system.json"), JsonNode.class);
//        Stat stat = metricConfiguration.getStats()[0];
//        Map<String, String> expectedSystemMetrics = initExpectedSystemAndSystemMetrics();
//        MetricDataParser metricDataParser = new MetricDataParser(monitorContextConfiguration);
//        String serverName = "Server 1";
//        Map<String, Metric> metricMap = metricDataParser.parseNodeData(stat,node,mapper,serverName, metricReplacer);
//
//    }


//
//    private Map<String, String> initExpectedMBeanCACHEperSegFilterMetrics() {
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Evictions", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Size", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Warmup Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|perSegFilter|Cumulative Evictions", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCACHEqueryResultCacheMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Evictions", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Size", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Warmup Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|queryResultCache|Cumulative Evictions", "0");
//
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCACHEdocumentCacheMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Evictions", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Size", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Warmup Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|documentCache|Cumulative Evictions", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCACHEfilterCacheMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Evictions", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Size", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Warmup Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Lookups", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Inserts", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hits", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Hit Ratio", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|filterCache|Cumulative Evictions", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCACHEfieldCacheMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CACHE|fieldCache|Entries Count", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCOREandCoreMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Total Space", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Size In Bytes", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|SEARCHER New Errors", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Ref Count", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|core|Usable Space", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanCOREsearcherMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Max Docs", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Deleted Docs", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Warmup Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Index Version", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|CORE|searcher|Number Of Docs", "0");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedMBeanQUERYandSQLMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Total Time", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Client Error Count", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Timeout", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Server Errors", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Request Times Mean", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Handlet Start", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Error Count", "0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|QUERY|/sql|Requests", "0");
//        return expectedValueMap;
//    }
//
//
//
//    private Map<String, String> initExpectedSystemAndSystemMetrics(){
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Available Processors", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|System Load Average", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Committed Virtual Memory Size", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Physical Memory Size", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Free Swap Space Size", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process Cpu Time", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Physical Memory Size", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Total Swap Space Size", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Max File Descriptor Count", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Open File Descriptor Count", "6.576627712E9");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|System|Process CPU Load", "6.576627712E9");
//        return expectedValueMap;
//    }
//
//    private Map<String, String> initExpectedSystemandMemoryMetrics() {
//
//        Map<String, String> expectedValueMap = new HashMap<String, String>();
//
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Free MB", "411.0");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Total MB", "490.7");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Max MB", "490.7");
//        expectedValueMap.put("Server|Component:awsReportingTier|Custom Metrics|Solr Monitor|Server 1|Memory|Used MB", "79.6");
//
//        return expectedValueMap;
//    }

}
