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
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.util.AssertUtils;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

import static com.appdynamics.extensions.netscaler.util.Constants.DEFAULT_METRIC_PREFIX;
import static com.appdynamics.extensions.netscaler.util.Constants.MONITOR_NAME;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetScalerMonitor extends ABaseMonitor {
    private static Logger logger = ExtensionsLoggerFactory.getLogger(NetScalerMonitor.class);
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
        List<Map<String, ?>> servers = getServers();
        AssertUtils.assertNotNull(servers, "The 'servers' section in config.yml is not initialised");
        AssertUtils.assertNotNull(this.getContextConfiguration().getMetricsXml(), "The metrics.xml has been not been created.");
        for (Map server : servers) {
            logger.debug("Starting the NetScaler Monitoring Task for server : " + server.get("name"));
            NetScalerMonitorTask task = new NetScalerMonitorTask(this.getContextConfiguration(), taskExecutor.getMetricWriteHelper(), server);
            taskExecutor.submit("NetScalar", task);
        }
    }

    @Override
    protected List<Map<String, ?>> getServers() {
        return (List) getContextConfiguration().getConfigYml().get("servers");
    }

    @Override
    protected void initializeMoreStuff(Map<String, String> args) {
        this.getContextConfiguration().setMetricXml(args.get("metric-file"), Stat.Stats.class);

    }
}
