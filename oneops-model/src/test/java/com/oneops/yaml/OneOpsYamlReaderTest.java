package com.oneops.yaml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.oneops.Assembly;
import com.oneops.Attachment;
import com.oneops.Cloud;
import com.oneops.Component;
import com.oneops.Environment;
import com.oneops.OneOps;
import com.oneops.Platform;
import com.oneops.Template;
import com.oneops.TestCase;
import com.oneops.boo.BooAttachment;
import com.oneops.boo.BooCloud;
import com.oneops.boo.BooEnvironment;
import com.oneops.boo.BooModelTransformer;
import com.oneops.boo.BooPlatform;
import com.oneops.boo.BooScale;
import com.oneops.boo.BooYaml;

public class OneOpsYamlReaderTest {

  private String basedir;

  @Before
  public void beforeTests() {
    basedir = System.getProperty("basedir", new File("").getAbsolutePath());
  }

  @Test
  public void validateReadingTestYaml() throws Exception {
    OneOpsYamlReader reader = new OneOpsYamlReader();
    TestCase testCaseYaml = reader.read(yaml("src/test/yaml/oo-v2", "testcase.yaml"), TestCase.class);

    assertEquals("Evaluate optional component add/delete", testCaseYaml.getDescription());
    assertEquals("false", testCaseYaml.getRandomcloud());

    assertEquals("pulldesign", testCaseYaml.getTags().get(0));

    assertTrue(testCaseYaml.getClouds().keySet().contains("cloud0"));
    List<Cloud> clouds = Lists.newArrayList(testCaseYaml.getClouds().values());
    assertEquals(1, clouds.get(0).getDeploymentorder());

    assertTrue(testCaseYaml.getTemplates().keySet().contains("template-1.yaml"));
    List<Template> templates = Lists.newArrayList(testCaseYaml.getTemplates().values());
    Template template = templates.get(0);
    assertEquals("evaluate add of optional component user-temp", template.getAssertions().get("addusercomponent").getMessage());
  }


  @Test
  public void validateReadingTestOldBooYaml1() throws Exception {
    OneOpsYamlReader reader = new OneOpsYamlReader();
    BooYaml booYaml = reader.read(yaml("src/test/yaml/boo", "assembly.yaml"), BooYaml.class);
    assertEquals(booYaml.getBoo().getEnvironment_name(), "environment_name");
    Map<String, String> tags = Maps.newHashMap();
    tags.put("tag-key1", "tag-value1");
    tags.put("tag-key2", "tag-value2");
    tags.put("tag-key3", "tag-value3");
    tags.put("tag-key4", "tag-value4");
    assertEquals(booYaml.getAssembly().getTags(), tags);

    List<BooPlatform> platformList = booYaml.getPlatformList();
    BooPlatform booPlatform0 = platformList.get(0);
    assertEquals(booPlatform0.getName(), "platform-0");
    assertEquals(booPlatform0.getPack(), "pack-platform-0");
    assertEquals(booPlatform0.getSource(), "source");

    Map<String, Map<String, Object>> components = booPlatform0.getComponents();
    Map<String, Object> component0 = components.get("component-0");
    assertEquals(component0.get("config-0"), "platform-0-config-0-value");

    Map<String, BooAttachment> attachments = booPlatform0.getAttachments();
    BooAttachment booAttachment = attachments.get("component-0");
    assertEquals(booAttachment.getName(), "attachment1");

    Map<String, Object> user = components.get("user");
    Map<String, String> userComp = Maps.newHashMap();
    userComp.put("username", "user1");
    userComp.put("authorized_keys", "[\"\"]");
    assertEquals(user.get("user1"), userComp);

    List<BooScale> scaleList = booYaml.getScaleList();
    BooScale booScale = scaleList.get(0);
    assertEquals(booScale.getPlatformName(), "platform-0");
    assertEquals(booScale.getComponentName(), "compute");
    assertEquals(booScale.getMin(), "2");
    assertEquals(booScale.getCurrent(), "2");

    List<BooEnvironment> environmentList = booYaml.getEnvironmentList();
    BooEnvironment environment0 = environmentList.get(0);
    assertEquals(environment0.getName(), booYaml.getBoo().getEnvironment_name());
    assertEquals(environment0.getProfile(), "DEV");

    List<BooCloud> cloudList = environment0.getCloudList();
    BooCloud cloud0 = cloudList.get(0);
    assertEquals(cloud0.getName(), "dev-cdc002");
    assertEquals(cloud0.getPriority(), "1");
    assertEquals(cloud0.getDpmt_order(), "1");
    assertEquals(cloud0.getPct_scale(), "30");

    List<BooPlatform> ep0 = environment0.getPlatformList();
    BooPlatform ep = ep0.get(0);
    assertEquals(ep.getName(), "platform-0");
    Map<String, String> autoHealing = Maps.newHashMap();
    autoHealing.put("autorepair", "false");
    autoHealing.put("autoreplace", "true");
    autoHealing.put("replace_after_minutes", "60");
    autoHealing.put("replace_after_repairs", "4");
    assertEquals(ep.getAuto_healing(), autoHealing);

    Map<String, Map<String, Object>> epc0 = ep.getComponents();
    Map<String, Object> lbConfig = epc0.get("lb");
    assertEquals(lbConfig.get("ecv_map"), "{\"8080\":\"GET /\"}");

    BooModelTransformer bmt = new BooModelTransformer();
    OneOps oneops = bmt.convert(booYaml);

    assertEquals("auth-key", oneops.getApikey());
    assertEquals("https://localhost:9000/", oneops.getHost());
    assertEquals("local-org", oneops.getOrganization());

    Assembly assembly = oneops.getAssembly();
    // Assembly
    assertEquals("assembly-name", assembly.getName());

  }

