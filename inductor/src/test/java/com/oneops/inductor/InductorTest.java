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
package com.oneops.inductor;

import com.google.gson.Gson;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.inductor.util.JSONUtils;
import com.oneops.inductor.util.ResourceUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import static com.oneops.cms.util.CmsConstants.MANAGED_VIA;
import static com.oneops.cms.util.CmsConstants.SECURED_BY;
import static com.oneops.inductor.InductorConstants.*;
import static com.oneops.inductor.util.ResourceUtils.readResourceAsBytes;
import static com.oneops.inductor.util.ResourceUtils.readResourceAsString;
import static java.util.Collections.emptyMap;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/** Inductor AO/WO Unit tests. */
public class InductorTest {

  private final Gson gson = new Gson();
  private static String remoteWo;
  private static String remoteAo;
  private static String localWo;
  private static Yaml yaml;
  private static CommonCloudConfigurationsHelper commonCloudConfigurationsHelper;
  private static Map<String, Object> cloudConfig;

  @BeforeClass
  public static void init() {
    remoteWo = readResourceAsString("/remoteWorkOrder.json");
    localWo = readResourceAsString("/localWorkOrder.json");
    remoteAo = readResourceAsString("/remoteActionOrder.json");
    yaml = new Yaml();
    commonCloudConfigurationsHelper =
        new CommonCloudConfigurationsHelper(
            Logger.getLogger(CommonCloudConfigurationsTest.class.getName()), "");
    String jsonContent = ResourceUtils.readResourceAsString(CLOUD_CONFIG_FILE_PATH);
    cloudConfig = JSONUtils.convertJsonToMap(jsonContent);
  }

  @Test
  public void testProvider() {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    WorkOrderExecutor executor = new WorkOrderExecutor(mock(Config.class), mock(Semaphore.class));
    String provider = executor.getProvider(wo);
    assertTrue(provider.equals("azure"));
  }

  @Test
  public void testWOVerifyConfig() {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setDataDir("/tmp/wos");

    WorkOrderExecutor woExec = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    assertEquals("/opt/oneops/inductor/circuit-oneops-1", woExec.getCircuitDir(wo).toString());
    assertEquals(
        "/opt/oneops/inductor/circuit-oneops-1/components/cookbooks/user",
        woExec.getCookbookDir(wo).toString());
    assertEquals(
        "/opt/oneops/inductor/circuit-oneops-1/components/cookbooks/user/test/integration/add/serverspec/add_spec.rb",
        woExec.getActionSpecPath(wo).toString());

    final String[] cmdLine = woExec.getRemoteWoRsyncCmd(wo, "sshkey", "");
    String rsync =
        "[/usr/bin/rsync, -az, --force, --exclude=*.png, --rsh=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p 22 -qi sshkey, --timeout=0, /tmp/wos/190494.json, oneops@inductor-test-host:/opt/oneops/workorder/user.test_wo-25392-1.json]";
    assertEquals(rsync, Arrays.toString(cmdLine));

    // Assertions for windows computes.
    assertFalse("WO should be managed via a non-windows compute.", woExec.isWinCompute(wo));
    wo.getPayLoadEntryAt(MANAGED_VIA, 0).getCiAttributes().put("size", "M-WIN");
    assertTrue("WO should be managed via a windows compute.", woExec.isWinCompute(wo));
  }

