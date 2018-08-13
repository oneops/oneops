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

import static com.oneops.cms.util.CmsConstants.EXECUTION_TIME;
import static com.oneops.cms.util.CmsConstants.LOCAL_WAIT_TIME;
import static com.oneops.cms.util.CmsConstants.MANAGED_VIA;
import static com.oneops.cms.util.CmsConstants.REQUEST_DEQUE_TS;
import static com.oneops.cms.util.CmsConstants.SECURED_BY;
import static com.oneops.cms.util.CmsConstants.SERVICED_BY;
import static com.oneops.inductor.InductorConstants.ADD;
import static com.oneops.inductor.InductorConstants.COMPLETE;
import static com.oneops.inductor.InductorConstants.COMPUTE;
import static com.oneops.inductor.InductorConstants.DELETE;
import static com.oneops.inductor.InductorConstants.ENVIRONMENT;
import static com.oneops.inductor.InductorConstants.ERROR_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.FAILED;
import static com.oneops.inductor.InductorConstants.KEYPAIR;
import static com.oneops.inductor.InductorConstants.OK_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.ONEOPS_USER;
import static com.oneops.inductor.InductorConstants.PRIVATE;
import static com.oneops.inductor.InductorConstants.PRIVATE_KEY;
import static com.oneops.inductor.InductorConstants.REMOTE;
import static com.oneops.inductor.InductorConstants.SEARCH_TS_FORMATS;
import static java.lang.String.format;
import static java.nio.file.StandardOpenOption.CREATE_NEW;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.inductor.util.PathUtils;
import com.oneops.inductor.util.ResourceUtils;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.stringtemplate.v4.ST;

/**
 * AbstractOrderExecutor- base class for WorkOrderExecutor and ActionOrderExecutor
 */
public abstract class AbstractOrderExecutor {

  public static final String ONDEMAND = "ondemand";
  public static final String USER_CUSTOM_ATTACHMENT = "user-custom-attachment";
  private static final Logger logger = Logger.getLogger(AbstractOrderExecutor.class);

  protected static final String RUN_LIST_SEPARATOR = "::";
  protected static final String RUN_LIST_PREFIX = "recipe[";
  protected static final String RUN_LIST_SUFFIX = "]";

  final protected Gson gson = new Gson();
  final protected Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

  protected int retryCount;
  protected ProcessRunner processRunner;
  protected String[] sshCmdLine;
  protected String[] rsyncCmdLine;
  protected String[] sshInteractiveCmdLine;
  protected Random randomGenerator = new Random();
  protected StatCollector inductorStat;

  private Config config;
  // Verification template.
  private String verifyTemplate;

  public AbstractOrderExecutor(Config config) {
    this.config = config;
    processRunner = new ProcessRunner(config);

    rsyncCmdLine = new String[]{"/usr/bin/rsync", "-az", "--force", "--exclude=*.png",
        "--rsh=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ",
        "--timeout=" + config.getRsyncTimeout()};

    sshCmdLine = new String[]{"ssh", "-o", "StrictHostKeyChecking=no", "-o",
        "UserKnownHostsFile=/dev/null", "-qi"};

    // Interactive needed to get output of execute resource. 2-t's needed to get output
    // of execute resource - probably anything that uses mixlib shell out.
    sshInteractiveCmdLine = new String[]{"ssh", "-t", "-t", "-o", "StrictHostKeyChecking=no", "-o",
        "UserKnownHostsFile=/dev/null", "-qi"};

    retryCount = config.getRetryCount();
    initVerificationConfig();
  }


  /**
   * Initializes the verification config template. It uses ANTLR StringTemplate format.
   *
   * @see <a href="https://github.com/antlr/stringtemplate4">StringTemplate</a>
   */
  private void initVerificationConfig() {
    verifyTemplate = ResourceUtils.readResourceAsString("/verification/kitchen-tmpl.yml");
  }

