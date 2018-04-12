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
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SoftQuotaTest {

    @Test
    public void testReserveQuota() throws IOException {
        BomAsyncProcessor bomAsyncProcessor = new BomAsyncProcessor();
        CmsCmProcessor cmsCmProcessor = Mockito.mock(CmsCmProcessor.class);

        CmsVar cmsVar = new CmsVar();
        String cloudProviderMappings = "[{\"provider\":\"Azure\",\"computeMapping\":[{\"size\":\"M\",\"ip\":1,\"nic\":1,\"cores\":2}]}]";
        cmsVar.setValue(cloudProviderMappings);
        Mockito.when(cmsCmProcessor.getCmSimpleVar(Mockito.eq(BomAsyncProcessor.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVar);
        Mockito.when(cmsCmProcessor.getNextDjId()).thenReturn(1000L);

        bomAsyncProcessor.setCmProcessor(cmsCmProcessor);
        ArrayList<CmsRfcCI> cis = new ArrayList<>();
        ArrayList<CmsRfcRelation > relations = new ArrayList<>();

        CmsRfcCI computeAddRfc_1 = new CmsRfcCI();
        computeAddRfc_1.setRfcAction("add");
        computeAddRfc_1.setCiClassName("bom.Compute");
        computeAddRfc_1.setCiId(1);
        CmsRfcAttribute attribute = new CmsRfcAttribute();
        attribute.setAttributeName("size");
        attribute.setNewValue("M");
        computeAddRfc_1.addAttribute(attribute);

        cis.add(computeAddRfc_1);

        CmsRfcRelation deployedToRelation = new CmsRfcRelation();
        deployedToRelation.setRelationName("base.DeployedTo");
        deployedToRelation.setFromCiId(1L);
        deployedToRelation.setComments("{\"toCiName\":\"azure-east\"}");

        relations.add(deployedToRelation);

        BomData bomData = new BomData(null, cis, relations);

        String orgName = "oneops";
        String userName = "user1";
        TektonClient tektonClientMock = Mockito.mock(TektonClient.class);
        bomAsyncProcessor.setTektonClient(tektonClientMock);

        Long deploymentId = bomAsyncProcessor.reserveQuota(bomData, orgName, userName, bomAsyncProcessor.getCloudProviderMappings());

        assert(deploymentId > 0);

        Map<String, Map<String, Integer>> expectedQuotaRequest = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("cores", 2);
        expectedQuotaRequest.put("Azure", resources);

        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(Matchers.argThat(new QuotaRequestMatcher(expectedQuotaRequest)),
                        Mockito.anyString(), Mockito.eq(orgName), Mockito.eq(userName));
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