  @Test
  public void validateReadingTestOldBooYaml2() throws Exception {
    OneOpsYamlReader reader = new OneOpsYamlReader();
    BooYaml booYaml = reader.read(yaml("src/test/yaml/boo", "assembly2.yaml"), BooYaml.class);
    assertEquals(booYaml.getBoo().getEnvironment_name(), "environment_name");
    Map<String, String> tags = Maps.newHashMap();
    tags.put("tag-key1", "tag-value1");
    tags.put("tag-key2", "tag-value2");
    tags.put("tag-key3", "tag-value3");
    tags.put("tag-key4", "tag-value4");
    assertEquals(booYaml.getAssembly().getTags(), tags);

    List<BooPlatform> platformList = booYaml.getPlatformList();
    BooPlatform booPlatform0 = platformList.get(0);
    assertEquals(booPlatform0.getName(), "platform-0");
    assertEquals(booPlatform0.getPack(), "pack-platform-0");
    assertEquals(booPlatform0.getSource(), "source");

    Map<String, Map<String, Object>> components = booPlatform0.getComponents();
    Map<String, Object> component0 = components.get("component-0");
    assertEquals(component0.get("config-0"), "platform-0-config-0-value");

    Map<String, Object> user = components.get("user");
    Map<String, String> userComp = Maps.newHashMap();
    userComp.put("username", "user1");
    userComp.put("authorized_keys", "[\"\"]");
    assertEquals(user.get("user1"), userComp);

    List<BooEnvironment> environmentList = booYaml.getEnvironmentList();
    BooEnvironment environment0 = environmentList.get(0);
    assertEquals(environment0.getName(), "dev");
    assertEquals(environment0.getAvailability(), "redundant");

    List<BooCloud> cloudList = environment0.getCloudList();
    BooCloud cloud0 = cloudList.get(0);
    assertEquals(cloud0.getName(), "stub-dfw2a");
    assertEquals(cloud0.getPriority(), "1");
    assertEquals(cloud0.getDpmt_order(), "1");
    assertEquals(cloud0.getPct_scale(), "100");

    List<BooPlatform> ep0 = environment0.getPlatformList();
    BooPlatform ep = ep0.get(0);
    assertEquals(ep.getName(), "platform-0");
    Map<String, String> autoHealing = Maps.newHashMap();
    autoHealing.put("autorepair", "false");
    autoHealing.put("autoreplace", "true");
    autoHealing.put("replace_after_minutes", "60");
    autoHealing.put("replace_after_repairs", "4");
    assertEquals(ep.getAuto_healing(), autoHealing);

    Map<String, Map<String, Object>> epc0 = ep.getComponents();
    Map<String, Object> lbConfig = epc0.get("lb");
    assertEquals(lbConfig.get("ecv_map"), "{\"8080\":\"GET /\"}");

    List<BooScale> scaleList = ep.getScaleList();
    BooScale booScale = scaleList.get(0);
    assertEquals(booScale.getPlatformName(), "platform-0");
    assertEquals(booScale.getComponentName(), "compute");
    assertEquals(booScale.getMin(), "1");
    assertEquals(booScale.getCurrent(), "3");

    BooModelTransformer bmt = new BooModelTransformer();
    OneOps oneops = bmt.convert(booYaml);

    assertEquals("auth-key", oneops.getApikey());
    assertEquals("https://localhost:9000/", oneops.getHost());
    assertEquals("local-org", oneops.getOrganization());

    Assembly assembly = oneops.getAssembly();
    // Assembly
    assertEquals("assembly-name", assembly.getName());
  }

