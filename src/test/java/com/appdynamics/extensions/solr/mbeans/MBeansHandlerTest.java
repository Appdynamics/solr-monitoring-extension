package com.appdynamics.extensions.solr.mbeans;

import com.appdynamics.extensions.solr.core.Core;
import com.appdynamics.extensions.solr.mbeans.MBeansHandler;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MBeansHandlerTest {
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private BasicHttpEntity entity;

    @Before
    public void setup() throws IOException {
        httpClient = mock(CloseableHttpClient.class);
        response = mock(CloseableHttpResponse.class);
        entity = new BasicHttpEntity();
        entity.setContent(new FileInputStream("src/test/resources/MBeans.json"));
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
    }

    @Test
    public void populateStatsTest() throws IOException {
        Core core = createTestCore();
        MBeansHandler mBeansHandler = new MBeansHandler(httpClient);

        Map<String, BigDecimal> metrics = mBeansHandler.populateStats(core, "contextroot", "serverurl");
        String coreMetricPath = "|Cores|" + core.getName() + "|CORE|";
        String queryCacheMetricPath = "|Cores|" + core.getName() + "|CACHE|QueryResultCache|";
        String documentCacheMetricPath = "|Cores|" + core.getName() + "|CACHE|DocumentCache|";
        String filterCacheMetricPath = "|Cores|" + core.getName() + "|CACHE|FilterCache|";
        String fieldCacheMetricPath = "|Cores|" + core.getName() + "|CACHE|FieldValueCache|";
        String queryMetricPath = "|Cores|" + core.getName() + "|QUERYHANDLER|" + core.getQueryHandlers().get(0) + "|";

        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(fieldCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "HitRatio %").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(fieldCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal(0)));

        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(queryCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(queryCacheMetricPath + "HitRatio %").equals(new BigDecimal("94")));
        Assert.assertTrue(metrics.get(queryCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal("1")));
        Assert.assertTrue(metrics.get(queryCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal("94")));

        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(filterCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "HitRatio %").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(filterCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal(0)));

        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "HitRatio %"));
        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "CacheSize (Bytes)"));
        Assert.assertTrue(metrics.containsKey(documentCacheMetricPath + "HitRatioCumulative %"));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "HitRatio %").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "CacheSize (Bytes)").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(documentCacheMetricPath + "HitRatioCumulative %").equals(new BigDecimal(0)));

        Assert.assertTrue(metrics.containsKey(coreMetricPath + "Number of Docs"));
        Assert.assertTrue(metrics.containsKey(coreMetricPath + "Deleted Docs"));
        Assert.assertTrue(metrics.containsKey(coreMetricPath + "Max Docs"));
        Assert.assertTrue(metrics.get(coreMetricPath + "Number of Docs").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(coreMetricPath + "Deleted Docs").equals(new BigDecimal("0")));
        Assert.assertTrue(metrics.get(coreMetricPath + "Max Docs").equals(new BigDecimal("0")));

        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Average Requests Per Minute"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Average Requests Per Second"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Errors"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Timeouts"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "5 min Rate Requests Per Minute"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Average Time Per Request (milliseconds)"));
        Assert.assertTrue(metrics.containsKey(queryMetricPath + "Requests"));

        Assert.assertTrue(metrics.get(queryMetricPath + "Average Requests Per Minute").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(queryMetricPath + "Average Requests Per Second").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(queryMetricPath + "Errors").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(queryMetricPath + "Timeouts").equals(new BigDecimal(0)));
        Assert.assertTrue(metrics.get(queryMetricPath + "5 min Rate Requests Per Minute").equals(new BigDecimal("3")));
        Assert.assertTrue(metrics.get(queryMetricPath + "Average Time Per Request (milliseconds)").equals(new BigDecimal("2")));
        Assert.assertTrue(metrics.get(queryMetricPath + "Requests").equals(new BigDecimal("33")));
    }

    private Core createTestCore() {
        Core core = new Core();
        core.setName("TestCore");
        List<String> queryHandlers = new ArrayList<String>();
        queryHandlers.add("/select");
        core.setQueryHandlers(queryHandlers);
        return core;
    }
}
