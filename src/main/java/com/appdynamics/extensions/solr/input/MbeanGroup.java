package com.appdynamics.extensions.solr.input;

/**
 * Created by bhuvnesh.kumar on 4/13/18.
 */

import javax.xml.bind.annotation.*;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
public class MbeanGroup {
    @XmlAttribute
    private String name;

    @XmlAttribute
    private String metricSection;

//    @XmlElement
//    private KeyGroup keyGroup;

    @XmlElement(name="keyGroup")
    public KeyGroup keyGroup;

    public KeyGroup getKeyGroup() {
        return keyGroup;
    }

    public void setKeyGroup(KeyGroup keyGroup) {
        this.keyGroup = keyGroup;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getMetricSection() {
        return metricSection;
    }

    public void setMetricSection(String metricSection) {
        this.metricSection = metricSection;
    }
//    public KeyGroup getKeyGroup() {
//        return keyGroup;
//    }
//
//    public void setKeyGroup(KeyGroup keyGroup) {
//        this.keyGroup = keyGroup;
//    }
}
