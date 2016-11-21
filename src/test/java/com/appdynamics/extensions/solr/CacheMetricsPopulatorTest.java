package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.mbeans.cache.DocumentCacheMetrics;
import com.appdynamics.extensions.solr.mbeans.cache.FieldCacheMetrics;
import com.appdynamics.extensions.solr.mbeans.cache.FilterCacheMetrics;
import com.appdynamics.extensions.solr.mbeans.cache.QueryCacheMetrics;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CacheMetricsPopulatorTest {
    private Map<String, JsonNode> map;
    private JsonNode jsonNode;
    private String collection;

    @Before
    public void setup() throws IOException {
        map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        jsonNode = mapper.readValue(new File("src/test/resources/Cache.json"), JsonNode.class);
        map.put("CACHE", jsonNode);
        collection = "collection";
    }

    @Test
    public void populateStatsTest_DocumentCacheMetrics () {
        String documentCacheMetricPath = "|Cores|collection|CACHE|DocumentCache|";
        DocumentCacheMetrics documentCacheMetricsPopulator = new DocumentCacheMetrics(map, collection);
        Map<String, Long> map = documentCacheMetricsPopulator.populateStats();
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(documentCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(map.containsKey(documentCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(map.containsKey(documentCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(map.get(documentCacheMetricPath + "HitRatio %").equals(new Long(0)));
        Assert.assertTrue(map.get(documentCacheMetricPath + "CacheSize (Bytes)").equals(new Long(0)));
        Assert.assertTrue(map.get(documentCacheMetricPath + "HitRatioCumulative %").equals(new Long(0)));
    }

    @Test
    public void populateStatsTest_FieldCacheMetrics() {
        String fieldCacheMetricPath = "|Cores|collection|CACHE|FieldValueCache|";
        FieldCacheMetrics fieldCacheMetricsPopulator = new FieldCacheMetrics(map, collection);
        Map<String, Long> map = fieldCacheMetricsPopulator.populate();
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(fieldCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(map.containsKey(fieldCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(map.containsKey(fieldCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(map.get(fieldCacheMetricPath + "HitRatio %").equals(new Long(0)));
        Assert.assertTrue(map.get(fieldCacheMetricPath + "CacheSize (Bytes)").equals(new Long(0)));
        Assert.assertTrue(map.get(fieldCacheMetricPath + "HitRatioCumulative %").equals(new Long(0)));
    }

    @Test
    public void populateStatsTest_QueryCacheMetrics() {
        String queryCacheMetricPath = "|Cores|collection|CACHE|QueryResultCache|";
        QueryCacheMetrics queryCacheMetricsPopulator = new QueryCacheMetrics(map, collection);
        Map<String, Long> map = queryCacheMetricsPopulator.populate();
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(queryCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(map.containsKey(queryCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(map.containsKey(queryCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(map.get(queryCacheMetricPath + "HitRatio %").equals(new Long("4634978072750194688")));
        Assert.assertTrue(map.get(queryCacheMetricPath + "CacheSize (Bytes)").equals(new Long("4607182418800017408")));
            Assert.assertTrue(map.get(queryCacheMetricPath + "HitRatioCumulative %").equals(new Long("4634978072750194688")));
    }

    @Test
    public void populateStatsTest_FilterCacheMetrics() {
        String filterCacheMetricPath = "|Cores|collection|CACHE|FilterCache|";
        FilterCacheMetrics filterCacheMetricsPopulator = new FilterCacheMetrics(map, collection);
        Map<String, Long> map = filterCacheMetricsPopulator.populate();
        Assert.assertTrue(map.size() == 3);
        Assert.assertTrue(map.containsKey(filterCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(map.containsKey(filterCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(map.containsKey(filterCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(map.get(filterCacheMetricPath + "HitRatio %").equals(new Long(0)));
        Assert.assertTrue(map.get(filterCacheMetricPath + "CacheSize (Bytes)").equals(new Long(0)));
        Assert.assertTrue(map.get(filterCacheMetricPath + "HitRatioCumulative %").equals(new Long(0)));
    }
}
