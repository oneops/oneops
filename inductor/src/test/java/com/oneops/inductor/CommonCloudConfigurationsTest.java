package com.oneops.inductor;

import com.oneops.inductor.util.JSONUtils;
import com.oneops.inductor.util.ResourceUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

import static com.oneops.inductor.InductorConstants.CLOUD_CONFIG_FILE_PATH;

public class CommonCloudConfigurationsTest {

  private static CommonCloudConfigurationsHelper commonCloudConfigurationsHelper;
  private static Map<String, Object> cloudConfig;

  @BeforeClass
  public static void init() {
    commonCloudConfigurationsHelper = new CommonCloudConfigurationsHelper(Logger.getLogger(CommonCloudConfigurationsTest.class.getName()), "");
    String jsonContent = ResourceUtils.readResourceAsString(CLOUD_CONFIG_FILE_PATH);
    cloudConfig = JSONUtils.convertJsonToMap(jsonContent);
  }

  @Test
  public void testConfigurationsAtOrgLevelWhenOrgMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtOrgLevel(cloudConfig, "org", "cloud-exact");
    Assert.assertTrue(servicesMap.toString().contains("org & cloud exact match"));
  }

  @Test
  public void testConfigurationsAtOrgLevelWhenOrgNotMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtOrgLevel(cloudConfig, "org1", "cloud-exact");
    Assert.assertTrue(servicesMap.isEmpty());
  }

  @Test
  public void testConfigurationsWhenCloudExactMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, "cloud-exact");
    Assert.assertTrue(servicesMap.toString().contains("cloud exact match"));
  }

  @Test
  public void testConfigurationsWhenCloudRegexMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, "cloud-regex123");
    Assert.assertTrue(servicesMap.toString().contains("cloud regex match"));
  }

  @Test
  public void testConfigurationsWhenCloudNotMatch() {
    Map<String, Object> servicesMap = commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, "cloud-exact123");
    Assert.assertTrue(servicesMap.isEmpty());
  }

  @Test
  public void testConfigurationsWhenServicesFound() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service");
    Assert.assertTrue(!classesMap.isEmpty());
  }

  @Test
  public void testConfigurationsWhenServicesNotFound() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service2");
    Assert.assertTrue(classesMap.isEmpty());
  }

  @Test
  public void testCiAttributesFound() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname");
    Assert.assertTrue(ciAttributes.get("attr1").toString().equals("service match"));
  }

  @Test
  public void testCiAttributesNotFound() {
    Map<String, Object> classesMap = commonCloudConfigurationsHelper.findServiceClasses(cloudConfig, "service");
    Map<String, Object> ciAttributes = commonCloudConfigurationsHelper.findClassCiAttributes(classesMap, "classname2");
    Assert.assertTrue(ciAttributes.isEmpty());
  }
}