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
import com.oneops.cms.cm.domain.CmsLink;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.RfcHint;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.exceptions.ExceptionConsolidator;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsClazzAttribute;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;
import com.oneops.cms.md.service.CmsMdProcessor;
import com.oneops.cms.util.CIValidationResult;
import com.oneops.cms.util.CmsDJValidator;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.exceptions.TransistorException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;

public class BomRfcBulkProcessor {
	static Logger logger = Logger.getLogger(BomRfcBulkProcessor.class);

    private static final Map<String, Integer> priorityMap = new HashMap<>();
    static {
    	//priorityMap.put("Compute", 2);
    	//priorityMap.put("Storage", 2);
    	priorityMap.put("Keypair", 1);
    }

    private static final int priorityMax = 1;

    private static final String BOM_CLOUD_RELATION_NAME       = "base.DeployedTo";
    private static final String BOM_REALIZED_RELATION_NAME    = "base.RealizedAs";
    private static final String BOM_DEPENDS_ON_RELATION_NAME  = "bom.DependsOn";
    private static final String BOM_MANAGED_VIA_RELATION_NAME = "bom.ManagedVia";
	private static final String CONVERGE_RELATION_ATTRIBUTE   = "converge";

    private static final int MAX_RECURSION_DEPTH = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxRecursion", "150"));
    private static final int MAX_NUM_OF_EDGES    = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxEdges", "100000"));