  @Test
  public void testAOVerifyConfig() {
    CmsActionOrderSimple ao = gson.fromJson(remoteAo, CmsActionOrderSimple.class);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setDataDir("/tmp/wos");

    ActionOrderExecutor aoExec = new ActionOrderExecutor(cfg, mock(Semaphore.class));
    assertEquals("/opt/oneops/inductor/circuit-oneops-1", aoExec.getCircuitDir(ao).toString());
    assertEquals(
        "/opt/oneops/inductor/circuit-oneops-1/components/cookbooks/tomcat",
        aoExec.getCookbookDir(ao).toString());
    assertEquals(
        "/opt/oneops/inductor/circuit-oneops-1/components/cookbooks/tomcat/test/integration/status/serverspec/status_spec.rb",
        aoExec.getActionSpecPath(ao).toString());

    final String[] cmdLine = aoExec.getRemoteWoRsyncCmd(ao, "sshkey", "");
    String rsync =
        "[/usr/bin/rsync, -az, --force, --exclude=*.png, --rsh=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null -p 22 -qi sshkey, --timeout=0, /tmp/wos/211465.json, oneops@inductor-test-host:/opt/oneops/workorder/tomcat.tomcat-9687230-1.json]";
    assertEquals(rsync, Arrays.toString(cmdLine));

    // Assertions for windows computes.
    assertFalse("AO should be managed via a non-windows compute.", aoExec.isWinCompute(ao));
    ao.getPayLoadEntryAt(MANAGED_VIA, 0).getCiAttributes().put("size", "L-WIN");
    assertTrue("AO should be managed via a windows compute.", aoExec.isWinCompute(ao));
  }

  @Test
  public void testRemoteKitchenConfig() {
    testKitchenConfig(remoteWo, true);
    testWinKitchenConfig(remoteWo);
  }

  @Test
  public void testLocalKitchenConfig() {
    testKitchenConfig(localWo, false);
  }

  @Test
  public void testBomClass() {
    String bomPrefix = "bom\\.(.*\\.)*";
    String fqdnBomClass = bomPrefix + "Fqdn";
    assertTrue("bom.Fqdn".matches(fqdnBomClass));
    assertTrue("bom.oneops.1.Fqdn".matches(fqdnBomClass));
    assertTrue("bom.main.Fqdn".matches(fqdnBomClass));
    assertFalse("bomFqdn".matches(fqdnBomClass));
    assertFalse("bom.Compute".matches(fqdnBomClass));

    String ringBomClass = bomPrefix + "Ring";
    assertTrue("bom.Ring".matches(ringBomClass));
    assertTrue("bom.oneops.1.Ring".matches(ringBomClass));
    assertTrue("bom.main.Ring".matches(ringBomClass));
    assertFalse("bomRing".matches(ringBomClass));
    assertFalse("bom.Compute".matches(ringBomClass));

    String clusterBomClass = bomPrefix + "Cluster";
    assertTrue("bom.Cluster".matches(clusterBomClass));
    assertTrue("bom.oneops.1.Cluster".matches(clusterBomClass));
    assertTrue("bom.main.Cluster".matches(clusterBomClass));
    assertFalse("bomCluster".matches(clusterBomClass));
    assertFalse("bom.Compute".matches(clusterBomClass));
  }

  @Test
  public void testStatFile() throws Exception {
    String dataDir = "/opt/oneops/inductor/xxx/data";
    String logDir = "/opt/oneops/inductor/xxx/log";
    String statsLog = "inductor-stat.log";

    Config cfg = new Config();
    cfg.setDataDir(dataDir);
    StatCollector c = new StatCollector(cfg);
    c.setStatFileName(statsLog);
    assertEquals(c.getStatFileName(), Paths.get(logDir, statsLog).toString());

    statsLog = "/opt/inductor/log/test.log";
    c.setStatFileName(statsLog);
    assertEquals(c.getStatFileName(), statsLog);
  }

  @Test
  public void testEnvVars() {
    ProcessRunner p = new ProcessRunner(mock(Config.class));
    String remoteCmd[] = {"ssh", "-i"};
    assertNull(p.getEnvVars(remoteCmd, emptyMap()));

    String localCmd[] = new String[] {"chef-solo", "-i"};
    String envName = "WORKORDER";
    String envValue = "/tmp/wo.json";

    Map<String, String> extraVars = new HashMap<>();
    extraVars.put(envName, envValue);
    Map<String, String> envVars = p.getEnvVars(localCmd, extraVars);
    assertEquals(envValue, envVars.get(envName));

    localCmd = new String[] {"KITCHEN", "verify"};
    envVars = p.getEnvVars(localCmd, extraVars);
    assertEquals(envValue, envVars.get(envName));
  }

