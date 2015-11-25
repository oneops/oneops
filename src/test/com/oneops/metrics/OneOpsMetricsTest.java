package com.oneops.metrics;

import com.codahale.metrics.*;
import com.oneops.config.OneOpsConfig;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Map;

import static com.codahale.metrics.MetricRegistry.name;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;


/**
 * OneOps metrics measuring instruments test.
 *
 * @author Suresh G
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {OneOpsConfig.class})
public class OneOpsMetricsTest extends AbstractTestNGSpringContextTests {

    public static final String METRIC_PREFIX = "oneops.test";

    @Autowired
    private MetricRegistry ooMetricRegistry;

    private int gaugeVal;

    @Test
    public void testCounter() {
        Counter c = ooMetricRegistry.counter(name(METRIC_PREFIX, "counter"));
        for (int i = 0; i < 10; i++) {
            c.inc();
        }
        for (int i = 0; i < 5; i++) {
            c.dec();
        }
        Map<String, Counter> cm = ooMetricRegistry.getCounters();
        assertTrue(cm.containsKey("oneops.test.counter"));
        assertEquals(cm.get("oneops.test.counter").getCount(), 5);
    }

    @Test
    public void testMeter() {
        Meter m = ooMetricRegistry.meter(name(METRIC_PREFIX, "meter"));
        for (int i = 0; i < 10; i++) {
            tick(1);
            m.mark();
        }
        Map<String, Meter> mm = ooMetricRegistry.getMeters();
        assertTrue(mm.containsKey("oneops.test.meter"));
        assertEquals(mm.get("oneops.test.meter").getCount(), 10);
        double rate = mm.get("oneops.test.meter").getMeanRate();
        /* Mean rate should be around 1 tick/seconds */
        assertTrue(rate > 0.9 && rate <= 1.0);
    }


    @Test
    public void testGauge() {
        gaugeVal = 0;
        ooMetricRegistry.register(name(METRIC_PREFIX, "gauge"), new Gauge<Integer>() {
            @Override
            public Integer getValue() {
                return gaugeVal++;
            }
        });

        Map<String, Gauge> gm = ooMetricRegistry.getGauges();
        assertTrue(gm.containsKey("oneops.test.gauge"));
        for (int i = 0; i < 10; i++) {
            assertEquals(gm.get("oneops.test.gauge").getValue(), i);
        }
    }

    @Test
    public void testTimer() {
        int count = 10;
        int tickSec = 1;
        Timer t = ooMetricRegistry.timer(name(METRIC_PREFIX, "timer"));
        for (int i = 0; i < count; i++) {
            Timer.Context ctx = t.time();
            tick(tickSec);
            ctx.stop();
            ctx.close();
        }
        Map<String, Timer> tm = ooMetricRegistry.getTimers();
        assertTrue(tm.containsKey("oneops.test.timer"));

        Timer tt = tm.get("oneops.test.timer");
        // 2 samples will be collected (start,end time)
        assertEquals(tt.getCount(), count * 2);
        // Max should be at-least 1 sec tick
        assertTrue(SECONDS.convert(tt.getSnapshot().getMax(), NANOSECONDS) >= 1);
    }

    /**
     * Approximately tick <b>t</b> seconds.
     *
     * @param t time in sec
     */
    private void tick(long t) {
        try {
            Thread.sleep(t * 1000);
        } catch (InterruptedException e) {
        }
    }

}