	private CmsCmProcessor cmProcessor;
	private CmsMdProcessor mdProcessor;
	private CmsRfcProcessor rfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsDJValidator djValidator;
	private Gson gson = new Gson();
	private TransUtil trUtil;
	private CmsUtil cmsUtil;

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}

	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setDjValidator(CmsDJValidator djValidator) {
		this.djValidator = djValidator;
	}

	public int processManifestPlatform(PlatformManifest pm, CmsCIRelation bindingRel, String envBomNsPath, int startExecOrder, Map<String, String> globalVars, Map<String, String> cloudVars, String userId, boolean usePercent) {
		long startingTime = System.currentTimeMillis();

		if (startExecOrder <= priorityMax) {
			startExecOrder = priorityMax + 1;
		}
		int maxExecOrder = 0;
		CmsCI platformCi = pm.platform;
		String bomNsPath =  envBomNsPath + "/" + platformCi.getCiName() + "/" + platformCi.getAttribute("major_version").getDjValue();

		logger.info(bomNsPath + " >>> Start working on " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName());

		long nsId = trUtil.verifyAndCreateNS(bomNsPath);

		List<CmsCI> components = cmProcessor.getToCIs(platformCi.getCiId(), null, "Requires", null);
		if (components.size() > 0) {
			Long releaseId  = rfcProcessor.getOpenReleaseIdByNs(getReleaseNs(bomNsPath).toString(), null, userId);

			boolean isPartial = false;
			for (CmsCIRelation rel : pm.dependsOns) {
				if (rel.getAttribute("pct_dpmt") != null && !"100".equals(rel.getAttribute("pct_dpmt").getDjValue())){
					isPartial = true;
					break;
				}
			}

			List<CmsCI> cisToValidate = new ArrayList<>(components);
			cisToValidate.addAll(pm.attachments);
			cisToValidate.addAll(pm.monitors);
			cisToValidate.addAll(pm.logs);
			processAndValidateVars(cisToValidate, cloudVars, globalVars, pm.vars);

			List<BomRfc> boms = new ArrayList<>();
			Map<String, List<BomRfc>> mfstId2nodeId = new HashMap<>();
			Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels = new HashMap<>();
			CmsCI startingPoint = components.get(0);
			while (startingPoint != null) {
				BomRfc newBom = bootstrapNewBom(startingPoint, bindingRel.getToCiId(), 1);
				boms.add(newBom);
				mfstId2nodeId.put(String.valueOf(newBom.manifestCiId) + "-" + 1, new ArrayList<>(Collections.singletonList(newBom)));
				long startTime = System.currentTimeMillis();
				boms.addAll(processNode(newBom, bindingRel, mfstId2nodeId, manifestDependsOnRels, 1, usePercent, 1, pm.dependsOnsFromMap, pm.dependsOnsToMap));
				logger.debug("Time to process Nodes:"+ (System.currentTimeMillis()-startTime));
				startingPoint = getStartingPoint(components, boms);
			}
			// this is needed to work around ibatis
			// if there is no any updates within current transaction
			// ibatis would not return a new object as query result but instead a ref to the previousely created one
			// if it was modified outside - the changes will not be reset
			for(BomRfc bom : boms) {
				bom.mfstCi = trUtil.cloneCI(bom.mfstCi);
			}

			ExistingRels existingRels = new ExistingRels(bomNsPath);
			Map<String, CmsCI> existingCIs = getExistingCis(bindingRel.getToCiId(), bomNsPath);
			Map<String, CmsRfcCI> existingRFCs = getOpenRFCs(bomNsPath);

			long bomCreationStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", processing creating rfcs");
			maxExecOrder = createBomRfcsAndRels(boms, bomNsPath, nsId, bindingRel, startExecOrder, isPartial, userId, existingRels, existingCIs, existingRFCs, releaseId, pm.dependsOnsFromMap, pm.dependsOnsToMap);
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", Done with main RFCs and relations, time spent - " + (System.currentTimeMillis() - bomCreationStartTime));

			Map<Long, List<BomRfc>> bomsMap = buildMfstToBomRfcMap(boms);

			long mngviaStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", processing managed via");
			processManagedViaRels(components, bomsMap, bomNsPath, nsId, existingRels, releaseId, pm.dependsOnsFromMap);
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", Done with managed via, time spent - " + (System.currentTimeMillis() - mngviaStartTime));

			long secByStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", processing secured by");
			processSecuredByRels(components, bomsMap, bomNsPath, nsId, userId, existingRels, releaseId);
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", Done with secured by, time spent - " + (System.currentTimeMillis() - secByStartTime));

			long entryPointStartTime = System.currentTimeMillis();
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", processing entry point");
			processEntryPointRel(platformCi.getCiId(), bomsMap, bomNsPath, userId, existingRels);
			logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", Done with entry point, time spent - " + (System.currentTimeMillis() - entryPointStartTime));

			if (!usePercent || !isPartial) {
				if (maxExecOrder == 0) maxExecOrder++;
				long obsoleteStartTime = System.currentTimeMillis();
				logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", finding obsolete boms");
				maxExecOrder = findObsolete(boms, bomNsPath, maxExecOrder, existingCIs, userId, pm.dependsOnsFromMap, pm.dependsOnsToMap);
				logger.info(bomNsPath + " >>> " + platformCi.getCiName() + ", Done with obsolete boms, time spent - " + (System.currentTimeMillis() - obsoleteStartTime));
			}
			if (logger.isDebugEnabled()) {
				for(BomRfc bom : boms) {
					logger.debug(bom.ciName + "::" + bom.execOrder);
				}
			}
			if (rfcProcessor.getRfcCount(releaseId) == 0) {  // clean up redundant release
				logger.info("No release because rfc count is 0. Cleaning up release.");
				rfcProcessor.deleteRelease(releaseId);
			}
		}

		long timeTook = System.currentTimeMillis() - startingTime;
		logger.info(bomNsPath + ">>> Done with " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName() + " in " + timeTook + " ms.");
		return maxExecOrder;
	}

	private void mapDependsOnRelations(String nsPath, List<CmsCIRelation> depOns, Map<Long, List<CmsCIRelation>> depOnFromMap, Map<Long, List<CmsCIRelation>> depOnToMap) {
		depOns.addAll(cmProcessor.getCIRelations(nsPath, null, "DependsOn", null, null));
		for (CmsCIRelation doRel: depOns){
			depOnFromMap.computeIfAbsent(doRel.getFromCiId(), k -> new ArrayList<>());
			depOnFromMap.get(doRel.getFromCiId()).add(doRel);
			depOnToMap.computeIfAbsent(doRel.getToCiId(), k -> new ArrayList<>());
			depOnToMap.get(doRel.getToCiId()).add(doRel);
		}
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

	private int findObsolete(List<BomRfc> newBoms,
							 String nsPath,
							 int startingExecOrder,
							 Map<String, CmsCI> existingCIs,
							 String userId,
							 Map<Long, List<CmsCIRelation>> depOnFromMap,
							 Map<Long, List<CmsCIRelation>> depOnToMap) {
		logger.info(nsPath + " >>> finding cis to delete..." );
		long startTime = System.currentTimeMillis();
		int maxExecOrder = startingExecOrder;
		Map<String, BomRfc> bomMap = new HashMap<>();
		for (BomRfc bom : newBoms) {
			bomMap.put(bom.ciName, bom);
		}

		List<CmsCI> existingCis = new ArrayList<>(existingCIs.values());

		Map<Long, CmsCI> obsoleteCisMap = new HashMap<>();
		for (CmsCI ci : existingCis) {
			if (!bomMap.containsKey(ci.getCiName())) {
				logger.info("This ci should be deleted - " + ci.getCiName());
				obsoleteCisMap.put(ci.getCiId(), ci);
			}
		}

		logger.info(nsPath + " >>> creating delete rfcs and traversing strong relations..." );
		if (obsoleteCisMap.size()>0) {
			maxExecOrder = processObsolete(newBoms, obsoleteCisMap, startingExecOrder, nsPath, userId, depOnFromMap, depOnToMap);
		}

		logger.info(nsPath + " >>> Done creating delete rfcs, time taken:" +  (System.currentTimeMillis() - startTime));
		return maxExecOrder;
	}

	private int processObsolete(List<BomRfc> bomRfcs,
								Map<Long, CmsCI> obsoleteCisMap,
								int startingExecOrder,
								String nsPath,
								String userId,
								Map<Long, List<CmsCIRelation>> depOnFromMap,
								Map<Long, List<CmsCIRelation>> depOnToMap) {

		int maxExecOrder = startingExecOrder;

		Set<Long> obsoleteToRelations = new HashSet<>();
		Map<Long, List<CmsCIRelation>> obsoleteFromRelations = new HashMap<>();
		List<CmsCIRelation> dummyUpdateRels = new ArrayList<>();

		for (Long ciId : obsoleteCisMap.keySet()) {
			if (depOnToMap.containsKey(ciId)) {
				for (CmsCIRelation fromDependsOnCiIdLink : depOnToMap.get(ciId)) {
					if (obsoleteCisMap.containsKey(fromDependsOnCiIdLink.getFromCiId())) {
						obsoleteToRelations.add(ciId);
						if (!obsoleteFromRelations.containsKey(fromDependsOnCiIdLink.getFromCiId())) {
							obsoleteFromRelations.put(fromDependsOnCiIdLink.getFromCiId(), new ArrayList<>());
						}
						obsoleteFromRelations.get(fromDependsOnCiIdLink.getFromCiId()).add(fromDependsOnCiIdLink);
					} else {
						dummyUpdateRels.add(fromDependsOnCiIdLink);
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
			createDeleteRfc(ci,actualExecOrder, userId);
			maxExecOrder = (ciExecOrder > maxExecOrder) ? ciExecOrder : maxExecOrder;
		}
		Map<Long, List<String>> manifestPropagations = new HashMap<>();
		Set<Long> propagations = new HashSet<>();
		long totalPropagationTime = 0;

		//now lets submit submit dummy update
		Set<Long> dummyUpdates = new HashSet<>();
		if (dummyUpdateRels.size() > 0) {
			List<CmsCIRelation> bomDepOns = null;
			for (CmsCIRelation rel : dummyUpdateRels) {
				dummyUpdates.add(rel.getFromCiId());
				for (BomRfc bomRfc : bomRfcs) {
					if (bomRfc.rfc == null) {
						 logger.info("bom.rfc null for " + bomRfc.ciName + " nspath: " + nsPath);
					} else if (bomRfc.rfc.getCiId() == rel.getFromCiId()) {
						long startTime = System.currentTimeMillis();
						mapPropagations(bomRfc.manifestCiId, depOnFromMap, depOnToMap, manifestPropagations);
						if (bomDepOns == null) {
							bomDepOns = cmProcessor.getCIRelationsNoAttrs(nsPath.replace("/manifest/", "/bom/"), BOM_DEPENDS_ON_RELATION_NAME, null);
						}
						propagateUpdate(bomRfc.rfc.getCiId(), bomRfc.manifestCiId, manifestPropagations, bomDepOns, propagations);
						long endTime = System.currentTimeMillis();
						totalPropagationTime += totalPropagationTime + (endTime - startTime);
					}
				}
			}
		}
		dummyUpdates.addAll(propagations);
		maxExecOrder = processDummyUpdates(dummyUpdates, bomRfcs, maxExecOrder);
		logger.info(nsPath + " >>> Total time taken by propagation in seconds: " + totalPropagationTime/1000.0);
		return maxExecOrder;
	}

	private int processDummyUpdates(Set<Long> dummyUpdates,
			List<BomRfc> bomRfcs, int maxExecOrder) {
		if (dummyUpdates.size() > 0) {
			TreeMap<Integer, List<Long>> dummyUpdateExecOrders = new TreeMap<>();
			//now lets grab the execution orders from the bomRfcs for the CIs to be dummy updated.
			for (BomRfc bom : bomRfcs) {
				if (bom.rfc == null) {
					logger.info("rfc null for: " + bom.ciName);
					continue;
				}
				if (dummyUpdates.contains(bom.rfc.getCiId())) {
					List<Long> ciIds = dummyUpdateExecOrders.get(bom.execOrder);
					if (ciIds == null) {
						ciIds = new ArrayList<>();
						dummyUpdateExecOrders.put(bom.execOrder, ciIds);
					}
					ciIds.add(bom.rfc.getCiId());
				}
			}
			// Now lets iterate over the sorted order map to touch the dummy update CIs with exec order starting from max exec order
			for (int order : dummyUpdateExecOrders.keySet()) {
				maxExecOrder++;
				for (long dummyUpdateCiId : dummyUpdateExecOrders.get(order)) {
					cmRfcMrgProcessor.createDummyUpdateRfc(dummyUpdateCiId, null, maxExecOrder, "oneops-transistor");
				}
			}
		}
		return maxExecOrder;
	}

	private void createDeleteRfc(CmsCI ci, int execOrder, String userId) {
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
		rfcProcessor.createRfcCI(newRfc, userId);
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

	private int createBomRfcsAndRels(List<BomRfc> boms,
									 String nsPath,
									 long nsId,
									 CmsCIRelation bindingRel,
									 int startExecOrder,
									 boolean isPartial,
									 String userId,
									 ExistingRels existingRels,
									 Map<String, CmsCI> existingCIs,
									 Map<String, CmsRfcCI> existingRFCs,
									 Long releaseId,
									 Map<Long, List<CmsCIRelation>> depOnFromMap,
									 Map<Long, List<CmsCIRelation>> depOnToMap) {

		Map<String, BomRfc> bomMap = new HashMap<>();
		for (BomRfc bom : boms) {
			bomMap.put(bom.nodeId, bom);
		}
		// need to verify all the to links for the case when we have converge link
		verifyToLinks(bomMap);
		//lets find out the exec order and populate relations list
		Map<String, BomLink> links = new HashMap<>();
		for (BomRfc bom :boms) {
			if (bom.fromLinks.size()==0) {
				processOrder(bom, bomMap, startExecOrder, 1);
			} else {
				for (BomLink link : bom.fromLinks) {
					links.put(link.fromNodeId + "@" + link.toNodeId, link);
				}
			}
		}

		int maxExecOrder = getMaxExecOrder(boms);

		Map<Integer, List<BomRfc>> orderedMap = new HashMap<>();
		for (BomRfc bom : boms) {
			if (!orderedMap.containsKey(bom.execOrder)) {
				orderedMap.put(bom.execOrder, new ArrayList<>());
			}
			orderedMap.get(bom.execOrder).add(bom);
		}
		Set<Long> propagations = new HashSet<>();
		Set<Long> bomCiIds = new HashSet<>();
		Map<Long, List<String>> manifestPropagations = new HashMap<>();
		long timeTakenByPropagation = 0;
		logger.info("Starting insert");
		long rfcInsertStartTime = System.currentTimeMillis();
		//now lets create rfcs
		int realExecOrder = startExecOrder;
		int numberOfRFCs = 0;
		List<CmsRfcCI> replacedComputes = new ArrayList<>();
		long upsert = 0;
		List<CmsCIRelation> bomDepOns = null;
		for (int i=startExecOrder; i<=maxExecOrder; i++) {
			boolean incOrder = false;
			if (orderedMap.containsKey(i)) {
				for (BomRfc bom : orderedMap.get(i)) {
					String shortClazzName = trUtil.getShortClazzName(bom.mfstCi.getCiClassName());
					String bomId = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()) + ":" + bom.ciName;
					CmsCI existingCi = existingCIs.get(bomId);
					CmsRfcCI existingRfc = existingRFCs.get(bomId);
					boolean rfcCreated;
					if (priorityMap.containsKey(shortClazzName)) {
						bom.execOrder = priorityMap.get(shortClazzName);
						upsert-=System.currentTimeMillis();
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, nsPath, bindingRel, releaseId, userId, existingRels);
						upsert+=System.currentTimeMillis();
						if (rfcCreated && realExecOrder == 1) {
							incOrder = true;
						}
					} else {
						//bom.execOrder = realExecOrder;
						upsert-=System.currentTimeMillis();
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, nsPath, bindingRel, releaseId, userId, existingRels);
						upsert+=System.currentTimeMillis();
						if (rfcCreated && bom.rfc != null) {
							//if rfc was created, lets check if any propagation is required
							if(bom.rfc.getCiClassName().equals("bom.Compute") && bom.rfc.getRfcAction().equals("replace")) {
								replacedComputes.add(bom.rfc);
							}

							long startTime = System.currentTimeMillis();
							mapPropagations(bom.manifestCiId, depOnFromMap, depOnToMap, manifestPropagations);
							if (bomDepOns == null) {
								bomDepOns = cmProcessor.getCIRelationsNoAttrs(nsPath.replace("/manifest/", "/bom/"), BOM_DEPENDS_ON_RELATION_NAME, null);
							}
							propagateUpdate(bom.rfc.getCiId(), bom.manifestCiId, manifestPropagations, bomDepOns, propagations);
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
							logger.debug(">>> Inserted " + numberOfRFCs + " rfcs;");
						}
					}
				}
			}
			if (incOrder) realExecOrder++;
		}
		logger.info(">>> Done with inserting RFC CIs in " + (System.currentTimeMillis() - rfcInsertStartTime) + "ms,  working on relations...");

		long rfcRelationInsertStartTime = System.currentTimeMillis();
		//lets create dependsOn Relations
		//TODO question should we propagate rel attrs
		int maxRfcExecOrder = getMaxRfcExecOrder(boms);

		maxExecOrder = (maxRfcExecOrder > 0) ? maxRfcExecOrder : maxExecOrder;
		//execute all dummy updates in one last step
		//maxExecOrder++;
		//List<CmsRfcRelation> existingDependsOnRels = cmRfcMrgProcessor.getDfDjRelations("bom.DependsOn", null, nsPath, null, null, null);
		Set<String> djRelGoids = new HashSet<>();
		boolean increaseMaxOrder = false;
		int numberOfRelRFCs = 0;
		for (BomLink link : links.values()) {
			if (bomMap.get(link.fromNodeId).rfc != null &&
					bomMap.get(link.toNodeId).rfc != null) {
				long fromCiId = bomMap.get(link.fromNodeId).rfc.getCiId();
				long toCiId = bomMap.get(link.toNodeId).rfc.getCiId();
				CmsRfcRelation dependsOn = bootstrapRelationRfc(fromCiId,toCiId,BOM_DEPENDS_ON_RELATION_NAME, nsPath, existingRels);
				dependsOn.setComments(CmsUtil.generateRelComments(bomMap.get(link.fromNodeId).rfc.getCiName(),
						bomMap.get(link.fromNodeId).rfc.getCiClassName(),
						bomMap.get(link.toNodeId).rfc.getCiName(),
						bomMap.get(link.toNodeId).rfc.getCiClassName()));

				dependsOn.setCreatedBy(userId);
				dependsOn.setUpdatedBy(userId);
				dependsOn.setNsId(nsId);
				if (bomMap.get(link.fromNodeId).rfc.getRfcId() > 0) {
					dependsOn.setFromRfcId(bomMap.get(link.fromNodeId).rfc.getRfcId());
				}
				if (bomMap.get(link.toNodeId).rfc.getRfcId() >0) {
					dependsOn.setToRfcId(bomMap.get(link.toNodeId).rfc.getRfcId());
				}
				//since the DependsOn validation happened on Manifest level already we will skip validation here for perf reasons
				//dependsOn.setValidated(true);

				//CmsRfcRelation newRel =	cmRfcMrgProcessor.upsertRfcRelationNoCheck(dependsOn, userId, "dj");
				createBomRelationRfc(dependsOn, existingRels, releaseId);
				djRelGoids.add(dependsOn.getRelationGoid());
				//if we got new relation lets update create dummy update rfcs
				if (dependsOn.getRfcId()>0) {
					numberOfRelRFCs++;
					existingRels.addRelRfc(dependsOn);

					if (bomMap.get(link.fromNodeId).rfc.getRfcId()==0) {
						cmRfcMrgProcessor.createDummyUpdateRfc(fromCiId, null, bomMap.get(link.fromNodeId).execOrder, userId);
						long startTime = System.currentTimeMillis();
						long manifestCiId = bomMap.get(link.fromNodeId).manifestCiId;
						mapPropagations(manifestCiId, depOnFromMap, depOnToMap, manifestPropagations);
						if (bomDepOns == null) {
							bomDepOns = cmProcessor.getCIRelationsNoAttrs(nsPath.replace("/manifest/", "/bom/"), BOM_DEPENDS_ON_RELATION_NAME, null);
						}
						propagateUpdate(fromCiId, manifestCiId, manifestPropagations, bomDepOns, propagations);
						long endTime = System.currentTimeMillis();
						timeTakenByPropagation = timeTakenByPropagation + (endTime - startTime);
						increaseMaxOrder = true;
					}
					if (numberOfRelRFCs % 100 == 0) {
						logger.debug(">>> Inserted " + numberOfRelRFCs + " relation rfcs;");
					}
				}
			}
		}
		logger.info(">>> Done with inserting RFC relations in " + (System.currentTimeMillis() - rfcRelationInsertStartTime) + "ms.");

		//Now create dummy updates for all the dependency-propagations needed
		if (propagations.size() > 0) {
				for (BomRfc bom : boms) {
					if (bom.rfc == null) {
						logger.info("rfc null for: " + bom.ciName);
						continue;
					}

					if (propagations.contains(bom.rfc.getCiId())) {
						String bomId = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()) + ":" + bom.ciName;
						CmsCI existingCi = existingCIs.get(bomId);
						CmsRfcCI existingRfc = existingRFCs.get(bomId);
						if (existingRfc == null && bom.rfc.getRfcId() == 0) {
							logger.info("creating dummy update rfc with hint for " + existingCi.getCiId());
							cmRfcMrgProcessor.createDummyUpdateRfcWithHint(existingCi.getCiId(), getPropagateUpdateHint(),
									null, bom.execOrder, userId);
						}
						else {
							long ciId = existingRfc != null ? existingRfc.getCiId() : existingCi.getCiId();
							cmRfcMrgProcessor.createDummyUpdateRfc(ciId, null, bom.execOrder, userId);
						}
					}
				}
		}
		//hack for lb/fqdn update on replaced computes
		propagate4ComputeReplace(replacedComputes);

		if (!isPartial) {
			for (CmsCIRelation existingRel : existingRels.getExistingRel(BOM_DEPENDS_ON_RELATION_NAME)) {
				if (!djRelGoids.contains(existingRel.getRelationGoid())
					&& bomCiIds.contains(existingRel.getFromCiId())
					&& bomCiIds.contains(existingRel.getToCiId())) {
					cmRfcMrgProcessor.requestRelationDelete(existingRel.getCiRelationId(), userId);
				}
			}
		}
		if (increaseMaxOrder) maxExecOrder++;

		logger.info(nsPath + " >>> Total time taken by propagation in ms: " + timeTakenByPropagation);
		return maxExecOrder;
	}

	private String getPropagateUpdateHint() {
		RfcHint hint = new RfcHint();
		hint.setPropagation("true");
		return gson.toJson(hint);
	}

    private StringBuilder getReleaseNs(String nsPath) {
        String[] nsParts = nsPath.split("/");
        StringBuilder releaseNs = new StringBuilder();
        for (int i = 1; i < nsParts.length; i++) {
            if (nsParts[i].equals("_design")) break;
            releaseNs.append("/").append(nsParts[i]);
            if (nsParts[i].equals("bom")) break;
            if (nsParts[i].equals("manifest")) break;
        }
        return releaseNs;
    }

    private void propagate4ComputeReplace(List<CmsRfcCI> bomCompRfcs) {
		for (CmsRfcCI rfc : bomCompRfcs) {
			for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), BOM_DEPENDS_ON_RELATION_NAME, null, "bom.Lb")) {
				cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
			}
			for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), BOM_DEPENDS_ON_RELATION_NAME, null, "bom.Fqdn")) {
				cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
			}
		}

	}

	private Map<String, CmsCI> getExistingCis(long cloudId, String nsPath) {
		List<CmsCIRelation> bomRels = cmProcessor.getToCIRelationsByNsNoAttrs(cloudId, BOM_CLOUD_RELATION_NAME, null, null, nsPath);
		Map<String, CmsCI> bomCIs = new HashMap<>();
		for (CmsCIRelation rel : bomRels) {
			CmsCI bomCi = rel.getFromCi();
			String key =bomCi.getCiClassName() + ":" + bomCi.getCiName();
			bomCIs.put(key, bomCi);
		}
		return bomCIs;
	}

	private Map<String, Map<String, CmsCIRelation>> getExistingRelations(String nsPath) {
		List<CmsCIRelation> bomRels = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, null, null, null, null);
		Map<String, Map<String, CmsCIRelation>> bomRelsMap = new HashMap<>();
		for (CmsCIRelation rel : bomRels) {
			if (!bomRelsMap.containsKey(rel.getRelationName())) {
				bomRelsMap.put(rel.getRelationName(), new HashMap<>());
			}
			bomRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
		}
		return bomRelsMap;
	}


	private Map<String, CmsRfcCI> getOpenRFCs(String nsPath) {
		List<CmsRfcCI> existingRfcs = rfcProcessor.getOpenRfcCIByClazzAndName(nsPath, null, null);
		Map<String, CmsRfcCI> rfcs = new HashMap<>();
		for (CmsRfcCI rfc : existingRfcs) {
			String key = rfc.getCiClassName() + ":" + rfc.getCiName();
			rfcs.put(key,rfc);
		}
		return rfcs;
	}


	private Map<String, Map<String,CmsRfcRelation>> getOpenRelationsRfcs(String nsPath) {

		List<CmsRfcRelation> bomRels = rfcProcessor.getOpenRfcRelationsByNs(nsPath);
		Map<String, Map<String,CmsRfcRelation>> bomRelsMap = new HashMap<>();
		for (CmsRfcRelation rel : bomRels) {
			if (!bomRelsMap.containsKey(rel.getRelationName())) {
				bomRelsMap.put(rel.getRelationName(), new HashMap<>());
			}
			bomRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
		}
		return bomRelsMap;
	}


	private void propagateUpdate(long bomCiId, long manifestId, Map<Long, List<String>> manifestPropagations, List<CmsCIRelation> bomDepOns, Set<Long> propagations) {
		List<String> targetManifestCiNames = manifestPropagations.get(manifestId);
		if (targetManifestCiNames == null || targetManifestCiNames.isEmpty()) {
			return;
		}

		List<CmsCIRelation> rels  = bomDepOns.stream().filter(r -> r.getFromCiId() == bomCiId || r.getToCiId() == bomCiId).collect(Collectors.toList());
		for (String targetCiName : targetManifestCiNames) {
			for (CmsCIRelation rel : rels) {
				if (rel.getFromCi() != null) {
					String ciName = rel.getFromCi().getCiName();
					if (ciName != null && ciName.startsWith(targetCiName + "-")) {
						if (propagations.contains(rel.getFromCiId())) {
							continue;
						}
						logger.info("propagating update from bom cid : " + bomCiId + " to " + rel.getFromCiId());
						propagations.add(rel.getFromCiId());
						List<CmsCIRelation> realizedAs = cmProcessor.getToCIRelations(rel.getFromCiId(),
								BOM_REALIZED_RELATION_NAME, rel.getFromCi().getCiClassName().replaceFirst("bom", "manifest"));
						if (realizedAs != null) {
							propagateUpdate(rel.getFromCiId(), realizedAs.get(0).getFromCiId(), manifestPropagations, bomDepOns, propagations);
						}
					}
				} else if (rel.getToCi() != null) {
					String ciName = rel.getToCi().getCiName();
					if (ciName != null && ciName.startsWith(targetCiName + "-")) {
						if (propagations.contains(rel.getToCiId())) {
							continue;
						}
						logger.info("propagating update from bom cid : " + bomCiId + " to " + rel.getToCiId());
						propagations.add(rel.getToCiId());
						List<CmsCIRelation> realizedAs = cmProcessor.getToCIRelations(rel.getToCiId(),
								BOM_REALIZED_RELATION_NAME, rel.getToCi().getCiClassName().replaceFirst("bom", "manifest"));
						if (realizedAs != null) {
							propagateUpdate(rel.getToCiId(), realizedAs.get(0).getFromCiId(), manifestPropagations, bomDepOns, propagations);
						}
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
					BomLink tolink = new BomLink();
					tolink.fromNodeId = entry.getValue().nodeId;
					tolink.fromMfstCiId = entry.getValue().manifestCiId;
					tolink.toNodeId = toBom.nodeId;
					tolink.toMfstCiId = toBom.manifestCiId;
					toBom.toLinks.add(tolink);
				}
			}
		}
	}

	private boolean upsertRfcs(BomRfc bom, CmsCI existingCi, CmsRfcCI existingRfc, long nsId, String nsPath, CmsCIRelation bindingRel, Long releaseId, String userId, ExistingRels existingRels) {

		boolean rfcCreated = false;
		if (bom.mfstCi.getCiState().equalsIgnoreCase("pending_deletion")) {
			List<CmsRfcCI> cis2delete = cmRfcMrgProcessor.getDfDjCi(nsPath, "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()), bom.ciName, "dj");
			if (cis2delete.size() > 0) {
				for (CmsRfcCI ci2delete : cis2delete) {
					//bom.rfc = cmRfcMrgProcessor.requestCiDelete(ci2delete.getCiId(), userId, bom.execOrder);
					bom.rfc = cmRfcMrgProcessor.requestCiDeleteCascadeNoRelsRfcs(ci2delete.getCiId(), userId, bom.execOrder);
					rfcCreated = bom.rfc.getRfcId() > 0;
				}
			} else {
				//if no boms lets see if we have some in other cloud
				if (cmProcessor.getCountFromCIRelationsByNS(bom.mfstCi.getCiId(),  BOM_REALIZED_RELATION_NAME, null, null, nsPath, false) == 0) {
					cmProcessor.deleteCI(bom.mfstCi.getCiId(), true, userId);
				}
			}
		} else {
			CmsRfcCI rfc = bootstrapRfc(bom, existingRfc, existingCi, nsPath);
			rfc.setCreatedBy(userId);
			rfc.setUpdatedBy(userId);
			rfc.setNsId(nsId);
			//bom.rfc = cmRfcMrgProcessor.upsertRfcCINoChecks(rfc, userId, "dj");
			createBomRfc(rfc,existingCi, existingRfc, releaseId);
			bom.rfc = rfc;
			rfcCreated = bom.rfc.getRfcId() > 0;


			if (bom.rfc.getRfcId() == 0) {
				//lets make sure the manifest object has not changed or we will create dummy update
				CmsCIRelation realizedAsRel = existingRels.getExistingRel(BOM_REALIZED_RELATION_NAME, bom.mfstCi.getCiId(), bom.rfc.getCiId());
				//cmProcessor.getFromToCIRelations(bom.mfstCi.getCiId(), "base.RealizedAs", bom.rfc.getCiId());
				if (realizedAsRel != null && realizedAsRel.getAttribute("last_manifest_rfc") != null) {
					long deployedManifestRfc = Long.valueOf(realizedAsRel.getAttribute("last_manifest_rfc").getDjValue());
					if (bom.mfstCi.getLastAppliedRfcId() > deployedManifestRfc) {
						//TODO convert to direct insert
						bom.rfc = cmRfcMrgProcessor.createDummyUpdateRfc(bom.rfc.getCiId(), null, bom.execOrder, userId);
						rfcCreated = true;
					}
				}
			}

			//lets create RealizedAs relation
			Map<String,String> attrs = new HashMap<>();
			attrs.put("last_manifest_rfc", String.valueOf(bom.mfstCi.getLastAppliedRfcId()));
			CmsRfcRelation realizedAs = bootstrapRelationRfcWithAttributes(bom.mfstCi.getCiId(), bom.rfc.getCiId(), BOM_REALIZED_RELATION_NAME, nsPath, attrs, existingRels);
			if (rfcCreated) {
				realizedAs.setToRfcId(bom.rfc.getRfcId());
			}
			realizedAs.setComments(CmsUtil.generateRelComments(bom.mfstCi.getCiName(), bom.mfstCi.getCiClassName(), bom.rfc.getCiName(), bom.rfc.getCiClassName()));
			realizedAs.getAttribute("priority").setNewValue(bindingRel.getAttribute("priority").getDjValue());
			realizedAs.setCreatedBy(userId);
			realizedAs.setUpdatedBy(userId);
			realizedAs.setNsId(nsId);
			//validateRelRfc(realizedAs, bom.mfstCi.getCiClassId(), bom.rfc.getCiClassId());
			//realizedAs.setValidated(true);
			createBomRelationRfc(realizedAs, existingRels, releaseId);
			//cmRfcMrgProcessor.upsertRfcRelationNoCheck(realizedAs, userId, "dj");

			//lest create relation to the binding
			CmsRfcRelation deployedTo = bootstrapRelationRfc(bom.rfc.getCiId(), bindingRel.getToCiId(), BOM_CLOUD_RELATION_NAME, nsPath, existingRels);
			deployedTo.setComments(CmsUtil.generateRelComments(bom.rfc.getCiName(), bom.rfc.getCiClassName(), bindingRel.getToCi().getCiName(), bindingRel.getToCi().getCiClassName()));
			deployedTo.getAttribute("priority").setNewValue(bindingRel.getAttribute("priority").getDjValue());
			deployedTo.setCreatedBy(userId);
			deployedTo.setUpdatedBy(userId);
			deployedTo.setNsId(nsId);
			//validateRelRfc(deployedTo, bom.rfc.getCiClassId(), bindingRel.getToCi().getCiClassId());
			//deployedTo.setValidated(true);
			if (rfcCreated) {
				deployedTo.setFromRfcId(bom.rfc.getRfcId());
			}
			createBomRelationRfc(deployedTo, existingRels, releaseId);
			//cmRfcMrgProcessor.upsertRfcRelationNoCheck(deployedTo, userId, "dj");
		}

		return rfcCreated;
	}

	private void createBomRfc(CmsRfcCI rfc, CmsCI existingCi, CmsRfcCI existingRfc, Long releaseId) {
		if (rfc.getReleaseId()==0 && releaseId!=null){
			rfc.setReleaseId(releaseId);
		}
		if (rfc.getCiId() == 0) {
			//this is add rfc


			rfc.setIsActiveInRelease(true);
			rfc.setRfcAction("add");

			if (rfc.getRfcId() == 0) {
				rfcProcessor.createRfc(rfc);
			} else {
				rfcProcessor.updateRfc(rfc, existingRfc);
			}
		} else {
			//need to figure out delta and create update rfc
			if (needUpdateRfc(rfc, existingCi)) {

				rfc.setIsActiveInRelease(true);
				if (rfc.getRfcId() == 0) {
					rfcProcessor.createRfc(rfc);
				} else {
					rfcProcessor.updateRfc(rfc, existingRfc);
				}
				/*
				if(rfc.getCiClassName().equals("bom.Compute")
						&& rfc.getRfcAction().equals("replace")) {
					for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Lb")) {
						cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
					}
					for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Fqdn")) {
						cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
					}
				}
				*/
			}
		}
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
			if (djValidator.equalStrs(attr.getNewValue(), existingAttr.getDjValue())) {
				equalAttrs.add(attr.getAttributeName());
			} else {
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

	private void createBomRelationRfc(CmsRfcRelation rfc, ExistingRels existingRels, Long releaseId) {
		if (rfc.getReleaseId()==0 && releaseId!=null){
			rfc.setReleaseId(releaseId);
		}
		if (rfc.getCiRelationId() == 0) {
			//this is add rfc

			rfc.setIsActiveInRelease(true);
			rfc.setRfcAction("add");

			if (rfc.getRfcId() == 0) {
				rfcProcessor.createRfcRelationRaw(rfc);
			} else {
				rfcProcessor.updateRfcRelation(rfc, existingRels.getOpenRelRfc(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
			}
		} else {
			//need to figure out delta and create update rfc
			CmsCIRelation existingRel = existingRels.getExistingRel(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId());
			if (needUpdateRfcRelation(rfc, existingRel)) {
				rfc.setIsActiveInRelease(true);
				rfc.setRfcAction("update");
				if (rfc.getRfcId() == 0) {
					rfcProcessor.createRfcRelationRaw(rfc);
				} else {
					rfcProcessor.updateRfcRelation(rfc, existingRels.getOpenRelRfc(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
				}
			}
		}
	}

	private boolean needUpdateRfcRelation(CmsRfcRelation rfcRel, CmsCIRelation baseRel) {

		boolean needUpdate = false;
		Set<String> equalAttrs = new HashSet<>(rfcRel.getAttributes().size());
		for (CmsRfcAttribute attr : rfcRel.getAttributes().values()){
			CmsCIRelationAttribute existingAttr = baseRel.getAttribute(attr.getAttributeName());
			if (djValidator.equalStrs(attr.getNewValue(), existingAttr.getDjValue())) {
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


	private void validateRelRfc(CmsRfcRelation rfcRelation, int fromClassId, int toClassId) {
		CIValidationResult validation = djValidator.validateRfcRelation(rfcRelation, fromClassId, toClassId);

		if (!validation.isValidated()) {
			logger.error(validation.getErrorMsg());
			throw new DJException(CmsError.DJ_VALIDATION_ERROR, validation.getErrorMsg());
		}
		rfcRelation.setValidated(true);
	}

	public int deleteManifestPlatform(CmsCI platformCi, CmsCIRelation bindingRel, String nsPath, int startExecOrder, String userId){
		int maxExecOrder = 0;

		List<CmsCIRelation> mfstPlatComponents = cmProcessor.getFromCIRelationsNakedNoAttrs(platformCi.getCiId(), null, "Requires", null);
		if (mfstPlatComponents.size() > 0) {

			String platNsPath = nsPath + "/" + platformCi.getCiName();
			if (!platformCi.getCiClassName().equals("manifest.Iaas")) {
				platNsPath += "/" + platformCi.getAttribute("major_version").getDjValue();
			}

			long numOfBoms = cmProcessor.getCountBy3(platNsPath, null, null, false);
			if (numOfBoms >0) {
				logger.info(nsPath + ">>>" + platformCi.getCiName() + ", finding obsolete boms");
				Map<String, CmsCI> existingCIs = getExistingCis(bindingRel.getToCiId(), platNsPath);

				List<CmsCIRelation> depOns = new ArrayList<>();
				Map<Long, List<CmsCIRelation>> depOnFromMap = new HashMap<>();
				Map<Long, List<CmsCIRelation>> depOnToMap = new HashMap<>();
				mapDependsOnRelations(platNsPath, depOns, depOnFromMap, depOnToMap);
				maxExecOrder = findObsolete(new ArrayList<>(), platNsPath, startExecOrder, existingCIs, userId, depOnFromMap, depOnToMap);
			} else {
				// there is no boms lets cleanup any open rfcs if any
				List<CmsRfcRelation> deployedTorfcRels = rfcProcessor.getOpenToRfcRelationByTargetClazzNoAttrs(bindingRel.getToCiId(), BOM_CLOUD_RELATION_NAME, null, null);
				for (CmsRfcRelation deployedToRel : deployedTorfcRels) {
					List<CmsRfcRelation> rfcRels = rfcProcessor.getOpenRfcRelationBy2(deployedToRel.getFromCiId(), null, null, null);
					rfcRels.addAll(rfcProcessor.getOpenRfcRelationBy2(null, deployedToRel.getFromCiId(), null, null));
					for (CmsRfcRelation rfcRel : rfcRels) {
						rfcProcessor.rmRfcRelationFromRelease(rfcRel.getRfcId());
					}
					rfcProcessor.rmRfcCiFromRelease(deployedToRel.getFromRfcId());
				}
			}
			if (platformCi.getCiState().equalsIgnoreCase("pending_deletion") && numOfBoms==0) {
				//if no bom exists - delete the manifest platform for real
				for (CmsCIRelation mfstPlatComponentRel : mfstPlatComponents) {
					cmProcessor.deleteCI(mfstPlatComponentRel.getToCiId(), true, userId);
				}
				cmProcessor.deleteCI(platformCi.getCiId(), true, userId);
				trUtil.deleteNs(platNsPath);
			}

		}
		return maxExecOrder;
	}

	private CmsRfcCI bootstrapRfc(BomRfc bom, CmsRfcCI existingRfc, CmsCI existingBomCi, String nsPath) {

		CmsRfcCI newRfc = new CmsRfcCI();
		newRfc.setNsPath(nsPath);

		String targetClazzName = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName());
		CmsClazz targetClazz = mdProcessor.getClazz(targetClazzName);


		newRfc.setCiClassId(targetClazz.getClassId());
		newRfc.setCiClassName(targetClazz.getClassName());

		//bootstrap the default values from Class definition and populate map for checks
		Map<String, CmsClazzAttribute> clazzAttrs = new HashMap<>();
	    for (CmsClazzAttribute clAttr : targetClazz.getMdAttributes()) {
	    	if (clAttr.getDefaultValue() != null) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(clAttr.getAttributeId());
	    		rfcAttr.setAttributeName(clAttr.getAttributeName());
	    		rfcAttr.setNewValue(clAttr.getDefaultValue());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    	clazzAttrs.put(clAttr.getAttributeName(), clAttr);
	    }

	    //populate values from manifest obj if it's not null
	    applyCiToRfc(newRfc, bom, clazzAttrs);
	    newRfc.setExecOrder(bom.execOrder);
	    setCiId(newRfc, existingRfc, existingBomCi);
		return newRfc;
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

	private int getMaxExecOrder(List<BomRfc> boms) {
		int maxExecOrder = 0;
		for (BomRfc bom : boms) {
			maxExecOrder = (bom.execOrder > maxExecOrder) ? bom.execOrder : maxExecOrder;
		}
		return maxExecOrder;
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

	private void processEntryPointRel(long platformCiId, Map<Long, List<BomRfc>> bomsMap, String nsPath, String user, ExistingRels existingRels) {
		List<CmsCIRelation> entryPoints = cmProcessor.getFromCIRelationsNakedNoAttrs(platformCiId, null, "Entrypoint", null);
		for (CmsCIRelation epRel : entryPoints) {
			if (bomsMap.containsKey(epRel.getToCiId())) {
				for (BomRfc bom : bomsMap.get(epRel.getToCiId())) {
					if (bom.rfc != null) {
						CmsRfcRelation entryPoint = bootstrapRelationRfc(platformCiId, bom.rfc.getCiId(), "base.Entrypoint", nsPath, existingRels);
						cmRfcMrgProcessor.upsertRelationRfc(entryPoint, user, "dj");
					}
				}
			}
		}
	}

	private void processManagedViaRels(List<CmsCI> components, Map<Long, List<BomRfc>> bomsMap, String nsPath, long nsId, ExistingRels existingRels, Long releaseId, Map<Long, List<CmsCIRelation>> depOnFromMap) {
		logger.info(nsPath + " >>> Path calc BFS optimization");

		List<CmsLink> dependsOnlinks = cmRfcMrgProcessor.getLinks(nsPath, BOM_DEPENDS_ON_RELATION_NAME);
		//convert to depOnMap for traversing the path
		Map<Long, Map<String,List<Long>>> dependsOnMap = new HashMap<>();
		for (CmsLink link : dependsOnlinks) {
			if (!dependsOnMap.containsKey(link.getFromCiId())) {
				dependsOnMap.put(link.getFromCiId(), new HashMap<>());
			}
			if (!dependsOnMap.get(link.getFromCiId()).containsKey(link.getToClazzName())) {
				dependsOnMap.get(link.getFromCiId()).put(link.getToClazzName(), new ArrayList<>());
			}
			dependsOnMap.get(link.getFromCiId()).get(link.getToClazzName()).add(link.getToCiId());
		}
		long counter = 0;
		long lengthSum = 0;
		long lengthCounter = 0;
		long leafTime = 0;
		long leafCalls = 0;
		long dpOnPathTime = 0;
		long dpOnPathCalls = 0;

		Set<String> relRfcGoids = new HashSet<>();
		Map<Long, List<CmsCIRelation>> managedViaMap = null;
		for (CmsCI component : components) {
			if (managedViaMap == null) {
				managedViaMap = cmProcessor.getCIRelationsNakedNoAttrs(component.getNsPath(), null, "ManagedVia", null, null).stream()
						.collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));
			}
			List<CmsCIRelation> mfstMngViaRels = managedViaMap.containsKey(component.getCiId()) ? managedViaMap.get(component.getCiId()) : new ArrayList<>();

			for (CmsCIRelation mfstMngViaRel : mfstMngViaRels) {
				// lets find the path
				//List<String> pathClasses = getTraversalPath(mfstMngViaRel);
				long time = System.currentTimeMillis();
				List<String> pathClasses = getDpOnPathBfs(mfstMngViaRel.getFromCiId(), mfstMngViaRel.getToCiId(), depOnFromMap);
				dpOnPathTime+=System.currentTimeMillis()-time;
				dpOnPathCalls++;
				lengthCounter++;
				lengthSum+=pathClasses.size();
				if (pathClasses.size()==0) {
					String err = "Can not traverse ManagedVia relation using DependsOn path from ci " + mfstMngViaRel.getFromCiId() + ", to ci " + mfstMngViaRel.getToCiId() + "\n";
					err += mfstMngViaRel.getComments();
					logger.error(err);
					throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
				}

				for (BomRfc bomRfc : bomsMap.get(component.getCiId())) {
					//for this rfc we need to traverse by the DependsOn path down to ManagedVia Ci and create the relation\
					//Now this is tricky since it could get resolved as a tree so we need to use recursion
					LinkedList<String> path = new LinkedList<>();
					path.addAll(pathClasses);
					if (bomRfc.rfc != null) {
						long startTime = System.currentTimeMillis();
						leafCalls++;
						List<Long> targets = getLeafsByPath(bomRfc.rfc.getCiId(), path, dependsOnMap);
						leafTime += (System.currentTimeMillis()-startTime);

						Map<Long, BomRfc> targetMap = new HashMap<>();
						for (BomRfc targetBom :  bomsMap.get(mfstMngViaRel.getToCiId())) {
							targetMap.put(targetBom.rfc.getCiId(), targetBom);
						}
						for (long managedViaCiId : targets) {
							CmsCIRelation existingRel = existingRels.getExistingRel(BOM_MANAGED_VIA_RELATION_NAME, bomRfc.rfc.getCiId(), managedViaCiId);
									//cmProcessor.getFromToCIRelationsNaked(bomRfc.rfc.getCiId(), "bom.ManagedVia", managedViaCiId);
							if (existingRel == null) {
								CmsRfcRelation managedVia = bootstrapRelationRfc(bomRfc.rfc.getCiId(), managedViaCiId, BOM_MANAGED_VIA_RELATION_NAME, nsPath, existingRels);
								managedVia.setNsId(nsId);
								managedVia.setReleaseId(bomRfc.rfc.getReleaseId());
								if (!relRfcGoids.contains(managedVia.getRelationGoid())) {
									if (targetMap.containsKey(managedViaCiId)) {
										CmsRfcCI toCiRfc = targetMap.get(managedViaCiId).rfc;
										managedVia.setComments(CmsUtil.generateRelComments(bomRfc.rfc.getCiName(), bomRfc.rfc.getCiClassName(), toCiRfc.getCiName(), toCiRfc.getCiClassName()));

										if (bomRfc.rfc != null && bomRfc.rfc.getRfcId() > 0) {
											managedVia.setFromRfcId(bomRfc.rfc.getRfcId());
										}

										if (toCiRfc.getRfcId() > 0) {
											managedVia.setToRfcId(toCiRfc.getRfcId());
										}

										//managedVia.setValidated(true);
										createBomRelationRfc(managedVia,existingRels,releaseId);
										counter++;
										relRfcGoids.add(managedVia.getRelationGoid());
										//cmRfcMrgProcessor.upsertRfcRelationNoCheck(managedVia, user, "dj");
									}
								}
							}
						}
					}
					//}
				}
			}
		}
		logger.info(nsPath + " >>> dpOnPath time: "+dpOnPathTime+" Calls: "+dpOnPathCalls+" leafsByPath time: " +  leafTime+ " calls: "+ leafCalls +" Relation Counter:"+ counter+" Avg path length:"+(double)lengthSum/lengthCounter);
	}

	private void processSecuredByRels(List<CmsCI> components, Map<Long, List<BomRfc>> bomsMap, String nsPath, long nsId,  String user, ExistingRels existingRels, Long releaseId) {
		if (components.isEmpty()) return;

		Map<Long, List<CmsCIRelation>> securedByMap = cmProcessor.getCIRelationsNakedNoAttrs(nsPath, null, "SecuredBy", null, null).stream()
				.collect(Collectors.groupingBy(CmsCIRelation::getFromCiId));
		for (CmsCI component : components) {
			List<CmsCIRelation> mfstSecuredByRels = securedByMap.get(component.getCiId());
			if (mfstSecuredByRels == null) continue;

			for (CmsCIRelation mfstSecuredByRel : mfstSecuredByRels) {
				for (BomRfc fromBomRfc : bomsMap.get(component.getCiId())) {
					for (BomRfc toBomRfc : bomsMap.get(mfstSecuredByRel.getToCiId())) {
						CmsRfcRelation securedBy = bootstrapRelationRfc(fromBomRfc.rfc.getCiId(), toBomRfc.rfc.getCiId(), "bom.SecuredBy", nsPath, existingRels);

						securedBy.setComments(CmsUtil.generateRelComments(fromBomRfc.rfc.getCiName(), fromBomRfc.rfc.getCiClassName(), toBomRfc.rfc.getCiName(), toBomRfc.rfc.getCiClassName()));
						securedBy.setCreatedBy(user);
						securedBy.setUpdatedBy(user);
						securedBy.setNsId(nsId);
						validateRelRfc(securedBy, fromBomRfc.rfc.getCiClassId(), toBomRfc.rfc.getCiClassId());
						if (fromBomRfc.rfc.getRfcId() > 0) {
							securedBy.setFromRfcId(fromBomRfc.rfc.getRfcId());
						}
						if (toBomRfc.rfc.getRfcId() > 0 ) {
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

		//List<CmsRfcRelation> dependsOnRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(startCiId, null, "DependsOn", bomClass);
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

	private Map<Long, List<BomRfc>> buildMfstToBomRfcMap(List<BomRfc> boms) {
		Map<Long, List<BomRfc>> map = new HashMap<>();
		for (BomRfc bom : boms) {
			if (!map.containsKey(bom.manifestCiId)) {
				map.put(bom.manifestCiId, new ArrayList<>());
			}
			map.get(bom.manifestCiId).add(bom);
		}
		return map;
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

			if (!mfstIdEdge2nodeId.containsKey(key)
				|| 	numEdges > 1) {
				//for (int i=node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size()+1; i<=numEdges; i++) {
				for (int i=node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size() + 1 + ((edgeNumLocal-1) * numEdges); i<=numEdges + ((edgeNumLocal-1) * numEdges); i++) {
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

					key = String.valueOf(newBom.manifestCiId)+ "-" + newEdgeNum;

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
								&& node.getExisitngToLinks(toRel.getFromCi().getCiId()
								+ getName(toRel.getFromCi().getCiName(), binding.getToCiId(), edgeNum)) == null)) {
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

	private void applyCiToRfc(CmsRfcCI newRfc, BomRfc bom, Map<String, CmsClazzAttribute> mdAttrs) {
    	newRfc.setCiName(bom.ciName);
    	newRfc.setComments(bom.mfstCi.getComments());

    	for (CmsCIAttribute mfstAttr : bom.mfstCi.getAttributes().values()) {
    		if (mdAttrs.containsKey(mfstAttr.getAttributeName())) {
    			if (mfstAttr.getDfValue() != null) {
		    		if (newRfc.getAttribute(mfstAttr.getAttributeName()) != null) {
		    			newRfc.getAttribute(mfstAttr.getAttributeName()).setNewValue(mfstAttr.getDfValue());
		    			newRfc.getAttribute(mfstAttr.getAttributeName()).setComments(mfstAttr.getComments());
		    		} else {
			    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
			    		rfcAttr.setAttributeId(mdAttrs.get(mfstAttr.getAttributeName()).getAttributeId());
			    		rfcAttr.setAttributeName(mfstAttr.getAttributeName());
			    		rfcAttr.setNewValue(mfstAttr.getDfValue());
			    		newRfc.addAttribute(rfcAttr);
		    		}
		    	}
    		}
	    }
	}

	private void setCiId(CmsRfcCI rfc, CmsRfcCI existingRfc, CmsCI existingBomCi) {
		if (existingRfc != null) {
			rfc.setCiId(existingRfc.getCiId());
			rfc.setRfcId(existingRfc.getRfcId());
			rfc.setReleaseId(existingRfc.getReleaseId());
		} else if (existingBomCi != null) {
			rfc.setCiId(existingBomCi.getCiId());
			rfc.setCiState(existingBomCi.getCiState());
		}
	}

	private CmsRfcRelation bootstrapRelationRfc(long fromCiId, long toCiId, String relName, String nsPath, ExistingRels existingRels) {
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);

		CmsRelation targetRelation = mdProcessor.getRelation(relName);

		newRfc.setRelationId(targetRelation.getRelationId());
		newRfc.setRelationName(targetRelation.getRelationName());

		//bootstrap the default values from Class definition
	    for (CmsRelationAttribute relAttr : targetRelation.getMdAttributes()) {
	    	if (relAttr.getDefaultValue() != null) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(relAttr.getAttributeId());
	    		rfcAttr.setAttributeName(relAttr.getAttributeName());
	    		rfcAttr.setNewValue(relAttr.getDefaultValue());
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }

	    newRfc.setFromCiId(fromCiId);
	    newRfc.setToCiId(toCiId);
	    newRfc.setRelationGoid(String.valueOf(newRfc.getFromCiId()) + '-' + String.valueOf(newRfc.getRelationId()) + '-' +String.valueOf(newRfc.getToCiId()));
	    setCiRelationId(newRfc, existingRels.getOpenRelRfc(relName, fromCiId, toCiId), existingRels.getExistingRel(relName, fromCiId, toCiId));
		return newRfc;
	}

	private CmsRfcRelation bootstrapRelationRfcWithAttributes(long fromCiId, long toCiId, String relName, String nsPath, Map<String,String> attrs, ExistingRels existingRels) {
		CmsRfcRelation newRfc = new CmsRfcRelation();
		newRfc.setNsPath(nsPath);

		CmsRelation targetRelation = mdProcessor.getRelation(relName);

		newRfc.setRelationId(targetRelation.getRelationId());
		newRfc.setRelationName(targetRelation.getRelationName());

		//bootstrap the default values from Class definition
	    for (CmsRelationAttribute relAttr : targetRelation.getMdAttributes()) {
	    	if (relAttr.getDefaultValue() != null || attrs.containsKey(relAttr.getAttributeName())) {
	    		CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
	    		rfcAttr.setAttributeId(relAttr.getAttributeId());
	    		rfcAttr.setAttributeName(relAttr.getAttributeName());
	    		if (attrs.containsKey(relAttr.getAttributeName())) {
	    			rfcAttr.setNewValue(attrs.get(relAttr.getAttributeName()));
	    		} else if (relAttr.getDefaultValue() != null){
	    			rfcAttr.setNewValue(relAttr.getDefaultValue());
	    		}
	    		newRfc.addAttribute(rfcAttr);
	    	}
	    }

	    newRfc.setFromCiId(fromCiId);
	    newRfc.setToCiId(toCiId);
	    setCiRelationId(newRfc, existingRels.getOpenRelRfc(relName, fromCiId, toCiId), existingRels.getExistingRel(relName, fromCiId, toCiId));
		return newRfc;
	}


	private void setCiRelationId(CmsRfcRelation rfc, CmsRfcRelation existingRfc, CmsCIRelation existingRel) {
		if (existingRfc != null) {
			rfc.setCiRelationId(existingRfc.getCiRelationId());
			rfc.setRfcId(existingRfc.getRfcId());
			rfc.setReleaseId(existingRfc.getReleaseId());
		} else if (existingRel != null){
			rfc.setCiRelationId(existingRel.getCiRelationId());
		}
	}

	private String extractPlatformNameFromNsPath(String ns) {
		String[] nsParts = ns.split("/");
		return nsParts[nsParts.length-2] + "(" + nsParts[nsParts.length-1] + ")";
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

	private class ExistingRels {
		private Map<String, Map<String,CmsCIRelation>> existingRels;
		private Map<String, Map<String,CmsRfcRelation>> openRelRfcs;

		ExistingRels(String nsPath) {
			this.existingRels = getExistingRelations(nsPath);
			this.openRelRfcs = getOpenRelationsRfcs(nsPath);
		}

		CmsCIRelation getExistingRel(String relName, long fromCiId, long toCiId) {
			if (existingRels.containsKey(relName)) {
				return existingRels.get(relName).get(fromCiId + ":" + toCiId);
			}
			return null;
		}

		Collection<CmsCIRelation> getExistingRel(String relName) {
			if (existingRels.containsKey(relName)) {
				return existingRels.get(relName).values();
			}
			return new ArrayList<>(0);
		}
		
		void addRelRfc(CmsRfcRelation relRfc) {
			String localKey = relRfc.getFromCiId() + ":" + relRfc.getToCiId();
			if (!openRelRfcs.containsKey(relRfc.getRelationName())) {
				openRelRfcs.put(relRfc.getRelationName(), new HashMap<>());
			}
			openRelRfcs.get(relRfc.getRelationName()).put(localKey, relRfc);
		}

		CmsRfcRelation getOpenRelRfc(String relName, long fromCiId, long toCiId) {
			if (openRelRfcs.containsKey(relName)) {
				return openRelRfcs.get(relName).get(fromCiId + ":" + toCiId);
			}
			return null;
		}
	}
}
