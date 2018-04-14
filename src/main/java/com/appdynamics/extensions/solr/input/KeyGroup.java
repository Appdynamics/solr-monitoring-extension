package com.appdynamics.extensions.solr.input;


/**
 * Created by bhuvnesh.kumar on 4/13/18.
 */

import javax.xml.bind.annotation.*;


@XmlAccessorType(XmlAccessType.FIELD)
public class KeyGroup {

    @XmlAttribute
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}


