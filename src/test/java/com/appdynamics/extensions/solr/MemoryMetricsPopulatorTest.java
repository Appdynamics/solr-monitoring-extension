package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.Memory.JVMMemoryMetricsPopulator;
import com.appdynamics.extensions.solr.Memory.SystemMemoryMetricsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * Created by adityajagtiani on 11/8/16.
 */
public class MemoryMetricsPopulatorTest {
    private JsonNode jsonNode;
    private String collection;

    @Before
    public void setup() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readValue(new File("src/test/resources/MemoryMetricsJSON"), JsonNode.class);
        collection = "Collection";
    }

    @Test
    public void populateStatsTest_JVMMemoryMetrics() throws IOException {
        JVMMemoryMetricsPopulator jvmMemoryMetricsPopulator = new JVMMemoryMetricsPopulator(jsonNode, collection);
        Assert.assertTrue(jvmMemoryMetricsPopulator.populate().size() == 3);
    }

    @Test
    public void populateStatsTest_SystemMemoryMetrics() throws IOException {
        SystemMemoryMetricsPopulator systemMemoryMetricsPopulator = new SystemMemoryMetricsPopulator(jsonNode, collection);
        Assert.assertTrue(systemMemoryMetricsPopulator.populate().size() == 7);
    }
}
