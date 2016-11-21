package com.appdynamics.extensions.solr;


import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.helpers.HttpHelper;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PluginsVerifier {

    CloseableHttpClient httpClient;
    private static String PLUGINS_PATH = "/%s/admin/plugins?wt=json";
    private static final Logger logger = LoggerFactory.getLogger(PluginsVerifier.class);

    PluginsVerifier(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    boolean arePluginsEnabled(Core core, String contextRoot,String serverUrl) {
        String url = buildUrl(core,contextRoot,serverUrl);
        CloseableHttpResponse response = null;
        boolean mbeanSupport = false;
        try {
            logger.debug("checking if plugins are enabled from {}",url);
            response = HttpHelper.doGet(httpClient,url);
            JsonNode jsonNode = SolrUtils.getJsonNode(response);
            if (jsonNode != null) {
                JsonNode node = jsonNode.findValue("QUERYHANDLER");
                if (node == null) {
                    logger.error("Missing 'QUERYHANDLER' while parsing response");
                    return false;
                }
                mbeanSupport = node.has("/admin/mbeans");
                if (!mbeanSupport) {
                    logger.error("Stats are collected through an HTTP Request to SolrInfoMBeanHandler but " +
                            "SolrInfoMbeanHandler (/admin/mbeans) or /admin request handler is disabled in solrconfig.xml");
                }
            } else {
                logger.error("Unable to get a proper response while checking plugins availability.");
                return false;
            }
        } catch (Exception e) {
            logger.error("Could not connect to core to check plugins availability.", e.getMessage());
        } finally {
            HttpHelper.closeHttpResponse(response);
        }
        return mbeanSupport;
    }

    private String buildUrl(Core core, String contextRoot, String serverUrl){
        StringBuilder url = new StringBuilder(serverUrl);
        url.append(contextRoot).append(String.format(PLUGINS_PATH, core.getName()));
        return url.toString();
    }
}
