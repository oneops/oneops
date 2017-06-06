package com.oneops.cms.dj.service;

import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.domain.CmsDeployment;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;


/**
 * Test for CmsDpmtProcessorTest; 
 * 
 */
public class CmsDpmtProcessorTest {

    private DJDpmtMapper dpmtMapper = mock(DJDpmtMapper.class);
    private String nsPath = "/test/";


    @Test
    public void testOpenDeploymentsWhenInActive() throws Exception {
        //active
        CmsDeployment openDeployments = getOpenDeployment("active");
        assertTrue(openDeployments != null);
    }


    @Test
    public void testOpenDeploymentsWhenInFailed() throws Exception {
        //failed
        CmsDeployment openDeployments = getOpenDeployment("failed");
        assertTrue(openDeployments != null);
    }

    @Test
    public void testOpenDeploymentsWhenInPaused() throws Exception {
        //paused
        CmsDeployment openDeployments = getOpenDeployment("paused");
        assertTrue(openDeployments != null);
    }

    @Test
    public void testOpenDeploymentsWhenInUnknownState() throws Exception {
        //unknown 
        CmsDeployment openDeployments = getOpenDeployment("unknown");
        //should return null when unknown state
        assertTrue(openDeployments == null);
    }


    /**
     * Get deployments which are in one of the state(active|failed|canceled) , should return null
     *
     * @param state the state of deployment which needs to be looked
     * @return deployment of the expected sate 
     * @throws Exception 
     */
    public CmsDeployment getOpenDeployment(String state) throws Exception {
        CmsDpmtProcessor processor = new CmsDpmtProcessor();
        processor.setDpmtMapper(dpmtMapper);
        List<CmsDeployment> deploymentsExpected = getCmsDeployments(state);
        when(dpmtMapper.findLatestDeployment(nsPath, null)).thenReturn(deploymentsExpected);
        CmsDeployment openDeployments = processor.getOpenDeployments(nsPath);
        return openDeployments;
    }


    private List<CmsDeployment> getCmsDeployments(String state) {
        CmsDeployment deployment = new CmsDeployment();
        deployment.setDeploymentState(state);
        return Arrays.asList(deployment);
    }

}
