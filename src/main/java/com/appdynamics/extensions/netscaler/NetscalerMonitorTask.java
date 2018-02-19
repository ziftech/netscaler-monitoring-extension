/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler;

import com.appdynamics.extensions.AMonitorTaskRunnable;
import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.netscaler.metrics.LoadBalancingMetricsProcessor;
import com.appdynamics.extensions.netscaler.metrics.ServiceMetricsProcessor;
import com.appdynamics.extensions.netscaler.metrics.SystemMetricsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetscalerMonitorTask implements AMonitorTaskRunnable {
    private static Logger logger = LoggerFactory.getLogger(NetscalerMonitorTask.class);
    private MonitorConfiguration monitorConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, String> server;
    private String serverURL;
    private String serverName;
    private Map<String, ?> netscalerCfg;
    private Map<String, ?> configuredMetrics;

    public NetscalerMonitorTask(MonitorConfiguration monitorConfiguration, MetricWriteHelper metricWriteHelper,
                                Map<String, String> server) {
        this.monitorConfiguration = monitorConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.netscalerCfg = monitorConfiguration.getConfigYml();
        this.server = server;
        this.serverURL = UrlBuilder.fromYmlServerConfig(server).build();
        this.serverName = server.get("name");
        this.configuredMetrics = (Map<String, ?>) netscalerCfg.get("metrics");
    }

    public void run() {
        try {
            populateAndPrintMetrics();
        } catch (Exception ex) {
            logger.error("An error was encountered during the Netscaler Monitoring Task", ex.getMessage());
        }
    }

    public void populateAndPrintMetrics() {
        CountDownLatch latch = new CountDownLatch(3);
        SystemMetricsProcessor systemMetricsProcessor = new SystemMetricsProcessor(monitorConfiguration, metricWriteHelper,
                serverURL, serverName, (List) configuredMetrics.get("System"), latch);
        monitorConfiguration.getExecutorService().submit("System Metrics", systemMetricsProcessor);

        LoadBalancingMetricsProcessor lbMetricsProcessor = new LoadBalancingMetricsProcessor(monitorConfiguration, metricWriteHelper,
                serverURL, serverName, (List) configuredMetrics.get("Load Balancing"), latch);
        monitorConfiguration.getExecutorService().submit("Load Balancing Metrics", lbMetricsProcessor);

        ServiceMetricsProcessor serviceMetricsProcessor = new ServiceMetricsProcessor(monitorConfiguration, metricWriteHelper,
                serverURL, serverName, (List) configuredMetrics.get("Service"), latch);
        monitorConfiguration.getExecutorService().submit("Service Metrics", serviceMetricsProcessor);

        try {
            latch.await();
        } catch (InterruptedException ie) {
            logger.error(ie.getMessage());
        }
    }

    public void onTaskComplete() {
    }
}
