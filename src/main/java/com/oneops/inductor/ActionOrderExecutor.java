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

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.google.gson.JsonSyntaxException;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsUtil;

import static com.oneops.inductor.InductorConstants.*;

/**
 * 
 * ActionOrder specific processing
 *
 */
public class ActionOrderExecutor extends AbstractOrderExecutor {

	private Semaphore semaphore = null;

	public ActionOrderExecutor(Config config, Semaphore semaphore) {
		super(config);
		this.config = config;
		this.semaphore = semaphore;
	}

	private static Logger logger = Logger.getLogger(WorkOrderExecutor.class);
	private Config config = null;

	/**
	 * processActionOrder - process the actionorder and return message to be put
	 * in the controller response queue
	 *
	 * @param o             workorder
	 * @param correlationId jms correlation Id
	 * @return
	 * @throws IOException
	 */
	@Override
	public Map<String, String> process(CmsWorkOrderSimpleBase o, String correlationId) throws IOException {

		CmsActionOrderSimple ao = (CmsActionOrderSimple) o;
		long startTime = System.currentTimeMillis();

		CmsCISimple resultCi = new CmsCISimple();
		mergeCiToResult(ao.getCi(), resultCi);
		ao.setResultCi(resultCi);
		if (config.isCloudStubbed(ao)) {
			//delay the processing as configured and return response.
			processStubbedCloud(ao);
            logger.info("completing ao without doing anything because cloud is stubbed");
		} else {
			// creates the json chefRequest and exec's chef to run chef
			// local or remote via ssh/mc
			runActionOrder(ao);
		}

		long endTime = System.currentTimeMillis();

		int duration = Math.round((endTime - startTime) / 1000);
		logger.info(ao.getActionName() + " " + ao.getCi().getCiClassName()
				+ " " + ao.getCi().getCiName() + " took: " + duration + "sec");

		// state and resultCI gets set via chef response
		// serialize and send to controller
		String responseCode = "200";
		if (!ao.getActionState().equals(OpsActionState.complete)) {
			logger.warn("FAIL: " + ao.getActionId() + " state:"
					+ ao.getActionState());
			responseCode = "500";
			ao.setActionState(OpsActionState.failed);
			ao.getActionState().toString().equals("active");
		}

		setTotalExecutionTime(ao, endTime - startTime);
		ao.getSearchTags()
				.put(CmsConstants.RESPONSE_ENQUE_TS,
						DateUtil.formatDate(new Date(),
								CmsConstants.SEARCH_TS_PATTERN));

		String responseText = gson.toJson(ao);

		// Mask secured fields before logging
		CmsUtil.maskSecuredFields(ao, CmsUtil.ACTION_ORDER_TYPE);
		String logResponseText = gson.toJson(ao);

		// InductorLogSink will process this message
		logger.info("{ \"resultCode\": " + responseCode + ", "
				+ " \"JMSCorrelationID:\": \"" + correlationId + "\", "
				+ "\"responseActionorder\": " + logResponseText + " }");

		// Controller will process this message
		Map<String, String> message = new HashMap<String, String>();
		message.put("body", responseText);
		message.put("correlationID", correlationId);
		message.put("task_result_code", responseCode);
		message.put("priority", "oneops-autorepair".equals(ao.getCreatedBy()) ? "regular" : "high");

		startTime = System.currentTimeMillis();

		return message;
	}


