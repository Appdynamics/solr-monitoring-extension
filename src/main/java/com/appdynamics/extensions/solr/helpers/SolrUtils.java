package com.appdynamics.extensions.solr.helpers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

public class SolrUtils {
    private static final double BYTES_CONVERSION_FACTOR = 1024.0;
    private static final Logger logger = LoggerFactory.getLogger(SolrHelper.class);

    public static JsonNode getJsonNode(CloseableHttpResponse response) throws IOException {
        if (response == null) {
            return null;
        }
        ObjectMapper mapper = new ObjectMapper();

        return mapper.readValue(EntityUtils.toString(response.getEntity(), "UTF-8"), JsonNode.class);
    }

    /**
     * Converts Bytes to MegaBytes
     *
     * @param d
     * @return
     */
    public static double convertBytesToMB(Double d) {
        if (d != null) {
            d = d / (BYTES_CONVERSION_FACTOR * BYTES_CONVERSION_FACTOR);
        }
        return d;
    }

    /**
     * Converts from String form with Units("224 MB") to a number(224)
     *
     * @param valueStr
     * @return
     */
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
                // ignore
            }
            logger.error("Unrecognized string format: " + valueStr);
        }
        return null;
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

    public static Double multipyBy(Double value, int multiplier) {
        if (value != null) {
            value = value * multiplier;
        }
        return value;
    }
}
