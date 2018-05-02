package com.oneops.controller.jms;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_COMPLETE;
import static com.oneops.cms.dj.service.CmsDpmtProcessor.DPMT_STATE_FAILED;

public class WorkOrderResponseSoftQuotaTest {
    private static final String SUBSCRIPTION_ID = "azure-subscription1";
    InductorListener inductorListener = new InductorListener();
    CmsWorkOrderSimple workOrder = new CmsWorkOrderSimple();
    Map<String, Object> params = new HashMap<>();
    private static final long  DEPLOYMENT_ID = 11L;
    TektonClient tektonClientMock = Mockito.mock(TektonClient.class);
    CmsVar cmsVar = new CmsVar();
    private static final long CLOUD_ID = 20;
    List<CmsCIRelation> cloudServicesRelations = new ArrayList<>();
    CmsCI cloudCi = new CmsCI();

    @BeforeTest
    public void setup() {
        inductorListener = new InductorListener();
        workOrder = new CmsWorkOrderSimple();
        CmsCISimple cloudCiSimple = new CmsCISimple();
        cloudCiSimple.setCiName("azure");
        cloudCiSimple.setCiId(CLOUD_ID);
        workOrder.setCloud(cloudCiSimple);
        workOrder.setDeploymentId(DEPLOYMENT_ID);
        CmsCmProcessor cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

        String cloudProviderMappings = "{\n" +
                "  \"compute\": {\n" +
                "    \"azure\": {\n" +
                "      \"size\": {\n" +
                "        \"M\": {\n" +
                "          \"Dv2\": 2,\n" +
                "          \"vm\": 1\n" +
                "        },\n" +
                "        \"L\": {\n" +
                "          \"Dv2\": 6,\n" +
                "          \"vm\": 1\n" +
                "        },\n" +
                "        \"mem-L\": {\n" +
                "          \"DSv3\": 4,\n" +
                "          \"vm\": 1\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"openstack\": {\n" +
                "      \"size\": {\n" +
                "        \"M\": {\n" +
                "          \"cores\": 4\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        cmsVar.setValue(cloudProviderMappings);
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(TektonUtils.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVar);
        TektonUtils tektonUtils = new TektonUtils();
        tektonUtils.setCmProcessor(cmsCmProcessor);
        inductorListener.setTektonUtils(tektonUtils);
        inductorListener.setTektonClient(tektonClientMock);

        //create provides rel
        CmsCIRelation relation = new CmsCIRelation();
        cloudServicesRelations.add(relation);
        //set rel attribute "service" with df value as "compute"
        CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
        attribute.setAttributeName("service");
        attribute.setDfValue("compute");
        relation.addAttribute(attribute);

        //create cloudService CI and set it as toCi.
        CmsCI computeCloudService = new CmsCI();
        relation.setToCi(computeCloudService);
        CmsCIAttribute subscriptionAttribute = new CmsCIAttribute();
        subscriptionAttribute.setAttributeName("subscription");
        subscriptionAttribute.setDfValue(SUBSCRIPTION_ID);
        computeCloudService.addAttribute(subscriptionAttribute);

        //Add "subscription" as ciAttribute for cloudService ci with df value
        Mockito.when(cmsCmProcessor.getFromCIRelations(Mockito.eq(CLOUD_ID),
                Mockito.eq("base.Provides"), Mockito.anyString())).thenReturn(cloudServicesRelations);

        cloudCi = new CmsCI();
        CmsCIAttribute locationAttribute = new CmsCIAttribute();
        locationAttribute.setAttributeName("location");
        locationAttribute.setDfValue("/providers/azure-somecloud");
        cloudCi.addAttribute(locationAttribute);
        cloudCi.setCiId(CLOUD_ID);

        Mockito.when(cmsCmProcessor.getCiById(Mockito.eq(CLOUD_ID))).thenReturn(cloudCi);
    }

    @Test
    public void testAddSuccess() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_COMPLETE);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("Dv2", 2);
        expectedResourceNumbers.put("vm", 1);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .commitReservation(argument.capture(), Mockito.eq(DEPLOYMENT_ID + SUBSCRIPTION_ID));

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
        expectedResourceNumbers.put("Dv2", 2);
        expectedResourceNumbers.put("vm", 1);

        Mockito.verify(tektonClientMock, Mockito.times(0))
                .rollbackReservation(Mockito.anyMap(), Mockito.anyString());

        Mockito.verify(tektonClientMock, Mockito.times(0))
                .commitReservation(Mockito.anyMap(), Mockito.anyString());
    }

    @Test
    public void testDeleteSuccess() throws IOException {
        CmsRfcCISimple rfcCI = createComputeRfc(1L);
        rfcCI.setRfcAction("delete");
        params.put(CmsConstants.WORK_ORDER_STATE, DPMT_STATE_COMPLETE);
        workOrder.setRfcCi(rfcCI);
        inductorListener.updateQuota(workOrder, params);
        Map<String, Integer> expectedResourceNumbers = new HashMap<>();
        expectedResourceNumbers.put("Dv2", 2);
        expectedResourceNumbers.put("vm", 1);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .releaseResources(Mockito.eq("oneops"), Mockito.eq(SUBSCRIPTION_ID), argument.capture());

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