  @Test
  public void validateReadingAssembly() throws Exception {

    OneOpsYamlReader reader = new OneOpsYamlReader();
    OneOps oneops = reader.read(yaml("src/test/yaml/oo-v2", "assembly.yaml"), OneOps.class);

    assertEquals("auth-key", oneops.getApikey());
    assertEquals("https://localhost:9000/", oneops.getHost());
    assertEquals("local-org", oneops.getOrganization());

    Assembly assembly = oneops.getAssembly();
    // Assembly
    assertEquals("assembly-name", assembly.getName());
    Map<String, String> variables = assembly.getVariables();
    assertEquals("variable-value-0", variables.get("variable-0"));
    assertEquals("variable-value-1", variables.get("variable-1"));

    Map<String, String> encryptedVariables = assembly.getEncryptedvariables();
    assertEquals("encrypted-variable-value-0", encryptedVariables.get("encrypted-variable-0"));

    // Tags
    Map<String, String> tags = assembly.getTags();
    assertEquals("tag-value-0", tags.get("tag-0"));
    assertEquals("tag-value-1", tags.get("tag-1"));
    // Platform 0
    List<Platform> platforms = assembly.getPlatformList();
    Platform p0 = platforms.get(0);
    assertEquals("platform-0", p0.getId());
    assertEquals("source/pack-platform-0", p0.getPack());
    assertEquals("1", p0.getPackVersion());
    // Links
    List<String> links = p0.getLinks();
    assertEquals("platform-0-link-0", links.get(0));
    assertEquals("platform-0-link-1", links.get(1));
    // Variables
    Map<String, String> platformVariables = p0.getVariables();
    assertEquals("pack-platform-0-variable-0-value", platformVariables.get("pack-platform-variable-0"));
    assertEquals("pack-platform-0-variable-1-value", platformVariables.get("pack-platform-variable-1"));

    Map<String, String> encryptedPlatformVariables = p0.getEncryptedvariables();
    assertEquals("encrypted-pack-platform-0-variable-0-value", encryptedPlatformVariables.get("encrypted-pack-platform-variable-0"));
    // Components
    List<Component> components = p0.getComponentList();
    Component c0 = components.get(0);
    assertEquals("component-0", c0.getId());
    Map<String, String> config = c0.getConfiguration();
    assertEquals("platform-0-config-0-value", config.get("config-0"));
    assertEquals("platform-0-config-1-value", config.get("config-1"));
    // Platform 1
    assertEquals("platform-1", platforms.get(1).getId());
    assertEquals("1", platforms.get(1).getConfiguration().get("version"));
    List<Component> pcomps = platforms.get(1).getComponentList();
    // Attachment
    List<Attachment> pAttachments = pcomps.get(0).getAttachmentList();
    Attachment pa0 = pAttachments.get(0);
    assertEquals("attachment1", pa0.getId());
    Map<String, String> paconfig0 = pa0.getConfiguration();
    assertEquals("before-add,before-update,before-replace", paconfig0.get("run_on"));

    // Environment
    List<Environment> environments = assembly.getEnvironmentList();
    Environment e0 = environments.get(0);
    assertEquals("environment-0", e0.getId());
    assertTrue(e0.isGlobaldns());
    assertEquals("redundant", e0.getAvailability());
    //Environment global variable
    assertEquals("variable-new-value-0", e0.getVariables().get("variable-0"));
    assertEquals("encrypted-variable-new-value-0", e0.getEncryptedvariables().get("encrypted-variable-0"));
    //Environment platform
    List<Platform> environmentPlatforms = e0.getPlatformList();
    Platform ep0 = environmentPlatforms.get(0);
    assertEquals("platform-1", ep0.getId());
    assertEquals("false", ep0.getEnable());

    //auto-healing
    Map<String, String> ep0config = ep0.getConfiguration();
    assertEquals("redundant", ep0config.get("availability"));
    assertEquals("false", ep0config.get("autorepair"));
    assertEquals("true", ep0config.get("autoreplace"));
    assertEquals("55", ep0config.get("replace_after_minutes"));
    assertEquals("4", ep0config.get("replace_after_repairs"));
    //scale
    Map<String, Map<String, String>> scale = ep0.getScale();
    Map<String, String> computeScale = scale.get("compute");
    assertEquals("3", computeScale.get("current"));
    assertEquals("1", computeScale.get("min"));
    assertEquals("5", computeScale.get("max"));

    //Environment platform variable
    assertEquals("pack-platform-0-variable-0-new-value", ep0.getVariables().get("pack-platform-variable-0"));

    List<Component> environmentComponents = ep0.getComponentList();
    Component epc0 = environmentComponents.get(0);
    assertEquals("component-0", epc0.getId());
    assertEquals("component-0-type", epc0.getType());
    assertEquals("true", epc0.getTouch());
    Map<String, String> config0 = epc0.getConfiguration();
    assertEquals("platform-1-config-0-value", config0.get("config-2"));
    assertEquals("platform-1-config-1-value", config0.get("config-3"));
    // Attachment
    List<Attachment> epAttachments = epc0.getAttachmentList();
    Attachment epa0 = epAttachments.get(0);
    assertEquals("attachment1", epa0.getId());
    Map<String, String> epaconfig0 = epa0.getConfiguration();
    assertEquals("before-add,before-update,before-replace", epaconfig0.get("run_on"));
    // Cloud
    List<Cloud> clouds = e0.getCloudList();
    Cloud cloud = clouds.get(0);
    assertEquals(1, cloud.getPriority());
    assertEquals(1, cloud.getDeploymentorder());
    assertEquals(100, cloud.getScalepercentage());
  }

  protected File yaml(String path, String name) {
    return new File(basedir, String.format("%s/%s", path, name));
  }
}
