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
package com.oneops.metrics;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jvm.*;
import com.oneops.metrics.es.ElasticsearchReporter;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * OneOps metrics service is responsible for bootstrapping OneOps metrics
 * registries and reporters for different subsystems.
 *
 * @author Suresh G
 */
@Component
public class OneOpsMetrics {

    private static final Logger logger = Logger.getLogger(OneOpsMetrics.class);

    // Metrics property prefix
    public static final String MPP = "oo.metrics";
    // Subsystem metric prefixes.
    public static final String ANTENNA = MPP + ".antenna";
    public static final String INDUCTOR = MPP + ".inductor";
    public static final String OPAMP = MPP + ".opamp";
    public static final String SENSOR = MPP + ".sensor";
    public static final String CMS = MPP + ".cms";

    @Autowired
    private Environment env;

    @Autowired
    private MetricRegistry ooMetricsRegistry;
    @Autowired
    private HealthCheckRegistry ooHealthRegistry;

    private JmxReporter jmxReporter;
    private ElasticsearchReporter esReporter;
    private ConsoleReporter consoleReporter;

    /**
     * Initializes all the metrics reporters, based on it's property configuration.
     */
    @PostConstruct
    private void init() {
        logger.info("Initializing OneOps metrics system...");
        addMetricsListener();
        addJvmMetrics();
        addMetricsReporters();
        Set<String> metrics = ooMetricsRegistry.getNames();
        logger.info("Start collecting " + metrics.size() + " metrics, " + metrics + "  for OneOps system.");
        Set<String> healthChecks = ooHealthRegistry.getNames();
        logger.info("OneOps health checks (" + healthChecks.size() + "): " + healthChecks);
    }

    /**
     * Add metrics add/remove listener.
     */
    private void addMetricsListener() {
        if (getB("listener.enabled", false)) {
            ooMetricsRegistry.addListener(new OneOpsMetricListener());
        }
    }

    /**
     * Add metrics JVM gauges.
     */
    private void addJvmMetrics() {
        if (getB("jvm.gcstats", false)) {
            ooMetricsRegistry.registerAll(new GarbageCollectorMetricSet());
        }
        if (getB("jvm.memory", false)) {
            ooMetricsRegistry.registerAll(new MemoryUsageGaugeSet());
        }
        if (getB("jvm.threadstate", false)) {
            ooMetricsRegistry.registerAll(new ThreadStatesGaugeSet());
        }
        if (getB("jvm.filedescriptor", false)) {
            ooMetricsRegistry.register("openfd.ratio", new FileDescriptorRatioGauge());
        }
    }

    /**
     * Add metrics reporters based on the configuration.
     */
    private void addMetricsReporters() {
        if (getB("reporter.es", false)) {
            try {
                logger.info("OneOps metrics elastic search reporting is enabled!");
                esReporter = ElasticsearchReporter.forRegistry(ooMetricsRegistry).build();
                esReporter.start(getI("reporter.timeout", 60), TimeUnit.SECONDS);
            } catch (IOException e) {
                logger.error("Can't start elastic search reporting.", e);
            }
        } else {
            logger.warn("OneOps metrics elastic search reporting is disabled!");
        }

        if (getB("reporter.jmx", true)) {
            logger.info("OneOps metrics JMX reporting is enabled!");
            jmxReporter = JmxReporter.forRegistry(ooMetricsRegistry).build();
            jmxReporter.start();
        } else {
            logger.warn("OneOps metrics JMX reporting is disabled!");
        }

        if (getB("reporter.console", false)) {
            consoleReporter = ConsoleReporter.forRegistry(ooMetricsRegistry).build();
            consoleReporter.start(getI("reporter.timeout", 30), TimeUnit.SECONDS);
        } else {
            logger.warn("OneOps metrics console reporting is disabled!");
        }
    }

    /**
     * Boolean property accessor
     *
     * @param prop metrics property suffix
     * @param def  default value
     * @return boolean property value
     */
    private boolean getB(String prop, boolean def) {
        return env.getProperty(MPP + "." + prop, Boolean.class, def);
    }

    /**
     * Int property accessor
     *
     * @param prop metrics property suffix
     * @param def  default value
     * @return int property value
     */
    private int getI(String prop, int def) {
        return env.getProperty(MPP + "." + prop, Integer.class, def);
    }

    public MetricRegistry getOoMetricsRegistry() {
        return ooMetricsRegistry;
    }

    public HealthCheckRegistry getOoHealthRegistry() {
        return ooHealthRegistry;
    }

    public JmxReporter getJmxReporter() {
        return jmxReporter;
    }

    public ElasticsearchReporter getEsReporter() {
        return esReporter;
    }

    public ConsoleReporter getConsoleReporter() {
        return consoleReporter;
    }
}
