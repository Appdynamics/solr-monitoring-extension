/*
 * Copyright 2014 AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 */

package com.appdynamics.extensions.solr.mbeans;

import com.appdynamics.extensions.solr.mbeans.QueryMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class QueryMetricsTest {
    @Test
    public void populateStatsTest() throws IOException {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/QueryHandler.json"), JsonNode.class);
        map.put("QUERYHANDLER", jsonNode);
        String queryMetricPath = "|Cores|collection|QUERYHANDLER|/select|";
        QueryMetrics queryMetricsPopulator = new QueryMetrics("collection");
        Map<String, BigDecimal> queryMetrics = queryMetricsPopulator.populateStats(map, "/select");
        Assert.assertTrue(queryMetrics.size() == 7);
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Requests Per Minute"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Requests Per Second"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Errors"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Timeouts"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "5 min Rate Requests Per Minute"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Time Per Request (milliseconds)"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Requests"));

        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Requests Per Minute").equals(new BigDecimal("0")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Requests Per Second").equals(new BigDecimal("0")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Errors").equals(new BigDecimal("0")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Timeouts").equals(new BigDecimal("0")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "5 min Rate Requests Per Minute").equals(new BigDecimal("0")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Time Per Request (milliseconds)").equals(new BigDecimal("41")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Requests").equals(new BigDecimal("1")));
    }
}
