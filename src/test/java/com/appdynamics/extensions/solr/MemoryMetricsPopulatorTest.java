package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.memory.JVMMemoryMetricsPopulator;
import com.appdynamics.extensions.solr.memory.SystemMemoryMetricsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by adityajagtiani on 11/8/16.
 */
public class MemoryMetricsPopulatorTest {
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
        JVMMemoryMetricsPopulator jvmMemoryMetricsPopulator = new JVMMemoryMetricsPopulator(jsonNode, collection);
        String jvmMemoryMetricPath = "|Cores|Collection|MEMORY|JVM|";
        Map<String, Long> map = jvmMemoryMetricsPopulator.populate();
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Total (MB)"));
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Used (MB)"));
        Assert.assertTrue(map.containsKey(jvmMemoryMetricPath + "Free (MB)"));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Total (MB)").equals(new Long("4647345379539419136")));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Used (MB)").equals(new Long("4628293042053316608")));
        Assert.assertTrue(map.get(jvmMemoryMetricPath + "Free (MB)").equals(new Long("4646870390516219904")));
    }

    @Test
    public void populateStatsTest_SystemMemoryMetrics() throws IOException {
        SystemMemoryMetricsPopulator systemMemoryMetricsPopulator = new SystemMemoryMetricsPopulator(jsonNode, collection);
        String systemMemoryMetricPath = "|Cores|Collection|MEMORY|System|";
        Map<String, Long> map = systemMemoryMetricsPopulator.populate();
        Assert.assertTrue(map.size() == 7);
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Committed Virtual memory (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Total Physical memory (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Total Swap Size (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Max File Descriptor Count"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Free Swap Size (MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Free Physical memory(MB)"));
        Assert.assertTrue(map.containsKey(systemMemoryMetricPath + "Open File Descriptor Count"));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Committed Virtual memory (MB)").equals(new Long("4751685433832767488")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Total Physical memory (MB)").equals(new Long("4760304806130614272")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Total Swap Size (MB)").equals(new Long("4749045807062188032")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Max File Descriptor Count").equals(new Long("4666855113862676480")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Free Swap Size (MB)").equals(new Long("4742110087714177024")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Free Physical memory(MB)").equals(new Long("4730671730811469824")));
        Assert.assertTrue(map.get(systemMemoryMetricPath + "Open File Descriptor Count").equals(new Long("4641170522237829120")));
    }
}
