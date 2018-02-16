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

import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class Config {

  private static Logger logger = Logger.getLogger(Config.class);

  @Value("${packer_home}")
  private String circuitDir;

  @Value("${amq.in_queue}")
  private String inQueue;

  @Value("${retry_count:3}")
  private int retryCount;

  // allows use of public or private ip
  @Value("${ip_attribute}")
  private String ipAttribute;

  @Value("${data_dir}")
  private String dataDir;

  @Value("${mgmt_domain}")
  private String mgmtDomain;

  @Value("${perf_collector_cert_location:unset}")
  private String perfCollectorCertLocation;

  @Value("${mgmt_url}")
  private String mgmtUrl;

  @Value("${mgmt_cert:unset}")
  private String mgmtCert;

  @Value("${min_free_space_mb:50}")
  private int minFreeSpaceMB;

  // For backwards compliance (using PPC.ignoreUnresolvablePlaceholders)
  // missing autowired values default to a String, Hence this dns value
  // to boolean inside init.
  @Value("${dns}")
  private String dnsEnabled;

  private boolean dnsDisabled = false;

  @Value("${dns_config_file}")
  private String dnsConfigFile;

  // Added to debug compute add - prevents compute::delete on
  // compute::add (install_base/remote) failure
  @Value("${debug_mode}")
  private String debugMode;

  // Enable jmx metrics
  @Value("${enable_jmx:true}")
  private boolean isJMXEnabled;

  // Enable auto shutdown
  @Value("${autoShutDown:false}")
  private boolean isAutoShutDown;

  @Value("${initial_user:unset}")
  private String initialUser;

  @Value("${local_max_consumers:10}")
  private int localMaxConsumers;

  // rsync timeout. Default value is 30 sec
  @Value("${rsync_timeout:30}")
  private int rsyncTimeout;

  /**
   * The list of clouds which are marked to be in stub mode. Inductor will mark those work-order and
   * action orders execution result as per <value>stubResultCode</value>
   */
  @Value("#{'${stub.clouds:}'.toLowerCase().split(',')}")
  private List<String> stubbedCloudsList;

  /**
   * The list of clouds, whose resources are already decommissioned or removed. Inductor will mark
   * those work-order (no action orders) execution result as success (0) regardless of the execution
   * outcome for the clouds listed in this (shutdown.clouds) inductor property. Spring EPL default
   * value is empty string.
   */
  @Value("#{'${shutdown.clouds:}'.toLowerCase().split(',')}")
  private List<String> clouds;

  /**
   * Default to fail; Config should be provided to make the resultCode '1' as failure.
   */
  @Value("${stubResultCode:1}")
  private int stubResultCode;

  /**
   * How long should inductor wait before returning a stubbed response ? Defaults to 5 seconds
   */
  @Value("${stub.responseTimeInSeconds:5}")
  private int stubResponseTimeInSeconds;

  @Value("${autoShutDownThreshold:99.99}")
  private double autoShutDownThreshold;

  /**
   * List of bom classes, whose process result status needs to keep intact. By default
   * <b>bom.Fqdn</b>,<b>bom.Lb</b> are added to this list as it doesn't have any openstack
   * hypervisor dependency.
   */
  @Value("#{'${shutdown.skipClasses:bom.Fqdn,bom.Lb}'.toLowerCase().split(',')}")
  private List<String> bomClasses;

  /**
   * List of rfc actions for which the result need to be processed. <b>DELETE</b>action is added by
   * default.
   */
  @Value("#{'${shutdown.rfcActions:DELETE}'.toLowerCase().split(',')}")
  private List<String> rfcActions;

  /**
   * Timeout  value for command execution for shutdown clouds. Default value is set as 10 sec.
   */
  @Value("${shutdown.cmdTimeout:10}")
  private long cmdTimeout;

  /**
   * Additional env variables to be used for work-order exec. The value can be file location or a
   * string containing multiple ENV_NAME=VALUE entries. Entries are separated by newline (file) or
   * ',' (string). Right now this configuration is used only for local work-orders.
   */
  @Value("${env_vars:}")
  private String env;

  /**
   * Max number of reboots that can happen in a single work-order execution.
   */
  @Value("${reboot_limit:5}")
  private int rebootLimit;

  @Value("#{T(java.lang.Boolean).valueOf('${verify.mode}')}")
  private boolean verifyMode;

  @Value("#{'${verify.exclude.paths:Berksfile}'.split(',')}")
  private List<String> verifyExcludePaths;

  /**
   * Pass additional args for test-kitchen
   * LOG_LEVEL (debug, info, warn, error, fatal)
   * e.g.  --log-level=LOG_LEVEL
   *       --no-color
   */
  @Value("${verify.args:}")
  private String verifyArgs;

  /**
   * Env vars read from {@link #env}. This will get initialized in ${@link #init()}
   */
  private Map<String, String> envVars;

  private String publicKey = "";

  private String dnsKey = null;

  private String dnsSecret = null;

  private String ipAddr = null;

  private String mgmtCertContent = null;

  private String perfCollectorCertContent = null;

  /**
   * init - configuration / defaults
   */
  public void init() {
    // Read env vars.
    envVars = readEnvVars(env);

    // Null checks due to spring ignoreUnresolvablePlaceholders
    // not working on junit tests.
    if (mgmtDomain == null) {
      mgmtDomain = getMgmtDomainFromFile();
    }

    // Read mgmt certificate file content
    mgmtCertContent = readCertFile(mgmtCert);
    perfCollectorCertContent = readCertFile(perfCollectorCertLocation);

    // defaults for some backwards compliance
    if (circuitDir == null) {
      circuitDir = "/opt/oneops/inductor";
    }

    if (dataDir == null || dataDir.equals("${data_dir}")) {
      dataDir = "/opt/oneops/tmp";
    }
    if (ipAttribute == null || ipAttribute.equals("${ip_attribute}")) {
      ipAttribute = "public_ip";
    }

    if (dnsEnabled == null || dnsEnabled.equalsIgnoreCase("off") || dnsEnabled
        .equalsIgnoreCase("false")) {
      dnsDisabled = true;
    }
    if (dnsConfigFile == null || dnsConfigFile.equals("${dns_config_file}")) {
      dnsConfigFile = "/opt/oneops/inductor/global/dns.conf";
    }
    if (debugMode == null) {
      debugMode = "off";
    }
    // Sets the IP for logging.
    ipAddr = getInductorIPv4Addr();
    if (ipAddr == null) {
      ipAddr = "N/A";
    }

    getGlobalDnsConfFromFile();

    Map<String, String> env = System.getenv();
    String envPackerDir = env.get("PACKER_DIR");
    if (envPackerDir != null) {
      circuitDir = envPackerDir;
    }

    if (clouds == null) {
      clouds = new ArrayList<>();
    }

    // Remove the empty default
    clouds.remove("");
    if (!clouds.isEmpty()) {
      logger.info("*** " + this.toString());
    }
  }

  /**
   * get mgmt domain so computes know how to talk to daq
   */
  private String getMgmtDomainFromFile() {
    String outFile = "/opt/oneops/domain";
    String block = "";
    String thisLine;
    BufferedReader br;
    try {
      br = new BufferedReader(new FileReader(outFile));
      while ((thisLine = br.readLine()) != null) {
        block += thisLine;
      }
      br.close();
    } catch (IOException e) {
      logger.error("cannot read /opt/oneops/domain");
    }
    return block;
  }

  /**
   * Read the given cert file.
   *
   * @param certFile cert file path
   * @return string containing content of the file.
   */
  private String readCertFile(String certFile) {
    if (certFile == null || "unset".equals(certFile)) {
      return null;
    }

    String block = "";
    try (BufferedReader br = new BufferedReader(new FileReader(certFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        block += line + "\r\n";
      }
    } catch (Exception e) {
      logger.error("Cannot read " + certFile, e);
      return null;
    }
    return block;
  }

  /**
   * get mgmt domain so computes know how to talk to daq
   */
  private void getGlobalDnsConfFromFile() {
    String thisLine;
    BufferedReader br;
    logger.info("using dns config: " + dnsConfigFile);
    try {
      br = new BufferedReader(new FileReader(dnsConfigFile));
      int count = 0;
      while ((thisLine = br.readLine()) != null) {
        if (count == 0) {
          dnsKey = thisLine.replaceAll("\n", "");
          logger.info("using " + InductorConstants.DEFAULT_DOMAIN
              + " key:" + dnsKey);
        } else if (count == 1) {
          dnsSecret = thisLine.replaceAll("\n", "");
          logger.info("using " + InductorConstants.DEFAULT_DOMAIN
              + " secret:" + dnsSecret);
        }
        count++;
      }
    } catch (FileNotFoundException e) {
      logger.info("not a public inductor - missing: " + dnsConfigFile);
    } catch (IOException e) {
      logger.info("cannot read " + dnsConfigFile);
    }
  }

  /**
   * Retruns the inductor IP address (IPV4 address). If there are multiple NICs/IfAddresses, it
   * selects the first one. Openstack VMs normally has only one network interface (eth0).
   *
   * @return IPV4 address of inductor with interface name. Returns <code>null</code> if it couldn't
   * find anything.
   */
  private String getInductorIPv4Addr() {
    try {
      Enumeration<NetworkInterface> nics = NetworkInterface.getNetworkInterfaces();
      while (nics.hasMoreElements()) {
        NetworkInterface nic = nics.nextElement();
        if (nic.isUp() && !nic.isLoopback()) {
          Enumeration<InetAddress> addrs = nic.getInetAddresses();
          while (addrs.hasMoreElements()) {
            InetAddress add = addrs.nextElement();
            // Print only IPV4 address
            if (add instanceof Inet4Address && !add.isLoopbackAddress()) {
              // Log the first one.
              String ip = add.getHostAddress() + " (" + nic.getDisplayName() + ")";
              logger.info("Inductor IP : " + ip);
              return ip;
            }
          }
        }
      }
    } catch (Exception e) {
      logger.warn("Error getting inductor IP address", e);
      // Skip any errors
    }
    return null;
  }

  /**
   * Helper method to read inductor ${@link #env} and returns an env vars map.
   *
   * @param env env can be a file location or a string containing multiple ENV_NAME=VALUE entries.
   * Entries are separated by newline (file) or ',' (string).
   * @return env var map.
   */
  private Map<String, String> readEnvVars(String env) {
    Path path = Paths.get(env);
    List<String> kvList;
    if (path.toFile().exists()) {
      try {
        kvList = Files.readAllLines(path);
      } catch (IOException ioe) {
        logger.warn("Error reading env var file: " + path, ioe);
        kvList = Collections.emptyList();
      }
    } else {
      kvList = Arrays.asList(env.trim().split(","));
    }
    return kvList.stream()
        .map(s -> s.split("="))
        .filter(p -> p.length == 2)
        .collect(Collectors.toMap(p -> p[0].trim(), p -> p[1].trim()));
  }

  /**
   * Checks if the cloud for given wo has been configured as shutdown
   *
   * @param bwo work order
   * @return <code>true</code> if the wo cloud has configured as shutdown, else return
   * <code>false</code>
   */
  public boolean hasCloudShutdownFor(CmsWorkOrderSimpleBase bwo) {
    if (!clouds.isEmpty()) {
      // Proceed only if it's not null
      if (bwo != null) {
        // Do it only for work orders
        if (bwo instanceof CmsWorkOrderSimple) {
          CmsWorkOrderSimple wo = CmsWorkOrderSimple.class.cast(bwo);
          // Do it only for configured rfc actions (DELETE by default)
          if (rfcActions.contains(wo.getRfcCi().getRfcAction().toLowerCase())) {
            String cloudName = wo.getCloud().getCiName();
            // Do it only for the shutdown clouds
            if (clouds.contains(cloudName.toLowerCase())) {
              String bomClass = wo.getRfcCi().getCiClassName();
              // Skip configured bom classes
              return !bomClasses.contains(bomClass.toLowerCase());
            }
          }

        }
      }

    }
    return false;
  }

  public boolean isCloudStubbed(CmsWorkOrderSimpleBase bwo) {
    boolean stubbedMode = false;
    String cloudName = StringUtils.EMPTY;
    if (!CollectionUtils.isEmpty(stubbedCloudsList)) {
      if (bwo != null) {
        cloudName = getCloud(bwo, cloudName);
        if (StringUtils.isEmpty(cloudName)) {
          stubbedMode = false;
        } else if (stubbedCloudsList.contains(cloudName.toLowerCase())) {
          stubbedMode = true;
        }
      }
    }
    if (stubbedMode) {
      logger.warn("Cloud :" + cloudName + " is running in stub mode.");
    }
    return stubbedMode;
  }

  private String getCloud(CmsWorkOrderSimpleBase bwo, String cloudName) {
    if (bwo instanceof CmsWorkOrderSimple) {
      CmsWorkOrderSimple wo = CmsWorkOrderSimple.class.cast(bwo);
      cloudName = wo.getCloud().getCiName();
    } else if (bwo instanceof CmsActionOrderSimple) {
      CmsActionOrderSimple ao = CmsActionOrderSimple.class.cast(bwo);
      cloudName = ao.getCloud().getCiName();
    }
    return cloudName;
  }


  public static Logger getLogger() {
    return logger;
  }

  public int getMinFreeSpaceMB() {
    return minFreeSpaceMB;
  }

  public List<String> getClouds() {
    return clouds;
  }

  public List<String> getBomClasses() {
    return bomClasses;
  }

  public List<String> getRfcActions() {
    return rfcActions;
  }

  public List<String> getStubbedCloudsList() {
    return stubbedCloudsList;
  }

  public void setStubbedCloudsList(List<String> stubbedCloudsList) {
    this.stubbedCloudsList = stubbedCloudsList;
  }

  public String getCircuitDir() {
    return circuitDir;
  }

  public String getInQueue() {
    return inQueue;
  }

  public int getRetryCount() {
    return retryCount;
  }

  public String getIpAttribute() {
    return ipAttribute;
  }

  public void setIpAttribute(String ipAttribute) {
    this.ipAttribute = ipAttribute;
  }

  public String getDataDir() {
    return dataDir;
  }

  public String getMgmtDomain() {
    return mgmtDomain;
  }

  public String getPerfCollectorCertLocation() {
    return perfCollectorCertLocation;
  }

  public String getMgmtUrl() {
    return mgmtUrl;
  }

  public String getMgmtCert() {
    return mgmtCert;
  }

  public String getDnsEnabled() {
    return dnsEnabled;
  }

  public boolean isDnsDisabled() {
    return dnsDisabled;
  }

  public String getDnsConfigFile() {
    return dnsConfigFile;
  }

  public String getDebugMode() {
    return debugMode;
  }

  public String getInitialUser() {
    return initialUser;
  }

  public int getLocalMaxConsumers() {
    return localMaxConsumers;
  }

  public int getRsyncTimeout() {
    return rsyncTimeout;
  }

  public String getPublicKey() {
    return publicKey;
  }

  public String getDnsKey() {
    return dnsKey;
  }

  public String getDnsSecret() {
    return dnsSecret;
  }

  public String getIpAddr() {
    return ipAddr;
  }

  public String getMgmtCertContent() {
    return mgmtCertContent;
  }

  public String getPerfCollectorCertContent() {
    return perfCollectorCertContent;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public void setCircuitDir(String circuitDir) {
    this.circuitDir = circuitDir;
  }

  public void setVerifyArgs(String verifyArgs) {
    this.verifyArgs = verifyArgs;
  }

  public String getVerifyArgs() {
    return verifyArgs;
  }

  public boolean isVerifyMode() {
    return verifyMode;
  }

  public void setVerifyMode(boolean verifyMode) {
    this.verifyMode = verifyMode;
  }

  public Map<String, String> getEnvVars() {
    return this.envVars;
  }

  public List<String> clouds() {
    return this.clouds;
  }

  public List<String> bomClasses() {
    return this.bomClasses;
  }

  public List<String> rfcActions() {
    return this.rfcActions;
  }

  public long getCmdTimeout() {
    return cmdTimeout;
  }

  public int getStubResponseTimeInSeconds() {
    return stubResponseTimeInSeconds;
  }

  public int getStubResultCode() {
    return stubResultCode;
  }

  public boolean isJMXEnabled() {
    return isJMXEnabled;
  }

  public void setJMXEnabled(boolean JMXEnabled) {
    isJMXEnabled = JMXEnabled;
  }

  public boolean isAutoShutDown() {
    return isAutoShutDown;
  }

  public void setAutoShutDown(boolean autoShutDown) {
    isAutoShutDown = autoShutDown;
  }

  public double getAutoShutDownThreshold() {
    return autoShutDownThreshold;
  }

  public int getRebootLimit() {
    return rebootLimit;
  }

  public void setRebootLimit(int rebootLimit) {
    this.rebootLimit = rebootLimit;
  }

  public void setDataDir(String dataDir) {
    this.dataDir = dataDir;
  }

  public void setClouds(List clouds) {
    this.clouds = clouds;
  }

  public List<String> getVerifyExcludePaths() {
    return verifyExcludePaths;
  }

  public void setVerifyExcludePaths(List<String> verifyExcludePaths) {
    this.verifyExcludePaths = verifyExcludePaths;
  }

  @Override
  public String toString() {
    return "Config{" +
        "circuitDir='" + circuitDir + '\'' +
        ", inQueue='" + inQueue + '\'' +
        ", retryCount=" + retryCount +
        ", ipAttribute='" + ipAttribute + '\'' +
        ", dataDir='" + dataDir + '\'' +
        ", mgmtDomain='" + mgmtDomain + '\'' +
        ", perfCollectorCertLocation='" + perfCollectorCertLocation + '\'' +
        ", mgmtUrl='" + mgmtUrl + '\'' +
        ", mgmtCert='" + mgmtCert + '\'' +
        ", minFreeSpaceMB=" + minFreeSpaceMB +
        ", dnsEnabled='" + dnsEnabled + '\'' +
        ", dnsDisabled=" + dnsDisabled +
        ", dnsConfigFile='" + dnsConfigFile + '\'' +
        ", debugMode='" + debugMode + '\'' +
        ", isJMXEnabled=" + isJMXEnabled +
        ", isAutoShutDown=" + isAutoShutDown +
        ", initialUser='" + initialUser + '\'' +
        ", localMaxConsumers=" + localMaxConsumers +
        ", rsyncTimeout=" + rsyncTimeout +
        ", stubbedCloudsList=" + stubbedCloudsList +
        ", clouds=" + clouds +
        ", stubResultCode=" + stubResultCode +
        ", stubResponseTimeInSeconds=" + stubResponseTimeInSeconds +
        ", autoShutDownThreshold=" + autoShutDownThreshold +
        ", bomClasses=" + bomClasses +
        ", rfcActions=" + rfcActions +
        ", cmdTimeout=" + cmdTimeout +
        ", env='" + env + '\'' +
        ", rebootLimit=" + rebootLimit +
        ", verifyMode=" + verifyMode +
        ", verifyExcludePaths=" + verifyExcludePaths +
        ", verifyArgs=" + verifyArgs +
        ", ipAddr='" + ipAddr + '\'' +
        '}';
  }
}
