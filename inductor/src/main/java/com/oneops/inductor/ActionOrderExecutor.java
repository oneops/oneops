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

import static com.oneops.cms.util.CmsConstants.ESCORTED_BY;
import static com.oneops.cms.util.CmsConstants.MANAGED_VIA;
import static com.oneops.cms.util.CmsConstants.RESPONSE_ENQUE_TS;
import static com.oneops.cms.util.CmsConstants.SEARCH_TS_PATTERN;
import static com.oneops.inductor.InductorConstants.COMPUTE;
import static com.oneops.inductor.InductorConstants.ENVIRONMENT;
import static com.oneops.inductor.InductorConstants.ERROR_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.OK_RESPONSE_CODE;
import static com.oneops.inductor.InductorConstants.ONEOPS_USER;
import static com.oneops.inductor.InductorConstants.SHARED_IP;
import static org.apache.commons.httpclient.util.DateUtil.formatDate;

import com.google.gson.JsonSyntaxException;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * ActionOrder specific processing
 */
public class ActionOrderExecutor extends AbstractOrderExecutor {

  private static Logger logger = Logger.getLogger(ActionOrderExecutor.class);
  private Semaphore semaphore;
  private Config config;

  public ActionOrderExecutor(Config config, Semaphore semaphore) {
    super(config);
    this.config = config;
    this.semaphore = semaphore;
  }

  /**
   * processActionOrder - process the actionorder and return message to be put in the controller
   * response queue
   *
   * @param o workorder
   * @param correlationId jms correlation Id
   */
  @Override
  public Map<String, String> process(CmsWorkOrderSimpleBase o, String correlationId) {

    CmsActionOrderSimple ao = (CmsActionOrderSimple) o;
    long startTime = System.currentTimeMillis();
    CmsCISimple resultCi = new CmsCISimple();
    mergeCiToResult(ao.getCi(), resultCi);
    ao.setResultCi(resultCi);

    if (config.isCloudStubbed(ao)) {
      assembleRequest(ao);
      //delay the processing as configured and return response.
      processStubbedCloud(ao);
      logger.info("completing ao without doing anything because cloud is stubbed");
    } else {
      // creates the json chefRequest and exec's chef to run chef
      // local or remote via ssh/mc
      runActionOrder(ao);
    }

    // state and resultCI gets set via chef response
    // serialize and send to controller
    String responseCode = OK_RESPONSE_CODE;
    if (!isStateComplete(ao)) {
      logger.warn("FAIL: " + ao.getActionId() + " state:" + ao.getActionState());
      responseCode = ERROR_RESPONSE_CODE;
      ao.setActionState(OpsActionState.failed);
    }
    long endTime = System.currentTimeMillis();
    int duration = Math.round((endTime - startTime) / 1000);
    logger.info(ao.getActionName() + " " + ao.getCi().getCiClassName()
        + " " + ao.getCi().getCiName() + " took: " + duration + "sec");
    setTotalExecutionTime(ao, endTime - startTime);
    ao.putSearchTag(RESPONSE_ENQUE_TS, formatDate(new Date(), SEARCH_TS_PATTERN));
    String responseText = gson.toJson(ao);
    logger.info("{ ResultCode: " + responseCode + ", JMSCorrelationID: " + correlationId + " }");

    // Controller will process this message
    Map<String, String> message = new HashMap<>();
    message.put("body", responseText);
    message.put("correlationID", correlationId);
    message.put("task_result_code", responseCode);
    message.put("priority", "oneops-autorepair".equals(ao.getCreatedBy()) ? "regular" : "high");
    return message;
  }

  private boolean isStateComplete(CmsActionOrderSimple ao) {
    return ao.getActionState().equals(OpsActionState.complete);
  }

  @Override
  protected List<String> getRunList(CmsWorkOrderSimpleBase wo) {
    return getRunList(wo, getAppName(wo), getAction(wo));
  }

  @Override
  protected String getAppName(CmsWorkOrderSimpleBase ao) {
    String appName = normalizeClassName(ao);
    String action = ao.getAction();
    if (action.equalsIgnoreCase("user-custom-attachment")) {
      appName = "attachment";
    }
    return appName;
  }

