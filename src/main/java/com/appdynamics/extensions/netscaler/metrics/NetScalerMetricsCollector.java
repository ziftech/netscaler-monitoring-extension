/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.metrics;

import com.appdynamics.extensions.MetricWriteHelper;
import com.appdynamics.extensions.conf.MonitorContext;
import com.appdynamics.extensions.http.HttpClientUtils;
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * Created by aditya.jagtiani on 3/26/18.
 */
public class NetScalerMetricsCollector implements Runnable {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(NetScalerMetricsCollector.class);
    private Stat stat;
    private MonitorContext monitorContext;
    private Map<String, String> server;
    private Phaser phaser;
    private MetricWriteHelper metricWriteHelper;
    private List<Metric> metrics = new ArrayList<>();
    private MetricDataParser metricDataParser;
    private String endpoint;
    private String serverName;
    private String metricPrefix;

    public NetScalerMetricsCollector(Stat stat, MonitorContext monitorContext, Map<String, String> server,
                                     Phaser phaser, MetricWriteHelper metricWriteHelper, String metricPrefix) {
        this.stat = stat;
        this.monitorContext = monitorContext;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.endpoint = buildUrl(server, stat.getUrl());
        this.metricPrefix = metricPrefix;
        this.metricDataParser = new MetricDataParser(metricPrefix);
    }

    public void run() {
        try {
            phaser.register();
            serverName = server.get("name");
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonData = HttpClientUtils.getResponseAsJson(monitorContext.getHttpClient(), endpoint,
                    JsonNode.class);
            metrics.addAll(metricDataParser.parseNodeData(stat, jsonData, new ObjectMapper(), serverName));
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ONE),
                    metricPrefix + "|" + serverName + "|Heart Beat"));
            if (metrics != null && metrics.size() > 0) {
                logger.debug("Printing {} metrics for stat: {}", metrics.size(), stat.getAlias());
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        } catch (Exception ex) {
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, ex.getMessage());
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ZERO),
                    metricPrefix + "|" + serverName + "|Heart Beat"));

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + statEndpoint;
    }
}
