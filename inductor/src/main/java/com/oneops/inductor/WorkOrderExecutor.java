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

import static com.oneops.cms.util.CmsConstants.MANAGED_VIA;
import static com.oneops.cms.util.CmsConstants.RESPONSE_ENQUE_TS;
import static com.oneops.cms.util.CmsConstants.SEARCH_TS_PATTERN;
import static com.oneops.inductor.InductorConstants.ADD;
import static com.oneops.inductor.InductorConstants.ADD_FAIL_CLEAN;
import static com.oneops.inductor.InductorConstants.AFTER_ATTACHMENT;
import static com.oneops.inductor.InductorConstants.ATTACHMENT;
import static com.oneops.inductor.InductorConstants.BEFORE_ATTACHMENT;
import static com.oneops.inductor.InductorConstants.CLOUD_CONFIG_FILE_PATH;
import static com.oneops.inductor.InductorConstants.COMPLETE;
import static com.oneops.inductor.InductorConstants.COMPUTE;
import static com.oneops.inductor.InductorConstants.DELETE;
import static com.oneops.inductor.InductorConstants.ERROR_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.EXTRA_RUN_LIST;
import static com.oneops.inductor.InductorConstants.FAILED;
import static com.oneops.inductor.InductorConstants.INFOBLOX_CN;
import static com.oneops.inductor.InductorConstants.INFOBLOX_PARAM;
import static com.oneops.inductor.InductorConstants.INFOBLOX_SN;
import static com.oneops.inductor.InductorConstants.KEYWHIZ_BASE_PATH;
import static com.oneops.inductor.InductorConstants.KEYWHIZ_PREFIX;
import static com.oneops.inductor.InductorConstants.KNOWN;
import static com.oneops.inductor.InductorConstants.LOG;
import static com.oneops.inductor.InductorConstants.LOGGED_BY;
import static com.oneops.inductor.InductorConstants.MONITOR;
import static com.oneops.inductor.InductorConstants.OK_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.ONEOPS_USER;
import static com.oneops.inductor.InductorConstants.REMOTE;
import static com.oneops.inductor.InductorConstants.REPLACE;
import static com.oneops.inductor.InductorConstants.UPDATE;
import static com.oneops.inductor.InductorConstants.WATCHED_BY;
import static com.oneops.inductor.util.JSONUtils.convertJsonToMap;
import static com.oneops.inductor.util.ResourceUtils.readExternalFile;
import static java.lang.String.format;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.gson.internal.LinkedTreeMap;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.codahale.metrics.MetricRegistry;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.dj.domain.CmsRfcCIBasic;
import com.oneops.cms.dj.domain.RfcHint;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.inductor.util.ResourceUtils;

/** WorkOrder specific processing */
public class WorkOrderExecutor extends AbstractOrderExecutor {

  private static final String BOM_CLASS_PREFIX = "bom\\.(.*\\.)*";
  private static final String FAIL_ON_DELETE_FAILURE_ATTR = "fail_on_delete_failure";
  private static final String BASE_INSTALL_TIME = "bInstallTime";
  private static final String REBOOT_RUN_LIST = "recipe[shared::reboot_vm]";
  private static final String STUB_RESP_COMPONENT_PREFIX = "stub.respTime.";
  private static Logger logger = Logger.getLogger(WorkOrderExecutor.class);
  private Semaphore semaphore;
  private Config config;
  private MetricRegistry registry;

  public WorkOrderExecutor(Config config, Semaphore semaphore) {
    super(config);
    this.config = config;
    this.semaphore = semaphore;
  }

  /**
   * Process work-order and return message to be put in the controller response queue.
   *
   * @param o CmsWorkOrderSimpleBase
   * @param correlationId JMS correlation Id
   * @returns response message map.
   */
  @Override
  public Map<String, String> process(CmsWorkOrderSimpleBase o, String correlationId) {
    CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;
    Map<String, Object> cloudConfig = readCloudConfig(KEYWHIZ_BASE_PATH + CLOUD_CONFIG_FILE_PATH);
    if (cloudConfig.isEmpty()) {
      logger.info(getLogKey(wo) + "No config found, continuing with what is provided in WO.");
    } else {
      String cloudName = getCloudName(wo);
      String orgName = getOrganizationName(wo);
      CommonCloudConfigurationsHelper commonCloudConfigurationsHelper =
          new CommonCloudConfigurationsHelper(logger, getLogKey(wo));
      final Map<String, Object> servicesMap =
          getServicesMap(commonCloudConfigurationsHelper, cloudConfig, cloudName, orgName);

      updateCiAttributes(
          wo, commonCloudConfigurationsHelper, getEnhancedServiceMap(wo, cloudName, servicesMap));
    }
    // compute::replace will do a delete and add - only for old pre-versioned compute
    String[] classParts = wo.getRfcCi().getCiClassName().split("\\.");
    if (classParts.length < 3 && isWorkOrderOfCompute(wo) && isAction(wo, REPLACE)) {
      logger.info("compute::replace - delete then add");
      wo.getRfcCi().setRfcAction(DELETE);
      process(wo, correlationId);

      if (wo.getDpmtRecordState().equals(COMPLETE)) {
        if (wo.getRfcCi().getCiAttributes().containsKey("instance_id")) {
          wo.getRfcCi().getCiAttributes().remove("instance_id");
        }
        wo.getRfcCi().setRfcAction(ADD);
      } else {
        logger.info("compute::replace - delete failed");
        return buildResponseMessage(wo, correlationId);
      }
    }

    long startTime = System.currentTimeMillis();
    if (config.isCloudStubbed(wo)) {
      String fileName = config.getDataDir() + "/" + wo.getDpmtRecordId() + ".json";
      writeChefRequest(wo, fileName);
      processStubbedCloud(wo);
      logger.warn("completing wo without doing anything because cloud is stubbed");
    } else {
      // skip fqdn workorder if dns is disabled
      if (config.isDnsDisabled()
          && wo.getRfcCi().getCiClassName().matches(BOM_CLASS_PREFIX + "Fqdn")) {
        wo.setDpmtRecordState(COMPLETE);
        CmsCISimple resultCi = new CmsCISimple();
        mergeRfcToResult(wo.getRfcCi(), resultCi);
        wo.setResultCi(resultCi);
        logger.info("completing wo without doing anything because dns is off");
      } else {
        // creates the json chefRequest and exec's chef to run chef local or remote via ssh/mc
        runWorkOrder(wo);
      }
    }

    long endTime = System.currentTimeMillis();
    int duration = Math.round((endTime - startTime) / 1000);
    logger.info(
        format(
            "%s %s %s %s took: %d sec",
            wo.getRfcCi().getRfcAction(),
            wo.getRfcCi().getImpl(),
            wo.getRfcCi().getCiClassName(),
            wo.getRfcCi().getCiName(),
            duration));

    setTotalExecutionTime(wo, endTime - startTime);
    wo.putSearchTag(RESPONSE_ENQUE_TS, DateUtil.formatDate(new Date(), SEARCH_TS_PATTERN));
    return buildResponseMessage(wo, correlationId);
  }

