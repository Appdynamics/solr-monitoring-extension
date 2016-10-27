/**
 * Copyright 2013 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamics.extensions.solr.stats;

import com.appdynamics.extensions.solr.SolrHelper;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;

public class MemoryStats {

    public static final Logger logger = LoggerFactory.getLogger(MemoryStats.class);

    private Double jvmMemoryUsed;
    private Double jvmMemoryFree;
    private Double jvmMemoryTotal;
    private Double freePhysicalMemorySize;
    private Double totalPhysicalMemorySize;
    private Double committedVirtualMemorySize;
    private Double freeSwapSpaceSize;
    private Double totalSwapSpaceSize;
    private Double openFileDescriptorCount;
    private Double maxFileDescriptorCount;

    public void populateStats(CloseableHttpResponse response) throws IOException {
        JsonNode jsonNode = SolrHelper.getJsonNode(response);
        if (jsonNode != null) {
            JsonNode jvmMBeansNode = jsonNode.path("jvm").path("memory");
            JsonNode memoryMBeansNode = jsonNode.path("system");
            if (!jvmMBeansNode.isMissingNode()) {
                this.setJvmMemoryUsed(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("used").asText()));
                this.setJvmMemoryFree(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("free").asText()));
                this.setJvmMemoryTotal(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("total").asText()));
                if (logger.isDebugEnabled()) {
                    logger.debug("used=" + getJvmMemoryUsed());
                    logger.debug("free=" + getJvmMemoryFree());
                }
            } else {
                logger.error("Missing json node while retrieving jvm memory stats");
            }

            if (!memoryMBeansNode.isMissingNode()) {
                this.setFreePhysicalMemorySize(memoryMBeansNode.path("freePhysicalMemorySize").asDouble());
                this.setTotalPhysicalMemorySize(memoryMBeansNode.path("totalPhysicalMemorySize").asDouble());
                this.setCommittedVirtualMemorySize(memoryMBeansNode.path("committedVirtualMemorySize").asDouble());
                this.setFreeSwapSpaceSize(memoryMBeansNode.path("freeSwapSpaceSize").asDouble());
                this.setTotalSwapSpaceSize(memoryMBeansNode.path("totalSwapSpaceSize").asDouble());
                this.setOpenFileDescriptorCount(memoryMBeansNode.path("openFileDescriptorCount").asDouble());
                this.setMaxFileDescriptorCount(memoryMBeansNode.path("maxFileDescriptorCount").asDouble());
            } else {
                logger.error("Missing json node while retrieving system memory stats");
            }
        }
    }

    public Double getJvmMemoryUsed() {
        return jvmMemoryUsed;
    }

    public void setJvmMemoryUsed(Double jvmMemoryUsed) {
        this.jvmMemoryUsed = jvmMemoryUsed;
    }

    public Double getJvmMemoryFree() {
        return jvmMemoryFree;
    }

    public void setJvmMemoryFree(Double jvmMemoryFree) {
        this.jvmMemoryFree = jvmMemoryFree;
    }

    public Double getJvmMemoryTotal() {
        return jvmMemoryTotal;
    }

    public void setJvmMemoryTotal(Double jvmMemoryTotal) {
        this.jvmMemoryTotal = jvmMemoryTotal;
    }

    public Double getFreePhysicalMemorySize() {
        return freePhysicalMemorySize;
    }

    public void setFreePhysicalMemorySize(Double freePhysicalMemorySize) {
        this.freePhysicalMemorySize = SolrHelper.convertBytesToMB(freePhysicalMemorySize);
    }

    public Double getTotalPhysicalMemorySize() {
        return totalPhysicalMemorySize;
    }

    public void setTotalPhysicalMemorySize(Double totalPhysicalMemorySize) {
        this.totalPhysicalMemorySize = SolrHelper.convertBytesToMB(totalPhysicalMemorySize);
    }

    public Double getCommittedVirtualMemorySize() {
        return committedVirtualMemorySize;
    }

    public void setCommittedVirtualMemorySize(Double committedVirtualMemorySize) {
        this.committedVirtualMemorySize = SolrHelper.convertBytesToMB(committedVirtualMemorySize);
    }

    public Double getFreeSwapSpaceSize() {
        return freeSwapSpaceSize;
    }

    public void setFreeSwapSpaceSize(Double freeSwapSpaceSize) {
        this.freeSwapSpaceSize = SolrHelper.convertBytesToMB(freeSwapSpaceSize);
    }

    public Double getTotalSwapSpaceSize() {
        return totalSwapSpaceSize;
    }

    public void setTotalSwapSpaceSize(Double totalSwapSpaceSize) {
        this.totalSwapSpaceSize = SolrHelper.convertBytesToMB(totalSwapSpaceSize);
    }

    public Double getOpenFileDescriptorCount() {
        return openFileDescriptorCount;
    }

    public void setOpenFileDescriptorCount(Double openFileDescriptorCount) {
        this.openFileDescriptorCount = openFileDescriptorCount;
    }

    public Double getMaxFileDescriptorCount() {
        return maxFileDescriptorCount;
    }

    public void setMaxFileDescriptorCount(Double maxFileDescriptorCount) {
        this.maxFileDescriptorCount = maxFileDescriptorCount;
    }
}
