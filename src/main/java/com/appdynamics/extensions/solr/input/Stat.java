/*
 *   Copyright 2018 . AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.input;


import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
public class Stat {
    @XmlAttribute
    private String url;

    @XmlAttribute
    private String alias;

    @XmlAttribute(name = "rootElement")
    private String rootElement;

    @XmlAttribute
    private String structure;

    @XmlElement(name = "metric")
    private ArrayList<MetricConfig> metricConfig;

    @XmlElement(name = "stat")
    public Stat[] stats;

    @XmlAttribute
    private String metricSection;

    public String getStructure() {
        return structure;
    }

    public void setStructure(String structure) {
        this.structure = structure;
    }

    public String getMetricSection() {
        return metricSection;
    }

    public void setMetricSection(String metricSection) {
        this.metricSection = metricSection;
    }


    public String getRootElement() {
        return rootElement;
    }

    public void setRootElement(String rootElement) {
        this.rootElement = rootElement;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public ArrayList<MetricConfig> getMetricConfig() {
        return metricConfig;
    }

    public void setMetricConfig(ArrayList<MetricConfig> metricConfig) {
        this.metricConfig = metricConfig;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Stat[] getStats() {
        return stats;
    }

    public void setStats(Stat[] stats) {
        this.stats = stats;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Stats {
        @XmlElement(name = "stat")
        private Stat[] stats;

        public Stat[] getStats() {
            return stats;
        }

        public void setStats(Stat[] stats) {
            this.stats = stats;
        }
    }
}
