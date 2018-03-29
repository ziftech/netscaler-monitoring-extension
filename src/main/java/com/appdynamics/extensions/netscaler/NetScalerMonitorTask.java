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
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.netscaler.metrics.NetScalerMetricsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * Created by aditya.jagtiani on 11/29/17.
 */
public class NetScalerMonitorTask implements AMonitorTaskRunnable {
    private static Logger logger = LoggerFactory.getLogger(NetScalerMonitorTask.class);
    private MonitorConfiguration monitorConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private Map<String, String> server;

    public NetScalerMonitorTask(MonitorConfiguration monitorConfiguration, MetricWriteHelper metricWriteHelper,
                                Map<String, String> server) {
        this.monitorConfiguration = monitorConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.server = server;
    }

    public void run() {
        try {
            Phaser phaser = new Phaser();
            Stat.Stats metricConfiguration = (Stat.Stats) monitorConfiguration.getMetricsXmlConfiguration();
            for (Stat stat : metricConfiguration.getStats()) {
                phaser.register();
                NetScalerMetricsCollector netScalerMetricsCollector = new NetScalerMetricsCollector(stat, monitorConfiguration, server, phaser, metricWriteHelper);
                monitorConfiguration.getExecutorService().execute("MetricCollectorTask", netScalerMetricsCollector);
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
