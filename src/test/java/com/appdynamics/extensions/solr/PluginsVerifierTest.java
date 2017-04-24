package com.appdynamics.extensions.solr;

import com.appdynamics.extensions.solr.core.Core;
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PluginsVerifierTest {
    private CloseableHttpClient httpClient;
    private CloseableHttpResponse httpResponse;
    private BasicHttpEntity entity;
    private PluginsVerifier pluginsVerifier;
    private Core core;

    @Before
    public void setup () throws IOException {
        core = new Core();
        core.setName("TestCore");
        httpClient = mock(CloseableHttpClient.class);
        httpResponse = mock(CloseableHttpResponse.class);
        entity = new BasicHttpEntity();
    }

    @Test
    public void arePluginsEnabled_whenNodeIsFound_newerSolrVersions () throws IOException {
        entity.setContent(new FileInputStream("src/test/resources/MBeansPlugins_newerVersions.json"));
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        pluginsVerifier = new PluginsVerifier(httpClient);
        Assert.assertTrue(pluginsVerifier.arePluginsEnabled(core, "contextRoot", "serverUrl"));
    }

    @Test
    public void arePluginsEnabled_whenNodeIsNotFound () throws IOException {
        entity.setContent(new FileInputStream("src/test/resources/MBeans.json"));
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        pluginsVerifier = new PluginsVerifier(httpClient);
        Assert.assertFalse(pluginsVerifier.arePluginsEnabled(core, "contextRoot", "serverUrl"));
    }

    @Test
    public void arePluginsEnabled_whenNodeIsFound_olderSolrVersions () throws IOException {
        entity.setContent(new FileInputStream("src/test/resources/MBeansPlugins_olderVersions.json"));
        when(httpResponse.getEntity()).thenReturn(entity);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);
        pluginsVerifier = new PluginsVerifier(httpClient);
        Assert.assertTrue(pluginsVerifier.arePluginsEnabled(core, "contextRoot", "serverUrl"));
    }
}
