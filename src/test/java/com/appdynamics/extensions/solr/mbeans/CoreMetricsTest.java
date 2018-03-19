/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.mbeans;


import com.appdynamics.extensions.solr.mbeans.CoreMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CoreMetricsTest {

    @Test
    public void populateStatsTest() throws IOException {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/Core.json"), JsonNode.class);
        map.put("CORE", jsonNode);
        String coreMetricPath = "|Cores|collection|CORE|";
        CoreMetrics coreMetricsPopulator = new CoreMetrics("collection");
        Map<String, BigDecimal> coreMetrics = coreMetricsPopulator.populateStats(map);
        Assert.assertTrue(coreMetrics.size() == 3);
        Assert.assertTrue(coreMetrics.containsKey(coreMetricPath + "Number of Docs"));
        Assert.assertTrue(coreMetrics.containsKey(coreMetricPath + "Deleted Docs"));
        Assert.assertTrue(coreMetrics.containsKey(coreMetricPath + "Max Docs"));
        Assert.assertTrue(coreMetrics.get(coreMetricPath + "Number of Docs").equals(new BigDecimal("0")));
        Assert.assertTrue(coreMetrics.get(coreMetricPath + "Deleted Docs").equals(new BigDecimal("0")));
        Assert.assertTrue(coreMetrics.get(coreMetricPath + "Max Docs").equals(new BigDecimal("0")));
    }
}
