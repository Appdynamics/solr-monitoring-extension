/**
 * Copyright 2013 AppDynamics, Inc.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.solr.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SolrHelper {
    private static final Logger logger = LoggerFactory.getLogger(SolrHelper.class);
    private CloseableHttpClient httpClient;

    public SolrHelper (CloseableHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public CloseableHttpResponse getHttpResponse (String uri) {
        CloseableHttpResponse response;
        try {
            HttpGet get = new HttpGet(uri);
            response = httpClient.execute(get);

        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
        return response;
    }

    public Map<String, JsonNode> parseResponseAsJson (CloseableHttpResponse httpResponse) throws IOException {
        Map<String, JsonNode> solrStatsMap = new HashMap<String, JsonNode>();
        JsonNode jsonNode = SolrUtils.getJsonNode(httpResponse);
        if (jsonNode != null) {
            JsonNode solrMBeansNode = jsonNode.path("solr-mbeans");
            if (solrMBeansNode.isMissingNode()) {
                throw new IllegalArgumentException("Missing node while parsing solr-mbeans node json string for core");
            }
            for (int i = 1; i <= solrMBeansNode.size(); i += 2) {
                solrStatsMap.put(solrMBeansNode.get(i - 1).asText(), solrMBeansNode.get(i));
            }
        }
        return solrStatsMap;
    }

    public boolean checkIfMBeanHandlerSupported (CloseableHttpResponse httpResponse) throws IOException {
        JsonNode jsonNode = SolrUtils.getJsonNode(httpResponse);
        if (jsonNode != null) {
            JsonNode node = jsonNode.findValue("QUERYHANDLER");
            if (node == null) {
                logger.error("Missing 'QUERYHANDLER' while parsing response");
                return false;
            }
            boolean mbeanSupport = node.has("/admin/mbeans");
            if (!mbeanSupport) {
                logger.error("Stats are collected through an HTTP Request to SolrInfoMBeanHandler");
                logger.error("SolrInfoMbeanHandler (/admin/mbeans) or /admin request handler is disabled in " +
                        "solrconfig.xml");
            }
            return mbeanSupport;
        } else {
            logger.error("Response null when accessing core");
            return false;
        }
    }
}



