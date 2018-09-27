/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.inductor;

import java.io.StringReader;
import java.util.*;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.inductor.util.JSONUtils;
import com.oneops.inductor.util.ResourceUtils;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

import com.oneops.cms.simple.domain.CmsRfcCISimple;

import junit.framework.Assert;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;

import static com.oneops.inductor.InductorConstants.CLOUD_CONFIG_FILE_PATH;
import static com.oneops.inductor.util.ResourceUtils.readResourceAsString;

public class WoExecutorTest {

	CommonCloudConfigurationsHelper commonCloudConfigurationsHelper;
	Map<String, Object> cloudConfig;
	WorkOrderExecutor woExecutor;
	private static String localWo;
	final protected Gson gson = new Gson();

	@Before
	public void init() {
		Config config = new Config();
		config.setCircuitDir("/opt/oneops/inductor/packer");
		woExecutor = new WorkOrderExecutor(config, null);
		localWo = readResourceAsString("/localWorkOrder.json");

		commonCloudConfigurationsHelper = new CommonCloudConfigurationsHelper(Logger.getLogger(CommonCloudConfigurationsTest.class.getName()), "");
		String jsonContent = ResourceUtils.readResourceAsString(CLOUD_CONFIG_FILE_PATH);
		cloudConfig = JSONUtils.convertJsonToMap(jsonContent);
	}
	
	@Test
	public void testExtraRunList4Wo() {
		List<String> classes = new ArrayList<>();
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Security");
		classes.add("cloud.compliance.Dummy");
		classes.add("cloud.compliance.Dummy");
		CmsWorkOrderSimple wo = new CmsWorkOrderSimple();
		wo.putPayLoadEntry(InductorConstants.EXTRA_RUN_LIST,getRfcCiForExtraRunList(classes));
		CmsRfcCISimple rfci = new CmsRfcCISimple();
		rfci.setRfcAction("add");
		wo.setRfcCi(rfci);
		List<String> runList = woExecutor.getExtraRunListClasses(wo);

		Assert.assertFalse(runList.isEmpty());
		Assert.assertEquals(2, runList.size());

		String securityRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "security" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		String dummyRecipe = AbstractOrderExecutor.RUN_LIST_PREFIX + "dummy" + AbstractOrderExecutor.RUN_LIST_SEPARATOR + 
				"add" + AbstractOrderExecutor.RUN_LIST_SUFFIX;
		Assert.assertTrue(runList.contains(securityRecipe));
		Assert.assertTrue(runList.contains(dummyRecipe));
		
		classes = null;
        wo.putPayLoadEntry(InductorConstants.EXTRA_RUN_LIST,getRfcCiForExtraRunList(classes));

        List<String> runList1 = woExecutor.getExtraRunListClasses(wo);
		Assert.assertTrue(runList1.isEmpty());
	}
	
	private List<CmsRfcCISimple> getRfcCiForExtraRunList(List<String> classes) {
		List<CmsRfcCISimple> list = new ArrayList<>();
		if (classes != null) {
			for (String clazz : classes) {
				CmsRfcCISimple rfc = new CmsRfcCISimple();
				rfc.setCiClassName(clazz);
				list.add(rfc);
			}
		}
		return list;
	}

	@Test
	public void testCreateCookbookSearchPath() {
		String cloudName = "stg-dfw1";
		Map<String, Map<String, CmsCISimple>> cloudServices = new HashMap<>();
		Map<String, CmsCISimple> serviceCis = new HashMap<>();
		CmsCISimple cloudServiceCi = new CmsCISimple();
		cloudServiceCi.setCiClassName("cloud.service.oneops.1.Keywhiz-cloud-service");
		serviceCis.put(cloudName, cloudServiceCi);
		cloudServices.put("secret", serviceCis);

		LinkedHashSet<String> searchPaths = woExecutor.createCookbookSearchPath("circuit-main-1", cloudServices, cloudName);
		LinkedHashSet<String> expectedResult = new LinkedHashSet<>();

		expectedResult.add("/opt/oneops/inductor/circuit-oneops-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/circuit-main-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/shared/cookbooks");

		Assert.assertEquals(gson.toJson(searchPaths), gson.toJson(expectedResult));

		//now try the case where there is no cloud service involved
		searchPaths = woExecutor.createCookbookSearchPath("circuit-main-1", new HashMap<>(), cloudName);
		expectedResult = new LinkedHashSet<>();

		expectedResult.add("/opt/oneops/inductor/circuit-main-1/components/cookbooks");
		expectedResult.add("/opt/oneops/inductor/shared/cookbooks");

		Assert.assertEquals(gson.toJson(searchPaths), gson.toJson(expectedResult));
	}

  @Test
  public void testWorkorderCiAttributesReplace() {
    CmsWorkOrderSimple cmsWorkorder;
    cmsWorkorder = (CmsWorkOrderSimple) getWorkOrderOf(localWo, CmsWorkOrderSimple.class);
    String cloudName = getCloudName(cmsWorkorder);
    String orgName = getOrganizationName(cmsWorkorder);
    final Map<String, Object> servicesMap = woExecutor.getServicesMap(commonCloudConfigurationsHelper, cloudConfig, cloudName, orgName);
    woExecutor.updateCiAttributes(cmsWorkorder, commonCloudConfigurationsHelper, servicesMap);
    org.junit.Assert.assertEquals("service match", cmsWorkorder.getServices().get("service").get("stub-dfw2b").getCiAttributes().get("attr1"));
  }


  private String getOrganizationName(CmsWorkOrderSimple wo) {
    String orgName = "";
    if (wo.getPayLoad().containsKey("Organization")) {
      if (!wo.getPayLoad().get("Organization").isEmpty()) {
        orgName = wo.getPayLoad().get("Organization").get(0).getCiName();
      }
    }
    return orgName;
  }

  private String getCloudName(CmsWorkOrderSimple wo) {
    return wo.getCloud().getCiName();
  }

  private CmsWorkOrderSimpleBase getWorkOrderOf(String msgText, Class c) {
    CmsWorkOrderSimpleBase wo;
    JsonReader reader = new JsonReader(new StringReader(msgText));
    reader.setLenient(true);
    wo = gson.fromJson(reader, c);
    return wo;
  }

  @Test
  public void testOrgLevelWhenOrgMatchCloudExactMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtOrgLevel(cloudConfig, "org", "cloud-exact");
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(servicesMap, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertEquals("org & cloud exact match", ciAttributes.get("attr1"));
  }

  @Test
  public void testOrgLevelWhenOrgMatchCloudRegexMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtOrgLevel(cloudConfig, "org", "cloud-regex123");
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(servicesMap, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertEquals("org & cloud regex match", ciAttributes.get("attr1"));
  }

  @Test
  public void testCloudLevelWhenCloudExactMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, "cloud-exact");
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(servicesMap, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertEquals("cloud exact match", ciAttributes.get("attr1"));
  }

  @Test
  public void testCloudLevelWhenCloudRegexMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, "cloud-regex123");
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(servicesMap, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertEquals("cloud regex match", ciAttributes.get("attr1"));
  }

  @Test
  public void testServicesLevelWhenServiceMatchAndAttrMatch() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertEquals("service match", ciAttributes.get("attr1"));
  }

  @Test
  public void testServicesLevelWhenServiceMatchAndAttrNotMatch() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    org.junit.Assert.assertNotEquals("service not match", ciAttributes.get("attr1"));
  }
}
