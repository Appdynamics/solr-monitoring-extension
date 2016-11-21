package com.appdynamics.extensions.solr.core;

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


//TODO refactor this class like other classes.
public class CoreContext {

    CloseableHttpClient httpClient;
    String contextRoot;

    private static final String SOLR_CONTEXT_ROOT = "/solr/";
    private static final Logger logger = LoggerFactory.getLogger(CoreContext.class);
    private static final String CORE_URI = "/admin/cores?action=STATUS&wt=json";

    public CoreContext(CloseableHttpClient httpClient,Map server){
        this.httpClient = httpClient;
        setContextRoot(server);
    }

    public String getContextRoot() {
        return contextRoot;
    }

    void setContextRoot(Map server) {
        this.contextRoot = (server.get("contextRoot") == null) ? SOLR_CONTEXT_ROOT : server.get("contextRoot").toString();
    }

    public List<Core> getCores (Map<String, ?> config, String uri) throws IOException {
        List<Core> cores = new ArrayList<Core>();
        if (config != null) {
            /*
             TODO this will throw a Null pointer Exception if no cores are present. If there are no cores, having
             empty cores element in config.yaml should be avoided.  Please add a Test case for no cores element as well.
            */

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
            uri += contextRoot + CORE_URI;
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
                //TODO this is not fetching defaultCoreName even if there is one. Please check the test case.
                defaultCore = node.path("defaultCoreName").asText();
                if (logger.isDebugEnabled()) logger.debug("Default core name is " + defaultCore);
            }
        } catch (Exception e) {
            logger.error("Error while fetching default core name " + uri, e);
            throw new RuntimeException();
        } finally {
            if (response != null) {
                response.close();
            } else {
                //#TODO why is this left empty??
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
