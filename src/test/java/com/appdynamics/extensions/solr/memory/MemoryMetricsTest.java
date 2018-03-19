
/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.memory;

import com.appdynamics.extensions.solr.memory.JVMMemoryMetrics;
import com.appdynamics.extensions.solr.memory.SystemMemoryMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/8/16.
 */

public class MemoryMetricsTest {
    private JsonNode jsonNode;
    private String collection;

    @Before
    public void setup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readValue(new File("src/test/resources/MemoryMetrics.json"), JsonNode.class);
        collection = "Collection";
    }

    @Test
    public void populateStatsTest_JVMMemoryMetrics() throws IOException {
        JVMMemoryMetrics jvmMemoryMetricsPopulator = new JVMMemoryMetrics(collection);
        String jvmMemoryMetricPath = "|Cores|Collection|MEMORY|JVM|";
        Map<String, BigDecimal> map = jvmMemoryMetricsPopulator.populateStats(jsonNode);
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Total (MB)"));
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Used (MB)"));
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Free (MB)"));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Total (MB)").equals(new BigDecimal("491")));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Used (MB)").equals(new BigDecimal("27")));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Free (MB)").equals(new BigDecimal("464")));
    }

    @Test
    public void populateStatsTest_SystemMemoryMetrics() throws IOException {
        SystemMemoryMetrics systemMemoryMetricsPopulator = new SystemMemoryMetrics(collection);
        String systemMemoryMetricPath = "|Cores|Collection|MEMORY|System|";
        Map<String, BigDecimal> map = systemMemoryMetricsPopulator.populateStats(jsonNode);
        Assert.assertTrue(map.size() == 7);
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Committed Virtual memory (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Total Physical memory (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Total Swap Size (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Max File Descriptor Count"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Free Swap Size (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Free Physical memory(MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Open File Descriptor Count"));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Committed Virtual memory (MB)").equals(new BigDecimal("4449")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Total Physical memory (MB)").equals(new BigDecimal("16384")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Total Swap Size (MB)").equals(new BigDecimal("3072")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Max File Descriptor Count").equals(new BigDecimal("10240")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Free Swap Size (MB)").equals(new BigDecimal("1004")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Free Physical memory(MB)").equals(new BigDecimal("182")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Open File Descriptor Count").equals(new BigDecimal("198")));
    }
}
