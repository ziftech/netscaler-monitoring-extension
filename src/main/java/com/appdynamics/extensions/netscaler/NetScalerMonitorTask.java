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
import com.appdynamics.extensions.conf.MonitorContextConfiguration;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.netscaler.metrics.NetScalerMetricsCollector;
import org.slf4j.Logger;

import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetScalerMonitorTask implements AMonitorTaskRunnable {
    private static Logger logger = ExtensionsLoggerFactory.getLogger(NetScalerMonitorTask.class);
    private MonitorContextConfiguration monitorContextConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, String> server;

    public NetScalerMonitorTask(MonitorContextConfiguration monitorContextConfiguration, MetricWriteHelper metricWriteHelper,
                                Map<String, String> server) {
        this.monitorContextConfiguration = monitorContextConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.server = server;
    }

    public void run() {
        try {
            Phaser phaser = new Phaser();
            Stat.Stats metricConfiguration = (Stat.Stats) monitorContextConfiguration.getMetricsXml();
            for (Stat stat : metricConfiguration.getStats()) {
                phaser.register();
                NetScalerMetricsCollector netScalerMetricsCollector = new NetScalerMetricsCollector(stat, monitorContextConfiguration.getContext(), server, phaser, metricWriteHelper, monitorContextConfiguration.getMetricPrefix());
                monitorContextConfiguration.getContext().getExecutorService().execute("MetricCollectorTask", netScalerMetricsCollector);
                logger.debug("Registering MetricCollectorTask phaser for {}", server.get("name"));
            }
            phaser.arriveAndAwaitAdvance();
            logger.info("Completed the NetScaler Metric Monitoring task");

        } catch (Exception ex) {
            logger.error("An error was encountered during the NetScaler Monitoring Task for server : " + server.get("name"), ex.getMessage());
        }
    }

    public void onTaskComplete() {
        logger.info("Completed the NetScaler Monitoring Task for server {}", server.get("name"));
    }
}
