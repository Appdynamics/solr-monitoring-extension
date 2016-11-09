package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.Cache.DocumentCacheMetricsPopulator;
import com.appdynamics.extensions.solr.Cache.FieldCacheMetricsPopulator;
import com.appdynamics.extensions.solr.Cache.FilterCacheMetricsPopulator;
import com.appdynamics.extensions.solr.Cache.QueryCacheMetricsPopulator;
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
        jsonNode = mapper.readValue(new File("src/test/resources/CacheJSON"), JsonNode.class);
        map.put("CACHE", jsonNode);
        collection = "collection";
    }

    @Test
    public void populateStatsTest_DocumentCacheMetrics () {
        DocumentCacheMetricsPopulator documentCacheMetricsPopulator = new DocumentCacheMetricsPopulator(map, collection);
        Assert.assertTrue(documentCacheMetricsPopulator.populate().size() == 3);
    }

    @Test
    public void populateStatsTest_FieldCacheMetrics() {
        FieldCacheMetricsPopulator fieldCacheMetricsPopulator = new FieldCacheMetricsPopulator(map, collection);
        Assert.assertTrue(fieldCacheMetricsPopulator.populate().size() == 3);
    }

    @Test
    public void populateStatsTest_QueryCacheMetrics() {
        QueryCacheMetricsPopulator queryCacheMetricsPopulator = new QueryCacheMetricsPopulator(map, collection);
        Assert.assertTrue(queryCacheMetricsPopulator.populate().size() == 3);
    }

    @Test
    public void populateStatsTest_FilterCacheMetrics() {
        FilterCacheMetricsPopulator filterCacheMetricsPopulator = new FilterCacheMetricsPopulator(map, collection);
        Assert.assertTrue(filterCacheMetricsPopulator.populate().size() == 3);
    }
}
