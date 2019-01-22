/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.utils;

import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.Stat;
import com.google.common.base.Strings;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

import static com.appdynamics.extensions.solr.utils.Constants.*;

/**
 * Created by bhuvnesh.kumar on 4/25/18.
 */
public class MetricUtils {
    private static final Logger logger = LoggerFactory.getLogger(MetricUtils.class);

    public static JsonNode getJsonNode(Stat stat, JsonNode nodes) {
        JsonNode newNode = nodes;

        if (stat.getRootElement() != null) {
            if (nodes.get(stat.getRootElement()) != null) {
                newNode = nodes.get(stat.getRootElement());
            }
        }
        return newNode;
    }

    public static JsonNode getMetricSectionMetrics(Stat stat, JsonNode jsonNode) {
        JsonNode node = jsonNode;
        if (stat.getMetricSection() != null) {
            if (jsonNode.get(stat.getMetricSection()) != null) {
                node = jsonNode.get(stat.getMetricSection());
            }
        }
        return node;

    }

    public static JsonNode getJsonNodeFromMap(JsonNode childNode, Map<String, ?> jsonMap, Stat childStat) {
        if (childStat.getRootElement() != null) {
            if (jsonMap.get(childStat.getRootElement()) != null) {
                childNode = (JsonNode) jsonMap.get(childStat.getRootElement());
            }
        }
        return childNode;
    }

    public static String getMetricPathAfterCharacterReplacement(String metricPath, List<Map<String, String>> metricReplacer) {

        for (Map chars : metricReplacer) {
            String replace = (String) chars.get(REPLACE);
            String replaceWith = (String) chars.get(REPLACE_WITH);

            if (metricPath.contains(replace)) {
                metricPath = metricPath.replaceAll(replace, replaceWith);
            }
        }
        return metricPath;
    }

    public static Map<String, Object> getMapOfArrayNodes(JsonNode arrayOfNodes) {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < arrayOfNodes.size(); i = i + 2) {
            String name = arrayOfNodes.get(i).asText();
            if (arrayOfNodes.get(i + 1) != null) {
                JsonNode jsonNode = mapper.convertValue(arrayOfNodes.get(i + 1), JsonNode.class);
                map.put(name, jsonNode);
            }
        }
        return map;
    }

    public static Boolean isVersion7OrHigher(Map server, CloseableHttpClient httpClient) {
        String applicationName = server.get("applicationName").toString();
        String firstCollectionNameFromList = ((List) server.get(COLLECTIONNAME)).get(0).toString();
        String url = UrlBuilder.fromYmlServerConfig(server).build() + FORWARD_SLASH + applicationName + FORWARD_SLASH + firstCollectionNameFromList + "/admin/system?stats=true&wt=json";
        JsonNode jsonNode = HttpClientUtils.getResponseAsJson(httpClient, url, JsonNode.class);
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ?> jsonMap = objectMapper.convertValue(jsonNode, Map.class);
        Map<String, ?> luceneMap = (Map) jsonMap.get(LUCENE);
        String versionValue = luceneMap.get(SOLR_SPEC_VERSION).toString();
        String[] charArray = versionValue.split("\\.");
        int version = Integer.valueOf(charArray[0]);
        return version >= 7;
    }

    public static Double convertMemoryStringToDouble(String valueStr) {
        if (!Strings.isNullOrEmpty(valueStr)) {
            String strippedValueStr;
            try {
                if (valueStr.contains(KB)) {
                    strippedValueStr = valueStr.split(KB)[0].trim();
                    return unLocalizeStrValue(strippedValueStr) / BYTES_CONVERSION_FACTOR;
                } else if (valueStr.contains(MB)) {
                    strippedValueStr = valueStr.split(MB)[0].trim();
                    return unLocalizeStrValue(strippedValueStr);
                } else if (valueStr.contains(GB)) {
                    strippedValueStr = valueStr.split(GB)[0].trim();
                    return unLocalizeStrValue(strippedValueStr) * BYTES_CONVERSION_FACTOR;
                }
            } catch (Exception e) {
                logger.error("Unrecognized string format: " + valueStr, e);
            }
        }
        return unLocalizeStrValue(valueStr);
    }


    private static Double unLocalizeStrValue(String valueStr) {
        try {
            Locale loc = Locale.getDefault();
            if (loc != null) {
                return NumberFormat.getInstance(loc).parse(valueStr).doubleValue();
            } else {
                logger.debug("Locale Object's default value = null, returning null");
                return null;
            }
        } catch (ParseException e) {
            logger.error("Exception while unlocalizing number string " + valueStr, e);
        }
        return null;
    }

    public static List<Metric> getListMetrics(Map<String, Metric> metricMap) {
        List<Metric> metricList = new ArrayList<Metric>();
        for (String path : metricMap.keySet()) {
            metricList.add(metricMap.get(path));
        }
        return metricList;
    }

    public static boolean isJsonArray(Stat stat) {
        return stat.getStructure() != null && stat.getStructure().equals("array");
    }
}
