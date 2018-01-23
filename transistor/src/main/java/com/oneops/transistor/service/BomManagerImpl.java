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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.oneops.cms.cm.domain.*;
import com.oneops.transistor.util.CloudUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;

import static com.oneops.cms.util.CmsConstants.*;
import static com.oneops.cms.util.CmsError.TRANSISTOR_ALL_INSTANCES_SECONDARY;
import static java.lang.System.getProperty;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

public class BomManagerImpl implements BomManager {
	private static final Logger logger = Logger.getLogger(BomManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor rfcProcessor;
	private BomRfcBulkProcessor bomRfcProcessor;
	private TransUtil trUtil;
	private CmsDpmtProcessor dpmtProcessor;
	private CmsUtil cmsUtil;
	private CloudUtil cloudUtil;

	private static final boolean checkSecondary = Boolean.valueOf(getProperty("transistor.checkSecondary", "true"));
	private static final boolean check4Services = Boolean.valueOf(getProperty("transistor.checkServices", "true"));

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

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setBomRfcProcessor(BomRfcBulkProcessor bomRfcProcessor) {
		this.bomRfcProcessor = bomRfcProcessor;
	}

	public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
		this.dpmtProcessor = dpmtProcessor;
	}

	@Override
	public long generateAndDeployBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
		long releaseId = generateBom(envId, userId, excludePlats, desc, commit);
		if (releaseId > 0) {
			return submitDeployment(releaseId, userId, desc);
		} else {
			return 0;
		}
	}

	@Override
	public long generateBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
		return generateBomForClouds(envId, userId, excludePlats, desc, commit);
	}

	private long generateBomForClouds(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit) {
		long startTime = System.currentTimeMillis();

		EnvBomGenerationContext context = new EnvBomGenerationContext(envId, excludePlats, userId, cmProcessor, cmsUtil, rfcProcessor);
		String manifestNsPath = context.getManifestNsPath();
		String bomNsPath = context.getBomNsPath();

		check4openDeployment(bomNsPath);

		trUtil.verifyAndCreateNS(bomNsPath);
		trUtil.lockNS(bomNsPath);

		if (commit) {
			//get open manifest release and soft commit it (no real deletes)
			commitManifestRelease(manifestNsPath, bomNsPath, userId, desc);
		}

		//if we have an open bom release then return the release id
		List<CmsRelease> bomReleases = rfcProcessor.getReleaseBy3(bomNsPath, null, "open");
		if (bomReleases.size() > 0) {
			long bomReleaseId = bomReleases.get(0).getReleaseId();
			logger.info("Existing open bom release " + bomReleaseId + " found, returning it");
			return bomReleaseId;
		}

		context.load();

		int execOrder = generateBomForActiveClouds(context);
		generateBomForOfflineClouds(context, execOrder);

		long rfcCiCount = 0;
		long rfcRelCount = 0;
		long releaseId = getPopulateParentAndGetReleaseId(bomNsPath, manifestNsPath, "open");
		if (releaseId > 0) {
			rfcProcessor.brushExecOrder(releaseId);
			rfcCiCount = rfcProcessor.getRfcCiCount(releaseId);
			rfcRelCount = rfcProcessor.getRfcRelationCount(releaseId);

//			if (logger.isInfoEnabled()) {
			if (logger.isDebugEnabled()) {
				String rfcs = rfcProcessor.getRfcCIBy3(releaseId, true, null).stream()
						.map(rfc -> rfc.getNsPath() + " " + rfc.getExecOrder() + " !! " + rfc.getCiClassName() + " !! " + rfc.getCiName() + " -- " + rfc.getRfcAction() + " -- " + rfc.getAttributes().size())
						.sorted(String::compareTo)
						.collect(Collectors.joining("\n", "", "\n"));
				rfcs = rfcProcessor.getRfcRelationBy3(releaseId, true, null).stream()
						.map(rfc -> rfc.getNsPath() + " " + rfc.getExecOrder() + " !! " + rfc.getRelationName() + " -- " + rfc.getRfcAction() + " -- " + rfc.getAttributes().size() + " -- " + rfc.getComments())
						.sorted(String::compareTo)
						.collect(Collectors.joining("\n", rfcs, ""));
				logger.debug(rfcs);
//				System.out.println(rfcs);
			}

			if (rfcCiCount == 0) {
				logger.info("No release because rfc count is 0. Cleaning up release.");
				rfcProcessor.deleteRelease(releaseId);
			}
		}
		else {
			//if there is no open release check if there are global vars in pending_deletion state. If yes delete it.
			for (CmsCI localVar : cmProcessor.getCiByNsLikeByStateNaked(manifestNsPath, "manifest.Globalvar", "pending_deletion")) {
				cmProcessor.deleteCI(localVar.getCiId(), true, userId);
			}
			//if there is nothing to deploy update parent release on latest closed bom release
			getPopulateParentAndGetReleaseId(bomNsPath, manifestNsPath, "closed");
		}

		logger.info(bomNsPath + " >>> Generated BOM in " + (System.currentTimeMillis() - startTime) + " ms. Created rfcs: " + rfcCiCount + " CIs, " + rfcRelCount + " relations.");
		return releaseId;
	}

	private int generateBomForActiveClouds(EnvBomGenerationContext context) {
		String envManifestNsPath = context.getManifestNsPath();
		logger.info(envManifestNsPath + " >>> Starting generating BOM for active clouds... ");
		long globalStartTime = System.currentTimeMillis();

		Map<Integer, List<CmsCI>> platsToProcess = getOrderedPlatforms(context);

		if (check4Services) {
			cloudUtil.check4missingServices(getPlatformIds(platsToProcess));
		}

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
						check4Secondary(new PlatformBomGenerationContext(platform, context, cmProcessor, cmsUtil), platformCloudRels);
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

								PlatformBomGenerationContext platformContext = new PlatformBomGenerationContext(platform, context, cmProcessor, cmsUtil);
								int maxExecOrder;
								if (context.getDisabledPlatformIds().contains(platform.getCiId()) || platform.getCiState().equalsIgnoreCase("pending_deletion")) {
									maxExecOrder = bomRfcProcessor.deleteManifestPlatform(context, platformContext, platformCloudRel, platExecOrder);
								} else {
									maxExecOrder = bomRfcProcessor.processManifestPlatform(context, platformContext, platformCloudRel, platExecOrder, true);
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

	protected void check4Secondary(PlatformBomGenerationContext context, List<CmsCIRelation> platformCloudRels) {
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

			throw new TransistorException(TRANSISTOR_ALL_INSTANCES_SECONDARY, message);
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
									int maxExecOrder = bomRfcProcessor.deleteManifestPlatform(context, context.getPlatformContext(platform), platformCloudRel, platExecOrder);
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
		CmsRelease bomRelease = rfcProcessor.getReleaseById(releaseId);
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

	private long getPopulateParentAndGetReleaseId(String nsPath, String manifestNsPath, String bomReleaseState) {
		List<CmsRelease> releases = rfcProcessor.getLatestRelease(nsPath, bomReleaseState);
		if (releases.size() >0) {
			CmsRelease bomRelease = releases.get(0);
			List<CmsRelease> manifestReleases = rfcProcessor.getLatestRelease(manifestNsPath, "closed");
			if (manifestReleases.size()>0) bomRelease.setParentReleaseId(manifestReleases.get(0).getReleaseId());
			rfcProcessor.updateRelease(bomRelease);
			return bomRelease.getReleaseId();
		}
		return 0;
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
		List<CmsRelease> manifestReleases = rfcProcessor.getReleaseBy3(manifestNsPath, null, "open");
		for (CmsRelease release : manifestReleases) {
			rfcProcessor.commitRelease(release.getReleaseId(), true, null,false,userId, desc);
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
		if (manifestReleases.size()>0) {
			List<CmsRelease> bomReleases = rfcProcessor.getReleaseBy3(bomNsPath, null, "open");
			for (CmsRelease bomRel : bomReleases) {
				bomRel.setReleaseState("canceled");
				rfcProcessor.updateRelease(bomRel);
			}
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

}
