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

import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsUtil;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.Semaphore;

import static com.oneops.inductor.InductorConstants.*;

/**
 * 
 * WorkOrder specific processing
 *
 */
public class WorkOrderExecutor extends AbstractOrderExecutor {

	private Semaphore semaphore = null;

	public WorkOrderExecutor(Config config, Semaphore semaphore) {
		super(config);
		this.config = config;
		this.semaphore = semaphore;
	}

	private static Logger logger = Logger.getLogger(WorkOrderExecutor.class);
	private Config config = null;

	/**
	 * Process workorder and return message to be put in
	 * the controller response queue
	 *
	 * @param o             CmsWorkOrderSimpleBase
	 * @param correlationId JMS correlation Id
	 * @returns Map<String, String> message
	 */
	@Override
	public Map<String, String> process(CmsWorkOrderSimpleBase o,
									   String correlationId) throws IOException {

		CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;

		// compute::replace will do a delete and add - only for old
		// pre-versioned compute
		String[] classParts = wo.getRfcCi().getCiClassName().split("\\.");
		if (classParts.length < 3
				&& wo.getRfcCi().getCiClassName().equals("bom.Compute")
				&& wo.getRfcCi().getRfcAction().equals("replace")) {

			logger.info("compute::replace - delete then add");
			wo.getRfcCi().setRfcAction("delete");
			process(wo, correlationId);
			if (wo.getDpmtRecordState().equals(COMPLETE)) {
				if (wo.getRfcCi().getCiAttributes().containsKey("instance_id"))
					wo.getRfcCi().getCiAttributes().remove("instance_id");

				wo.getRfcCi().setRfcAction("add");
			} else {
				logger.info("compute::replace - delete failed");

				return buildResponseMessage(wo, correlationId);
			}

		}
		long startTime = System.currentTimeMillis();
		if (config.isCloudStubbed(wo)) {
			processStubbedCloud(wo);
			logger.info("completing wo without doing anything because cloud is stubbed");
		} else {
			// skip fqdn workorder if dns is disabled
				if (config.isDnsDisabled()
					&& wo.getRfcCi().getCiClassName().equals("bom.Fqdn")) {
				wo.setDpmtRecordState(COMPLETE);
				CmsCISimple resultCi = new CmsCISimple();

				mergeRfcToResult(wo.getRfcCi(), resultCi);
				wo.setResultCi(resultCi);

				logger.info("completing wo without doing anything because dns is off");
			} else {
				// creates the json chefRequest and exec's chef to run chef
				// local or remote via ssh/mc
				runWorkOrder(wo);
			}
		}
		long endTime = System.currentTimeMillis();

		int duration = Math.round((endTime - startTime) / 1000);
		logger.info(wo.getRfcCi().getRfcAction() + " "
				+ wo.getRfcCi().getImpl() + " "
				+ wo.getRfcCi().getCiClassName() + " "
				+ wo.getRfcCi().getCiName() + " took: " + duration + "sec");

		setTotalExecutionTime(wo, endTime - startTime);
		wo.getSearchTags()
				.put(CmsConstants.RESPONSE_ENQUE_TS,
						DateUtil.formatDate(new Date(),
								CmsConstants.SEARCH_TS_PATTERN));
		return buildResponseMessage(wo, correlationId);
	}

	/**
	 * Assemble the json request for chef
	 */
	public Map<String, Object> assembleRequest(CmsWorkOrderSimpleBase o) {
		CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;
		String appName = normalizeClassName(wo);

		Map<String, Object> chefRequest = new HashMap<String, Object>();
		Map<String, String> global = new HashMap<String, String>();

		ArrayList<String> runList = new ArrayList<String>();

		// component recipe
		String action = wo.getRfcCi().getRfcAction();

		runList.add("recipe[" + appName + "::" + action + "]");

		// if remotely executed
		if (isRemoteChefCall(wo)) {

			// monitors, but only remote wo's
			if (wo.getPayLoad().containsKey(WATCHED_BY)) {
				if (action.equals(REMOTE)) {
					runList.add("recipe[monitor::add]");
				} else {
					runList.add("recipe[monitor::" + action + "]");
				}
			}

			// custom logging
			if (wo.getPayLoad().containsKey(LOGGED_BY)) {
				if (action.equals(REMOTE)) {
					runList.add("recipe[log::add]");
				} else {
					runList.add("recipe[log::" + action + "]");
				}
			}

			if (!(appName.equals(COMPUTE) && action.equals(REMOTE))) {
				// only run attachments on remote calls
				runList.add(0, "recipe[attachment::before_" + action + "]");
				runList.add("recipe[attachment::after_" + action + "]");
			}

		} else {
			// this is to call global monitoring service from the inductor host 
			// or gen config on inductor to handle new compute
			if (wo.getServices().get("monitoring") != null ||
				wo.getRfcCi().getCiClassName().matches("bom\\..*\\..*\\.Compute")) {
				runList.add("recipe[monitor::" + action + "]");
			}
		}

		chefRequest.put(appName, wo.getRfcCi().getCiAttributes());

		// default dns domain to oneops.me
		CmsRfcCISimple env = null;
		if (wo.getPayLoad().containsKey(ENVIRONMENT)) {
			env = wo.getPayLoad().get(ENVIRONMENT).get(0);
		}

		String cloudName = wo.getCloud().getCiName();
		CmsCISimple cloudService = new CmsCISimple();
		if (wo.getServices().containsKey("dns"))
			cloudService = wo.getServices().get("dns").get(cloudName);

		chefRequest.put("customer_domain", getCustomerDomain(cloudService, env));
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
		chefRequest.put("name", wo.getRfcCi().getCiName());

		// set mgmt cert
		if (config.getMgmtCertContent() != null)
			chefRequest.put("mgmt_cert", config.getMgmtCertContent());
		
		//set perf-collector cert
		if (config.getPerfCollectorCertContent() != null)
			chefRequest.put("perf_collector_cert", config.getPerfCollectorCertContent());

		return chefRequest;
	}

