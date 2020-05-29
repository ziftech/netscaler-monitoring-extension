/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.metrics;

import com.appdynamics.extensions.logging.ExtensionsLoggerFactory;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.netscaler.input.MetricConfig;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.util.StringUtils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 3/26/18.
 */

class MetricDataParser {

    private static final Logger logger = ExtensionsLoggerFactory.getLogger(MetricDataParser.class);
    private String metricPrefix;
    private List<Metric> metrics = new ArrayList<>();

    MetricDataParser(String metricPrefix) {
        this.metricPrefix = metricPrefix;
    }

    List<Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper oMapper, String serverName) {
        JsonNode currentNode;
        if (nodes != null) {
            currentNode = nodes.get(stat.getRootElement());
            if (currentNode != null) {
                if (!currentNode.isArray()) {
                    for (MetricConfig metricConfig : stat.getMetricConfig()) {
                        metrics.add(parseAndRetrieveMetric(metricConfig, stat, currentNode, oMapper, serverName));
                    }
                } else {
                    for (JsonNode node : currentNode) {
                        for (MetricConfig metricConfig : stat.getMetricConfig()) {
                            metrics.add(parseAndRetrieveMetric(metricConfig, stat, node, oMapper, serverName));
                        }
                    }
                }

            } else {
                logger.debug("{} metrics are not available for server: {}. Skipping.", stat.getRootElement(), serverName);
            }
        }
        return metrics;
    }

    private Metric parseAndRetrieveMetric(MetricConfig metricConfig, Stat stat, JsonNode currentNode, ObjectMapper oMapper,
                                          String serverName) {
        Metric metric = null;
        String metricValue;
        if (currentNode.has(metricConfig.getAttr())) {
            metricValue = currentNode.findValue(metricConfig.getAttr()).asText();
            if (metricValue != null) {
                String prefix = StringUtils.trim(stat.getAlias(), "|");
                String name = (currentNode.has("name")) ? currentNode.get("name").asText() + "|" : "";
                Map<String, String> propertiesMap = oMapper.convertValue(metricConfig, Map.class);
                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue),
                        metricPrefix + "|" + serverName + "|" + prefix + "|" + name
                                + metricConfig.getAlias(), propertiesMap);
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());
            }
        }
        return metric;
    }
}
