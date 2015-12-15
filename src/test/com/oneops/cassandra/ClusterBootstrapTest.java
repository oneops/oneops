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