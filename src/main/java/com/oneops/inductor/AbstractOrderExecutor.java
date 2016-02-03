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

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.route53.AmazonRoute53;
import com.amazonaws.services.route53.AmazonRoute53Client;
import com.amazonaws.services.route53.model.*;
import com.google.common.base.Joiner;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.domain.CmsWorkOrderSimpleBase;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.oneops.inductor.InductorConstants.COMPLETE;
import static com.oneops.inductor.InductorConstants.FAILED;

/**
 *  AbstractOrderExecutor- base class for WorkOrderExecutor and ActionOrderExecutor
 */

public abstract class AbstractOrderExecutor {

	private static Logger logger = Logger.getLogger(AbstractOrderExecutor.class);

	final protected Gson gson = new Gson();
	final protected Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();

	protected int retryCount = 3;

	protected ProcessRunner procR;

	protected String[] sshCmdLine = null;

	protected String[] rsyncCmdLine = null;

	protected String[] sshInteractiveCmdLine = null;

	protected Random randomGenerator = new Random();

	private Config config = null;

	public AbstractOrderExecutor(Config config) {
		this.config = config;
		procR = new ProcessRunner(config);
		

        rsyncCmdLine = new String[]{
                "/usr/bin/rsync",
                "-az",
                "--force",
                "--exclude=*.png",
                "--exclude=*.md",
                "--rsh=ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null ",
                "--timeout=" + config.getRsyncTimeout()
        };

        sshCmdLine = new String[]{
                "ssh",
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                "-qi"
        };

        // interactive needed to get output of execute resource
        sshInteractiveCmdLine = new String[]{
                "ssh",
                "-t", "-t", // 2-t's needed to get output of execute resource - probably anything that uses mixlib shell out
                "-o", "StrictHostKeyChecking=no",
                "-o", "UserKnownHostsFile=/dev/null",
                "-qi"
        };
		
		retryCount = config.getRetryCount();
	}

	/**
	 * Process the workorder or actionorder and return message to be
	 * put in the controller response queue
	 *
	 * @param order wo/ao
	 * @param correlationId correlationId
	 * @return Map<String, String> message
	 *
	 * @throws IOException
     */
	abstract Map<String, String> process(CmsWorkOrderSimpleBase order,
			String correlationId) throws IOException;

	protected void processStubbedCloud(CmsWorkOrderSimpleBase wo) {
		try {
			TimeUnit.SECONDS.sleep(config.getStubResponseTimeInSeconds());
		} catch (InterruptedException e) {
			//Sleep for response.
		}
		if (wo instanceof CmsWorkOrderSimple) {
			((CmsWorkOrderSimple) (wo)).setDpmtRecordState(config.getStubResultCode() == 0 ? COMPLETE : FAILED);
			CmsCISimple resultCi = new CmsCISimple();
			mergeRfcToResult(((CmsWorkOrderSimple) wo).getRfcCi(), resultCi);
			wo.setResultCi(resultCi);
		} else if (wo instanceof CmsActionOrderSimple) {
			((CmsActionOrderSimple) (wo)).setActionState(config.getStubResultCode() == 0 ? OpsActionState.complete : OpsActionState.failed);
		}
	}

	/**
	 * assemble the json request for chef
	 */
	abstract Map<String, Object> assembleRequest(CmsWorkOrderSimpleBase wo);

	/**
	 * Set the queue time in the wo/ao for search/analytics
	 * 
	 * @param wo
	 */
	protected void setQueueTime(CmsWorkOrderSimpleBase wo) {
		String totalTime, requestDequeTs;
		try {
			requestDequeTs = DateUtil.formatDate(new Date(),
					CmsConstants.SEARCH_TS_PATTERN);
			wo.getSearchTags().put(CmsConstants.REQUEST_DEQUE_TS,
					requestDequeTs);

			totalTime = String
					.valueOf((DateUtil
							.parseDate(
									requestDequeTs,
									Arrays.asList(new String[] { CmsConstants.SEARCH_TS_PATTERN }))
							.getTime() - DateUtil
							.parseDate(
									wo.getSearchTags().get(
											CmsConstants.REQUEST_ENQUE_TS),
									Arrays.asList(new String[] { CmsConstants.SEARCH_TS_PATTERN }))
							.getTime()) / 1000.0);
			wo.getSearchTags().put(CmsConstants.QUEUE_TIME, totalTime);
		} catch (Exception e) {
			logger.error("Exception occured while setting queue time " + e);
		}
	}

