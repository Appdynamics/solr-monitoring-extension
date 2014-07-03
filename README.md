# AppDynamics Solr Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Solr is a popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
This extension collects metrics from Solr search engine and uploads them to AppDynamics Metric Broswer.

Solr statistics (Core, Query, Cache) are obtained through an HTTP request to the SolrInfoMBeanHandler at `http://\<host\>:\<port\>/solr/admin/mbeans`. Please refer to [SolrInfoMBeanHandler](http://wiki.apache.org/solr/SystemInformationRequestHandlers) for details.

Memory statistics are collected through an HTTP request SystemInfoHandler at `http://\<host\>:\<port\>/solr/admin/system`

##Installation

1. Run 'mvn clean install' from the solr-monitoring-extension directory
2. Download the file SolrMonitor.zip located in the 'target' directory into `<MACHINE_AGENT_HOME>/monitors`
3. Unzip the downloaded file
4. In `<MACHINE_AGENT_HOME>/monitors/SolrMonitor/`, open monitor.xml and configure the Solr parameters.
     <pre>
     &lt;argument name="host" is-required="true" default-value="localhost" /&gt;
     &lt;argument name="port" is-required="true" default-value="8983" /&gt;
			<!--  Optional Parameters -->
     &lt;argument name="proxy-host" is-required="false" default-value="" /&gt;
     &lt;argument name="proxy-port" is-required="false" default-value="" /&gt;
     &lt;argument name="context-root" is-required="false" default-value="/solr" /&gt;
     &lt;argument name="metric-prefix" is-required="false" default-value="Custom Metrics|Solr|" /&gt;
     &lt;argument name="config-file" is-required="false" default-value="monitors/SolrMonitor/config.yml"/&gt;
     </pre>
5. Configure the Solr Cores and Request handlers to monitor by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/SolrMonitor/`.
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/).
Below is the sample.
  	```
        cores:
        - name: "collection1"
        queryHandlers: ["/select", "/update"]
        - name: ""
        queryHandlers: []
        .....
        .....
    ```
Specify as many cores as you want to monitor and corresponding comma separated request handlers. If none of the cores are specified, default core with empty request handlers is monitored.
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr for default metric-path.


## Metrics
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

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).