  /**
   * boolean check for uuid
   *
   * @param uuid string
   */
  public static boolean isUUID(String uuid) {
    if (uuid == null) {
      return false;
    }
    try {
      // we have to convert to object and back to string because the built
      // in fromString does not have good validation logic.
      UUID fromStringUUID = UUID.fromString(uuid);
      String toStringUUID = fromStringUUID.toString();
      return toStringUUID.equalsIgnoreCase(uuid);
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  public void setProcessRunner(ProcessRunner processRunner) {
    this.processRunner = processRunner;
  }

  /**
   * Process the work-order or action-order and return message to be put
   * in the controller response queue
   *
   * @param order wo/ao
   * @param correlationId correlationId
   * @return Process response map.                                                                                                                                                                                                                                                             ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               String> message
   */
  abstract Map<String, String> process(CmsWorkOrderSimpleBase order, String correlationId)
      throws IOException;

  /**
   * Process the work-order or action-order and run the verification if it's completes successfully.
   *
   * @param wo wo/ao
   * @param correlationID JMS correlationId.
   * @return Process response map.
   */
  public Map<String, String> processAndVerify(CmsWorkOrderSimpleBase wo, String correlationID)
      throws IOException {
    Map<String, String> resMap = process(wo, correlationID);
    String resCode = resMap.getOrDefault("task_result_code", ERROR_RESPONSE_CODE);
    return resCode.equals(OK_RESPONSE_CODE) ? runVerification(wo, resMap) : resMap;
  }

  protected void processStubbedCloud(CmsWorkOrderSimpleBase wo) {
    try {
      TimeUnit.SECONDS.sleep(getStubSleepTime(wo));
    } catch (InterruptedException e) {
      //Sleep for response.
    }
    if (wo instanceof CmsWorkOrderSimple) {
      ((CmsWorkOrderSimple) (wo))
          .setDpmtRecordState(config.getStubResultCode() == 0 ? COMPLETE : FAILED);
      CmsCISimple resultCi = new CmsCISimple();
      if (wo.getClassName().contains("Compute")) {
        resultCi.addCiAttribute("private_ip", "1.0.0.0");
        resultCi.addCiAttribute("metadata", "{\"owner\" : \"oneops@walmartlabs.com\"}");
      }
      mergeRfcToResult(((CmsWorkOrderSimple) wo).getRfcCi(), resultCi);
      wo.setResultCi(resultCi);
    } else if (wo instanceof CmsActionOrderSimple) {
      ((CmsActionOrderSimple) (wo)).setActionState(
          config.getStubResultCode() == 0 ? OpsActionState.complete : OpsActionState.failed);
    }
  }

  protected long getStubSleepTime(CmsWorkOrderSimpleBase wo) {
    return config.getStubResponseTimeInSeconds();
  }

  /**
   * Set the local wait time for local work-order/action-order
   */
  protected void setLocalWaitTime(CmsWorkOrderSimpleBase wo) {
    String localWaitTime;
    try {
      localWaitTime = String.valueOf(getTimeElapsed(wo) / 1000.0);
      wo.getSearchTags().put(LOCAL_WAIT_TIME, localWaitTime);
    } catch (Exception e) {
      logger.error("Exception occurred while setting local wait time " + e);
    }
  }

  private long getTimeElapsed(CmsWorkOrderSimpleBase wo) throws DateParseException {
    return System.currentTimeMillis() - DateUtil
        .parseDate(getSearchTag(wo, REQUEST_DEQUE_TS), SEARCH_TS_FORMATS).getTime();
  }

  protected <T> String getSearchTag(CmsWorkOrderSimpleBase<T> wo, String searchTag) {
    return wo.getSearchTags().get(searchTag);
  }

  /**
   * Set the total execution time for work-order/action-order
   */
  protected void setTotalExecutionTime(CmsWorkOrderSimpleBase wo,
      long duration) {
    try {
      if (getSearchTag(wo, LOCAL_WAIT_TIME) != null) {
        duration -= Double.valueOf(getSearchTag(wo, LOCAL_WAIT_TIME));
      }
      wo.getSearchTags().put(EXECUTION_TIME, String.valueOf(duration / 1000.0));
    } catch (Exception e) {
      logger.error("Exception occurred while setting execution time", e);
    }

  }

  /**
   * writes private key
   *
   * @param key String
   */
  protected String writePrivateKey(String key) {
    String uuid = UUID.randomUUID().toString();
    String fileName = config.getDataDir() + "/" + uuid;
    try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName))) {
      bw.write(key);
      bw.close();
      File f = new File(fileName);
      f.setExecutable(false, false);
      f.setReadable(false, false);
      f.setWritable(false, false);
      f.setReadable(true, true);
      f.setWritable(true, true);
      logger.debug("file: " + fileName);
    } catch (IOException e) {
      logger.error("could not write file: " + fileName + " msg:"
          + e.getMessage());
    }
    return fileName;
  }

  /**
   * removeFile - removes a file with uuid checking
   */
  protected void removeFile(String filename) {
    String[] fileParts = filename.split("/");
    String possibleUuid = fileParts[fileParts.length - 1];
    if (isUUID(possibleUuid) && !config.getDebugMode().equals("on")) {
      File f = new File(filename);
      f.delete();
      logger.debug("deleted: " + filename);
    }
  }


  /**
   * generateRsyncErrorMessage: generate user friendly message based on rsync exit code
   *
   * @param exitCode int
   * @param ipPort String
   */
  protected String generateRsyncErrorMessage(int exitCode, String ipPort) {
    if (exitCode == 11 || exitCode == 12) {
      return String.format("Filesystem full on %s - cleanup and retry", ipPort);
    } else {
      return String.format("Cannot connect to %s - network or compute service issue", ipPort);
    }
  }


  /**
   * getCookbookPath: gets cookbook path by ciClassname
   *
   * @param className String
   */
  protected String getCookbookPath(String className) {
    String cookbookPath = "";
    // Arrays.asList is fixed size - need the LinkedList ... also \\. to
    // split by "."
    List<String> classParts = new LinkedList<>(
        Arrays.asList(className.split("\\.")));

    // remove first bom. or cloud. and last Component Class
    classParts.remove(0);
    classParts.remove(classParts.size() - 1);

    // remove service
    if (classParts.size() > 0 && classParts.get(0).equals("service")) {
      classParts.remove(0);
    }

    if (classParts.size() > 0) {
      cookbookPath = "circuit-" + Joiner.on("-").join(classParts);
    } else {
      cookbookPath = "circuit-main-1";
    }

    // cloud service use the default packer
    return cookbookPath;
  }

  /**
   * Gets cookbook path by ciClassname
   */
  protected void writeChefRequest(CmsWorkOrderSimpleBase wo, String fileName) {
    // build a chef request
    Map<String, Object> chefRequest = assembleRequest(wo);
    // write the request to a .json file
    try (BufferedWriter out = Files.newBufferedWriter(Paths.get(fileName))) {
      out.write(gsonPretty.toJson(chefRequest));
    } catch (IOException e) {
      logger.error("Exception occurred in writing", e);
    }
  }

  /**
   * Creates a chef config file for unique lockfile by ci. returns chef config full path.
   *
   * @param cookbookRelativePath chef config full path
   */
  protected String writeChefConfig(String ci, String cookbookRelativePath, String cloudName,
      Map<String, Map<String, CmsCISimple>> cloudServices, String logLevel) {
    String filename = "/tmp/chef-" + ci;

    Set<String> cookbookPaths = createCookbookSearchPath(cookbookRelativePath, cloudServices,
        cloudName);

    String content = "cookbook_path " + gson.toJson(cookbookPaths) + "\n";

    content += "lockfile \"" + filename + ".lock\"\n";
    content += "file_cache_path \"/tmp\"\n";
    content += "log_level :" + logLevel + "\n";
    content += "verify_api_cert true\n";
    FileWriter fstream;
    try {
      fstream = new FileWriter(filename);
      BufferedWriter bw = new BufferedWriter(fstream);
      bw.write(content);
      bw.close();

      File f = new File(filename);
      f.setExecutable(false, false);
      f.setReadable(false, false);
      f.setWritable(false, false);
      f.setReadable(true, true);
      f.setWritable(true, true);
      logger.debug("file: " + filename);

    } catch (IOException e) {
      logger.error("could not write file: " + filename + " msg:"
          + e.getMessage());
    }
    return filename;
  }

  protected LinkedHashSet<String> createCookbookSearchPath(String cookbookRelativePath,
      Map<String, Map<String, CmsCISimple>> cloudServices,
      String cloudName) {
    String cookbookDir = config.getCircuitDir();
    if (!cookbookRelativePath.equals("")) {
      cookbookDir = config.getCircuitDir().replace("packer",
          cookbookRelativePath);
    }

    cookbookDir += "/components/cookbooks";
    String sharedDir = config.getCircuitDir().replace("packer", "shared/cookbooks");

    LinkedHashSet<String> cookbookPaths = new LinkedHashSet<>();
    if (cloudServices != null) {
      for (String serviceName : cloudServices.keySet()) { // for each service
        CmsCISimple serviceCi = cloudServices.get(serviceName).get(cloudName);
        if (serviceCi != null) {
          String serviceClassName = serviceCi.getCiClassName();
          String serviceCookbookCircuit = getCookbookPath(serviceClassName);
          if (!serviceCookbookCircuit.equals(cookbookRelativePath)) {
            cookbookPaths.add(config.getCircuitDir().replace("packer", serviceCookbookCircuit)
                + "/components/cookbooks");
          }
        }
      }
    }
    if (cookbookPaths.size() > 0) {
      //Remove the current component's circuit from the cookbook_path so that we can add it after other circuits
      //This is to make sure the current component's circuit is higher priority in search path
      cookbookPaths.remove(cookbookDir);
    }
    cookbookPaths.add(cookbookDir);
    cookbookPaths.add(sharedDir);
    return cookbookPaths;
  }

  /**
   * Populates list of proxies from a json bash string
   */
  protected void updateProxyListBash(ArrayList<String> proxyList, String jsonProxyHash) {
    Map<String, String> proxyMap = gson.fromJson(jsonProxyHash, Map.class);
    if (proxyMap != null) {
      for (String key : proxyMap.keySet()) {
        proxyList.add(key + ":" + proxyMap.get(key));
      }
    }

  }

  /**
   * Populates list of proxies from a json string
   */
  protected void updateProxyList(ArrayList<String> proxyList, Map<String, String> proxyMap) {
    if (proxyMap != null) {
      for (String key : proxyMap.keySet()) {
        proxyList.add(key + "_proxy=" + proxyMap.get(key));
      }
    }
  }


  /**
   * Chef-solo command builder
   *
   * @param fileName payload file name
   * @param chefConfig chef config file
   * @param debug <code>true</code> if the chef debug (-l) is enabled.
   * @return command string array.
   */
  protected String[] buildChefSoloCmd(String fileName, String chefConfig, boolean debug) {
    List<String> cmd = buildDefaultChefSolo(fileName, chefConfig, debug);
    return cmd.toArray(new String[cmd.size()]);
  }

  protected List<String> buildDefaultChefSolo(String fileName, String chefConfig, boolean debug) {
    List<String> cmd = new ArrayList<>();
    cmd.add("chef-solo");
    if (debug) {
      cmd.add("-l");
      cmd.add("debug");
    }
    cmd.add("-c");
    cmd.add(chefConfig);
    cmd.add("-j");
    cmd.add(fileName);
    return cmd;
  }

  /**
   * getCommentsFromResult: gets comments using the fault map
   *
   * @param result ProcessResult
   */
  protected String getCommentsFromResult(ProcessResult result) {
    String comments = "";

    // for now just have 1 comment
    for (Map.Entry<String, String> entry : result.getFaultMap().entrySet()) {
      comments = entry.getKey() + ": " + entry.getValue();
    }
    if (comments.isEmpty()) {
      comments = "failed without any specified faults.";
    }

    return comments;
  }

  /**
   * mergeRfcToResult: copies rfc attrs to resultCi
   *
   * @param rfc CmsRfcCISimple
   * @param result CmsCISimple
   */
  protected void mergeRfcToResult(CmsRfcCISimple rfc, CmsCISimple result) {

    result.setCiId(rfc.getCiId());
    result.setLastAppliedRfcId(rfc.getRfcId());
    result.setCiClassName(rfc.getCiClassName());
    result.getAttrProps().putAll(rfc.getCiAttrProps());

    Map<String, String> rfcAttrs = rfc.getCiAttributes();

    if (result.getCiAttributes() == null) {
      result.setCiAttributes(new HashMap<>());
    }
    Map<String, String> resultAttrs = result.getCiAttributes();
    for (String key : rfcAttrs.keySet()) {
      if (!resultAttrs.containsKey(key)) {
        resultAttrs.put(key, rfcAttrs.get(key));
      }
    }
  }


  /**
   * mergeRfcToResult: copies ci attrs to resultCi
   *
   * @param ci CmsCISimple
   * @param result CmsCISimple
   */
  protected void mergeCiToResult(CmsCISimple ci, CmsCISimple result) {

    result.setCiId(ci.getCiId());
    result.setCiClassName(ci.getCiClassName());
    result.getAttrProps().putAll(ci.getAttrProps());

    Map<String, String> rfcAttrs = ci.getCiAttributes();

    if (result.getCiAttributes() == null) {
      result.setCiAttributes(new HashMap<>());
    }
    Map<String, String> resultAttrs = result.getCiAttributes();
    for (String key : rfcAttrs.keySet()) {
      if (!resultAttrs.containsKey(key)) {
        resultAttrs.put(key, rfcAttrs.get(key));
      }
    }
  }

  /**
   * Returns the authoritative server for the given DNS zone. It queries the
   * NS record using platform dig command.
   *
   * @param cloudService CmsCISimple
   * @return list of authoritative servers. Empty if it cou;dn't find any.
   */
  protected List<String> getAuthoritativeServersByCloudService(
      CmsCISimple cloudService) {

    List<String> nameServers = new ArrayList<>();
    String dnsZone = cloudService.getCiAttributes().get("zone");
    String[] digCmd = new String[]{"/usr/bin/dig", "+short", "NS", dnsZone};
    ProcessResult result = processRunner.executeProcessRetry(digCmd, "", retryCount);
    if (result.getResultCode() > 0) {
      logger.error("dig +short NS " + dnsZone + " returned: " + result.getStdErr());
    }

    String res = result.getStdOut();
    if (!res.equalsIgnoreCase("")) {
      nameServers = Arrays.asList(res.split("\n"));
    }
    return nameServers;
  }

  /**
   * Determine if workorder should run remotely
   *
   * @param wo work order
   */
  protected Boolean isRemoteChefCall(CmsWorkOrderSimpleBase wo) {
    if (wo.getPayLoad() == null) {
      return false;
    }
    // if has a servicedBy (cluster) or ManagedVia (compute) and not a
    // compute::add
    if ((isManagedVia(wo) || (isServiceBy(wo)
        && !isKeyPairClass(wo)))
        && !(isWorkOrderOfCompute(wo) && isAddDelete(wo))) {//TODO chanhge isCo
      return true;
    }
    return false;
  }

  private boolean isManagedVia(CmsWorkOrderSimpleBase wo) {
    return wo.isPayLoadEntryPresent(MANAGED_VIA);
  }

  private boolean isServiceBy(CmsWorkOrderSimpleBase wo) {
    return wo.isPayLoadEntryPresent(SERVICED_BY);
  }

  private boolean isNetscaler(List<CmsRfcCISimple> servicedBy) {
    return servicedBy
        .get(0).getCiName().startsWith("netscaler");
  }

  protected boolean isKeyPairClass(CmsWorkOrderSimpleBase wo) {
    return wo.getClassName()
        .endsWith(KEYPAIR);
  }

  private boolean isCompute(String className) {
    return className.contains(COMPUTE);
  }

  private boolean isAddDelete(CmsWorkOrderSimpleBase wo) {
    return ADD.equalsIgnoreCase(wo.getAction()) || DELETE.equalsIgnoreCase(wo.getAction());
  }


  public <T> String getCustomerDomain(CmsWorkOrderSimpleBase<T> wo) {
    final T env = wo.getPayLoadEntryAt(ENVIRONMENT, 0);
    String cloudName = wo.getCloud().getCiName();
    CmsCISimple cloudService = new CmsCISimple();
    if (wo.getServices() != null && wo.getServices().containsKey("dns")) {
      cloudService = wo.getServices().get("dns").get(cloudName);
    }
    CmsCISimple envSimple;
    //cloud actions
    if (env == null) {
      return getCustomerDomain(cloudService, null);
    }
    if (env instanceof CmsCISimple) {
      envSimple = CmsCISimple.class.cast(env);
    } else {
      envSimple = new CmsCISimple();
      mergeRfcToResult(CmsRfcCISimple.class.cast(env), envSimple);
    }
    return getCustomerDomain(cloudService, envSimple);
  }

  /**
   * generates the dns domain name from cloud service zone and env subdomain <subdomain from
   * env>.<cloud dns id>.<cloud service zone> <env.assembly.org>.<cloud>.<zone.com>
   */
  public String getCustomerDomain(CmsCISimple cloudService, CmsCISimple env) {
    String domain = "";
    if (env != null && env.getCiAttributes().containsKey("subdomain")
        && env.getCiAttributes().get("subdomain") != null) {
      domain = env.getCiAttributes().get("subdomain");
    }

    if (cloudService.getCiAttributes().containsKey("cloud_dns_id")
        && cloudService.getCiAttributes().get("cloud_dns_id").length() > 0) {
      domain += '.' + cloudService.getCiAttributes().get("cloud_dns_id");
    }

    if (cloudService.getCiAttributes().containsKey("zone")) {
      domain += '.' + cloudService.getCiAttributes().get("zone");
    }

    return domain;
  }


  protected <T> String writePrivateKey(CmsWorkOrderSimpleBase<T> wo)
      throws KeyNotFoundException {

    if (wo.isPayLoadEntryPresent(SECURED_BY)) {
      String key = wo.getPayLoadAttribute(SECURED_BY, PRIVATE);
      checkIfEmpty(wo, key);
      return writePrivateKey(key);
    } else if (wo.isPayLoadEntryPresent(SERVICED_BY)
        && wo.isAttributePresentInPayload(SERVICED_BY, PRIVATE_KEY)) {
      String key = wo.getPayLoadAttribute(SERVICED_BY, PRIVATE_KEY);
      checkIfEmpty(wo, key);
      return writePrivateKey(key);
    }
    throw newKeyNotFoundException(wo);
  }

  private void checkIfEmpty(CmsWorkOrderSimpleBase wo, String key) throws KeyNotFoundException {
    if (StringUtils.isEmpty(key)) {
      throw newKeyNotFoundException(wo);
    }
  }

  /**
   * Gets dns servers
   *
   * @param wo action order
   */
  protected <T> List<String> getAuthoritativeServers(CmsWorkOrderSimpleBase<T> wo) {
    String cloudName = wo.getCloud().getCiName();
    CmsCISimple cloudService = wo.getServices().get("dns").get(cloudName);
    return getAuthoritativeServersByCloudService(cloudService);
  }

  /**
   * Check if the verify.mode is enabled for the work order
   * The value of Environment attribute 'verify' if set trumps the config value
   *
   * @param wo {@code CmsWorkOrderSimple}
   * @return <code>true</code> if the environment verify is selected, else return <code>false</code>
   */
  protected <T> boolean isVerifyEnabled(CmsWorkOrderSimpleBase<T> wo) {
    if (wo.isPayloadEntryEqual(ENVIRONMENT, "verify", "default")) {
        return config.isVerifyMode();
    }
    else {
        return (wo.isPayloadEntryEqual(ENVIRONMENT, "verify", "true"));
    }
  }

  /**
   * Check if the debug mode is enabled for the work order
   *
   * @param wo {@code CmsWorkOrderSimple}
   * @return <code>true</code> if the environment debug is selected, else return <code>false</code>
   */
  protected <T> boolean isDebugEnabled(CmsWorkOrderSimpleBase<T> wo) {
    return (config.getDebugMode().equals("on") || wo
        .isPayloadEntryEqual(ENVIRONMENT, "debug", "true"));
  }

  protected <T> boolean equals(CmsWorkOrderSimpleBase<T> wo, String payloadEntry,
      String attributeName, String valueToBeCompared) {
    return wo.getPayLoadAttribute(payloadEntry, attributeName).equals(valueToBeCompared);
  }

  /**
   * writes private key and returns String of the filename
   *
   * @param wo CmsWorkOrderSimple
   */
  private <T> KeyNotFoundException newKeyNotFoundException(CmsWorkOrderSimpleBase<T> wo) {
    String errorMessage = String
        .format("workorder: %s %s missing SecuredBy sshkey.", wo.getNsPath(), wo.getAction());
    logger.error(errorMessage);
    return new KeyNotFoundException(errorMessage);
  }

  /**
   * Removes a file with uuid checking
   */
  protected void removeFile(CmsWorkOrderSimpleBase wo, String filename) {
    if (!isDebugEnabled(wo)) {
      removeFile(filename);
    }
  }


  protected String getDebugFlag(CmsWorkOrderSimpleBase ao) {
    String debugFlag = StringUtils.EMPTY;
    if (isDebugEnabled(ao)) {
      debugFlag = "-d";
    }
    return debugFlag;
  }

  protected String getShortenedClass(String className) {
    return StringUtils.substringAfterLast(className, ".").toLowerCase();
  }

  protected String normalizeClassName(CmsWorkOrderSimpleBase wo) {
    return getShortenedClass(wo.getClassName());
  }

  /**
   * @param wo
   * @return
   */
  public Map<String, Object> assembleRequest(CmsWorkOrderSimpleBase wo) {
    String appName = getAppName(wo);
    Map<String, Object> chefRequest = new HashMap<>();
    Map<String, String> global = new HashMap<>();
    List<String> runList = getRunList(wo);
    chefRequest.put(appName, wo.getCiAttributes());
    chefRequest.put("customer_domain", getCustomerDomain(wo));
    chefRequest.put("mgmt_domain", config.getMgmtDomain());
    chefRequest.put("mgmt_url", config.getMgmtUrl());
    chefRequest.put("workorder", wo);
    chefRequest.put("global", global);
    chefRequest.put("run_list", runList.toArray());
    chefRequest.put("app_name", appName);
    chefRequest.put("public_key", config.getPublicKey());
    chefRequest.put("ip_attribute", config.getIpAttribute());
    if (appName.equals(COMPUTE)) {
      chefRequest.put("initial_user", config.getInitialUser());
      chefRequest.put("circuit_dir", config.getCircuitDir());
    }
    // needed to prevent chef error:
    // Chef::Exceptions::ValidationFailed: Option name's value foo does not
    // match regular expression /^[\-[:alnum:]_:.]+$/
    chefRequest.put("name", wo.getCiName());

    // set mgmt cert
    if (config.getMgmtCertContent() != null) {
      chefRequest.put("mgmt_cert", config.getMgmtCertContent());
    }

    //set perf-collector cert
    if (config.getPerfCollectorCertContent() != null) {
      chefRequest.put("perf_collector_cert", config.getPerfCollectorCertContent());
    }

    return chefRequest;
  }

  protected String getAction(CmsWorkOrderSimpleBase ao) {
    String action = ao.getAction();
    if (USER_CUSTOM_ATTACHMENT.equalsIgnoreCase(action)) {
      action = ONDEMAND;
    }
    return action;
  }

  protected String getAppName(CmsWorkOrderSimpleBase wo) {
    return normalizeClassName(wo);
  }

  /**
   * Assemble the json request for chef
   */
  public String getRunListEntry(String recipeName, String action) {
    return RUN_LIST_PREFIX + recipeName + RUN_LIST_SEPARATOR + action + RUN_LIST_SUFFIX;
  }

  public String getRecipeAction(String action) {
    boolean isRemoteAction = isRemoteAction(action);
    if (isRemoteAction) {
      return "add";
    }
    return action;
  }


  protected boolean isWorkOrderOfCompute(CmsWorkOrderSimpleBase wo) {
    return "bom.Compute".equals(wo.getClassName());
  }

  protected boolean isAction(CmsWorkOrderSimpleBase wo, String action) {
    if (action == null) {
      return false;
    }
    return action.equals(wo.getAction());
  }

  protected abstract List<String> getRunList(CmsWorkOrderSimpleBase wo);

  protected boolean isRemoteAction(String action) {
    return REMOTE.equals(action);
  }

  protected boolean rsynch(ExecutionContext ctx) {
    boolean rsynchFailed = false;
    ProcessResult result = processRunner.executeProcessRetry(ctx);
    if (result.getResultCode() > 0) {
      logger.error(
          ctx.getLogKey() + " FATAL: " + generateRsyncErrorMessage(result.getResultCode(),
              ctx.getHost()));
      handleRsyncFailure(ctx.getWo(), ctx.getKeyFile());
      rsynchFailed = true;
    }
    return rsynchFailed;
  }

  protected void handleRsyncFailure(CmsWorkOrderSimpleBase wo, String keyFile) {
    inductorStat.addRsyncFailed();
    removeFile(wo, keyFile);
  }

  protected void copySearchTagsFromResult(CmsWorkOrderSimpleBase wo, ProcessResult result) {
    wo.getSearchTags().putAll(result.getTagMap());
  }

  public void setInductorStat(StatCollector inductorStat) {
    this.inductorStat = inductorStat;
  }

  /**
   * Returns the remote host of wo/ao.
   *
   * @param wo work/action order.
   * @param logKey inductor wo/ao order log key.
   * @return host ip address.
   */
  public abstract String getHost(CmsWorkOrderSimpleBase wo, String logKey);

  /**
   * Checks if the remote wo/ao is managed via a <b>windows</b> compute.
   * Currently we are relying on the managed via compute size attribute
   * to determine the os type. This is used until we figure out a proper
   * solution.
   *
   * @param o wo/ao.
   * @return <code>true</code> if the wo/ao is managed via windows compute.
   */
  public boolean isWinCompute(CmsWorkOrderSimpleBase o) {
    Map<String, String> compAttrs = Collections.emptyMap();
    if (o instanceof CmsWorkOrderSimple) {
      CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;
      CmsRfcCISimple compute = wo.getPayLoadEntryAt(MANAGED_VIA, 0);
      if (compute != null) {
        compAttrs = compute.getCiAttributes();
      }
    } else if (o instanceof CmsActionOrderSimple) {
      CmsActionOrderSimple ao = (CmsActionOrderSimple) o;
      CmsCISimple compute = ao.getPayLoadEntryAt(MANAGED_VIA, 0);
      if (compute != null) {
        compAttrs = compute.getCiAttributes();
      }
    }

    String computeSize = compAttrs.getOrDefault("size", "N/A").toUpperCase();
    List<String> winSizes = Arrays.asList("S-WIN", "M-WIN", "L-WIN", "XL-WIN", "XL-WIN-LDO");
    return winSizes.contains(computeSize);
  }

  /**
   * Returns inductor wo/ao order log key. Extra ' - ' for pattern matching -
   * daq InductorLogSink will parse this and insert into log store see
   * https://github.com/oneops/daq/wiki/schema for more info.
   *
   * @param o work/action order.
   * @return log key.
   */
  public String getLogKey(CmsWorkOrderSimpleBase o) {
    return o.getRecordId() + ":" + o.getCiId() + " - ";
  }

  /**
   * Returns the file name used when executing remote wo/ao.
   *
   * @param wo work/action order.
   * @return file path.
   */
  public String getRemoteFileName(CmsWorkOrderSimpleBase wo) {
    return format("/opt/oneops/workorder/%s.%s.json", getShortenedClass(wo.getClassName()),
        wo.getCiName());
  }

  /**************************
   *  Verification APIs
   **************************/

  /**
   * Returns the circuit directory of the component.
   *
   * @param wo component work order.
   * @return circuit root directory path.
   */
  public Path getCircuitDir(CmsWorkOrderSimpleBase wo) {
    String circuitName = getCookbookPath(wo.getClassName());
    return Paths.get(config.getCircuitDir().replace("packer", circuitName));
  }

  /**
   * Returns the cookbook directory of the component.
   *
   * @param wo component work order.
   * @return cookbook directory path.
   */
  public Path getCookbookDir(CmsWorkOrderSimpleBase wo) {
    String compName = getShortenedClass(wo.getClassName());
    Path circuitDir = getCircuitDir(wo);
    return circuitDir.resolve("components/cookbooks/" + compName);
  }

  /**
   * Returns the verification spec file path for the component action. The path is :
   * {circuit_root}/components/cookbooks/user/test/integration/{action}/serverspec/{action}_spec.rb
   *
   * @param wo component work order.
   * @return action spec file path.
   */
  public Path getActionSpecPath(CmsWorkOrderSimpleBase wo) {
    String action = wo.getAction();
    return getCookbookDir(wo)
        .resolve(format("test/integration/%s/serverspec/%s_spec.rb", action, action));
  }

  /**
   * Generate the kitchen yaml string for given local/remote work-order.
   *
   * @param wo work order.
   * @param sshKey ssh key path for the work order.
   * @param logKey log key
   * @return kitchen yaml string for the work-order.
   */
  public String generateKitchenConfig(CmsWorkOrderSimpleBase wo, String sshKey, String logKey) {
    String inductorHome = config.getCircuitDir().replace("/packer", "");
    ST st = new ST(verifyTemplate);

    boolean isWin = isWinCompute(wo);
    String chefSolo = isWin ? "c:/opscode/chef/embedded/bin/chef-solo" : "/usr/local/bin/chef-solo";
    String rubyBindir = isWin ? "c:/opscode/chef/embedded/bin" : "/usr/bin";
    String provisionerPath = isWin ? "c:/tmp/kitchen" : "/tmp/kitchen";

    st.add("local", !isRemoteChefCall(wo));
    st.add("circuit_root", getCircuitDir(wo));
    st.add("inductor_home", inductorHome);
    st.add("recipe_name", wo.getAction());
    st.add("driver_host", getHost(wo, logKey));
    st.add("platform_name", "centos-7.1");
    st.add("user", ONEOPS_USER);
    st.add("ssh_key", sshKey);
    st.add("windows", isWin);
    st.add("chef_solo_path", chefSolo);
    st.add("ruby_bindir", rubyBindir);
    st.add("provisioner_root_path", provisionerPath);
    st.add("verifier_root_path", getVerifierPath(wo));
    return st.render();
  }

  /**
   * Returns the platform specific unique verifier path.
   *
   * @param wo wo/ao.
   * @return path string.
   */
  private String getVerifierPath(CmsWorkOrderSimpleBase wo) {
    String path = isWinCompute(wo) ? "c:/tmp/verifier" : "/tmp/verifier";
    return String.format("%s-%s", path, wo.getRecordId());
  }

  /**
   * Returns the remote work order rsync command.
   *
   * @param o component work order.
   * @param sshKey ssh key path.
   * @param logKey log key.
   * @return rsync command.
   */
  public String[] getRemoteWoRsyncCmd(CmsWorkOrderSimpleBase o, String sshKey, String logKey) {
    int size = rsyncCmdLine.length;
    String host = getHost(o, logKey);
    String[] cmd = Arrays.copyOf(rsyncCmdLine, size + 2);
    // Some nasty hack due to legacy code :‑/
    cmd[4] += format("-p 22 -qi %s", sshKey);
    cmd[size] = format("%s/%d.json", config.getDataDir(), o.getRecordId());
    cmd[size + 1] = format("oneops@%s:%s", host, getRemoteFileName(o));
    return cmd;
  }


  /**
   * Run verification tests for the component. Usually this is done after executing the work order.
   *
   * @param wo work order
   * @param responseMap response map result of work-order run.
   * @return updated response map.
   */
  protected Map<String, String> runVerification(CmsWorkOrderSimpleBase wo,
      Map<String, String> responseMap) {

      String logKey = getLogKey(wo) + " verify -> ";
      Boolean VerifyEnabled = isVerifyEnabled(wo);

      logger.info(
          format("%sConfig.verify mode: '%s'", logKey, config.isVerifyMode()));
      logger.info(
          format("%sEnvironment.verify mode: '%s'", logKey, wo.getPayLoadAttribute(ENVIRONMENT, "verify")));
      logger.info(
          format("%sIsVerifyEnabled: '%s'", logKey, VerifyEnabled));

    if (VerifyEnabled) {
      long start = System.currentTimeMillis();

      if (config.isCloudStubbed(wo)) {
        logger.info(logKey + "Skipping verification for stubbed cloud.");
        return responseMap;
      }

      if (!Files.exists(getActionSpecPath(wo))) {
        logger.info(logKey + "Skipping verification. No spec found at : " + getActionSpecPath(wo));
        return responseMap;
      }

      String action = wo.getAction();
      String compName = getShortenedClass(wo.getClassName());

      try {
        logger.info(
            format("%sRunning '%s' verification for component '%s'", logKey, action, compName));
        String host = getHost(wo, logKey);
        String localWOPath = format("%s/%d.json", config.getDataDir(), wo.getRecordId());
        String remoteWOPath = getRemoteFileName(wo);

        boolean debugMode = isDebugEnabled(wo);
        boolean isRemoteWO = isRemoteChefCall(wo);
        logger.info(logKey + "Local WO Path: " + localWOPath);
        logger.info(logKey + "Remote WO Path: " + remoteWOPath);
        logger.info(logKey + "Circuit Path: " + getCircuitDir(wo));
        logger.info(logKey + "Debug mode: " + debugMode);

        // Copy remote work-order.
        String sshKey = null;
        if (isRemoteWO) {
          sshKey = writePrivateKey(wo);
          logger.info(logKey + "SSH key path: " + sshKey);
          String[] cmdLine = getRemoteWoRsyncCmd(wo, sshKey, logKey);
          logger.info(logKey + "### SYNC: " + remoteWOPath);
          ProcessResult result = processRunner
              .executeProcessRetry(new ExecutionContext(wo, cmdLine, logKey, retryCount));

          if (result.getResultCode() > 0) {
            wo.setComments(
                "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":22"));
            handleRsyncFailure(wo, sshKey);
            responseMap.put("task_result_code", "500");
            return responseMap;
          }
        }

        // Copy cookbook to tmp working directory.
        Path workDir = Paths.get(format("/tmp/%s-%d", compName, wo.getRecordId()));
        logger.info(logKey + "Working Dir: " + workDir);
        PathUtils.delete(workDir);
        PathUtils.copy(getCookbookDir(wo), workDir, config.getVerifyExcludePaths());

        // Generate kitchen config.
        String kitchenConfigPath = format("%s/%dk.yaml", workDir, wo.getRecordId());
        logger.info(logKey + "Generating Kitchen Config : " + kitchenConfigPath);
        String kitchenConfig = generateKitchenConfig(wo, sshKey, logKey);
        Files.write(Paths.get(kitchenConfigPath), kitchenConfig.getBytes(), CREATE_NEW);

        // Execute the kitchen verify
        String woPath = isRemoteWO ? remoteWOPath : localWOPath;
        String[] cmd = {"kitchen", "verify"};
        ProcessResult result = new ProcessResult();
        Map<String, String> envVars = new HashMap<>();
        envVars.put("WORKORDER", woPath);
        envVars.put("KITCHEN_YAML", kitchenConfigPath);
        processRunner.executeProcess(cmd, logKey, result, envVars, workDir.toFile());
        if (result.getResultCode() > 0) {
          wo.setComments("FATAL: Spec verification failed!");
          responseMap.put("task_result_code", "500");
          if("compute".equals(compName)) {
            responseMap.put("compute_kci_failure", "true");  
          }
        }

        // Clean up working dir.
        if (!debugMode) {
          PathUtils.delete(workDir);
        }
      } catch (Throwable t) {
        logger.info(logKey + "Verification failed: " + t.getMessage());
        logger.error("Verification failed", t);
        responseMap.put("task_result_code", "500");
        if("compute".equals(compName)) {
          responseMap.put("compute_kci_failure", "true");  
        }
      } finally {
        logger.info(logKey + " Run Verification took: "
            + MILLISECONDS.toSeconds(System.currentTimeMillis() - start) + " seconds.");
      }
    } else {
      responseMap.put("kci_status", "verify is disabled");
    }

    return responseMap;
  }
}