  @Test
  public void testWoExecutorConfigWithEmptyCloudServiceEnvVar() {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setEnv("");
    cfg.init();
    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    final String vars = executor.getProxyEnvVars(wo);
    assertTrue(vars.equals("class=user pack=circuit-oneops-1"));
  }

  @Test
  public void testWoExecutorConfigWithNoCloudServiceEnvVar() {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    wo.setServices(Collections.EMPTY_MAP);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setEnv("");
    cfg.init();
    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    final String vars = executor.getProxyEnvVars(wo);
    assertTrue(
        vars.equals(
            "rubygems_proxy=http://repos.org/gemrepo/ rubygemsbkp_proxy=http://dal-repos.org/gemrepo/ ruby_proxy= DATACENTER_proxy=dal misc_proxy=http://repos.org/mirrored-assets/apache.mirrors.pair.com/ class=user pack=circuit-oneops-1"));
  }

  @Test
  public void testWoExecutorConfigWithCloudCloudServiceEnvVarOverriding() {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    HashMap<String, String> m = new HashMap<>();
    m.put("rubygems", "compute_proxy");
    m.put("rubygemsbkp", "compute_proxy_backup");
    wo.getPayLoad().get(MANAGED_VIA).get(0).addCiAttribute("proxy_map", gson.toJson(m));
    wo.setServices(Collections.EMPTY_MAP);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setEnv("");
    cfg.init();
    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    final String vars = executor.getProxyEnvVars(wo);

    assertTrue(
        vars.equals(
            "rubygems_proxy=compute_proxy rubygemsbkp_proxy=compute_proxy_backup ruby_proxy= DATACENTER_proxy=dal misc_proxy=http://repos.org/mirrored-assets/apache.mirrors.pair.com/ class=user pack=circuit-oneops-1"));
  }

  /**
   * This could be used for local testing, Need to add key and modify the user-app.json accordingly
   */
  @Test
  public void runVerification() throws IOException {
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setDataDir("/tmp/wos");
    cfg.setVerifyMode(true);
    cfg.setClouds(Collections.EMPTY_LIST);

    String privKey = readResourceAsString("/verification/key");
    wo.getPayLoadEntryAt(SECURED_BY, 0).getCiAttributes().put(PRIVATE, privKey);
    wo.getPayLoad()
        .get(MANAGED_VIA)
        .get(0)
        .setCiAttributes(Collections.singletonMap("public_ip", ""));
    wo.getRfcCi().setCiName("app-7401500-1");
    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    HashMap<String, CmsWorkOrderSimple> hm = new HashMap<>();
    hm.put("workorder", wo);
    byte[] userWO = readResourceAsBytes("/user-app.json");

    // Files.write(Paths.get("/tmp/wos/190494.json"), userWO, TRUNCATE_EXISTING);
    Map<String, String> mp = new HashMap<>();
    mp = executor.runVerification(wo, mp);
    assertTrue(!mp.containsKey("compute_kci_status"));
    assertTrue(mp.containsKey("kci_status"));
    assertTrue(mp.get("kci_status").toString().equals("verify is disabled"));
  }

  /** This is used to check if the compute kci has failed */
  @Test
  public void computeKciFailedVerification() {
    Listener listener = new Listener();
    Map<String, String> responseMap = new HashMap<>();
    assertFalse(listener.computeKciFailed(responseMap));
    responseMap.put("compute_kci_failure", "true");
    assertTrue(listener.computeKciFailed(responseMap));
  }

