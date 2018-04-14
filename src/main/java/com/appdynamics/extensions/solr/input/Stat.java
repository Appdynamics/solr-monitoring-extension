/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.solr.input;

/**
 * Created by aditya.jagtiani on 3/26/18.
 */

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
    @XmlAttribute(name = "metric-type")
    private String metricType;
    @XmlAttribute
    public String children;
    @XmlElement(name = "metric")
    private ArrayList<MetricConfig> metricConfig;
    @XmlElement(name = "stat")
    public Stat[] stats;
    @XmlElement(name="mBeanGroup")
    public MbeanGroup mbeanGroup;

    public MbeanGroup getMbeanGroup() {
        return mbeanGroup;
    }

    public void setMbeanGroup(MbeanGroup mbeanGroup) {
        this.mbeanGroup = mbeanGroup;
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

    public String getMetricType() {
        return metricType;
    }

    public void setMetricType(String metricType) {
        this.metricType = metricType;
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

    public String getChildren() {
        return children;
    }

    public void setChildren(String children) {
        this.children = children;
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
