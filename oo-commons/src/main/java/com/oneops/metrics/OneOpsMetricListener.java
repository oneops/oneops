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

import com.codahale.metrics.*;
import org.apache.log4j.Logger;

/**
 * OneOps metrics registry listener.
 * <p/>
 * Created by Suresh G
 */
public class OneOpsMetricListener implements MetricRegistryListener {

    private static final Logger logger = Logger.getLogger(OneOpsMetricListener.class);

    @Override
    public void onGaugeAdded(String name, Gauge<?> gauge) {
        logger.info("Metric Gauge added: " + name);
    }

    @Override
    public void onGaugeRemoved(String name) {
        logger.warn("Metric Gauge removed: " + name);
    }

    @Override
    public void onCounterAdded(String name, Counter counter) {
        logger.info("Metric Counter added: " + name);
    }

    @Override
    public void onCounterRemoved(String name) {
        logger.warn("Metric Counter removed: " + name);
    }

    @Override
    public void onHistogramAdded(String name, Histogram histogram) {
        logger.info("Metric Histogram added: " + name);
    }

    @Override
    public void onHistogramRemoved(String name) {
        logger.warn("Metric Histogram removed: " + name);
    }

    @Override
    public void onMeterAdded(String name, Meter meter) {
        logger.info("Metric Meter added: " + name);
    }

    @Override
    public void onMeterRemoved(String name) {
        logger.warn("Metric Meter removed: " + name);
    }

    @Override
    public void onTimerAdded(String name, Timer timer) {
        logger.info("Metric Timer added: " + name);
    }

    @Override
    public void onTimerRemoved(String name) {
        logger.warn("Metric Timer removed: " + name);
    }
}
