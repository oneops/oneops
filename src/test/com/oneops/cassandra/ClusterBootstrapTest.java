package com.oneops.cassandra;

import com.oneops.cassandra.ClusterBootstrap;
import com.oneops.config.OneOpsConfig;

import me.prettyprint.hector.api.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.AnnotationConfigContextLoader;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import static com.oneops.cassandra.ClusterBootstrap.*;
import static org.testng.Assert.assertNotNull;

/**
 * Cluster bootstrap test.
 *
 * @author Suresh G
 */
@ContextConfiguration(loader = AnnotationConfigContextLoader.class, classes = {OneOpsConfig.class})
public class ClusterBootstrapTest extends AbstractTestNGSpringContextTests {

    static {
        System.setProperty(CPP + ".hosts", "daq");
    }

    @Autowired
    private ClusterBootstrap cb;


    @Test
    public void testGetCluster() throws Exception {
        assertNotNull(cb);
        Cluster c = (cb.getCluster("test_cluster"));
        assertNotNull(c);
    }
}