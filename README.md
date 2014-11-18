# AppDynamics Solr Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Solr is a popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
This extension collects metrics from Solr search engine and uploads them to AppDynamics Metric Broswer.

Solr statistics (Core, Query, Cache) are obtained through an HTTP request to the SolrInfoMBeanHandler at `http://<host>:<port>/solr/admin/mbeans`. Please refer to [SolrInfoMBeanHandler](http://wiki.apache.org/solr/SystemInformationRequestHandlers) for details.

Memory statistics are collected through an HTTP request SystemInfoHandler at `http://<host>:<port>/solr/admin/system`

##Installation

1. Run 'mvn clean install' from the solr-monitoring-extension directory and find the SolrMonitor.zip in the 'target' directory.
2. Unzip SolrMonitor.zip and copy the "SolrMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`
3. Configure the extension by referring to the below section.
4. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr for default metric-path.

##Configuration
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Solr instance, Cores and Request handlers to monitor by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/SolrMonitor/`.
Below is the sample.
    ```
        server:
          host: "localhost"
          port: 8983
        # Optional Parameters. Configure if any required
          username: ""
          password: ""
          contextRoot: "/solr"
          usessl: ""
          proxyHost: ""
          proxyPort: ""
          proxyUsername: ""
          proxyPassword: ""

        cores:
        - name: "collection1"
          queryHandlers: ["/select", "/update"]
        - name: ""
          queryHandlers: []
        .....
        .....
   
        #prefix used to show up metrics in AppDynamics
        metricPrefix:  "Custom Metrics|Solr|"

    ```
Specify as many cores as you want to monitor and corresponding comma separated request handlers. If none of the cores are specified, default core with empty request handlers is monitored.

2. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/SolrMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/SolrMonitor/config.yml" />
          ....
     </task-arguments>
    ```

## Metrics
Note : By default, a Machine agent or a AppServer agent can send a fixed number of metrics to the controller. To change this limit, please follow the instructions mentioned [here](http://docs.appdynamics.com/display/PRO14S/Metrics+Limits).
For eg.  
```    
    java -Dappdynamics.agent.maxMetrics=2500 -jar machineagent.jar
```

The following metrics are available for each core under Cores
###Core Metrics

The following metrics are reported under CORE

| Metric Name 			|
|-------------------------------|
|Number of Docs			|
|Max Docs				|
|Deleted Docs			|


###Query Statistics

The following metrics are reported under QUERYHANDLER for SearchHandler and UpdateHandler

| Metric Name 			|
|-------------------------------|
|Requests						|
|Errors							|
|Timeouts						|
|Average Requests Per Minute	|
|Average Requests Per Second	|
|5 min Rate Requests Per Minute	|
|Average Time Per Request (milliseconds)	|

###Memory Statistics

The following metrics are reported under MEMORY/JVM

| Metric Name 			|
|-------------------------------|
|Used (MB)			|
|Free (MB)			|
|Total (MB)			|

The following metrics are reported under MEMORY/System

| Metric Name 			|
|-------------------------------|
|Free Physical Memory (MB)	|
|Total Physical Memory (MB)	|
|Committed Virtual Memory (MB)	|
|Free Swap Size (MB)		|
|Total Swap Size (MB)		|
|Open File Descriptor Count	|
|Max File Descriptor Count	|

###Cache Statistics

The following metrics are reported under Cache /QueryResultCache

| Metric Name 			|
|-------------------------------|
|HitRatio %				|
|HitRatioCumulative %	|
|CacheSize (Bytes)		|

The following metrics are reported under Cache/DocumentCache

| Metric Name 			|
|-------------------------------|
|HitRatio %				|
|HitRatioCumulative	%	| 
|CacheSize (Bytes)		|

The following metrics are reported under Cache/FieldValueCache

| Metric Name 			|
|-------------------------------|
|HitRatio %				| 
|HitRatioCumulative %	| 
|CacheSize (Bytes)		|

The following metrics are reported under Cache/FilterCache

| Metric Name 			|
|-------------------------------|
|HitRatio %				| 
|HitRatioCumulative	%	| 
|CacheSize (Bytes)		|

## Custom Dashboard
![](https://github.com/Appdynamics/solr-monitoring-extension/raw/master/SolrDashboard.png)


##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/AppDynamics-eXchange/Solr-Monitoring-Extension/idi-p/6887) community.

##Support

For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).


