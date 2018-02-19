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
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.netscaler.util.NetscalerUtils.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.netscaler.util.NetscalerUtils.MONITOR_NAME;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetscalerMonitor extends ABaseMonitor {
    private static Logger logger = LoggerFactory.getLogger(NetscalerMonitor.class);

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
        for (Map<String, String> server : servers) {
            logger.debug("Starting the Netscaler Monitoring Task for server : " + server.get("name"));
            NetscalerMonitorTask task = new NetscalerMonitorTask(configuration, taskExecutor.getMetricWriteHelper(), server);
            taskExecutor.submit(server.get("name"), task);
        }
    }

    @Override
    protected int getTaskCount() {
        List<Map<String, String>> servers = (List<Map<String, String>>) configuration.getConfigYml().get("servers");
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        return servers.size();
    }
}
