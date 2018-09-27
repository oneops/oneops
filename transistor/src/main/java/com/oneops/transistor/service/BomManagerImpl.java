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
package com.oneops.transistor.service;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.oneops.cms.cm.domain.*;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.ExceptionConsolidator;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.util.CloudUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.*;

public class BomManagerImpl implements BomManager {
	private static final Logger logger = Logger.getLogger(BomManagerImpl.class);

	private static final boolean checkSecondary = Boolean.valueOf(getProperty("transistor.checkSecondary", "true"));
	private static final boolean check4Services = Boolean.valueOf(getProperty("transistor.checkServices", "true"));

	static final String PACK_CLOUD_NS_WHITELIST_CMS_VAR_NAME = "PACK_CLOUD_NS_WHITELIST";

	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor manifestRfcProcessor;
	private CmsRfcProcessor bomRfcProcessor;
	private BomRfcBulkProcessor bomGenerationProcessor;
	private TransUtil trUtil;
	private CmsDpmtProcessor dpmtProcessor;
	private CmsUtil cmsUtil;
	private CloudUtil cloudUtil;
	Gson gson = new Gson();

	public void setCloudUtil(CloudUtil cloudUtil) {
		this.cloudUtil = cloudUtil;
	}

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setManifestRfcProcessor(CmsRfcProcessor manifestRfcProcessor) {
		this.manifestRfcProcessor = manifestRfcProcessor;
	}

	public void setBomRfcProcessor(CmsRfcProcessor bomRfcProcessor) {
		this.bomRfcProcessor = bomRfcProcessor;
	}

	public void setBomGenerationProcessor(BomRfcBulkProcessor bomGenerationProcessor) {
		this.bomGenerationProcessor = bomGenerationProcessor;
	}

