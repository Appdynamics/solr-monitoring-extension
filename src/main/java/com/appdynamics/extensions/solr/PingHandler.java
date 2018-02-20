/*
 * Copyright 2014 AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.helpers.HttpHelper;
import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

class PingHandler {

    private CloseableHttpClient httpClient;
    static final BigDecimal ONE = BigDecimal.ONE;
    static final BigDecimal ZERO = BigDecimal.ZERO;

    private static final String PING_STATUS = "Ping Status";
    private static final Logger logger = LoggerFactory.getLogger(PingHandler.class);

    PingHandler(CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    boolean isReachable(Core core, String contextRoot, String serverUrl) {
        if (Strings.isNullOrEmpty(core.getPingHandler())) {
            return false;
        }
        String url = buildUrl(core, contextRoot, serverUrl);
        logger.debug("get ping handler status from {}", url);
        CloseableHttpResponse response = null;
        try {
            response = HttpHelper.doGet(httpClient, url);
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                JsonNode pingResponseNode = SolrUtils.getJsonNode(response);
                if (pingResponseNode != null && "OK".equals(pingResponseNode.path("status").asText())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error("Could not connect to core to fetch ping handler status", e.getMessage());
        } finally {
            HttpHelper.closeHttpResponse(response);
        }
        return false;
    }

    private String buildUrl(Core core, String contextRoot, String serverUrl) {
        StringBuilder url = new StringBuilder(serverUrl);
        url.append(contextRoot).append('/').append(core.getName()).append(core.getPingHandler()).append("?wt=json");
        return url.toString();
    }

    String getPingStatus(Core core) {
        return "|" + "Cores" + "|" + core.getName() + "|" + PING_STATUS;
    }
}