  public void updateCiAttributes(
      CmsWorkOrderSimple wo,
      CommonCloudConfigurationsHelper commonCloudConfigurationsHelper,
      Map<String, Object> servicesMap) {

    String cloudName = getCloudName(wo);
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();

    services.forEach(
        (serviceKey, serviceValue) -> {
          try {
            if (serviceValue.containsKey(cloudName)) {
              String className = getShortenedClass(serviceValue.get(cloudName).getCiClassName());

              Map<String, Object> cloudCommonCiAttributes =
                  commonCloudConfigurationsHelper.findClassCiAttributes(
                      commonCloudConfigurationsHelper.findServiceClasses(servicesMap, serviceKey),
                      className);
              logger.info(
                  getLogKey(wo)
                      + "Verifying  "
                      + cloudName
                      + " "
                      + serviceKey
                      + " attributes for class name "
                      + className);

              cloudCommonCiAttributes.forEach(
                  (ciAttrKey, ciAttrValue) -> {
                    try {
                      String value = ciAttrValue.toString();
                      if (value.contains(KEYWHIZ_PREFIX)) {
                        value = getFromKeywhiz(ciAttrKey, value);
                      }
                      if (value != null) {

                        logger.info(getLogKey(wo) + "Changing component attribute: " + ciAttrKey);
                        wo.getServices()
                            .get(serviceKey)
                            .get(cloudName)
                            .getCiAttributes()
                            .put(ciAttrKey, value);
                      }
                    } catch (Exception e) {
                      logger.info(
                          String.format(
                              " Work Order Exception  :: %s %s", getLogKey(wo), e.getMessage()));
                    }
                  });
            }
          } catch (Exception e) {
            logger.info(getLogKey(wo) + e.getMessage());
          }
        });
  }

  private String getFromKeywhiz(String ciAttrKey, String ciAttrValue) {
    String placeHolderValue = null;
    Matcher matcher = Pattern.compile("\\((.*?)\\)").matcher(ciAttrValue);
    if (matcher.find()) {
      ciAttrValue = getCiAttributeValueFromKeywhiz(matcher.group(1));
      if (!ciAttrValue.isEmpty()) {
        Map<String, Object> secretsMap = convertJsonToMap(ciAttrValue);
        if (secretsMap.containsKey(ciAttrKey)) {
          placeHolderValue = secretsMap.get(ciAttrKey).toString();
        }
      }
    }
    return placeHolderValue;
  }

  public Map<String, Object> getEnhancedServiceMap(
      CmsWorkOrderSimple wo,
      String cloudName,
      Map<String, Object> servicesMap) {
    // If infoblox is not in inputted cloud's work order, no need to enhanced service map
    if (!wo.getServices().containsKey(INFOBLOX_SN)
        || wo.getServices().get(INFOBLOX_SN).get(cloudName) == null) {
      return servicesMap;
    }
    String woInfobloxValue =
        wo.getServices().get(INFOBLOX_SN).get(cloudName).getCiAttributes().get(INFOBLOX_PARAM);
    if (woInfobloxValue == null) {
      return servicesMap;
    }

    final Map<String, Object> enMap = new HashMap<>();

    servicesMap
        .entrySet()
        .forEach(
            e -> {
              Map<String, Object> data = (Map<String, Object>) e.getValue();
              if (e.getKey().equalsIgnoreCase(INFOBLOX_SN)) {
                if (data.containsKey(INFOBLOX_CN)) {
                  Map<Object, Object> ibService =
                      getFromInfoblox(woInfobloxValue, data, INFOBLOX_CN);
                  enMap.put(e.getKey(), ibService);
                }
              } else {
                enMap.put(e.getKey(), e.getValue());
              }
            });

    return enMap;
  }

  public Map<Object, Object> getFromInfoblox(
      String patternParam, Map<String, Object> data, String servicename) {

    Map<Object, Object> ibloxConfig = new HashMap<>();

    List<Object> iBloxList = (List) data.get(servicename);

    for (Object obj : iBloxList) {

      if (((Map<Object, Object>) obj).get("host").toString().equalsIgnoreCase(patternParam)) {
        ibloxConfig.put(servicename, obj);
      }
    }
    return ibloxConfig;
  }

