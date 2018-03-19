/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.memory;

import com.appdynamics.extensions.solr.helpers.SolrUtils;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class SystemMemoryMetrics {
    private String coreName;
    private static final String METRIC_SEPARATOR = "|";
    private static final Logger logger = LoggerFactory.getLogger(SystemMemoryMetrics.class);

    SystemMemoryMetrics(String coreName) {
        this.coreName = coreName;
    }

    Map<String, BigDecimal> populateStats(JsonNode jsonNode) {
        String metricPath = METRIC_SEPARATOR + "Cores" + METRIC_SEPARATOR + coreName + METRIC_SEPARATOR + "MEMORY"
                + METRIC_SEPARATOR;
        String systemPath = metricPath + "System" + METRIC_SEPARATOR;
        Map<String, BigDecimal> systemMemoryMetrics = new HashMap<String, BigDecimal>();

        if (jsonNode != null) {
            JsonNode memoryMBeansNode = jsonNode.path("system");
            if (!memoryMBeansNode.isMissingNode()) {
                systemMemoryMetrics.put(systemPath + "Free Physical memory(MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertBytesToMB(memoryMBeansNode.path
                        ("freePhysicalMemorySize").asDouble())));
                systemMemoryMetrics.put(systemPath + "Total Physical memory (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertBytesToMB(memoryMBeansNode.path
                        ("totalPhysicalMemorySize").asDouble())));
                systemMemoryMetrics.put(systemPath + "Committed Virtual memory (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertBytesToMB(memoryMBeansNode.path
                        ("committedVirtualMemorySize").asDouble())));
                systemMemoryMetrics.put(systemPath + "Free Swap Size (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertBytesToMB(memoryMBeansNode.path
                        ("freeSwapSpaceSize").asDouble())));
                systemMemoryMetrics.put(systemPath + "Total Swap Size (MB)", SolrUtils.convertDoubleToBigDecimal(SolrUtils.convertBytesToMB(memoryMBeansNode.path
                        ("totalSwapSpaceSize").asDouble())));
                systemMemoryMetrics.put(systemPath + "Open File Descriptor Count", SolrUtils.convertDoubleToBigDecimal(memoryMBeansNode.path
                        ("openFileDescriptorCount").asDouble()));
                systemMemoryMetrics.put(systemPath + "Max File Descriptor Count", SolrUtils.convertDoubleToBigDecimal(memoryMBeansNode.path
                        ("maxFileDescriptorCount").asDouble()));
            } else {
                logger.error("Missing json node while retrieving system memory stats");
            }
        }
        return systemMemoryMetrics;
    }
}

