
package com.appdynamics.extensions.solr.core;

import com.appdynamics.extensions.yml.YmlReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CoreContextTest {
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private BasicHttpEntity entity;
    private CoreContext coreContextStats;

    @Before
    public void setup () throws IOException {
        httpClient = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class);
        Map<String, String> server = new HashMap<String, String>();
        server.put("uri", "xyz.com");
        entity = new BasicHttpEntity();
        entity.setContent(new FileInputStream("src/test/resources/DefaultCore.json"));
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        coreContextStats = new CoreContext(httpClient, server);
    }

    @Test
    public void getCores_whenNoCoresPresent () throws IOException {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_with_no_cores.yml"));
        List<Core> cores = coreContextStats.getCores(config);
        Assert.assertTrue(cores.get(0).getName().equals("DefaultCore"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().size() == 0);
        Assert.assertTrue(cores.get(0).getPingHandler() == null);
    }

    @Test
    public void getCores_whenCoreIsPresent () throws IOException {
        Map<String, ?> config = YmlReader.readFromFile(new File("src/test/resources/conf/config_with_core.yml"));
        List<Core> cores = coreContextStats.getCores(config);
        Assert.assertTrue(cores.get(0).getName().equals("gettingstarted"));
        Assert.assertTrue(cores.get(0).getPingHandler().equals("/admin/ping"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().get(0).equals("/select"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().get(1).equals("/update"));
    }

    @Test
    public void getCores_whenEmptyCoresElementIsPresent () throws IOException {
        Map<String, ?> config = YmlReader.readFromFile(new File
                ("src/test/resources/conf/config_with_empty_cores_element.yml"));
        List<Core> cores = coreContextStats.getCores(config);
        Assert.assertTrue(cores.get(0).getName().equals("DefaultCore"));
        Assert.assertTrue(cores.get(0).getQueryHandlers().size() == 0);
        Assert.assertTrue(cores.get(0).getPingHandler() == null);
    }
}