	/**
	 * Assemble the json request for chef
	 *
	 * @param o workorder
	 * @return chef reuqest map.
	 */
	@Override
	public Map<String, Object> assembleRequest(CmsWorkOrderSimpleBase o) {

		CmsActionOrderSimple ao = (CmsActionOrderSimple) o;

		// component recipe
		String appName = normalizeClassName(ao);
		String action = ao.getActionName();

		if (action.equalsIgnoreCase("user-custom-attachment")) {
			appName = "attachment";
			action = "ondemand";
		}

		Map<String, Object> chefRequest = new HashMap<String, Object>();

		String runList[] = new String[1];
		runList[0] = "recipe[" + appName + "::" + action + "]";

		chefRequest.put(appName, ao.getCi().getCiAttributes());
		// override with env
		CmsCISimple env = null;
		if (ao.getPayLoad() != null
				&& ao.getPayLoad().containsKey(InductorConstants.ENVIRONMENT)
				&& ao.getPayLoad().get(InductorConstants.ENVIRONMENT).size() > 0) {

			env = ao.getPayLoad().get(InductorConstants.ENVIRONMENT).get(0);
		}

		String cloudName = ao.getCloud().getCiName();
		CmsCISimple cloudService = null;
		if (ao.getServices() != null && ao.getServices().containsKey("dns"))
			cloudService = ao.getServices().get("dns").get(cloudName);

		if (cloudService != null && env != null)
			chefRequest.put("customer_domain",
					getCustomerDomain(cloudService, env));
		chefRequest.put("mgmt_domain", config.getMgmtDomain());
		chefRequest.put("workorder", ao);
		chefRequest.put("run_list", runList);
		chefRequest.put("app_name", appName);
		chefRequest.put("ip_attribute", config.getIpAttribute());		
		chefRequest.put("name", ao.getCi().getCiName());

		// set mgmt cert
		if (config.getMgmtCertContent() != null)
			chefRequest.put("mgmt_cert", config.getMgmtCertContent());

		return chefRequest;
	}


