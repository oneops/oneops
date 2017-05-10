/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.mockrunner.mock.web.MockServletContext;
import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.config.OneOpsConfig;

import com.oneops.util.Version;
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
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {OneOpsConfig.class,
    Version.class, MockServletContext.class})
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