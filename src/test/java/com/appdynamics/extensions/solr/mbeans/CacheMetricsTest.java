package com.appdynamics.extensions.solr.mbeans;

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
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CacheMetricsTest {
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
        DocumentCacheMetrics documentCacheMetricsPopulator = new DocumentCacheMetrics(collection);
        Map<String, BigDecimal> metrics = documentCacheMetricsPopulator.populateStats(map);
        Assert.assertTrue(metrics.size() == 3);
        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "HitRatio %").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal("0")));
    }

    @Test
    public void populateStatsTest_FieldCacheMetrics() {
        String fieldCacheMetricPath = "|Cores|collection|CACHE|FieldValueCache|";
        FieldCacheMetrics fieldCacheMetricsPopulator = new FieldCacheMetrics(collection);
        Map<String, BigDecimal> metrics = fieldCacheMetricsPopulator.populateStats(map);
        Assert.assertTrue(metrics.size() == 3);
        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "HitRatio %").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal("0")));
    }

    @Test
    public void populateStatsTest_QueryCacheMetrics() {
        String queryCacheMetricPath = "|Cores|collection|CACHE|QueryResultCache|";
        QueryCacheMetrics queryCacheMetricsPopulator = new QueryCacheMetrics(collection);
        Map<String, BigDecimal> metrics = queryCacheMetricsPopulator.populateStats(map);
        Assert.assertTrue(metrics.size() == 3);
        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(queryCacheMetricPath + "HitRatio %").equals(new BigDecimal("75")));
        Assert.assertTrue(metrics.get(queryCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal("1")));
            Assert.assertTrue(metrics.get(queryCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal("75")));
    }

    @Test
    public void populateStatsTest_FilterCacheMetrics() {
        String filterCacheMetricPath = "|Cores|collection|CACHE|FilterCache|";
        FilterCacheMetrics filterCacheMetricsPopulator = new FilterCacheMetrics(collection);
        Map<String, BigDecimal> metrics = filterCacheMetricsPopulator.populateStats(map);
        Assert.assertTrue(metrics.size() == 3);
        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "HitRatio %").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal("0")));
    }
}
