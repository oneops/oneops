/**
 * *****************************************************************************
 *
 * <p>Copyright 2015 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.controller.cms;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.collections.CollectionProcessor;
import com.oneops.cms.collections.def.CollectionLinkDefinition;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.util.CmsConstants;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.JMSException;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class WoProviderTest {

  private static ApplicationContext context;

  private CmsCmProcessor cmProcessor;
  private CmsCmRfcMrgProcessor cmsCmRfcMrgProcessor;
  private CmsWoProvider woProvider;
  private CollectionProcessor colProcessor;

  private Gson gson = new Gson();

  private static final String EXPR_WO = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";

  private static final String EXPR_AO =
      "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Compute' and ciAttributes['ostype'] == 'centos-7.0')";

  @BeforeClass
  public void setUp() throws JMSException {
    context = new ClassPathXmlApplicationContext("**/test-wo-context.xml");
    cmProcessor = context.getBean(CmsCmProcessor.class);
    woProvider = context.getBean(CmsWoProvider.class);
    cmsCmRfcMrgProcessor = context.getBean(CmsCmRfcMrgProcessor.class);
    colProcessor = context.getBean(CollectionProcessor.class);
    woProvider.setGslbWeightsEnabled(true);
  }

  @Test
  public void testGetWorkOrderManagedViaPayload() {
    long computeBomCiId = 1235;
    long computeManifestCiId = 1234;
    long managedViaTemplateCiId = 1233;

    long userBomCiId = 2235;
    long userManifestCiId = 2234;
    long userTemplateCiId = 2233;

    CmsRfcCI managedViaCompute = new CmsRfcCI(); // bom compute
    managedViaCompute.setCiId(computeBomCiId);
    managedViaCompute.setCiClassName("bom.Compute");

    CmsRfcRelation relation = new CmsRfcRelation();
    relation.setToRfcCi(managedViaCompute);

    List<CmsRfcRelation> managedViaRels = new ArrayList<>();
    managedViaRels.add(relation);

    when(cmsCmRfcMrgProcessor.getFromCIRelations(
            anyLong(), Mockito.eq("bom.ManagedVia"), anyString(), anyString()))
        .thenReturn(managedViaRels);

    // mock the cmsdal realizedAs call to get the manifest ci for compute bom
    List<CmsCIRelation> manifestComputeRelations = new ArrayList<>();
    CmsCIRelation manifestComputeRelation = new CmsCIRelation();
    manifestComputeRelation.setFromCiId(computeManifestCiId);
    manifestComputeRelations.add(manifestComputeRelation);

    when(cmProcessor.getToCIRelationsNakedNoAttrs(
            anyLong(), Mockito.eq("base.RealizedAs"), anyString(), anyString()))
        .thenReturn(manifestComputeRelations);

    // mock cmsdal getPayloadDef
    CollectionLinkDefinition collLinkDef = new CollectionLinkDefinition();
    collLinkDef.setTargetCiName("os"); // just to indentify

    List<CmsCIRelation> managedViasPayloadRels = new ArrayList<>();
    CmsCIRelation managedViasPayloadRel = new CmsCIRelation();
    CmsCI payloadDef = new CmsCI();
    payloadDef.setCiName("os");
    CmsCIAttribute payloadDefString = new CmsCIAttribute();
    payloadDefString.setAttributeName("definition");
    payloadDefString.setDfValue(gson.toJson(collLinkDef));
    payloadDef.addAttribute(payloadDefString);
    managedViasPayloadRel.setToCi(payloadDef);
    managedViasPayloadRels.add(managedViasPayloadRel);

    when(cmProcessor.getFromCIRelations(
            managedViaTemplateCiId, "mgmt.manifest.Payload", "mgmt.manifest.Qpath"))
        .thenReturn(managedViasPayloadRels);

    // now for user ci payload
    CollectionLinkDefinition userCollLinkDef = new CollectionLinkDefinition();
    userCollLinkDef.setTargetCiName("userPayload"); // just to indentify

    List<CmsCIRelation> userPayloadRels = new ArrayList<>();
    CmsCIRelation userPayloadRel = new CmsCIRelation();
    CmsCI userPayloadDef = new CmsCI();
    userPayloadDef.setCiName("userPayload");
    CmsCIAttribute userPayloadDefString = new CmsCIAttribute();
    userPayloadDefString.setAttributeName("definition");
    userPayloadDefString.setDfValue(gson.toJson(userCollLinkDef));
    userPayloadDef.addAttribute(userPayloadDefString);
    userPayloadRel.setToCi(userPayloadDef);
    userPayloadRels.add(userPayloadRel);
    when(cmProcessor.getFromCIRelations(
            userTemplateCiId, "mgmt.manifest.Payload", "mgmt.manifest.Qpath"))
        .thenReturn(userPayloadRels);

    // colprocessor
    List<CmsRfcCI> osCis = new ArrayList<>();
    CmsRfcCI osCi = new CmsRfcCI();
    osCis.add(osCi);
    osCi.setCiName("os-bom-ci");

    // payload result for user ci
    List<CmsRfcCI> userCis = new ArrayList<>();
    CmsRfcCI userCi = new CmsRfcCI();
    userCis.add(userCi);
    userCi.setCiName("user-bom-ci");
    userCi.setCiId(userBomCiId);

    when(colProcessor.getFlatCollectionRfc(
            anyLong(), argThat(new OsCollectionLinkDefinitionMatcher())))
        .thenReturn(osCis);
    when(colProcessor.getFlatCollectionRfc(
            anyLong(), argThat(new UserCollectionLinkDefinitionMatcher())))
        .thenReturn(userCis);

    CmsWorkOrder wo = new CmsWorkOrder();
    HashMap<Long, CmsCI> manifestToTemplateMap = new HashMap<>();
    CmsRfcCI userManifestRfcCi = new CmsRfcCI();

    userManifestRfcCi.setCiId(userManifestCiId);
    wo.addPayLoadEntry("RealizedAs", userManifestRfcCi);

    CmsCI userManifestCi = new CmsCI();
    userManifestCi.setCiId(userManifestCiId);
    CmsCI userTemplateCi = new CmsCI();
    userTemplateCi.setCiId(userTemplateCiId);
    // mock the cmsdal getTemplateObj
    CmsCI managedViaTemplateCi = new CmsCI();
    managedViaTemplateCi.setCiId(managedViaTemplateCiId);
    when(cmProcessor.getTemplateObjForManifestObj(Mockito.eq(userManifestCi), anyObject()))
        .thenReturn(userTemplateCi);
    when(cmProcessor.getTemplateObjForManifestObj(
            argThat(new ComputeManifestCiMatcher()), anyObject()))
        .thenReturn(managedViaTemplateCi);

    when(cmProcessor.getCiById(userManifestCiId)).thenReturn(userManifestCi);

    HashMap<String, String> vars = new HashMap<>();
    wo.setRfcCi(userCi);
    woProvider.processCustomPayloads(wo, manifestToTemplateMap, null, vars, vars, vars);
    Assert.assertEquals(wo.getPayLoad().get("os").get(0).getCiName(), "os-bom-ci");
    Assert.assertEquals(wo.getPayLoad().get("userPayload").get(0).getCiName(), "user-bom-ci");

    // now assert that the os payload is not overwritten if the payloadName is same for current ci
    // and its compute
    wo = new CmsWorkOrder();
    wo.setRfcCi(userCi);
    manifestToTemplateMap = new HashMap<>();
    wo.addPayLoadEntry("RealizedAs", userManifestRfcCi);
    userPayloadDef.setCiName("os");
    woProvider.processCustomPayloads(wo, manifestToTemplateMap, null, vars, vars, vars);
    Assert.assertEquals(wo.getPayLoad().get("os").get(0).getCiName(), "user-bom-ci");
  }

  public class ComputeManifestCiMatcher extends ArgumentMatcher<CmsCI> {
    @Override
    public boolean matches(Object object) {
      if (object instanceof CmsCI) {
        CmsCI obj = (CmsCI) object;
        return obj.getCiId() == 1234;
      }
      return false;
    }
  }

  public class OsCollectionLinkDefinitionMatcher extends ArgumentMatcher<CollectionLinkDefinition> {
    @Override
    public boolean matches(Object object) {
      if (object instanceof CollectionLinkDefinition) {
        CollectionLinkDefinition obj = (CollectionLinkDefinition) object;
        return obj.getTargetCiName().equalsIgnoreCase("os");
      }
      return false;
    }
  }

  public class UserCollectionLinkDefinitionMatcher
      extends ArgumentMatcher<CollectionLinkDefinition> {
    @Override
    public boolean matches(Object object) {
      if (object instanceof CollectionLinkDefinition) {
        CollectionLinkDefinition obj = (CollectionLinkDefinition) object;
        return obj.getTargetCiName().equalsIgnoreCase("userPayload");
      }
      return false;
    }
  }

  @Test
  public void testWoComplianceObject() {
    CmsWorkOrder wo = getTestWorkOrder();
    List<CmsCIRelation> list = new ArrayList<>();
    String version = "1.0";
    list.add(createComplianceRelForExpr(EXPR_WO, "true", version));

    when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString()))
        .thenReturn(list);
    List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
    Assert.assertNotNull(complList);
    Assert.assertEquals(complList.size(), 1);
    CmsRfcAttribute filterAttr =
        complList.get(0).getAttribute(ExpressionEvaluator.ATTR_NAME_FILTER);
    Assert.assertNotNull(filterAttr);
    Assert.assertEquals(filterAttr.getNewValue(), EXPR_WO);
    CmsRfcAttribute versionAttr = complList.get(0).getAttribute("version");
    Assert.assertNotNull(versionAttr);
    Assert.assertEquals(versionAttr.getNewValue(), version);
  }

  @Test
  public void testAoComplianceObject() {
    CmsActionOrder ao = getTestActionOrder();
    List<CmsCIRelation> list = new ArrayList<>();
    String version = "1.0";
    list.add(createComplianceRelForExpr(EXPR_AO, "true", version));

    when(cmProcessor.getFromCIRelations(eq(ao.getCloud().getCiId()), anyString(), anyString()))
        .thenReturn(list);
    List<CmsCI> complList = woProvider.getMatchingCloudCompliance(ao);
    Assert.assertNotNull(complList);
    Assert.assertEquals(complList.size(), 1);
    CmsCIAttribute filterAttr = complList.get(0).getAttribute(ExpressionEvaluator.ATTR_NAME_FILTER);
    Assert.assertNotNull(filterAttr);
    Assert.assertEquals(filterAttr.getDfValue(), EXPR_AO);
    CmsCIAttribute versionAttr = complList.get(0).getAttribute("version");
    Assert.assertNotNull(versionAttr);
    Assert.assertEquals(versionAttr.getDfValue(), version);
  }

  @Test
  public void testAoWithComplianceDisabled() {
    CmsActionOrder ao = getTestActionOrder();
    List<CmsCIRelation> list = new ArrayList<>();
    String version = "1.0";
    list.add(createComplianceRelForExpr(EXPR_AO, "false", version));

    when(cmProcessor.getFromCIRelations(eq(ao.getCloud().getCiId()), anyString(), anyString()))
        .thenReturn(list);
    List<CmsCI> complList = woProvider.getMatchingCloudCompliance(ao);
    Assert.assertNotNull(complList);
    Assert.assertEquals(complList.size(), 1);
  }

  @Test
  public void testWoWithComplianceDisabed() {
    CmsWorkOrder wo = getTestWorkOrder();
    List<CmsCIRelation> list = new ArrayList<>();
    String expr = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";
    String version = "1.0";
    list.add(createComplianceRelForExpr(expr, "false", version));

    when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString()))
        .thenReturn(list);
    List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
    Assert.assertTrue(complList != null && complList.size() == 0);
  }

  @Test
  public void testWoWithAutoComplyDisabled() {
    CmsWorkOrder wo = getTestWorkOrder();
    List<CmsCIRelation> list = new ArrayList<>();
    String expr = "(ciClassName matches 'bom(\\..*\\.[0-9]+)?\\.Os')";
    String version = "1.0";
    list.add(createComplianceRelForExpr(expr, "false", version));

    // disable auto comply in rfc
    wo.getBox().getAttribute(CmsConstants.ATTR_NAME_AUTO_COMPLY).setDfValue("false");
    wo.getBox().getAttribute(CmsConstants.ATTR_NAME_AUTO_COMPLY).setDjValue("false");

    when(cmProcessor.getFromCIRelations(eq(wo.getCloud().getCiId()), anyString(), anyString()))
        .thenReturn(list);
    List<CmsRfcCI> complList = woProvider.getMatchingCloudCompliance(wo);
    Assert.assertTrue(complList != null && complList.size() == 0);
  }

  private CmsWorkOrder getTestWorkOrder() {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-wo.json");
    JsonParser parser = new JsonParser();
    JsonElement jsonElement = (JsonElement) parser.parse(new InputStreamReader(is));
    return gson.fromJson(jsonElement, CmsWorkOrder.class);
  }

  private CmsActionOrder getTestActionOrder() {
    InputStream is = this.getClass().getClassLoader().getResourceAsStream("test-ao.json");
    JsonParser parser = new JsonParser();
    JsonElement jsonElement = (JsonElement) parser.parse(new InputStreamReader(is));
    return gson.fromJson(jsonElement, CmsActionOrder.class);
  }

  private CmsCIRelation createComplianceRelForExpr(String expr, String enabled, String version) {

    CmsCI exprCi = new CmsCI();
    exprCi.setCiClassName("base.Compliance");
    CmsCIAttribute filterAttribute = new CmsCIAttribute();
    filterAttribute.setAttributeName(ExpressionEvaluator.ATTR_NAME_FILTER);
    filterAttribute.setDjValue(expr);
    filterAttribute.setDfValue(expr);
    exprCi.addAttribute(filterAttribute);

    CmsCIAttribute enabledAttribute = new CmsCIAttribute();
    enabledAttribute.setAttributeName(CmsConstants.ATTR_NAME_ENABLED);
    enabledAttribute.setDjValue(enabled);
    enabledAttribute.setDfValue(enabled);
    exprCi.addAttribute(enabledAttribute);

    CmsCIAttribute versionAttribute = new CmsCIAttribute();
    versionAttribute.setAttributeName("version");
    versionAttribute.setDjValue(version);
    versionAttribute.setDfValue(version);
    exprCi.addAttribute(versionAttribute);
    exprCi.setNsPath("/org1/test");

    CmsCIRelation rel = new CmsCIRelation();
    rel.setToCi(exprCi);
    return rel;
  }

  public void addAttribute(CmsRfcCI rfc, String name, String newValue) {
    CmsRfcAttribute attribute = new CmsRfcAttribute();
    attribute.setAttributeName(name);
    attribute.setNewValue(newValue);
    rfc.addAttribute(attribute);
  }

  private CmsWorkOrder baseFqdnWo() {
    CmsWorkOrder wo = new CmsWorkOrder();
    CmsRfcCI rfc = new CmsRfcCI();
    rfc.setCiName("fqdn-1000-1");
    rfc.setCiClassName("bom.Fqdn");
    addAttribute(rfc, "service_type", "torbit");
    wo.setRfcCi(rfc);
    return wo;
  }

  private void enableGslbWeight() {
		when(cmProcessor.getVarByMatchingCriteriaBoolean(eq("GSLB_WEIGHTS_ENABLED"), anyString()))
				.thenReturn(true);
	}

  @Test
  public void fqdnWeightsForTwoPrimaryClouds() {
    CmsWorkOrder wo = baseFqdnWo();
    addTwoPrimaryCloudsPayload(wo);
    List<CmsCIRelation> deployedToRelations = new ArrayList<>();

    CmsCI cloud1 = new CmsCI();
    cloud1.setCiName("cl1");
    CmsCIRelation rel1 = new CmsCIRelation();
    rel1.setToCi(cloud1);
    deployedToRelations.addAll(relations(rel1, 5));

    CmsCI cloud2 = new CmsCI();
    cloud2.setCiName("cl2");
    CmsCIRelation rel2 = new CmsCIRelation();
    rel2.setToCi(cloud2);
    deployedToRelations.addAll(relations(rel2, 5));

    when(cmProcessor.getCIRelationsWithToCIAndNoAttrs(
            anyString(), eq("base.DeployedTo"), anyString(), eq("Compute"), anyString()))
        .thenReturn(deployedToRelations);
		enableGslbWeight();

    woProvider.addComponentConfig(wo);
    assertTrue(wo.getConfig() != null && wo.getConfig().containsKey("weights"));
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson(wo.getConfig().get("weights"), type);
    assertEquals(map.get("cl1").intValue(), 50);
    assertEquals(map.get("cl2").intValue(), 50);
  }

  @Test
  public void fqdnWeightsForPrimarySecondary() {
    CmsWorkOrder wo = baseFqdnWo();
    addPrimarySecondaryCloudsPayload(wo);
    List<CmsCIRelation> deployedToRelations = new ArrayList<>();

    CmsCI cloud1 = new CmsCI();
    cloud1.setCiName("cl1");
    CmsCIRelation rel1 = new CmsCIRelation();
    rel1.setToCi(cloud1);
    deployedToRelations.addAll(relations(rel1, 3));

    CmsCI cloud2 = new CmsCI();
    cloud2.setCiName("cl2");
    CmsCIRelation rel2 = new CmsCIRelation();
    rel2.setToCi(cloud2);
    deployedToRelations.addAll(relations(rel2, 3));

    when(cmProcessor.getCIRelationsWithToCIAndNoAttrs(
            anyString(), eq("base.DeployedTo"), anyString(), eq("Compute"), anyString()))
        .thenReturn(deployedToRelations);
		enableGslbWeight();

    woProvider.addComponentConfig(wo);
    assertTrue(wo.getConfig() != null && wo.getConfig().containsKey("weights"));
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson(wo.getConfig().get("weights"), type);
    assertEquals(map.get("cl1").intValue(), 100);
  }

  @Test
  public void fqdnWeightsWithMultiClouds() {
    CmsWorkOrder wo = baseFqdnWo();
    addCloudsPayload(wo, 3, 2);

    List<CmsCIRelation> deployedToRelations = new ArrayList<>();

    CmsCI cloud1 = new CmsCI();
    cloud1.setCiName("cl1");
    CmsCIRelation rel1 = new CmsCIRelation();
    rel1.setToCi(cloud1);
    deployedToRelations.addAll(relations(rel1, 3));

    cloud1 = new CmsCI();
    cloud1.setCiName("cl2");
    rel1 = new CmsCIRelation();
    rel1.setToCi(cloud1);
    deployedToRelations.addAll(relations(rel1, 6));

    cloud1 = new CmsCI();
    cloud1.setCiName("cl3");
    rel1 = new CmsCIRelation();
    rel1.setToCi(cloud1);
    deployedToRelations.addAll(relations(rel1, 4));

    int k = 4;
    for (int i = 0; i < 2; i++) {
      CmsCI cloud2 = new CmsCI();
      cloud2.setCiName("cl" + k++);
      CmsCIRelation rel2 = new CmsCIRelation();
      rel2.setToCi(cloud2);
      deployedToRelations.addAll(relations(rel2, 2));
    }
    when(cmProcessor.getCIRelationsWithToCIAndNoAttrs(
            anyString(), eq("base.DeployedTo"), anyString(), eq("Compute"), anyString()))
        .thenReturn(deployedToRelations);

		enableGslbWeight();
    woProvider.addComponentConfig(wo);
    assertTrue(wo.getConfig() != null && wo.getConfig().containsKey("weights"));
    Type type = new TypeToken<Map<String, Integer>>() {}.getType();
    Map<String, Integer> map = gson.fromJson(wo.getConfig().get("weights"), type);
    assertEquals(map.get("cl1").intValue(), 23);
    assertEquals(map.get("cl2").intValue(), 47);
    assertEquals(map.get("cl3").intValue(), 30);
  }

  private void addTwoPrimaryCloudsPayload(CmsWorkOrder wo) {
    CmsRfcCI cl1 = new CmsRfcCI();
    cl1.setCiName("cl1");
    cl1.setCiId(200);
    addAttribute(cl1, "base.Consumes.priority", "1");
    addAttribute(cl1, "base.Consumes.adminstatus", "active");
    cl1.setCiClassName("account.Cloud");

    CmsRfcCI cl2 = new CmsRfcCI();
    cl2.setCiName("cl2");
    cl2.setCiId(202);
    addAttribute(cl2, "base.Consumes.priority", "1");
    addAttribute(cl2, "base.Consumes.adminstatus", "active");
    cl2.setCiClassName("account.Cloud");

    wo.putPayLoadEntry("fqdnclouds", Arrays.asList(cl1, cl2));
  }

  private void addPrimarySecondaryCloudsPayload(CmsWorkOrder wo) {
    CmsRfcCI cl1 = new CmsRfcCI();
    cl1.setCiName("cl1");
    cl1.setCiId(200);
    addAttribute(cl1, "base.Consumes.priority", "1");
    addAttribute(cl1, "base.Consumes.adminstatus", "active");
    cl1.setCiClassName("account.Cloud");

    CmsRfcCI cl2 = new CmsRfcCI();
    cl2.setCiName("cl2");
    cl2.setCiId(202);
    addAttribute(cl2, "base.Consumes.priority", "2");
    addAttribute(cl2, "base.Consumes.adminstatus", "active");
    cl2.setCiClassName("account.Cloud");

    wo.putPayLoadEntry("fqdnclouds", Arrays.asList(cl1, cl2));
  }

  private void addCloudsPayload(CmsWorkOrder wo, int primary, int secondary) {
    List<CmsRfcCI> clouds = new ArrayList<>();
    int k = 1;
    for (int i = 0; i < primary; i++) {
      CmsRfcCI cl1 = new CmsRfcCI();
      cl1.setCiName("cl" + k++);
      cl1.setCiId(200);
      addAttribute(cl1, "base.Consumes.priority", "1");
      addAttribute(cl1, "base.Consumes.adminstatus", "active");
      cl1.setCiClassName("account.Cloud");
      clouds.add(cl1);
    }

    for (int i = 0; i < secondary; i++) {
      CmsRfcCI cl2 = new CmsRfcCI();
      cl2.setCiName("cl" + k++);
      cl2.setCiId(202);
      addAttribute(cl2, "base.Consumes.priority", "2");
      addAttribute(cl2, "base.Consumes.adminstatus", "active");
      cl2.setCiClassName("account.Cloud");
    }

    wo.putPayLoadEntry("fqdnclouds", clouds);
  }

  private List<CmsCIRelation> relations(CmsCIRelation rel1, int size) {
    List<CmsCIRelation> list = new ArrayList<>();
    list.add(rel1);
    while (list.size() < size) {
      list.add(rel1);
    }
    return list;
  }
}
