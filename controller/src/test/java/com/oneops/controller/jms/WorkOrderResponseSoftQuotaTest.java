package com.oneops.controller.jms;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.tekton.TektonClient;
import com.oneops.tekton.TektonUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_COMPLETE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_FAILED;

public class WorkOrderResponseSoftQuotaTest {
    InductorListener inductorListener = new InductorListener();
    CmsWorkOrderSimple workOrder = new CmsWorkOrderSimple();
    Map<String, Object> params = new HashMap<>();
    private long deploymentId = 11L;
    TektonClient tektonClientMock = Mockito.mock(TektonClient.class);
    CmsVar cmsVar = new CmsVar();

    @BeforeTest
    public void setup() {
        inductorListener = new InductorListener();
        workOrder = new CmsWorkOrderSimple();
        CmsCISimple cloudCi = new CmsCISimple();
        cloudCi.setCiName("azure");
        workOrder.setCloud(cloudCi);
        workOrder.setDeploymentId(deploymentId);
        CmsCmProcessor cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

        String cloudProviderMappings = "[{\"provider\":\"Azure\",\"computeMapping\":[{\"size\":\"M\",\"ip\":1,\"nic\":1,\"cores\":2}]}]";
        cmsVar.setValue(cloudProviderMappings);
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(TektonUtils.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVar);
        TektonUtils tektonUtils = new TektonUtils();
        tektonUtils.setCmProcessor(cmsCmProcessor);
        inductorListener.setTektonUtils(tektonUtils);
        inductorListener.setTektonClient(tektonClientMock);
    }

    @Test
    public void testAddSuccess() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_COMPLETE);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("cores", 2);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .commitReservation(argument.capture(), Mockito.eq(deploymentId + "Azure"));

        Map<String, Integer> actualArgument = argument.getValue();

        Assert.assertEquals(actualArgument, expectedResourceNumbers);
    }

    @Test
    public void testAddFailure() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_FAILED);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("cores", 2);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .rollbackReservation(argument.capture(), Mockito.eq(deploymentId + "Azure"));

        Map<String, Integer> actualArgument = argument.getValue();

        Assert.assertEquals(actualArgument, expectedResourceNumbers);
    }

    @Test
    public void testDeleteSuccess() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        rfcCI.setRfcAction("delete");
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_COMPLETE);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("cores", 2);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .releaseResources(Mockito.eq("oneops"), Mockito.eq("Azure"), argument.capture());

        Map<String, Integer> actualArgument = argument.getValue();

        Assert.assertEquals(actualArgument, expectedResourceNumbers);
    }

    @Test
    public void testDeleteFailure() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        rfcCI.setRfcAction("delete");
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_FAILED);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("cores", 2);

        Mockito.verify(tektonClientMock, Mockito.times(0))
                .releaseResources(Mockito.anyString(), Mockito.anyString(), Mockito.anyMap());
    }

    private CmsRfcCISimple createComputeRfc(long id) {
        CmsRfcCISimple rfcCI = new CmsRfcCISimple();
        rfcCI.addCiAttribute("size", "M");
        rfcCI.setCiId(id);
        rfcCI.setCiClassName("bom.Compute");
        rfcCI.setNsPath("/oneops/assembly/env");
        rfcCI.setRfcAction("add");
        return rfcCI;
    }

}

