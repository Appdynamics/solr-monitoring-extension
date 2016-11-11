package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.query.QueryMetricsPopulator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QueryMetricsPopulatorTest {
    @Test
    public void populateStatsTest() throws IOException {
        Map<String, JsonNode> map = new HashMap<String, JsonNode>();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode jsonNode = mapper.readValue(new File("src/test/resources/QueryHandler.json"), JsonNode.class);
        map.put("QUERYHANDLER", jsonNode);
        String queryMetricPath = "|Cores|collection|QUERYHANDLER|/select|";
        QueryMetricsPopulator queryMetricsPopulator = new QueryMetricsPopulator("collection");
        Map<String, Long> queryMetrics = queryMetricsPopulator.populateStats(map, "/select");
        Assert.assertTrue(queryMetrics.size() == 7);
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Requests Per Minute"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Requests Per Second"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Errors"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Timeouts"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "5 min Rate Requests Per Minute"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Average Time Per Request (milliseconds)"));
        Assert.assertTrue(queryMetrics.containsKey(queryMetricPath + "Requests"));

        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Requests Per Minute").equals(new Long(0)));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Requests Per Second").equals(new Long(0)));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Errors").equals(new Long(0)));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Timeouts").equals(new Long(0)));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "5 min Rate Requests Per Minute").equals(new Long(0)));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Average Time Per Request (milliseconds)").equals(new Long("4630967054332067840")));
        Assert.assertTrue(queryMetrics.get(queryMetricPath + "Requests").equals(new Long("4607182418800017408")));
    }
}
