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
import org.apache.commons.httpclient.util.DateParseException;
import org.apache.commons.httpclient.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.oneops.cms.util.CmsConstants.*;
import static com.oneops.inductor.InductorConstants.*;

/**
 * AbstractOrderExecutor- base class for WorkOrderExecutor and ActionOrderExecutor
 */

public abstract class AbstractOrderExecutor {

    public static final String ONDEMAND = "ondemand";
    private static final Logger logger = Logger.getLogger(AbstractOrderExecutor.class);

    protected static final String RUN_LIST_SEPARATOR = "::";
    protected static final String RUN_LIST_PREFIX = "recipe[";
    protected static final String RUN_LIST_SUFFIX = "]";
    public static final String USER_CUSTOM_ATTACHMENT = "user-custom-attachment";
    final protected Gson gson = new Gson();
    final protected Gson gsonPretty = new GsonBuilder().setPrettyPrinting().create();
    protected int retryCount = 3;
    protected ProcessRunner processRunner;
    protected String[] sshCmdLine = null;
    protected String[] rsyncCmdLine = null;
    protected String[] sshInteractiveCmdLine = null;
    protected Random randomGenerator = new Random();
    private Config config = null;
    protected StatCollector inductorStat;

    public AbstractOrderExecutor(Config config) {
        this.config = config;
        processRunner = new ProcessRunner(config);


        rsyncCmdLine = new String[]{
                "/usr/bin/rsync",
                "-az",
                "--force",
                "--exclude=*.png",
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
            return toStringUUID.equalsIgnoreCase(uuid);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    public void setProcessRunner(ProcessRunner processRunner) {
        this.processRunner = processRunner;
    }

    /**
     * Process the workorder or actionorder and return message to be
     * put in the controller response queue
     *
     * @param order         wo/ao
     * @param correlationId correlationId
     * @return Map<String, String> message
     * @throws IOException
     */
    abstract Map<String, String> process(CmsWorkOrderSimpleBase order,
                                         String correlationId) throws IOException;

    protected void processStubbedCloud(CmsWorkOrderSimpleBase wo) {
        try {
            TimeUnit.SECONDS.sleep(getStubSleepTime(wo));
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

    protected long getStubSleepTime(CmsWorkOrderSimpleBase wo) {
        return config.getStubResponseTimeInSeconds();
    }

    /**
     * Set the local wait time for local work-order/action-order
     *
     * @param wo
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
        return System.currentTimeMillis() - DateUtil.parseDate(getSearchTag(wo, REQUEST_DEQUE_TS),
                SEARCH_TS_FORMATS).getTime();
    }

    protected <T> String getSearchTag(CmsWorkOrderSimpleBase<T> wo, String searchTag) {
        return wo.getSearchTags().get(searchTag);
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
            if (getSearchTag(wo, LOCAL_WAIT_TIME) != null) {
                duration -= Double.valueOf(getSearchTag(wo, LOCAL_WAIT_TIME));
            }
            wo.getSearchTags().put(EXECUTION_TIME,String.valueOf(duration / 1000.0));
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
        try (BufferedWriter bw = Files.newBufferedWriter(Paths.get(fileName))){
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
     * @param ipPort   String
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
        List<String> classParts = new LinkedList<>(
                Arrays.asList(className.split("\\.")));

        // remove first bom. or cloud. and last Component Class
        classParts.remove(0);
        classParts.remove(classParts.size() - 1);

        // remove service
        if (classParts.size() > 0 && classParts.get(0).equals("service"))
            classParts.remove(0);

        if (classParts.size() > 0)
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
     * Creates a chef config file for unique lockfile by ci.
     * returns chef config full path.
     *
     * @param ci
     * @param cookbookRelativePath chef config full path
     * @param logLevel
     * @return
     */
    protected String writeChefConfig(String ci, String cookbookRelativePath, String cloudName, 
    		Map<String, Map<String, CmsCISimple>> cloudServices, String logLevel) {
        String filename = "/tmp/chef-" + ci;

        String cookbookDir = config.getCircuitDir();
        if (!cookbookRelativePath.equals(""))
            cookbookDir = config.getCircuitDir().replace("packer",
                    cookbookRelativePath);

        cookbookDir += "/components/cookbooks";
        String sharedDir = config.getCircuitDir().replace("packer",
                "shared/cookbooks");

        Set<String> cookbookPaths = new HashSet<>();
        cookbookPaths.add(cookbookDir);
        cookbookPaths.add(sharedDir);
        
        if (cloudServices != null) {
        	for (String serviceName : cloudServices.keySet()) { // for each service
        		CmsCISimple serviceCi = cloudServices.get(serviceName).get(cloudName);
        		if (serviceCi != null) {
        			String serviceClassName = serviceCi.getCiClassName();
        	        String serviceCookbookCircuit = getCookbookPath(serviceClassName);
        	        if (! serviceCookbookCircuit.equals(cookbookRelativePath)) { 
        	        	cookbookPaths.add(config.getCircuitDir().replace("packer", serviceCookbookCircuit) + "/components/cookbooks");
        	        }
        		}
        	}
        }
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

    /**
     * Populates list of proxies from a json bash string
     *
     * @param proxyList
     * @param jsonProxyHash
     */
    protected void updateProxyListBash(ArrayList<String> proxyList,
                                       String jsonProxyHash) {

        Map<String, String> proxyMap = gson.fromJson(jsonProxyHash,
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


        Map<String, String> proxyMap = gson.fromJson(jsonProxyHash,
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
     * @param ci     CmsCISimple
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
            String[] digCmd = new String[]{"/usr/bin/dig", "+short", "NS",
                    dnsZone};
            ProcessResult result = processRunner.executeProcessRetry(digCmd, "",
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
        List<String> dnsServers = new ArrayList<>();
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
    protected Boolean isRemoteChefCall(CmsWorkOrderSimpleBase wo) {
        if (wo.getPayLoad() == null)
            return false;
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
        return ADD.equalsIgnoreCase(wo.getAction() )|| DELETE.equalsIgnoreCase(wo.getAction());
    }



    public <T> String  getCustomerDomain(CmsWorkOrderSimpleBase<T> wo) {
        final T env = wo.getPayLoadEntryAt(ENVIRONMENT, 0);
        String cloudName = wo.getCloud().getCiName();
        CmsCISimple cloudService = new CmsCISimple();
        if (wo.getServices() != null && wo.getServices().containsKey("dns"))
            cloudService = wo.getServices().get("dns").get(cloudName);
        CmsCISimple envSimple;
        //cloud actions
        if(env ==null)  return  getCustomerDomain(cloudService, null);
        if (env instanceof CmsCISimple) {
            envSimple = CmsCISimple.class.cast(env);
        } else {
            envSimple = new CmsCISimple();
            mergeRfcToResult(CmsRfcCISimple.class.cast(env), envSimple);
        }
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
     * @param ao action order
     * @return
     */
    protected <T> List<String> getAuthoritativeServers(CmsWorkOrderSimpleBase<T> wo) {
        String cloudName = wo.getCloud().getCiName();
        CmsCISimple cloudService = wo.getServices().get("dns").get(cloudName);
        return getAuthoritativeServersByCloudService(cloudService);
    }

    /**
     * Check if the debug mode is enabled for the work order
     *
     * @param wo {@code CmsWorkOrderSimple}
     * @return <code>true</code> if the environment debug is selected, else
     * return <code>false</code>
     */
    protected <T> boolean isDebugEnabled(CmsWorkOrderSimpleBase<T> wo) {
        return (config.getDebugMode().equals("on") || wo.isPayloadEntryEqual(ENVIRONMENT, "debug", "true"));
    }

    protected <T> boolean equals(CmsWorkOrderSimpleBase<T> wo, String payloadEntry, String attributeName, String valueToBeCompared) {
        return wo.getPayLoadAttribute(payloadEntry, attributeName).equals(valueToBeCompared);
    }

    /**
     * writes private key and returns String of the filename
     *
     * @param wo CmsWorkOrderSimple
     */


    private <T> KeyNotFoundException newKeyNotFoundException(CmsWorkOrderSimpleBase<T> wo) {

        String errorMessage = "workorder: "
                + wo.getNsPath() + " "
                + wo.getAction() + " missing SecuredBy sshkey.";
        logger.error(errorMessage);
        return new KeyNotFoundException(errorMessage);
    }
    /**
     * Removes a file with uuid checking
     *
     * @param wo
     * @param filename
     */
    protected void removeFile(CmsWorkOrderSimpleBase wo, String filename) {
        if (!isDebugEnabled(wo))
            removeFile(filename);
    }


    protected String getDebugFlag(CmsWorkOrderSimpleBase ao) {
        String debugFlag =StringUtils.EMPTY;
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
     *
      * @param o
     *  @return
     */
    public Map<String, Object> assembleRequest(CmsWorkOrderSimpleBase wo) {
        //CmsWorkOrderSimple wo = (CmsWorkOrderSimple) o;
        String appName = getAppName(wo);
        Map<String, Object> chefRequest = new HashMap<>();
        Map<String, String> global = new HashMap<>();
        List<String> runList = getRunList(wo);
        chefRequest.put(appName, wo.getCiAttributes());
        chefRequest.put("customer_domain",getCustomerDomain(wo));
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
        if (config.getMgmtCertContent() != null)
            chefRequest.put("mgmt_cert", config.getMgmtCertContent());

        //set perf-collector cert
        if (config.getPerfCollectorCertContent() != null)
            chefRequest.put("perf_collector_cert", config.getPerfCollectorCertContent());

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
        if(action==null) return false;
        return action.equals(wo.getAction());
    }

    protected abstract List<String> getRunList(CmsWorkOrderSimpleBase wo);

    protected boolean isRemoteAction(String action) {
        return REMOTE.equals(action);
    }

    protected boolean isNotaTestHost(String host) {
        return !host.equals(InductorConstants.TEST_HOST);
    }

    protected boolean rsynch(ExecutionContext ctx) {
        boolean rsynchFailed = false;
        if (isNotaTestHost(ctx.getHost())) {
            ProcessResult result = processRunner.executeProcessRetry(ctx);
            if (result.getResultCode() > 0) {
                logger.error(ctx.getLogKey() + " FATAL: " + generateRsyncErrorMessage(result.getResultCode(), ctx.getHost()));
                handleRsyncFailure(ctx.getWo(), ctx.getKeyFile());;
                rsynchFailed = true;
            }
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
}
