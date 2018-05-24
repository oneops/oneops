package com.oneops.capacity;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.domain.CmsVar;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;

public class CapacityProcessorTest {

    private static final long AZURE_CLOUD_ID = 20;
    private static final long OPENSTACK_CLOUD_ID = 21;
    private static final long GOOGLE_CLOUD_ID = 22;
    private static final String AZURE_CLOUD_LOCATION = "azure-eastus";
    private static final String OPENSTACK_CLOUD_LOCATION = "openstack-dal";
    private static final String GOOGLE_CLOUD_LOCATION = "google-westcoast";
    private static final String AZURE_SUBSCRIPTION_ID = "azure-sub1";
    private static final String OPENSTACK_SUBSCRIPTION_ID = "openstack-tenant1";
    private static final String GOOGLE_SUBSCRIPTION_ID = "google-sub1";
    private static final String ENV_NS_PATH = "/org1/a1/e1";


    private CmsVar cmsVarCapacityManagement;
    private CmsCI azureCloud;
    private CmsCI openstackCloud;
    private CmsCI googleCloud;
    private ArrayList<CmsRfcCI> bomCIs;
    private ArrayList<CmsRfcRelation > bomRels;

    private CapacityProcessor capacityProcessor = new CapacityProcessor();
    private CmsCmProcessor cmProcessorMock;
    private TektonClient tektonClientMock = Mockito.mock(TektonClient.class);

    @BeforeMethod
    public void setupTest() {
        bomCIs = new ArrayList<>();
        bomRels = new ArrayList<>();

        cmProcessorMock = Mockito.mock(CmsCmProcessor.class);

        CmsVar cmsVarProviderMapping = new CmsVar();
        cmsVarCapacityManagement = new CmsVar();
        String cloudProviderMappings = "{\n" +
                "  \"azure\": {\n" +
                "    \"compute\": {\n" +
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
                "          \"DSv3\": 6,\n" +
                "          \"vm\": 1\n" +
                "        }\n" +
                "      },\n" +
                "      \"*\": {\n" +
                "        \"*\": {\n" +
                "          \"nic\": 1\n" +
                "        }\n" +
                "      }\n" +
                "    },\n" +
                "    \"lb\": {\n" +
                "      \"*\": {\n" +
                "        \"*\": {\n" +
                "          \"lb\": 1\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  },\n" +
                "  \"openstack\": {\n" +
                "    \"compute\": {\n" +
                "      \"size\": {\n" +
                "        \"M\": {\n" +
                "          \"core\": 4,\n" +
                "          \"vm\": 1\n" +
                "        }\n" +
                "      }\n" +
                "    }\n" +
                "  }\n" +
                "}";
        cmsVarProviderMapping.setValue(cloudProviderMappings);
        cmsVarCapacityManagement.setValue("true");
        Mockito.when(cmProcessorMock.getCmSimpleVar(Mockito.eq(CapacityProcessor.PROVIDER_MAPPINGS_CMS_VAR_NAME))).thenReturn(cmsVarProviderMapping);
        Mockito.when(cmProcessorMock.getCmSimpleVar(Mockito.eq(CapacityProcessor.CAPACITY_MANAGEMENT_VAR_NAME))).thenReturn(cmsVarCapacityManagement);

        azureCloud = setupCloud(AZURE_CLOUD_ID, "azure", "cloud.service.Azure", AZURE_CLOUD_LOCATION, "subscription", AZURE_SUBSCRIPTION_ID);
        openstackCloud = setupCloud(OPENSTACK_CLOUD_ID, "openstack", "cloud.service.Openstack", OPENSTACK_CLOUD_LOCATION, "tenant", OPENSTACK_SUBSCRIPTION_ID);
        googleCloud = setupCloud(GOOGLE_CLOUD_ID, "google", "cloud.service.Google", GOOGLE_CLOUD_LOCATION, "subscription", GOOGLE_SUBSCRIPTION_ID);

        Mockito.when(cmProcessorMock.getFromCIRelationsNakedNoAttrs(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString(), Mockito.anyString()))
                .thenAnswer((i) -> bomRels.stream()
                        .filter(r -> r.getFromCiId().equals(i.getArguments()[0]))
                        .map(r -> {
                            CmsCIRelation rel = new CmsCIRelation();
                            rel.setRelationName("base.DeployedTo");
                            rel.setFromCiId(r.getFromCiId());
                            rel.setToCiId(r.getToCiId());
                            return rel;
                        })
                        .collect(Collectors.toList()));

        capacityProcessor.setCmProcessor(cmProcessorMock);


        CmsRfcProcessor rfcProcessor = Mockito.mock(CmsRfcProcessor.class);
        Mockito.when(rfcProcessor.getRfcCIBy3(Mockito.anyLong(), Mockito.anyBoolean(), Mockito.anyLong()))
                .thenReturn(bomCIs);
        Mockito.when(cmProcessorMock.getCountCIRelationsGroupByToCiId(Mockito.eq("base.Consumes"), Mockito.anyString(), Mockito.anyString(), Mockito.eq(ENV_NS_PATH)))
                .thenAnswer(i -> bomRels.stream().collect(Collectors.groupingBy(CmsRfcRelation::getToCiId, Collectors.counting())));
        Mockito.when(rfcProcessor.getRfcRelationByReleaseAndClassNoAttrs(Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()))
                .thenReturn(bomRels);
        Mockito.when(rfcProcessor.getOpenRfcRelationBy2NoAttrs(Mockito.anyLong(), Mockito.anyLong(), Mockito.anyString(), Mockito.anyString()))
                .thenAnswer(i -> bomRels.stream().filter(r -> r.getFromCiId().equals(i.getArguments()[0])).collect(Collectors.toList()));
        capacityProcessor.setRfcProcessor(rfcProcessor);


        tektonClientMock = Mockito.mock(TektonClient.class);
        capacityProcessor.setTektonClient(tektonClientMock);
    }

