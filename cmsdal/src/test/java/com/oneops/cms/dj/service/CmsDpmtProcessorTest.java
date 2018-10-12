package com.oneops.cms.dj.service;

import com.oneops.capacity.CapacityProcessor;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.CmsVar;
import org.testng.annotations.Test;

import java.util.ArrayList;
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
    private static final String DEPLOYMENT_APPROVAL_BYPASS_FLAG = "DEPLOYMENT_APPROVAL_BYPASS_FLAG";
    private String nsPath = "/test/";
    DJMapper djMapper = mock(DJMapper.class);
    UtilMapper utilMapper = mock(UtilMapper.class);


    @Test
    public void testOpenDeploymentsWhenInActive() throws Exception {
        //active
        CmsDeployment openDeployments = getOpenDeployment("active");
        assertTrue(openDeployments != null);
    }

    @Test
    public void testGlobalDeploymentFlagTrue() throws Exception {
        //active
        CmsDeployment openDeployments = getGlobalDeploymentFlagTrue("active");
        assertTrue(openDeployments.getDeploymentState().equalsIgnoreCase("active"));
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

    public CmsDeployment getGlobalDeploymentFlagTrue(String state) throws Exception {
        CmsDpmtProcessor processor = new CmsDpmtProcessor();


        CmsCmProcessor cmProcessor = new CmsCmProcessor();
        CmsRfcProcessor rfcProcessor = new CmsRfcProcessor();
        CapacityProcessor capacityProcessor = new CapacityProcessor();

        cmProcessor.setDjMapper(djMapper);
        rfcProcessor.setDjMapper(djMapper);
        cmProcessor.setUtilMapper(utilMapper);
        processor.setCmProcessor(cmProcessor);
        processor.setRfcProcessor(rfcProcessor);
        processor.setCapacityProcessor(capacityProcessor);
        processor.setDpmtMapper(dpmtMapper);
        capacityProcessor.setCmProcessor(cmProcessor);
       // when(dpmtMapper.getGlobalDeploymentApprovalBypassFlag(DEPLOYMENT_APPROVAL_BYPASS_FLAG)).thenReturn("true");

        when(djMapper.getNextDjId()).thenReturn(1L);
        CmsRelease cmsRelease = new CmsRelease();
        cmsRelease.setReleaseState("open");
        cmsRelease.setReleaseStateId(1);
        cmsRelease.setNsPath(nsPath + "/test/open/java/one/bom");
        when(djMapper.getReleaseById(1)).thenReturn(cmsRelease);
        when(cmProcessor.getNextDjId()).thenReturn(1L);
        CmsVar softQuotaEnabled = new CmsVar();
        softQuotaEnabled.setId(1);
        softQuotaEnabled.setName("test");
        softQuotaEnabled.setValue("test");
        CmsVar isBypassApprovalFlagEnabled = new CmsVar();
        isBypassApprovalFlagEnabled.setId(2);
        isBypassApprovalFlagEnabled.setName("DEPLOYMENT_APPROVAL_BYPASS_FLAG");
        isBypassApprovalFlagEnabled.setValue("true");
        when(cmProcessor.getCmSimpleVar("CAPACITY_MANAGEMENT")).thenReturn(softQuotaEnabled);
        when(cmProcessor.getCmSimpleVar("DEPLOYMENT_APPROVAL_BYPASS_FLAG")).thenReturn(isBypassApprovalFlagEnabled);
        when(rfcProcessor.getReleaseById(1)).thenReturn(cmsRelease);
        CmsDeployment cmsDeployment = new CmsDeployment();
        cmsDeployment.setReleaseId(1);
        List<CmsDeployment> list = new ArrayList<>();
        list.add(cmsDeployment);
        when(dpmtMapper.findLatestDeploymentByReleaseId(cmsDeployment.getReleaseId(), null)).thenReturn(list);
        when(utilMapper.getCmSimpleVar("CAPACITY_MANAGEMENT")).thenReturn(softQuotaEnabled);
        when(utilMapper.getCmSimpleVar("DEPLOYMENT_APPROVAL_BYPASS_FLAG")).thenReturn(isBypassApprovalFlagEnabled);
        CmsDeployment cmsDeploymentforRelease = processor.deployRelease(cmsDeployment);
        return cmsDeploymentforRelease;
    }

}
