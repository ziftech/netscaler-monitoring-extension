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
import com.appdynamics.extensions.metrics.Metric;
import com.appdynamics.extensions.yml.YmlReader;
import com.google.common.collect.Lists;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.mockito.Mockito.*;

/**
 * Created by aditya.jagtiani on 12/6/17.
 */
public class ServiceMetricsProcessorTest {
    private MonitorConfiguration configuration = mock(MonitorConfiguration.class);
    private CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
    private CloseableHttpResponse httpResponse = mock(CloseableHttpResponse.class);
    private MetricWriteHelper metricWriteHelper = mock(MetricWriteHelper.class);
    private Map<String, ?> conf;
    private BasicHttpEntity entity;
    private StatusLine statusLine = mock(StatusLine.class);

    @Before
    public void setup() throws IOException {
        conf = YmlReader.readFromFile(new File
                ("src/test/resources/conf/config.yml"));
        entity = new BasicHttpEntity();
        entity.setContent(new FileInputStream("src/test/resources/json/service.json"));
    }

    @Test
    public void getServiceMetricsTest() throws IOException {
        ArgumentCaptor<List> captor = ArgumentCaptor.forClass(List.class);
        CountDownLatch latch = new CountDownLatch(1);
        when(configuration.getHttpClient()).thenReturn(httpClient);
        when(configuration.getMetricPrefix()).thenReturn("Metric Prefix|");
        when(httpClient.execute(any(HttpGet.class))).thenReturn(httpResponse);
        when(statusLine.getStatusCode()).thenReturn(200);
        when(httpResponse.getStatusLine()).thenReturn(statusLine);
        when(httpResponse.getEntity()).thenReturn(entity);

        Map allMetrics = (Map) conf.get("metrics");
        List<Map> serviceMetricsFromCfg = (List) allMetrics.get("Service");
        ServiceMetricsProcessor serviceMetricsProcessor = new ServiceMetricsProcessor(configuration, metricWriteHelper,
                "serverURL", "server1", serviceMetricsFromCfg, latch);
        serviceMetricsProcessor.run();
        verify(metricWriteHelper, times(1)).transformAndPrintMetrics(captor.capture());

        List<Metric> resultList = captor.getValue();
        for (Metric metric : resultList) {
            Assert.assertTrue(metricNamesFromAPI().contains(metric.getMetricName()));
        }
    }

    private List<String> metricNamesFromAPI() {
        List<String> serviceMetricsFromAPI = Lists.newArrayList();
        serviceMetricsFromAPI.add("avgsvrttfb");
        serviceMetricsFromAPI.add("responsesrate");
        serviceMetricsFromAPI.add("state");
        serviceMetricsFromAPI.add("curload");
        serviceMetricsFromAPI.add("curclntconnections");
        serviceMetricsFromAPI.add("cursrvrconnections");
        serviceMetricsFromAPI.add("activetransactions");
        return serviceMetricsFromAPI;
    }
}
