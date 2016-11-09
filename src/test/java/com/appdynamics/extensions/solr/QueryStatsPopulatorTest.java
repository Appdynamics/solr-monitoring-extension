package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.Query.QueryStatsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryStatsPopulatorTest {
    @Test
    public void populateStatsTest() throws IOException {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/QueryHandlerJSON"), JsonNode.class);
        map.put("QUERYHANDLER", jsonNode);
        QueryStatsPopulator queryStatsPopulator = new QueryStatsPopulator("collection");
        Assert.assertTrue(queryStatsPopulator.populateStats(map, "/select").size() == 7);
    }
}
