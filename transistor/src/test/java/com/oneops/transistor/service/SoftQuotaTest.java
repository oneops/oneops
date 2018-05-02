package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.tekton.TektonClient;
import com.oneops.tekton.TektonUtils;
import com.oneops.transistor.service.peristenceless.BomData;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SoftQuotaTest {

    private static final long CLOUD_ID = 20;
    private static final String SUBSCRIPTION_ID = "azure-sub1";
    CmsVar cmsVarProviderMapping = new CmsVar();
    CmsVar cmsVarSoftQuotaEnabled = new CmsVar();
    BomAsyncProcessor bomAsyncProcessor = new BomAsyncProcessor();
    TektonUtils tektonUtils = new TektonUtils();
    BomData bomData;
    CmsCI cloudCi = new CmsCI();
    ArrayList<CmsRfcCI> cis = new ArrayList<>();
    ArrayList<CmsRfcRelation > relations = new ArrayList<>();
    TektonClient tektonClientMock = Mockito.mock(TektonClient.class);
    List<CmsCIRelation> cloudServicesRelations = new ArrayList<>();

    @BeforeMethod
    public void setupTest() {
        cmsVarProviderMapping = new CmsVar();
        cmsVarSoftQuotaEnabled = new CmsVar();
        cloudCi = new CmsCI();
        CmsCIAttribute locationAttribute = new CmsCIAttribute();
        locationAttribute.setAttributeName("location");
        locationAttribute.setDfValue("/providers/azure-somecloud");
        cloudCi.addAttribute(locationAttribute);
        cloudCi.setCiId(CLOUD_ID);
        bomAsyncProcessor = new BomAsyncProcessor();
        cis = new ArrayList<>();
        relations = new ArrayList<>();
        tektonClientMock = Mockito.mock(TektonClient.class);
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
        cmsVarProviderMapping.setValue(cloudProviderMappings);
        cmsVarSoftQuotaEnabled.setValue("true");
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(TektonUtils.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVarProviderMapping);
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(TektonUtils.IS_SOFT_QUOTA_ENABLED_VAR_NAME))).thenReturn(cmsVarSoftQuotaEnabled);
        Mockito.when(cmsCmProcessor.getNextDjId()).thenReturn(1000L);
        Mockito.when(cmsCmProcessor.getCiById(Mockito.eq(CLOUD_ID))).thenReturn(cloudCi);

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

        bomAsyncProcessor.setCmProcessor(cmsCmProcessor);
        tektonUtils.setCmProcessor(cmsCmProcessor);
        bomAsyncProcessor.setTektonUtils(tektonUtils);
        bomData = new BomData(null, cis, relations);
        bomAsyncProcessor.setTektonClient(tektonClientMock);
    }

    @Test
    public void testReservation() throws IOException {

        CmsRfcCI computeAddRfc_1 = createComputeRfc(1L);
        CmsRfcCI computeUpdateRfc_1 = createComputeRfc(2L);
        computeUpdateRfc_1.setRfcAction("update");
        cis.add(computeAddRfc_1);
        cis.add(computeUpdateRfc_1);

        CmsRfcRelation deployedToRelationForAdd = createDeployedToRelation(1L, CLOUD_ID, "azure-east");
        CmsRfcRelation deployedToRelationForUpdate = createDeployedToRelation(2L, CLOUD_ID,"azure-east");

        relations.add(deployedToRelationForAdd);
        relations.add(deployedToRelationForUpdate);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName);

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("Dv2", 2);
        resources.put("vm", 1);
        expectedQuotaRequest.put(SUBSCRIPTION_ID, resources);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(Matchers.argThat(new QuotaRequestMatcher(expectedQuotaRequest)),
                        Mockito.anyString(), Mockito.eq(orgName), Mockito.eq(userName));
    }

    private CmsRfcRelation createDeployedToRelation(long ciId, long cloudId, String cloudName) {
        CmsRfcRelation deployedToRelation = new CmsRfcRelation();
        deployedToRelation.setRelationName("base.DeployedTo");
        deployedToRelation.setFromCiId(ciId);
        deployedToRelation.setToCiId(cloudId);
        deployedToRelation.setComments("{\"toCiName\":\"" + cloudName + "\"}");

        return deployedToRelation;
    }

    private CmsRfcCI createComputeRfc(long id) {
        CmsRfcCI computeAddRfc_1 = new CmsRfcCI();
        computeAddRfc_1.setRfcAction("add");
        computeAddRfc_1.setCiClassName("bom.Compute");
        computeAddRfc_1.setCiId(id);
        CmsRfcAttribute attribute = new CmsRfcAttribute();
        attribute.setAttributeName("size");
        attribute.setNewValue("M");
        computeAddRfc_1.addAttribute(attribute);

        return computeAddRfc_1;
    }

    @Test
    public void testNoReservation() throws IOException {

        CmsRfcCI computeUpdateRfc_1 = createComputeRfc(2L);
        computeUpdateRfc_1.setRfcAction("update");

        cis.add(computeUpdateRfc_1);

        CmsRfcRelation deployedToRelation = createDeployedToRelation(2L, CLOUD_ID, "azure-east");

        relations.add(deployedToRelation);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName);

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("Dv2", 2);
        expectedQuotaRequest.put("azure", resources);

        Mockito.verify(tektonClientMock, Mockito.times(0))
                .reserveQuota(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    public void testNoReservationForCloud() throws IOException {
        CmsRfcCI computeAddRfc_1 = createComputeRfc(1L);
        CmsRfcCI computeUpdateRfc_1 = createComputeRfc(2L);
        computeUpdateRfc_1.setRfcAction("update");
        cis.add(computeAddRfc_1);
        cis.add(computeUpdateRfc_1);
        cloudCi.getAttribute("location").setDfValue("/providers/google-somecloud");

        CmsRfcRelation deployedToRelationForAdd = createDeployedToRelation(1L, CLOUD_ID, "cdc1");
        CmsRfcRelation deployedToRelationForUpdate = createDeployedToRelation(2L, CLOUD_ID, "cdc1");

        relations.add(deployedToRelationForAdd);
        relations.add(deployedToRelationForUpdate);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName);

        assert(deploymentId > 0);

        Mockito.verify(tektonClientMock, Mockito.times(0))
                .reserveQuota(Mockito.anyObject(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    private static final class QuotaRequestMatcher extends ArgumentMatcher<Map<String, Map<String, Integer>>> {

        Map<String, Map<String, Integer>> expectedQuotaReqest;
        public QuotaRequestMatcher(Map<String, Map<String, Integer>> expectedQuotaReqest) {
            this.expectedQuotaReqest = expectedQuotaReqest;
        }

        @Override
        public boolean matches(Object argument) {
            Map<String, Map<String, Integer>> actualQuotaRequest = (Map<String, Map<String, Integer>>) argument;
            if (actualQuotaRequest.size() != this.expectedQuotaReqest.size()) {
                return false;
            }
            for (String provider : actualQuotaRequest.keySet()) {
                Map<String, Integer> expectedResources = expectedQuotaReqest.get(provider);
                Map<String, Integer> actualResources = actualQuotaRequest.get(provider);

                if (actualResources.size() != expectedResources.size()) {
                    return false;
                }

                for (String key : actualResources.keySet()) {
                    if (! actualResources.get(key).equals(expectedResources.get(key))) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}
