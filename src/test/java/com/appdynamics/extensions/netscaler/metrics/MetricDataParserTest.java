/*
 * Copyright 2018. AppDynamics LLC and its affiliates.
 * All Rights Reserved.
 * This is unpublished proprietary source code of AppDynamics LLC and its affiliates.
 * The copyright notice above does not evidence any actual or intended publication of such source code.
 *
 */

package com.appdynamics.extensions.netscaler.metrics;

import com.appdynamics.extensions.AMonitorJob;
import com.appdynamics.extensions.conf.MonitorConfiguration;
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.netscaler.input.Stat;
import com.google.common.collect.Maps;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

/**
 * Created by aditya.jagtiani on 3/28/18.
 */
public class MetricDataParserTest {

    private MonitorConfiguration monitorConfiguration = new MonitorConfiguration("NetScaler",
            "Custom Metrics|NetScaler|", Mockito.mock(AMonitorJob.class));

    @Test
    public void parseNodeDataTest_SystemMetrics() throws Exception {
        monitorConfiguration.setMetricsXml("src/test/resources/conf/system-metrics.xml", Stat.Stats.class);
        Stat.Stats metricConfiguration = (Stat.Stats) monitorConfiguration.getMetricsXmlConfiguration();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("src/test/resources/json/system.json"), JsonNode.class);
        Stat stat = metricConfiguration.getStats()[0];
        Map<String, String> expectedSystemMetrics = getExpectedSystemMetrics();
        MetricDataParser metricDataParser = new MetricDataParser(monitorConfiguration);
        List<Metric> result = metricDataParser.parseNodeData(stat, node, new ObjectMapper(), "NetScaler Instance 1");
        Assert.assertTrue(result.size() == expectedSystemMetrics.size());
        for (Metric metric : result) {
            Assert.assertTrue(expectedSystemMetrics.containsKey(metric.getMetricPath()));
            Assert.assertTrue(expectedSystemMetrics.get(metric.getMetricPath())
                    .equals(metric.getMetricValue()));
        }
    }

    @Test
    public void parseNodeDataTest_LoadBalancingMetrics() throws Exception {
        monitorConfiguration.setMetricsXml("src/test/resources/conf/loadbalancing-metrics.xml", Stat.Stats.class);
        Stat.Stats metricConfiguration = (Stat.Stats) monitorConfiguration.getMetricsXmlConfiguration();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("src/test/resources/json/loadbalancing.json"), JsonNode.class);
        Stat stat = metricConfiguration.getStats()[0];
        Map<String, String> expectedLoadBalancingMetrics = getExpectedLoadBalancingMetrics();
        MetricDataParser metricDataParser = new MetricDataParser(monitorConfiguration);
        List<Metric> result = metricDataParser.parseNodeData(stat, node, new ObjectMapper(), "NetScaler Instance 1");
        Assert.assertTrue(result.size() == expectedLoadBalancingMetrics.size());
        for (Metric metric : result) {
            Assert.assertTrue(expectedLoadBalancingMetrics.containsKey(metric.getMetricPath()));
            Assert.assertTrue(expectedLoadBalancingMetrics.get(metric.getMetricPath())
                    .equals(metric.getMetricValue()));
        }
    }

    @Test
    public void parseNodeDataTest_ServiceMetrics() throws Exception {
        monitorConfiguration.setMetricsXml("src/test/resources/conf/service-metrics.xml", Stat.Stats.class);
        Stat.Stats metricConfiguration = (Stat.Stats) monitorConfiguration.getMetricsXmlConfiguration();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readValue(new FileInputStream("src/test/resources/json/service.json"), JsonNode.class);
        Stat stat = metricConfiguration.getStats()[0];
        Map<String, String> expectedServiceMetrics = getExpectedServiceMetrics();
        MetricDataParser metricDataParser = new MetricDataParser(monitorConfiguration);
        List<Metric> result = metricDataParser.parseNodeData(stat, node, new ObjectMapper(), "NetScaler Instance 1");
        Assert.assertTrue(result.size() == expectedServiceMetrics.size());
        for (Metric metric : result) {
            Assert.assertTrue(expectedServiceMetrics.containsKey(metric.getMetricPath()));
            Assert.assertTrue(expectedServiceMetrics.get(metric.getMetricPath())
                    .equals(metric.getMetricValue()));
        }
    }


    private Map<String, String> getExpectedSystemMetrics() {
        Map<String, String> expectedSystemMetrics = Maps.newHashMap();
        expectedSystemMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|System Metrics|CPU utilization %", "5");
        expectedSystemMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|System Metrics|Allocated Memory (MB)", "5");
        expectedSystemMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|System Metrics|Memory Utilization %", "5.37227");
        expectedSystemMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|System Metrics|CPU Count", "5");
        expectedSystemMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|System Metrics|Memory used (MB)", "580");
        return expectedSystemMetrics;
    }

    private Map<String, String> getExpectedLoadBalancingMetrics() {
        Map<String, String> expectedLBMetrics = Maps.newHashMap();
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|Hits", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|Requests", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|Responses", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|Server Connections", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|Client Connections", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|Hits", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|Requests", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|Responses", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|Server Connections", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|Client Connections", "15");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 1|State", "UP");
        expectedLBMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Load Balancing Server Metrics|LB Server 2|State", "UP");

        return expectedLBMetrics;
    }

    private Map<String, String> getExpectedServiceMetrics() {
        Map<String, String> expectedServiceMetrics = Maps.newHashMap();
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Throughput (MBPS)", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Average time to first byte", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Requests", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Responses", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Server Connections", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Client Connections", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|Active Transactions", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 1|State", "UNKNOWN");

        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Throughput (MBPS)", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Average time to first byte", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Requests", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Responses", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Server Connections", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Client Connections", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|Active Transactions", "20");
        expectedServiceMetrics.put(monitorConfiguration.getMetricPrefix() + "|NetScaler Instance 1|Service Metrics|Service 2|State", "UNKNOWN");
        return expectedServiceMetrics;
    }
}