	/**
	 * Gets host from action order
	 *
	 * @param ao action order
	 * @param logKey log key
     * @return hostname
     */
	private String getActionOrderHost(CmsActionOrderSimple ao, String logKey) {

		String host = null;

		// Databases are ManagedVia Cluster - use fqdn of the Cluster for the
		// host
		if (ao.getActionName().equalsIgnoreCase("user-custom-attachment")) {
			CmsCISimple hostCi = null;
			if (ao.getPayLoad().containsKey(InductorConstants.MANAGED_VIA)) {
				hostCi = ao.getPayLoad().get(InductorConstants.MANAGED_VIA)
						.get(0);
			} else if (ao.getCi().getCiClassName()
					.equalsIgnoreCase("bom.Compute")) {
				hostCi = ao.getCi();
			}
			host = hostCi.getCiAttributes().get(config.getIpAttribute());

		} else if (ao.getPayLoad() != null
				&& ao.getPayLoad().containsKey(InductorConstants.MANAGED_VIA)) {
			CmsCISimple hostCi = ao.getPayLoad()
					.get(InductorConstants.MANAGED_VIA).get(0);

			if (hostCi.getCiClassName() != null
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
					CmsCISimple env = null;
					if (ao.getPayLoad().containsKey(
							InductorConstants.ENVIRONMENT)) {
						env = ao.getPayLoad()
								.get(InductorConstants.ENVIRONMENT).get(0);
					}

					String cloudName = ao.getCloud().getCiName();
					CmsCISimple cloudService = ao.getServices().get("dns")
							.get(cloudName);

					host = ao.getBox().getCiName() + "."
							+ getCustomerDomain(cloudService, env);
					logger.info("ManagedVia cluster host:" + host);

					// get the list from route53 / dns service
					List<String> authoritativeDnsServers = getAuthoritativeServers(ao);

					// workaround for osx mDNSResponder cache issue:
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
						logger.info("using cluster host ip:" + host);
					}
				}

			} else {
				host = hostCi.getCiAttributes().get(config.getIpAttribute());
			}

		}
		return host;
	}

	/**
	 * Runs action orders on the inductor box, ex compute::reboot
	 *
	 * @param pr {@code ProcessRunner}
	 * @param ao action order
	 * @param appName
	 * @param logKey
	 * @param fileName
	 * @param cookbookPath
	 * @param attempt
	 *
	 * @throws JsonSyntaxException
     * @throws IOException
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

			if (hasQueueOnLock)
				logger.info(logKey + " waiting for semaphore...");

			semaphore.acquire();

			if (hasQueueOnLock)
				logger.info(logKey + " got semaphore");

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
	 * @param ao action order
     */
	private void runActionOrder(CmsActionOrderSimple ao) {

		// file-based request keyed by deployment record id - remotely by
		// class.ciName for ease of debug
		String remoteFileName = "/opt/oneops/workorder/"
				+ ao.getCi().getCiClassName().substring(4).toLowerCase() + "."
				+ ao.getActionName() + "-" + ao.getCiId() + ".json";
		String fileName = config.getDataDir() + "/" + ao.getActionId()
				+ ".json";
		logger.info("writing config to:" + fileName + " remote: "
				+ remoteFileName);

		// build a chef request
		Map<String, Object> chefRequest = assembleRequest(ao);
		String appName = (String) chefRequest.get("app_name");

		// extra ' - ' for pattern matching - daq InductorLogSink will parse
		// this and insert into log store
		// see https://github.com/oneops/daq/wiki/schema for more info
		String logKey = ao.getActionId() + ":" + ao.getCi().getCiId() + " - ";

		logger.info(logKey + " Inductor: " + config.getIpAddr());
		ao.getSearchTags().put("inductor", config.getIpAddr());

		// assume failed; gets set to COMPLETE at the end
		ao.setActionState(OpsActionState.failed);

		String cookbookPath = getCookbookPath(ao.getCi().getCiClassName());
		logger.info("cookbookPath: " + cookbookPath);

		try {
			// write the request to a .json file
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(gsonPretty.toJson(chefRequest));
			out.close();

			// sync cookbook and chef json request to remote site
			String host = getActionOrderHost(ao, logKey);
			String user = ONEOPS_USER;

			// run local when no managed via
			if (host == null || host.isEmpty()) {
				runLocalActionOrder(procR, ao, appName, logKey, fileName,
						cookbookPath);
				removeFile(fileName);
				return;
			}

			String keyFile = "";
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

			String baseDir = config.getCircuitDir().replace("packer",
					cookbookPath);
			String components = baseDir + "/components";
			String destination = "/home/" + user + "/" + cookbookPath;

			String[] rsyncCmdLineWithKey = rsyncCmdLine.clone();
			rsyncCmdLineWithKey[4] += "-p " + port + " -qi " + keyFile;

			// always sync base cookbooks/modules
			String[] deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
					new String[] { components,
							user + "@" + host + ":" + destination });
			logger.info(logKey + " ### SYNC BASE: " + components);

			if (!host.equals(InductorConstants.TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(ao, deploy,
						logKey, retryCount, config);
				if (result.getResultCode() > 0) {
					logger.error(logKey + " FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host));
					removeFile(ao, keyFile);
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

			if (!host.equals(InductorConstants.TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(ao, deploy,
						logKey, retryCount, config);
				if (result.getResultCode() > 0) {
					logger.error(logKey + " FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host));					
					removeFile(ao, keyFile);
					return;
				}
			}

			// put actionorder
			deploy = (String[]) ArrayUtils.addAll(rsyncCmdLineWithKey,
					new String[] { fileName,
							user + "@" + host + ":" + remoteFileName });
			logger.info(logKey + " ### SYNC: " + remoteFileName);
			if (!host.equals(InductorConstants.TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(deploy,
						logKey, retryCount);
				if (result.getResultCode() > 0) {
					logger.error(logKey + " FATAL: " + generateRsyncErrorMessage(result.getResultCode(),host));
					removeFile(ao, keyFile);
					return;
				}
			}

			String debugFlag = "";
			if (isDebugEnabled(ao)) {
				debugFlag = "-d";
			}

			// run the chef command
			String remoteCmd = "sudo shared/exec-order.rb "
					+ ao.getCi().getImpl() + " " + remoteFileName + " "
					+ cookbookPath + " " + debugFlag;
			String[] cmd = null;
			cmd = (String[]) ArrayUtils.addAll(sshCmdLine, new String[] {
					keyFile, "-p " + port, user + "@" + host, remoteCmd });
			logger.info(logKey + " ### EXEC: " + user + "@" + host + " "
					+ remoteCmd);
			if (!host.equals(InductorConstants.TEST_HOST)) {
				ProcessResult result = procR.executeProcessRetry(cmd, logKey,
						retryCount, config.getIpAttribute());
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

			}
			ao.setActionState(OpsActionState.complete);
			removeFile(ao, keyFile);

		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}
		if (!isDebugEnabled(ao))
			removeFile(fileName);
	}


	/**
	 * WriteChefConfig: creates a chef config file for unique lockfile by ci.
	 * returns chef config full path
	 *
	 * @param ao
	 * @param cookbookRelativePath
	 * @return chef config file path
	 */
	private String writeChefConfig(CmsActionOrderSimple ao,
								   String cookbookRelativePath) {
		String logLevel = "info";
		if (isDebugEnabled(ao)) {
			logLevel = "debug";
		}
		return writeChefConfig(Long.toString(ao.getCi().getCiId()),
				cookbookRelativePath, logLevel);
	}


	/**
	 * Removes a file with uuid checking
	 *
	 * @param ao
	 * @param filename
	 */
	private void removeFile(CmsActionOrderSimple ao, String filename) {
		if (!isDebugEnabled(ao))
			removeFile(filename);
	}
	

	/**
	 * Check if the debug mode is enabled for the action order.
	 * 
	 * @param ao
	 *            {@code CmsActionOrderSimple}
	 * @return <code>true</code> if the environment debug is selected, else
	 *         return <code>false</code>
	 */
	private boolean isDebugEnabled(CmsActionOrderSimple ao) {
		return (config.getDebugMode().equals("on") || ao.getPayLoad() != null
				&& ao.getPayLoad().containsKey("Environment")
				&& ao.getPayLoad().get("Environment").get(0).getCiAttributes()
						.containsKey("debug")
				&& ao.getPayLoad().get("Environment").get(0).getCiAttributes()
						.get("debug").equals("true"));
	}	

	/**
	 * Gets dns servers
	 *
	 * @param ao action order
	 * @return
     */
	private List<String> getAuthoritativeServers(CmsActionOrderSimple ao) {

		String cloudName = ao.getCloud().getCiName();
		CmsCISimple cloudService = ao.getServices().get("dns").get(cloudName);

		return getAuthoritativeServersByCloudService(cloudService);
	}	

	
	/**
	 * writes private key and returns String of the filename
	 * 
	 * @param ao
	 *            CmsActionOrderSimple
	 */
	protected String writePrivateKey(CmsActionOrderSimple ao)
			throws KeyNotFoundException {
		if (ao.getPayLoad().containsKey(InductorConstants.SECURED_BY)) {
			String key = ao.getPayLoad().get(InductorConstants.SECURED_BY)
					.get(0).getCiAttributes().get(InductorConstants.PRIVATE);
			return writePrivateKey(key);
		} else if (ao.getPayLoad().containsKey(InductorConstants.SERVICED_BY)
				&& ao.getPayLoad().get(InductorConstants.SERVICED_BY).get(0)
						.getCiAttributes()
						.containsKey(InductorConstants.PRIVATE_KEY)) {
			String key = ao.getPayLoad().get(InductorConstants.SERVICED_BY)
					.get(0).getCiAttributes()
					.get(InductorConstants.PRIVATE_KEY);
			return writePrivateKey(key);
		}
		throw new KeyNotFoundException("actionorder: " + ao.getCi().getNsPath()
				+ " " + ao.getActionName() + " missing SecuredBy sshkey.");
	}	
	

	/**
	 * Uses the result map to set attributes of the resultCi
	 *
	 * @param result
	 * @param ao
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
			if (i > 0)
				resultForLog += ", ";
			// no printing of private key
			if (!key.equalsIgnoreCase("private"))
				resultForLog += key + "=" + attrs.get(key);
			i++;
		}
		logger.debug("resultCi attrs:" + resultForLog);

		// put tags from result / recipe OutputHandler
		ao.getSearchTags().putAll(result.getTagMap());
	}
	
}
