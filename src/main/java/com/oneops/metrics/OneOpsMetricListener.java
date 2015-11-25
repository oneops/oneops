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
