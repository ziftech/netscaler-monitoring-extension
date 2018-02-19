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

public class ServiceMetricsProcessor implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(SystemMetricsProcessor.class);
    private MonitorConfiguration monitorConfiguration;
    private MetricWriteHelper metricWriteHelper;
    private String serverURL;
    private String serverName;
    private List<Map> serviceMetricsFromCfg;
    private CountDownLatch countDownLatch;
    private CloseableHttpClient httpClient;

    public ServiceMetricsProcessor(MonitorConfiguration monitorConfiguration, MetricWriteHelper metricWriteHelper,
                                   String serverURL, String serverName, List<Map> serviceMetricsFromCfg,
                                   CountDownLatch countDownLatch) {
        this.monitorConfiguration = monitorConfiguration;
        this.metricWriteHelper = metricWriteHelper;
        this.serverURL = serverURL;
        this.serverName = serverName;
        this.serviceMetricsFromCfg = serviceMetricsFromCfg;
        this.countDownLatch = countDownLatch;
        this.httpClient = this.monitorConfiguration.getHttpClient();
    }

    public void run() {
        try {
            List<Metric> systemMetrics = getServiceMetrics();
            metricWriteHelper.transformAndPrintMetrics(systemMetrics);
        } catch (Exception ex) {
            logger.error("Error encountered while fetching Service metrics", ex.getMessage());
        } finally {
            countDownLatch.countDown();
        }
    }

    private List<Metric> getServiceMetrics() {
        List<Metric> serviceMetrics = Lists.newArrayList();
        JsonNode services = HttpClientUtils.getResponseAsJson(httpClient,
                serverURL + API_BASE + SERVICE_ENDPOINT, JsonNode.class).get("service");
        if (services != null) {
            for (JsonNode service : services) {
                String name = service.get("name").asText();
                logger.debug("Currently processing metrics for Service : " + name);
                serviceMetrics.addAll(getMetrics(monitorConfiguration.getMetricPrefix() + serverName +
                        METRIC_SEPARATOR + "Services" + METRIC_SEPARATOR +
                        name, serviceMetricsFromCfg, services));
            }
        } else {
            logger.debug("No Service metrics were retrieved from the Netscaler Nitro API.");
        }
        return serviceMetrics;
    }
}
