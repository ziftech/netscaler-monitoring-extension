# AppDynamics Monitoring Extension for Citrix NetScaler

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

In order to use this extension, you do need a [Standalone JAVA Machine Agent](https://docs.appdynamics.com/display/PRO44/Java+Agent) or [SIM Agent](https://docs.appdynamics.com/display/PRO44/Server+Visibility). 
For more details on downloading these products, please visit https://download.appdynamics.com/.

The extension needs to be able to connect to the NetScaler NITRO API in order to be able to collect and send the metrics. 
To do this, you will have to either establish a remote connection in between the extension and the product, or have an agent on the same machine running the product in order for the extension to collect and send the metrics.

The NetScaler extension connects to the Citrix Netscaler appliance using the NITRO REST API. Please ensure that your system meets the 
following requirements before installing this extension: 


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
    
3. Configure the path to the config.yml & metrics.xml by editing the <task-arguments> in the monitor.xml file in the `<MACHINE_AGENT_HOME>/monitors/NetScalerMonitor/` directory. Below is the sample

     ```
     <task-arguments>
         <!-- config file-->
         <argument name="config-file" is-required="true" default-value="monitors/NetScalerMonitor/config.yml" />
         <argument name="metric-file" is-required="true" default-value="monitors/NetScalerMonitor/metrics.xml" />
          ....
     </task-arguments>
      ```
4. Restart the machine agent. 

### Metrics 
Here is a summary of the metrics published by this extension. You can add/remove metrics of your choosing by modifying the provided metrics.xml file by 
using the correct stat names as per the [Citrix documentation](https://developer-docs.citrix.com/projects/netscaler-nitro-api/en/11.1/statistics/statistics/)

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

1. Stop the running machine agent .
2. Delete all existing logs under <MachineAgent>/logs .
3. Please enable debug logging by editing the file <MachineAgent>/conf/logging/log4j.xml. Change the level value of the following <logger> elements to debug. 
   ```
   <logger name="com.singularity">
   <logger name="com.appdynamics">
     ```
4. Start the machine agent and please let it run for 10 mins. Then zip and upload all the logs in the directory <MachineAgent>/logs/*.
5. Attach the zipped <MachineAgent>/conf/* directory here.
 6. Attach the zipped <MachineAgent>/monitors/NetScalerMonitor directory here .

For any support related questions, you can also contact help@appdynamics.com.

### Contributing
Always feel free to fork and contribute any changes directly via GitHub.


### Version
Version: 1.0.0 <br />
Controller Compatibility: 3.7 or Later <br />
Product Tested On: NetScaler VPX 10.x on AWS <br />
Last updated On: 03/29/2018 <br />
List of Changes to this extension: [Change log](https://github.com/Appdynamics/netscaler-monitoring-extension/blob/netscaler-monitoring-extension-1.0.0/changelog.md)
