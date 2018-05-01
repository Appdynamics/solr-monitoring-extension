package com.appdynamics.extensions.solr.utils;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.solr.input.MetricConfig;
import com.appdynamics.extensions.solr.input.Stat;
import com.google.common.base.Strings;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

/**
 * Created by bhuvnesh.kumar on 4/25/18.
 */
public class MetricUtils {
    private static final Logger logger = LoggerFactory.getLogger(MetricUtils.class);
    private static final double BYTES_CONVERSION_FACTOR = 1024.0;

    public static JsonNode getJsonNode(Stat stat, JsonNode nodes) {
        JsonNode newNode = nodes;

        if(stat.getRootElement() != null){
            if(nodes.get(stat.getRootElement()) != null) {
                newNode = nodes.get(stat.getRootElement());
            }
        }
        return newNode;
    }


    public static String replaceCharacter(String metricPath, List<Map<String, String>> metricReplacer) {

        for (Map chars : metricReplacer) {
            String replace = (String) chars.get("replace");
            String replaceWith = (String) chars.get("replaceWith");

            if (metricPath.contains(replace)) {
                metricPath = metricPath.replaceAll(replace, replaceWith);
            }
        }
        return metricPath;
    }

    public static Map mapOfArrayList(ArrayList<?> arrayOfNodes) {
        Map<String, Object> map = new HashMap<String, Object>();

        for (int i = 0; i < arrayOfNodes.size(); i = i + 2) {
            String name = (String) arrayOfNodes.get(i);
            if (arrayOfNodes.get(i + 1) != null) {
                map.put(name, arrayOfNodes.get(i + 1));
            }
        }
        return map;
    }

    public static Boolean checkForEmptyAttribute(MetricConfig metricConfig){
        Boolean result = false;
        if( metricConfig.getAttr() == null ){
            logger.debug("No Metric Attribute defined");
            result = true;
        }
        return result;
    }


    public static Double convertMemoryStringToDouble(String valueStr) {
        if (!Strings.isNullOrEmpty(valueStr)) {
            String strippedValueStr = null;
            try {
                if (valueStr.contains("KB")) {
                    strippedValueStr = valueStr.split("KB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr) / BYTES_CONVERSION_FACTOR;
                } else if (valueStr.contains("MB")) {
                    strippedValueStr = valueStr.split("MB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr);
                } else if (valueStr.contains("GB")) {
                    strippedValueStr = valueStr.split("GB")[0].trim();
                    return unLocalizeStrValue(strippedValueStr) * BYTES_CONVERSION_FACTOR;
                }
            } catch (Exception e) {
                logger.error("Unrecognized string format: " + valueStr);
            }
        }
        return unLocalizeStrValue(valueStr);
    }


    private static Double unLocalizeStrValue(String valueStr) {
        try {
            Locale loc = Locale.getDefault();
            return Double.valueOf(NumberFormat.getInstance(loc).parse(valueStr).doubleValue());
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
}
