/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.metrics.Metric;
import com.google.common.collect.Lists;
import org.apache.http.impl.client.CloseableHttpClient;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static com.appdynamics.extensions.netscaler.util.NetscalerUtils.*;

/**
 * Created by aditya.jagtiani on 12/5/17.
 */

public class SystemMetricsProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SystemMetricsProcessor.class);
    private MonitorConfiguration monitorConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private String serverURL;
    private String serverName;
    private List<Map> systemMetricsFromCfg;
    private CountDownLatch countDownLatch;
    private CloseableHttpClient httpClient;

    public SystemMetricsProcessor(MonitorConfiguration monitorConfiguration, MetricWriteHelper metricWriteHelper,
                                  String serverURL, String serverName, List<Map> systemMetricsFromCfg,
                                  CountDownLatch countDownLatch) {
        this.monitorConfiguration = monitorConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.serverURL = serverURL;
        this.serverName = serverName;
        this.systemMetricsFromCfg = systemMetricsFromCfg;
        this.countDownLatch = countDownLatch;
        this.httpClient = this.monitorConfiguration.getHttpClient();
    }

    public void run() {
        try {
            List<Metric> systemMetrics = getSystemMetrics();
            metricWriteHelper.transformAndPrintMetrics(systemMetrics);
        } catch (Exception ex) {
            logger.error("Error encountered while fetching System metrics", ex.getMessage());
        } finally {
            countDownLatch.countDown();
        }
    }

    private List<Metric> getSystemMetrics() {
        List<Metric> systemMetrics = Lists.newArrayList();
        JsonNode systemNode = HttpClientUtils.getResponseAsJson(httpClient,
                serverURL + API_BASE + SYSTEM_ENDPOINT, JsonNode.class).get("system");
        if (systemNode != null) {
            systemMetrics = getMetrics(monitorConfiguration.getMetricPrefix() + serverName +
                    METRIC_SEPARATOR + "System", systemMetricsFromCfg, systemNode);
        } else {
            logger.debug("No system metrics were retrieved from the Netscaler Nitro API.");
        }
        return systemMetrics;
    }
}
