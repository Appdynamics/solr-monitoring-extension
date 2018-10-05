/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

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
        taskArgs.put("config-file", "src/test/resources/conf/config.yml");
        taskArgs.put("metric-file", "src/test/resources/conf/metrics-v7.xml");
        monitor.execute(taskArgs, null);
    }
}
