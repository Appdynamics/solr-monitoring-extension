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

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.appdynamics.extensions.solr.SolrHelper;
import com.fasterxml.jackson.databind.JsonNode;

public class MemoryStats {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.MemoryStats");

	private Double jvmMemoryUsed;

	private Double jvmMemoryFree;

	private Double jvmMemoryTotal;

	private Number freePhysicalMemorySize;

	private Number totalPhysicalMemorySize;

	private Number committedVirtualMemorySize;

	private Number freeSwapSpaceSize;

	private Number totalSwapSpaceSize;

	private Number openFileDescriptorCount;

	private Number maxFileDescriptorCount;

	public void populateStats(InputStream inputStream) throws IOException {
		JsonNode jsonNode = SolrHelper.getJsonNode(inputStream);
		if (jsonNode != null) {
			JsonNode jvmMBeansNode = jsonNode.path("jvm").path("memory");
			JsonNode memoryMBeansNode = jsonNode.path("system");
			if (!jvmMBeansNode.isMissingNode()) {
				this.setJvmMemoryUsed(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("used").asText()));
				this.setJvmMemoryFree(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("free").asText()));
				this.setJvmMemoryTotal(SolrHelper.convertMemoryStringToDouble(jvmMBeansNode.path("total").asText()));
				if (LOG.isDebugEnabled()) {
					LOG.debug("used=" + getJvmMemoryUsed());
					LOG.debug("free=" + getJvmMemoryFree());
				}
			} else {
				LOG.error("Missing json node while retrieving jvm memory stats");
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
				LOG.error("Missing json node while retrieving system memory stats");
			}
		}
	}

	public Number getJvmMemoryUsed() {
		return jvmMemoryUsed;
	}

	public void setJvmMemoryUsed(Double jvmMemoryUsed) {
		this.jvmMemoryUsed = jvmMemoryUsed;
	}

	public Number getJvmMemoryFree() {
		return jvmMemoryFree;
	}

	public void setJvmMemoryFree(Double jvmMemoryFree) {
		this.jvmMemoryFree = jvmMemoryFree;
	}

	public Number getJvmMemoryTotal() {
		return jvmMemoryTotal;
	}

	public void setJvmMemoryTotal(Double jvmMemoryTotal) {
		this.jvmMemoryTotal = jvmMemoryTotal;
	}

	public Number getFreePhysicalMemorySize() {
		return freePhysicalMemorySize;
	}

	public void setFreePhysicalMemorySize(Number freePhysicalMemorySize) {
		this.freePhysicalMemorySize = SolrHelper.convertBytesToMB(freePhysicalMemorySize);
	}

	public Number getTotalPhysicalMemorySize() {
		return totalPhysicalMemorySize;
	}

	public void setTotalPhysicalMemorySize(Number totalPhysicalMemorySize) {
		this.totalPhysicalMemorySize = SolrHelper.convertBytesToMB(totalPhysicalMemorySize);
	}

	public Number getCommittedVirtualMemorySize() {
		return committedVirtualMemorySize;
	}

	public void setCommittedVirtualMemorySize(Number committedVirtualMemorySize) {
		this.committedVirtualMemorySize = SolrHelper.convertBytesToMB(committedVirtualMemorySize);
	}

	public Number getFreeSwapSpaceSize() {
		return freeSwapSpaceSize;
	}

	public void setFreeSwapSpaceSize(Number freeSwapSpaceSize) {
		this.freeSwapSpaceSize = SolrHelper.convertBytesToMB(freeSwapSpaceSize);
	}

	public Number getTotalSwapSpaceSize() {
		return totalSwapSpaceSize;
	}

	public void setTotalSwapSpaceSize(Number totalSwapSpaceSize) {
		this.totalSwapSpaceSize = SolrHelper.convertBytesToMB(totalSwapSpaceSize);
	}

	public Number getOpenFileDescriptorCount() {
		return openFileDescriptorCount;
	}

	public void setOpenFileDescriptorCount(Number openFileDescriptorCount) {
		this.openFileDescriptorCount = openFileDescriptorCount;
	}

	public Number getMaxFileDescriptorCount() {
		return maxFileDescriptorCount;
	}

	public void setMaxFileDescriptorCount(Number maxFileDescriptorCount) {
		this.maxFileDescriptorCount = maxFileDescriptorCount;
	}
}
