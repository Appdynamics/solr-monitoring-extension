package com.appdynamics.extensions.solr;


import com.appdynamics.extensions.solr.Core.CoreStatsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CoreStatsPopulatorTest {

    @Test
    public void populateStatsTest() throws IOException {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/CoreJSON"), JsonNode.class);
        map.put("CORE", jsonNode);
        CoreStatsPopulator coreStatsPopulator = new CoreStatsPopulator("collection");
        Assert.assertTrue(coreStatsPopulator.populateStats(map).size() == 3);
    }
}
