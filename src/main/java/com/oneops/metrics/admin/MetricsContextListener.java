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
package com.oneops.metrics.admin;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlet.InstrumentedFilter;
import com.codahale.metrics.servlets.HealthCheckServlet;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A {@link javax.servlet.ServletContextListener} to programmatically inject
 * OneOps metrics and health registries to use inside the servlets. The contract
 * is to have an attribute set for each registries in the servlet context.
 *
 * @author Suresh G
 * @see <a href="http://metrics.codahale.com/manual/servlets/">Java Metrics</a>
 */
public class MetricsContextListener implements ServletContextListener {

    private static final Logger logger = Logger.getLogger(MetricsContextListener.class);

    // As per the java metrics convention.
    public static final String METRICS_REGISTRY = MetricsServlet.class.getCanonicalName() + ".registry";
    public static final String HEALTH_CHECK_REGISTRY = HealthCheckServlet.class.getCanonicalName() + ".registry";
    public static final String FILTER_REGISTRY = InstrumentedFilter.class.getName() + ".registry";
    public static final String RATE_UNIT = MetricsServlet.class.getCanonicalName() + ".rateUnit";
    public static final String DURATION_UNIT = MetricsServlet.class.getCanonicalName() + ".durationUnit";
    public static final String ALLOWED_ORIGIN = MetricsServlet.class.getCanonicalName() + ".allowedOrigin";
    public static final String HEALTH_CHECK_EXECUTOR = HealthCheckServlet.class.getCanonicalName() + ".executor";

    @Autowired
    private MetricRegistry ooMetricsRegistry;
    @Autowired
    private HealthCheckRegistry ooHealthRegistry;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initializing the Metrics Servlet Context Listener.");
        final ServletContext context = sce.getServletContext();
        // Autowire this context listener
        WebApplicationContextUtils.
                getWebApplicationContext(context).
                getAutowireCapableBeanFactory().
                autowireBean(this);
        context.setAttribute(METRICS_REGISTRY, ooMetricsRegistry);
        context.setAttribute(FILTER_REGISTRY, ooMetricsRegistry);
        context.setAttribute(HEALTH_CHECK_REGISTRY, ooHealthRegistry);
        context.setAttribute(RATE_UNIT, getRateUnit());
        context.setAttribute(DURATION_UNIT, getDurationUnit());
        context.setAttribute(ALLOWED_ORIGIN, getAllowedOrigin());
        context.setAttribute(HEALTH_CHECK_EXECUTOR, getExecutorService());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.warn("Destroying the Metrics Servlet Context Listener.");
    }

    /**
     * Returns the {@link java.util.concurrent.TimeUnit} to which rates
     * should be converted, or {@code null} if the default should be used.
     */
    protected TimeUnit getRateUnit() {
        // use the default
        return null;
    }

    /**
     * Returns the {@link TimeUnit} to which durations should be converted,
     * or {@code null} if the default should be used.
     */
    protected TimeUnit getDurationUnit() {
        // use the default
        return null;
    }

    /**
     * Returns the {@code Access-Control-Allow-Origin} header value, if any.
     */
    protected String getAllowedOrigin() {
        // use the default
        return null;
    }

    /**
     * Returns the {@link java.util.concurrent.ExecutorService} to inject into
     * the servlet context, or {@code null} if the health checks should be
     * run in the servlet worker thread.
     */
    protected ExecutorService getExecutorService() {
        // don't use a thread pool by default
        return null;
    }
}
