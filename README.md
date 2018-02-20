# netscaler-monitoring-extension
AppDynamics monitoring extension for Citrix NetScaler

### Use Case
Citrix NetScaler is an all-in-one web application delivery controller (ADC) that makes applications run up to five times 
faster, cuts web application ownership costs with server offloading, and makes sure that applications are always available 
with its application load balancing capabilities. 

NetScaler VPX provides the complete NetScaler web and application load balancing, secure and remote access, acceleration, 
security and offload feature set in a simple, easy-to-install virtual appliance. 

The AppDynamics monitoring extension for NetScaler provides metrics collected by the NetScaler appliance about the usage of its features and corresponding 
resources using the NITRO API. These metrics can be classified under the following groups: 

1. Core System Metrics 

2. Service Resource metrics 

3. Load Balancing metrics

### Prerequisites 
The NetScaler extension connects to the Citrix Netscaler appliance using the NITRO REST API. Please ensure that your system meets the 
following requirements before installing this extension: 

- **NetScaler Version:** The extension requires NetScaler VPX or MPX, version 10.x+. You can install NetScaler VPX on AWS using 
these [instructions](https://docs.citrix.com/en-us/netscaler/11-1/deploying-vpx/install-vpx-on-aws.html)
- In the NetScaler user interface, you must create a user account for monitoring purposes 
- An AppDynamics Java Machine Agent

### Installation 
1. To build from source, clone this repository and run 'mvn clean install'. This will produce a NetScalerMonitor-VERSION.zip in the target directory
Alternatively, download the latest release archive from [Github](https://github.com/Appdynamics/netscaler-monitoring-extension/releases)
2. Unzip the file NetScalerMonitor-[version].zip into `<MACHINE_AGENT_HOME>/monitors/`
3. In the newly created directory "NetScalerMonitor", edit the config.yml configuring the parameters (See Configuration section below)
4. Restart the machine agent
5. In the AppDynamics Metric Browser, look for: Application Infrastructure Performance|\<Tier\>|Custom Metrics|NetScaler  .

### Configuration 
1. Configure the NetScaler parameters by editing the config.yml file in `<MACHINE_AGENT_HOME>/monitors/NetScalerMonitor/`. 

   Here is a sample config.yml file
    ```
    # Extensions Troubleshooting Document: https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695
    
    # Please refer to step 4 in the Extensions Troubleshooting Document for instructions on how to obtain your Tier ID
    # Please make sure your metric prefix always ends with a '|'
    metricPrefix: "Server|Component:<Tier ID/Tier Name>|Custom Metrics|Netscaler Monitor|"
    
    # Configure your Netscaler ADX servers here
    servers:
     - name: ""
       host: ""
       port: ""
       username: ""
       password: ""
       encryptedPassword: ""
    
    #Please refer to the Password Encryption Documentation for instructions on how to configure an encryptionKey & encryptedPassword
    # https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397
    encryptionKey:
    
    numberOfThreads: 20
    
    # The metrics published by the extension. You can configure additional metrics using the Netscaler API documentation
    # https://developer-docs.citrix.com/projects/netscaler-nitro-api/en/11.1/statistics/statistics/?_ga=2.173931875.1514738593.1519078996-904321061.1516686644
    metrics:
     Service:
       - throughput:
           alias: "throughput" #Throughput in mbps
       - avgsvrttfb:
           alias: "avgsvrttfb" #Average time to first byte
       - state:
           alias: "state" #Current state of server
       - totalrequests:
           alias: "totalrequests" #Total number of requests received on the service
       - totalresponses:
           alias: "totalresponses" #Total number of responses
       - curclntconnections:
           alias: "curclntconnections" #Average time to first byte
       - cursrvrconnections:
           alias: "cursrvrconnections" #Number of connections to the server behind the virtual server
       - activetransactions:
           alias: "activetransactions" #Number of active transactions handled by the service
       - state:
           alias: "state" #Current state of the system
           convert:
             "DOWN" : 0
             "UP" : 1
             "UNKNOWN" : 2
             "OFS" : 3 #Out of service
             "TROFS" : 4 #Transition out of service"
             "TROFS_DOWN" : 5 #Down when going out of service
    
     System:
       - cpuusagepcnt:
           alias: "cpuusagepcnt" #CPU utilization in %
       - memsizemb:
           alias: "memsizemb" #Currently allocated memory in MB
       - memusagepcnt:
           alias: "memusagepcnt" #Percentage of memory utilization
    
     Load Balancing:
       - tothits:
           alias: "tothits" #Total number of hits
       - totalrequests:
           alias: "totalrequests" #Total number of requests
       - totalresponses:
           alias: "totalresponses"  #Total number of responses
       - cursrvrconnections:
           alias: "cursrvrconnections" #Number of connections to the server behind the virtual server
       - curclntconnections:
           alias: "curclntconnections" #Number of client connections
       - state:
           alias: #Current state of the system
           convert:
             "DOWN" : 0
             "UP" : 1
             "UNKNOWN" : 2
             "OFS" : 3 #Out of service
             "TROFS" : 4 #Transition out of service"
             "TROFS_DOWN" : 5 #Down when going out of service

    ```
3. Configure the path to the config.yml file by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/NetScalerMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/NetScalerMonitor/config.yml" />
          ....
     </task-arguments>
      ```
4. Restart the machine agent. 

### Metrics 
Here is a summary of the metrics published by this extension. You can add/remove metrics of your choosing by modifying the config.yml file by 
using the correct group names and stat names as per the [Citrix documentation](https://developer-docs.citrix.com/projects/netscaler-nitro-api/en/11.1/statistics/statistics/)


<table>
<tr><td><strong>Metric Class</strong></td><td><strong>Description</strong></td>

<tr>
<td>Load Balancing</td>
<td>

<table>
    <tr><td><strong>Metric</strong></td><td><strong>Description</strong></td>
    <tr><td>tothits</td><td>Total number of hits</td></tr>
    <tr><td>totalrequests</td><td>Total number of requests</td></tr>
    <tr><td>totalresponses</td><td>Total number of responses</td></tr>
    <tr><td>cursrvrconnections</td><td>Number of connections to the server behind the virtual server</td></tr>
    <tr><td>curclntconnections</td><td>Number of client connections</td></tr>
    <tr><td>state</td><td>Current state of the system. Possible values: 
    - Down: 0
    - Up: 1
    - Unknown: 2 
    - Out of Service: 3
    - Transition out of service: 4
    - Down when going out of service: 5 </td></tr>
</table>
  
</td>
</tr>

<tr><td><strong>Metric Class</strong></td><td><strong>Description</strong></td>

<tr>
<td>Service Resource</td>
<td>

<table>
    <tr><td><strong>Metric</strong></td><td><strong>Description</strong></td>
    <tr><td>throughput</td><td>Throughput in mbps</td></tr>
    <tr><td>avgsvrttfb</td><td>Average time to first byte</td></tr>
    <tr><td>totalrequests</td><td>Total number of responses</td></tr>
    <tr><td>totalresponses</td><td>Total number of responses</td></tr>
    <tr><td>cursrvrconnections</td><td>Number of connections to the server</td></tr>
    <tr><td>curclntconnections</td><td>Number of client connections</td></tr>
    <tr><td>activetransactions</td><td>Number of active transactions handled by the service</td></tr>
    <tr><td>state</td><td>Current state of the system. Possible values: 
    - Down: 0
    - Up: 1
    - Unknown: 2 
    - Out of Service: 3
    - Transition out of service: 4
    - Down when going out of service: 5 </td></tr>
</table>
  
</td>
</tr>

<tr>
<td>System</td>
<td>

<table>
    <tr><td><strong>Metric</strong></td><td><strong>Description</strong></td>
    <tr><td>cpuusagepcnt</td><td>CPU Utilization Percentage</td></tr>
    <tr><td>memsizemb</td><td>Currently allocated memory in MB</td></tr>
    <tr><td>memusagepcnt</td><td>Percentage of memory utilization</td></tr>
</table>
  
</td>
</tr>

</table>

### Credentials Encryption
Please visit [this](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-Password-Encryption-with-Extensions/ta-p/29397) page to get detailed instructions on password encryption. The steps in this document will guide you through the whole process.

### Extensions Workbench
Workbench is an inbuilt feature provided with each extension in order to assist you to fine tune the extension setup before you actually deploy it on the controller. Please review the following [document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-use-the-Extensions-WorkBench/ta-p/30130) for how to use the Extensions WorkBench

### Troubleshooting
Please follow the steps listed in the [extensions troubleshooting document](https://community.appdynamics.com/t5/Knowledge-Base/How-to-troubleshoot-missing-custom-metrics-or-extensions-metrics/ta-p/28695) in order to troubleshoot your issue. These are a set of common issues that customers might have faced during the installation of the extension. If these don't solve your issue, please follow the last step on the troubleshooting-document to contact the support team.

### Support Tickets
If after going through the Troubleshooting Document you have not been able to get your extension working, please file a ticket and add the following information.

Please provide the following in order for us to assist you better.  

Stop the running machine agent .
Delete all existing logs under <MachineAgent>/logs .
Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug. 
<logger name="com.singularity">
<logger name="com.appdynamics">
Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
Attach the zipped <MachineAgent>/conf/* directory here.
 Attach the zipped <MachineAgent>/monitors/ExtensionFolderYouAreHavingIssuesWith directory here .
For any support related questions, you can also contact help@appdynamics.com.

### Contributing
Always feel free to fork and contribute any changes directly via GitHub.

### Compatibility
This extension has been built using an instance of NetScaler VPX 12.0 53.18 within Amazon Web Services. 
Compatible with Machine Agent v4.2.x+ 

### Version
| Version | Description |
| ----- | ----- |
| 1.0.0 | Development of the extension using the 2.0 framework. |
