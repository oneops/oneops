package com.oneops.transistor.ws.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsRfcProcessor;

@RequestMapping(value = "/compute")
@Controller
public class ComputeRSController extends AbstractRestController {

	private static Logger logger = Logger.getLogger(ComputeRSController.class);

	private CmsCmProcessor cmProcessor;
	private RestTemplate restTemplate;
	private String transistorUrl;
	private CmsCmManager cmManager;

	// following objects are taken from OMPS EnvPropsProcessor class implementation
	private CmsRfcProcessor rfcProcessor;// TODO: setup app context
	private static final long COOLOFF_PREIOD_4_RELEASE = 180000; //TODO: is this really required?

	//TODO: Check with team how to handle security of this API, User ID of the calling API should be used here.
	protected static final String X_CMS_USER = "X-Cms-User";
	protected static final String ONEOPS_AUTO_REPLACE_USER_PROP_NAME = "oneops-auto-replace-user";
	// protected static final String ONEOPS_AUTOREPLACE_USER =
	// System.getProperty(ONEOPS_AUTO_REPLACE_USER_PROP_NAME,"oneops-autoreplace");
	// // set value after testing
	protected static final String ONEOPS_AUTOREPLACE_USER = ONEOPS_AUTO_REPLACE_USER_PROP_NAME; // TODO: Since this is not auto-replace rather API driven replacement, check with team how to get API user

	
	public CmsRfcProcessor getRfcProcessor() {
		return rfcProcessor;
	}

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public CmsCmManager getCmManager() {
		return cmManager;
	}

	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	public CmsCmProcessor getCmProcessor() {
		return cmProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public RestTemplate getRestTemplate() {
		return restTemplate;
	}

	public void setRestTemplate(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	public String getTransistorUrl() {
		return transistorUrl;
	}

	public void setTransistorUrl(String transistorUrl) {
		this.transistorUrl = transistorUrl;
	}

	@RequestMapping(value = "/replaceCompute/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Long>  replaceComputeByCid(@PathVariable long ciId) {

		logger.info("compute replacement request received for ciId: " + ciId);

		CmsCI platform = getPlatform4Bom(ciId);
		logger.info("Platform name for compute : " + platform.getCiName());

		CmsCI env = getEnv4Platform(platform);
		logger.info("Oneops environment for ciId : " + env.getCiId() + ": "+env.getCiName());

		// check Release Status, do not deploy if marked Open, Cancelled, Discarded or Closed.
		boolean releaseStatus = isOpenRelease4Env(env);
		logger.info("Release Status : " + releaseStatus);
		Map<String,Long> result = new HashMap<>(1);
	
		if (!releaseStatus) {
			result=replace(ciId, env); // replace compute
			return result; 
			
		}
		result.put("Compute could not be replaced", 0L); //TODO: check with team if failure code should be 0L
		return result;

	}

	private Map<String, Long> replace(long ciId, CmsCI env) {

		
		// first mark the ci state as "replace" 
		//TODO : do not mark compute for replacement if there is already a deployment pending or stuck
		// though we are making a check before coming to this level however if another request is fired almost same time, this can conflict. 
		// As of now there is no way to acquire lock on environment before heading for any operations
		cmManager.updateCiState(ciId, "replace", "bom.ManagedVia", "to", false, ONEOPS_AUTOREPLACE_USER);
		logger.info("marked the ciId [" + ciId + "] for replace using headers using user" + ONEOPS_AUTOREPLACE_USER);

		
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("description", "Auto-Replace by OneOps [" + env.getNsPath() + "]");

		Map<String, String> params = new HashMap<>();
		params.put("envId", String.valueOf(env.getCiId()));

		List<CmsCI> platformsOfEnv=getPlatformsForEnv(env.getCiId()); 
		CmsCI platformOfBomCi = getPlatform4Bom(ciId);
		
		if (platformsOfEnv.size() > 1) {
			StringBuilder excludePlatforms = new StringBuilder();
			for (CmsCI platform : platformsOfEnv) {
				if (platform.getCiId() != platformOfBomCi.getCiId()) {
					if (excludePlatforms.length() > 0) excludePlatforms.append(",");
					excludePlatforms.append(platform.getCiId());
				}
			}
			requestBody.put("exclude", excludePlatforms.toString()); // excluding all platforms other than target compute's platform
		}
		
		
		HttpHeaders requestHeaders = new HttpHeaders();
		requestHeaders.set(X_CMS_USER, ONEOPS_AUTOREPLACE_USER);
		requestHeaders.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<Map<String, String>> requestWitHeaders = new HttpEntity<>(requestBody, requestHeaders);
		logger.info("requestWitHeaders Headers: "+requestWitHeaders.getHeaders().toString());
		logger.info("requestWitHeaders Body: "+requestWitHeaders.getBody().toString());
		
		// Start Deployment
		
		  @SuppressWarnings("unchecked") Map<String, Long> response =
		  restTemplate.postForObject( transistorUrl + "environments/" + env.getCiId() +
		  "/deployments/deploy", requestWitHeaders, Map.class, params);
		  
		  logger.info("response for deployment API Call: " + response.entrySet());
		  Long exitCode = response.get("deploymentId"); 
		  logger.info("exitCode: " + exitCode);
		 return response;
	}

	public CmsCI getPlatform4Bom(long ciId) {

		List<CmsCIRelation> manifestCiRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs", null,
				null);

		if (manifestCiRels.size() > 0) {
			long manifestCiId = manifestCiRels.get(0).getFromCiId();
			List<CmsCIRelation> manifestPlatRels = cmProcessor.getToCIRelations(manifestCiId, "manifest.Requires", null,
					"manifest.Platform");

			if (manifestPlatRels != null && manifestPlatRels.size() > 0) {
				return manifestPlatRels.get(0).getFromCi();
			}
		}

		return null;
	}

	public CmsCI getEnv4Platform(CmsCI platform) {
		if (platform == null) {
			logger.error("platform is null, can not get environment for it");
			return null;
		}

		List<CmsCIRelation> envRels = cmProcessor.getToCIRelations(platform.getCiId(), "manifest.ComposedOf", null,
				"manifest.Environment");

		if (envRels.size() > 0) {
			return envRels.get(0).getFromCi();
		}
		return null;
	}

	
	public List<CmsCI> getPlatformsForEnv(long ciId) {
		List<CmsCI> platforms = new ArrayList<>();
		List<CmsCIRelation> envToPlatformsRels = cmProcessor.getFromCIRelations(ciId, "manifest.ComposedOf",null, "manifest.Platform");
		if (envToPlatformsRels != null) {
			for (CmsCIRelation rel : envToPlatformsRels) {
				platforms.add(rel.getToCi());
			}
		}
		return platforms;
	}
	
	 
	public boolean isOpenRelease4Env(CmsCI env) {
		String envNsPath = env.getNsPath() + "/" + env.getCiName();
		List<CmsRelease> manReleases = rfcProcessor.getLatestRelease(envNsPath + "/manifest", null);
		
		// scenario 1: closed, when completed deployment
		// scenario 2: closed, even when deployment plan has been generated & yet to be deployed. In this case "bomReleases" are use to check status of release!	
		logger.info("current release state for "+envNsPath +" :"+manReleases.get(0).getReleaseState()); // logging release state for env
		
		if (manReleases.size() > 0) {
			if ("open".equals(manReleases.get(0).getReleaseState())) {
				return true;
			}
		}
		String bomNs = envNsPath + "/bom";
		List<CmsRelease> bomReleases = rfcProcessor.getLatestRelease(bomNs, null);
		logger.info("Latest Releases for : "+bomReleases +": " +bomReleases.toString());

		if (bomReleases.size() > 0) {
			if (!"closed".equals(bomReleases.get(0).getReleaseState())) {
				return true;
			} else if ((System.currentTimeMillis()
					- bomReleases.get(0).getUpdated().getTime()) < COOLOFF_PREIOD_4_RELEASE) {
				return true; // API driven deployment not allowed more than once in 3 minutes, this condition is copied from auto-compute replacement. Can be removed if team do not need cool off period.  
			} else {
				List<CmsRelease> latestClosedManifestReleases = rfcProcessor.getLatestRelease(envNsPath + "/manifest",
						"closed");
				if (latestClosedManifestReleases.size() == 0) {
					logger.info("Env " + envNsPath + " has no committed release.");
					return true;
				}
				if (bomReleases.get(0).getParentReleaseId() != latestClosedManifestReleases.get(0).getReleaseId()) {
					// latest bom release is closed (deployed) and it's parent manifest release id
					// is the same as latest manifest release id
					// This means there are changes which are committed but not deployed yet by the
					// user.
					logger.info("Env namespace " + envNsPath + " has changes which are committed but not deployed.");
					return true;
				}
			}
		}

		return false;
	}

}
