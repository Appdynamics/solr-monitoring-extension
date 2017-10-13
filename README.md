# AppDynamics Monitoring Extension for use with Solr

##Use Case

Solr is a popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
This extension collects metrics from the Solr search engine and uploads them to the AppDynamics Metric Broswer.

Solr statistics (Core, Query, Cache) are obtained through an HTTP request to the SolrInfoMBeanHandler at `http://<host>:<port>/solr/admin/mbeans`. Please refer to [SolrInfoMBeanHandler](http://wiki.apache.org/solr/SystemInformationRequestHandlers) for details.

Memory statistics are collected through an HTTP request SystemInfoHandler at `http://<host>:<port>/solr/admin/system`

##Prerequisites

This extension requires an AppDynamics Java Machine Agent installed and running. 

##Installation

1. Run 'mvn clean install' from the solr-monitoring-extension directory and find the SolrMonitor.zip in the 'target' directory.
2. Unzip SolrMonitor.zip and copy the "SolrMonitor" directory to `<MACHINE_AGENT_HOME>/monitors`
3. Configure the extension by referring to the below section.
4. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr for default metric-path.

##Configuration
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

1. Configure the Solr instance, Cores and Request handlers to monitor by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/SolrMonitor/`.

<pre>
        #prefix used to show up metrics in AppDynamics
metricPrefix: "Custom Metrics|Solr"

#This will create it in specific Tier. Replace <TIER_ID>
#metricPrefix: "Server|Component:<TierID>|Custom Metrics|Solr Monitor|"

servers:
   - host: "localhost"
     port: 8983
     name: "Solr Monitor 1" #optional if only one server
     # Optional Parameters. Configure if any required
     username: ""
     password: ""
     contextRoot: ""
     usessl: ""
     proxyHost: ""
     proxyPort: ""
     proxyUsername: ""
     proxyPassword: ""
   - host: "localhost"
     port: 8983
     name: "Solr Monitor 2" #optional if only one server
     # Optional Parameters. Configure if any required
#     username: ""
#     password: ""
#     contextRoot: ""
#     usessl: ""
#    proxyHost: ""
#     proxyPort: ""
#     proxyUsername: ""
#     proxyPassword: ""

# Example
# cores
# - name: "collection1"
#     pingHandler: "/admin/ping"
#     queryHandlers: ["/select", "/update"]
#   - name: "collection2"
#     pingHandler: "/admin/ping"
#     queryHandlers: ["/admin/ping"]
cores:
   - name: "gettingstarted"
     pingHandler: "/admin/ping"
     queryHandlers: ["/select", "/update"]

numberOfThreads: 5
</pre>

Specify as many cores as you want to monitor and corresponding comma separated request handlers. If none of the cores are specified, default core with empty request handlers is monitored. 
The Solr extension now includes support for multiple instances. You can specify as many servers as you want.

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
By default, the metrics will be reported under the following metric tree:
```Application Infrastructure Performance|Custom Metrics|$SERVERNAME|Solr Monitor```

This will register metrics to all tiers within the application. We strongly recommend using the tier specific metric prefix so that metrics are reported only to a specified tier. Please change the metric prefix in your config.yaml

```metricPrefix: "Server|Component:<TierID>|Custom Metrics|Solr Monitor|"```

For instructions on how to find the tier ID, please refer to the ```Metric Path``` subsection [here](https://docs.appdynamics.com/display/PRO42/Build+a+Monitoring+Extension+Using+Java).

Metrics will now be seen under the following metric tree:

```Application Infrastructure Performance|$TIER|Custom Metrics|$SERVERNAME|Solr Monitor```

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

## Troubleshooting
1. Verify Machine Agent Data: Please start the Machine Agent without the extension and make sure that it reports data. Verify that the machine agent status is UP and it is reporting Hardware Metrics.
2. config.yml: Validate the file [here](http://www.yamllint.com/) 
3. Metric Limit: Please start the machine agent with the argument -Dappdynamics.agent.maxMetrics=5000 if there is a metric limit reached error in the logs. If you don't see the expected metrics, this could be the cause.
4. Check Logs: There could be some obvious errors in the machine agent logs. Please take a look.
5. `The config cannot be null` error.
   This usually happenes when on a windows machine in monitor.xml you give config.yaml file path with linux file path separator `/`. Use Windows file path separator `\` e.g. `monitors\MQMonitor\config.yaml` .
6. Collect Debug Logs: Edit the file, <MachineAgent>/conf/logging/log4j.xml and update the level of the appender com.appdynamics to debug Let it run for 5-10 minutes and attach the logs to a support ticket
7. You may see in the logs that all metrics have a value of 0. This usually happens when your Solr instance is corrupt or offline. Please restart your Solr instance in this situation and then restart the machine agent. The 'Ping Status' is a quick way to check whether or not the current Solr instance is up. 

## Custom Dashboard
![](https://github.com/Appdynamics/solr-monitoring-extension/raw/master/SolrDashboard.png)

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/AppDynamics-eXchange/Solr-Monitoring-Extension/idi-p/6887) community.

##Support

For any questions or feature request, please contact [AppDynamics Support](mailto:help@appdynamics.com).