    @Test
    public void capacityManagementEnabled() {
        cmsVarCapacityManagement.setValue("false");
        boolean enabled = capacityProcessor.isCapacityManagementEnabled(null);
        assertEquals(enabled, false, "Capacity Management should be disabled when CAPACITY_MANAGEMENT=false");

        cmsVarCapacityManagement.setValue("true");
        enabled = capacityProcessor.isCapacityManagementEnabled(null);
        assertEquals(enabled, true, "Capacity Management should be enabled when CAPACITY_MANAGEMENT=true");

        cmsVarCapacityManagement.setValue("/org1,/org2,/org3");
        enabled = capacityProcessor.isCapacityManagementEnabled("/org2/a1/e1");
        assertEquals(enabled, true, "Capacity Management should be enabled for ns included in CAPACITY_MANAGEMENT");
        enabled = capacityProcessor.isCapacityManagementEnabled("/org4/a1");
        assertEquals(enabled, false, "Capacity Management should be disabled for ns when not included in CAPACITY_MANAGEMENT");
    }

    @Test
    public void testReserve() throws ReservationException {
        CmsDeployment deployment = new CmsDeployment();
        long deploymentId = 1L;
        long releaseId = 1L;
        String createdBy = "user1";
        deployment.setReleaseId(deploymentId);
        deployment.setDeploymentId(releaseId);
        deployment.setNsPath(ENV_NS_PATH + "/bom");
        deployment.setCreatedBy(createdBy);


        // Should reserve.
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "update", "M", azureCloud);
        createComputeRfc(2L, "add", "M", azureCloud);
        createComputeRfc(3L, "delete", "M", azureCloud);
        createComputeRfc(4L, "add", "M", azureCloud);
        createComputeRfc(5L, "add", "mem-L", azureCloud);
        createComputeRfc(6L, "add", "M", openstackCloud);
        createRfc(7L, "add", "bom.oneops.1.Lb", azureCloud);
        createRfc(8L, "update", "bom.oneops.1.Lb", azureCloud);
        createRfc(9L, "add", "bom.oneops.1.Lb", openstackCloud);