	/**
	 * buildResponseMessage - builds the response message for the controller
	 *
	 * @param wo            CmsWorkOrderSimple
	 * @param correlationId String
	 * @returns Map<String, String> message
	 */
	private Map<String, String> buildResponseMessage(CmsWorkOrderSimple wo,
													 String correlationId) {

		// state and resultCI gets set via chef response
		// serialize and send to controller
		String responseCode = "200";
		String responseText = gson.toJson(wo);

		if (!wo.getDpmtRecordState().equalsIgnoreCase(COMPLETE)) {
			logger.warn("FAIL: " + wo.getDpmtRecordId() + " state:"
					+ wo.getDpmtRecordState());
			responseCode = "500";
		}

		if ("replace".equals(wo.getRfcCi().getCiState())
				&& "delete".equals(wo.getRfcCi().getRfcAction())) {
			// Don't mask and log the wo text for replace::delete WOs
			logger.info("{ \"resultCode\": " + responseCode + ", "
					+ " \"JMSCorrelationID:\": \"" + correlationId + " }");
		} else {
			// Mask secured fields before logging
			CmsUtil.maskSecuredFields(wo, CmsUtil.WORK_ORDER_TYPE);
			String logResponseText = gson.toJson(wo);

			// InductorLogSink will process this message
			logger.info("{ \"resultCode\": " + responseCode + ", "
					+ " \"JMSCorrelationID:\": \"" + correlationId + "\", "
					+ "\"responseWorkorder\": " + logResponseText + " }");
		}

		Map<String, String> message = new HashMap<String, String>();
		message.put("body", responseText);
		message.put("correlationID", correlationId);
		message.put("task_result_code", responseCode);
		return message;
	}

	/**
	 * Runs work orders on the inductor box, ex compute::add
	 */
	private void runLocalWorkOrder(ProcessRunner pr, CmsWorkOrderSimple wo,
			String appName, String logKey, String fileName, String cookbookPath)
			throws JsonSyntaxException {
		runLocalWorkOrder(pr, wo, appName, logKey, fileName, cookbookPath, 1);
	}

