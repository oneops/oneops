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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * OneOps spring java configuration.
 *
 * @author Suresh G
 */
@Configuration
@ComponentScan(basePackages = {
        "com.oneops.config",
        "com.oneops.metrics",
        "com.oneops.cassandra"})
@PropertySource({"classpath:oneops-config.properties"})
public class OneOpsConfig {

    private static final Logger logger = Logger.getLogger(OneOpsConfig.class);

    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        logger.info("Initializing the OneOps config....");
        logger.info("Environment: " + env);
    }

    /**
     * Initializes OneOps metrics registry
     *
     * @return {@code MetricRegistry}
     */
    @Bean
    public MetricRegistry getMetricRegistry() {
        return new MetricRegistry();
    }

    /**
     * Initializes OneOps health registry
     *
     * @return {@code HealthCheckRegistry}
     */
    @Bean
    public HealthCheckRegistry getHealthCheckRegistry() {
        return new HealthCheckRegistry();
    }

    /**
     * Property place holder config bean.
     *
     * @return a pspc bean
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer pspc() {
        logger.info("Initializing Property source placeholder config...");
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setIgnoreResourceNotFound(true);
        pspc.setIgnoreUnresolvablePlaceholders(true);
        pspc.setOrder(1);
        return pspc;
    }



}
