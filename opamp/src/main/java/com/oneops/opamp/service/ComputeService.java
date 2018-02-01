package com.oneops.opamp.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmManager;

public class ComputeService {

	private static Logger logger = Logger.getLogger(ComputeService.class);

	protected static final String X_CMS_USER = "X-Cms-User";
	protected static final String ONEOPS_AUTO_REPLACE_USER_PROP_NAME = "oneops-auto-replace-user";
	protected static final String ONEOPS_AUTOREPLACE_USER = System.getProperty(ONEOPS_AUTO_REPLACE_USER_PROP_NAME,
			"oneops-autoreplace");

	private EnvPropsProcessor envProcessor;
	private CmsCmManager cmManager;
	private String transistorUrl;
	private RestTemplate restTemplate;

	public Map<String, Integer> replaceComputeByCid(long ciId) {
		Map<String, Integer> result = new HashMap<>(1);
		
		CmsCI platform = envProcessor.getPlatform4Bom(ciId);
		logger.info("Platform name for compute : " + platform.getCiName());

		CmsCI env = envProcessor.getEnv4Platform(platform);
		logger.info("Oneops environment for ciId : " + env.getCiId() + ": " + env.getCiName());

		boolean isAutoReplaceEnabledForPlatform=envProcessor.isAutoReplaceEnabled(platform);
		logger.info("isAutoReplaceEnabledForPlatform: "+isAutoReplaceEnabledForPlatform);
		
		
		
		if (!isAutoReplaceEnabledForPlatform) {
			result.put("deploymentId", 1);
			return result;
		}
		boolean releaseStatus = envProcessor.isOpenRelease4Env(env);
		logger.info("releaseStatus: " + releaseStatus);

		

		if (!releaseStatus) {
			result = replace(ciId, env);
			return result;

		}
		result.put("deploymentId", 1);
		return result;

	}

	private Map<String, Integer> replace(long ciId, CmsCI env) {

		cmManager.updateCiState(ciId, "replace", "bom.ManagedVia", "to", false, ONEOPS_AUTOREPLACE_USER);
		logger.info("marked the ciId [" + ciId + "] for replace, user" + ONEOPS_AUTOREPLACE_USER);

		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("description", "Auto-Replace by OneOps [" + env.getNsPath() + "]");

		Map<String, String> params = new HashMap<>();
		params.put("envId", String.valueOf(env.getCiId()));

		List<CmsCI> platformsOfEnv = envProcessor.getPlatformsForEnv(env.getCiId());
		CmsCI platformOfBomCi = envProcessor.getPlatform4Bom(ciId);

		if (platformsOfEnv.size() > 1) {
			StringBuilder excludePlatforms = new StringBuilder();
			for (CmsCI platform : platformsOfEnv) {
				if (platform.getCiId() != platformOfBomCi.getCiId()) {
					if (excludePlatforms.length() > 0)
						excludePlatforms.append(",");
					excludePlatforms.append(platform.getCiId());
				}
			}
			requestBody.put("exclude", excludePlatforms.toString()); // excluding all platforms other than target
																		// compute's platform
		}

		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set(X_CMS_USER, ONEOPS_AUTOREPLACE_USER);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> requestWitHeaders = new HttpEntity<>(requestBody, requestHeaders);
		logger.info("requestWitHeaders Headers: " + requestWitHeaders.getHeaders().toString());
		logger.info("requestWitHeaders Body: " + requestWitHeaders.getBody().toString());

		// Start Deployment by making Transistor API call.

		@SuppressWarnings("unchecked")
		Map<String, Integer> response = restTemplate.postForObject(
				transistorUrl + "environments/" + env.getCiId() + "/deployments/deploy", requestWitHeaders, Map.class,
				params);

		logger.info("response for deployment API Call: " + response.entrySet());

		logger.info("Deployment ID object Type: " + response.get("deploymentId").getClass());
		Integer exitCode = response.get("deploymentId");

		logger.info("exitCode: " + exitCode);
		return response;
	}

	public EnvPropsProcessor getEnvProcessor() {
		return envProcessor;
	}

	public void setEnvProcessor(EnvPropsProcessor envProcessor) {
		this.envProcessor = envProcessor;
	}

	public CmsCmManager getCmManager() {
		return cmManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	public String getTransistorUrl() {
		return transistorUrl;
	}

	public void setTransistorUrl(String transistorUrl) {
		this.transistorUrl = transistorUrl;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

}