  /** This is used to check if the workorder is updated to include the add_fail_clean recipe */
  @Test
  public void updateWorkOrderWithActionVerification() {
    Listener listener = new Listener();
    CmsWorkOrderSimple wo = gson.fromJson(remoteWo, CmsWorkOrderSimple.class);
    CmsWorkOrderSimple workorder = listener.updateWorkOrderWithAction(wo, ADD_FAIL_CLEAN);
    assertEquals(workorder.getRfcCi().getRfcAction(), ADD_FAIL_CLEAN);
    assertEquals(workorder.getSearchTags().get("rfcAction"), ADD_FAIL_CLEAN);
  }

  /**
   * Helper method to test local/remote WO kitchen yaml config.
   *
   * @param woString wo string.
   * @param remote <code>true</code> if the wo is for remote compute.
   */
  private void testKitchenConfig(String woString, boolean remote) {
    CmsWorkOrderSimple wo = gson.fromJson(woString, CmsWorkOrderSimple.class);
    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setEnv("");
    cfg.init();

    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    String config = executor.generateKitchenConfig(wo, "/tmp/sshkey", "logkey");
    Object yamlConfig = yaml.load(config);

    assertNotNull("Invalid kitchen config.", yamlConfig);
    if (remote) {
      assertTrue(config.contains("root_path: /tmp/verifier-190494"));
      assertTrue(config.contains("custom_serverspec_command: sudo -E env WORKORDER="));
    }
  }

  private void testWinKitchenConfig(String woString) {
    CmsWorkOrderSimple winWO = gson.fromJson(woString, CmsWorkOrderSimple.class);
    winWO.getPayLoadEntryAt(MANAGED_VIA, 0).getCiAttributes().put("size", "M-WIN");

    Config cfg = new Config();
    cfg.setCircuitDir("/opt/oneops/inductor/packer");
    cfg.setIpAttribute("public_ip");
    cfg.setEnv("");
    cfg.init();

    WorkOrderExecutor executor = new WorkOrderExecutor(cfg, mock(Semaphore.class));
    String config = executor.generateKitchenConfig(winWO, "/tmp/sshkey", "logkey");
    Object winYaml = yaml.load(config);

    assertNotNull("Invalid kitchen config.", winYaml);
    assertTrue(config.contains("root_path: c:/tmp/verifier-190494"));
    assertTrue(config.contains("custom_serverspec_command: WORKORDER="));
  }

  @Test
  public void testWorkorderCiAttributesReplace() {

    Listener listener = new Listener();
    CmsWorkOrderSimple wo = gson.fromJson(localWo, CmsWorkOrderSimple.class);

    String cloudName = listener.getCloudName(wo);
    String orgName = listener.getOrganizationName(wo);
    final Map<String, Object> servicesMap =
        listener.getServicesMap(commonCloudConfigurationsHelper, cloudConfig, cloudName, orgName);
    final Map<String, Object> enhancedServicesMap =
        listener.getEnhancedServiceMap(wo.getServices(), cloudName, servicesMap);
    listener.updateCiAttributes(wo, commonCloudConfigurationsHelper, enhancedServicesMap);
    Assert.assertEquals(
        "test3", wo.getServices().get("dns").get("stub-dfw2b").getCiAttributes().get("password"));
  }

  @Test
  // make sure updateCiAttributes not updating attributes that are not provided in cloud config.
  public void testWorkorderCiAttributes() {
    Listener listener = new Listener();
    CmsWorkOrderSimple wo = gson.fromJson(localWo, CmsWorkOrderSimple.class);

    String cloudName = listener.getCloudName(wo);
    String orgName = listener.getOrganizationName(wo);
    final Map<String, Object> servicesMap =
        listener.getServicesMap(commonCloudConfigurationsHelper, cloudConfig, cloudName, orgName);
    final Map<String, Object> enhancedServicesMap =
        listener.getEnhancedServiceMap(wo.getServices(), cloudName, servicesMap);
    listener.updateCiAttributes(wo, commonCloudConfigurationsHelper, enhancedServicesMap);
    Assert.assertEquals(
        "testdnsid", wo.getServices().get("dns").get("stub-dfw2b").getCiAttributes().get("cloud_dns_id"));
  }
}
