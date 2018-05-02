package com.appdynamics.extensions.solr;

import com.google.common.collect.Maps;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.junit.Test;

import java.util.Map;

/**
 * Created by bhuvnesh.kumar on 5/2/18.
 */
public class SolrMonitorTaskTest {

    @Test
    public void test() throws TaskExecutionException {
        SolrMonitor monitor = new SolrMonitor();
        Map<String, String> taskArgs = Maps.newHashMap();
        taskArgs.put("config-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/config.yml");
        taskArgs.put("metric-file", "/Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/metrics.xml");
        monitor.execute(taskArgs, null);
    }
///Users/bhuvnesh.kumar/repos/appdynamics/extensions/solr-monitoring-extension/src/test/resources/conf/metrics.xml
}
