/*
 * Copyright 2014 AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.solr.core;

import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.solr.helpers.HttpHelper;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CoreContext {
    private CloseableHttpClient httpClient;
    private String contextRoot;
    private Map server;
    private static final String SOLR_CONTEXT_ROOT = "/solr";
    private static final Logger logger = LoggerFactory.getLogger(CoreContext.class);
    private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";

    public CoreContext (CloseableHttpClient httpClient, Map server) {
        this.httpClient = httpClient;
        this.server = server;
        setContextRoot(server);
    }

    public String getContextRoot () {
        return contextRoot;
    }

    private void setContextRoot (Map server) {
        this.contextRoot = (server.get("contextRoot") == null || server.get("contextRoot").toString().equals("")) ? SOLR_CONTEXT_ROOT : server.get("contextRoot")
                .toString();
    }

    public List<Core> getCores (Map<String, ?> config) throws IOException {
        List<Core> cores = new ArrayList<Core>();
        if (config != null) {
            if (config.get("cores") != null) {
                List<Map<String, ?>> coresFromCfg = (List) config.get("cores");
                for (Map<String, ?> map : coresFromCfg) {
                    String name = (String) map.get("name");
                    String pingHandler = (String) map.get("pingHandler");
                    List<String> queryHandlers = (List) map.get("queryHandlers");
                    cores.add(buildCore(name, pingHandler, queryHandlers));
                }
            }
        }

        if (cores.size() == 0) {
            String uri = generateURIFromConfig() + contextRoot + CORE_URI;
            String defaultCore = getDefaultCore(uri, httpClient);
            logger.info("Cores not configured in config.yml, default core " + defaultCore + " to be used for " +
                    "stats");
            cores.add(buildCore(defaultCore, null, new ArrayList<String>()));
        }
        return cores;
    }

    private String getDefaultCore (String uri, CloseableHttpClient httpClient) throws IOException {
        String defaultCore = "";
        CloseableHttpResponse response = null;
        try {
            HttpGet get = new HttpGet(uri);
            response = httpClient.execute(get);
            JsonNode node = SolrUtils.getJsonNode(response);
            if (node != null) {
                defaultCore = node.path("defaultCoreName").path("name").asText();
                if (logger.isDebugEnabled()) logger.debug("Default core name is " + defaultCore);
            }
        } catch (Exception e) {
            logger.error("Error while fetching default core name " + uri, e);
            throw new RuntimeException();
        } finally {
            HttpHelper.closeHttpResponse(response);
        }
        return defaultCore;
    }

    private Core buildCore (String name, String pingHandler, List<String> queryHandlers) {
        Core core = new Core();
        core.setName(name);
        core.setQueryHandlers(queryHandlers);
        core.setPingHandler(pingHandler);
        return core;
    }

    private String generateURIFromConfig () {
        return UrlBuilder.fromYmlServerConfig(server).build();
    }
}