        Map<String, Map<String, Integer>> expectedCapacity = new HashMap<>();
        Map<String, Integer> resources = new HashMap<>();
        resources.put("Dv2", 4);
        resources.put("DSv3", 6);
        resources.put("vm", 3);
        resources.put("nic", 3);
        resources.put("lb", 1);
        expectedCapacity.put(AZURE_CLOUD_LOCATION + ":" + AZURE_SUBSCRIPTION_ID, resources);
        resources = new HashMap<>();
        resources.put("core", 4);
        resources.put("vm", 1);
        expectedCapacity.put(OPENSTACK_CLOUD_LOCATION + ":" + OPENSTACK_SUBSCRIPTION_ID, resources);
        ArgumentCaptor<HashMap> argument = ArgumentCaptor.forClass(HashMap.class);

        capacityProcessor.reserveCapacityForDeployment(deployment);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(argument.capture(), Mockito.eq(ENV_NS_PATH), Mockito.eq(createdBy));

        Assert.assertEquals(argument.getValue(), expectedCapacity);

        // Should not reserve - update RFCs only.
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "update", "M", azureCloud);

        capacityProcessor.reserveCapacityForDeployment(deployment);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(Mockito.anyMap(), Mockito.eq(ENV_NS_PATH), Mockito.eq(createdBy));

        // Should not reserve - no mapping for cloud provider.
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "add", "M", googleCloud);
        createComputeRfc(2L, "update", "M", googleCloud);

        capacityProcessor.reserveCapacityForDeployment(deployment);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .reserveQuota(Mockito.anyMap(), Mockito.eq(ENV_NS_PATH), Mockito.eq(createdBy));
    }

    @Test
    public void testDiscard() {
        CmsDeployment deployment = new CmsDeployment();
        long deploymentId = 1L;
        long releaseId = 1L;
        String createdBy = "user1";
        deployment.setReleaseId(deploymentId);
        deployment.setDeploymentId(releaseId);
        deployment.setNsPath(ENV_NS_PATH + "/bom");
        deployment.setCreatedBy(createdBy);


        // Should reserve.
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "add", "M", azureCloud);
        createComputeRfc(2L, "add", "mem-L", azureCloud);
        createComputeRfc(3L, "add", "M", openstackCloud);

        Set<String> expected = new HashSet<>();
        expected.add(AZURE_CLOUD_LOCATION + ":" + AZURE_SUBSCRIPTION_ID);
        expected.add(OPENSTACK_CLOUD_LOCATION + ":" + OPENSTACK_SUBSCRIPTION_ID);
        ArgumentCaptor<Set> argument = ArgumentCaptor.forClass(Set.class);

        capacityProcessor.discardCapacityForDeployment(deployment);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .deleteReservations(Mockito.eq(ENV_NS_PATH), argument.capture());

        Assert.assertEquals(argument.getValue(), expected);
    }

    @Test
    public void testCommit() throws IOException {
        long deploymentId = 1L;
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "add", "M", azureCloud);

        CmsWorkOrder workOrder = new CmsWorkOrder();
        workOrder.setRfcCi(bomCIs.get(0));
        workOrder.setCloud(azureCloud);
        workOrder.setDeploymentId(deploymentId);

        Map<String, Integer> expectedCapacity = new HashMap<>();
        expectedCapacity.put("Dv2", 2);
        expectedCapacity.put("vm", 1);
        expectedCapacity.put("nic", 1);

        ArgumentCaptor<HashMap> argument = ArgumentCaptor.forClass(HashMap.class);

        capacityProcessor.commitCapacity(workOrder);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .commitReservation(argument.capture(), Mockito.eq(ENV_NS_PATH), Mockito.eq(AZURE_CLOUD_LOCATION + ":" + AZURE_SUBSCRIPTION_ID));
        Assert.assertEquals(argument.getValue(), expectedCapacity);
    }

    @Test
    public void testRelease() throws IOException {
        long deploymentId = 1L;
        bomCIs.clear();
        bomRels.clear();
        createComputeRfc(1L, "add", "M", azureCloud);

        CmsWorkOrder workOrder = new CmsWorkOrder();
        workOrder.setRfcCi(bomCIs.get(0));
        workOrder.setCloud(azureCloud);
        workOrder.setDeploymentId(deploymentId);

        Map<String, Integer> expectedCapacity = new HashMap<>();
        expectedCapacity.put("Dv2", 2);
        expectedCapacity.put("vm", 1);
        expectedCapacity.put("nic", 1);
        ArgumentCaptor<HashMap> argument= ArgumentCaptor.forClass(HashMap.class);

        capacityProcessor.releaseCapacity(workOrder);
        Mockito.verify(tektonClientMock, Mockito.times(1))
                .releaseResources(argument.capture(),
                                  Mockito.eq(ENV_NS_PATH),
                                  Mockito.eq(AZURE_CLOUD_LOCATION + ":" + AZURE_SUBSCRIPTION_ID));
        Assert.assertEquals(argument.getValue(), expectedCapacity);
    }

    private CmsCI setupCloud(long id, String name, String className, String location, String subscriptionAttr, String subscriptionValue) {
        CmsCI cloud = new CmsCI();
        cloud.setCiId(id);
        cloud.setCiName(name);
        CmsCIAttribute locationAttribute = new CmsCIAttribute();
        locationAttribute.setAttributeName("location");
        locationAttribute.setDfValue("/providers/" + location);
        cloud.addAttribute(locationAttribute);

        CmsCI computeService = new CmsCI();
        CmsCIAttribute subscriptionAttribute = new CmsCIAttribute();
        computeService.setCiClassName(className);
        subscriptionAttribute.setAttributeName(subscriptionAttr);
        subscriptionAttribute.setDfValue(subscriptionValue);
        computeService.addAttribute(subscriptionAttribute);

        List<CmsCIRelation> providesRels = new ArrayList<>();
        CmsCIRelation relation = new CmsCIRelation();
        relation.setRelationName("base.Provides");
        providesRels.add(relation);
        CmsCIRelationAttribute attribute = new CmsCIRelationAttribute();
        attribute.setAttributeName("service");
        attribute.setDfValue("compute");
        relation.addAttribute(attribute);
        relation.setToCi(computeService);

        Mockito.when(cmProcessorMock.getCiById(Mockito.eq(id))).thenReturn(cloud);
        Mockito.when(cmProcessorMock.getFromCIRelationsByAttrs(Mockito.eq(id), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList())).thenReturn(providesRels);
        Mockito.when(cmProcessorMock.getFromCIRelationsByAttrs(Mockito.eq(id), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(), Mockito.anyList())).thenReturn(providesRels);
        return cloud;
    }

    private CmsRfcRelation createRfc(long ciId, String rfcAction, String ciClassName, CmsCI cloud) {
        CmsRfcCI ci = new CmsRfcCI();
        ci.setCiId(ciId);
        ci.setRfcAction(rfcAction);
        ci.setCiClassName(ciClassName);
        ci.setNsPath(ENV_NS_PATH + "/bom/p1/1");

        CmsRfcRelation rel = new CmsRfcRelation();
        rel.setRelationName("base.DeployedTo");
        rel.setFromCiId(ciId);
        rel.setToCiId(cloud.getCiId());
        rel.setToRfcCi(ci);

        bomCIs.add(ci);
        bomRels.add(rel);

        return rel;
    }

    private CmsRfcRelation createComputeRfc(long ciId, String rfcAction, String size, CmsCI cloud) {
        CmsRfcRelation rel = createRfc(ciId, rfcAction, "bom.oneops.1.Compute", cloud);
        CmsRfcAttribute attribute = new CmsRfcAttribute();
        attribute.setAttributeName("size");
        attribute.setNewValue(size);
        attribute.setOldValue(size);
        rel.getToRfcCi().addAttribute(attribute);

        return rel;
    }
}
