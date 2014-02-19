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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.singularity.ee.util.httpclient.HttpClientWrapper;
import com.singularity.ee.util.httpclient.HttpExecutionRequest;
import com.singularity.ee.util.httpclient.HttpExecutionResponse;
import com.singularity.ee.util.httpclient.HttpOperation;
import com.singularity.ee.util.httpclient.IHttpClientWrapper;
import com.singularity.ee.util.log4j.Log4JLogger;

public class MemoryStats extends Stats {

	private static Logger logger = Logger.getLogger(MemoryStats.class.getName());

	private String memoryresource = "/solr/admin/system";

	private Number jvmMemoryUsed;

	private Number jvmMemoryFree;

	private Number jvmMemoryTotal;

	private Number freePhysicalMemorySize;

	private Number totalPhysicalMemorySize;

	private Number committedVirtualMemorySize;

	public MemoryStats(String host, String port) {
		super(host, port);
		logger.setLevel(Level.INFO);
	}

	public void populateStats() {
		String jsonString = getJsonResponseString(getUrl() + memoryresource + getQueryString());

		ObjectMapper mapper = new ObjectMapper();
		JsonNode jvmMBeansNode = null;
		JsonNode memoryMBeansNode = null;
		try {
			jvmMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("jvm").path("memory");
			memoryMBeansNode = mapper.readValue(jsonString.getBytes(), JsonNode.class).path("system");
		} catch (JsonParseException e) {
			logger.error("JsonParseException in " + e.getClass());
			throw new RuntimeException("JsonParseException in " + e.getClass());
		} catch (JsonMappingException e) {
			logger.error("JsonMappingException in " + e.getClass());
			throw new RuntimeException("JsonMappingException in " + e.getClass());
		} catch (IOException e) {
			logger.error("IOException in " + e.getClass());
			throw new RuntimeException("IOException in " + e.getClass());
		}

		if (!jvmMBeansNode.isMissingNode()) {
			this.setJvmMemoryUsed(jvmMBeansNode.path("used").asDouble());
			this.setJvmMemoryFree(jvmMBeansNode.path("free").asDouble());
			this.setJvmMemoryTotal(jvmMBeansNode.path("total").asDouble());
		} else {
			logger.error("Error in retrieving jvm memory stats");
		}

		if (!memoryMBeansNode.isMissingNode()) {
			this.setFreePhysicalMemorySize(memoryMBeansNode.path("freePhysicalMemorySize").asDouble());
			this.setTotalPhysicalMemorySize(memoryMBeansNode.path("totalPhysicalMemorySize").asDouble());
			this.setCommittedVirtualMemorySize(memoryMBeansNode.path("committedVirtualMemorySize").asDouble());
		} else {
			logger.error("Error in retrieving system memory stats");
		}

	}

	@Override
	public String getJsonResponseString(String resource) {
		IHttpClientWrapper httpClient = HttpClientWrapper.getInstance();
		HttpExecutionRequest request = new HttpExecutionRequest(resource, "", HttpOperation.GET);
		HttpExecutionResponse response = httpClient.executeHttpOperation(request, new Log4JLogger(logger));
		if (response.isExceptionHappened() || response.getStatusCode() == 400) {
			logger.error("Solr instance down OR URL " + resource + " not supported");
			throw new RuntimeException("Solr instance down OR URL " + resource + " not supported");
		}
		return response.getResponseBody();
	}

	public String getResourceAppender() {
		return memoryresource;
	}

	public void setResourceAppender(String resourceAppender) {
		this.memoryresource = resourceAppender;
	}

	public Number getJvmMemoryUsed() {
		return jvmMemoryUsed;
	}

	public void setJvmMemoryUsed(Number jvmMemoryUsed) {
		this.jvmMemoryUsed = jvmMemoryUsed;
	}

	public Number getJvmMemoryFree() {
		return jvmMemoryFree;
	}

	public void setJvmMemoryFree(Number jvmMemoryFree) {
		this.jvmMemoryFree = jvmMemoryFree;
	}

	public Number getJvmMemoryTotal() {
		return jvmMemoryTotal;
	}

	public void setJvmMemoryTotal(Number jvmMemoryTotal) {
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

	private double convertBytesToMB(Number d) {
		return (double) Math.round(d.doubleValue() / (1024.0 * 1024.0));
	}

}
