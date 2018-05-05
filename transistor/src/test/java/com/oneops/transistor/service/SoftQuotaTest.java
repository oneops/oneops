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
    private static final long GOOGLE_CLOUD_ID = 21;
    private static final String AZURE_SUBSCRIPTION_ID = "azure-sub1";
    private static final String GOOGLE_SUBSCRIPTION_ID = "google-sub1";
    private static final String AZURE_CLOUD_LOCATION = "azure-somecloud";
    CmsVar cmsVarProviderMapping = new CmsVar();
    CmsVar cmsVarSoftQuotaEnabled = new CmsVar();
    BomAsyncProcessor bomAsyncProcessor = new BomAsyncProcessor();
    TektonUtils tektonUtils = new TektonUtils();
    BomData bomData;
    CmsCI azureCloudCi = new CmsCI();
    CmsCI googleCloudCi = new CmsCI();
    ArrayList<CmsRfcCI> cis = new ArrayList<>();
    ArrayList<CmsRfcRelation > relations = new ArrayList<>();
    TektonClient tektonClientMock = Mockito.mock(TektonClient.class);
    List<CmsCIRelation> azureCloudServicesRelations = new ArrayList<>();
    List<CmsCIRelation> googleCloudServicesRelations = new ArrayList<>();
    CmsCI azureComputeCloudService = new CmsCI();
    CmsCI googleComputeCloudService = new CmsCI();
    CmsCmProcessor cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

    @BeforeMethod
    public void setupTest() {
        Mockito.reset(cmsCmProcessor);
        cmsVarProviderMapping = new CmsVar();
        cmsVarSoftQuotaEnabled = new CmsVar();
        azureCloudCi = new CmsCI();
        CmsCIAttribute locationAttribute = new CmsCIAttribute();
        locationAttribute.setAttributeName("location");
        locationAttribute.setDfValue("/providers/" + AZURE_CLOUD_LOCATION);
        azureCloudCi.addAttribute(locationAttribute);
        azureCloudCi.setCiId(CLOUD_ID);
        //for google cloud
        googleCloudCi = new CmsCI();
        CmsCIAttribute googleCloudLocationAttribute = new CmsCIAttribute();
        googleCloudLocationAttribute.setAttributeName("location");
        googleCloudLocationAttribute.setDfValue("/providers/google-somecloud");
        googleCloudCi.addAttribute(googleCloudLocationAttribute);
        googleCloudCi.setCiId(GOOGLE_CLOUD_ID);


        bomAsyncProcessor = new BomAsyncProcessor();
        cis = new ArrayList<>();
        relations = new ArrayList<>();
        tektonClientMock = Mockito.mock(TektonClient.class);
        cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

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
        Mockito.when(cmsCmProcessor.getCiById(Mockito.eq(CLOUD_ID))).thenReturn(azureCloudCi);
        Mockito.when(cmsCmProcessor.getCiById(Mockito.eq(GOOGLE_CLOUD_ID))).thenReturn(googleCloudCi);

        //create provides rel
        CmsCIRelation relation = new CmsCIRelation();
        azureCloudServicesRelations.add(relation);
        //set rel attribute "service" with df value as "compute"
        CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
        attribute.setAttributeName("service");
        attribute.setDfValue("compute");
        relation.addAttribute(attribute);

        //create cloudService CI and set it as toCi.
        azureComputeCloudService = new CmsCI();
        relation.setToCi(azureComputeCloudService);
        CmsCIAttribute subscriptionAttribute = new CmsCIAttribute();
        subscriptionAttribute.setAttributeName("subscription");
        subscriptionAttribute.setDfValue(AZURE_SUBSCRIPTION_ID);
        azureComputeCloudService.addAttribute(subscriptionAttribute);
        azureComputeCloudService.setCiClassName("cloud.service.Azure");

        Mockito.when(cmsCmProcessor.getFromCIRelations(Mockito.eq(CLOUD_ID),
                Mockito.eq("base.Provides"), Mockito.anyString())).thenReturn(azureCloudServicesRelations);

        //for google
        CmsCIRelation relationGoogle = new CmsCIRelation();
        googleCloudServicesRelations.add(relationGoogle);
        //set rel attribute "service" with df value as "compute"
        CmsCIRelationAttribute attributeGoogle = new CmsCIRelationAttribute();
        attributeGoogle.setAttributeName("service");
        attributeGoogle.setDfValue("compute");
        relationGoogle.addAttribute(attributeGoogle);
        googleComputeCloudService = new CmsCI();
        relationGoogle.setToCi(googleComputeCloudService);
        subscriptionAttribute = new CmsCIAttribute();
        subscriptionAttribute.setAttributeName("subscription");
        subscriptionAttribute.setDfValue(GOOGLE_SUBSCRIPTION_ID);
        googleComputeCloudService.addAttribute(subscriptionAttribute);
        googleComputeCloudService.setCiClassName("cloud.service.Google");

        Mockito.when(cmsCmProcessor.getFromCIRelations(Mockito.eq(CLOUD_ID),
                Mockito.eq("base.Provides"), Mockito.anyString())).thenReturn(azureCloudServicesRelations);

        Mockito.when(cmsCmProcessor.getFromCIRelations(Mockito.eq(GOOGLE_CLOUD_ID),
                Mockito.eq("base.Provides"), Mockito.anyString())).thenReturn(googleCloudServicesRelations);

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
        expectedQuotaRequest.put(AZURE_CLOUD_LOCATION + ":" + AZURE_SUBSCRIPTION_ID, resources);

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

        //Add "subscription" as ciAttribute for cloudService ci with df value
        CmsRfcRelation deployedToRelationForAdd = createDeployedToRelation(1L, GOOGLE_CLOUD_ID, "cdc1");
        CmsRfcRelation deployedToRelationForUpdate = createDeployedToRelation(2L, GOOGLE_CLOUD_ID, "cdc1");

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
