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

import com.appdynamics.extensions.solr.SolrMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class CoreStats {

    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);

    private Double numDocs;
    private Double maxDocs;
    private Double deletedDocs;

    public void populateStats(Map<String, JsonNode> solrMBeansHandlersMap) {
        JsonNode node = solrMBeansHandlersMap.get("CORE");
        if (node != null) {
            JsonNode coreNode = node.path("searcher").path("stats");
            if (!coreNode.isMissingNode()) {
                this.setNumDocs(coreNode.path("numDocs").asDouble());
                this.setMaxDocs(coreNode.path("maxDoc").asDouble());
                this.setDeletedDocs(coreNode.path("deletedDocs").asDouble());
                if (logger.isDebugEnabled()) {
                    logger.debug("Docs=" + getNumDocs());
                    logger.debug("Max Docs=" + getMaxDocs());
                }
            }
        }
    }

    public Double getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(Double numDocs) {
        this.numDocs = numDocs;
    }

    public Double getMaxDocs() {
        return maxDocs;
    }

    public void setMaxDocs(Double maxDocs) {
        this.maxDocs = maxDocs;
    }

    public Double getDeletedDocs() {
        return deletedDocs;
    }

    public void setDeletedDocs(Double deletedDocs) {
        this.deletedDocs = deletedDocs;
    }
}
