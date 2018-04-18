package com.oneops.transistor.service;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.tekton.TektonClient;
import com.oneops.transistor.service.peristenceless.BomData;
import org.mockito.ArgumentMatcher;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SoftQuotaTest {

    CmsVar cmsVar = new CmsVar();
    BomAsyncProcessor bomAsyncProcessor = new BomAsyncProcessor();
    BomData bomData;
    ArrayList<CmsRfcCI> cis = new ArrayList<>();
    ArrayList<CmsRfcRelation > relations = new ArrayList<>();
    TektonClient tektonClientMock = Mockito.mock(TektonClient.class);

    @BeforeMethod
    public void setupTest() {
        cmsVar = new CmsVar();
        bomAsyncProcessor = new BomAsyncProcessor();
        cis = new ArrayList<>();
        relations = new ArrayList<>();
        tektonClientMock = Mockito.mock(TektonClient.class);
        CmsCmProcessor cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

        String cloudProviderMappings = "[{\"provider\":\"Azure\",\"computeMapping\":[{\"size\":\"M\",\"ip\":1,\"nic\":1,\"cores\":2}]}]";
        cmsVar.setValue(cloudProviderMappings);
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(BomAsyncProcessor.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVar);
        Mockito.when(cmsCmProcessor.getNextDjId()).thenReturn(1000L);

        bomAsyncProcessor.setCmProcessor(cmsCmProcessor);
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

        CmsRfcRelation deployedToRelationForAdd = createDeployedToRelation(1L, "azure-east");
        CmsRfcRelation deployedToRelationForUpdate = createDeployedToRelation(2L, "azure-east");

        relations.add(deployedToRelationForAdd);
        relations.add(deployedToRelationForUpdate);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName,
                bomAsyncProcessor.getCloudProviderMappings());

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("cores", 2);
        expectedQuotaRequest.put("Azure", resources);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(Matchers.argThat(new QuotaRequestMatcher(expectedQuotaRequest)),
                        Mockito.anyString(), Mockito.eq(orgName), Mockito.eq(userName));
    }

    private CmsRfcRelation createDeployedToRelation(long id, String cloudName) {
        CmsRfcRelation deployedToRelation = new CmsRfcRelation();
        deployedToRelation.setRelationName("base.DeployedTo");
        deployedToRelation.setFromCiId(id);
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

        CmsRfcRelation deployedToRelation = createDeployedToRelation(2L, "azure-east");

        relations.add(deployedToRelation);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName,
                bomAsyncProcessor.getCloudProviderMappings());

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("cores", 2);
        expectedQuotaRequest.put("Azure", resources);

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

        CmsRfcRelation deployedToRelationForAdd = createDeployedToRelation(1L, "cdc1");
        CmsRfcRelation deployedToRelationForUpdate = createDeployedToRelation(2L, "cdc1");

        relations.add(deployedToRelationForAdd);
        relations.add(deployedToRelationForUpdate);

        String orgName = "oneops";
        String userName = "user1";

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName,
                bomAsyncProcessor.getCloudProviderMappings());

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("cores", 2);
        expectedQuotaRequest.put("Azure", resources);

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
