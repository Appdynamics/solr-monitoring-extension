package com.appdynamics.extensions.solr;

public class SolrStats {

    /*private static final Logger logger = LoggerFactory.getLogger(SolrStats.class);
    private static final String METRIC_SEPARATOR = "|";
    private static String mbeansUri = "/%s/admin/mbeans?stats=true&wt=json";
    private static String context_root = "/solr";
    private static String plugins_uri = "/%s/admin/plugins?wt=json";
    private static String memory_uri = "/%s/admin/system?stats=true&wt=json";
    private MonitorConfiguration configuration;
    private String serverName;
    private String uri;

    public SolrStats (MonitorConfiguration configuration, String serverName, String uri) {
        this.configuration = configuration;
        this.serverName = serverName;
        this.uri = uri;
    }

    public Map<String, Long> populateStats (Core coreConfig) throws IOException {
        Map<String, Long> solrMetrics = new HashMap<String, Long>();
        Map<String, JsonNode> solrMBeansHandlersMap = new HashMap<String, JsonNode>();
        String core = coreConfig.getName();
        CloseableHttpClient httpClient = configuration.getHttpClient();
        SolrHelper helper = new SolrHelper(httpClient);
        PingHandlerHelper pingHandlerHelper = new PingHandlerHelper();

        if (!pingHandlerHelper.isPingHandler(coreConfig, httpClient, uri)) {
            solrMetrics.put(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR + " " +
                    "Ping Status", new Long(0));
        } else {
            solrMetrics.put(METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + core + METRIC_SEPARATOR + " " +
                    "Ping Status", new Long(1));
            CloseableHttpResponse httpResponse = helper.getHttpResponse(uri + String.format(context_root + plugins_uri, core));
            if (helper.checkIfMBeanHandlerSupported(httpResponse)) {
                try {
                    httpResponse = helper.getHttpResponse(uri + String.format(context_root + mbeansUri, core));
                    solrMBeansHandlersMap = helper.parseResponseAsJson(httpResponse);
                } catch (Exception e) {
                    logger.error("Error in retrieving mbeans info for " + core);
                }
                finally {
                    if(httpResponse != null) {
                        httpResponse.close();
                    }
                    else {
                        logger.error("Error while closing input stream");
                    }
                }

                try {
                    CoreMetrics coreMetricsPopulator = new CoreMetrics(core);
                    solrMetrics.putAll(coreMetricsPopulator.populateStats(solrMBeansHandlersMap));
                } catch (Exception e) {
                    logger.error("Error Retrieving core Stats for " + core, e);
                }

                try {
                    for (String handler : coreConfig.getQueryHandlers()) {
                        QueryMetrics queryStatsPopulator = new QueryMetrics(core);
                        solrMetrics.putAll(queryStatsPopulator.populateStats(solrMBeansHandlersMap,
                                handler));
                    }
                } catch (Exception e) {
                    logger.error("Error Retrieving query Stats for " + core, e);
                }

                try {
                    CacheMetrics cacheMetricsHandler = new CacheMetrics(solrMBeansHandlersMap, core);
                    solrMetrics.putAll(cacheMetricsHandler.populate());
                } catch (Exception e) {
                    logger.error("Error Retrieving cache Stats for " + core, e);
                }
            }

            CloseableHttpResponse response = null;
            try {
                uri += "/" + context_root + String.format(memory_uri, core);
                HttpGet get = new HttpGet(uri);
                response = httpClient.execute(get);
                MemoryMetricsHandler memoryMetricsHandler = new MemoryMetricsHandler(response, core);
                solrMetrics.putAll(memoryMetricsHandler.populate());
            } catch (Exception e) {
                logger.error("Error retrieving memory stats for " + core, e);
            } finally {
                if (response != null) {
                    response.close();
                }
            }
        }
        return solrMetrics;
    }

    public void printMetrics (Map<String, Long> solrMetrics) {
        MetricWriteHelper metricWriter = configuration.getMetricWriter();
        String metricPrefix = configuration.getMetricPrefix();
        String aggregation = MetricWriter.METRIC_AGGREGATION_TYPE_AVERAGE;
        String cluster = MetricWriter.METRIC_CLUSTER_ROLLUP_TYPE_INDIVIDUAL;
        String timeRollup = MetricWriter.METRIC_TIME_ROLLUP_TYPE_AVERAGE;

        for (Map.Entry<String, Long> entry : solrMetrics.entrySet()) {
            String metricPath = metricPrefix + METRIC_SEPARATOR + serverName + entry.getKey();
            String metricValue = String.valueOf(entry.getValue());
            metricWriter.printMetric(metricPath, metricValue, aggregation, timeRollup, cluster);
        }
    }*/
}
