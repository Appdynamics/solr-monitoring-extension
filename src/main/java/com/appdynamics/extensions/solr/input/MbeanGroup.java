package com.appdynamics.extensions.solr.input;

/**
 * Created by bhuvnesh.kumar on 4/13/18.
 */

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
public class MbeanGroup {
    @XmlAttribute
    private String category;

    @XmlAttribute
    private String subCategory;

    @XmlAttribute
    private String metricSection;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategory() {
        return subCategory;
    }

    public void setSubcategory(String subcategory) {
        this.subCategory = subcategory;
    }


    public String getMetricSection() {
        return metricSection;
    }

    public void setMetricSection(String metricSection) {
        this.metricSection = metricSection;
    }
}
