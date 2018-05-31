# AppDynamics Monitoring Extension for use with Solr

## Use Case

Solr is a popular open source enterprise search platform from the Apache Lucene project.
Its major features include powerful full-text search, hit highlighting, faceted search, near real-time indexing, dynamic clustering, database integration, rich document (e.g., Word, PDF) handling, and geospatial search.
This extension collects metrics from the Solr search engine and uploads them to the AppDynamics Metric Broswer.

Solr statistics (Core, Query, Cache) are obtained through an HTTP request to the SolrInfoMBeanHandler at `http://<host>:<port>/solr/admin/mbeans`. Please refer to [SolrInfoMBeanHandler](http://wiki.apache.org/solr/SystemInformationRequestHandlers) for details.

Memory statistics are collected through an HTTP request SystemInfoHandler at `http://<host>:<port>/solr/admin/system`

## Prerequisites

In order to use this extension, you do need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Standalone+Machine+Agents) or [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility).  For more details on downloading these products, please  visit [here](https://download.appdynamics.com/).

The extension needs to be able to connect to Solr in order to collect and send metrics. To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.


## Installation

1. Download and unzip the RabbitMQMonitor.zip to the "<MachineAgent_Dir>/monitors" directory
2. Edit the file config.yml as described below in Configuration Section, located in    <MachineAgent_Dir>/monitors/SolrMonitor and update the Solr server(s) details.
3. All metrics to be reported are configured in metrics.xml. Users can remove entries from metrics.xml to stop the metric from reporting.
4. Restart the Machine Agent

Please place the extension in the **"monitors"** directory of your **Machine Agent** installation directory. Do not place the extension in the **"extensions"** directory of your **Machine Agent** installation directory.

In the AppDynamics Metric Browser, look for: Application Infrastructure Performance  | \<Tier\> | Custom Metrics | Solr for default metric-path.



## Configuration
Note : Please make sure to not use tab (\t) while editing yaml files. You may want to validate the yaml file using a [yaml validator](http://yamllint.com/)

  1. Configure the "tier" under which the metrics need to be reported. This can be done by changing the value of `<TIER ID>` in
     metricPrefix: "Server|Component:`<TIER ID>`|Custom Metrics|Solr".
     For example,
     ```
     metricPrefix: "Server|Component:438|Custom Metrics|RabbitMQ"
     ```
  2. Configure the Solr instances by specifying the name(required), host(required), port(required) and collectionName(required) of the Solr instance, and rest of the fields (only if authentication enabled),
     encryptedPassword(only if password encryption required). You can configure multiple instances as follows to report metrics
     For example,
     
     ```
     servers:
        # mandatory parameters
       - host: "localhost"
         port: 8983
         name: "Server 1"
         collectionName : "gettingStarted"
    
    
       - host: "localhost"
         port: 7574
         name: "Server 2"
         collectionName : "techproducts"

     ```
     3. Configure the encyptionKey for encryptionPasswords(only if password encryption required).
        For example,
     ```
        #Encryption key for Encrypted password.
        encryptionKey: "axcdde43535hdhdgfiniyy576"
     ```
     4. Configure the numberOfThreads
        For example,
        If number of servers that need to be monitored is 3, then number of threads required is 5 * 3 = 15
     ```
        numberOfThreads: 15
     ```  



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
By default, the metrics will be reported under the following metric tree:
```Application Infrastructure Performance|Custom Metrics|$SERVERNAME|Solr Monitor```

This will register metrics to all tiers within the application. We strongly recommend using the tier specific metric prefix so that metrics are reported only to a specified tier. Please change the metric prefix in your config.yaml

```metricPrefix: "Server|Component:<TierID>|Custom Metrics|Solr Monitor|"```

For instructions on how to find the tier ID, please refer to the ```Metric Path``` subsection [here](https://docs.appdynamics.com/display/PRO44/Build+a+Monitoring+Extension+Using+Java).

Metrics will now be seen under the following metric tree:

```Application Infrastructure Performance|$TIER|Custom Metrics|$SERVERNAME|Solr Monitor```

The following metrics are available for each core under Cores
#### Core Metrics

The following metrics are reported under CORE

| Metric Name 			|
|-------------------------------|
|Number of Docs			|
|Max Docs				|
|Deleted Docs			|


#### Query Statistics

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

#### Memory Statistics

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

#### Cache Statistics

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


#### Custom Dashboards
![](https://github.com/Appdynamics/solr-monitoring-extension/raw/master/SolrDashboard.png)


### Credentials Encryption

Please visit [this page](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

### Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following document on [How to use the Extensions WorkBench](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130)

### Troubleshooting
1. Please ensure the RabbitMQ Management Plugin is enabled. Please check "" section of [this page](http://www.rabbitmq.com/management.html) for more details.
2. Please follow the steps listed in this [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the [troubleshooting-document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) to contact the support team.

### Support Tickets
If after going through the [Troubleshooting Document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.

    1. Stop the running machine agent.
    2. Delete all existing logs under <MachineAgent>/logs.
    3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug.
        <logger name="com.singularity">
        <logger name="com.appdynamics">
    4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
    5. Attach the zipped <MachineAgent>/conf/* directory here.
    6. Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here.

For any support related questions, you can also contact help@appdynamics.com.



### Contributing

Always feel free to fork and contribute any changes directly here on [GitHub](https://github.com/Appdynamics/rabbitmq-monitoring-extension/).

### Version
|          Name            |  Version   |
|--------------------------|------------|
|Extension Version         |1.2.0       |
|Controller Compatibility  |3.7 or Later|
|Product Tested On         |3.2.0+      |
|Last Update               |05/21/2018 |
