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
import com.appdynamics.extensions.http.UrlBuilder;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.netscaler.input.Stat;
import net.minidev.json.JSONNavi;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Phaser;

/**
 * Created by aditya.jagtiani on 3/26/18.
 */
public class NetScalerMetricsCollector implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NetScalerMetricsCollector.class);
    private Stat stat;
    private MonitorConfiguration monitorConfiguration;
    private Map<String, String> server;
    private Phaser phaser;
    private MetricWriteHelper metricWriteHelper;
    private List<Metric> metrics = new ArrayList<>();
    private MetricDataParser metricDataParser;
    private String endpoint;
    private String serverName;

    public NetScalerMetricsCollector(Stat stat, MonitorConfiguration monitorConfiguration, Map<String, String> server,
                                     Phaser phaser, MetricWriteHelper metricWriteHelper) {
        this.stat = stat;
        this.monitorConfiguration = monitorConfiguration;
        this.server = server;
        this.phaser = phaser;
        this.metricWriteHelper = metricWriteHelper;
        this.metricDataParser = new MetricDataParser(monitorConfiguration);
        this.endpoint = buildUrl(server, stat.getUrl());
    }

    public void run() {
        try {
            serverName = server.get("name");
            logger.info("Currently fetching metrics from endpoint: {}", endpoint);
            JsonNode jsonData = HttpClientUtils.getResponseAsJson(monitorConfiguration.getHttpClient(), endpoint,
                    JsonNode.class);
            metrics.addAll(metricDataParser.parseNodeData(stat, jsonData, new ObjectMapper(), serverName));
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ONE),
                    monitorConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));
            if (metrics != null && metrics.size() > 0) {
                logger.debug("Printing {} metrics for stat: {}", metrics.size(), stat.getAlias());
                metricWriteHelper.transformAndPrintMetrics(metrics);
            }
        } catch (Exception ex) {
            logger.error("Error encountered while collecting metrics from endpoint: " + endpoint, ex.getMessage());
            metrics.add(new Metric("Heart Beat", String.valueOf(BigInteger.ZERO),
                    monitorConfiguration.getMetricPrefix() + "|" + serverName + "|Heart Beat"));

        } finally {
            logger.debug("Completing metric collection from endpoint: " + endpoint);
            phaser.arriveAndDeregister();
        }
    }

    private String buildUrl(Map<String, String> server, String statEndpoint) {
        return UrlBuilder.fromYmlServerConfig(server).build() + statEndpoint;
    }
}
