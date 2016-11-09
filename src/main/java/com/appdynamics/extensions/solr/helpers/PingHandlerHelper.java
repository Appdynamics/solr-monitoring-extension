package com.appdynamics.extensions.solr.helpers;

import com.appdynamics.extensions.solr.config.Core;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Strings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by adityajagtiani on 11/1/16.
 */
public class PingHandlerHelper {
    public static final Logger logger = LoggerFactory.getLogger(PingHandlerHelper.class);

    public boolean isPingHandler (Core core, CloseableHttpClient httpClient, String uri) throws IOException {
        CloseableHttpResponse response = null;
        String pingHandler = core.getPingHandler();
        if (!Strings.isNullOrEmpty(pingHandler)) {
            uri +=  "/solr/" + core.getName() + pingHandler + "?wt=json";
            try {
                HttpGet get = new HttpGet(uri);
                response = httpClient.execute(get);
                if (response.getStatusLine().getStatusCode() == 200) {
                    JsonNode pingResponseNode = SolrUtils.getJsonNode(response);
                    if (pingResponseNode != null && "OK".equals(pingResponseNode.path("status").asText())) {
                        return true;
                    }
                }
            } catch (Exception e) {
                logger.error("Could not connect to Core", e.getMessage());
            } finally {
                if(response != null) {
                    response.close();
                }
                else {
                    logger.error("Error encountered while fetching an HTTP response");
                }
            }
        }
        return false;
    }
}