  private ArrayList<String> getRunList(CmsWorkOrderSimpleBase ao, String appName, String action) {
    ArrayList<String> runList = new ArrayList<>();
    runList.add(getRunListEntry(appName, action));
    if (!ONDEMAND.equals(action) && shouldAddAttachment(ao)) {
      // only run attachments on remote calls
      runList.add(0, getRunListEntry("attachment", "before"));
      runList.add(getRunListEntry("attachment", "after"));
    }
    return runList;
  }


  protected boolean shouldAddAttachment(CmsWorkOrderSimpleBase ao) {
    String className = normalizeClassName(ao);
    boolean isManagedVia = ao.isPayLoadEntryPresent(MANAGED_VIA);
    boolean isEscortedBy = ao.isPayLoadEntryPresent(ESCORTED_BY);
    logger.debug("isManagedVia" + isManagedVia + "isEscortedBy" + isEscortedBy + " compute");
    return !(className.equals(COMPUTE)) && isManagedVia && isEscortedBy;
  }

  /**
   * Gets host from action order
   *
   * @param o action order
   * @param logKey log key
   * @return hostname
   */
  public String getHost(CmsWorkOrderSimpleBase o, String logKey) {
    String host;
    CmsActionOrderSimple ao = (CmsActionOrderSimple) o;

    // Databases are ManagedVia Cluster - use fqdn of the Cluster for the host
    if (ao.getActionName().equalsIgnoreCase("user-custom-attachment")) {
      //CmsCISimple hostCi = getPayLoadCI(ao, MANAGED_VIA);
      CmsCISimple hostCi = ao.getPayLoadEntryAt(MANAGED_VIA, 0);
      if (hostCi == null) {
        if (isWorkOrderOfCompute(ao)) {
          hostCi = ao.getCi();
        }
      }
      if (hostCi == null) {
        logger.info("no managed via for ao(" + ao.getActionName() + ")" + ao.getNsPath());
        return null;
      }
      host = hostCi.getCiAttributes().get(config.getIpAttribute());
      logger.info("using  host ip for custom-attachment:" + host);
    } else {
      CmsCISimple hostCi = ao.getPayLoadEntryAt(MANAGED_VIA, 0);
      // return null for local action orders
      if (hostCi == null) {
        logger.info("no managed via for ao(" + ao.getActionName() + ")" + ao.getNsPath());
        return null;
      }
      if (hostCi.getCiClassName() != null && hostCi.getCiClassName()
          .equalsIgnoreCase("bom.Cluster")) {
        if (isAttributeNotEmpty(hostCi, SHARED_IP)) {
          host = getAttribute(hostCi, SHARED_IP);//hostCi.getCiAttributes().get(SHARED_IP);
        } else {

          CmsCISimple env = ao.getPayLoadEntryAt(ENVIRONMENT, 0);
          if (env == null) {
            throw new IllegalArgumentException(
                "ao for this action (" + ao.getActionName() + ") for ns " + ao.getNsPath());
          }
          String cloudName = ao.getCloud().getCiName();
          CmsCISimple cloudService = ao.getServices().get("dns").get(cloudName);
          host = ao.getBox().getCiName() + "." + getCustomerDomain(cloudService, env);
          logger.info("ManagedVia cluster host:" + host);
          // get the list from route53 / dns service
          List<String> authoritativeDnsServers = getAuthoritativeServers(ao);
          // workaround for osx mDNSResponder cache issue:
          // http://serverfault.com/questions/64837/dns-name-lookup-was-ssh-not-working-after-snow-leopard-upgrade
          int randomInt = randomGenerator.nextInt(authoritativeDnsServers.size() - 1);
          String[] digCmd = new String[]{"/usr/bin/dig", "+short", host};
          if (randomInt > -1) {
            digCmd = new String[]{"/usr/bin/dig", "+short", host,
                "@" + authoritativeDnsServers.get(randomInt)};
          }
          ProcessResult result = processRunner.executeProcessRetry(digCmd, logKey, retryCount);
          if (result.getResultCode() > 0) {
            return null;
          }
          if (!result.getStdOut().equalsIgnoreCase("")) {
            host = result.getStdOut().trim();
            logger.info("using cluster host ip:" + host);
          }
        }
      } else {
        host = hostCi.getCiAttributes().get(config.getIpAttribute());
      }
    }
    logger.info("using  host ip for ao(" + ao.getActionName() + ")" + host);
    return host;
  }

