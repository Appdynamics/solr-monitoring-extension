package com.appdynamics.extensions.solr;


import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.mbeans.MBeansHandler;
import com.appdynamics.extensions.solr.memory.MemoryMetricsHandler;
import org.apache.http.impl.client.CloseableHttpClient;

import java.util.HashMap;
import java.util.Map;

class SolrStats {
    private CloseableHttpClient httpClient;
    private String serverUrl;
    private String contextRoot = "/solr";

    SolrStats (Map server, String contextRoot, CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
        this.contextRoot = contextRoot;
        this.serverUrl = UrlBuilder.fromYmlServerConfig(server).build();
    }

    Map<String, Long> populateStats (Core core) {
        Map<String, Long> solrMetrics = new HashMap<String, Long>();
        PingHandler pingHandler = new PingHandler(httpClient);
        if (pingHandler.isReachable(core, contextRoot, serverUrl)) {
            solrMetrics.put(pingHandler.getPingStatus(core), PingHandler.ONE);
            PluginsVerifier pluginsHandler = new PluginsVerifier(httpClient);
            if (pluginsHandler.arePluginsEnabled(core, contextRoot, serverUrl)) {
                MBeansHandler mBeansHandler = new MBeansHandler(httpClient);
                solrMetrics.putAll(mBeansHandler.populateStats(core, contextRoot, serverUrl));
            }
        } else {
            solrMetrics.put(pingHandler.getPingStatus(core), PingHandler.ZERO);
        }
        MemoryMetricsHandler memoryMetricsHandler = new MemoryMetricsHandler(httpClient);
        solrMetrics.putAll(memoryMetricsHandler.populateStats(core, contextRoot, serverUrl));
        return solrMetrics;
    }
}
