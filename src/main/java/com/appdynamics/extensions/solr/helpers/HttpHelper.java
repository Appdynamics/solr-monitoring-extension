/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.helpers;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class HttpHelper {

    private static final Logger logger = LoggerFactory.getLogger(HttpHelper.class);

    public static CloseableHttpResponse doGet(CloseableHttpClient httpClient, String uri) {
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

    public static void closeHttpResponse(CloseableHttpResponse response) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            logger.error("Error encountered while fetching an HTTP response");
        }
    }
}
