package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.config.Core;
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

public class CoreContextStats {

    public static final Logger logger = LoggerFactory.getLogger(CoreContextStats.class);
    private static String context_root = "/solr";
    private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";

    public CoreContextStats () {
    }

    public List<Core> getCores (Map<String, ?> config, CloseableHttpClient httpClient, String uri) throws IOException {
        List<Core> cores = new ArrayList<Core>();
        if (config != null) {
            List<Map<String, ?>> coresFromCfg = (List) config.get("cores");
            if(coresFromCfg != null) {
                for (Map<String, ?> map : coresFromCfg) {
                    String name = (String) map.get("name");
                    String pingHandler = (String) map.get("pingHandler");
                    List<String> queryHandlers = (List) map.get("queryHandlers");
                    cores.add(buildCore(name, pingHandler, queryHandlers));
                }
            }
        }

        if (cores.size() == 0) {
            uri += context_root + CORE_URI;
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
                defaultCore = node.path("defaultCoreName").asText();
                if (logger.isDebugEnabled()) logger.debug("Default Core name is " + defaultCore);
            }
        } catch (Exception e) {
            logger.error("Error while fetching default Core name " + uri, e);
            throw new RuntimeException();
        } finally {
            if (response != null) {
                response.close();
            } else {
                logger.error("");
            }
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
}