	/**
	 * Set the local wait time for local work-order/action-order
	 * 
	 * @param wo
	 */
	protected void setLocalWaitTime(CmsWorkOrderSimpleBase wo) {
		String localWaitTime;
		try {
			localWaitTime = String
					.valueOf((System.currentTimeMillis() - DateUtil
							.parseDate(
									wo.getSearchTags().get(
											CmsConstants.REQUEST_DEQUE_TS),
									Arrays.asList(new String[] { CmsConstants.SEARCH_TS_PATTERN }))
							.getTime()) / 1000.0);
			wo.getSearchTags().put(CmsConstants.LOCAL_WAIT_TIME, localWaitTime);
		} catch (Exception e) {
			logger.error("Exception occured while setting local wait time " + e);
		}
	}

	/**
	 * Set the total execution time for work-order/action-order
	 * 
	 * @param wo
	 * @param duration
	 */
	protected void setTotalExecutionTime(CmsWorkOrderSimpleBase wo,
			long duration) {

		try {
			if (wo.getSearchTags().get(CmsConstants.LOCAL_WAIT_TIME) != null) {
				duration -= Double.valueOf(wo.getSearchTags().get(
						CmsConstants.LOCAL_WAIT_TIME));
			}
			wo.getSearchTags().put(CmsConstants.EXECUTION_TIME,
					String.valueOf(duration / 1000.0));
		} catch (Exception e) {
			logger.error("Execption occured while setting execution time", e);
		}

	}


