package com.appdynamics.extensions.solr.utils;

import com.appdynamics.extensions.http.HttpClientUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;

/**
 * Created by bhuvnesh.kumar on 10/11/18.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientUtils.class)
@PowerMockIgnore("javax.net.ssl.*")

public class MetricUtilsTest {
    private CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
    private Map server = new HashMap();

    @Before
    public void before() {
        server.put("name", "Server 1");
        server.put("host", "localhost");
        server.put("port", 9999);
        server.put("applicationName", "solr");

        List<String> collections = new ArrayList<String>();
        collections.add("techproducts");
        server.put("collectionName", collections);

    }

    @Test
    public void isVersion7orMoreTestTrue() throws Exception {
        PowerMockito.mockStatic(HttpClientUtils.class);
        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String file = "/json/system.json";
                        return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);
                    }
                });
        boolean value = MetricUtils.isVersion7OrHigher(server, httpClient);
        Assert.assertTrue(value);
    }

    @Test
    public void isVersion7orMoreTestFalse() throws Exception {
        PowerMockito.mockStatic(HttpClientUtils.class);
        PowerMockito.when(HttpClientUtils.getResponseAsJson(any(CloseableHttpClient.class), anyString(), any(Class.class))).thenAnswer(
                new Answer() {
                    public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                        ObjectMapper mapper = new ObjectMapper();
                        String file = "/json/systemV5.5.json";
                        return mapper.readValue(getClass().getResourceAsStream(file), JsonNode.class);
                    }
                });
        CloseableHttpClient httpClient = Mockito.mock(CloseableHttpClient.class);
        Map server = new HashMap();
        server.put("name", "Server 1");
        server.put("host", "localhost");
        server.put("port", 9999);
        server.put("applicationName", "solr");
        List<String> collections = new ArrayList<String>();
        collections.add("techproducts");
        server.put("collectionName", collections);
        boolean value = MetricUtils.isVersion7OrHigher(server, httpClient);
        Assert.assertFalse(value);
    }
}