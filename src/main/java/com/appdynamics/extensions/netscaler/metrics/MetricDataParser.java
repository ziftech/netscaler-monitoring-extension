/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.metrics;

import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.netscaler.input.MetricConfig;
import com.appdynamics.extensions.netscaler.input.MetricConverter;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.appdynamics.extensions.util.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 3/26/18.
 */

class MetricDataParser {

    private static final Logger logger = LoggerFactory.getLogger(MetricDataParser.class);
    private MonitorConfiguration monitorConfiguration;
    private List<Metric> metrics = new ArrayList<>();

    MetricDataParser(MonitorConfiguration monitorConfiguration) {
        this.monitorConfiguration = monitorConfiguration;
    }

    List<Metric> parseNodeData(Stat stat, JsonNode nodes, ObjectMapper oMapper, String serverName) {
        JsonNode currentNode;
        if (nodes != null) {
            currentNode = nodes.get(stat.getFilterName());
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
                logger.debug("{} metrics are not available for server: {}. Skipping.", stat.getFilterName(), serverName);
            }
        }
        return metrics;
    }

    private Metric parseAndRetrieveMetric(MetricConfig metricConfig, Stat stat, JsonNode currentNode, ObjectMapper oMapper,
                                          String serverName) {
        Metric metric = null;
        BigDecimal metricValue;
        if (currentNode.has(metricConfig.getAttr())) {
            //TODO you shouldn't be using your own convert. The commons platform handles convert.
            if (metricConfig.hasConverter()) {
                metricValue = convertMetricValue(metricConfig, currentNode);
            } else {
                metricValue = new BigDecimal(currentNode.findValue(metricConfig.getAttr()).asText());
            }
            if (metricValue != null) {
                String prefix = StringUtils.trim(stat.getAlias(), "|");
                String name = (currentNode.has("name")) ? currentNode.get("name").asText() + "|" : "";
                Map<String, String> propertiesMap = oMapper.convertValue(metricConfig, Map.class);
                metric = new Metric(metricConfig.getAlias(), String.valueOf(metricValue),
                        monitorConfiguration.getMetricPrefix() + "|" + serverName + "|" + prefix + "|" + name
                                + metricConfig.getAlias(), propertiesMap);
                logger.info("Adding metric {} to the queue for publishing", metric.getMetricPath());
            }
        }
        return metric;
    }

    private BigDecimal convertMetricValue(MetricConfig metricConfig, JsonNode currentNode) {
        BigDecimal value = null;
        String valueToBeConverted;
        if (metricConfig.getConverters() != null) {
            valueToBeConverted = currentNode.findValue(metricConfig.getAttr()).asText();
            for (MetricConverter converter : metricConfig.getConverters()) {
                if (valueToBeConverted.equals(converter.getLabel())) {
                    logger.debug("Found metric {} for conversion", metricConfig.getAttr());
                    value = new BigDecimal(converter.getValue());
                }
            }
        }
        return value;
    }
}