	/**
	 * writes private key
	 * 
	 * @param key
	 *            String
	 */
	protected String writePrivateKey(String key) {
		String uuid = UUID.randomUUID().toString();
		String filename = config.getDataDir() + "/" + uuid;
		FileWriter fstream;
		try {
			fstream = new FileWriter(filename);
			BufferedWriter bw = new BufferedWriter(fstream);
			bw.write(key);
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

	/**
	 * boolean check for uuid
	 * 
	 * @param uuid string
	 */
	public static boolean isUUID(String uuid) {
		if (uuid == null)
			return false;
		try {
			// we have to convert to object and back to string because the built
			// in fromString does not have
			// good validation logic.
			UUID fromStringUUID = UUID.fromString(uuid);
			String toStringUUID = fromStringUUID.toString();
			return toStringUUID.equals(uuid);
		} catch (IllegalArgumentException e) {
			return false;
		}
	}

	/**
	 * removeFile - removes a file with uuid checking
	 * 
	 * @param filename
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
	 *
	 * @param ipPort String
	 */
	protected String generateRsyncErrorMessage(int exitCode, String ipPort) {				
		if (exitCode == 11 || exitCode == 12)
			return String.format("Filesystem full on %s - cleanup and retry", ipPort);
		else
			return String.format("Cannot connect to %s - network or compute service issue", ipPort);					
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
		List<String> classParts = new LinkedList<String>(
				Arrays.asList(className.split("\\.")));

		// remove first bom. and last Component Class
		classParts.remove(0);
		classParts.remove(classParts.size() - 1);
		if (classParts.size() > 0 && !classParts.get(0).equals("service"))
			cookbookPath = "circuit-" + Joiner.on("-").join(classParts);
		else
			cookbookPath = "circuit-main-1";

		// cloud service use the default packer
		return cookbookPath;
	}

	/**
	 * Gets cookbook path by ciClassname
	 *
	 * @param wo
	 * @param fileName
     */
	protected void writeChefRequest(CmsWorkOrderSimpleBase wo, String fileName) {
		try {
			// build a chef request
			Map<String, Object> chefRequest = assembleRequest(wo);

			// write the request to a .json file
			BufferedWriter out = new BufferedWriter(new FileWriter(fileName));
			out.write(gsonPretty.toJson(chefRequest));
			out.close();
		} catch (IOException e) {
			logger.error(e);
			e.printStackTrace();
		}

	}

	/**
	 * Creates a chef config file for unique lockfile by ci.
	 * returns chef config full path.
	 *
	 * @param ci
	 * @param cookbookRelativePath chef config full path
	 * @param logLevel
	 * @return
	 */
	protected String writeChefConfig(String ci, String cookbookRelativePath,
									 String logLevel) {
		String filename = "/tmp/chef-" + ci;

		String cookbookDir = config.getCircuitDir();
		if (!cookbookRelativePath.equals(""))
			cookbookDir = config.getCircuitDir().replace("packer",
					cookbookRelativePath);

		cookbookDir += "/components/cookbooks";
		String sharedDir = config.getCircuitDir().replace("packer",
				"shared/cookbooks");

		String content = "cookbook_path [\"" + cookbookDir + "\",\""
				+ sharedDir + "\"]\n";
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

	/**
	 * Populates list of proxies from a json bash string
	 *
	 * @param proxyList
	 * @param jsonProxyHash
     */
	protected void updateProxyListBash(ArrayList<String> proxyList,
			String jsonProxyHash) {

		JsonReader reader = new JsonReader(new StringReader(jsonProxyHash));
		reader.setLenient(true);
		HashMap<String, String> proxyMap = gson.fromJson(reader,
				Map.class);

		if (proxyMap != null) {
			for (String key : proxyMap.keySet()) {
				proxyList.add(key + ":" + proxyMap.get(key));
			}
		}

	}

	/**
	 * Populates list of proxies from a json string
	 *
	 * @param proxyList
	 * @param jsonProxyHash
     */
	protected void updateProxyList(ArrayList<String> proxyList,
			String jsonProxyHash) {

		JsonReader reader = new JsonReader(new StringReader(jsonProxyHash));
		reader.setLenient(true);
		HashMap<String, String> proxyMap = gson.fromJson(reader,
				Map.class);

		if (proxyMap != null) {
			for (String key : proxyMap.keySet()) {
				proxyList.add(key + "_proxy=" + proxyMap.get(key));
			}
		}

	}

	/**
	 * Chef-solo command builder
	 *
	 * @param fileName   payload file name
	 * @param chefConfig chef config file
	 * @param debug      <code>true</code> if the chef debug (-l) is enabled.
	 * @return command string array.
	 */
	protected String[] buildChefSoloCmd(String fileName, String chefConfig, boolean debug) {
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
		return cmd.toArray(new String[cmd.size()]);
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
		if (comments.isEmpty())
			comments = "failed without any specified faults.";

		return comments;
	}

	/**
	 * mergeRfcToResult: copies rfc attrs to resultCi
	 *
	 * @param rfc    CmsRfcCISimple
	 * @param result CmsCISimple
	 */
	protected void mergeRfcToResult(CmsRfcCISimple rfc, CmsCISimple result) {

		result.setCiId(rfc.getCiId());
		result.setLastAppliedRfcId(rfc.getRfcId());
		result.setCiClassName(rfc.getCiClassName());
		result.getAttrProps().putAll(rfc.getCiAttrProps());

		Map<String, String> rfcAttrs = rfc.getCiAttributes();

		if (result.getCiAttributes() == null) {
			result.setCiAttributes(new HashMap<String, String>());
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
	 *
	 * @param result CmsCISimple
	 *
	 */
	protected void mergeCiToResult(CmsCISimple ci, CmsCISimple result) {

		result.setCiId(ci.getCiId());
		result.setCiClassName(ci.getCiClassName());
		result.getAttrProps().putAll(ci.getAttrProps());

		Map<String, String> rfcAttrs = ci.getCiAttributes();

		if (result.getCiAttributes() == null) {
			result.setCiAttributes(new HashMap<String, String>());
		}
		Map<String, String> resultAttrs = result.getCiAttributes();
		for (String key : rfcAttrs.keySet()) {
			if (!resultAttrs.containsKey(key)) {
				resultAttrs.put(key, rfcAttrs.get(key));
			}
		}
	}


	/**
	 * getAuthoritativeServersByCloudService: gets dns servers
	 *
	 * @param cloudService CmsCISimple
	 */
	protected List<String> getAuthoritativeServersByCloudService(
			CmsCISimple cloudService) {

		String dnsZoneName = cloudService.getCiAttributes().get("zone");
		List<String> nameservers = new ArrayList<>();

		// aws use the api
		if (cloudService.getCiClassName().endsWith("AWS")) {
			AWSCredentials awsCredentials;
			if (cloudService.getCiAttributes().containsKey("dns_key")) {
				awsCredentials = new BasicAWSCredentials(cloudService
						.getCiAttributes().get("dns_key"), cloudService
						.getCiAttributes().get("dns_secret"));
			} else {
				awsCredentials = new BasicAWSCredentials(config.getDnsKey(),
						config.getDnsSecret());
			}
			return getAuthoritativeServersWithAwsCreds(awsCredentials,
					dnsZoneName);

			// else use ns record
		} else {

			String dnsZone = cloudService.getCiAttributes().get("zone");
			String[] digCmd = new String[] { "/usr/bin/dig", "+short", "NS",
					dnsZone };
			ProcessResult result = procR.executeProcessRetry(digCmd, "",
					retryCount);
			if (result.getResultCode() > 0) {
				logger.error("dig +short NS " + dnsZone + " returned: "
						+ result.getStdErr());
			}

			if (!result.getStdOut().equalsIgnoreCase("")) {
				nameservers = Arrays.asList(result.getStdOut().split("\n"));
			}

		}
		return nameservers;
	}

	/**
	 * Gets dns servers
	 *
	 * @param awsCredentials AWSCredentials
	 * @param zoneDomainName zoneDomainName
	 * @return dns servers
	 */
	private List<String> getAuthoritativeServersWithAwsCreds(
			AWSCredentials awsCredentials, String zoneDomainName) {

		if (!zoneDomainName.endsWith(".")) {
			zoneDomainName += ".";
		}

		AmazonRoute53 route53 = new AmazonRoute53Client(awsCredentials);
		ListHostedZonesResult result = route53.listHostedZones();
		List<HostedZone> zones = result.getHostedZones();
		List<String> dnsServers = new ArrayList<String>();
		for (int i = 0; i < zones.size(); i++) {
			HostedZone hostedZone = zones.get(i);
			logger.info("zone: " + hostedZone.getName());
			if (hostedZone.getName().equalsIgnoreCase(zoneDomainName)) {
				logger.info("matched zone");
				GetHostedZoneResult zone = route53
						.getHostedZone(new GetHostedZoneRequest()
								.withId(hostedZone.getId().replace(
										"/hostedzone/", "")));
				DelegationSet delegationSet = zone.getDelegationSet();
				dnsServers = delegationSet.getNameServers();
				break;
			}
		}
		logger.info("dnsServer: " + dnsServers.toString());
		return dnsServers;
	}

	/**
	 * Determine if workorder should run remotely
	 *
	 * @param wo work order
	 */
	protected Boolean isRemoteChefCall(CmsWorkOrderSimple wo) {
		if (wo.getPayLoad() == null)
			return false;

		List<CmsRfcCISimple> managedVia = wo.getPayLoad().get(
				InductorConstants.MANAGED_VIA);
		List<CmsRfcCISimple> servicedBy = wo.getPayLoad().get(
				InductorConstants.SERVICED_BY);
		String className = wo.getRfcCi().getCiClassName().toLowerCase();

		// if has a servicedBy (cluster) or ManagedVia (compute) and not a
		// compute::add
		if (((managedVia != null && managedVia.size() > 0) || (servicedBy != null
				&& servicedBy.size() > 0
				&& !wo.getRfcCi().getCiClassName()
						.endsWith(InductorConstants.KEYPAIR) && !servicedBy
				.get(0).getCiName().startsWith("netscaler")))
				&& !(className.indexOf(InductorConstants.COMPUTE) > -1 && (wo
						.getRfcCi().getRfcAction()
						.equalsIgnoreCase(InductorConstants.ADD) || wo
						.getRfcCi().getRfcAction()
						.equalsIgnoreCase(InductorConstants.DELETE)))) {
			return true;
		}
		return false;
	}

	/**
	 * cleans up the rfc ci classname
	 */
	protected String normalizeClassName(CmsActionOrderSimple ao) {
		String appName = ao.getCi().getCiClassName();
		List<String> classParts = Arrays.asList(appName.split("\\."));
		return classParts.get(classParts.size() - 1).toLowerCase();
	}

	/**
	 * getCustomerDomain: gets proxy bash vars
	 *
	 * @param cloudService CmsCISimple
	 * @param env          CmsRfcCISimple
	 */
	public String getCustomerDomain(CmsCISimple cloudService, CmsRfcCISimple env) {
		CmsCISimple envSimple = new CmsCISimple();
		mergeRfcToResult(env, envSimple);

		return getCustomerDomain(cloudService, envSimple);
	}

	/**
	 * generates the dns domain name from cloud service zone and env subdomain
	 * <subdomain from env>.<cloud dns id>.<cloud service zone>
	 * <env.assembly.org>.<cloud>.<zone.com>
	 */
	public String getCustomerDomain(CmsCISimple cloudService, CmsCISimple env) {

		String domain = "";

		if (env != null && env.getCiAttributes().containsKey("subdomain")
				&& env.getCiAttributes().get("subdomain") != null)

			domain = env.getCiAttributes().get("subdomain");

		if (cloudService.getCiAttributes().containsKey("cloud_dns_id")
				&& cloudService.getCiAttributes().get("cloud_dns_id").length() > 0)
			domain += '.' + cloudService.getCiAttributes().get("cloud_dns_id");

		if (cloudService.getCiAttributes().containsKey("zone"))
			domain += '.' + cloudService.getCiAttributes().get("zone");

		return domain;
	}

}