	private void runLocalWorkOrder(ProcessRunner pr, CmsWorkOrderSimple wo,
			String appName, String logKey, String fileName,
			String cookbookPath, int attempt) throws JsonSyntaxException {

		String chefConfig = writeChefConfig(wo, cookbookPath);

		// run local - iaas calls to provision vm
		String[] cmd = buildChefSoloCmd(fileName, chefConfig,
				isDebugEnabled(wo));
		logger.info(logKey + " ### EXEC: localhost "
				+ StringUtils.join(cmd, " "));

		int woRetryCount = getRetryCountForWorkOrder(wo);
		;

		if (attempt > retryCount) {
			logger.error("hit max retries - kept getting InterruptedException");
			return;
		}

		try {
			boolean hasQueueOnLock = semaphore.hasQueuedThreads();
			if (hasQueueOnLock)
				logger.info(logKey + " waiting for semaphore...");
			semaphore.acquire();
			if (hasQueueOnLock)
				logger.info(logKey + " got semaphore");
			setLocalWaitTime(wo);
			ProcessResult result = null;
			try {
				result = pr.executeProcessRetry(wo, cmd, logKey, woRetryCount, config);
			} finally {
				semaphore.release();
			}
			// try to recover from a bad compute
			if (result.getResultCode() != 0
					&& appName.equalsIgnoreCase(COMPUTE)
					&& wo.getRfcCi().getRfcAction().equalsIgnoreCase(ADD)) {
				if (result.getFaultMap().containsKey(KNOWN)) {
					logger.info("deleting and retrying compute because known issue: "
							+ result.getFaultMap().get(KNOWN));
					cleanupFailedCompute(result, wo);
					wo.getRfcCi().getCiAttributes().remove("instance_id");
					wo.getRfcCi().setRfcAction(ADD);
					chefConfig = writeChefConfig(wo, cookbookPath);
					writeChefRequest(wo, fileName);
					result = pr
							.executeProcessRetry(wo, cmd, logKey, retryCount, config);
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
				// only do for compute::remote add and update for pre-versioned
				// classes
				// does some remote stuff like resolv.conf, dns, java, nagios,
				// and flume )
			} else if (classParts.length < 3
					&& appName.equalsIgnoreCase(COMPUTE)
					&& (rfcAction.equalsIgnoreCase(ADD) || rfcAction
							.equalsIgnoreCase(UPDATE))) {
				logger.info("classParts: " + classParts.length);
				String host = null;
				// set the result status
				if (result.getResultMap().containsKey(config.getIpAttribute())) {
					host = result.getResultMap().get(config.getIpAttribute());
				} else {
					logger.error("resultCi missing " + config.getIpAttribute());
					return;
				}

				String originalRfcAction = wo.getRfcCi().getRfcAction();
				List<CmsRfcCISimple> ciList = new ArrayList<CmsRfcCISimple>();
				CmsRfcCISimple manageViaCi = new CmsRfcCISimple();
				manageViaCi.addCiAttribute(config.getIpAttribute(), host);
				if (wo.getRfcCi().getCiAttributes().containsKey("proxy_map"))
					manageViaCi.addCiAttribute("proxy_map", wo.getRfcCi()
							.getCiAttributes().get("proxy_map"));

				ciList.add(manageViaCi);
				wo.payLoad.put(MANAGED_VIA, ciList);
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
					if (originalRfcAction.equalsIgnoreCase(ADD))
						cleanupFailedCompute(result, wo);
					return;
				}

				logger.debug("setting to complete: " + wo.getDpmtRecordId());
				wo.setDpmtRecordState(COMPLETE);
				wo.setComments("done");

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
			logger.info("thread " + Thread.currentThread().getId()
					+ " waiting for semaphore was interrupted.");
			runLocalWorkOrder(pr, wo, appName, logKey, fileName, cookbookPath,
					attempt + 1);
		}

	}

	/**
	 * Cleanup (Deletes) the failed compute when <b>not</b> in debug mode
	 *
	 * @param result ProcessResult
	 * @param wo     CmsWorkOrderSimple
	 */
	private void cleanupFailedCompute(ProcessResult result,
									  CmsWorkOrderSimple wo) {
		// safety
		if (!wo.getRfcCi().getRfcAction().equals("add")
				&& !wo.getRfcCi().getRfcAction().equals("replace")) {
			logger.info("not deleting because rfcAction: "
					+ wo.getRfcCi().getRfcAction());
			return;
		}
		String instanceId = result.getResultMap().get("instance_id");
		if (!isDebugEnabled(wo)) {
			/*
			 * compute::delete uses instance_id - which should be in the
			 * resultMap
			 */
			wo.getRfcCi().addCiAttribute("instance_id", instanceId);
			logger.error("Debug mode is disabled. Cleaning up the failed instance: "
					+ instanceId);
			List<CmsRfcCISimple> ciList = new ArrayList<CmsRfcCISimple>();
			wo.payLoad.put(MANAGED_VIA, ciList);
			wo.getRfcCi().setRfcAction(ADD_FAIL_CLEAN);
			wo.getSearchTags().put("rfcAction", ADD_FAIL_CLEAN);
			runWorkOrder(wo);
		} else {
			logger.warn("Debug mode is enabled. Leaving the instance: "
					+ instanceId + " intact for troubleshooting.");
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

		// check to see if should be remote by looking at proxy
		Boolean isRemote = isRemoteChefCall(wo);

		// file-based request keyed by deployment record id - remotely by
		// class.ciName for ease of debug
		String remoteFileName = getRemoteFileName(wo);
		String fileName = config.getDataDir() + "/" + wo.getDpmtRecordId()
				+ ".json";
		logger.info("writing config to: " + fileName + " remote: "
				+ remoteFileName);

		// assume failed; gets set to COMPLETE at the end
		wo.setDpmtRecordState(FAILED);

		String appName = normalizeClassName(wo);

		String logKey = getLogKey(wo);
		logger.info(logKey + " Inductor: " + config.getIpAddr());
		wo.getSearchTags().put("inductor", config.getIpAddr());

		writeChefRequest(wo, fileName);

		String cookbookPath = getCookbookPath(wo.getRfcCi().getCiClassName());
		logger.info("cookbookPath: " + cookbookPath);

		// sync cookbook and chef json request to remote site
		String host = null;
		String user = "oneops";

		String keyFile = null;
		String port = "22";

		if (isRemote) {

			host = getWorkOrderHost(wo, logKey);

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

			String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
			rsyncCmdLineWithKey[5] += "-p " + port + " -qi " + keyFile;

			// return with failure if empty
			if (host == null || host.isEmpty()) {
				wo.setComments("failed : missing host/ip cannot connect");
				removeFile(wo, keyFile);
				return;
			}

			String rfcAction = wo.getRfcCi().getRfcAction();

			logger.info("appName: " + appName);
			logger.info("rfcAction: " + rfcAction);

			// v2 install base done via compute cookbook
			// compute::remote logic can be removed once v1 packs are decommed
			if (appName.equalsIgnoreCase(COMPUTE) && 	
				 rfcAction.equalsIgnoreCase(REMOTE)) {
				
				logger.info(logKey + " ### BASE INSTALL");
				wo.setComments("");
				runBaseInstall(procR, wo, host, port, logKey, keyFile);
				if (!wo.getComments().isEmpty()) {
					logger.info(logKey + " failed base install.");
					return;
				}
			}

			String baseDir = config.getCircuitDir().replace("packer",
					cookbookPath);
			String components = baseDir + "/components";
			String destination = "/home/" + user + "/" + cookbookPath;

			// always sync base cookbooks/modules
			String[] deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
					new String[] { components,
							user + "@" + host + ":" + destination });
			logger.info(logKey + " ### SYNC BASE: " + components);

			if (!host.equals(TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(wo, deploy,
						logKey, retryCount, config);
				if (result.getResultCode() > 0) {
					
					if(DELETE.equals(wo.getRfcCi().getRfcAction())) {
						List<CmsRfcCISimple> managedViaRfcs = wo.getPayLoad().get(MANAGED_VIA);
						if (managedViaRfcs != null && managedViaRfcs.size() > 0 
								&& DELETE.equals(managedViaRfcs.get(0).getRfcAction())) {
							logger.warn(logKey + "wo failed due to unreachable compute, but marking ok due to ManagedVia rfcAction==delete");
							wo.setDpmtRecordState(COMPLETE);
						} else {
							wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
						}
					} else {
						wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
					}
					
					removeFile(wo, keyFile);
					return;
				}
			}

			// rsync exec-order shared
			components = config.getCircuitDir().replace("packer", "shared/");
			destination = "/home/" + user + "/shared/";
			deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
					new String[] { components,
							user + "@" + host + ":" + destination });
			logger.info(logKey + " ### SYNC SHARED: " + components);

			if (!host.equals(TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(wo, deploy,
						logKey, retryCount, config);
				if (result.getResultCode() > 0) {
					wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
					removeFile(wo, keyFile);
					return;
				}
			}

			// put workorder
			deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
					new String[] { fileName,
							user + "@" + host + ":" + remoteFileName });
			logger.info(logKey + " ### SYNC: " + remoteFileName);
			if (!host.equals(TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(wo, deploy,
						logKey, retryCount, config);
				if (result.getResultCode() > 0) {
					wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
					removeFile(wo, keyFile);
					return;
				}
			}
		}

		// run the chef command
		String[] cmd = null;
		if (isRemote) {
			String vars = getProxyEnvVars(wo);
			// exec-order.rb takes -d switch and 3 args: impl, json node
			// structure w/ work/actionorder, and cookbook path
			String debugFlag = "";
			if (isDebugEnabled(wo)) {
				debugFlag = "-d";
			}
			String remoteCmd = "sudo " + vars + " shared/exec-order.rb "
					+ wo.getRfcCi().getImpl() + " " + remoteFileName + " "
					+ cookbookPath + " " + debugFlag;

			cmd = (String[]) ArrayUtils.addAll(sshCmdLine, new String[] {
					keyFile, "-p " + port, user + "@" + host, remoteCmd });
			logger.info(logKey + " ### EXEC: " + user + "@" + host + " "
					+ remoteCmd);
			int woRetryCount = getRetryCountForWorkOrder(wo);
			if (!host.equals(TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(wo, cmd,
						logKey, woRetryCount, config);
				
				// set the result status
				if (result.getResultCode() != 0) {
					// mark as complete when rfc and managed_via is DELETE
					if(DELETE.equals(wo.getRfcCi().getRfcAction())) {
						List<CmsRfcCISimple> managedViaRfcs = wo.getPayLoad().get(MANAGED_VIA);
						if (managedViaRfcs != null && managedViaRfcs.size() > 0
								&& DELETE.equals(managedViaRfcs.get(0).getRfcAction())) {
							logger.warn(logKey + "wo failed, but marking ok due to ManagedVia rfcAction==delete");
							wo.setDpmtRecordState(COMPLETE);
						}
					}else {
						String comments = getCommentsFromResult(result);
						logger.error(logKey + comments);
					}

					removeRemoteWorkOrder(wo, keyFile, procR);
					removeFile(wo, keyFile);
					return;
				}
				// remove remote workorder for success and failure.
				removeRemoteWorkOrder(wo, keyFile, procR);
				setResultCi(result, wo);
			}

			wo.setDpmtRecordState(COMPLETE);
			removeFile(wo, keyFile);

		} else {
			runLocalWorkOrder(procR, wo, appName, logKey, fileName,
					cookbookPath);
		}
		if (!isDebugEnabled(wo))
			removeFile(fileName);
	}

	/**
	 * Installs base software needed for chef / oneops
	 *
	 * @param pr      ProcessRunner
	 * @param wo      CmsWorkOrderSimple
	 * @param host    remote host
	 * @param port    remote port
	 * @param logKey
	 * @param keyFile
	 */
	public void runBaseInstall(ProcessRunner pr, CmsWorkOrderSimple wo,
							   String host, String port, String logKey, String keyFile) {
		// amazon public images use ubuntu user for ubuntu os
		String cloudName = wo.getCloud().getCiName();
		String osType = "";
		if (wo.getPayLoad().containsKey("DependsOn")
				&& wo.getPayLoad().get("DependsOn").get(0).getCiClassName()
						.contains("Compute"))
			osType = wo.getPayLoad().get("DependsOn").get(0).getCiAttributes()
					.get("ostype");
		else
			osType = wo.getRfcCi().getCiAttributes().get("ostype");

		if (osType.equals("default-cloud")) {

			if (!wo.getServices().containsKey("compute")) {
				wo.setComments("missing compute service");
				return;
			}

			osType = wo.getServices().get("compute").get(cloudName)
					.getCiAttributes().get("ostype");
			logger.info("using default-cloud ostype: " + osType);
		}
		String user = getUserForOsAndCloud(osType, wo);

		String sudo = "";
		if (!user.equals("root"))
			sudo = "sudo ";

		String setup = "";

		// rackspace images don't have rsync installed
		if (wo.getCloud().getCiName().indexOf("rackspace") > -1) {
			setup = "yum -d0 -e0 -y install rsync; apt-get -y install rsync; true; ";
			// fedora in aws needs it too
		} else if (osType.indexOf("edora") > -1) {
			setup = "sudo yum -d0 -e0 -y install rsync; ";
		}

		// make prerequisite dirs for /opt/oneops and cookbooks
		String prepCmdline = setup + sudo
				+ "mkdir -p /opt/oneops/workorder /home/" + user
				+ "/components" + ";" + sudo + "chown -R " + user + ":" + user
				+ " /opt/oneops;" + sudo + "chown -R " + user + ":" + user
				+ " /home/" + user + "/components";

		// double -t args are needed
		String[] cmd = (String[]) ArrayUtils.addAll(sshInteractiveCmdLine,
				new String[] { keyFile, "-p " + port, user + "@" + host,
						prepCmdline });

		// retry initial ssh 10x slow hypervisors hosts
		if (!host.equals(TEST_HOST)) {
			ProcessResult result = pr.executeProcessRetry(cmd, logKey, 10);
			if (result.getResultCode() > 0) {
				wo.setComments("failed : can't:" + prepCmdline);
				return;
			}
		}

		// install os package repos - repo_map keyed by os
		ArrayList<String> repoCmdList = new ArrayList<String>();
		if (wo.getServices().containsKey("compute")
				&& wo.getServices().get("compute").get(cloudName)
						.getCiAttributes().containsKey("repo_map")
				&& wo.getServices().get("compute").get(cloudName)
						.getCiAttributes().get("repo_map").indexOf(osType) > 0) {

			String repoMap = wo.getServices().get("compute").get(cloudName)
					.getCiAttributes().get("repo_map");
			repoCmdList = getRepoListFromMap(repoMap, osType);
		} else {
			logger.warn("no key in repo_map for os: " + osType);
		}

		// add repo_list from compute
		if (wo.getRfcCi().getCiAttributes().containsKey("repo_list")) {
			repoCmdList.addAll(getRepoList(wo.getRfcCi().getCiAttributes()
					.get("repo_list")));
		}

		if (repoCmdList.size() > 0) {

			String[] cmdTmp = (String[]) ArrayUtils.addAll(
					sshInteractiveCmdLine, new String[] { keyFile,
							"-p " + port, user + "@" + host });

			// add ";" to each cmd
			for (int i = 0; i < repoCmdList.size(); i++) {
				repoCmdList.set(i, repoCmdList.get(i) + "; ");
			}

			// add infront so env can be set before repo cmds
			repoCmdList.add(0, getProxyEnvVars(wo));

			cmd = (String[]) ArrayUtils.addAll(cmdTmp, repoCmdList.toArray());
			if (!host.equals(TEST_HOST)) {
				ProcessResult result = pr.executeProcessRetry(cmd, logKey,
						retryCount);
				if (result.getResultCode() > 0) {
					wo.setComments("failed : Replace the compute and retry the deployment");
					return;
				}
			}
		}

		// put ci cookbooks. "/" needed to get around symlinks
		String cookbookPath = getCookbookPath(wo.getRfcCi().getCiClassName());
		String cookbook = config.getCircuitDir()
				.replace("packer", cookbookPath) + "/";
		String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
		rsyncCmdLineWithKey[3] += "-p " + port + " -qi " + keyFile;
		String[] deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
				new String[] {
						cookbook,
						user + "@" + host + ":/home/" + user + "/"
								+ cookbookPath });
		if (!host.equals(TEST_HOST)) {
			ProcessResult result = pr.executeProcessRetry(deploy, logKey,
					retryCount);
			if (result.getResultCode() > 0) {
				wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
				return;
			}
		}

		// put shared cookbooks
		cookbook = config.getCircuitDir().replace("packer", "shared") + "/";
		rsyncCmdLineWithKey = rsyncCmdLine.clone();
		rsyncCmdLineWithKey[3] += "-p " + port + " -qi " + keyFile;
		deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
				new String[] { cookbook,
						user + "@" + host + ":/home/" + user + "/shared" });
		if (!host.equals(TEST_HOST)) {
			ProcessResult result = pr.executeProcessRetry(deploy, logKey,
					retryCount);
			if (result.getResultCode() > 0) {
				wo.setComments("FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host+":"+port));
				return;
			}
		}

		// install base: oneops user, ruby, chef
		// double -t args are needed
		String[] classParts = wo.getRfcCi().getCiClassName().split("\\.");
		String baseComponent = classParts[classParts.length - 1].toLowerCase();
		String[] cmdTmp = (String[]) ArrayUtils.addAll(sshInteractiveCmdLine,
				new String[]{
						keyFile,
						"-p " + port,
						user + "@" + host,
						sudo + "/home/" + user + "/" + cookbookPath
								+ "/components/cookbooks/" + baseComponent
								+ "/files/default/install_base.sh"});

		String[] proxyList = new String[] { getProxyBashVars(wo) };
		cmd = (String[]) ArrayUtils.addAll(cmdTmp, proxyList);

		if (!host.equals(TEST_HOST)) {
			ProcessResult result = pr.executeProcessRetry(cmd, logKey,
					retryCount);
			if (result.getResultCode() > 0) {
				wo.setComments("failed : can't run install_base.sh");
				return;
			}
		}

	}

	/**
	 * getRepoList: gets list of repos from a json string
	 *
	 * @param jsonReposArray String
	 */
	private ArrayList<String> getRepoList(String jsonReposArray) {

		JsonReader reader = new JsonReader(new StringReader(jsonReposArray));
		reader.setLenient(true);

		ArrayList<String> repos = gson.fromJson(reader,
				ArrayList.class);
		if (repos == null)
			repos = new ArrayList<String>();

		return repos;
	}

	/**
	 * getRepoListFromMap: gets list of repos from a json string by os
	 *
	 * @param jsonReposMap
	 * @param os
	 */
	private ArrayList<String> getRepoListFromMap(String jsonReposMap, String os) {

		JsonReader reader = new JsonReader(new StringReader(jsonReposMap));
		reader.setLenient(true);

		HashMap<String, String> repoMap = gson.fromJson(reader,
				HashMap.class);

		ArrayList<String> repos = new ArrayList<String>();

		if (repoMap != null && repoMap.containsKey(os))
			repos.add(repoMap.get(os));

		return repos;
	}

	private int getRetryCountForWorkOrder(CmsWorkOrderSimple wo) {
		int effectiveRetryCount = retryCount;
		if (wo.getRfcCi().getCiAttributes()
				.containsKey("workorder_retry_count")) {
			effectiveRetryCount = Integer.valueOf(wo.getRfcCi()
					.getCiAttributes().get("workorder_retry_count"));
		}
		return effectiveRetryCount;
	}

	
	/**
	 * Removes the remote work order after remote execution
	 * 
	 * @param wo
	 *            remote work order to be removed.
	 * @param keyFile
	 *            file to be used for executing the remote ssh
	 * @param pr
	 *            the process runner.
	 */
	private void removeRemoteWorkOrder(CmsWorkOrderSimple wo, String keyFile,
			ProcessRunner pr) {
		String user = ONEOPS_USER;
		String comments = "";
		if (!isDebugEnabled(wo)) {
			// clear the workorder files
			String logKey = getLogKey(wo);
			String host = getWorkOrderHost(wo, getLogKey(wo));
			String port = "22";
			if (host.contains(":")) {
				String[] parts = host.split(":");
				host = parts[0];
				port = parts[1];
				logger.info("using port from " + config.getIpAttribute());
			}

			String remoteCmd = "rm " + getRemoteFileName(wo);
			String[] cmd = (String[]) ArrayUtils.addAll(sshCmdLine,
					new String[] { keyFile, "-p " + port, user + "@" + host,
							remoteCmd });
			logger.info(logKey + " ### EXEC: " + user + "@" + host + " "
					+ remoteCmd);
			ProcessResult result = pr.executeProcessRetry(wo, cmd,
					getLogKey(wo), getRetryCountForWorkOrder(wo), config);
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
	 * getWorkOrderHost: gets host from the workorder
	 *
	 * @param wo     CmsWorkOrderSimple
	 * @param logKey String
	 */
	private String getWorkOrderHost(CmsWorkOrderSimple wo, String logKey) {
		String host = null;

		// Databases are ManagedVia Cluster - use fqdn of the Cluster for the
		// host
		if (wo.getPayLoad().containsKey(InductorConstants.MANAGED_VIA)) {

			CmsRfcCISimple hostCi = wo.getPayLoad()
					.get(InductorConstants.MANAGED_VIA).get(0);

			if (wo.getRfcCi().getCiClassName().equalsIgnoreCase("bom.Cluster")) {
				List<CmsRfcCISimple> hosts = wo.getPayLoad().get(
						InductorConstants.MANAGED_VIA);
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
					&& hostCi.getCiClassName().equalsIgnoreCase("bom.Ring")) {

				String[] ips = hostCi.getCiAttributes().get("dns_record")
						.split(",");
				if (ips.length > 0) {
					host = ips[0];
				} else {
					logger.error("ring dns_record has no values");
					wo.setComments("failed : can't get ip for: "
							+ hostCi.getCiName());
					return null;
				}

			} else if (hostCi.getCiClassName() != null
					&& hostCi.getCiClassName().equalsIgnoreCase("bom.Cluster")) {

				if ((hostCi.getCiAttributes().containsKey("shared_type") && hostCi
						.getCiAttributes().get("shared_type")
						.equalsIgnoreCase("ip"))
						|| (hostCi.getCiAttributes().containsKey(
								InductorConstants.SHARED_IP) && !hostCi
								.getCiAttributes()
								.get(InductorConstants.SHARED_IP).isEmpty())) {
					host = hostCi.getCiAttributes().get(
							InductorConstants.SHARED_IP);
				} else {
					// override with env
					CmsRfcCISimple env = null;
					if (wo.getPayLoad().containsKey(
							InductorConstants.ENVIRONMENT)) {
						env = wo.getPayLoad()
								.get(InductorConstants.ENVIRONMENT).get(0);
					}

					String cloudName = wo.getCloud().getCiName();
					CmsCISimple cloudService = wo.getServices().get("dns")
							.get(cloudName);

					host = wo.getBox().getCiName() + "."
							+ getCustomerDomain(cloudService, env);
					logger.info("ManagedVia cluster host:" + host);

					// get the list from route53 / dns service
					List<String> authoritativeDnsServers = getAuthoritativeServers(wo);

					// workaround for osx mDNSResponder cache issue -
					// http://serverfault.com/questions/64837/dns-name-lookup-was-ssh-not-working-after-snow-leopard-upgrade
					int randomInt = randomGenerator
							.nextInt(authoritativeDnsServers.size() - 1);
					String[] digCmd = new String[] { "/usr/bin/dig", "+short",
							host };

					if (randomInt > -1)
						digCmd = new String[] { "/usr/bin/dig", "+short", host,
								"@" + authoritativeDnsServers.get(randomInt) };

					ProcessResult result = procR.executeProcessRetry(digCmd,
							logKey, retryCount);
					if (result.getResultCode() > 0) {
						return null;
					}

					if (!result.getStdOut().equalsIgnoreCase("")) {
						host = result.getStdOut().trim();
					} else {
						wo.setComments("failed : can't get ip for: "
								+ hostCi.getCiName());
						return null;
					}
				}
				logger.info("using cluster host ip:" + host);

			} else {
				host = hostCi.getCiAttributes().get(config.getIpAttribute());
				logger.info("using ManagedVia " + config.getIpAttribute()
						+ ": " + host);
			}

		}

		return host;
	}


	/**
	 * getAuthoritativeServers: gets dns servers
	 *
	 * @param wo CmsWorkOrderSimple
	 */
	private List<String> getAuthoritativeServers(CmsWorkOrderSimple wo) {

		String cloudName = wo.getCloud().getCiName();
		CmsCISimple cloudService = wo.getServices().get("dns").get(cloudName);

		return getAuthoritativeServersByCloudService(cloudService);
	}
	
	
	/**
	 * extra ' - ' for pattern matching - daq InductorLogSink will parse this
	 * and insert into log store see https://github.com/oneops/daq/wiki/schema
	 * for more info
	 */
	private String getLogKey(CmsWorkOrderSimple wo) {
		return wo.getDpmtRecordId() + ":" + wo.getRfcCi().getCiId() + " - ";
	}

	
	/**
	 * cleans up the rfc ci classname
	 */
	private String normalizeClassName(CmsWorkOrderSimple wo) {
		String appName = wo.getRfcCi().getCiClassName();
		List<String> classParts = Arrays.asList(appName.split("\\."));
		return classParts.get(classParts.size() - 1).toLowerCase();
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
		String cloudName = wo.getCloud().getCiName();
		if (wo.getServices().containsKey("compute")
				&& wo.getServices().get("compute").get(cloudName)
						.getCiAttributes().containsKey("env_vars")) {

			updateProxyListBash(proxyList,
					wo.getServices().get("compute").get(cloudName)
							.getCiAttributes().get("env_vars"));
		}

		// get http proxy by managed_via
		CmsRfcCISimple getMgmtCi = wo.getPayLoad()
				.get(InductorConstants.MANAGED_VIA).get(0);

		if (getMgmtCi.getCiAttributes().containsKey("proxy_map")) {

			String jsonProxyHash = getMgmtCi.getCiAttributes().get("proxy_map");
			updateProxyListBash(proxyList, jsonProxyHash);
		}

		for (String proxy : proxyList)
			vars += proxy + " ";

		return vars;

	}

	/**
	 * getProxyBashVars: gets proxy env vars
	 *
	 * @param wo CmsWorkOrderSimple
	 */
	private String getProxyEnvVars(CmsWorkOrderSimple wo) {
		String vars = "";
		ArrayList<String> proxyList = new ArrayList<String>();

		// use proxy_map from compute cloud service
		String cloudName = wo.getCloud().getCiName();
		if (wo.getServices().containsKey("compute")
				&& wo.getServices().get("compute").get(cloudName)
						.getCiAttributes().containsKey("env_vars")) {

			updateProxyList(proxyList,
					wo.getServices().get("compute").get(cloudName)
							.getCiAttributes().get("env_vars"));
		}

		// get http proxy by managed_via
		CmsRfcCISimple getMgmtCi = wo.getPayLoad()
				.get(InductorConstants.MANAGED_VIA).get(0);

		if (getMgmtCi.getCiAttributes().containsKey("proxy_map")) {

			String jsonProxyHash = getMgmtCi.getCiAttributes().get("proxy_map");
			updateProxyList(proxyList, jsonProxyHash);
		}

		for (String proxy : proxyList)
			vars += proxy + " ";

		return vars;
	}

	/**
	 * getUserForOsAndCloud: get username based on os and cloud provider
	 *
	 * @param osType String
	 * @param wo
	 * @returns username
	 */
	private String getUserForOsAndCloud(String osType, CmsWorkOrderSimple wo) {
		
		String cloudName = wo.getCloud().getCiName();
		if (wo.getServices().containsKey("compute")
				&& wo.getServices().get("compute").get(cloudName)
						.getCiAttributes().containsKey("initial_user")) {
			
			String user = wo.getServices().get("compute").get(cloudName)
					.getCiAttributes().get("initial_user");
			logger.info("using initial username from compute service: " + user);
			return user;
			
		}

		// override via config
		if (config.getInitialUser() != null
				&& !config.getInitialUser().equals("unset")) {
			return config.getInitialUser();
		}

		String user = "root";
		// ubuntu supplied image (vagrant/ec2) use ubuntu
		if (osType.indexOf("buntu") > -1 &&
		// rackspace uses root for all images
				wo.getCloud().getCiAttributes().get("location")
						.indexOf("rackspace") == -1) {

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

	private String getRemoteFileName(CmsWorkOrderSimple wo) {
		return "/opt/oneops/workorder/"
				+ wo.rfcCi.getCiClassName().substring(4).toLowerCase() + "."
				+ wo.rfcCi.getCiName() + ".json";
	}


	/**
	 * writeChefConfig: creates a chef config file for unique lockfile by ci.
	 * returns chef config full path
	 *
	 * @param wo                   CmsWorkOrderSimple
	 * @param cookbookRelativePath
	 * @returns chef config full path
	 */
	private String writeChefConfig(CmsWorkOrderSimple wo,
								   String cookbookRelativePath) {
		String logLevel = "info";
		if (isDebugEnabled(wo)) {
			logLevel = "debug";
		}
		return writeChefConfig(Long.toString(wo.getRfcCi().getCiId()),
				cookbookRelativePath, logLevel);
	}


	/**
	 * removeFile - removes a file with uuid checking
	 *
	 * @param filename String
	 */
	private void removeFile(CmsWorkOrderSimple wo, String filename) {
		if (!isDebugEnabled(wo))
			removeFile(filename);
	}
	
	
	/**
	 * Check if the debug mode is enabled for the work order
	 * 
	 * @param wo
	 *            {@code CmsWorkOrderSimple}
	 * @return <code>true</code> if the environment debug is selected, else
	 *         return <code>false</code>
	 */
	private boolean isDebugEnabled(CmsWorkOrderSimple wo) {
		return (config.getDebugMode().equals("on") || wo.getPayLoad()
				.containsKey("Environment")
				&& wo.getPayLoad().get("Environment").get(0).getCiAttributes()
						.containsKey("debug")
				&& wo.getPayLoad().get("Environment").get(0).getCiAttributes()
						.get("debug").equals("true"));
	}


	/**
	 * setResultCi: uses the result map to set attributes of the resultCi
	 *
	 * @param result ProcessResult
	 * @param wo     CmsWorkOrderSimple
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
			if (i > 0)
				resultForLog += ", ";
			// no printing of private key
			if (!key.equalsIgnoreCase("private"))
				resultForLog += key + "=" + attrs.get(key);
			i++;
		}
		logger.debug("resultCi attrs:" + resultForLog);

		// put tags from result / recipe OutputHandler
		wo.getSearchTags().putAll(result.getTagMap());

	}


	/**
	 * writes private key and returns String of the filename
	 *
	 * @param wo
	 *            CmsWorkOrderSimple
	 */
	private String writePrivateKey(CmsWorkOrderSimple wo)
			throws KeyNotFoundException {
		if (wo.getPayLoad().containsKey(InductorConstants.SECURED_BY)) {
			String key = wo.getPayLoad().get(InductorConstants.SECURED_BY)
					.get(0).getCiAttributes().get(InductorConstants.PRIVATE);
			checkIfEmpty(wo, key);
			return writePrivateKey(key);
		} else if (wo.getPayLoad().containsKey(InductorConstants.SERVICED_BY)
				&& wo.getPayLoad().get(InductorConstants.SERVICED_BY).get(0)
				.getCiAttributes()
				.containsKey(InductorConstants.PRIVATE_KEY)) {
			String key = wo.getPayLoad().get(InductorConstants.SERVICED_BY)
					.get(0).getCiAttributes()
					.get(InductorConstants.PRIVATE_KEY);
			checkIfEmpty(wo, key);
			return writePrivateKey(key);
		}
		throw newKeyNotFoundException(wo);
	}

	private KeyNotFoundException newKeyNotFoundException(CmsWorkOrderSimple wo) {
		return new KeyNotFoundException("workorder: "
				+ wo.getRfcCi().getNsPath() + " "
				+ wo.getRfcCi().getRfcAction() + " missing SecuredBy sshkey.");
	}

	private void checkIfEmpty(CmsWorkOrderSimple wo, String key) throws KeyNotFoundException {
		if (StringUtils.isEmpty(key)) {
			throw newKeyNotFoundException(wo);
		}
	}
}