  private String getAttribute(CmsCISimple hostCi, String sharedIp) {
    return hostCi.getCiAttributes().get(SHARED_IP);
  }

  private boolean isAttributeNotEmpty(CmsCISimple hostCi, String attributeName) {
    return hostCi.getCiAttributes().containsKey(
        attributeName) && !hostCi
        .getCiAttributes()
        .get(attributeName).isEmpty();
  }

  /**
   * Runs action orders on the inductor box, ex compute::reboot
   *
   * @param pr {@code ProcessRunner}
   * @param ao action order
   */
  private void runLocalActionOrder(ProcessRunner pr,
      CmsActionOrderSimple ao, String appName,
      String logKey, String fileName,
      String cookbookPath, int attempt) throws JsonSyntaxException, IOException {

    String chefConfig = writeChefConfig(ao, cookbookPath);
    String[] cmd = buildChefSoloCmd(fileName, chefConfig,
        isDebugEnabled(ao));

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

      setLocalWaitTime(ao);

      try {
        logger.info(logKey + " ### EXEC: localhost "
            + StringUtils.join(cmd, " "));
        ProcessResult result = pr.executeProcessRetry(cmd, logKey,
            retryCount);

        removeFile(chefConfig);
        setResultCi(result, ao);

        if (result.getResultCode() != 0) {
          ao.setActionState(OpsActionState.failed);
        } else {
          ao.setActionState(OpsActionState.complete);
        }
      } finally {
        semaphore.release();
      }

    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      logger.info("thread " + Thread.currentThread().getId()
          + " waiting for semaphore was interrupted.");
      runLocalActionOrder(pr, ao, appName, logKey, fileName,
          cookbookPath, attempt + 1);
    }

  }

  private void runLocalActionOrder(ProcessRunner pr, CmsActionOrderSimple ao,
      String appName, String logKey, String fileName, String cookbookPath)
      throws JsonSyntaxException, IOException {

    runLocalActionOrder(pr, ao, appName, logKey, fileName, cookbookPath, 1);
  }

  /**
   * Calls local or remote chef to do recipe [ciClassname::wo.getRfcCi().rfcAction]
   *
   * @param ao action order
   */
  private void runActionOrder(CmsActionOrderSimple ao) {

    // file-based request keyed by deployment record id - remotely by
    // class.ciName for ease of debug
    String remoteFileName = getRemoteFileName(ao);
    String fileName = config.getDataDir() + "/" + ao.getActionId() + ".json";
    logger.info("writing config to:" + fileName + " remote: " + remoteFileName);

    // build a chef request
    Map<String, Object> chefRequest = assembleRequest(ao);
    String appName = (String) chefRequest.get("app_name");

    String logKey = getLogKey(ao);
    logger.info(logKey + " Inductor: " + config.getIpAddr());
    ao.getSearchTags().put("inductor", config.getIpAddr());

    // assume failed; gets set to COMPLETE at the end
    ao.setActionState(OpsActionState.failed);

    String cookbookPath = getCookbookPath(ao.getCi().getCiClassName());
    logger.info("cookbookPath: " + cookbookPath);

    try {
      // write the request to a .json file
      //Get the file reference
      writeChefRequestToFile(fileName, chefRequest);

      // sync cookbook and chef json request to remote site
      String host = getHost(ao, logKey);
      String user = ONEOPS_USER;

      // run local when no managed via
      if (host == null || host.isEmpty()) {
        runLocalActionOrder(processRunner, ao, appName, logKey, fileName, cookbookPath);
        removeFile(fileName);
        return;
      }
      String keyFile;
      try {
        keyFile = writePrivateKey(ao);
      } catch (KeyNotFoundException e) {
        logger.error(e.getMessage());
        return;
      }

      String port = "22";
      if (host.contains(":")) {
        String[] parts = host.split(":");
        host = parts[0];
        port = parts[1];
      }

      String baseDir = config.getCircuitDir().replace("packer", cookbookPath);
      String components = baseDir + "/components";
      String destination = "/home/" + user + "/" + cookbookPath;
      String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
      rsyncCmdLineWithKey[4] += "-p " + port + " -qi " + keyFile;

      // always sync base cookbooks/modules
      String[] cmdLine = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
          new String[]{components,
              user + "@" + host + ":" + destination});
      logger.info(logKey + " ### SYNC BASE: " + components);
      ExecutionContext ctx = new ExecutionContext(ao, cmdLine, logKey, host, keyFile, retryCount);
      boolean rsynchFailed = rsynch(ctx);
      if (rsynchFailed) {
        return;
      }

      // rsync exec-order shared
      components = config.getCircuitDir().replace("packer", "shared/");
      destination = "/home/" + user + "/shared/";
      cmdLine = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
          new String[]{components,
              user + "@" + host + ":" + destination});
      //add new command in the context
      ctx.setCmd(cmdLine);
      logger.info(logKey + " ### SYNC SHARED: " + components);

      rsynchFailed = rsynch(ctx);
      if (rsynchFailed) {
        return;
      }

      // put actionorder
      cmdLine = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
          new String[]{fileName,
              user + "@" + host + ":" + remoteFileName});
      //add new command in the context
      ctx.setCmd(cmdLine);
      logger.info(logKey + " ### SYNC: " + remoteFileName);
      rsynchFailed = rsynch(ctx);
      if (rsynchFailed) {
        return;
      }

      String debugFlag = getDebugFlag(ao);
      // run the chef command
      String remoteCmd = String
          .format("sudo %s shared/exec-order.rb %s %s %s %s"," class="+ normalizeClassName(ao) +" pack=" + getCookbookPath(ao.getClassName()), ao.getCi().getImpl(), remoteFileName,
              cookbookPath, debugFlag);
      String[] cmd;
      cmd = (String[]) ArrayUtils
          .addAll(sshCmdLine, new String[]{keyFile, "-p " + port, user + "@" + host, remoteCmd});
      logger.info(logKey + " ### EXEC: " + user + "@" + host + " " + remoteCmd);

      ProcessResult result = processRunner.executeProcessRetry(cmd, logKey);
      // set the result status
      if (result.getResultCode() != 0) {
        logger.debug(logKey
            + "setting to failed - got non-zero exitStatus from remote chef");
        return;
      }
      // compute resultCi gets populated on compute add (down further)
      // and is already there
      if (!appName.equalsIgnoreCase(InductorConstants.COMPUTE)) {
        setResultCi(result, ao);
      }

      ao.setActionState(OpsActionState.complete);
      removeFile(ao, keyFile);

    } catch (IOException e) {
      logger.error(e);
      e.printStackTrace();
    }
    if (!isDebugEnabled(ao)) {
      removeFile(fileName);
    }
  }

  private void writeChefRequestToFile(String fileName, Map<String, Object> chefRequest)
      throws IOException {
    try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(fileName))) {
      writer.write(gsonPretty.toJson(chefRequest));
    }
  }

  /**
   * WriteChefConfig: creates a chef config file for unique lockfile by ci. returns chef config full
   * path
   *
   * @return chef config file path
   */
  private String writeChefConfig(CmsActionOrderSimple ao,
      String cookbookRelativePath) {
    String logLevel = "info";
    if (isDebugEnabled(ao)) {
      logLevel = "debug";
    }
    return writeChefConfig(Long.toString(ao.getCi().getCiId()),
        cookbookRelativePath, ao.getCloud().getCiName(), ao.getServices(), logLevel);
  }


  /**
   * Uses the result map to set attributes of the resultCi
   */
  private void setResultCi(ProcessResult result, CmsActionOrderSimple ao) {
    CmsCISimple resultCi = new CmsCISimple();
    if (result.getResultMap().size() > 0) {
      resultCi.setCiAttributes(result.getResultMap());
    }
    mergeCiToResult(ao.getCi(), resultCi);
    ao.setResultCi(resultCi);
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
    ao.getSearchTags().putAll(result.getTagMap());
  }
}
