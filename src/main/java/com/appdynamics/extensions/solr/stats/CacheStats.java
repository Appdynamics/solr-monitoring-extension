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
import com.appdynamics.extensions.solr.SolrMonitor;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Map;

public class CacheStats {

    private static final int PERCENT_MULTIPLIER = 100;

    public static final Logger logger = LoggerFactory.getLogger(SolrMonitor.class);
    private Double queryResultCacheHitRatio;
    private Double queryResultCacheHitRatioCumulative;
    private Double queryResultCacheSize;

    private Double documentCacheHitRatio;
    private Double documentCacheHitRatioCumulative;
    private Double documentCacheSize;

    private Double fieldValueCacheHitRatio;
    private Double fieldValueCacheHitRatioCumulative;
    private Double fieldValueCacheSize;

    private Double filterCacheHitRatio;
    private Double filterCacheHitRatioCumulative;
    private Double filterCacheSize;

    public void populateStats(Map<String, JsonNode> solrMBeansHandlersMap) throws Exception {

        JsonNode cacheNode = solrMBeansHandlersMap.get("CACHE");
        JsonNode queryResultCacheStats = cacheNode.path("queryResultCache").path("stats");

        if (!queryResultCacheStats.isMissingNode()) {
            this.setQueryResultCacheHitRatio(SolrHelper.multipyBy(queryResultCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setQueryResultCacheHitRatioCumulative(SolrHelper.multipyBy(queryResultCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setQueryResultCacheSize(queryResultCacheStats.path("size").asDouble());

            if (logger.isDebugEnabled()) {
                logger.debug("hitratio % = " + getQueryResultCacheHitRatio());
                logger.debug("cumulative_hitratio % = " + getQueryResultCacheHitRatioCumulative());
                logger.debug("size= " + getQueryResultCacheSize());
            }
        } else {
            logger.info("queryResultCache is disabled in solrconfig.xml");
        }

        JsonNode documentCacheStats = cacheNode.path("documentCache").path("stats");

        if (!documentCacheStats.isMissingNode()) {
            this.setDocumentCacheHitRatio(SolrHelper.multipyBy(documentCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setDocumentCacheHitRatioCumulative(SolrHelper.multipyBy(documentCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setDocumentCacheSize(documentCacheStats.path("size").asDouble());
        } else {
            logger.info("documentCache is disabled in solrconfig.xml");
        }

        JsonNode fieldValueCacheStats = cacheNode.path("fieldValueCache").path("stats");

        if (!fieldValueCacheStats.isMissingNode()) {
            this.setFieldValueCacheHitRatio(SolrHelper.multipyBy(fieldValueCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setFieldValueCacheHitRatioCumulative(SolrHelper.multipyBy(fieldValueCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setFieldValueCacheSize(fieldValueCacheStats.path("size").asDouble());
        } else {
            logger.info("fieldValueCache is disabled in solrconfig.xml");
        }

        JsonNode filterCacheStats = cacheNode.path("filterCache").path("stats");

        if (!filterCacheStats.isMissingNode()) {
            this.setFilterCacheHitRatio(SolrHelper.multipyBy(filterCacheStats.path("hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setFilterCacheHitRatioCumulative(SolrHelper.multipyBy(filterCacheStats.path("cumulative_hitratio").asDouble(), PERCENT_MULTIPLIER));
            this.setFilterCacheSize(filterCacheStats.path("size").asDouble());
        } else {
            logger.info("filterCache is disabled in solrconfig.xml");
        }
    }

    public Double getQueryResultCacheHitRatio() {
        return queryResultCacheHitRatio;
    }

    public void setQueryResultCacheHitRatio(Double queryResultCacheHitRatio) {
        this.queryResultCacheHitRatio = queryResultCacheHitRatio;
    }

    public Double getDocumentCacheHitRatio() {
        return documentCacheHitRatio;
    }

    public void setDocumentCacheHitRatio(Double documentCacheHitRatio) {
        this.documentCacheHitRatio = documentCacheHitRatio;
    }

    public Double getFieldValueCacheHitRatio() {
        return fieldValueCacheHitRatio;
    }

    public void setFieldValueCacheHitRatio(Double fieldValueCacheHitRatio) {
        this.fieldValueCacheHitRatio = fieldValueCacheHitRatio;
    }

    public Double getFilterCacheHitRatio() {
        return filterCacheHitRatio;
    }

    public void setFilterCacheHitRatio(Double filterCacheHitRatio) {
        this.filterCacheHitRatio = filterCacheHitRatio;
    }

    public Double getQueryResultCacheHitRatioCumulative() {
        return queryResultCacheHitRatioCumulative;
    }

    public void setQueryResultCacheHitRatioCumulative(Double queryResultCacheHitRatioCumulative) {
        this.queryResultCacheHitRatioCumulative = queryResultCacheHitRatioCumulative;
    }

    public Double getDocumentCacheHitRatioCumulative() {
        return documentCacheHitRatioCumulative;
    }

    public void setDocumentCacheHitRatioCumulative(Double documentCacheHitRatioCumulative) {
        this.documentCacheHitRatioCumulative = documentCacheHitRatioCumulative;
    }

    public Double getFieldValueCacheHitRatioCumulative() {
        return fieldValueCacheHitRatioCumulative;
    }

    public void setFieldValueCacheHitRatioCumulative(Double fieldValueCacheHitRatioCumulative) {
        this.fieldValueCacheHitRatioCumulative = fieldValueCacheHitRatioCumulative;
    }

    public Double getFilterCacheHitRatioCumulative() {
        return filterCacheHitRatioCumulative;
    }

    public void setFilterCacheHitRatioCumulative(Double filterCacheHitRatioCumulative) {
        this.filterCacheHitRatioCumulative = filterCacheHitRatioCumulative;
    }

    public Double getQueryResultCacheSize() {
        return queryResultCacheSize;
    }

    public void setQueryResultCacheSize(Double queryResultCacheSize) {
        this.queryResultCacheSize = queryResultCacheSize;
    }

    public Double getDocumentCacheSize() {
        return documentCacheSize;
    }

    public void setDocumentCacheSize(Double documentCacheSize) {
        this.documentCacheSize = documentCacheSize;
    }

    public Double getFieldValueCacheSize() {
        return fieldValueCacheSize;
    }

    public void setFieldValueCacheSize(Double fieldValueCacheSize) {
        this.fieldValueCacheSize = fieldValueCacheSize;
    }

    public Double getFilterCacheSize() {
        return filterCacheSize;
    }

    public void setFilterCacheSize(Double filterCacheSize) {
        this.filterCacheSize = filterCacheSize;
    }
}
