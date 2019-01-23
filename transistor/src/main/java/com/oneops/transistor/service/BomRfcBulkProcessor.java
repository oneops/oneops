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
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.ExceptionConsolidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;

public class BomRfcBulkProcessor {
	static final int MIN_COMPUTES_SCALE = 3;
	private static Logger logger = Logger.getLogger(BomRfcBulkProcessor.class);

    private static final Map<String, Integer> priorityMap = new HashMap<>();
    static {
    	//priorityMap.put("Compute", 2);
    	//priorityMap.put("Storage", 2);
    	priorityMap.put("Keypair", 1);
    }

    private static final int priorityMax = 1;

	private static final String CONVERGE_RELATION_ATTRIBUTE = "converge";

    private static final int MAX_RECURSION_DEPTH = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxRecursion", "150"));
    private static final int MAX_NUM_OF_EDGES    = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxEdges", "100000"));

	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor manifestRfcProcessor;
	private CmsRfcProcessor bomRfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private TransUtil trUtil;
	private CmsUtil cmsUtil;
	private Gson gson = new Gson();
	private BomEnvManager envManager;

	//Compares bom cis by the numbers in their ci_name. Essentially sorts them by "edge numbers" like *-1, *-2 etc
	public static Comparator<CmsCI> bomCiComparatorByName = Comparator.comparing(bomCi -> Long.valueOf(bomCi.getCiName().replaceAll("[^0-9]", "")));


	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setManifestRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.manifestRfcProcessor = rfcProcessor;
	}
	public void setBomRfcProcessor(CmsRfcProcessor bomRfcProcessor) {
		this.bomRfcProcessor = bomRfcProcessor;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setEnvManager(BomEnvManager envManager) {
		this.envManager = envManager;
	}

	public int processManifestPlatform(EnvBomGenerationContext ec, PlatformBomGenerationContext pc, CmsCIRelation bindingRel, int startExecOrder, boolean usePercent) {
		long startingTime = System.currentTimeMillis();

		int maxExecOrder = 0;
		CmsCI platformCi = pc.getPlatform();
		String bomNsPath = pc.getBomNsPath();
		CmsCI cloud = bindingRel.getToCi();

		logger.info(bomNsPath + " >>> Start working on cloud - " + cloud.getCiName() + " (" + cloud.getCiId()	+ ")");
		List<CmsCI> components = pc.getComponents();
		if (components.size() > 0) {
			if (startExecOrder <= priorityMax) {
				startExecOrder = priorityMax + 1;
			}

			long nsId = trUtil.verifyAndCreateNS(bomNsPath);

			boolean isPartial = false;
			for (CmsCIRelation rel : pc.getDependsOns()) {
				if (rel.getAttribute("pct_dpmt") != null && !"100".equals(rel.getAttribute("pct_dpmt").getDjValue())){
					isPartial = true;
					break;
				}
			}

			List<CmsCI> cisToValidate = new ArrayList<>(components);
			cisToValidate.addAll(pc.getAttachments());
			cisToValidate.addAll(pc.getMonitors());
			cisToValidate.addAll(pc.getLogs());
			processAndValidateVars(cisToValidate, ec.getCloudVariables(cloud), ec.getGlobalVariables(), pc.getVariables());

			List<BomRfc> bomRfcs = new ArrayList<>();
			Map<String, List<BomRfc>> mfstId2nodeId = new HashMap<>();
			Map<Long, Map<String, List<CmsCIRelation>>> manifestDependsOnRels = new HashMap<>();
			CmsCI startingPoint = components.get(0);
			while (startingPoint != null) {
				BomRfc newBom = bootstrapNewBom(startingPoint, bindingRel.getToCiId(), 1);
				bomRfcs.add(newBom);
				mfstId2nodeId.put(newBom.manifestCiId + "-1", new ArrayList<>(Collections.singletonList(newBom)));
				bomRfcs.addAll(processNode(newBom, bindingRel, mfstId2nodeId, manifestDependsOnRels, 1, usePercent, 1, pc.getDependsOnFromMap(), pc.getDependsOnToMap()));
				startingPoint = getStartingPoint(components, bomRfcs);
			}

			// this is needed to work around ibatis
			// if there is no any updates within current transaction
			// ibatis would not return a new object as query result but instead a ref to the previousely created one
			// if it was modified outside - the changes will not be reset
			for(BomRfc bom : bomRfcs) {
				bom.mfstCi = trUtil.cloneCI(bom.mfstCi);
			}

			List<CmsCI> existingCIs = pc.getBomCIs(bindingRel.getToCiId());
			Map<String, CmsRfcCI> existingRfcCIs = getOpenRfcCis(bomNsPath);
			ExistingRelations existingRels = new ExistingRelations(pc);
			String userId = ec.getUserId();
			long releaseId = ec.getReleaseId();
			long bomCreationStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> Processing components...");
			maxExecOrder = createBomRfcsAndRels(pc, bindingRel, existingCIs, existingRfcCIs, existingRels, bomRfcs, isPartial, nsId, startExecOrder, userId, releaseId);
			Map<Long, List<BomRfc>> bomRfcMap = bomRfcs.stream().collect(Collectors.groupingBy(bom -> bom.manifestCiId));
			logger.info(bomNsPath + " >>> Done with components in " + (System.currentTimeMillis() - bomCreationStartTime) + " ms.");

			long mngviaStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> Processing MANAGED_VIA relations");
			processManagedViaRels(pc, existingCIs, existingRels, bomRfcMap, nsId, userId, releaseId);
			logger.info(bomNsPath + " >>>  Done with MANAGED_VIA relations in " + (System.currentTimeMillis() - mngviaStartTime) + " ms.");

			long secByStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> Processing SECURED_BY relations...");
			processSecuredByRels(components, pc.getSecuredByMap(), bomRfcMap, bomNsPath, nsId, userId, existingRels, releaseId);
			logger.info(bomNsPath + " >>> Done with SECURED_BY relations in " + (System.currentTimeMillis() - secByStartTime) + " ms.");

			long entryPointStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> Processing ENTRYPOINT relations...");
			processEntryPointRel(platformCi, pc.getEntryPoints(), bomRfcMap, bomNsPath, nsId, userId, existingRels, releaseId);
			logger.info(bomNsPath + " >>> Done with ENTRYPOINT relations in " + (System.currentTimeMillis() - entryPointStartTime) + " ms.");

			if (!usePercent || !isPartial) {
				if (maxExecOrder == 0) {
					maxExecOrder++;
				}
				maxExecOrder = findObsolete(pc, existingCIs, bomRfcs, maxExecOrder, userId, releaseId);
			}
		}

		long timeTook = System.currentTimeMillis() - startingTime;
		logger.info(bomNsPath + " >>> Done with " + platformCi.getCiName() + ", cloud - " + cloud.getCiName() + " in " + timeTook + " ms.");
		return maxExecOrder;
	}

	public int deleteManifestPlatform(EnvBomGenerationContext ec, PlatformBomGenerationContext pc, CmsCIRelation bindingRel, int startExecOrder) {
		int maxExecOrder = 0;
		String userId = ec.getUserId();
		CmsCI platformCi = pc.getPlatform();
		List<CmsCI> components = pc.getComponents();
		if (components.size() > 0) {
			String bomNsPath = pc.getBomNsPath();
			if (pc.getBomRelations().size() > 0) {
				maxExecOrder = findObsolete(pc, pc.getBomCIs(bindingRel.getToCiId()), new ArrayList<>(), startExecOrder, userId, ec.getReleaseId());
			} else if (platformCi.getCiState().equalsIgnoreCase("pending_deletion")) {
				for (CmsCI component : components) {
					cmProcessor.deleteCI(component.getCiId(), true, userId);
				}
				cmProcessor.deleteCI(platformCi.getCiId(), true, userId);
				trUtil.deleteNs(bomNsPath);
			}
		}
		return maxExecOrder;
	}

	private CmsCI getStartingPoint(List<CmsCI> components, List<BomRfc> boms) {
		Set<Long> processedNodes = boms.stream().map(bom -> bom.manifestCiId).collect(Collectors.toSet());
		for (CmsCI component : components) {
			if (!processedNodes.contains(component.getCiId())) {
				return component;
			}
		}
		return null;
	}

	void processAndValidateVars(List<CmsCI> cis, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
		ExceptionConsolidator ec = CIValidationException.consolidator(CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF, cmsUtil.getCountOfErrorsToReport());
		for (CmsCI ci : cis) {
			ec.invokeChecked(() -> cmsUtil.processAllVars(ci, cloudVars, globalVars, localVars));
		}
		ec.rethrowExceptionIfNeeded();
	}

	private int findObsolete(PlatformBomGenerationContext pc,
							 List<CmsCI> existingCIs,
							 List<BomRfc> newBoms,
							 int startingExecOrder,
							 String userId,
							 long releaseId) {
		long startTime = System.currentTimeMillis();
		String bomNsPath = pc.getBomNsPath();
		logger.info(bomNsPath + " >>> Processing obsolete boms...");
		int maxExecOrder = startingExecOrder;
		Map<String, BomRfc> bomMap = new HashMap<>();
		for (BomRfc bom : newBoms) {
			bomMap.put(bom.ciName, bom);
		}

		Map<Long, CmsCI> obsoleteCisMap = new HashMap<>();
		for (CmsCI ci : existingCIs) {
			if (!bomMap.containsKey(ci.getCiName())) {
				logger.debug("This ci should be deleted - " + ci.getCiName());
				obsoleteCisMap.put(ci.getCiId(), ci);
			}
		}

		if (obsoleteCisMap.size()>0) {
			logger.info(bomNsPath + " >>> creating delete rfcs and traversing strong relations..." );
			maxExecOrder = processObsolete(pc, existingCIs, newBoms, obsoleteCisMap, startingExecOrder, releaseId, userId);
		}

		logger.info(bomNsPath + " >>> Done with obsolete boms in " + (System.currentTimeMillis() - startTime) + " ms.");
		return maxExecOrder;
	}

	private int processObsolete(PlatformBomGenerationContext pc,
								List<CmsCI> existingCIs,
								List<BomRfc> bomRfcs,
								Map<Long, CmsCI> obsoleteCisMap,
								int startingExecOrder,
								long releaseId,
								String userId) {
		int maxExecOrder = startingExecOrder;

		Set<Long> obsoleteToRelations = new HashSet<>();
		Map<Long, List<CmsCIRelation>> obsoleteFromRelations = new HashMap<>();
		List<CmsCIRelation> dummyUpdateRels = new ArrayList<>();
		Map<Long, List<CmsCIRelation>> depOnToMap = pc.getBomRelations().stream()
				.filter(r -> r.getRelationName().equals(BOM_DEPENDS_ON))
				.collect(Collectors.groupingBy(CmsCIRelation::getToCiId, Collectors.toList()));

		for (Long ciId : obsoleteCisMap.keySet()) {
			if (depOnToMap.containsKey(ciId)) {
				for (CmsCIRelation fromDependsOnCiId : depOnToMap.get(ciId)) {
					if (obsoleteCisMap.containsKey(fromDependsOnCiId.getFromCiId())) {
						obsoleteToRelations.add(ciId);
						if (!obsoleteFromRelations.containsKey(fromDependsOnCiId.getFromCiId())) {
							obsoleteFromRelations.put(fromDependsOnCiId.getFromCiId(), new ArrayList<>());
						}
						obsoleteFromRelations.get(fromDependsOnCiId.getFromCiId()).add(fromDependsOnCiId);
					} else {
						dummyUpdateRels.add(fromDependsOnCiId);
					}
				}
			}
		}

		Map<Long, Integer> execOrder = new HashMap<>();

		for (Long ciId : obsoleteCisMap.keySet()) {
			if (!obsoleteToRelations.contains(ciId)) {
				execOrder.put(ciId, startingExecOrder);
				processObsoleteOrder(ciId, execOrder, obsoleteFromRelations);
			}
		}

		for (Long ciId : execOrder.keySet()) {
			int ciExecOrder = execOrder.get(ciId);
			CmsCI ci = obsoleteCisMap.get(ciId);
			String shortClazzName = trUtil.getShortClazzName(ci.getCiClassName());
			int actualExecOrder = ciExecOrder;
			if (priorityMap.containsKey(shortClazzName)) {
				int priorityOrder = priorityMap.get(shortClazzName);
				actualExecOrder = startingExecOrder + obsoleteCisMap.size() + priorityMax - priorityOrder + 1;
			}
			createDeleteRfc(ci,actualExecOrder, userId, releaseId);
			maxExecOrder = (ciExecOrder > maxExecOrder) ? ciExecOrder : maxExecOrder;
		}
		Map<Long, List<String>> manifestPropagations = new HashMap<>();
		Set<Long> propagations = new HashSet<>();

		//now lets submit submit dummy update
		String bomNsPath = pc.getBomNsPath();
		Map<Long, CmsCIRelation> realizedAsToMap = pc.getBomRelations().stream()
				.filter(r -> r.getRelationName().equals(BASE_REALIZED_AS))
				.collect(Collectors.toMap(CmsCIRelation::getToCiId, Function.identity()));
		Set<Long> dummyUpdates = new HashSet<>();
		if (dummyUpdateRels.size() > 0) {
			List<CmsCIRelation> bomDepOns = pc.getBomRelations().stream()
					.filter(r -> r.getRelationName().equals(BOM_DEPENDS_ON))
					.collect(Collectors.toList());
			for (CmsCIRelation rel : dummyUpdateRels) {
				dummyUpdates.add(rel.getFromCiId());
				for (BomRfc bomRfc : bomRfcs) {
					if (bomRfc.rfc == null) {
						 logger.info("bom.rfc null for " + bomRfc.ciName + " nspath: " + bomNsPath);
					} else if (bomRfc.rfc.getCiId() == rel.getFromCiId()) {
						mapPropagations(bomRfc.manifestCiId, pc.getDependsOnFromMap(), depOnToMap, manifestPropagations);
						propagateUpdate(bomRfc.rfc.getCiId(), bomRfc.manifestCiId, realizedAsToMap, manifestPropagations, bomDepOns, propagations);
					}
				}
			}
		}
		dummyUpdates.addAll(propagations);
		if (dummyUpdates.size() > 0) {
			TreeMap<Integer, List<Long>> dummyUpdateExecOrders = new TreeMap<>();
			//now lets grab the execution orders from the bomRfcs for the CIs to be dummy updated.
			for (BomRfc bom : bomRfcs) {
				if (bom.rfc == null) {
					logger.info("rfc null for: " + bom.ciName);
					continue;
				}
				if (dummyUpdates.contains(bom.rfc.getCiId())) {
					dummyUpdateExecOrders.computeIfAbsent(bom.execOrder, ArrayList::new).add(bom.rfc.getCiId());
				}
			}

			// Now lets iterate over the sorted order map to touch the dummy update CIs with exec order starting from max exec order
			Map<Long, CmsCI> existingCiMap = existingCIs.stream()
					.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));
			for (int order : dummyUpdateExecOrders.keySet()) {
				maxExecOrder++;
				for (long dummyUpdateCiId : dummyUpdateExecOrders.get(order)) {
					createDummyUpdateRfc(existingCiMap.get(dummyUpdateCiId), releaseId, maxExecOrder, "oneops-transistor");
				}
			}
		}

		return maxExecOrder;
	}

	private void createDeleteRfc(CmsCI ci, int execOrder, String userId, long releaseId) {
		CmsRfcCI newRfc = new CmsRfcCI();

		newRfc.setCiId(ci.getCiId());
		newRfc.setCiClassId(ci.getCiClassId());
		newRfc.setCiClassName(ci.getCiClassName());
		newRfc.setCiGoid(ci.getCiGoid());
		newRfc.setCiName(ci.getCiName());
		newRfc.setComments("deleting");
		newRfc.setNsId(ci.getNsId());
		newRfc.setNsPath(ci.getNsPath());
		newRfc.setRfcAction("delete");

		newRfc.setExecOrder(execOrder);
		newRfc.setCreatedBy(userId);
		newRfc.setUpdatedBy(userId);
		newRfc.setReleaseId(releaseId);
		bomRfcProcessor.createRfcCI(newRfc, userId);
	}

	private void processObsoleteOrder(long startingCiId, Map<Long, Integer> execOrder, Map<Long, List<CmsCIRelation>> obsoleteRelations) {
		if (obsoleteRelations.containsKey(startingCiId)) {
			int nextExecOrder = execOrder.get(startingCiId) + 1;
			for (CmsCIRelation rel : obsoleteRelations.get(startingCiId)) {
				long nextCiId = rel.getToCiId();
				if (execOrder.containsKey(nextCiId)) {
					int currentEO = execOrder.get(nextCiId);
					if (nextExecOrder > currentEO) {
						execOrder.put(nextCiId, nextExecOrder);
					}
				} else {
					execOrder.put(nextCiId, nextExecOrder);
				}
				processObsoleteOrder(nextCiId, execOrder, obsoleteRelations);
			}
		}
	}

	private int createBomRfcsAndRels(PlatformBomGenerationContext pc,
									 CmsCIRelation bindingRel,
									 List<CmsCI> existingCIs,
									 Map<String, CmsRfcCI> existingRfcCIs,
									 ExistingRelations existingRels,
									 List<BomRfc> bomRfcs,
									 boolean isPartial,
									 long nsId,
									 int startExecOrder,
									 String userId,
									 Long releaseId) {

		String bomNsPath = pc.getBomNsPath();
		Map<Long, List<CmsCIRelation>> depOnFromMap = pc.getDependsOnFromMap();
		Map<Long, List<CmsCIRelation>> depOnToMap = pc.getDependsOnToMap();
		ForceUpdateCache forceUpdateCache = new ForceUpdateCache();


		Map<String, BomRfc> bomMap = new HashMap<>();
		for (BomRfc bom : bomRfcs) {
			bomMap.put(bom.nodeId, bom);
		}
		// need to verify all the to links for the case when we have converge link
		verifyToLinks(bomMap);
		//lets find out the exec order and populate relations list
		Map<String, BomLink> links = new HashMap<>();
		for (BomRfc bom :bomRfcs) {
			if (bom.fromLinks.size()==0) {
				processOrder(bom, bomMap, startExecOrder, 1);
			} else {
				for (BomLink link : bom.fromLinks) {
					links.put(link.fromNodeId + "@" + link.toNodeId, link);
				}
			}
		}

		int maxExecOrder = bomRfcs.stream().map(b -> b.execOrder).max(Comparator.comparingInt(i -> i)).orElse(0);

		Map<Integer, List<BomRfc>> orderedMap = new HashMap<>();
		for (BomRfc bom : bomRfcs) {
			if (!orderedMap.containsKey(bom.execOrder)) {
				orderedMap.put(bom.execOrder, new ArrayList<>());
			}
			orderedMap.get(bom.execOrder).add(bom);
		}

		Map<String, CmsCI> existingCIsByBomIdMap = existingCIs.stream()
				.collect(Collectors.toMap(ci -> ci.getCiClassName() + ":" + ci.getCiName(), Function.identity()));
		Map<Long, CmsCIRelation> realizedAsToMap = pc.getBomRelations().stream()
				.filter(r -> r.getRelationName().equals(BASE_REALIZED_AS))
				.collect(Collectors.toMap(CmsCIRelation::getToCiId, Function.identity()));
		Set<Long> propagations = new HashSet<>();
		Set<Long> bomCiIds = new HashSet<>();
		Map<Long, List<String>> manifestPropagations = new HashMap<>();
		long timeTakenByPropagation = 0;
		long rfcInsertStartTime = System.currentTimeMillis();
		int realExecOrder = startExecOrder;
		int numberOfRFCs = 0;
		List<CmsRfcCI> replacedComputes = new ArrayList<>();
		List<CmsCIRelation> bomDepOns = pc.getBomRelations().stream()
				.filter(r -> r.getRelationName().equals(BOM_DEPENDS_ON))
				.collect(Collectors.toList());
		for (int i = startExecOrder; i <= maxExecOrder; i++) {
			boolean incOrder = false;
			if (orderedMap.containsKey(i)) {
				for (BomRfc bom : orderedMap.get(i)) {
					String shortClazzName = trUtil.getShortClazzName(bom.mfstCi.getCiClassName());
					String bomId = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()) + ":" + bom.ciName;
					CmsCI existingCi = existingCIsByBomIdMap.get(bomId);
					CmsRfcCI existingRfc = existingRfcCIs.get(bomId);
					boolean rfcCreated;
					if (priorityMap.containsKey(shortClazzName)) {
						bom.execOrder = priorityMap.get(shortClazzName);
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, bomNsPath, bindingRel, releaseId, userId, existingRels, forceUpdateCache);
						if (rfcCreated && realExecOrder == 1) {
							incOrder = true;
						}
					} else {
						//bom.execOrder = realExecOrder;
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, bomNsPath, bindingRel, releaseId, userId, existingRels, forceUpdateCache);
						if (rfcCreated && bom.rfc != null) {
							//if rfc was created, lets check if any propagation is required
							if(bom.rfc.getCiClassName().equals("bom.Compute") && bom.rfc.getRfcAction().equals("replace")) {
								replacedComputes.add(bom.rfc);
							}

							long startTime = System.currentTimeMillis();
							mapPropagations(bom.manifestCiId, depOnFromMap, depOnToMap, manifestPropagations);
							propagateUpdate(bom.rfc.getCiId(), bom.manifestCiId, realizedAsToMap, manifestPropagations, bomDepOns, propagations);
							long endTime = System.currentTimeMillis();
							timeTakenByPropagation = timeTakenByPropagation + (endTime - startTime);
						}
						incOrder = incOrder || rfcCreated;
					}

					if (bom.rfc != null) {
						bomCiIds.add(bom.rfc.getCiId());
					}
					if (rfcCreated) {
						numberOfRFCs++;
						if (numberOfRFCs % 100 == 0) {
							logger.debug(bomNsPath + " >>> Inserted " + numberOfRFCs + " rfcs");
						}
					}
				}
			}
			if (incOrder) realExecOrder++;
		}
		logger.info(bomNsPath + " >>> Inserted " + numberOfRFCs + " RFC CIs in " + (System.currentTimeMillis() - rfcInsertStartTime) + " ms.");

		long rfcRelationInsertStartTime = System.currentTimeMillis();
		//lets create dependsOn Relations
		//TODO question should we propagate rel attrs
		int maxRfcExecOrder = getMaxRfcExecOrder(bomRfcs);

		Map<Long, CmsCI> existingCiMap = existingCIs.stream()
				.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));
		maxExecOrder = (maxRfcExecOrder > 0) ? maxRfcExecOrder : maxExecOrder;
		//execute all dummy updates in one last step
		//maxExecOrder++;
		Set<String> djRelGoids = new HashSet<>();
		boolean increaseMaxOrder = false;
		int numberOfRelRFCs = 0;
		for (BomLink link : links.values()) {
			BomRfc fromNode = bomMap.get(link.fromNodeId);
			BomRfc toNode = bomMap.get(link.toNodeId);
			if (fromNode.rfc != null && toNode.rfc != null) {
				long fromCiId = fromNode.rfc.getCiId();
				long toCiId = toNode.rfc.getCiId();
				String comments = CmsUtil.generateRelComments(fromNode.rfc.getCiName(), fromNode.rfc.getCiClassName(), toNode.rfc.getCiName(), toNode.rfc.getCiClassName());
				CmsRfcRelation dependsOn = bootstrapRfcRelation(fromCiId, toCiId, BOM_DEPENDS_ON, bomNsPath, nsId, userId, comments, existingRels);
				if (fromNode.rfc.getRfcId() > 0) {
					dependsOn.setFromRfcId(fromNode.rfc.getRfcId());
				}
				if (toNode.rfc.getRfcId() > 0) {
					dependsOn.setToRfcId(toNode.rfc.getRfcId());
				}

				createBomRelationRfc(dependsOn, existingRels, releaseId);
				djRelGoids.add(dependsOn.getRelationGoid());
				//if we got new relation lets update create dummy update rfcs
				if (dependsOn.getRfcId() > 0) {
					numberOfRelRFCs++;

					if (fromNode.rfc.getRfcId() == 0) {
						createDummyUpdateRfc(existingCiMap.get(fromCiId), releaseId, fromNode.execOrder, userId);
						long startTime = System.currentTimeMillis();
						long manifestCiId = fromNode.manifestCiId;
						mapPropagations(manifestCiId, depOnFromMap, depOnToMap, manifestPropagations);
						propagateUpdate(fromCiId, manifestCiId, realizedAsToMap, manifestPropagations, bomDepOns, propagations);
						long endTime = System.currentTimeMillis();
						timeTakenByPropagation = timeTakenByPropagation + (endTime - startTime);
						increaseMaxOrder = true;
					}
					if (numberOfRelRFCs % 100 == 0) {
						logger.debug(bomNsPath + " >>> Inserted " + numberOfRelRFCs + " relation rfcs");
					}
				}
			}
		}
		logger.info(bomNsPath + " >>> Inserted " + numberOfRelRFCs + " RFC relations in " + (System.currentTimeMillis() - rfcRelationInsertStartTime) + " ms.");

		//Now create dummy updates for all the dependency-propagations needed
		if (propagations.size() > 0) {
			for (BomRfc bom : bomRfcs) {
				if (bom.rfc == null) {
					logger.info("rfc null for: " + bom.ciName);
					continue;
				}

				if (propagations.contains(bom.rfc.getCiId())) {
					String bomId = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()) + ":" + bom.ciName;
					CmsCI existingCi = existingCIsByBomIdMap.get(bomId);
					CmsRfcCI existingRfc = existingRfcCIs.get(bomId);
					if (existingRfc == null && bom.rfc.getRfcId() == 0) {
						createDummyUpdateRfc(existingCi, releaseId, bom.execOrder, userId, true);
					} else {
						createDummyUpdateRfc(existingCi, releaseId, bom.execOrder, userId);
					}
				}
			}
		}

		//hack for lb/fqdn update on replaced computes
		for (CmsRfcCI rfc : replacedComputes) {
			pc.getBomRelations().stream()
					.filter(r -> {
						String fromClassName = r.getFromCi().getCiClassName();
						return r.getRelationName().equals(BOM_DEPENDS_ON) && r.getToCiId() == rfc.getCiId() && (fromClassName.equals("bom.Lb") || fromClassName.equals("bom.Fqdn"));
					})
					.forEach(r -> createDummyUpdateRfc(existingCiMap.get(r.getFromCiId()), releaseId, rfc.getExecOrder() + 1, rfc.getCreatedBy()));
		}
		
		if (!isPartial) {
			for (CmsCIRelation existingRel : existingRels.getExistingRels(BOM_DEPENDS_ON)) {
				if (!djRelGoids.contains(existingRel.getRelationGoid())
					&& bomCiIds.contains(existingRel.getFromCiId())
					&& bomCiIds.contains(existingRel.getToCiId())) {
					cmRfcMrgProcessor.requestRelationDelete(existingRel.getCiRelationId(), userId);
				}
			}
		}
		if (increaseMaxOrder) maxExecOrder++;

		return maxExecOrder;
	}

	private Map<String, CmsRfcCI> getOpenRfcCis(String nsPath) {
		return bomRfcProcessor.getOpenRfcCIByClazzAndName(nsPath, null, null).stream()
				.collect(Collectors.toMap(ci -> ci.getCiClassName() + ":" + ci.getCiName(), Function.identity()));
	}

	private void propagateUpdate(long bomCiId,
								 long manifestId,
								 Map<Long, CmsCIRelation> realizedAsToMap,
								 Map<Long, List<String>> manifestPropagations,
								 List<CmsCIRelation> bomDepOns,
								 Set<Long> propagations) {
		List<String> targetManifestCiNames = manifestPropagations.get(manifestId);
		if (targetManifestCiNames == null || targetManifestCiNames.isEmpty()) {
			return;
		}
		List<CmsCIRelation> rels = bomDepOns.stream()
											.filter(r -> r.getFromCiId() == bomCiId || r.getToCiId() == bomCiId)
											.collect(Collectors.toList());
		for (String targetCiName : targetManifestCiNames) {
			for (CmsCIRelation rel : rels) {
				long ciId;
				String ciName;
				long toCiId = rel.getToCiId();
				if (toCiId == bomCiId) {
					ciId = rel.getFromCiId();
					ciName = rel.getFromCi().getCiName();
				} else {
					ciId = toCiId;
					ciName = rel.getToCi().getCiName();
				}
				if (ciName != null && ciName.startsWith(targetCiName + "-") && !propagations.contains(ciId)) {
					propagations.add(ciId);
					CmsCIRelation realizedAs = realizedAsToMap.get(ciId);
					if (realizedAs != null) {
						propagateUpdate(ciId, realizedAs.getFromCiId(), realizedAsToMap, manifestPropagations, bomDepOns, propagations);
					}
				}
			}
		}
	}

	private void mapPropagations(long manifestCiId, Map<Long, List<CmsCIRelation>> depOnFromMap, Map<Long, List<CmsCIRelation>> depOnToMap, Map<Long,  List<String>> manifestPropagations) {
		List<String> targetManifests = manifestPropagations.get(manifestCiId);
		if (targetManifests != null) {
			return;   //propagations already calculated for this manifest cid
		}

		targetManifests = new ArrayList<>();
		manifestPropagations.put(manifestCiId, targetManifests);

		List<CmsCIRelation> depOns = depOnFromMap.get(manifestCiId);
		if (depOns != null) {
			for (CmsCIRelation rel : depOns) {
				CmsCIRelationAttribute attrib = rel.getAttribute("propagate_to");
				if (attrib == null) continue;

				String value = attrib.getDfValue();
				if ("to".equalsIgnoreCase(value) || "both".equalsIgnoreCase(value)) {
					targetManifests.add(rel.getToCi().getCiName());
					mapPropagations(rel.getToCiId(), depOnFromMap, depOnToMap, manifestPropagations);
				}
			}
		}

		depOns = depOnToMap.get(manifestCiId);
		if (depOns != null) {
			for (CmsCIRelation rel : depOns) {
				CmsCIRelationAttribute attrib = rel.getAttribute("propagate_to");
				if (attrib == null) continue;

				String value = attrib.getDfValue();
				if ("from".equalsIgnoreCase(value) || "both".equalsIgnoreCase(value)) {
					targetManifests.add(rel.getFromCi().getCiName());
					mapPropagations(rel.getFromCiId(), depOnFromMap, depOnToMap, manifestPropagations);
				}
			}
		}
	}

	private void verifyToLinks(Map<String, BomRfc> bomMap) {
		for (Map.Entry<String, BomRfc> entry : bomMap.entrySet()) {
			for (BomLink link : entry.getValue().fromLinks) {
				BomRfc toBom = bomMap.get(link.toNodeId);
				if (toBom.getExisitngToLinks(entry.getValue().nodeId) == null) {
					BomLink toLink = new BomLink();
					toLink.fromNodeId = entry.getValue().nodeId;
					toLink.fromMfstCiId = entry.getValue().manifestCiId;
					toLink.toNodeId = toBom.nodeId;
					toLink.toMfstCiId = toBom.manifestCiId;
					toBom.toLinks.add(toLink);
				}
			}
		}
	}

	private boolean upsertRfcs(BomRfc bom, CmsCI existingCi, CmsRfcCI existingRfc, long nsId, String nsPath, CmsCIRelation bindingRel, Long releaseId, String userId, ExistingRelations existingRels, ForceUpdateCache forceUpdateCache) {
		boolean rfcCreated = false;
		if (bom.mfstCi.getCiState().equalsIgnoreCase("pending_deletion")) {
			List<CmsRfcCI> cis2delete = cmRfcMrgProcessor.getDfDjCi(nsPath, "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()), bom.ciName, "dj");
			if (cis2delete.size() > 0) {
				for (CmsRfcCI ci2delete : cis2delete) {
					bom.rfc = cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(ci2delete.getCiId(), userId, bom.execOrder);
					rfcCreated = bom.rfc.getRfcId() > 0;
				}
			} else {
				//if no boms lets see if we have some in other cloud
				if (cmProcessor.getRelationCounts(BASE_REALIZED_AS, nsPath, false, bom.mfstCi.getCiId(), null, null, null, null, null).get("count") == 0) {
					cmProcessor.deleteCI(bom.mfstCi.getCiId(), true, userId);
				}
			}
		} else {
			bom.rfc = createBomRfc(bom, existingCi, existingRfc, nsPath, nsId, userId, releaseId);
			rfcCreated = bom.rfc.getRfcId() > 0;
			long lastAppliedRfcId = bom.mfstCi.getLastAppliedRfcId();
			if (!rfcCreated) {
				//lets make sure the manifest object has not changed or we will create dummy update
				CmsCIRelation realizedAsRel = existingRels.getExistingRel(BASE_REALIZED_AS, bom.mfstCi.getCiId(), bom.rfc.getCiId());
				if (realizedAsRel != null) {
					CmsCIRelationAttribute lastManifestRfcAttr = realizedAsRel.getAttribute("last_manifest_rfc");
					if (lastManifestRfcAttr != null) {
						long deployedManifestRfc = Long.valueOf(lastManifestRfcAttr.getDjValue());
						if (lastAppliedRfcId > deployedManifestRfc && forceUpdateCache.shouldForce(bom.manifestCiId, deployedManifestRfc, lastAppliedRfcId)) {
							bom.rfc = createDummyUpdateRfc(existingCi, releaseId, bom.execOrder, userId);
							rfcCreated = true;
						}
					}
				}
			}

			String priority = bindingRel.getAttribute("priority").getDjValue();
			String comments = CmsUtil.generateRelComments(bom.mfstCi.getCiName(), bom.mfstCi.getCiClassName(), bom.rfc.getCiName(), bom.rfc.getCiClassName());
			CmsRfcRelation realizedAs = bootstrapRealizedAsRfcRelation(bom.mfstCi.getCiId(), bom.rfc.getCiId(), nsPath, nsId, userId, comments, priority, lastAppliedRfcId, existingRels);
			if (rfcCreated) {
				realizedAs.setToRfcId(bom.rfc.getRfcId());
			}
			createBomRelationRfc(realizedAs, existingRels, releaseId);

			comments = CmsUtil.generateRelComments(bom.rfc.getCiName(), bom.rfc.getCiClassName(), bindingRel.getToCi().getCiName(), bindingRel.getToCi().getCiClassName());
			CmsRfcRelation deployedTo = bootstrapDeployedToRfcRelation(bom.rfc.getCiId(), bindingRel.getToCiId(), nsPath, nsId, userId, comments, priority, existingRels);
			if (rfcCreated) {
				deployedTo.setFromRfcId(bom.rfc.getRfcId());
			}
			createBomRelationRfc(deployedTo, existingRels, releaseId);
		}

		return rfcCreated;
	}

	private CmsRfcCI createBomRfc(BomRfc bom, CmsCI existingCi, CmsRfcCI existingRfc, String nsPath, long nsId, String userId, Long releaseId) {
		String targetClazzName = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName());
		Map<String, CmsCIAttribute> mfstAttrs = bom.mfstCi.getAttributes().values().stream().filter(a -> a.getDfValue() != null).collect(Collectors.toMap(CmsCIAttribute::getAttributeName, Function.identity()));
		CmsRfcCI rfc = trUtil.bootstrapRfc(bom.ciName, targetClazzName, nsPath, null, mfstAttrs.keySet());
		rfc.setReleaseId(releaseId);
		rfc.setNsId(nsId);
		rfc.setComments(bom.mfstCi.getComments());
		rfc.setExecOrder(bom.execOrder);
		rfc.setCreatedBy(userId);
		rfc.setUpdatedBy(userId);

		for (CmsCIAttribute mfstAttr : mfstAttrs.values()) {
			CmsRfcAttribute rfcAttribute = rfc.getAttribute(mfstAttr.getAttributeName());
			if (rfcAttribute != null) {
				rfcAttribute.setNewValue(mfstAttr.getDfValue());
				rfcAttribute.setComments(mfstAttr.getComments());
			}
		}

		if (existingRfc != null) {
			rfc.setCiId(existingRfc.getCiId());
			rfc.setRfcId(existingRfc.getRfcId());
			rfc.setReleaseId(existingRfc.getReleaseId());
		} else if (existingCi != null) {
			rfc.setCiId(existingCi.getCiId());
			rfc.setCiState(existingCi.getCiState());
		}


		if (rfc.getCiId() == 0) {
			rfc.setRfcAction("add");
		} else {
			// Remove bom-only attributes so they are not compared against their default values and
			// thus cause an unnecessary update RFC on its own without actual changes in manifest attributes.
			rfc.getAttributes().keySet().retainAll(mfstAttrs.keySet());

			if (!needUpdateRfc(rfc, existingCi)) {
				return rfc;
			}
		}

		rfc.setIsActiveInRelease(true);
		if (rfc.getRfcId() == 0) {
			bomRfcProcessor.createRfcRaw(rfc);
		} else {
			bomRfcProcessor.updateRfc(rfc, existingRfc);
		}

		return rfc;
	}


	private boolean needUpdateRfc(CmsRfcCI rfcCi, CmsCI baseCi) {
		boolean needUpdate = false;
		if ("replace".equals(baseCi.getCiState())) {
			rfcCi.setRfcAction("replace");
			needUpdate = true;
		} else {
			rfcCi.setRfcAction("update");
		}
		Set<String> equalAttrs = new HashSet<>(rfcCi.getAttributes().size());
		for (CmsRfcAttribute attr : rfcCi.getAttributes().values()){
			CmsCIAttribute existingAttr = baseCi.getAttribute(attr.getAttributeName());
			String oldValue = existingAttr == null ? null : existingAttr.getDjValue();
			if (Objects.equals(attr.getNewValue(), oldValue)) {
				equalAttrs.add(attr.getAttributeName());
			} else {
				attr.setOldValue(oldValue);
				needUpdate = true;
			}
		}
		if (needUpdate) {
			for (String equalAttrName : equalAttrs) {
				rfcCi.getAttributes().remove(equalAttrName);
			}
		}
		return needUpdate;
	}

	private void createBomRelationRfc(CmsRfcRelation rfc, ExistingRelations existingRels, Long releaseId) {
		if (rfc.getReleaseId()==0 && releaseId!=null){
			rfc.setReleaseId(releaseId);
		}
		if (rfc.getCiRelationId() == 0) {
			//this is add rfc

			rfc.setIsActiveInRelease(true);
			rfc.setRfcAction("add");

			if (rfc.getRfcId() == 0) {
				bomRfcProcessor.createRfcRelationRaw(rfc);
			} else {
				bomRfcProcessor.updateRfcRelation(rfc, existingRels.getRfcRel(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
			}
		} else {
			//need to figure out delta and create update rfc
			CmsCIRelation existingRel = existingRels.getExistingRel(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId());
			if (needUpdateRfcRelation(rfc, existingRel)) {
				rfc.setIsActiveInRelease(true);
				rfc.setRfcAction("update");
				if (rfc.getRfcId() == 0) {
					bomRfcProcessor.createRfcRelationRaw(rfc);
				} else {
					bomRfcProcessor.updateRfcRelation(rfc, existingRels.getRfcRel(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
				}
			}
		}
		if (rfc.getRfcId() > 0) {
			existingRels.add(rfc);
		}
	}

	private boolean needUpdateRfcRelation(CmsRfcRelation rfcRel, CmsCIRelation baseRel) {
		boolean needUpdate = false;
		Set<String> equalAttrs = new HashSet<>(rfcRel.getAttributes().size());
		for (CmsRfcAttribute attr : rfcRel.getAttributes().values()){
			CmsCIRelationAttribute existingAttr = baseRel.getAttribute(attr.getAttributeName());
			if (Objects.equals(attr.getNewValue(), existingAttr.getDjValue())) {
				equalAttrs.add(attr.getAttributeName());
			} else {
				needUpdate = true;
			}
		}

		if (needUpdate) {
			for (String attrName : equalAttrs) {
				rfcRel.getAttributes().remove(attrName);
			}
		}
		return needUpdate;
	}

	private void processOrder(BomRfc bom, Map<String, BomRfc> bomMap, int order, int recursionDepth) {
		if (recursionDepth >= MAX_RECURSION_DEPTH) {
			String err = "Circular dependency detected, (level - " + recursionDepth + "),\n please check the platform diagram for " + extractPlatformNameFromNsPath(bom.mfstCi.getNsPath());
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
		}

		bom.execOrder = (order > bom.execOrder) ? order : bom.execOrder;
		order += 1;
		for (BomLink link : bom.toLinks) {
			BomRfc parentBom = bomMap.get(link.fromNodeId);
			processOrder(parentBom, bomMap, order, recursionDepth + 1);
		}
	}

	private int getMaxRfcExecOrder(List<BomRfc> boms) {
		int maxExecOrder = 0;
		for (BomRfc bom : boms) {
			if (bom.rfc != null && bom.rfc.getRfcId()>0) {
				maxExecOrder = (bom.execOrder > maxExecOrder) ? bom.execOrder : maxExecOrder;
			}
		}
		return maxExecOrder;
	}

	private void processEntryPointRel(CmsCI platform, List<CmsCIRelation> entryPoints, Map<Long, List<BomRfc>> bomsMap, String nsPath, long nsId, String userId, ExistingRelations existingRels, long releaseId) {
		for (CmsCIRelation epRel : entryPoints) {
			if (bomsMap.containsKey(epRel.getToCiId())) {
				for (BomRfc bom : bomsMap.get(epRel.getToCiId())) {
					if (bom.rfc != null) {
						String comments = CmsUtil.generateRelComments(platform.getCiName(), platform.getCiClassName(), bom.rfc.getCiName(), bom.rfc.getCiClassName());
						CmsRfcRelation entryPoint = bootstrapRfcRelation(platform.getCiId(), bom.rfc.getCiId(), BASE_ENTRYPOINT, nsPath, nsId, userId, comments, existingRels);
						if (bom.rfc.getRfcId() > 0) {
							entryPoint.setToRfcId(bom.rfc.getRfcId());
						}
						createBomRelationRfc(entryPoint, existingRels, releaseId);
					}
				}
			}
		}
	}

	private void processManagedViaRels(PlatformBomGenerationContext pc,
									   List<CmsCI> existingCIs,
									   ExistingRelations existingRels,
									   Map<Long, List<BomRfc>> bomsMap,
									   long nsId,
									   String userId,
									   Long releaseId) {
		String bomNsPath = pc.getBomNsPath();
		logger.info(bomNsPath + " >>> Path calc BFS optimization");

		Map<Long, String> toClassNameMap = existingCIs.stream()
				.collect(Collectors.toMap(CmsCI::getCiId, CmsCI::getCiClassName));
		bomsMap.values().stream()
			   .flatMap(List::stream)
			   .filter(b -> b.rfc != null)
			   .forEach(b -> toClassNameMap.put(b.rfc.getCiId(), b.rfc.getCiClassName()));

		Map<String, CmsLink> depOnLinks = existingRels.getExistingRels(BOM_DEPENDS_ON).stream()
			  .filter(r -> toClassNameMap.containsKey(r.getToCiId()))
			  .collect(Collectors.toMap(r -> r.getFromCiId() + ":" + r.getToCiId(),
										r -> new CmsLink(r.getFromCiId(), r.getToCiId(), toClassNameMap.get(r.getToCiId()))));
		existingRels.getRfcRels(BOM_DEPENDS_ON).stream()
				.filter(r -> toClassNameMap.containsKey(r.getToCiId()))
				.forEach(r -> {
					Long toCiId = r.getToCiId();
					String key = r.getFromCiId() + ":" + toCiId;
					String rfcAction = r.getRfcAction();
					if ("add".equals(rfcAction)) {
						depOnLinks.put(key, new CmsLink(r.getFromCiId(), toCiId, toClassNameMap.get(toCiId)));
					} else if ("delete".equals(rfcAction)) {
						depOnLinks.remove(key);
					}
				});

		Map<Long, Map<String, List<Long>>> dependsOnMap = depOnLinks.values().stream()
			.collect(Collectors.groupingBy(CmsLink::getFromCiId,
										   Collectors.groupingBy(CmsLink::getToClazzName,
																 Collector.of(ArrayList::new,
																			  (a, l) -> a.add(l.getToCiId()),
																			  (x, y) -> {
																				  x.addAll(y);
																				  return x;
																			  }))));


		long counter = 0;
		long lengthSum = 0;
		long lengthCounter = 0;
		long leafTime = 0;
		long leafCalls = 0;
		long dpOnPathTime = 0;
		long dpOnPathCalls = 0;

		Map<Long, List<CmsCIRelation>> managedViaMap = pc.getManagedViaMap();
		Set<String> relRfcGoids = new HashSet<>();
		for (CmsCI component : pc.getComponents()) {
			List<CmsCIRelation> componentManagedViaRels = managedViaMap.get(component.getCiId());
			if (componentManagedViaRels == null) continue;

			for (CmsCIRelation manifestRel : componentManagedViaRels) {
				long time = System.currentTimeMillis();
				List<String> pathClasses = getDpOnPathBfs(manifestRel.getFromCiId(), manifestRel.getToCiId(), pc.getDependsOnFromMap());
				dpOnPathTime+=System.currentTimeMillis()-time;
				dpOnPathCalls++;
				lengthCounter++;
				lengthSum+=pathClasses.size();
				if (pathClasses.size()==0) {
					String err = "Can not traverse ManagedVia relation using DependsOn path from ci " + manifestRel.getFromCiId() + ", to ci " + manifestRel.getToCiId() + "\n";
					err += manifestRel.getComments();
					logger.error(err);
					throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
				}

				for (BomRfc bomRfc : bomsMap.get(component.getCiId())) {
					//for this rfc we need to traverse by the DependsOn path down to ManagedVia Ci and create the relation\
					//Now this is tricky since it could get resolved as a tree so we need to use recursion
					LinkedList<String> path = new LinkedList<>();
					path.addAll(pathClasses);
					CmsRfcCI fromRfc = bomRfc.rfc;
					if (fromRfc != null) {
						long startTime = System.currentTimeMillis();
						leafCalls++;
						List<Long> targets = getLeafsByPath(fromRfc.getCiId(), path, dependsOnMap);
						leafTime += (System.currentTimeMillis()-startTime);

						Map<Long, BomRfc> targetMap = new HashMap<>();
						for (BomRfc targetBom :  bomsMap.get(manifestRel.getToCiId())) {
							targetMap.put(targetBom.rfc.getCiId(), targetBom);
						}
						for (long managedViaCiId : targets) {
							CmsCIRelation existingRel = existingRels.getExistingRel(BOM_MANAGED_VIA, fromRfc.getCiId(), managedViaCiId);
							if (existingRel == null && targetMap.containsKey(managedViaCiId)) {
								CmsRfcRelation managedVia = bootstrapRfcRelation(fromRfc.getCiId(), managedViaCiId, BOM_MANAGED_VIA, bomNsPath, nsId, userId, existingRels);
								if (!relRfcGoids.contains(managedVia.getRelationGoid())) {
									CmsRfcCI toRfc = targetMap.get(managedViaCiId).rfc;
									managedVia.setComments(CmsUtil.generateRelComments(fromRfc.getCiName(), fromRfc.getCiClassName(), toRfc.getCiName(), toRfc.getCiClassName()));
									if (fromRfc.getRfcId() > 0) {
										managedVia.setFromRfcId(fromRfc.getRfcId());
									}
									if (toRfc.getRfcId() > 0) {
										managedVia.setToRfcId(toRfc.getRfcId());
									}
									createBomRelationRfc(managedVia, existingRels, releaseId);

									counter++;
									relRfcGoids.add(managedVia.getRelationGoid());
								}
							}
						}
					}
				}
			}
		}
		logger.info(bomNsPath + " >>> dpOnPath time: "+dpOnPathTime+" Calls: "+dpOnPathCalls+" leafsByPath time: " +  leafTime+ " calls: "+ leafCalls +" Relation Counter:"+ counter+" Avg path length:"+(double)lengthSum/lengthCounter);
	}

	private void processSecuredByRels(List<CmsCI> components, Map<Long, List<CmsCIRelation>> securedByMap, Map<Long, List<BomRfc>> bomsMap, String bomNsPath, long nsId, String userId, ExistingRelations existingRels, Long releaseId) {
		if (components.isEmpty()) return;

		for (CmsCI component : components) {
			List<CmsCIRelation> mfstSecuredByRels = securedByMap.get(component.getCiId());
			if (mfstSecuredByRels == null) continue;

			for (CmsCIRelation mfstSecuredByRel : mfstSecuredByRels) {
				for (BomRfc fromBomRfc : bomsMap.get(component.getCiId())) {
					for (BomRfc toBomRfc : bomsMap.get(mfstSecuredByRel.getToCiId())) {
						String comments = CmsUtil.generateRelComments(fromBomRfc.rfc.getCiName(), fromBomRfc.rfc.getCiClassName(), toBomRfc.rfc.getCiName(), toBomRfc.rfc.getCiClassName());
						CmsRfcRelation securedBy = bootstrapRfcRelation(fromBomRfc.rfc.getCiId(), toBomRfc.rfc.getCiId(), BOM_SECURED_BY, bomNsPath, nsId, userId, comments, existingRels);
						if (fromBomRfc.rfc.getRfcId() > 0) {
							securedBy.setFromRfcId(fromBomRfc.rfc.getRfcId());
						}
						if (toBomRfc.rfc.getRfcId() > 0) {
							securedBy.setToRfcId(toBomRfc.rfc.getRfcId());
						}
						createBomRelationRfc(securedBy, existingRels, releaseId);
					}
				}
			}
		}
	}


	private List<Long> getLeafsByPath(long startCiId, LinkedList<String> path, Map<Long, Map<String,List<Long>>> dependsOnMap) {
		List<Long> listOfTargets = new ArrayList<>();
		if (path.size() == 0) {
			//we reached end of the path but seems like there are multiple routes, but at this point we are good
			return listOfTargets;
		}

		String nextMfstClass = path.poll();
		String bomClass = "bom." + trUtil.getLongShortClazzName(nextMfstClass);

		List<Long> targets = new ArrayList<>();
		if (dependsOnMap.containsKey(startCiId)) {
			if (dependsOnMap.get(startCiId).containsKey(bomClass)) {
				targets.addAll(dependsOnMap.get(startCiId).get(bomClass));
			}
		}
		if (path.size() ==0) {
			listOfTargets.addAll(targets);
		} else {
			for (long toCiId : targets) {
				listOfTargets.addAll(getLeafsByPath(toCiId, new LinkedList<>(path), dependsOnMap));
			}
		}
		return listOfTargets;
	}

	private List<String> getDpOnPathBfs(long fromId, long endId, Map<Long, List<CmsCIRelation>> map) { // implement shortest path search (modified BFS)
		Map<Long, String> idToClassNameMap = new HashMap<>();
		Map<Long, Long> parents = new HashMap<>();
		Queue<Long> queue = new LinkedList<>();
		parents.put(fromId, null);
		queue.add(fromId);

		while (!queue.isEmpty()) {
			Long current = queue.poll();

			if (current == endId) {
				List<String> pathClasses = new LinkedList<>();
				do {
					if (idToClassNameMap.containsKey(current)) {
						pathClasses.add(0, idToClassNameMap.get(current));
					}
				} while ((current = parents.get(current)) != null);
				return pathClasses;
			}
			List<CmsCIRelation> dependsOnRelations = map.containsKey(current) ? map.get(current) : new ArrayList<>();
			for (CmsCIRelation dependsOnRelation : dependsOnRelations) {
				CmsCI toCi = dependsOnRelation.getToCi();
				idToClassNameMap.put(toCi.getCiId(), toCi.getCiClassName());
				if (!parents.containsKey(toCi.getCiId())) {
					parents.put(toCi.getCiId(), current);
					queue.add(toCi.getCiId());
				}
			}
		}
		logger.warn("Path wasn't found"); // path wasn't found???
		return new ArrayList<>();
	}

	private List<BomRfc> processNode(BomRfc node, CmsCIRelation binding, Map<String, List<BomRfc>> mfstIdEdge2nodeId, Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels, int edgeNum, boolean usePercent, int recursionDepth, Map<Long, List<CmsCIRelation>> fromMap, Map<Long, List<CmsCIRelation>> toMap){

		if (recursionDepth >= MAX_RECURSION_DEPTH) {
			String err = "Circular dependency detected, (level - " + recursionDepth + "),\n please check the platform diagram for " + extractPlatformNameFromNsPath(node.mfstCi.getNsPath());
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
		}

		if (edgeNum >= MAX_NUM_OF_EDGES) {
			String err = "Max number of edges is reached - " + edgeNum + "\n please check the platform diagram for " + extractPlatformNameFromNsPath(node.mfstCi.getNsPath());
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
		}


		if (logger.isDebugEnabled()){
			logger.debug("working on " + node.ciName + "; recursion depth - " + recursionDepth);
		}

		List<BomRfc> newBoms = new ArrayList<>();

		if (node.isProcessed) {
			return newBoms;
		}

		if (!manifestDependsOnRels.containsKey(node.manifestCiId)) {
			Map<String,List<CmsCIRelation>> rels = new HashMap<>();
			rels.put("from", fromMap.containsKey(node.manifestCiId) ? fromMap.get(node.manifestCiId) : new ArrayList<>());
			rels.put("to", toMap.containsKey(node.manifestCiId) ? toMap.get(node.manifestCiId) : new ArrayList<>());
			manifestDependsOnRels.put(node.manifestCiId, rels);
		}

		List<CmsCIRelation> mfstFromRels = manifestDependsOnRels.get(node.manifestCiId).get("from");
		List<CmsCIRelation> mfstToRels = manifestDependsOnRels.get(node.manifestCiId).get("to");

		for (CmsCIRelation fromRel : mfstFromRels) {
			int current = Integer.valueOf(fromRel.getAttribute("current").getDfValue());
			if (fromRel.getAttribute("flex") != null &&
					Boolean.valueOf(fromRel.getAttribute("flex").getDfValue())
					&& binding.getAttributes().containsKey("pct_scale")
					&& binding.getAttribute("pct_scale") != null) {
				int pctScale = Integer.valueOf(binding.getAttribute("pct_scale").getDjValue());
				current = (int) Math.ceil(current * (pctScale / 100.0));
			}

			int numEdges;
			if (usePercent && fromRel.getAttribute("pct_dpmt") != null) {
				int percent = Integer.valueOf(fromRel.getAttribute("pct_dpmt").getDjValue());
				numEdges = (int) Math.floor(current * (percent / 100.0));
			} else {
				numEdges = current;
			}
			int edgeNumLocal = edgeNum;

			//special case if the relation marked as converge
			if (fromRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE) != null
				&& Boolean.valueOf(fromRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE).getDfValue())) {
				edgeNumLocal = 1;
				numEdges = 1;
			}
			String key = String.valueOf(fromRel.getToCi().getCiId()) + "-" + edgeNumLocal;

			if (!mfstIdEdge2nodeId.containsKey(key) || numEdges > 1) {
				for (int i = node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size() + 1 + ((edgeNumLocal - 1) * numEdges); i <= numEdges + ((edgeNumLocal - 1) * numEdges); i++) {
					int newEdgeNum = (i > edgeNumLocal) ? i : edgeNumLocal;
					BomRfc newBom = bootstrapNewBom(fromRel.getToCi(), binding.getToCiId(), newEdgeNum);
					BomLink link = new BomLink();
					link.fromNodeId = node.nodeId;
					link.fromMfstCiId = node.manifestCiId;
					link.toNodeId = newBom.nodeId;
					link.toMfstCiId = newBom.manifestCiId;
					node.fromLinks.add(link);
					newBom.toLinks.add(link);
					newBoms.add(newBom);

					key = String.valueOf(newBom.manifestCiId) + "-" + newEdgeNum;

					if (!mfstIdEdge2nodeId.containsKey(key)) mfstIdEdge2nodeId.put(key, new ArrayList<>());
					mfstIdEdge2nodeId.get(key).add(newBom);
					newBoms.addAll(processNode(newBom, binding, mfstIdEdge2nodeId, manifestDependsOnRels, newEdgeNum, usePercent, recursionDepth + 1, fromMap, toMap));
				}
			} else {
				for (BomRfc toNode : mfstIdEdge2nodeId.get(key)) {
					if (node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size() == 0 ) {
						BomLink link = new BomLink();
						link.fromNodeId = node.nodeId;
						link.fromMfstCiId = node.manifestCiId;
						link.toNodeId = toNode.nodeId;
						link.toMfstCiId = fromRel.getToCi().getCiId();
						node.fromLinks.add(link);
					}
				}
			}
		}

		for (CmsCIRelation toRel : mfstToRels) {
			int edgeNumLocal = edgeNum;

			//special case if the relation marked as converge
			if (toRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE) != null
				&& Boolean.valueOf(toRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE).getDfValue())) {
				edgeNumLocal = 1;
			}
			String key = String.valueOf(toRel.getFromCi().getCiId()) + "-" + edgeNumLocal;

			if (!mfstIdEdge2nodeId.containsKey(key)) {
				mfstIdEdge2nodeId.put(key, new ArrayList<>());

				if (node.getExisitngToLinks(toRel.getFromCi().getCiId()).size() == 0
						|| ((toRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE) != null
						&& Boolean.valueOf(toRel.getAttribute(CONVERGE_RELATION_ATTRIBUTE).getDfValue()))
						&& node.getExisitngToLinks(toRel.getFromCi().getCiId() + getName(toRel.getFromCi().getCiName(), binding.getToCiId(), edgeNum)) == null)) {
					BomRfc newBom = bootstrapNewBom(toRel.getFromCi(), binding.getToCiId(), edgeNumLocal);
					BomLink link = new BomLink();
					link.toNodeId = node.nodeId;
					link.toMfstCiId = node.manifestCiId;
					link.fromNodeId = newBom.nodeId;
					link.fromMfstCiId = newBom.manifestCiId;
					node.toLinks.add(link);
					newBom.fromLinks.add(link);
					newBoms.add(newBom);
					mfstIdEdge2nodeId.get(String.valueOf(newBom.manifestCiId)+ "-" + edgeNumLocal).add(newBom);
					newBoms.addAll(processNode(newBom, binding, mfstIdEdge2nodeId, manifestDependsOnRels, edgeNumLocal, usePercent, recursionDepth + 1, fromMap, toMap));
				}
			} else {
				for (BomRfc fromNode : mfstIdEdge2nodeId.get(key)) {
					if (node.getExisitngToLinks(toRel.getFromCi().getCiId()).size() == 0 ) {
						BomLink link = new BomLink();
						link.toNodeId = node.nodeId;
						link.toMfstCiId = node.manifestCiId;
						link.fromNodeId = fromNode.nodeId;
						link.fromMfstCiId = toRel.getFromCi().getCiId();
						node.toLinks.add(link);
						fromNode.fromLinks.add(link);
					}
				}
			}
		}
		node.isProcessed = true;
		return newBoms;
	}

	private BomRfc bootstrapNewBom(CmsCI ci, long bindingId, int edgeNum) {
		BomRfc newBom = new BomRfc();
		newBom.manifestCiId = ci.getCiId();
		newBom.mfstCi = ci;
		newBom.ciName = getName(ci.getCiName(), bindingId, edgeNum);
		newBom.nodeId = newBom.manifestCiId + newBom.ciName;
		return newBom;
	}

	private String getName(String base, long bindingId, int edgeNum) {
		return base + "-" +  bindingId + "-" + edgeNum;
	}

	private CmsRfcCI createDummyUpdateRfc(CmsCI ci, Long releaseId, int execOrder, String userId) {
		return createDummyUpdateRfc(ci, releaseId, execOrder, userId, false);
	}

	private CmsRfcCI createDummyUpdateRfc(CmsCI ci, Long releaseId, int execOrder, String userId, boolean hint) {
		CmsRfcCI existingRfc = bomRfcProcessor.getOpenRfcCIByCiIdNoAttrs(ci.getCiId());
		if (existingRfc != null) {
			return existingRfc;
		}

		CmsRfcCI rfcCi = new CmsRfcCI(ci, userId);
		rfcCi.setReleaseId(releaseId);
		rfcCi.setRfcAction("update");
		rfcCi.setExecOrder(execOrder);
		if (hint) {
			rfcCi.setHint(RfcHint.PROPAGATION);
		}

		bomRfcProcessor.createRfcRaw(rfcCi);

		return rfcCi;
	}


	private CmsRfcRelation bootstrapRfcRelation(long fromCiId, long toCiId, String relName, String nsPath, long nsId, String userId, ExistingRelations existingRels) {
		return bootstrapRfcRelation(fromCiId, toCiId, relName, nsPath, nsId, userId, new HashSet<>(), existingRels);
	}

	private CmsRfcRelation bootstrapRfcRelation(long fromCiId, long toCiId, String relName, String nsPath, long nsId, String userId, Set<String> attrs, ExistingRelations existingRels) {
		CmsRfcRelation newRfc = trUtil.bootstrapRelationRfc(fromCiId, toCiId, relName, nsPath, null, attrs);
		newRfc.setNsId(nsId);
		newRfc.setCreatedBy(userId);
		newRfc.setUpdatedBy(userId);
		newRfc.setRelationGoid(String.valueOf(newRfc.getFromCiId()) + '-' + String.valueOf(newRfc.getRelationId()) + '-' +String.valueOf(newRfc.getToCiId()));

		CmsRfcRelation existingRfc = existingRels.getRfcRel(relName, fromCiId, toCiId);
		if (existingRfc != null) {
			newRfc.setCiRelationId(existingRfc.getCiRelationId());
			newRfc.setRfcId(existingRfc.getRfcId());
			newRfc.setReleaseId(existingRfc.getReleaseId());
		} else {
			CmsCIRelation existingRel = existingRels.getExistingRel(relName, fromCiId, toCiId);
			if (existingRel != null){
				newRfc.setCiRelationId(existingRel.getCiRelationId());
			}
		}

		return newRfc;
	}

	private CmsRfcRelation bootstrapRfcRelation(long fromCiId, long toCiId, String relName, String nsPath, long nsId, String userId, String comments, ExistingRelations existingRels) {
		CmsRfcRelation newRfc = bootstrapRfcRelation(fromCiId, toCiId, relName, nsPath, nsId, userId, existingRels);
		newRfc.setComments(comments);
		return newRfc;
	}

	private CmsRfcRelation bootstrapDeployedToRfcRelation(long fromCiId, long toCiId, String nsPath, long nsId, String userId, String comments, String priority, ExistingRelations existingRels) {
		Set<String> attrs = new HashSet<>();
		attrs.add("priority");
		CmsRfcRelation newRfc = bootstrapRfcRelation(fromCiId, toCiId, BASE_DEPLOYED_TO, nsPath, nsId, userId, attrs, existingRels);
		newRfc.setComments(comments);
		newRfc.addOrUpdateAttribute("priority", priority);
		return newRfc;
	}

	private CmsRfcRelation bootstrapRealizedAsRfcRelation(long fromCiId, long toCiId, String nsPath, long nsId, String userId, String comments, String priority, long lastManfestRfcId, ExistingRelations existingRels) {
		Set<String> attrs = new HashSet<>();
		attrs.add("priority");
		attrs.add("last_manifest_rfc");
		CmsRfcRelation newRfc = bootstrapRfcRelation(fromCiId, toCiId, BASE_REALIZED_AS, nsPath, nsId, userId, attrs, existingRels);
		newRfc.setComments(comments);
		newRfc.addOrUpdateAttribute("priority", priority);
		newRfc.addOrUpdateAttribute("last_manifest_rfc", String.valueOf(lastManfestRfcId));
		return newRfc;
	}

	private String extractPlatformNameFromNsPath(String ns) {
		String[] nsParts = ns.split("/");
		return nsParts[nsParts.length - 2] + "(" + nsParts[nsParts.length - 1] + ")";
	}

	private class BomRfc  {
		long manifestCiId;
		CmsCI mfstCi;
		int execOrder=0;
		String ciName;
		String nodeId;
		CmsRfcCI rfc;
		boolean isProcessed = false;

		List<BomLink> fromLinks = new ArrayList<>();
		List<BomLink> toLinks = new ArrayList<>();

		List<BomLink> getExisitngFromLinks(long toMfstCiId) {
			List<BomLink> links = new ArrayList<>();
			for (BomLink link : fromLinks) {
				if (link.toMfstCiId == toMfstCiId) {
					links.add(link);
				}
			}
			return links;
		}

		List<BomLink> getExisitngToLinks(long fromMfstCiId) {
			List<BomLink> links = new ArrayList<>();
			for (BomLink link : toLinks) {
				if (link.fromMfstCiId == fromMfstCiId) {
					links.add(link);
				}
			}
			return links;
		}

		BomLink getExisitngToLinks(String fromNodeId) {
			for (BomLink link : toLinks) {
				if (link.fromNodeId.equals(fromNodeId)) {
					return link;
				}
			}
			return null;
		}
	}

	private class BomLink {
		String fromNodeId;
		long fromMfstCiId;
		long toMfstCiId;
		String toNodeId;
	}

	private class ExistingRelations {
		private Map<String, Map<String, CmsCIRelation>> existing;
		private Map<String, Map<String, CmsRfcRelation>> rfcs;

		ExistingRelations(PlatformBomGenerationContext context) {
			this.existing = context.getBomRelations().stream()
					.collect(Collectors.groupingBy(CmsCIRelation::getRelationName,
												   Collectors.toMap(r -> r.getFromCiId() + ":" + r.getToCiId(),
																	Function.identity())));
			this.rfcs = bomRfcProcessor.getOpenRfcRelationsByNs(context.getBomNsPath()).stream()
					.collect(Collectors.groupingBy(CmsRfcRelation::getRelationName,
												   Collectors.toMap(r -> r.getFromCiId() + ":" + r.getToCiId(),
																	Function.identity())));
		}

		CmsCIRelation getExistingRel(String relName, long fromCiId, long toCiId) {
			return getExistingMap(relName).get(fromCiId + ":" + toCiId);
		}

		Collection<CmsCIRelation> getExistingRels(String relName) {
			return getExistingMap(relName).values();
		}

		void add(CmsRfcRelation relRfc) {
			getRfcMap(relRfc.getRelationName()).put(relRfc.getFromCiId() + ":" + relRfc.getToCiId(), relRfc);
		}

		CmsRfcRelation getRfcRel(String relName, long fromCiId, long toCiId) {
			return getRfcMap(relName).get(fromCiId + ":" + toCiId);
		}

		Collection<CmsRfcRelation> getRfcRels(String relName) {
			return getRfcMap(relName).values();
		}

		private Map<String, CmsCIRelation> getExistingMap(String relName) {
			return existing.computeIfAbsent(relName, k -> new HashMap<>());
		}

		private Map<String, CmsRfcRelation> getRfcMap(String relName) {
			return rfcs.computeIfAbsent(relName, k -> new HashMap<>());
		}
	}

	private class ForceUpdateCache {
		private Map<String, Boolean> cache = new HashMap<>();

		boolean shouldForce(Long ciId, Long afterRfcId, Long toRfcId) {
			String key = ciId + "-" + afterRfcId + "-" + toRfcId;
			Boolean result = cache.get(key);
			if (result == null) {
				result = manifestRfcProcessor.getAppliedRfcCIsAfterRfcIdNoAttrs(ciId, afterRfcId, toRfcId).stream()
						.anyMatch(rfc -> rfc.getHint() != null && !rfc.getHint().isEmpty());
				cache.put(key, result);
			}
			return result;
		}
	}

	public CmsDeployment scaleDown(CmsCI platformCi, CmsCI env, int scaleDownBy, int minComputesInEachCloud,
								   boolean ensureEvenScale, String user) {
		long startTimeMillis = System.currentTimeMillis();
		Map<String, List<CmsCI>> cloudToComputesMap = getComputesWithClouds(platformCi);

		if (ensureEvenScale && ! isEvenScale(cloudToComputesMap, platformCi)) {
			String errorMessage = "scale is not even currently, rejecting scale down for platform: " + platformCi.getCiId();
			logger.info(errorMessage);
			throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, errorMessage);
		}
		if (! hasSufficientComputes(cloudToComputesMap, scaleDownBy, minComputesInEachCloud)) {
			String errorMessage = "1 or more clouds has less than min computes, rejecting scale down for platform "
					+ platformCi.getCiId();
			logger.info(errorMessage);
			throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, errorMessage);
		}
		int maxCloudScale = getMaxCloudScale(platformCi);
		if (maxCloudScale > 100) {
			throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION,
					"Can't scale down because the cloud scale found to be " + maxCloudScale + "%");
		}
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		//TODO: cancel failed deployment
		envManager.discardEnvBom(env.getCiId());
		long releaseId = bomRfcProcessor.createRelease(bomNsPath, null, user);

		List<CmsCIRelation> dependsOnRelations = cmProcessor.getCIRelations(platformCi.getNsPath(),
				"manifest.DependsOn", null, null, null);
		reduceScaleNumber(platformCi, dependsOnRelations, scaleDownBy);

		Map<Long, Integer> deploymentOrder = getScaleDownDeploymentOrder(platformCi);
		Map<Long, Long> bomToManifestMap = getBomToManifestMap(platformCi);

		List<CmsCI> cisInRfcs = new ArrayList<>();
		for (String cloud : cloudToComputesMap.keySet()) {
			List<CmsCI> computes = cloudToComputesMap.get(cloud);
			Collections.sort(computes, bomCiComparatorByName);
			if (computes.size() >= scaleDownBy) {
				List<CmsCI> computesToBeDeleted = computes.subList(computes.size() - scaleDownBy, computes.size());
				if (computesToBeDeleted.size() == 0) {
					throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, "can not scale down, no computes found to be deleted");
				}
				for (CmsCI compute : computesToBeDeleted) {
					List<CmsCI> cisOnCompute = findCisOnCompute(compute.getCiId());

					for (CmsCI ci : cisOnCompute) {
						createDeleteRfc(ci, getExecOrder(ci, deploymentOrder, bomToManifestMap), user, releaseId);
						cisInRfcs.add(ci);
					}
				}

				for (CmsCI compute : computesToBeDeleted) {
					createDeleteRfc(compute, getExecOrder(compute, deploymentOrder, bomToManifestMap), user, releaseId);
					cisInRfcs.add(compute);
				}
			}
		}

		List<Long> processedPropagations = new ArrayList<>();

		Map<Long, List<Long>> propagationMap = mapPropagations(platformCi, dependsOnRelations);

		int maxDeploymentOrder = 0;

		for (Integer order : deploymentOrder.values()) {
			if (order > maxDeploymentOrder) {
				maxDeploymentOrder = order;
			}
		}

		for (CmsCI ci : cisInRfcs) {
			propagateUpdate(ci, maxDeploymentOrder, bomToManifestMap, processedPropagations, propagationMap, releaseId, user);
		}
		CmsDeployment deployment = new CmsDeployment();
		deployment.setNsPath(bomNsPath);
		deployment.setReleaseId(releaseId);
		deployment.setCreatedBy(user);

		long endTimeMillis = System.currentTimeMillis();
		logger.info(platformCi.getCiId() + " : platform id. Time taken to generate the scale down deployment, seconds: "
				+ (endTimeMillis - startTimeMillis)/1000);

		return deployment;
	}

	private int getMaxCloudScale(CmsCI platformCi) {
		int maxScale = 100;
		List<CmsCIRelation> consumesRelations = cmProcessor.getCIRelations(platformCi.getNsPath(),
				"base.Consumes", null, null, "account.Cloud");
		for (CmsCIRelation rel : consumesRelations) {
			int cloudScale = Integer.parseInt(rel.getAttribute("pct_scale").getDfValue());
			if (cloudScale > maxScale) {
				maxScale = cloudScale;
			}
		}
		return maxScale;
	}

	boolean hasSufficientComputes(Map<String, List<CmsCI>> computesWithClouds, int scaleDownBy,
								  int minComputesInEachCloud) {
		if (minComputesInEachCloud < MIN_COMPUTES_SCALE) {
			minComputesInEachCloud = MIN_COMPUTES_SCALE;
		}
		for (String cloud : computesWithClouds.keySet()) {
			List<CmsCI> computes = computesWithClouds.get(cloud);
			if (computes.size() - scaleDownBy < minComputesInEachCloud) {
				return false;
			}
		}
		return true;
	}

	//changes the "current" scale configuration for flex relation
	private void reduceScaleNumber(CmsCI platformCi, List<CmsCIRelation> dependsOnRelations, int scaleDownBy) {
		boolean scaleNumberUpdated = false;
		for (CmsCIRelation rel : dependsOnRelations) {
			CmsCIRelationAttribute flexAttribute = rel.getAttribute("flex");
			if (flexAttribute != null && flexAttribute.getDfValue().equalsIgnoreCase("true")) {
				CmsCIRelationAttribute currentScale = rel.getAttribute("current");
				if (currentScale != null) {
					int currentValue = Integer.valueOf(currentScale.getDfValue());
					if (currentValue - scaleDownBy >= MIN_COMPUTES_SCALE) {
						int newValue = currentValue - scaleDownBy;
						currentScale.setDfValue(newValue + "");
						currentScale.setDjValue(newValue + "");
						cmProcessor.updateRelation(rel);
						scaleNumberUpdated = true;
					} else {
						logger.info("current scale is less than min, can not scale down platform " + platformCi.getCiId());
					}
				}
			}
		}
		if (! scaleNumberUpdated) {
			throw new TransistorException(CmsError.TRANSISTOR_EXCEPTION, "could not change the scale number");
		}
	}

	private void propagateUpdate(CmsCI ci, int maxDeploymentOrder,
								 Map<Long, Long> bomToManifestMap, List<Long> bomCisProcessed,
								 Map<Long, List<Long>> propagationsMap, long releaseId, String user) {
		long bomCiId = ci.getCiId();
		if (bomCisProcessed.contains(bomCiId)) {
			return;
		}
		bomCisProcessed.add(bomCiId);
		//get the manifest id of this bom ci
		long manifestId = bomToManifestMap.get(bomCiId);

		//find the propagations from this manifest id
		List<Long> propagationsDirections = propagationsMap.get(manifestId);
		if (propagationsDirections == null || propagationsDirections.size() == 0) {
			return;
		}

		//find the bom.depends-on of this bom ci
		List<CmsCI> allDependsOnCis = new ArrayList<>();

		List<CmsCIRelation> fromRelations = cmProcessor.getFromCIRelations(bomCiId, BOM_DEPENDS_ON, null);
		for (CmsCIRelation relation : fromRelations) {
			allDependsOnCis.add(relation.getToCi());
		}

		List<CmsCIRelation> toRelations = cmProcessor.getToCIRelations(bomCiId, BOM_DEPENDS_ON, null);
		for (CmsCIRelation relation : toRelations) {
			allDependsOnCis.add(relation.getFromCi());
		}

		//for each of those bom depends-on ci, find its manifest id.
		// If that manifest id is in the propagations list, mark this ci for update
		for (CmsCI dependentCi : allDependsOnCis) {
			long manifestOfDependent = bomToManifestMap.get(dependentCi.getCiId());
			if (propagationsDirections.contains(manifestOfDependent)) {
				createDummyUpdateRfc(dependentCi, releaseId, maxDeploymentOrder + 1, user);
				propagateUpdate(dependentCi, maxDeploymentOrder + 1, bomToManifestMap,
						bomCisProcessed, propagationsMap, releaseId, user);
			}
		}
	}

	private Map<Long,List<Long>> mapPropagations(CmsCI platformCi, List<CmsCIRelation> dependsOnRelations) {
		Map<Long,List<Long>> propagationMap = new HashMap<>();

		for (CmsCIRelation rel : dependsOnRelations) {
			CmsCIRelationAttribute attribute = rel.getAttribute("propagate_to");
			if (attribute != null) {
				String direction = attribute.getDfValue();
				long toCiId = rel.getToCiId();
				long fromCiId = rel.getFromCiId();
				if ("both".equalsIgnoreCase(direction) || "from".equalsIgnoreCase(direction)) {
					List<Long> propagations = propagationMap.get(toCiId);
					if (propagations == null) {
						propagations = new ArrayList<>();
					}
					propagations.add(fromCiId);
					propagationMap.put(toCiId, propagations);
				}

				if ("both".equalsIgnoreCase(direction) || "to".equalsIgnoreCase(direction)) {
					List<Long> propagations = propagationMap.get(fromCiId);
					if (propagations == null) {
						propagations = new ArrayList<>();
					}
					propagations.add(toCiId);
					propagationMap.put(fromCiId, propagations);
				}
			}
		}
		return propagationMap;
	}

	private boolean isEvenScale(Map<String, List<CmsCI>> computesWithClouds, CmsCI platformCi) {
		int currentScale = 0;
		for (String cloud : computesWithClouds.keySet()) {
			List<CmsCI> computes = computesWithClouds.get(cloud);
			if (computes.size() > 0 && currentScale == 0) {
				currentScale = computes.size();
			}
			if (computes != null && computes.size() > 0 && computes.size() != currentScale ) {
				//this cloud has less computes than other clouds
				logger.info("Not scaling down for platform id " + platformCi.getCiId()
						+ " because this cloud has less or more computes than other cloud/s: " + cloud);
				return false;
			}
		}
		return true;
	}

	private Map<String,List<CmsCI>> getComputesWithClouds(CmsCI platformCi) {
		Map<String, List<CmsCI>> computesWithClouds = new HashMap<>();

		List<CmsCIRelation> deployedToRelations = cmProcessor.getCIRelations(platformCi.getNsPath()
						.replace("/manifest/", "/bom/"),
				"base.DeployedTo", null, "Compute", null);
		for (CmsCIRelation rel : deployedToRelations) {
			String cloud = rel.getToCi().getCiName();
			List<CmsCI> cis = new ArrayList<>();
			if (computesWithClouds.get(cloud) != null) {
				cis = computesWithClouds.get(cloud);
			}
			cis.add(rel.getFromCi());
			computesWithClouds.put(cloud, cis);
		}
		return computesWithClouds;
	}

	private int getExecOrder(CmsCI bomCi, Map<Long, Integer> deploymentOrder, Map<Long, Long> bomToManifestMap) {
		long manifestId = bomToManifestMap.get(bomCi.getCiId());

		if (deploymentOrder.get(manifestId) == null) {
			return 1;
		}
		return deploymentOrder.get(manifestId);
	}

	private Map<Long,Long> getBomToManifestMap(CmsCI platformCi) {
		Map<Long, Long> bomToManifestMap = new HashMap<>();
		List<CmsCIRelation> dependsOnRelations = cmProcessor.getCIRelations(platformCi.getNsPath()
						.replace("/manifest/", "/bom/"),
				"base.RealizedAs", null, null, null);
		for (CmsCIRelation rel: dependsOnRelations) {
			bomToManifestMap.put(rel.getToCiId(), rel.getFromCiId());
		}
		return bomToManifestMap;
	}

	private Map<Long,Integer> getScaleDownDeploymentOrder(CmsCI platformCi) {
		Map<Long, Integer> deploymentOrder = new HashMap<>();
		List<CmsCIRelation> dependsOnRelations = cmProcessor.getCIRelations(platformCi.getNsPath(),
				"manifest.DependsOn", null, null, null);
		Set<Long> processedManifestIds = new LinkedHashSet<>();
		for (CmsCIRelation rel : dependsOnRelations) {
			if (rel.getToCi().getCiClassName().endsWith(".Compute")) {
				deploymentOrder.put(rel.getToCiId(), 1);
				deploymentOrder = getDeploymentOrder(deploymentOrder, dependsOnRelations, rel.getToCi(), 1);
				break;
			}
		}
		//return deploymentOrder;
		return reverseOrder(deploymentOrder);
	}

	private Map<Long,Integer> reverseOrder(Map<Long, Integer> deploymentOrder) {
		LinkedHashMap<Long, Integer> reversedMap = deploymentOrder.entrySet().stream()
				.sorted(Map.Entry.<Long, Integer>comparingByValue().reversed())
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
						(e1, e2) -> e1, LinkedHashMap::new));
		Set entries = reversedMap.entrySet();
		if (entries.size() > 0) {
			int newOrder = 1;
			Iterator<Map.Entry> iterator = entries.iterator();
			Map.Entry<Long, Integer> entry = iterator.next();
			Integer oldOrder = entry.getValue();
			entry.setValue(newOrder);
			while (iterator.hasNext()) {
				entry = iterator.next();
				if (entry.getValue() == oldOrder) {
					entry.setValue(newOrder);
					continue;
				}
				oldOrder = entry.getValue();
				newOrder++;
				entry.setValue(newOrder);
			}
		}
		return reversedMap;
	}

	private Map<Long,Integer> getDeploymentOrder(Map<Long, Integer> deploymentOrder,
												 List<CmsCIRelation> dependsOnRelations, CmsCI parentCi, int parentOrder) {
		for (CmsCIRelation rel : dependsOnRelations) {
			if (rel.getToCiId() == parentCi.getCiId()) { //found a dependent
				if (deploymentOrder.get(rel.getFromCiId()) == null ||
						deploymentOrder.get(rel.getFromCiId()) < parentOrder + 1) {
					deploymentOrder.put(rel.getFromCiId(), parentOrder + 1);
				}
				getDeploymentOrder(deploymentOrder, dependsOnRelations, rel.getFromCi(), parentOrder + 1);
			}
		}
		return deploymentOrder;
	}

	private List<CmsCI> findCisOnCompute(long ciId) {
		List<CmsCI> onComputeCis = new ArrayList<>();
		List<CmsCIRelation> managedViaRelations = cmProcessor.getToCIRelations(ciId, BOM_MANAGED_VIA, null);
		for (CmsCIRelation managedViaRelation : managedViaRelations) {
			onComputeCis.add(managedViaRelation.getFromCi());
		}
		return onComputeCis;
	}

	private class CmsLink {
		private long fromCiId;
		private long toCiId;
		private String toClazzName;

		private CmsLink(long fromCiId, long toCiId, String toClazzName) {
			this.fromCiId = fromCiId;
			this.toCiId = toCiId;
			this.toClazzName = toClazzName;
		}

		private long getFromCiId() {
			return fromCiId;
		}
		private long getToCiId() {
			return toCiId;
		}
		private String getToClazzName() {
			return toClazzName;
		}
	}
}
