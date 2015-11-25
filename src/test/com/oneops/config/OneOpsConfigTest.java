/*
 * Copyright 2014-2015 WalmartLabs.
 *
 */
package com.oneops.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.config.OneOpsConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * OneOps spring java configuration test.
 *
 * @author Suresh G
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {OneOpsConfig.class})
public class OneOpsConfigTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private Environment env;

    @Autowired
    private ClusterBootstrap cb;

    @Autowired
    private MetricRegistry ooMetricRegistry;

    @Autowired
    private HealthCheckRegistry ooHealthCheckRegistry;

    @Value("${oo.metrics.reporter.es}")
    private boolean esReportingEnabled;

    static {
        System.setProperty("oo.metrics.reporter.es", "false");
    }

    @Test
    public void testGetClusterBootstrap() {
        assertNotNull(cb);
    }

    @Test
    public void testGetMetricRegistry() {
        assertNotNull(ooMetricRegistry);
    }

    @Test
    public void testGetHealthCheckRegistry() {
        assertNotNull(ooHealthCheckRegistry);
    }

    @Test
    public void testProperties() {
        assertEquals(esReportingEnabled, false);
        assertEquals(env.getProperty("oo.metrics.reporter.es", Boolean.class), Boolean.FALSE);
    }
}