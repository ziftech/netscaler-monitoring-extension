/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler;

import com.appdynamics.extensions.ABaseMonitor;
import com.appdynamics.extensions.TasksExecutionServiceProvider;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.util.AssertUtils;
import com.singularity.ee.agent.systemagent.api.exception.TaskExecutionException;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.netscaler.util.Constants.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.netscaler.util.Constants.MONITOR_NAME;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetScalerMonitor extends ABaseMonitor {
    private static Logger logger = LoggerFactory.getLogger(NetScalerMonitor.class);
    @Override
    public String getDefaultMetricPrefix() {
        return DEFAULT_METRIC_PREFIX;
    }

    @Override
    public String getMonitorName() {
        return MONITOR_NAME;
    }

    @Override
    public void doRun(TasksExecutionServiceProvider taskExecutor) {
        List<Map<String, String>> servers = (List<Map<String, String>>) configuration.getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        AssertUtils.assertNotNull(this.configuration.getMetricsXmlConfiguration(), "The metrics.xml has been not been created.");
        for (Map<String, String> server : servers) {
            logger.debug("Starting the NetScaler Monitoring Task for server : " + server.get("name"));
            NetScalerMonitorTask task = new NetScalerMonitorTask(configuration, taskExecutor.getMetricWriteHelper(), server);
            taskExecutor.submit(server.get("name"), task);
        }
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) configuration.getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args, MonitorConfiguration conf) {
        conf.setMetricsXml(args.get("metric-file"), Stat.Stats.class);

    }

    public static void main(String[] args) throws TaskExecutionException {
        ConsoleAppender ca = new ConsoleAppender();
        ca.setWriter(new OutputStreamWriter(System.out));
        ca.setLayout(new PatternLayout("%-5p [%t]: %m%n"));
        ca.setThreshold(Level
                .DEBUG);
        org.apache.log4j.Logger.getRootLogger().addAppender(ca);


    /*FileAppender fa = new FileAppender(new PatternLayout("%-5p [%t]: %m%n"), "cache.log");
    fa.setThreshold(Level.DEBUG);
    LOGGER.getRootLogger().addAppender(fa);*/


        NetScalerMonitor monitor = new NetScalerMonitor();


        Map<String, String> taskArgs = new HashMap<String, String>();
        taskArgs.put("config-file", "/Users/aditya.jagtiani/repos/appdynamics/extensions/netscaler-monitoring-extension/src/main/resources/conf/config.yml");
        taskArgs.put("metric-file", "/Users/aditya.jagtiani/repos/appdynamics/extensions/netscaler-monitoring-extension/src/main/resources/conf/metrics.xml");
        monitor.execute(taskArgs, null);
    }
}
