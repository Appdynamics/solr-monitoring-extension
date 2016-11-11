package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.helpers.SolrHelper;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.Map;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SolrHelperTest {
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse response;
    private BasicHttpEntity entity;

    @Before
    public void setup() throws IOException {
        httpClient = mock(CloseableHttpClient.class);
        response = mock(CloseableHttpResponse.class);
        entity = new BasicHttpEntity();
    }

    @Test
    public void parseResponseAsJsonTest() throws IOException {
        entity.setContent(new FileInputStream("src/test/resources/MBeans.json"));
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        SolrHelper helper = new SolrHelper(httpClient);
        Map<String, JsonNode> map = helper.parseResponseAsJson(response);
        Assert.assertTrue(map.size() == 7);
        Assert.assertTrue(map.containsKey("CORE"));
        Assert.assertTrue(map.containsKey("OTHER"));
        Assert.assertTrue(map.containsKey("QUERYHANDLER"));
        Assert.assertTrue(map.containsKey("CACHE"));
        Assert.assertTrue(map.containsKey("QUERYPARSER"));
        Assert.assertTrue(map.containsKey("HIGHLIGHTING"));
        Assert.assertTrue(map.containsKey("UPDATEHANDLER"));
    }

    @Test
    public void checkIfMBeanHandlerSupportedTest() throws IOException {
        entity.setContent(new FileInputStream("src/test/resources/MBeansPlugins.json"));
        when(response.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(response);
        SolrHelper helper = new SolrHelper(httpClient);
        Assert.assertTrue(helper.checkIfMBeanHandlerSupported(response));
    }
}
