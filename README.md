# AppDynamics Solr Monitoring Extension

This extension works only with the standalone machine agent.

##Use Case

Solr is a popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
This extension collects metrics from Solr search engine and uploads them to AppDynamics Metric Broswer.

Solr statistics (Core, Query, Cache) are obtained through an HTTP request to the SolrInfoMBeanHandler at http://\<host\>:\<port\>/solr/admin/mbeans. Please refer to [SolrJMX](http://wiki.apache.org/solr/SolrJmx) for details.

Memory statistics are collected through an HTTP request to http://\<host\>:\<port\>/solr/admin/system

##Installation

1. Run 'mvn clean install' from the solr-monitoring-extension directory
2. Download the file SolrMonitor.zip located in the 'target' directory into \<machineagent install dir\>/monitors/
3. Unzip the downloaded file
4. In \<machineagent install dir\>/monitors/SolrMonitor/, open monitor.xml and configure the Solr parameters.
     <pre>
     &lt;argument name="host" is-required="true" default-value="localhost" /&gt;
     &lt;argument name="port" is-required="true" default-value="8983" /&gt;
     &lt;argument name="metric-path" is-required="false" default-value="" /&gt;
     </pre>
5. Restart the Machine Agent.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr for default metric-path.


## Metrics

###Core Metrics

The following metrics are reported under Core

| Metric Name 			|
|-------------------------------|
|Number of Docs			|
|Max Docs			|
|Deleted Docs			|


###Query Statistics

The following metrics are reported under Query

| Metric Name 			|
|-------------------------------|
|Average Rate (requests per second)		|
|5 Minute Rate (requests per second)		|
|15 Minute Rate (requests per second)		|
|Average Time Per Request (milliseconds)	|
|Median Request Time (milliseconds)		|
|95th Percentile Request Time (milliseconds)	|

###Memory Statistics

The following metrics are reported under Memory/JVMMemory

| Metric Name 			|
|-------------------------------|
|Used (MB)			|
|Free (MB)			|
|Total (MB)			|

The following metrics are reported under Memory/SystemMemory

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
|HitRatio			|
|HitRatioCumulative		|
|CacheSize (Bytes)		|

The following metrics are reported under Cache/DocumentCache

| Metric Name 			|
|-------------------------------|
|HitRatio			|
|HitRatioCumulative		| 
|CacheSize (Bytes)		|

The following metrics are reported under Cache/FieldValueCache

| Metric Name 			|
|-------------------------------|
|HitRatio			| 
|HitRatioCumulative		| 
|CacheSize (Bytes)		|

The following metrics are reported under Cache/FilterCache

| Metric Name 			|
|-------------------------------|
|HitRatio			| 
|HitRatioCumulative		| 
|CacheSize (Bytes)		|

## Custom Dashboard
![](https://github.com/Appdynamics/solr-monitoring-extension/raw/master/SolrDashboard.png)

## Configuration Options
-Dcom.appdynamics.extensions.solr.useproxy=true|false
Set this option to use proxy settings specified by
-Dcom.singularity.httpclientwrapper.proxyHost
-Dcom.singularity.httpclientwrapper.proxyPort
when connecting to the Solr

##Contributing

Always feel free to fork and contribute any changes directly here on GitHub.

##Community

Find out more in the [AppSphere](http://appsphere.appdynamics.com/t5/AppDynamics-eXchange/Solr-Monitoring-Extension/idi-p/6887) community.

##Support

For any questions or feature request, please contact [AppDynamics Center of Excellence](mailto:ace-request@appdynamics.com).


