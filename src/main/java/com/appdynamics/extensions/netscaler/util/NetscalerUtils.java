/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.util;

import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.util.AssertUtils;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 12/5/17.
 */
public class NetscalerUtils {
    private static final Logger logger = LoggerFactory.getLogger(NetscalerUtils.class);
    public static final String API_BASE = "/nitro/v1/stat";
    public static final String SERVICE_ENDPOINT = "/service";
    public static final String SYSTEM_ENDPOINT = "/system";
    public static final String LB_ENDPOINT = "/lbvserver";
    public static final String DEFAULT_METRIC_PREFIX = "Custom Metrics|Netscaler Monitor|";
    public static final String MONITOR_NAME = "Netscaler Monitor";
    public static final String METRIC_SEPARATOR = "|";

    public static List<Metric> getMetrics(String metricPath, List<Map> metricsList, JsonNode jsonNode) {
        AssertUtils.assertNotNull(metricsList, "The metricsList passed is either null or empty");
        AssertUtils.assertNotNull(jsonNode, "The jsonNode passed is either null or empty");
        List<Metric> metricList = Lists.newArrayList();
        for (Map<String, ?> metric : metricsList) {
            String metricName = metric.entrySet().iterator().next().getKey();
            Map<String, ?> metricProperties = (Map<String, ?>) metric.entrySet().iterator().next().getValue();
            JsonNode jsonValue = jsonNode.get(metricName);
            if (jsonValue != null && jsonValue.isValueNode()) {
                Metric individualMetric;
                if (metricProperties != null) {
                    individualMetric = new Metric(metricName, jsonValue.asText(), metricPath + METRIC_SEPARATOR + metricName, metricProperties);
                } else {
                    individualMetric = new Metric(metricName, jsonValue.asText(), metricPath + METRIC_SEPARATOR + metricName);
                }
                metricList.add(individualMetric);
            } else {
                logger.debug("Value for {} does not exist", metricName);
            }
        }
        return metricList;
    }
}
