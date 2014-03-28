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

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

public class MemoryStats extends Stats {

	private static final Logger LOG = Logger.getLogger("com.singularity.extensions.MemoryStats");

	private static final String URI_QUERY_STRING = "/solr/admin/system?stats=true&wt=json";

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

	public MemoryStats(String host, String port, IHttpClientWrapper httpClient) {
		super(host, port, httpClient);
	}

	@Override
	public void populateStats() {
		String jsonString = getJsonResponseString(constructURL());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jvmMBeansNode = null;
		JsonNode memoryMBeansNode = null;
		try {
			jvmMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("jvm").path("memory");
			memoryMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("system");
		} catch (JsonParseException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} catch (JsonMappingException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		} catch (IOException e) {
			LOG.error(e.getMessage(), e);
			throw new RuntimeException(e.getMessage());
		}

		if (!jvmMBeansNode.isMissingNode()) {
			this.setJvmMemoryUsed(convertMemoryStringToDouble(jvmMBeansNode.path("used").asText()));
			this.setJvmMemoryFree(convertMemoryStringToDouble(jvmMBeansNode.path("free").asText()));
			this.setJvmMemoryTotal(convertMemoryStringToDouble(jvmMBeansNode.path("total").asText()));
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

	/**
	 * Returns JsonResponse as String for Memory stats. Overrides super class
	 * method as the structure of Json is different from other stats
	 */
	@Override
	public String getJsonResponseString(String resource) {
		HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
		HttpExecutionResponse response = getHttpClient().executeHttpOperation(request, new Log4JLogger(LOG));
		if (response.getStatusCode() == 404) {
			throw new RuntimeException("Error accessing " + resource);
		}
		return response.getResponseBody();
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
		this.freePhysicalMemorySize = convertBytesToMB(freePhysicalMemorySize);
	}

	public Number getTotalPhysicalMemorySize() {
		return totalPhysicalMemorySize;
	}

	public void setTotalPhysicalMemorySize(Number totalPhysicalMemorySize) {
		this.totalPhysicalMemorySize = convertBytesToMB(totalPhysicalMemorySize);
	}

	public Number getCommittedVirtualMemorySize() {
		return committedVirtualMemorySize;
	}

	public void setCommittedVirtualMemorySize(Number committedVirtualMemorySize) {
		this.committedVirtualMemorySize = convertBytesToMB(committedVirtualMemorySize);
	}

	public Number getFreeSwapSpaceSize() {
		return freeSwapSpaceSize;
	}

	public void setFreeSwapSpaceSize(Number freeSwapSpaceSize) {
		this.freeSwapSpaceSize = convertBytesToMB(freeSwapSpaceSize);
	}

	public Number getTotalSwapSpaceSize() {
		return totalSwapSpaceSize;
	}

	public void setTotalSwapSpaceSize(Number totalSwapSpaceSize) {
		this.totalSwapSpaceSize = convertBytesToMB(totalSwapSpaceSize);
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

	/**
	 * Converts Bytes to MegaBytes
	 * 
	 * @param d
	 * @return
	 */
	private double convertBytesToMB(Number d) {
		return (double) Math.round(d.doubleValue() / (1024.0 * 1024.0));
	}

	/**
	 * Converts from String form with Units("224 MB") to a number(224)
	 * 
	 * @param value
	 * @return
	 */
	private static Double convertMemoryStringToDouble(String value) {
		if (value.contains("KB"))
			return Double.valueOf(value.split("KB")[0].trim()) / 1024.0;
		else if (value.contains("MB"))
			return Double.valueOf(value.split("MB")[0].trim());
		else if (value.contains("GB"))
			return Double.valueOf(value.split("GB")[0].trim()) * 1024.0;
		else
			throw new RuntimeException("Unrecognized string format: " + value);
	}

	@Override
	public String constructURL() {
		return "http://" + getHost() + ":" + getPort() + URI_QUERY_STRING;
	}

}