  public Map<String, Object> getServicesMap(
      CommonCloudConfigurationsHelper commonCloudConfigurationsHelper,
      Map<String, Object> cloudConfig,
      String cloudName,
      String orgName) {
    Map<String, Object> servicesMap =
        commonCloudConfigurationsHelper.findServicesAtOrgLevel(cloudConfig, orgName, cloudName);
    if (servicesMap.isEmpty()) {
      servicesMap =
          commonCloudConfigurationsHelper.findServicesAtCloudLevel(cloudConfig, cloudName);
      if (servicesMap.isEmpty()) {
        servicesMap = cloudConfig;
      }
    }
    return servicesMap;
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

  private Map<String, Object> readCloudConfig(String path) {
    //  String confDir = System.getProperty("conf.dir", "");

    String jsonContent = ResourceUtils.readExternalFile(path);
    return convertJsonToMap(jsonContent);
  }

  private String getCiAttributeValueFromKeywhiz(String secretName) {
    return readExternalFile(KEYWHIZ_BASE_PATH + secretName);
  }

  @Override
  protected List<String> getRunList(CmsWorkOrderSimpleBase wo) {
    ArrayList<String> runList = new ArrayList<>();
    String appName = normalizeClassName(wo);
    runList.add(getRunListEntry(appName, wo.getAction()));

    // if remotely executed
    boolean isRemoteAction = isRemoteAction(wo.getAction());
    // monitors, but only remote wo's
    if (isRemoteChefCall(wo)) {
      if (wo.isPayLoadEntryPresent(WATCHED_BY)) {
        runList.add(getRunListEntry(MONITOR, getRecipeAction(wo.getAction())));
      }
      // custom logging
      if (wo.isPayLoadEntryPresent(LOGGED_BY)) {
        runList.add(getRunListEntry(LOG, getRecipeAction(wo.getAction())));
      }

      if (!(appName.equals(COMPUTE) && isRemoteAction)) {
        // only run attachments on remote calls
        runList.add(0, getRunListEntry(ATTACHMENT, BEFORE_ATTACHMENT + wo.getAction()));
        runList.add(getRunListEntry(ATTACHMENT, AFTER_ATTACHMENT + wo.getAction()));
      }
      addExtraRunListEntry(wo, runList);

    } else if (!wo.getAction().equals(ADD_FAIL_CLEAN)) {
      // this is to call global monitoring service from the inductor host
      // or gen config on inductor to handle new compute
      if (wo.getServices().get("monitoring") != null
          || wo.getClassName().matches("bom\\..*\\..*\\.Compute")) {
        // runList.add("recipe[monitor::" + wo.getAction() + "]");
        runList.add(getRunListEntry("monitor", wo.getAction()));
      }
    }
    return runList;
  }

  private void addExtraRunListEntry(CmsWorkOrderSimpleBase wo, List<String> runList) {
    if (wo.isPayLoadEntryPresent(EXTRA_RUN_LIST)) {
      if (wo instanceof CmsWorkOrderSimple) {
        runList.addAll(getExtraRunListClasses(CmsWorkOrderSimple.class.cast(wo)));
      } else {
        throw new IllegalArgumentException("wo can not be of type " + wo.getClass());
      }
    }
  }

  protected List<String> getExtraRunListClasses(CmsWorkOrderSimple wo) {
    List<CmsRfcCISimple> extraRunListRfc = wo.getPayLoadEntry(EXTRA_RUN_LIST);
    // get distinct class names as there could be multiple entries for same class name
    return extraRunListRfc
        .stream()
        .map(CmsRfcCIBasic::getCiClassName)
        .distinct()
        .map(
            className ->
                getRunListEntry(getShortenedClass(className), getRecipeAction(wo.getAction())))
        .collect(Collectors.toList());
  }

  /**
   * Builds the response message for the controller.
   *
   * @param wo CmsWorkOrderSimple
   * @param correlationId String
   * @returns response message map.
   */
  private Map<String, String> buildResponseMessage(CmsWorkOrderSimple wo, String correlationId) {
    long t1 = System.currentTimeMillis();
    // state and resultCI gets set via chef response serialize and send to controller
    String responseCode = OK_RESPONSE_CODE;
    String responseText = gson.toJson(wo);

    if (!COMPLETE.equalsIgnoreCase(wo.getDpmtRecordState())) {
      logger.warn("FAIL: " + wo.getDpmtRecordId() + " state:" + wo.getDpmtRecordState());
      responseCode = ERROR_RESPONSE_CODE;
    }

    Map<String, String> message = new HashMap<>();
    message.put("body", responseText);
    message.put("correlationID", correlationId);
    message.put("task_result_code", responseCode);
    // currently logging the time , will log response time
    logger.info("wo response time took  " + (System.currentTimeMillis() - t1));
    return message;
  }

  /** Runs work orders on the inductor box, ex compute::add */
  private void runLocalWorkOrder(
      CmsWorkOrderSimple wo, String appName, String logKey, String fileName, String cookbookPath)
      throws JsonSyntaxException {
    runLocalWorkOrder(wo, appName, logKey, fileName, cookbookPath, 1);
  }

  private void runLocalWorkOrder(
      CmsWorkOrderSimple wo,
      String appName,
      String logKey,
      String fileName,
      String cookbookPath,
      int attempt)
      throws JsonSyntaxException {

    String chefConfig = writeChefConfig(wo, cookbookPath);
    // run local - iaas calls to provision vm
    String[] cmd = buildChefSoloCmd(fileName, chefConfig, isDebugEnabled(wo));
    logger.info(logKey + " ### EXEC: localhost " + String.join(" ", cmd));

    int woRetryCount = getRetryCountForWorkOrder(wo);

    if (attempt > retryCount) {
      logger.error("hit max retries - kept getting InterruptedException");
      return;
    }

    try {
      boolean hasQueueOnLock = semaphore.hasQueuedThreads();
      if (hasQueueOnLock) {
        logger.info(logKey + " waiting for semaphore...");
      }
      semaphore.acquire();
      if (hasQueueOnLock) {
        logger.info(logKey + " got semaphore");
      }
      setLocalWaitTime(wo);
      ProcessResult result;
      try {
        result =
            processRunner.executeProcessRetry(new ExecutionContext(wo, cmd, logKey, woRetryCount));
      } finally {
        semaphore.release();
      }
      // try to recover from a bad compute
      if (result.getResultCode() != 0
          && appName.equalsIgnoreCase(COMPUTE)
          && wo.getRfcCi().getRfcAction().equalsIgnoreCase(ADD)) {
        if (result.getFaultMap().containsKey(KNOWN)) {
          logger.info(
              "deleting and retrying compute because known issue: "
                  + result.getFaultMap().get(KNOWN));
          cleanupFailedCompute(result, wo);
          wo.getRfcCi().getCiAttributes().remove("instance_id");
          wo.getRfcCi().setRfcAction(ADD);
          chefConfig = writeChefConfig(wo, cookbookPath);
          writeChefRequest(wo, fileName);
          result =
              processRunner.executeProcessRetry(
                  new ExecutionContext(wo, cmd, logKey, woRetryCount));
          //
        }
      }
      String[] classParts = wo.getRfcCi().getCiClassName().split("\\.");
      String rfcAction = wo.getRfcCi().getRfcAction();
      if (result.getResultCode() != 0) {
        if (appName.equalsIgnoreCase(COMPUTE)
            && wo.getRfcCi().getRfcAction().equalsIgnoreCase(ADD)) {
          cleanupFailedCompute(result, wo);
        }
        String comments = getCommentsFromResult(result);
        logger.debug("setting to failed: " + wo.getDpmtRecordId());
        wo.setDpmtRecordState(FAILED);
        wo.setComments(comments);
        setLocalRetries(result);
        copySearchTagsFromResult(wo, result);
        // only do for compute::remote add and update for pre-versioned
        // classes
        // does some remote stuff like resolv.conf, dns, java, nagios,
        // and flume )
      } else if (classParts.length < 3
          && appName.equalsIgnoreCase(COMPUTE)
          && (rfcAction.equalsIgnoreCase(ADD) || rfcAction.equalsIgnoreCase(UPDATE))) {
        logger.info("classParts: " + classParts.length);
        copySearchTagsFromResult(wo, result);
        runComputeRemoteWorkOrder(wo, result, chefConfig);

      } else {
        // for delete/updates
        logger.debug("setting to complete: " + wo.getDpmtRecordId());
        wo.setDpmtRecordState(COMPLETE);
        wo.setComments("done");
        removeFile(chefConfig);

        setResultCi(result, wo);
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.info(
          "thread " + Thread.currentThread().getId() + " waiting for semaphore was interrupted.");
      runLocalWorkOrder(wo, appName, logKey, fileName, cookbookPath, attempt + 1);
    }
  }

  private ProcessResult runLocalWorkOrderWithCommand(
      CmsWorkOrderSimple wo,
      String logKey,
      String fileName,
      String cookbookPath,
      List<String> additionalCmdList) {
    String chefConfig = writeChefConfig(wo, cookbookPath);
    writeChefRequest(wo, fileName);
    String[] cmd = buildChefSoloCmd(fileName, chefConfig, isDebugEnabled(wo), additionalCmdList);
    ProcessResult result =
        processRunner.executeProcessRetry(new ExecutionContext(wo, cmd, logKey, 1));
    return result;
  }

  private void runComputeRemoteWorkOrder(
      CmsWorkOrderSimple wo, ProcessResult result, String chefConfig) {
    String host = null;
    // set the result status
    if (isPropagationUpdate(wo)) {
      host = wo.getRfcCi().getCiAttributes().get(config.getIpAttribute());
    } else {
      if (result.getResultMap().containsKey(config.getIpAttribute())) {
        host = result.getResultMap().get(config.getIpAttribute());
      } else {
        logger.error("resultCi missing " + config.getIpAttribute());
        return;
      }
    }

    String originalRfcAction = wo.getRfcCi().getRfcAction();
    List<CmsRfcCISimple> ciList = new ArrayList<CmsRfcCISimple>();
    CmsRfcCISimple manageViaCi = new CmsRfcCISimple();
    manageViaCi.addCiAttribute(config.getIpAttribute(), host);
    if (wo.getRfcCi().getCiAttributes().containsKey("proxy_map")) {
      manageViaCi.addCiAttribute("proxy_map", wo.getRfcCi().getCiAttributes().get("proxy_map"));
    }

    ciList.add(manageViaCi);
    wo.payLoad.put(MANAGED_VIA, ciList);

    setLocalRetries(result);

    wo.getRfcCi().setRfcAction(REMOTE);
    setResultCi(result, wo);

    // reset to state to failed
    // gets set to complete only if all parts of install_base &
    // remote are ok
    wo.setDpmtRecordState(FAILED);
    runWorkOrder(wo);

    wo.getRfcCi().setRfcAction(originalRfcAction);
    removeFile(chefConfig);

    if (wo.getDpmtRecordState().equalsIgnoreCase(FAILED)) {
      // cleanup failed add
      if (originalRfcAction.equalsIgnoreCase(ADD)) {
        cleanupFailedCompute(result, wo);
      }
      return;
    }

    logger.debug("setting to complete: " + wo.getDpmtRecordId());
    wo.setDpmtRecordState(COMPLETE);
    wo.setComments("done");
  }

  private void setLocalRetries(ProcessResult result) {
    Map<String, String> tagMap = result.getTagMap();
    if (tagMap.containsKey(CmsConstants.INDUCTOR_RETRIES)) {
      tagMap.put(CmsConstants.INDUCTOR_LOCAL_RETRIES, tagMap.get(CmsConstants.INDUCTOR_RETRIES));
      tagMap.remove(CmsConstants.INDUCTOR_RETRIES);
    }
  }

  /**
   * Cleanup (Deletes) the failed compute when <b>not</b> in debug mode
   *
   * @param result ProcessResult
   * @param wo CmsWorkOrderSimple
   */
  private void cleanupFailedCompute(ProcessResult result, CmsWorkOrderSimple wo) {
    // safety
    if (!wo.getRfcCi().getRfcAction().equals("add")
        && !wo.getRfcCi().getRfcAction().equals("replace")) {
      logger.info("not deleting because rfcAction: " + wo.getRfcCi().getRfcAction());
      return;
    }
    String instanceId = result.getResultMap().get("instance_id");
    if (!isDebugEnabled(wo)) {
      /*
       * compute::delete uses instance_id - which should be in the
       * resultMap
       */
      wo.getRfcCi().addCiAttribute("instance_id", instanceId);
      logger.error("Debug mode is disabled. Cleaning up the failed instance: " + instanceId);
      List<CmsRfcCISimple> ciList = new ArrayList<CmsRfcCISimple>();
      wo.payLoad.put(MANAGED_VIA, ciList);
      wo.getRfcCi().setRfcAction(ADD_FAIL_CLEAN);
      wo.getSearchTags().put("rfcAction", ADD_FAIL_CLEAN);
      runWorkOrder(wo);
    } else {
      logger.warn(
          "Debug mode is enabled. Leaving the instance: "
              + instanceId
              + " intact for troubleshooting.");
    }
    /* Set as failed */
    wo.setDpmtRecordState(FAILED);
    wo.setComments("failed in compute::add");
  }

  /**
   * Calls local or remote chef to do recipe [ciClassname::wo.getRfcCi().rfcAction]
   *
   * @param wo CmsWorkOrderSimple
   */
  private void runWorkOrder(CmsWorkOrderSimple wo) {

    Boolean isRemote = isRemoteChefCall(wo);
    // Remote WO file name to rsync to.
    String remoteFileName = getRemoteFileName(wo);
    String fileName = config.getDataDir() + "/" + wo.getDpmtRecordId() + ".json";
    logger.info("writing config to: " + fileName + " remote: " + remoteFileName);

    // Assume failed; gets set to COMPLETE at the end.
    wo.setDpmtRecordState(FAILED);
    String appName = normalizeClassName(wo);
    String logKey = getLogKey(wo);
    wo.putSearchTag("inductor", config.getIpAddr());

    writeChefRequest(wo, fileName);
    String cookbookPath = getCookbookPath(wo.getRfcCi().getCiClassName());
    logger.info("cookbookPath: " + cookbookPath);

    // sync cookbook and chef json request to remote site
    Set<String> serviceCookbookPaths = null;
    String host = null;
    String user = "oneops";
    String keyFile = null;
    String port = "22";

    if (isRemote) {
      host = getHost(wo, logKey);

      try {
        keyFile = writePrivateKey(wo);
      } catch (KeyNotFoundException e) {
        logger.error(e.getMessage());
        return;
      }

      if (host.contains(":")) {
        String[] parts = host.split(":");
        host = parts[0];
        port = parts[1];
        logger.info("using port from " + config.getIpAttribute());
      }

      long rsyncStartTime = System.currentTimeMillis();
      String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
      rsyncCmdLineWithKey[4] += "-p " + port + " -qi " + keyFile;

      // return with failure if empty
      if (host == null || host.isEmpty()) {
        wo.setComments("failed : missing host/ip cannot connect");
        removeFile(wo, keyFile);
        return;
      }

      String rfcAction = wo.getRfcCi().getRfcAction();
      logger.info(
          "rfc: "
              + wo.getRfcCi().getRfcId()
              + ", appName: "
              + appName
              + ", rfcAction: "
              + rfcAction);

      // v2 install base done via compute cookbook
      // compute::remote logic can be removed once v1 packs are decommed
      // skip base install for propagation updates
      if (appName.equalsIgnoreCase(COMPUTE)
          && rfcAction.equalsIgnoreCase(REMOTE)
          && !isPropagationUpdate(wo)) {

        logger.info(logKey + " ### BASE INSTALL");
        wo.setComments("");
        runBaseInstall(wo, host, port, logKey, keyFile);
        if (!wo.getComments().isEmpty()) {
          logger.info(logKey + " failed base install.");
          return;
        }
      }

      String baseDir = config.getCircuitDir().replace("packer", cookbookPath);
      String components = baseDir + "/components";
      String destination = "/home/" + user + "/" + cookbookPath;

      // always sync base cookbooks/modules
      String[] cmdLine =
          (String[])
              ArrayUtils.addAll(
                  rsyncCmdLineWithKey,
                  new String[] {components, user + "@" + host + ":" + destination});
      logger.info(logKey + " ### SYNC BASE: " + components);

      ProcessResult result =
          processRunner.executeProcessRetry(new ExecutionContext(wo, cmdLine, logKey, retryCount));
      if (result.getResultCode() > 0) {
        if (DELETE.equals(wo.getRfcCi().getRfcAction())) {
          List<CmsRfcCISimple> managedViaRfcs = wo.getPayLoad().get(MANAGED_VIA);
          if (managedViaRfcs != null
              && managedViaRfcs.size() > 0
              && DELETE.equals(managedViaRfcs.get(0).getRfcAction())) {
            if (failOnDeleteFailure(wo)) {
              logger.info(
                  logKey
                      + "wo failed due to unreachable compute, this component is set to fail on delete failures");
            } else {
              logger.warn(
                  logKey
                      + "wo failed due to unreachable compute, but marking ok due to ManagedVia rfcAction==delete");
              wo.setDpmtRecordState(COMPLETE);
            }
          } else {
            wo.setComments(
                "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
          }
        } else {
          wo.setComments(
              "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
        }

        handleRsyncFailure(wo, keyFile);
        return;
      }

      // rsync exec-order shared
      String sharedComponents = config.getCircuitDir().replace("packer", "shared/");
      destination = "/home/" + user + "/shared/";
      cmdLine =
          (String[])
              ArrayUtils.addAll(
                  rsyncCmdLineWithKey,
                  new String[] {sharedComponents, user + "@" + host + ":" + destination});
      logger.info(logKey + " ### SYNC SHARED: " + sharedComponents);

      result =
          processRunner.executeProcessRetry(new ExecutionContext(wo, cmdLine, logKey, retryCount));
      if (result.getResultCode() > 0) {
        inductorStat.addRsyncFailed();
        wo.setComments(
            "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
        handleRsyncFailure(wo, keyFile);
        return;
      }

      serviceCookbookPaths =
          syncServiceCookbooks(
              wo, cookbookPath, user, rsyncCmdLineWithKey, host, port, logKey, keyFile);

      // rsync  workorder
      cmdLine =
          (String[])
              ArrayUtils.addAll(
                  rsyncCmdLineWithKey,
                  new String[] {fileName, user + "@" + host + ":" + remoteFileName});
      logger.info(logKey + " ### SYNC: " + remoteFileName);

      result =
          processRunner.executeProcessRetry(new ExecutionContext(wo, cmdLine, logKey, retryCount));
      if (result.getResultCode() > 0) {
        wo.setComments(
            "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
        handleRsyncFailure(wo, keyFile);
        return;
      }

      wo.putSearchTag(
          CmsConstants.INDUCTOR_RSYNC_TIME,
          Long.toString(System.currentTimeMillis() - rsyncStartTime));
    }

    // run the chef command
    String[] cmd;
    if (isRemote) {
      String vars = getProxyEnvVars(wo);
      // exec-order.rb takes -d switch and 3 args: impl, json node
      // structure w/ work/actionorder, and cookbook path
      String debugFlag = "";
      if (isDebugEnabled(wo)) {
        debugFlag = "-d";
      }

      String additionalCookbookPaths = "";
      if (serviceCookbookPaths != null && serviceCookbookPaths.size() > 0) {
        additionalCookbookPaths = String.join(",", serviceCookbookPaths);
      }

      String remoteCmd =
          "sudo "
              + vars
              + " shared/exec-order.rb "
              + wo.getRfcCi().getImpl()
              + " "
              + remoteFileName
              + " "
              + cookbookPath
              + " "
              + additionalCookbookPaths
              + " "
              + debugFlag;

      cmd =
          (String[])
              ArrayUtils.addAll(
                  sshCmdLine, new String[] {keyFile, "-p " + port, user + "@" + host, remoteCmd});
      logger.info(logKey + " ### EXEC: " + user + "@" + host + " " + remoteCmd);
      int woRetryCount = getRetryCountForWorkOrder(wo);

      ProcessResult result =
          executeWorkOrderRemote(
              new ExecutionContext(wo, cmd, logKey, woRetryCount), fileName, cookbookPath);

      // set the result status
      if (result.getResultCode() != 0) {
        inductorStat.addWoFailed();
        // mark as complete when rfc and managed_via is DELETE
        if (DELETE.equals(wo.getRfcCi().getRfcAction())) {
          List<CmsRfcCISimple> managedViaRfcs = wo.getPayLoad().get(MANAGED_VIA);
          if (managedViaRfcs != null
              && managedViaRfcs.size() > 0
              && DELETE.equals(managedViaRfcs.get(0).getRfcAction())) {
            if (failOnDeleteFailure(wo)) {
              logger.info(logKey + "wo failed, this component is set to fail on delete failures");
            } else {
              logger.warn(logKey + "wo failed, but marking ok due to ManagedVia rfcAction==delete");
              wo.setDpmtRecordState(COMPLETE);
            }
          }
        } else {
          String comments = getCommentsFromResult(result);
          logger.error(logKey + comments);
          wo.setComments(comments);
        }

        removeRemoteWorkOrder(wo, keyFile, processRunner);
        removeFile(wo, keyFile);
        copySearchTagsFromResult(wo, result);
        return;
      }
      // remove remote workorder for success and failure.
      removeRemoteWorkOrder(wo, keyFile, processRunner);
      setResultCi(result, wo);

      wo.setDpmtRecordState(COMPLETE);
      removeFile(wo, keyFile);

    } else {
      runLocalWorkOrder(wo, appName, logKey, fileName, cookbookPath);
    }
    if (!isDebugEnabled(wo)) {
      removeFile(fileName);
    }
  }

  private ProcessResult executeWorkOrderRemote(
      ExecutionContext executionContext, String fileName, String cookbookPath) {
    ProcessResult result = processRunner.executeProcessRetry(executionContext);
    // if the vm is rebooting execute reboot_vm as a local workorder and then retry
    int rebootCount = 0;
    String logKey = executionContext.getLogKey();
    while (result.isRebooting() && rebootCount < config.getRebootLimit()) {
      rebootCount++;
      logger.info(logKey + " executing reboot_vm as local workorder, reboot count " + rebootCount);

      List<String> cmdList = new ArrayList<>();
      cmdList.add("-o");
      cmdList.add(REBOOT_RUN_LIST);

      ProcessResult rebootResult =
          runLocalWorkOrderWithCommand(
              (CmsWorkOrderSimple) executionContext.getWo(),
              executionContext.getLogKey(),
              fileName,
              cookbookPath,
              cmdList);
      if (rebootResult.getResultCode() != 0) {
        logger.info(logKey + " reboot_vm failed " + rebootResult.getResultCode());
        return rebootResult;
      } else {
        logger.info(logKey + " executing workorder again after reboot_vm");
        result = processRunner.executeProcessRetry(executionContext);
      }
    }
    if (rebootCount > 0) {
      logger.info(logKey + " number of reboots attempted : " + rebootCount);
    }
    return result;
  }

  private String[] buildChefSoloCmd(
      String fileName, String chefConfig, boolean debug, List<String> additionalArgs) {
    List<String> cmd = buildDefaultChefSolo(fileName, chefConfig, debug);
    cmd.addAll(additionalArgs);
    return cmd.toArray(new String[cmd.size()]);
  }

  private boolean isPropagationUpdate(CmsWorkOrderSimple wo) {
    if (StringUtils.isNotBlank(wo.getRfcCi().getHint())) {
      RfcHint hint = gson.fromJson(wo.getRfcCi().getHint(), RfcHint.class);
      if ("true".equalsIgnoreCase(hint.getPropagation())) {
        logger.info("propagation true for rfc " + wo.getRfcCi().getRfcId());
        return true;
      }
    }
    return false;
  }

  private Set<String> syncServiceCookbooks(
      CmsWorkOrderSimple wo,
      String woBomCircuit,
      String user,
      String[] rsyncCmdLineWithKey,
      String host,
      String port,
      String logKey,
      String keyFile) {
    logger.info("checking for any service cookbook to be rsynched ..");
    // rsync cloud services cookbooks
    String cloudName = getCloudName(wo);
    Set<String> serviceCookbookPaths = new HashSet<>();
    Map<String, Map<String, CmsCISimple>> services = wo.getServices();
    if (services != null) {
      for (String serviceName : services.keySet()) { // for each service
        CmsCISimple serviceCi = services.get(serviceName).get(cloudName);
        if (serviceCi != null) {
          String serviceClassName = serviceCi.getCiClassName();
          String serviceCookbookCircuit = getCookbookPath(serviceClassName);
          if (!serviceCookbookCircuit.equals(woBomCircuit)) {
            // this service class is not in the same circuit as that of the bom ci getting deployed.
            // Go ahead and include the cookbook of this service to rsync to remote
            String serviceCookbookBaseDir =
                config.getCircuitDir().replace("packer", serviceCookbookCircuit);
            String serviceClassNameShort =
                serviceClassName.substring(serviceClassName.lastIndexOf(".") + 1);
            String serviceCookbookPath =
                serviceCookbookBaseDir
                    + "/components/cookbooks/"
                    + serviceClassNameShort.toLowerCase()
                    + "/";
            logger.info("service-serviceCookbookPath: " + serviceCookbookPath);
            if (new File(serviceCookbookPath).exists()) {
              String serviceCookbookCircuitPath =
                  "/home/" + user + "/" + serviceCookbookCircuit + "/components/cookbooks";
              serviceCookbookPaths.add(serviceCookbookCircuitPath);
              String destination =
                  serviceCookbookCircuitPath + "/" + serviceClassNameShort.toLowerCase() + "/";

              String remoteCmd = "mkdir -p " + destination;
              String[] cmd =
                  (String[])
                      ArrayUtils.addAll(
                          sshCmdLine,
                          new String[] {keyFile, "-p " + port, user + "@" + host, remoteCmd});
              logger.info(logKey + " ### EXEC: " + user + "@" + host + " " + remoteCmd);
              ProcessResult result =
                  processRunner.executeProcessRetry(
                      new ExecutionContext(wo, cmd, getLogKey(wo), getRetryCountForWorkOrder(wo)));
              if (result.getResultCode() != 0) {
                logger.error(logKey + " Error while creating service cookbook directory on remote");
              }

              String[] cmdLine =
                  (String[])
                      ArrayUtils.addAll(
                          rsyncCmdLineWithKey,
                          new String[] {
                            serviceCookbookPath, user + "@" + host + ":" + destination
                          });

              logger.info(logKey + " ### SYNC Service cookbook: " + serviceCookbookPath);

              result =
                  processRunner.executeProcessRetry(
                      new ExecutionContext(wo, cmdLine, logKey, retryCount));
              if (result.getResultCode() > 0) {
                wo.setComments(
                    "FATAL: "
                        + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
                return null;
              }

            } else {
              logger.warn("Cookbook " + serviceCookbookPath + " does not exist on this inductor");
            }
          }
        }
      }
    }
    return serviceCookbookPaths;
  }

  private boolean failOnDeleteFailure(CmsWorkOrderSimple wo) {
    String attrValue = wo.getBox().getCiAttributes().get(FAIL_ON_DELETE_FAILURE_ATTR);
    if (StringUtils.isNotBlank(attrValue)) {
      if (attrValue.startsWith("[") && attrValue.endsWith("]")) {
        attrValue = attrValue.substring(1, attrValue.length() - 1);
        String[] classes = attrValue.split(",");
        for (String clazz : classes) {
          if (wo.getRfcCi().getCiClassName().equals(clazz.trim())) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * Installs base software needed for chef / oneops
   *
   * @param wo CmsWorkOrderSimple
   * @param host remote host
   * @param port remote port
   */
  public void runBaseInstall(
      CmsWorkOrderSimple wo, String host, String port, String logKey, String keyFile) {
    long t1 = System.currentTimeMillis();
    // amazon public images use ubuntu user for ubuntu os
    String cloudName = getCloudName(wo);
    String osType;
    if (wo.getPayLoad().containsKey("DependsOn")
        && wo.getPayLoad().get("DependsOn").get(0).getCiClassName().contains("Compute")) {
      osType = wo.getPayLoad().get("DependsOn").get(0).getCiAttributes().get("ostype");
    } else {
      osType = wo.getRfcCi().getCiAttributes().get("ostype");
    }

    if (osType.equals("default-cloud")) {

      if (!wo.getServices().containsKey("compute")) {
        wo.setComments("missing compute service");
        return;
      }

      osType = wo.getServices().get("compute").get(cloudName).getCiAttributes().get("ostype");
      logger.info("using default-cloud ostype: " + osType);
    }
    String user = getUserForOsAndCloud(osType, wo);

    String sudo = "";
    if (!user.equals("root")) {
      sudo = "sudo ";
    }

    String setup = "";

    // rackspace images don't have rsync installed
    if (wo.getCloud().getCiName().indexOf("rackspace") > -1) {
      setup = "yum -d0 -e0 -y install rsync; apt-get -y install rsync; true; ";
      // fedora in aws needs it too
    } else if (osType.indexOf("edora") > -1) {
      setup = "sudo yum -d0 -e0 -y install rsync; ";
    }

    // make prerequisite dirs for /opt/oneops and cookbooks
    String prepCmdline =
        String.format(
            "%s%smkdir -p /opt/oneops/workorder /home/%s/components;%schown -R %s:%s /opt/oneops;%schown -R %s:%s /home/%s/components",
            setup, sudo, user, sudo, user, user, sudo, user, user, user);

    // double -t args are needed
    String[] cmd =
        (String[])
            ArrayUtils.addAll(
                sshInteractiveCmdLine,
                new String[] {keyFile, "-p " + port, user + "@" + host, prepCmdline});

    // retry initial ssh 10x slow hypervisors hosts
    ProcessResult result = processRunner.executeProcessRetry(cmd, logKey, 10);
    if (result.getResultCode() > 0) {
      wo.setComments("failed : can't:" + prepCmdline);
      return;
    }

    // install os package repos - repo_map keyed by os
    ArrayList<String> repoCmdList = new ArrayList<>();
    if (wo.getServices().containsKey("compute")
        && wo.getServices().get("compute").get(cloudName).getCiAttributes().containsKey("repo_map")
        && wo.getServices()
                .get("compute")
                .get(cloudName)
                .getCiAttributes()
                .get("repo_map")
                .indexOf(osType)
            > 0) {

      String repoMap =
          wo.getServices().get("compute").get(cloudName).getCiAttributes().get("repo_map");
      repoCmdList = getRepoListFromMap(repoMap, osType);
    } else {
      logger.warn("no key in repo_map for os: " + osType);
    }

    // add repo_list from compute
    if (wo.getRfcCi().getCiAttributes().containsKey("repo_list")) {
      repoCmdList.addAll(getRepoList(wo.getRfcCi().getCiAttributes().get("repo_list")));
    }

    if (repoCmdList.size() > 0) {

      String[] cmdTmp =
          (String[])
              ArrayUtils.addAll(
                  sshInteractiveCmdLine, new String[] {keyFile, "-p " + port, user + "@" + host});

      // add ";" to each cmd
      for (int i = 0; i < repoCmdList.size(); i++) {
        repoCmdList.set(i, repoCmdList.get(i) + "; ");
      }

      // add infront so env can be set before repo cmds
      repoCmdList.add(0, getProxyEnvVars(wo));
      cmd = (String[]) ArrayUtils.addAll(cmdTmp, repoCmdList.toArray());

      result = processRunner.executeProcessRetry(cmd, logKey, retryCount);
      if (result.getResultCode() > 0) {
        wo.setComments("failed : Replace the compute and retry the deployment");
        wo.putSearchTag(BASE_INSTALL_TIME, Long.toString(System.currentTimeMillis() - t1));
        return;
      }
    }

    // put ci cookbooks. "/" needed to get around symlinks
    String cookbookPath = getCookbookPath(wo.getRfcCi().getCiClassName());
    String cookbook = config.getCircuitDir().replace("packer", cookbookPath) + "/";
    String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
    rsyncCmdLineWithKey[4] += "-p " + port + " -qi " + keyFile;
    String[] deploy =
        (String[])
            ArrayUtils.addAll(
                rsyncCmdLineWithKey,
                new String[] {cookbook, user + "@" + host + ":/home/" + user + "/" + cookbookPath});

    result = processRunner.executeProcessRetry(deploy, logKey, retryCount);
    if (result.getResultCode() > 0) {
      wo.setComments(
          "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
      wo.putSearchTag(BASE_INSTALL_TIME, Long.toString(System.currentTimeMillis() - t1));
      return;
    }

    // put shared cookbooks
    cookbook = config.getCircuitDir().replace("packer", "shared") + "/";
    rsyncCmdLineWithKey = rsyncCmdLine.clone();
    rsyncCmdLineWithKey[4] += "-p " + port + " -qi " + keyFile;
    deploy =
        (String[])
            ArrayUtils.addAll(
                rsyncCmdLineWithKey,
                new String[] {cookbook, user + "@" + host + ":/home/" + user + "/shared"});

    result = processRunner.executeProcessRetry(deploy, logKey, retryCount);
    if (result.getResultCode() > 0) {
      wo.setComments(
          "FATAL: " + generateRsyncErrorMessage(result.getResultCode(), host + ":" + port));
      wo.putSearchTag(BASE_INSTALL_TIME, Long.toString(System.currentTimeMillis() - t1));
      return;
    }

    // install base: oneops user, ruby, chef double -t args are needed
    String[] classParts = wo.getRfcCi().getCiClassName().split("\\.");
    String baseComponent = classParts[classParts.length - 1].toLowerCase();
    String[] cmdTmp =
        (String[])
            ArrayUtils.addAll(
                sshInteractiveCmdLine,
                new String[] {
                  keyFile,
                  "-p " + port,
                  user + "@" + host,
                  sudo
                      + "/home/"
                      + user
                      + "/"
                      + cookbookPath
                      + "/components/cookbooks/"
                      + baseComponent
                      + "/files/default/install_base.sh"
                });

    // Add Cloud Provider here
    String[] proxyList = new String[] {getProxyBashVars(wo), "provider:" + getProvider(wo)};
    cmd = (String[]) ArrayUtils.addAll(cmdTmp, proxyList);

    result = processRunner.executeProcessRetry(cmd, logKey, retryCount);
    wo.putSearchTag(BASE_INSTALL_TIME, Long.toString(System.currentTimeMillis() - t1));
    if (result.getResultCode() > 0) {
      wo.setComments("failed : can't run install_base.sh");
      return;
    }
  }

  public String getProvider(CmsWorkOrderSimple wo) {
    String className = StringUtils.EMPTY;
    String cloudName = getCloudName(wo);
    if (wo.getServices().containsKey("compute")
        && wo.getServices().get("compute").get(cloudName).getCiClassName() != null) {
      className =
          getShortenedClass(wo.getServices().get("compute").get(cloudName).getCiClassName());
    }
    logger.info("Using provider :" + className);
    return className;
  }

  /**
   * getRepoList: gets list of repos from a json string
   *
   * @param jsonReposArray String
   */
  private ArrayList<String> getRepoList(String jsonReposArray) {
    JsonReader reader = new JsonReader(new StringReader(jsonReposArray));
    reader.setLenient(true);
    ArrayList<String> repos = gson.fromJson(reader, ArrayList.class);
    if (repos == null) {
      repos = new ArrayList<>();
    }
    return repos;
  }

  /** getRepoListFromMap: gets list of repos from a json string by os */
  private ArrayList<String> getRepoListFromMap(String jsonReposMap, String os) {
    Map<String, String> repoMap = gson.fromJson(jsonReposMap, Map.class);
    ArrayList<String> repos = new ArrayList<>();
    if (repoMap != null && repoMap.containsKey(os)) {
      repos.add(repoMap.get(os));
    }

    return repos;
  }

  private int getRetryCountForWorkOrder(CmsWorkOrderSimple wo) {
    int effectiveRetryCount = retryCount;
    if (wo.getRfcCi().getCiAttributes().containsKey("workorder_retry_count")) {
      effectiveRetryCount =
          Integer.valueOf(wo.getRfcCi().getCiAttributes().get("workorder_retry_count"));
    }
    return effectiveRetryCount;
  }

  /**
   * Removes the remote work order after remote execution
   *
   * @param wo remote work order to be removed.
   * @param keyFile file to be used for executing the remote ssh
   * @param pr the process runner.
   */
  private void removeRemoteWorkOrder(CmsWorkOrderSimple wo, String keyFile, ProcessRunner pr) {
    String user = ONEOPS_USER;
    String comments = "";
    if (!isDebugEnabled(wo)) {
      // clear the workorder files
      String logKey = getLogKey(wo);
      String host = getHost(wo, getLogKey(wo));
      String port = "22";
      if (host.contains(":")) {
        String[] parts = host.split(":");
        host = parts[0];
        port = parts[1];
        logger.info("using port from " + config.getIpAttribute());
      }

      String remoteCmd = "rm " + getRemoteFileName(wo);
      String[] cmd =
          (String[])
              ArrayUtils.addAll(
                  sshCmdLine, new String[] {keyFile, "-p " + port, user + "@" + host, remoteCmd});
      logger.info(logKey + " ### EXEC: " + user + "@" + host + " " + remoteCmd);
      ProcessResult result =
          pr.executeProcessRetry(
              new ExecutionContext(wo, cmd, getLogKey(wo), getRetryCountForWorkOrder(wo)));
      if (result.getResultCode() != 0) {
        // Not throwing exceptions, Should be ok if we are not able to
        // remove remote wo.
        logger.error(logKey + comments);
      } else {
        logger.info("removed remote workorder");
      }
    } else {
      logger.info("debug enabled, not removing remote workorders");
    }
  }

  /**
   * Get host from the work-order
   *
   * @param o CmsWorkOrderSimple
   * @param logKey String
   */
  public String getHost(CmsWorkOrderSimpleBase o, String logKey) {

    CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;
    String host = null;

    // Databases are ManagedVia Cluster - use fqdn of the Cluster for the host
    if (wo.getPayLoad().containsKey(MANAGED_VIA)) {
      CmsRfcCISimple hostCi = wo.getPayLoad().get(MANAGED_VIA).get(0);
      if (wo.getRfcCi().getCiClassName().matches(BOM_CLASS_PREFIX + "Cluster")) {
        List<CmsRfcCISimple> hosts = wo.getPayLoad().get(MANAGED_VIA);
        @SuppressWarnings("rawtypes")
        Iterator i = hosts.iterator();
        while (i.hasNext()) {
          CmsRfcCISimple ci = (CmsRfcCISimple) i.next();
          if (ci.getCiName().endsWith("1")) {
            hostCi = ci;
          }
        }
      }

      if (hostCi.getCiClassName() != null
          && hostCi.getCiClassName().matches(BOM_CLASS_PREFIX + "Ring")) {

        String[] ips = hostCi.getCiAttributes().get("dns_record").split(",");
        if (ips.length > 0) {
          host = ips[0];
        } else {
          logger.error("ring dns_record has no values");
          wo.setComments("failed : can't get ip for: " + hostCi.getCiName());
          return null;
        }

      } else if (hostCi.getCiClassName() != null
          && hostCi.getCiClassName().matches(BOM_CLASS_PREFIX + "Cluster")) {

        if ((hostCi.getCiAttributes().containsKey("shared_type")
                && hostCi.getCiAttributes().get("shared_type").equalsIgnoreCase("ip"))
            || (hostCi.getCiAttributes().containsKey(InductorConstants.SHARED_IP)
                && !hostCi.getCiAttributes().get(InductorConstants.SHARED_IP).isEmpty())) {
          host = hostCi.getCiAttributes().get(InductorConstants.SHARED_IP);
        } else {
          // override with env

          host = wo.getBox().getCiName() + "." + getCustomerDomain(wo);
          logger.info("ManagedVia cluster host:" + host);

          // get the list from route53 / dns service
          List<String> authoritativeDnsServers = getAuthoritativeServers(wo);

          // workaround for osx mDNSResponder cache issue -
          // http://serverfault.com/questions/64837/dns-name-lookup-was-ssh-not-working-after-snow-leopard-upgrade
          int randomInt = randomGenerator.nextInt(authoritativeDnsServers.size() - 1);
          String[] digCmd = new String[] {"/usr/bin/dig", "+short", host};

          if (randomInt > -1) {
            digCmd =
                new String[] {
                  "/usr/bin/dig", "+short", host, "@" + authoritativeDnsServers.get(randomInt)
                };
          }

          ProcessResult result = processRunner.executeProcessRetry(digCmd, logKey, retryCount);
          if (result.getResultCode() > 0) {
            return null;
          }

          if (!result.getStdOut().equalsIgnoreCase("")) {
            host = result.getStdOut().trim();
          } else {
            wo.setComments("failed : can't get ip for: " + hostCi.getCiName());
            return null;
          }
        }
        logger.info("using cluster host ip:" + host);

      } else {
        host = hostCi.getCiAttributes().get(config.getIpAttribute());
        logger.info("using ManagedVia " + config.getIpAttribute() + ": " + host);
      }
    }

    return host;
  }

  /**
   * getProxyBashVars: gets proxy bash vars
   *
   * @param wo CmsWorkOrderSimple
   */
  private String getProxyBashVars(CmsWorkOrderSimple wo) {
    String vars = "";
    ArrayList<String> proxyList = new ArrayList<String>();

    // use proxy_map from compute cloud service
    String cloudName = getCloudName(wo);
    if (wo.getServices().containsKey("compute")
        && wo.getServices()
            .get("compute")
            .get(cloudName)
            .getCiAttributes()
            .containsKey("env_vars")) {

      updateProxyListBash(
          proxyList,
          wo.getServices().get("compute").get(cloudName).getCiAttributes().get("env_vars"));
    }

    // get http proxy by managed_via
    CmsRfcCISimple getMgmtCi = wo.getPayLoad().get(MANAGED_VIA).get(0);

    if (getMgmtCi.getCiAttributes().containsKey("proxy_map")) {

      String jsonProxyHash = getMgmtCi.getCiAttributes().get("proxy_map");
      updateProxyListBash(proxyList, jsonProxyHash);
    }

    for (String proxy : proxyList) {
      vars += proxy + " ";
    }

    return vars;
  }

  /**
   * getProxyBashVars: gets proxy env vars
   *
   * @param wo CmsWorkOrderSimple
   */
  protected String getProxyEnvVars(CmsWorkOrderSimple wo) {
    String vars = "";
    ArrayList<String> proxyList = new ArrayList<>();
    Map<String, String> proxyMap = new HashMap<>();

    // use proxy_map from compute cloud service
    String cloudName = getCloudName(wo);
    if (wo.getServices().containsKey("compute")
        && wo.getServices()
            .get("compute")
            .get(cloudName)
            .getCiAttributes()
            .containsKey("env_vars")) {
      proxyMap =
          gson.fromJson(
              wo.getServices().get("compute").get(cloudName).getCiAttributes().get("env_vars"),
              Map.class);
    } else if (wo.getConfig() != null && wo.getConfig().get("env_vars") != null) {
      proxyMap = gson.fromJson(wo.getConfig().get("env_vars"), Map.class);
    }

    // get http proxy by managed_via
    CmsRfcCISimple getMgmtCi = wo.getPayLoad().get(MANAGED_VIA).get(0);

    if (getMgmtCi.getCiAttributes().containsKey("proxy_map")) {
      String jsonProxyHash = getMgmtCi.getCiAttributes().get("proxy_map");
      Map<String, String> computeProxyMap = gson.fromJson(jsonProxyHash, Map.class);
      proxyMap.putAll(computeProxyMap);
    }
    updateProxyList(proxyList, proxyMap);
    for (String proxy : proxyList) {
      vars += proxy + " ";
    }

    return vars + "class=" + normalizeClassName(wo) + " pack=" + getCookbookPath(wo.getClassName());
  }

  /**
   * getUserForOsAndCloud: get username based on os and cloud provider
   *
   * @param osType String
   * @returns username
   */
  private String getUserForOsAndCloud(String osType, CmsWorkOrderSimple wo) {

    String cloudName = getCloudName(wo);
    if (wo.getServices().containsKey("compute")
        && wo.getServices()
            .get("compute")
            .get(cloudName)
            .getCiAttributes()
            .containsKey("initial_user")) {

      String user =
          wo.getServices().get("compute").get(cloudName).getCiAttributes().get("initial_user");
      logger.info("using initial username from compute service: " + user);
      return user;
    }

    // override via config
    if (config.getInitialUser() != null && !config.getInitialUser().equals("unset")) {
      return config.getInitialUser();
    }

    String user = "root";
    // ubuntu supplied image (vagrant/ec2) use ubuntu
    if (osType.indexOf("buntu") > -1
        &&
        // rackspace uses root for all images
        wo.getCloud().getCiAttributes().get("location").indexOf("rackspace") == -1) {

      user = "ubuntu";
    }

    // ibm uses idcuser
    if (wo.getCloud().getCiName().contains("ibm.")) {
      user = "idcuser";
    }

    if (osType.indexOf("edora") > -1 || osType.indexOf("mazon") > -1) {
      user = "ec2-user";
    }
    return user;
  }

  /**
   * writeChefConfig: creates a chef config file for unique lockfile by ci. returns chef config full
   * path
   *
   * @param wo CmsWorkOrderSimple
   * @returns chef config full path
   */
  private String writeChefConfig(CmsWorkOrderSimple wo, String cookbookRelativePath) {
    String logLevel = "info";
    if (isDebugEnabled(wo)) {
      logLevel = "debug";
    }
    return writeChefConfig(
        Long.toString(wo.getRfcCi().getCiId()),
        cookbookRelativePath,
        wo.getCloud().getCiName(),
        wo.getServices(),
        logLevel);
  }

  /**
   * removeFile - removes a file with uuid checking
   *
   * @param filename String
   */
  private void removeFile(CmsWorkOrderSimple wo, String filename) {
    if (!isDebugEnabled(wo)) {
      removeFile(filename);
    }
  }

  /**
   * setResultCi: uses the result map to set attributes of the resultCi
   *
   * @param result ProcessResult
   * @param wo CmsWorkOrderSimple
   */
  private void setResultCi(ProcessResult result, CmsWorkOrderSimple wo) {

    CmsCISimple resultCi = new CmsCISimple();

    if (result.getResultMap().size() > 0) {
      resultCi.setCiAttributes(result.getResultMap());
    }
    mergeRfcToResult(wo.getRfcCi(), resultCi);

    wo.setResultCi(resultCi);
    String resultForLog = "";
    int i = 0;
    Map<String, String> attrs = resultCi.getCiAttributes();
    for (String key : attrs.keySet()) {
      if (i > 0) {
        resultForLog += ", ";
      }
      // no printing of private key
      if (!key.equalsIgnoreCase("private")) {
        resultForLog += key + "=" + attrs.get(key);
      }
      i++;
    }
    logger.debug("resultCi attrs:" + resultForLog);

    // put tags from result / recipe OutputHandler
    wo.getSearchTags().putAll(result.getTagMap());
    wo.getAdditionalInfo().putAll(result.getAdditionInfoMap());
  }

  protected long getStubSleepTime(CmsWorkOrderSimpleBase wo) {
    long sleepTime = 0;
    Map<String, String> envVars = config.getEnvVars();
    if (envVars != null) {
      String className = wo.getClassName();
      String var = STUB_RESP_COMPONENT_PREFIX + wo.getAction() + "." + className;
      if (envVars.containsKey(var)) {
        sleepTime =
            Integer.valueOf(envVars.get(var))
                + randomGenerator.nextInt(config.getStubResponseTimeInSeconds());
      }
      logger.info(
          "sleep for stub cloud, class : "
              + className
              + " action : "
              + wo.getAction()
              + " time : "
              + sleepTime);
    }
    if (sleepTime == 0) {
      return super.getStubSleepTime(wo);
    }
    return sleepTime;
  }

  public MetricRegistry getRegistry() {
    return registry;
  }

  public void setRegistry(MetricRegistry registry) {
    this.registry = registry;
  }
}
