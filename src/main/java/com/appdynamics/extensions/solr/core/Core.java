/*
 *   Copyright 2018. AppDynamics LLC and its affiliates.
 *   All Rights Reserved.
 *   This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 *   The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */
package com.appdynamics.extensions.solr.core;

import java.util.List;

public class Core {
    private String name;
    private String pingHandler;
    private List<String> queryHandlers;

    public String getPingHandler() {
        return pingHandler;
    }

    public void setPingHandler(String pingHandler) {
        this.pingHandler = pingHandler;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getQueryHandlers() {
        return queryHandlers;
    }

    public void setQueryHandlers(List<String> queryHandlers) {
        this.queryHandlers = queryHandlers;
    }

}