	public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
		this.dpmtProcessor = dpmtProcessor;
	}

	@Override
	public Map<String, Object> generateAndDeployBom(long envId, String userId, Set<Long> excludePlats, CmsDeployment dpmt, boolean commit) {
		Map<String, Object> bomInfo = generateBomForClouds(envId, userId, excludePlats, dpmt.getComments(), commit);
		CmsRelease bomRelease = (CmsRelease) bomInfo.get("release");
		if (bomRelease != null) {
			dpmt.setNsPath(bomRelease.getNsPath());
			dpmt.setReleaseId(bomRelease.getReleaseId());
			dpmt.setCreatedBy(userId);
			CmsDeployment deployment = dpmtProcessor.deployRelease(dpmt);
			bomInfo.put("deployment", deployment);
		}
		return bomInfo;
	}

	@Override
	public Map<String, Object> generateBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
		return generateBomForClouds(envId, userId, excludePlats, desc, commit);
	}

	private Map<String, Object> generateBomForClouds(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
		long startTime = System.currentTimeMillis();

		EnvBomGenerationContext context = new EnvBomGenerationContext(envId, excludePlats, userId, cmProcessor, cmsUtil, bomRfcProcessor);
		String manifestNsPath = context.getManifestNsPath();
		String bomNsPath = context.getBomNsPath();

		trUtil.verifyAndCreateNS(bomNsPath);
		trUtil.lockNS(bomNsPath);

		if (commit) {
			//get open manifest release and soft commit it (no real deletes)
			commitManifestRelease(manifestNsPath, bomNsPath, userId, desc);
		}

		List<CmsRelease> bomReleases = bomRfcProcessor.getReleaseBy3(bomNsPath, null, "open");
		if (bomReleases.size() > 0) {
			// Should not really happen as commit above will close any open bom releases. But...
			CmsRelease bomRelease = bomReleases.get(0);
			CmsRelease manifestRelease = bomRfcProcessor.getLatestRelease(manifestNsPath, "closed").get(0);
			if (bomRelease.getParentReleaseId().equals(manifestRelease.getReleaseId())) {
				logger.info("Existing open bom release " + bomRelease.getReleaseId() + " found, returning it");

				Map<String, Object> bomInfo = null;
				try {
//				bomInfo = gson.fromJson(bomRelease.getDescription(), (new TypeToken<HashMap<String, Object>>() {}).getType());
					bomInfo = gson.fromJson(bomRelease.getDescription(), HashMap.class);
				} catch (JsonSyntaxException ignore) {
				}
				if (bomInfo == null) {
					bomInfo = new HashMap<>();
				}
				bomInfo.put("release", bomRelease);

				return bomInfo;
			}
			else {
				discardOpenBomRelease(bomNsPath);
			}
		}

		context.load();

		int execOrder = generateBomForActiveClouds(context);
		generateBomForOfflineClouds(context, execOrder);

		long rfcCiCount = 0;
		long rfcRelCount = 0;
		CmsRelease bomRelease = updateParentReleaseId(bomNsPath, manifestNsPath, "open");
		long releaseId = bomRelease == null ? 0 : bomRelease.getReleaseId();
		if (releaseId > 0) {
			bomRfcProcessor.brushExecOrder(releaseId);
			rfcCiCount = bomRfcProcessor.getRfcCiCount(releaseId);
			rfcRelCount = bomRfcProcessor.getRfcRelationCount(releaseId);

			if (rfcCiCount == 0) {
				logger.info("No release because rfc count is 0. Cleaning up release.");
				bomRfcProcessor.deleteRelease(releaseId);
				bomRelease = null;
			}
		}
		else {
			//if there is no open release check if there are global vars in pending_deletion state. If yes delete it.
			for (CmsCI localVar : cmProcessor.getCiByNsLikeByStateNaked(manifestNsPath, "manifest.Globalvar", "pending_deletion")) {
				cmProcessor.deleteCI(localVar.getCiId(), true, userId);
			}
			//if there is nothing to deploy update parent release on latest closed bom release
			updateParentReleaseId(bomNsPath, manifestNsPath, "closed");
		}

		long duration = System.currentTimeMillis() - startTime;
		logger.info(bomNsPath + " >>> Generated BOM in " + duration + " ms. Created rfcs: " + rfcCiCount + " CIs, " + rfcRelCount + " relations.");

		Map<String, Object> bomInfo = new HashMap<>();
		bomInfo.put("rfcCiCount", rfcCiCount);
		bomInfo.put("rfcRelationCount", rfcRelCount);
		bomInfo.put("manifestCommit", commit);
		bomInfo.put("generationTime", duration);

		if (bomRelease != null) {
			bomRelease.setDescription(gson.toJson(bomInfo));
			bomRfcProcessor.updateRelease(bomRelease);

			bomInfo.put("release", bomRelease);

//			if (logger.isInfoEnabled()) {
			if (logger.isDebugEnabled()) {
				String rfcs = bomRfcProcessor.getRfcCIBy3(releaseId, true, null).stream()
						.map(rfc -> rfc.getNsPath() + " " + rfc.getExecOrder() + " !! " + rfc.getCiClassName() + " !! " + rfc.getCiName() + " -- " + rfc.getRfcAction() + " -- " + rfc.getAttributes().size())
						.sorted(String::compareTo)
						.collect(Collectors.joining("\n", "", "\n"));
				rfcs = bomRfcProcessor.getRfcRelationBy3(releaseId, true, null).stream()
						.map(rfc -> rfc.getNsPath() + " " + rfc.getExecOrder() + " !! " + rfc.getRelationName() + " -- " + rfc.getRfcAction() + " -- " + rfc.getAttributes().size() + " -- " + rfc.getComments())
						.sorted(String::compareTo)
						.collect(Collectors.joining("\n", rfcs, ""));
				logger.debug(rfcs);
//				System.out.println(rfcs);
			}

		}
		return bomInfo;
	}

	private int generateBomForActiveClouds(EnvBomGenerationContext context) {
		String envManifestNsPath = context.getManifestNsPath();
		logger.info(envManifestNsPath + " >>> Starting generating BOM for active clouds... ");
		long globalStartTime = System.currentTimeMillis();

		Map<Integer, List<CmsCI>> platsToProcess = getOrderedPlatforms(context);

		if (check4Services) {
			cloudUtil.check4missingServices(getPlatformIds(platsToProcess));
		}

		ExceptionConsolidator<TransistorException> packCloudWhiteListEC = new ExceptionConsolidator<>(TransistorException.class, CmsError.TRANSISTOR_CANNOT_DEPLOY_PACK_TO_CLOUD, 100);
		int startingExecOrder = 1;
		int maxOrder = getMaxExecOrder(platsToProcess);
		for (int i = 1; i <= maxOrder; i++) {
			if (platsToProcess.containsKey(i)) {
				startingExecOrder = (startingExecOrder > 1) ? startingExecOrder + 1 : startingExecOrder;
				int stepMaxOrder = 0;
				for (CmsCI platform : platsToProcess.get(i)) {
					long platStartTime = System.currentTimeMillis();
					List<CmsCIRelation> platformCloudRels = cmProcessor.getFromCIRelations(platform.getCiId(), BASE_CONSUMES, "account.Cloud");
					if (platformCloudRels.size() == 0) {
						//if platform does not have a relation to the cloud - consider it disabled
						continue;
					}

					if (checkSecondary) {
						check4Secondary(context.loadPlatformContext(platform), platformCloudRels);
					} else {
						logger.info("check secondary not configured.");
					}

					int platExecOrder = startingExecOrder;
					int thisPlatMaxExecOrder = 0;
					SortedMap<Integer, SortedMap<Integer, List<CmsCIRelation>>> orderedClouds = getOrderedClouds(platformCloudRels, false);
					for (SortedMap<Integer, List<CmsCIRelation>> priorityClouds : orderedClouds.values()) {
						for (List<CmsCIRelation> orderCloud : priorityClouds.values()) {
							for (CmsCIRelation platformCloudRel : orderCloud) {
								//now we need to check if the cloud is active for this given platform
								CmsCIRelationAttribute adminstatus = platformCloudRel.getAttribute("adminstatus");
								if (adminstatus != null && !CmsConstants.CLOUD_STATE_ACTIVE.equals(adminstatus.getDjValue())) {
									continue;
								}

								// Must load platform context again in case it was dirty after variable interpolation - the 'loadPlatformContext'
								// is smart to do partial reload if necessary.
								PlatformBomGenerationContext platformContext = context.loadPlatformContext(platform);
								int maxExecOrder;
								if (context.getDisabledPlatformIds().contains(platform.getCiId()) || platform.getCiState().equalsIgnoreCase("pending_deletion")) {
									maxExecOrder = bomGenerationProcessor.deleteManifestPlatform(context, platformContext, platformCloudRel, platExecOrder);
								} else {
									TransistorException ex = packCloudWhiteListEC.invokeChecked(() -> checkPackCloudWhiteList(platform, platformCloudRel.getToCi()));
									if (ex != null) continue;

									maxExecOrder = bomGenerationProcessor.processManifestPlatform(context, platformContext, platformCloudRel, platExecOrder, true);
								}
								stepMaxOrder = (maxExecOrder > stepMaxOrder) ? maxExecOrder : stepMaxOrder;
								thisPlatMaxExecOrder = (maxExecOrder > thisPlatMaxExecOrder) ? maxExecOrder : thisPlatMaxExecOrder;
							}
							platExecOrder = (thisPlatMaxExecOrder > platExecOrder) ? thisPlatMaxExecOrder + 1 : platExecOrder;
						}
					}
					logger.info(platform.getNsPath() + " >>> Done generating BOM for platform " + platform.getCiName() + "for all active clouds in " + (System.currentTimeMillis() - platStartTime) + " ms.");
				}
				startingExecOrder = (stepMaxOrder > 0) ? stepMaxOrder + 1 : startingExecOrder;
			}
		}
		packCloudWhiteListEC.rethrowExceptionIfNeeded("Some platforms can not be deployed to certain clouds:\n",
													  ".\nShutdown the disallowed clouds for these platforms before proceeding.",
													  ";\n");
		logger.info(envManifestNsPath + " >>> Done generating BOM for active clouds in " + (System.currentTimeMillis() - globalStartTime) + " ms.");

		return startingExecOrder;
	}

	private Set<Long> getPlatformIds(Map<Integer, List<CmsCI>> platsToProcess) {
		return platsToProcess.entrySet()
				.stream()
				.flatMap(e -> e.getValue().stream())
				.map(CmsCIBasic::getCiId)
				.collect(toSet());
	}

	void check4Secondary(PlatformBomGenerationContext context, List<CmsCIRelation> platformCloudRels) {
		String nsPath = context.getBomNsPath();
		List<CmsCIRelation> entryPoints = context.getEntryPoints();
		if(entryPoints.size() == 0) {
			// Some platforms do not have entrypoints.
			logger.info("Skipping secondary check - there is no entry point for platform " + context.getPlatform().getCiId() + " in " + nsPath);
			return;
		}

		Function<CmsCIRelation, Integer> getPriority = (deployedTo) -> deployedTo.getAttribute("priority") != null ? Integer.valueOf(deployedTo.getAttribute("priority").getDjValue()) : Integer.valueOf(0);
		CmsCIRelation entryPoint = entryPoints.get(0);
		//get manifest clouds and priority; what is intended
		Map<Long, Integer> intendedCloudPriority = platformCloudRels.stream()
				.filter(cloudUtil::isCloudActive)
				.collect(toMap(CmsCIRelationBasic::getToCiId, getPriority, (i, j) -> i));
		//are there any secondary clouds for deployment
		long numberOfSecondaryClouds = intendedCloudPriority.entrySet().stream()
				.filter(entry -> (entry.getValue().equals(SECONDARY_CLOUD_STATUS)))
				.count();
		if (numberOfSecondaryClouds == 0) {
			return;
		}

		String entryPointClass = trUtil.getShortClazzName(entryPoint.getToCi().getCiClassName());
		Set<Long> cloudIds = platformCloudRels.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet());
		Map<Long, Integer> existingCloudPriority = context.getBomRelations().stream()
				.filter(r -> cloudIds.contains(r.getToCiId()) && r.getRelationName().equals(DEPLOYED_TO) && trUtil.getShortClazzName(r.getFromCi().getCiClassName()).equals(entryPointClass))
				.collect(toMap(CmsCIRelationBasic::getToCiId, getPriority, Math::max));

		HashMap<Long, Integer> computedCloudPriority = new HashMap<>(existingCloudPriority);
		computedCloudPriority.putAll(intendedCloudPriority);

		//Now, take  all offline clouds from
		Map<Long, Integer> offlineClouds = platformCloudRels.stream()
				.filter(cloudUtil::isCloudOffline)
				.collect(toMap(CmsCIRelationBasic::getToCiId, getPriority, (i, j) -> i));
		if(!offlineClouds.isEmpty()){
			offlineClouds.forEach((k,v)->{
				if(computedCloudPriority.containsKey(k)){
					computedCloudPriority.remove(k);
				}
			});
		}

		long count = computedCloudPriority.entrySet().stream().filter(entry -> (entry.getValue().equals(CmsConstants.SECONDARY_CLOUD_STATUS))).count();
		if (computedCloudPriority.size() == count) {
			//throw transistor exception
			String message;
			String clouds = platformCloudRels.stream()
					.filter(rel-> !cloudUtil.isCloudActive(rel) && (getPriority.apply(rel) == PRIMARY_CLOUD_STATUS))
					.map(rel -> rel.getToCi().getCiName())
					.collect(joining(","));

			if (StringUtils.isNotEmpty(clouds)) {
				message = String.format("The deployment will result in no instances in primary clouds for platform %s. Primary clouds <%s>  are not in active state for this platform.  ", nsPath, clouds);
			} else {
				message = String.format("The deployment will result in no instances in primary clouds for platform %s. Please check the cloud priority of the clouds. .  ", nsPath);
			}

			throw new TransistorException(CmsError.TRANSISTOR_ALL_INSTANCES_SECONDARY, message);
		}
	}

	private int generateBomForOfflineClouds(EnvBomGenerationContext context, int startingExecOrder) {
		logger.info(context.getManifestNsPath() + " >>> Starting generating BOM for offline clouds... ");
		long globalStartTime = System.currentTimeMillis();

		Map<Integer, List<CmsCI>> platsToProcess = getOrderedPlatforms(context);


		int maxOrder = getMaxExecOrder(platsToProcess);

		for (int i = 1; i <= maxOrder; i++) {
			if (platsToProcess.containsKey(i)) {
				startingExecOrder = (startingExecOrder > 1) ? startingExecOrder + 1 : startingExecOrder;
				int stepMaxOrder = 0;
				for (CmsCI platform : platsToProcess.get(i)) {
					//now we need to check if the cloud is active for this given platform
					List<CmsCIRelation> platformCloudRels = cmProcessor.getFromCIRelations(platform.getCiId(), BASE_CONSUMES, "account.Cloud");
					if (platformCloudRels.size() == 0) {
						//if platform does not have a relation to the cloud - consider it disabled
						continue;
					}

					int platExecOrder = startingExecOrder;
					SortedMap<Integer, SortedMap<Integer, List<CmsCIRelation>>> orderedClouds = getOrderedClouds(platformCloudRels, true);
					for (SortedMap<Integer, List<CmsCIRelation>> priorityClouds : orderedClouds.values()) {
						for (List<CmsCIRelation> orderCloud : priorityClouds.values()) {
							for (CmsCIRelation platformCloudRel : orderCloud) {
								CmsCIRelationAttribute adminstatus = platformCloudRel.getAttribute("adminstatus");
								if (adminstatus != null && CmsConstants.CLOUD_STATE_OFFLINE.equals(adminstatus.getDjValue())) {
									int maxExecOrder = bomGenerationProcessor.deleteManifestPlatform(context, context.loadPlatformContext(platform), platformCloudRel, platExecOrder);
									stepMaxOrder = (maxExecOrder > stepMaxOrder) ? maxExecOrder : stepMaxOrder;
								}
							}
							platExecOrder = (stepMaxOrder > platExecOrder) ? stepMaxOrder + 1 : platExecOrder;
						}
					}
				}
				startingExecOrder = (stepMaxOrder > 0) ? stepMaxOrder + 1 : startingExecOrder;
			}
		}
		logger.info(context.getManifestNsPath() + " >>> Done generating BOM for offline clouds in " + (System.currentTimeMillis() - globalStartTime) + " ms.");

		return startingExecOrder;
	}

	private Integer getMaxExecOrder(Map<Integer, List<CmsCI>> platsToProcess) {
		return platsToProcess.keySet().stream().max(Comparator.comparingInt(i -> i)).orElse(0);
	}

	SortedMap<Integer, SortedMap<Integer, List<CmsCIRelation>>> getOrderedClouds(List<CmsCIRelation> cloudRels, boolean reverse) {

		SortedMap<Integer, SortedMap<Integer, List<CmsCIRelation>>> result = reverse ?
				new TreeMap<>(Collections.reverseOrder())
					: new TreeMap<>();

		for (CmsCIRelation binding : cloudRels) {

			Integer priority = Integer.valueOf(binding.getAttribute("priority").getDjValue());
			Integer order = 1;
			if (binding.getAttributes().containsKey("dpmt_order") && !binding.getAttribute("dpmt_order").getDjValue().isEmpty()) {
				order = Integer.valueOf(binding.getAttribute("dpmt_order").getDjValue());
			}
			if (!result.containsKey(priority)) {
				result.put(priority, new TreeMap<>());
			}
			if (!result.get(priority).containsKey(order)) {
				result.get(priority).put(order, new ArrayList<>());
			}
			result.get(priority).get(order).add(binding);
		}

		return result;
	}


	@Override
	public long submitDeployment(long releaseId, String userId, String desc){
		CmsRelease bomRelease = bomRfcProcessor.getReleaseById(releaseId);
		CmsDeployment dpmt = new CmsDeployment();
		dpmt.setNsPath(bomRelease.getNsPath());
		dpmt.setReleaseId(bomRelease.getReleaseId());
		dpmt.setCreatedBy(userId);
		if (desc!=null) {
			dpmt.setComments(desc);
		}
		CmsDeployment newDpmt = dpmtProcessor.deployRelease(dpmt);
		logger.info("created new deployment - " + newDpmt.getDeploymentId());
		return newDpmt.getDeploymentId();
	}

	@Override
	public Map<String, Object> scaleDown(CmsCI platformCi, CmsCI envCi, int scaleDownBy,
										 int minComputesInEachCloud, boolean ensureEvenScale,  String userId) {
		long startTime = System.currentTimeMillis();
		CmsDeployment deployment = bomGenerationProcessor.scaleDown(platformCi, envCi, scaleDownBy,
				minComputesInEachCloud, ensureEvenScale, userId);
		long endTime = System.currentTimeMillis();
		Map<String, Object> bomInfo = new HashMap<>();
		if (deployment != null) {
			long releaseId = deployment.getReleaseId();
			if (releaseId != 0) {
				bomInfo.put("releaseId", releaseId);
				bomInfo.put("rfcCiCount", bomRfcProcessor.getRfcCiCount(releaseId));
				bomInfo.put("rfcRelationCount", bomRfcProcessor.getRfcRelationCount(releaseId));
				bomInfo.put("manifestCommit", false);
				bomInfo.put("generationTime", endTime - startTime);
			}
		}

		if (deployment != null) {
			deployment = dpmtProcessor.deployRelease(deployment);
			bomInfo.put("deploymentId", deployment.getDeploymentId());
			bomInfo.put("deployment", deployment);
		}
		return bomInfo;
	}

	private CmsRelease updateParentReleaseId(String nsPath, String manifestNsPath, String bomReleaseState) {
		List<CmsRelease> releases = bomRfcProcessor.getLatestRelease(nsPath, bomReleaseState);
		if (releases.size() == 0) return null;

		CmsRelease bomRelease = releases.get(0);
		List<CmsRelease> manifestReleases = manifestRfcProcessor.getLatestRelease(manifestNsPath, "closed");
		if (manifestReleases.size() > 0) bomRelease.setParentReleaseId(manifestReleases.get(0).getReleaseId());
		bomRfcProcessor.updateRelease(bomRelease);
		return bomRelease;
	}

	Map<Integer, List<CmsCI>> getOrderedPlatforms(EnvBomGenerationContext context) {
		List<CmsCI> platforms = context.getPlatforms();
		List<CmsCIRelation> allLinksToRels = context.getLinksToRelations();

		Map<Long, Integer> plat2ExecOrderMap = new HashMap<>();
		Map<Long, CmsCI> platformIdMap = new HashMap<>();
		for (CmsCI platform : platforms) {
			platformIdMap.put(platform.getCiId(), platform);
			long linkToCount = allLinksToRels.stream().filter(r -> r.getFromCiId() == platform.getCiId()).count();
			if (linkToCount == 0) {
				plat2ExecOrderMap.put(platform.getCiId(), 1);
				processPlatformsOrder(platform.getCiId(), plat2ExecOrderMap, allLinksToRels);
			}
		}

		Set<Long> disabledPlats = context.getDisabledPlatformIds();
		int maxExecOrder = plat2ExecOrderMap.values().stream().max(Comparator.comparingInt(i -> i)).orElse(0);
		for (long platId : plat2ExecOrderMap.keySet()) {
			CmsCI plat = platformIdMap.get(platId);
			if ("pending_deletion".equalsIgnoreCase(plat.getCiState()) || disabledPlats.contains(plat.getCiId())) {
				plat2ExecOrderMap.put(platId, maxExecOrder+1);
			}
		}

		Set<Long> excludedPlats = context.getExcludedPlats();
		Map<Integer, List<CmsCI>> execOrder2PlatMap = new HashMap<>();
		for (long platId : plat2ExecOrderMap.keySet()) {
			if (excludedPlats == null || !excludedPlats.contains(platId)) {
				execOrder2PlatMap.computeIfAbsent(plat2ExecOrderMap.get(platId), ArrayList::new).add(platformIdMap.get(platId));
			}
		}

		return execOrder2PlatMap;
	}

	private void processPlatformsOrder(long startPlatId, Map<Long, Integer> platExecOrderMap, List<CmsCIRelation> allLinksToRels) {
		List<CmsCIRelation> linksToRels = allLinksToRels.stream().filter(r -> r.getToCiId() == startPlatId).collect(Collectors.toList());
		int execOrder = platExecOrderMap.get(startPlatId) + 1;
		for (CmsCIRelation linkToRel : linksToRels) {
			long fromCiId = linkToRel.getFromCiId();
			if (!platExecOrderMap.containsKey(fromCiId)) {
				platExecOrderMap.put(fromCiId, execOrder);
			} else if (platExecOrderMap.get(fromCiId) < execOrder) {
				platExecOrderMap.put(fromCiId, execOrder);
			}
			processPlatformsOrder(fromCiId, platExecOrderMap, allLinksToRels);
		}
	}

	private void commitManifestRelease(String manifestNsPath, String bomNsPath, String userId, String desc) {
		List<CmsRelease> manifestReleases = manifestRfcProcessor.getReleaseBy3(manifestNsPath, null, "open");
		for (CmsRelease release : manifestReleases) {
			manifestRfcProcessor.commitRelease(release.getReleaseId(), true, null,false,userId, desc);
		}
		// now we have a special case for the LinksTo relations
		// since nothing is really deleted but just marked as pending deletion until the bom is processed
		// but we need to delete LinksTo right now here because if nothing needs to be deployed or in case of circular
		// dependency the deployment will never happen
		List<CmsCIRelation> dLinkesToRels = cmProcessor.getCIRelationsNakedNoAttrsByState(manifestNsPath, "manifest.LinksTo", "pending_deletion", "manifest.Platform", "manifest.Platform");
		for (CmsCIRelation rel : dLinkesToRels) {
			cmProcessor.deleteRelation(rel.getCiRelationId(),true);
		}

		//if we have new manifest release - discard open bom release
		if (manifestReleases.size() > 0) {
			discardOpenBomRelease(bomNsPath);
		}
	}

	private void discardOpenBomRelease(String bomNsPath) {
		if (dpmtProcessor.getOpenDeployments(bomNsPath) == null) {
			manifestRfcProcessor.getReleaseBy3(bomNsPath, null, "open").forEach(r -> {
				r.setReleaseState("canceled");
				manifestRfcProcessor.updateRelease(r);
			});
		}
	}

	@Override
	public void check4openDeployment(String nsPath) {
		CmsDeployment openDeployments = dpmtProcessor.getOpenDeployments(nsPath);
		if (openDeployments != null) {
			String err = "There is an active deployment " + openDeployments.getDeploymentId() + " in this environment with id , you need to cancel or retry it,";
			throw new TransistorException(CmsError.TRANSISTOR_ACTIVE_DEPLOYMENT_EXISTS, err);
		}
	}

	void checkPackCloudWhiteList(CmsCI platform, CmsCI cloud) {
		CmsVar cmsVar = cmProcessor.getCmSimpleVar(PACK_CLOUD_NS_WHITELIST_CMS_VAR_NAME);
		if (cmsVar == null ) return;
		String json = cmsVar.getValue();
		if (json == null || json.isEmpty()) return;

		try {
			Map<String, Map<String, List<String>>> whitelist = gson.fromJson(json, (new TypeToken<Map<String, Map<String, List<String>>>>() {}).getType());
			String packKey = platform.getAttribute("source").getDfValue() + "/" + platform.getAttribute("pack").getDfValue();
			Map<String, List<String>> packWhiteList = whitelist.containsKey(packKey) ? whitelist.get(packKey) :
					whitelist.get(packKey + ":" + platform.getAttribute("version").getDfValue());
			if (packWhiteList == null) return;
			List<String> namespaces = packWhiteList.keySet().stream()
			.filter(c -> {
				// It could be either a simple value to match against ciName or ':' delimited tokens specifying attribute name and value.
				String[] split = c.split(":");
				String matchValue = split[split.length > 1 ? 1 : 0].toLowerCase();
				String cloudValue;
				if (split.length > 1) {
					CmsCIAttribute attribute = cloud.getAttribute(split[0]);
					if (attribute == null) return false;
					cloudValue = attribute.getDfValue();
				} else {
					cloudValue = cloud.getCiName();
				}
				return cloudValue != null && cloudValue.toLowerCase().contains(matchValue);
			})
			.map(packWhiteList::get)
			.findFirst().orElse(null);
			if (namespaces == null || namespaces.isEmpty()) return;
			String platformNsPath = platform.getNsPath();
			boolean ok = namespaces.stream().anyMatch(platformNsPath::startsWith);
			if (!ok) {
				String message = "platform '" + platform.getCiName() + "' can not be deployed to cloud '" + cloud.getCiName();
				throw new TransistorException(CmsError.TRANSISTOR_CANNOT_DEPLOY_PACK_TO_CLOUD, message);
			}

		} catch (JsonSyntaxException e) {
			logger.warn("Failed to parse PACK_CLOUD_NS_WHITELIST json: " + json + ". Exception: " + e.getMessage());
		}
	}
}
