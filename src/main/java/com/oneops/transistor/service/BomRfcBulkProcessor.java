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
import com.oneops.cms.cm.domain.*;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
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
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.log4j.Logger;

import java.util.*;

public class BomRfcBulkProcessor {
	static Logger logger = Logger.getLogger(BomRfcBulkProcessor.class);
	
    private static final Map<String, Integer> priorityMap = new HashMap<String, Integer>();
    static {
    	//priorityMap.put("Compute", 2);
    	//priorityMap.put("Storage", 2);
    	priorityMap.put("Keypair", 1);
    }
    
    private static final int priorityMax = 1;
    
    private static final String BOM_CLOUD_RELATION_NAME = "base.DeployedTo";
    private static final String BOM_REALIZED_RELATION_NAME = "base.RealizedAs";
    private static final String BOM_DEPENDS_ON_RELATION_NAME = "bom.DependsOn";
    private static final String BOM_MANAGED_VIA_RELATION_NAME = "bom.ManagedVia";
    private static final boolean ENABLE_BFS_OPTIMIZATION = Boolean.valueOf(System.getProperty("com.oneops.transistor.bfsOptimization", "true"));
    private static final int MAX_RECUSION_DEPTH = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxRecursion", "150"));
    private static final int MAX_NUM_OF_EDGES = Integer.valueOf(System.getProperty("com.oneops.transistor.MaxEdges", "100000"));
    private static final String CONVERGE_RELATION_ATTRIBUTE = "converge";
    private static final String DISABLE_BFS_VAR_NAME= "DISABLE_BFS";
    
	private CmsCmProcessor cmProcessor;
	private CmsMdProcessor mdProcessor;
	private CmsRfcProcessor rfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private CmsDJValidator djValidator;
	private Gson gson = new Gson();
	//private CmsNsManager nsManager;
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
	
	public int processManifestPlatform(CmsCI platformCi, CmsCIRelation bindingRel, String nsPath, int startExecOrder, Map<String,String> globalVars, Map<String,String> cloudVars,String userId, boolean usePercent){
		return processManifestPlatform(platformCi, bindingRel, nsPath, startExecOrder, globalVars, cloudVars, userId, false, usePercent);
	}
	
	public int processManifestPlatform(CmsCI platformCi, CmsCIRelation bindingRel, String nsPath, int startExecOrder, Map<String,String> globalVars, Map<String,String> cloudVars, String userId, boolean createPlatNs, boolean usePercent){
		
		if (startExecOrder <= priorityMax) startExecOrder = priorityMax+1;
		
		long startingTime = System.currentTimeMillis();
		int maxExecOrder = 0;
		if (createPlatNs) {
			if (platformCi.getCiClassName().equals("manifest.Iaas")) {
				nsPath = nsPath + "/" + platformCi.getCiName();
			} else {
				nsPath = nsPath + "/" + platformCi.getCiName() + "/" + platformCi.getAttribute("major_version").getDjValue();
			}
			trUtil.verifyAndCreateNS(nsPath);
		}

		logger.info(nsPath + " >>> Start working on " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName());
		Map<String,String> localVars = cmsUtil.getLocalVars(platformCi);
		
		List<CmsCIRelation> mfstPlatComponents = cmProcessor.getFromCIRelations(platformCi.getCiId(), null, "Requires", null);
		if (mfstPlatComponents.size() > 0) {
		
			String manifestNs = mfstPlatComponents.get(0).getNsPath();
			boolean isPartial = isPartialDeployment(manifestNs);
			
			List<BomRfc> boms = new ArrayList<BomRfc>();
			Map<String, List<BomRfc>> mfstId2nodeId = new HashMap<String,List<BomRfc>>();
			
			CmsCI startingPoint = mfstPlatComponents.get(0).getToCi(); 
			Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels = new HashMap<Long,Map<String,List<CmsCIRelation>>>();
			
			while (startingPoint != null) {
				BomRfc newBom = bootstrapNewBom(startingPoint, bindingRel.getToCiId(), 1);
				boms.add(newBom);	
				mfstId2nodeId.put(String.valueOf(newBom.manifestCiId) + "-" + 1, new ArrayList<BomRfc>(Arrays.asList(newBom)));
				
				boms.addAll(processNode(newBom, bindingRel, mfstId2nodeId, manifestDependsOnRels, 1, usePercent, 1));
				startingPoint = getStartingPoint(mfstPlatComponents, boms);
			}
			// this is needed to work around ibatis 
			// if there is no any updates within current transaction 
			// ibatis would not return a new object as query result but instead a ref to the previousely created one
			// if it was modified outside - the changes will not be reset
			for(BomRfc bom : boms) {
				bom.mfstCi = trUtil.cloneCI(bom.mfstCi);
			}	
			//process vars
			processVars(boms, cloudVars, globalVars, localVars);
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", starting creating rfcs");
			long bomCreationStartTime = System.currentTimeMillis();
			Long releaseId = null;
			
			ExistingRels existingRels = new ExistingRels(nsPath);
			Map<String, CmsCI> existingCIs = getExistingCis(bindingRel.getToCiId(), nsPath);
			Map<String, CmsRfcCI> existingRFCs = getOpenRFCs(nsPath);

			maxExecOrder = createBomRfcsAndRels(boms, nsPath, bindingRel, startExecOrder, isPartial, userId, existingRels, existingCIs, existingRFCs, releaseId);

			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", Done with main RFCs and relations, time spent - " + (System.currentTimeMillis() - bomCreationStartTime));

			Map<Long, List<BomRfc>> bomsMap = buildMfstToBomRfcMap(boms);
			
			long mngviaStartTime = System.currentTimeMillis();
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", processing managed via");
			processManagedViaRels(mfstPlatComponents,bomsMap,nsPath, userId, existingRels, releaseId);
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", Done with managed via, time spent - " + (System.currentTimeMillis() - mngviaStartTime));

			
			long secByStartTime = System.currentTimeMillis();
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", processing secured by");
			processSecuredByRels(mfstPlatComponents,bomsMap,nsPath, userId, existingRels, releaseId);
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", Done with secured by, time spent - " + (System.currentTimeMillis() - secByStartTime));
			
			long entryPointStartTime = System.currentTimeMillis();
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", processing entry point");
			processEntryPointRel(platformCi.getCiId(),bomsMap, nsPath, userId, existingRels);
			logger.info(nsPath + " >>> " + platformCi.getCiName() + ", Done with entry point, time spent - " + (System.currentTimeMillis() - entryPointStartTime));
			
			if (!usePercent || !isPartial) {
				if (maxExecOrder == 0) maxExecOrder++;
				long obsoleteStartTime = System.currentTimeMillis();
				logger.info(nsPath + " >>> " + platformCi.getCiName() + ", finding obsolete boms");
				maxExecOrder = findObsolete(boms, bindingRel, nsPath, maxExecOrder, existingCIs, userId, false);
				logger.info(nsPath + " >>> " + platformCi.getCiName() + ", Done with obsolete boms, time spent - " + (System.currentTimeMillis() - obsoleteStartTime));
			}
			if (logger.isDebugEnabled()) {
				for(BomRfc bom : boms) {
					logger.debug(bom.ciName + "::" + bom.execOrder);
				}
			}
			//help gc a little bit
			existingRels = null;
			existingCIs = null;
			existingRFCs = null;

		}
		
		long timeTook = System.currentTimeMillis() - startingTime;
		logger.info(nsPath + ">>> Done with " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName() + ", Time to process - " + timeTook + " ms.");
		return maxExecOrder;
	}
	
	private boolean isPartialDeployment(String manifestNs) {
		List<CmsCIRelation> dependsOns = cmProcessor.getCIRelationsNaked(manifestNs, "manifest.DependsOn", null, null, null);
		
		for (CmsCIRelation rel : dependsOns) {
			if (rel.getAttribute("pct_dpmt") != null && !"100".equals(rel.getAttribute("pct_dpmt").getDjValue())){
				return true;
			}
		}
		return false;
	}
	
	private CmsCI getStartingPoint(List<CmsCIRelation> mfstPlatComponents, List<BomRfc> boms) {
		Set<Long> processedNodes = new HashSet<Long>();
		for (BomRfc bom : boms) {
			processedNodes.add(bom.manifestCiId);
		}
		for (CmsCIRelation manifestRel : mfstPlatComponents) {
			if (!processedNodes.contains(manifestRel.getToCiId())) {
				return manifestRel.getToCi();
			}
		}
		return null;
	}

	private void processVars(List<BomRfc> boms, Map<String,String> cloudVars, Map<String,String> globalVars, Map<String,String> localVars) {
        ExceptionConsolidator ec = CIValidationException.consolidator(CmsError.TRANSISTOR_CM_ATTRIBUTE_HAS_BAD_GLOBAL_VAR_REF,cmsUtil.getCountOfErrorsToReport());
		for (BomRfc bom : boms) {
			ec.invokeChecked(()-> trUtil.processAllVars(bom.mfstCi, cloudVars, globalVars, localVars));
		}
		ec.rethrowExceptionIfNeeded();
	}
	
	private int findObsolete(List<BomRfc> newBoms, CmsCIRelation bindingRel, String nsPath, int startingExecOrder, Map<String, CmsCI> existingCIs,String userId,  boolean global) {
		logger.info(nsPath + " >>> finding cis to delete..." );
		long startTime = System.currentTimeMillis();
		int maxExecOrder = startingExecOrder;
		Map<String, BomRfc> bomMap = new HashMap<String, BomRfc>();
		for (BomRfc bom : newBoms) {
			bomMap.put(bom.ciName, bom);
		}

		List<CmsCI> existingCis = new ArrayList<CmsCI>(existingCIs.values());
		
		Map<Long, CmsCI> obsoleteCisMap = new HashMap<Long, CmsCI>();
		for (CmsCI ci : existingCis) {
			if (!bomMap.containsKey(ci.getCiName())) {
				logger.info("This ci should be deleted - " + ci.getCiName());
				obsoleteCisMap.put(ci.getCiId(), ci);
			}
		}
		
		logger.info(nsPath + " >>> creating delete rfcs and traversing strong relations..." );
		if (obsoleteCisMap.size()>0) { 
			maxExecOrder = processObsolete(newBoms, obsoleteCisMap, startingExecOrder, nsPath, userId, global);
		}
		logger.info(nsPath + " >>> Done creating delete rfcs, time taken:" +  (System.currentTimeMillis() - startTime));
		return maxExecOrder;
	}
	
	private int processObsolete(List<BomRfc> bomRfcs, Map<Long, CmsCI> obsoleteCisMap, int startingExecOrder, String nsPath, String userId,  boolean global){
		
		int maxExecOrder = startingExecOrder;
		
		Set<Long> obsoleteToRelations = new HashSet<Long>();
		Map<Long, List<CmsLink>> obsoleteFromRelations = new HashMap<Long, List<CmsLink>>();
		List<CmsLink> dummyUpdateRels = new ArrayList<CmsLink>();

		List<CmsLink> dependsOnLinks = cmProcessor.getLinks(nsPath, "bom.DependsOn");
		//convert to map
		Map<Long, List<CmsLink>> toCiDependsOnMap = new HashMap<Long, List<CmsLink>>();
		for (CmsLink link : dependsOnLinks) {
			if (!toCiDependsOnMap.containsKey(link.getToCiId())) {
				toCiDependsOnMap.put(link.getToCiId(), new ArrayList<CmsLink>());
			}
			toCiDependsOnMap.get(link.getToCiId()).add(link);
		}
		for (Long ciId : obsoleteCisMap.keySet()) {
			//List<CmsCIRelation> toDependsOnRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "bom.DependsOn", null, null);
			if (toCiDependsOnMap.containsKey(ciId)) {
				for (CmsLink fromDependsOnCiIdLink : toCiDependsOnMap.get(ciId)) {
					if (obsoleteCisMap.containsKey(fromDependsOnCiIdLink.getFromCiId())) {
						obsoleteToRelations.add(ciId);
						if (!obsoleteFromRelations.containsKey(fromDependsOnCiIdLink.getFromCiId())) {
							obsoleteFromRelations.put(fromDependsOnCiIdLink.getFromCiId(), new ArrayList<CmsLink>());
						}
						obsoleteFromRelations.get(fromDependsOnCiIdLink.getFromCiId()).add(fromDependsOnCiIdLink);
					} else {
						dummyUpdateRels.add(fromDependsOnCiIdLink);
					}
				}	
			}
		}
		
		Map<Long, Integer> execOrder = new HashMap<Long, Integer>();
		
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
		Map<Long, List<String>> manifestPropagations = new HashMap<Long, List<String>>();
		Set<Long> propagations = new HashSet<Long>(); 
		long totalPropagationTime = 0;
		//now lets submit submit dummy update
		Set<Long> dummyUpdates = new HashSet<Long>();
		if (dummyUpdateRels.size()>0) {
			for (CmsLink rel : dummyUpdateRels) {
				dummyUpdates.add(rel.getFromCiId());
				for (BomRfc bomRfc : bomRfcs) {
					if (bomRfc.rfc == null) {
						 logger.info("bom.rfc null for " + bomRfc.ciName + " nspath: " + nsPath);;
					} else if (bomRfc.rfc.getCiId() == rel.getFromCiId()) {
						long startTime = System.currentTimeMillis();
						mapPropagations(bomRfc.manifestCiId, manifestPropagations);
						if (manifestPropagations.get(bomRfc.manifestCiId).size() != 0) {
							propagateUpdate(bomRfc.rfc.getCiId(), bomRfc.manifestCiId, manifestPropagations, userId, propagations);
						}
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
			TreeMap<Integer, List<Long>> dummyUpdateExecOrders = new TreeMap<Integer, List<Long>>();
			//now lets grab the execution orders from the bomRfcs for the CIs to be dummy updated.
			for (BomRfc bom : bomRfcs) {
				if (bom.rfc == null) {
					logger.info("rfc null for: " + bom.ciName);
					continue;
				}
				if (dummyUpdates.contains(bom.rfc.getCiId())) {
					List<Long> ciIds = dummyUpdateExecOrders.get(bom.execOrder);
					if (ciIds == null) {
						ciIds = new ArrayList<Long>();
						dummyUpdateExecOrders.put(bom.execOrder, ciIds);
					}
					ciIds.add(bom.rfc.getCiId());
				}
			}
			//Now lets iterate over the sorted order map to touch the dummy update CIs with exec order starting from max exec order
			for (int order : dummyUpdateExecOrders.keySet()) {
				maxExecOrder++;
				for (long dummyUpdateCiId : dummyUpdateExecOrders.get(new Integer(order))) {
					cmRfcMrgProcessor.createDummyUpdateRfc(dummyUpdateCiId, null, maxExecOrder, "oneops-transistor");
				}
			}
		}
		return maxExecOrder;
	}

	private void createDeleteRfc(CmsCI ci, int execOrder, String userId)
	{
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
	private void processObsoleteOrder(long startingCiId, Map<Long, Integer> execOrder, Map<Long, List<CmsLink>> obsoleteRelations) {
		if (obsoleteRelations.containsKey(startingCiId)) {
			int nextExecOrder = execOrder.get(startingCiId) + 1;
			for (CmsLink rel : obsoleteRelations.get(startingCiId)) {
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
									 CmsCIRelation bindingRel, 
									 int startExecOrder, 
									 boolean isPartial, 
									 String userId,
									 ExistingRels existingRels,
									 Map<String, CmsCI> existingCIs,
									 Map<String, CmsRfcCI> existingRFCs,
									 Long releaseId) {
		
		long nsId = trUtil.verifyAndCreateNS(nsPath);
		
		Map<String, BomRfc> bomMap = new HashMap<String, BomRfc>();
		for (BomRfc bom : boms) {
			bomMap.put(bom.nodeId, bom);
		}
		// need to verify all the to links for the case when we have converge link
		verifyToLinks(bomMap);
		//lets find out the exec order and populate relations list
		Map<String, BomLink> links = new HashMap<String, BomLink>(); 
		for (BomRfc bom :boms) {
			if (bom.fromLinks.size()==0) {
				processOrder(bom, bomMap, startExecOrder, 1);
			} else {
				for (BomLink link : bom.fromLinks) {
					links.put(link.fromNodeId + "@" + link.toNodeId, link);
					//logger.info(link.fromNodeId + "-" + link.toNodeId);
				}
			}	
		}

		
		int maxExecOrder = getMaxExecOrder(boms);
		
		Map<Integer, List<BomRfc>> orderedMap = new HashMap<Integer, List<BomRfc>>();
		for (BomRfc bom : boms) {
			if (!orderedMap.containsKey(bom.execOrder)) {
				orderedMap.put(bom.execOrder, new ArrayList<BomRfc>());
			}
			orderedMap.get(bom.execOrder).add(bom);
		}
		Set<Long> propagations = new HashSet<Long>(); 
		Set<Long> bomCiIds = new HashSet<Long>();
		Map<Long, List<String>> manifestPropagations = new HashMap<Long, List<String>>();
		long timeTakenByPropagation = 0;
		//now lets create rfcs
		int realExecOrder = startExecOrder;
		int numberOfRFCs = 0;
		List<CmsRfcCI> replacedComputes = new ArrayList<CmsRfcCI>();
		for (int i=startExecOrder; i<=maxExecOrder; i++) {
			boolean incOrder = false;
			if (orderedMap.containsKey(i)) {
				for (BomRfc bom : orderedMap.get(i)) {
					String shortClazzName = trUtil.getShortClazzName(bom.mfstCi.getCiClassName());
					String bomId = "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()) + ":" + bom.ciName;
					CmsCI existingCi = existingCIs.get(bomId);
					CmsRfcCI existingRfc = existingRFCs.get(bomId);
					boolean rfcCreated = false;
					if (priorityMap.containsKey(shortClazzName)) {
						bom.execOrder = priorityMap.get(shortClazzName);
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, nsPath, bindingRel, releaseId, userId, existingRels);
						if (rfcCreated && realExecOrder == 1) incOrder = true;
					} else {
						//bom.execOrder = realExecOrder;
						rfcCreated = upsertRfcs(bom, existingCi, existingRfc, nsId, nsPath, bindingRel, releaseId, userId, existingRels);
						if (rfcCreated && bom.rfc != null) {
							//if rfc was created, lets check if any propagation is required
							if(bom.rfc.getCiClassName().equals("bom.Compute") 
									&& bom.rfc.getRfcAction().equals("replace")) {
								replacedComputes.add(bom.rfc);
							}
							
							long startTime = System.currentTimeMillis();
							if (manifestPropagations.get(bom.manifestCiId) == null) {
								mapPropagations(bom.manifestCiId, manifestPropagations);
							}
							if (manifestPropagations.get(bom.manifestCiId).size() != 0) {
								propagateUpdate(bom.rfc.getCiId(), bom.manifestCiId, manifestPropagations, userId, propagations);
							}
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
						if (numberOfRFCs % 10 == 0) {
							logger.info(">>> Inserted " + numberOfRFCs + " rfcs;");
						}
					}
				}
			}
			if (incOrder) realExecOrder++;
		}
		
		logger.info(">>> Inserted " + numberOfRFCs + " rfcs;");


		logger.info(">>> Done with RFCs working on relations...");

		//lets create dependsOn Relations
		//TODO question should we propagate rel attrs
		int maxRfcExecOrder = getMaxRfcExecOrder(boms);
		
		maxExecOrder = (maxRfcExecOrder > 0) ? maxRfcExecOrder : maxExecOrder;
		//execute all dummmy updates in one last step
		//maxExecOrder++;
		//List<CmsRfcRelation> existingDependsOnRels = cmRfcMrgProcessor.getDfDjRelations("bom.DependsOn", null, nsPath, null, null, null);
		Set<String> djRelGoids = new HashSet<String>();
		boolean increaseMaxOrder = false;
		int numberOfRelRFCs = 0;
		for (BomLink link : links.values()) {
			if (bomMap.get(link.fromNodeId).rfc != null &&
					bomMap.get(link.toNodeId).rfc != null) {
				long fromCiId = bomMap.get(link.fromNodeId).rfc.getCiId();
				long toCiId = bomMap.get(link.toNodeId).rfc.getCiId();
				CmsRfcRelation dependsOn = bootstrapRelationRfc(fromCiId,toCiId,"bom.DependsOn", nsPath, existingRels);
				dependsOn.setComments(generateRelComments(bomMap.get(link.fromNodeId).rfc.getCiName(),
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
						if (manifestPropagations.get(bomMap.get(link.fromNodeId).manifestCiId) == null) {
							mapPropagations(bomMap.get(link.fromNodeId).manifestCiId, manifestPropagations);
						}
						if (manifestPropagations.get(bomMap.get(link.fromNodeId).manifestCiId).size() != 0) {
							propagateUpdate(fromCiId, bomMap.get(link.fromNodeId).manifestCiId, manifestPropagations, userId, propagations);
						}
						long endTime = System.currentTimeMillis();
						timeTakenByPropagation = timeTakenByPropagation + (endTime - startTime);
						increaseMaxOrder = true;
					}
					if (numberOfRelRFCs % 10 == 0) {
						logger.info(">>> Inserted " + numberOfRelRFCs + " relation rfcs;");
					}
				}
			}
		}
		
		logger.info(">>> Inserted " + numberOfRelRFCs + " relation rfcs;");
		
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
						CmsRfcCI rfc = bootstrapRfc(bom,existingRfc, existingCi, nsPath);
						rfc.setCreatedBy(userId);
						rfc.setUpdatedBy(userId);
						rfc.setNsId(nsId);
						cmRfcMrgProcessor.createDummyUpdateRfc(rfc.getCiId(), null, bom.execOrder, userId);
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
		
		logger.info(nsPath + " >>> Total time taken by propagation in seconds: " + timeTakenByPropagation/1000);
		return maxExecOrder;
	}
	
	private void propagate4ComputeReplace(List<CmsRfcCI> bomCompRfcs) {
		for (CmsRfcCI rfc : bomCompRfcs) {
			for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Lb")) {
				cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
			}
			for (CmsCIRelation rel : cmProcessor.getToCIRelationsNakedNoAttrs(rfc.getCiId(), "bom.DependsOn", null, "bom.Fqdn")) {
				cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, rfc.getExecOrder() + 1, rfc.getCreatedBy());
			}
		}
		
	}
	
	private Map<String, CmsCI> getExistingCis(long cloudId, String nsPath) {
		List<CmsCIRelation> bomRels = cmProcessor.getToCIRelationsByNs(cloudId, BOM_CLOUD_RELATION_NAME, null, null, nsPath);
		Map<String, CmsCI> bomCIs = new HashMap<String, CmsCI>();
		for (CmsCIRelation rel : bomRels) {
			CmsCI bomCi = rel.getFromCi();
			String key =bomCi.getCiClassName() + ":" + bomCi.getCiName();
			bomCIs.put(key, bomCi);
		}
		return bomCIs;
	}

	private Map<String, Map<String,CmsCIRelation>> getExistingRelations(String nsPath) {
		List<CmsCIRelation> bomRels = cmProcessor.getCIRelationsNaked(nsPath, null, null, null, null);
		Map<String, Map<String,CmsCIRelation>> bomRelsMap = new HashMap<String, Map<String,CmsCIRelation>>();
		for (CmsCIRelation rel : bomRels) {
			if (!bomRelsMap.containsKey(rel.getRelationName())) {
				bomRelsMap.put(rel.getRelationName(), new HashMap<String,CmsCIRelation>());
			}
			bomRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
		}
		return bomRelsMap;
	}
	
	
	private Map<String, CmsRfcCI> getOpenRFCs(String nsPath) {
		List<CmsRfcCI> existingRfcs = rfcProcessor.getOpenRfcCIByClazzAndName(nsPath, null, null);
		Map<String, CmsRfcCI> rfcs = new HashMap<String, CmsRfcCI>();
		for (CmsRfcCI rfc : existingRfcs) {
			String key = rfc.getCiClassName() + ":" + rfc.getCiName();
			rfcs.put(key,rfc);
		}
		return rfcs;
	}

	
	private Map<String, Map<String,CmsRfcRelation>> getOpenRelationsRfcs(String nsPath) {
		
		List<CmsRfcRelation> bomRels = rfcProcessor.getOpenRfcRelationsByNs(nsPath);
		Map<String, Map<String,CmsRfcRelation>> bomRelsMap = new HashMap<String, Map<String,CmsRfcRelation>>();
		for (CmsRfcRelation rel : bomRels) {
			if (!bomRelsMap.containsKey(rel.getRelationName())) {
				bomRelsMap.put(rel.getRelationName(), new HashMap<String,CmsRfcRelation>());
			}
			bomRelsMap.get(rel.getRelationName()).put(rel.getFromCiId() + ":" + rel.getToCiId(), rel);
		}
		return bomRelsMap;
	}

	
	private void propagateUpdate(long bomCiId, long manifestId,
			Map<Long, List<String>> manifestPropagations, String userId, Set<Long> propagations) {
		List<String> targetManifestCiNames = manifestPropagations.get(manifestId);
		List<CmsCIRelation> rels  = cmProcessor.getAllCIRelations(bomCiId);// all bom relations for this bom ci
		
		if (targetManifestCiNames == null) {
			logger.info("nothing to propagate for bomCiId: " + bomCiId + " and manifestCiId: " + manifestId);
			return;
		}
		
		for (String targetCiName : targetManifestCiNames) {
			for (CmsCIRelation rel : rels) {
				if (! rel.getRelationName().equals("bom.DependsOn")) {
					continue;
				}
				if (rel.getFromCi() != null) {
					String ciName = rel.getFromCi().getCiName();
					if (ciName != null && ciName.startsWith(targetCiName + "-")) {
						if (propagations.contains(rel.getFromCiId())) {
							continue;
						}
						logger.info("propagating update from bom cid : " + bomCiId + " to " + rel.getFromCiId());
						propagations.add(rel.getFromCiId());
						List<CmsCIRelation> realizedAs = cmProcessor.getToCIRelations(rel.getFromCiId(),
								"base.RealizedAs", rel.getFromCi().getCiClassName().replaceFirst("bom", "manifest"));
						if (realizedAs != null) {
							propagateUpdate(rel.getFromCiId(), realizedAs.get(0).getFromCiId(), manifestPropagations, userId, propagations);
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
								"base.RealizedAs", rel.getToCi().getCiClassName().replaceFirst("bom", "manifest"));
						if (realizedAs != null) {
							propagateUpdate(rel.getToCiId(), realizedAs.get(0).getFromCiId(), manifestPropagations, userId, propagations);
						}
					}
				}
			}
		}
	}

	private void mapPropagations(long manifestCiId, Map<Long, List<String>> manifestPropagations) {
		List<String> targetManifests = manifestPropagations.get(manifestCiId);
		if (targetManifests != null) {
			return;//propagations already calculated for this manifest cid
		}
		targetManifests = new ArrayList<String>();
		manifestPropagations.put(manifestCiId, targetManifests);
		List<CmsCIRelation> rels  = cmProcessor.getAllCIRelations(manifestCiId);
		for (CmsCIRelation rel : rels) {
			if (! rel.getRelationName().equals("manifest.DependsOn")) {
				continue;
			}
			CmsCIRelationAttribute attrib = rel.getAttribute("propagate_to");
			if (attrib != null && attrib.getDfValue() != null
					) {
				if (rel.getFromCiId() > 0
                        &&rel.getFromCiId() == manifestCiId
                        && (attrib.getDfValue().equalsIgnoreCase("to") || attrib.getDfValue().equalsIgnoreCase("both"))) {
					//found 
					targetManifests.add(rel.getToCi().getCiName());	
					mapPropagations(rel.getToCiId(), manifestPropagations);
				} else if (rel.getToCiId() > 0
                        &&rel.getToCiId() == manifestCiId
                        && (attrib.getDfValue().equalsIgnoreCase("from") || attrib.getDfValue().equalsIgnoreCase("both"))) {
					//found 
					targetManifests.add(rel.getFromCi().getCiName());
					mapPropagations(rel.getFromCiId(), manifestPropagations);
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
				if (cmProcessor.getCountFromCIRelationsByNS(bom.mfstCi.getCiId(),  "base.RealizedAs", null, null, nsPath, false) == 0) {
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
			Map<String,String> attrs = new HashMap<String,String>();
			attrs.put("last_manifest_rfc", String.valueOf(bom.mfstCi.getLastAppliedRfcId()));
			CmsRfcRelation realizedAs = bootstrapRelationRfcWithAttributes(bom.mfstCi.getCiId(), bom.rfc.getCiId(), "base.RealizedAs", nsPath, attrs, existingRels);
			if (rfcCreated) {
				realizedAs.setToRfcId(bom.rfc.getRfcId());
			}
			realizedAs.setComments(generateRelComments(bom.mfstCi.getCiName(), bom.mfstCi.getCiClassName(), bom.rfc.getCiName(), bom.rfc.getCiClassName()));
			realizedAs.getAttribute("priority").setNewValue(bindingRel.getAttribute("priority").getDjValue());
			realizedAs.setCreatedBy(userId);
			realizedAs.setUpdatedBy(userId);
			realizedAs.setNsId(nsId);
			//validateRelRfc(realizedAs, bom.mfstCi.getCiClassId(), bom.rfc.getCiClassId());
			//realizedAs.setValidated(true);
			createBomRelationRfc(realizedAs, existingRels, releaseId);
			//cmRfcMrgProcessor.upsertRfcRelationNoCheck(realizedAs, userId, "dj");
			
			//lest create relation to the binding
			CmsRfcRelation deployedTo = bootstrapRelationRfc(bom.rfc.getCiId(), bindingRel.getToCiId(), "base.DeployedTo", nsPath, existingRels);
			deployedTo.setComments(generateRelComments(bom.rfc.getCiName(), bom.rfc.getCiClassName(), bindingRel.getToCi().getCiName(), bindingRel.getToCi().getCiClassName()));
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
		if (rfc.getCiId() == 0) {
			//this is add rfc
			if (releaseId == null) {
				if (rfc.getReleaseId() > 0) {
					releaseId = rfc.getReleaseId();
				} else {
					rfc.setReleaseId(rfcProcessor.getOpenReleaseIdByNs(rfc.getReleaseNsPath(), null, rfc.getCreatedBy()));
				}
			}
			rfc.setIsActiveInRelease(true);
			rfc.setRfcAction("add");

			if (rfc.getRfcId() == 0) {
				rfcProcessor.createBomRfc(rfc);
			} else {
				rfcProcessor.updateBomRfc(rfc, existingRfc);
			}
		} else {
			//need to figure out delta and create update rfc
			if (needUpdateRfc(rfc, existingCi)) {
				if (releaseId == null) {
					if (rfc.getReleaseId() > 0) {
						releaseId = rfc.getReleaseId();
					} else {
						rfc.setReleaseId(rfcProcessor.getOpenReleaseIdByNs(rfc.getReleaseNsPath(), null, rfc.getCreatedBy()));
					}
				}
				rfc.setIsActiveInRelease(true);
				if (rfc.getRfcId() == 0) {
					rfcProcessor.createBomRfc(rfc);
				} else {
					rfcProcessor.updateBomRfc(rfc, existingRfc);
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
		Set<String> equalAttrs = new HashSet<String>( rfcCi.getAttributes().size());
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
		if (rfc.getCiRelationId() == 0) {
			//this is add rfc
			if (releaseId == null) {
				if (rfc.getReleaseId() > 0) {
					releaseId = rfc.getReleaseId();
				} else {
					rfc.setReleaseId(rfcProcessor.getOpenReleaseIdByNs(rfc.getReleaseNsPath(), null, rfc.getCreatedBy()));
				}
			}
			rfc.setIsActiveInRelease(true);
			rfc.setRfcAction("add");

			if (rfc.getRfcId() == 0) {
				rfcProcessor.createBomRfcRelation(rfc);
			} else {
				rfcProcessor.updateBomRfcRelation(rfc, existingRels.getOpenRelRfc(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
			}
		} else {
			//need to figure out delta and create update rfc
			CmsCIRelation existingRel = existingRels.getExistingRel(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId());
			if (needUpdateRfcRelation(rfc, existingRel)) {
				if (releaseId == null) {
					if (rfc.getReleaseId() > 0) {
						releaseId = rfc.getReleaseId();
					} else {
						rfc.setReleaseId(rfcProcessor.getOpenReleaseIdByNs(rfc.getReleaseNsPath(), null, rfc.getCreatedBy()));
					}
				}
				rfc.setIsActiveInRelease(true);
				rfc.setRfcAction("update");
				if (rfc.getRfcId() == 0) {
					rfcProcessor.createBomRfcRelation(rfc);
				} else {
					rfcProcessor.updateBomRfcRelation(rfc, existingRels.getOpenRelRfc(rfc.getRelationName(), rfc.getFromCiId(), rfc.getToCiId()));
				}
			}
		}
	}
	
	private boolean needUpdateRfcRelation(CmsRfcRelation rfcRel, CmsCIRelation baseRel) {
		
		boolean needUpdate = false;
		Set<String> equalAttrs = new HashSet<String>( rfcRel.getAttributes().size());
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
	
	private String generateRelComments(String fromCiName, String fromCiClass, String toCiName, String toCiClass) {
		Map<String, String> strMap = new HashMap<String, String>();
		strMap.put("fromCiName", fromCiName);
		strMap.put("fromCiClass", fromCiClass);
		strMap.put("toCiName", toCiName);
		strMap.put("toCiClass", toCiClass);
		return gson.toJson(strMap);
	}
	
	public int deleteManifestPlatform(CmsCI platformCi, CmsCIRelation bindingRel, String nsPath, int startExecOrder, String userId){
		
		int maxExecOrder = 0;
		
		List<CmsCIRelation> mfstPlatComponents = cmProcessor.getFromCIRelations(platformCi.getCiId(), null, "Requires", null);
		if (mfstPlatComponents.size() > 0) {
			//List<BomRfc> boms = new ArrayList<BomRfc>();

			String platNsPath = null;
			if (platformCi.getCiClassName().equals("manifest.Iaas")) {
				platNsPath = nsPath + "/" + platformCi.getCiName();
			} else {
				platNsPath = nsPath + "/" + platformCi.getCiName() + "/" + platformCi.getAttribute("major_version").getDjValue();
			}
			
			long numOfBoms = cmProcessor.getCountBy3(platNsPath, null, null, false);
			if (numOfBoms >0) {
				logger.info(nsPath + ">>>" + platformCi.getCiName() + ", finding obsolete boms");
				Map<String, CmsCI> existingCIs = getExistingCis(bindingRel.getToCiId(), platNsPath);
				maxExecOrder = findObsolete(new ArrayList<BomRfc>(), bindingRel, platNsPath, startExecOrder, existingCIs, userId, true);
			} else {
				// there is no boms lets cleanup any open rfcs if any
				List<CmsRfcRelation> deployedTorfcRels = rfcProcessor.getOpenToRfcRelationByTargetClazzNoAttrs(bindingRel.getToCiId(), "base.DeployedTo", null, null);
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
		Map<String, CmsClazzAttribute> clazzAttrs = new HashMap<String, CmsClazzAttribute>();
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
	    applyCiToRfc(newRfc, bom, clazzAttrs, true);
	    newRfc.setExecOrder(bom.execOrder);
	    setCiId(newRfc, existingRfc, existingBomCi);
		return newRfc;
	}
	
   /*
	private void reverseExecOrder(List<BomRfc> boms, int startOrder) {
		int maxOrder = getMaxExecOrder(boms);
		for (BomRfc bom:boms) {
			bom.execOrder = maxOrder-bom.execOrder+startOrder;
		}
	}
	*/
	private void processOrder(BomRfc bom, Map<String, BomRfc> bomMap, int order, int recursionDepth) {

		if (recursionDepth >= MAX_RECUSION_DEPTH) {
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
		List<CmsCIRelation> entryPoints = cmProcessor.getFromCIRelationsNaked(platformCiId, null, "Entrypoint", null);
		for (CmsCIRelation epRel : entryPoints) {
			if (bomsMap.containsKey(epRel.getToCiId())) {
				for (BomRfc bom : bomsMap.get(epRel.getToCiId())) {
					if (bom.rfc != null) {
					CmsRfcRelation entryPoint = bootstrapRelationRfc(platformCiId, bom.rfc.getCiId(),"base.Entrypoint", nsPath, existingRels);
					cmRfcMrgProcessor.upsertRelationRfc(entryPoint, user, "dj");
					}
				}
			}
		}
	}

	private void processManagedViaRels(List<CmsCIRelation> mfstCiRels, Map<Long, List<BomRfc>> bomsMap, String nsPath, String user, ExistingRels existingRels, Long releaseId) {
	    
        CmsVar disableBFSVar = cmProcessor.getCmSimpleVar(DISABLE_BFS_VAR_NAME);
        boolean disableBFS = (disableBFSVar !=null && "true".equalsIgnoreCase(disableBFSVar.getValue()));
	    
		long nsId = trUtil.verifyAndCreateNS(nsPath);
		List<CmsLink> dependsOnlinks = cmRfcMrgProcessor.getLinks(nsPath, "bom.DependsOn");
		//convert to map for traversing the path
		Map<Long, Map<String,List<Long>>> dependsOnMap = new HashMap<Long, Map<String,List<Long>>>();
		for (CmsLink link : dependsOnlinks) {
			if (!dependsOnMap.containsKey(link.getFromCiId())) {
				dependsOnMap.put(link.getFromCiId(), new HashMap<String,List<Long>>());
			}
			if (!dependsOnMap.get(link.getFromCiId()).containsKey(link.getToClazzName())) {
				dependsOnMap.get(link.getFromCiId()).put(link.getToClazzName(), new ArrayList<Long>());
			}
			dependsOnMap.get(link.getFromCiId()).get(link.getToClazzName()).add(link.getToCiId());
		}
		long counter = 0;
		
		Set<String> relRfcGoids = new HashSet<String>();
		for (CmsCIRelation mfstCiRel : mfstCiRels) {
			CmsCI mfstCi = mfstCiRel.getToCi();
			//first lets check if we even have an add rfc for this Ci
			//if (newRfcExists(mfstCi.getCiId(), bomsMap)) {
			List<CmsCIRelation> mfstMngViaRels = cmProcessor.getFromCIRelationsNaked(mfstCi.getCiId(), null, "ManagedVia", null);
			for (CmsCIRelation mfstMngViaRel : mfstMngViaRels) {
				// lets find the path 
				//List<String> pathClasses = getTraversalPath(mfstMngViaRel);
				List<String> pathClasses = (ENABLE_BFS_OPTIMIZATION && !disableBFS)?
						getDpOnPathBfs(mfstMngViaRel.getFromCiId(), mfstMngViaRel.getToCiId()):
						getDpOnPath(mfstMngViaRel.getFromCiId(), mfstMngViaRel.getToCiId());
				if (pathClasses.size()==0) {
					String err = "Can not traverse ManagedVia relation using DependsOn path from ci " + mfstMngViaRel.getFromCiId() + ", to ci " + mfstMngViaRel.getToCiId() + "\n";
					err += mfstMngViaRel.getComments();
					logger.error(err);
					throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
				}
				for (BomRfc bomRfc : bomsMap.get(mfstCi.getCiId())) {
					
					//for this rfc we need to traverse by the DependsOn path down to ManagedVia Ci and create the relation\
					//Now this is tricky since it could get resolved as a tree so we need to use recursion
					LinkedList<String> path = new LinkedList<String>();
					path.addAll(pathClasses);
					if (bomRfc.rfc != null) {
						List<Long> targets = getLeafsByPath(bomRfc.rfc.getCiId(), path,mfstMngViaRel.getToCiId(), dependsOnMap);
						Map<Long, BomRfc> targetMap = new HashMap<Long, BomRfc>();
						for (BomRfc targetBom :  bomsMap.get(mfstMngViaRel.getToCiId())) {
							targetMap.put(targetBom.rfc.getCiId(), targetBom);
						}
						for (long managedViaCiId : targets) {
							CmsCIRelation existingRel = existingRels.getExistingRel(BOM_MANAGED_VIA_RELATION_NAME, bomRfc.rfc.getCiId(), managedViaCiId);
									//cmProcessor.getFromToCIRelationsNaked(bomRfc.rfc.getCiId(), "bom.ManagedVia", managedViaCiId);
							if (existingRel == null) {		
								CmsRfcRelation managedVia = bootstrapRelationRfc(bomRfc.rfc.getCiId(), managedViaCiId, "bom.ManagedVia", nsPath, existingRels);
								managedVia.setNsId(nsId);
								managedVia.setReleaseId(bomRfc.rfc.getReleaseId());
								if (!relRfcGoids.contains(managedVia.getRelationGoid())) {
									if (targetMap.containsKey(managedViaCiId)) {
										CmsRfcCI toCiRfc = targetMap.get(managedViaCiId).rfc; 
										managedVia.setComments(generateRelComments(bomRfc.rfc.getCiName(), bomRfc.rfc.getCiClassName(), toCiRfc.getCiName(), toCiRfc.getCiClassName()));
										
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
		logger.info("Bom ManagedVia Relation Counter:"+ counter);
	};

	private void processSecuredByRels(List<CmsCIRelation> mfstCiRels, Map<Long, List<BomRfc>> bomsMap, String nsPath,  String user, ExistingRels existingRels, Long releaseId) {
		
		long nsId = trUtil.verifyAndCreateNS(nsPath);
		
		for (CmsCIRelation mfstCiRel : mfstCiRels) {
			CmsCI mfstCi = mfstCiRel.getToCi();
			List<CmsCIRelation> mfstSecuredByRels = cmProcessor.getFromCIRelationsNaked(mfstCi.getCiId(), null, "SecuredBy", null);
			for (CmsCIRelation mfstSecuredByRel : mfstSecuredByRels) {
				for (BomRfc fromBomRfc : bomsMap.get(mfstCi.getCiId())) {
					for (BomRfc toBomRfc : bomsMap.get(mfstSecuredByRel.getToCiId())) {
						CmsRfcRelation securedBy = bootstrapRelationRfc(fromBomRfc.rfc.getCiId(), toBomRfc.rfc.getCiId(), "bom.SecuredBy", nsPath, existingRels);
						
						securedBy.setComments(generateRelComments(fromBomRfc.rfc.getCiName(), fromBomRfc.rfc.getCiClassName(), toBomRfc.rfc.getCiName(), toBomRfc.rfc.getCiClassName()));
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
						//cmRfcMrgProcessor.upsertRelationRfc(securedBy, user, "dj");
					}
				}
			}
		}
	};
	
	
	private List<Long> getLeafsByPath(long startCiId, LinkedList<String> path, long targetMfstCiId, Map<Long, Map<String,List<Long>>> dependsOnMap) {
		List<Long> listOfTargets = new ArrayList<Long>();
		if (path.size() == 0) {
			//we reached end of the path but seems like there are multiple routes, but at this point we are good
			return listOfTargets;
		}
		
		String nextMfstClass = path.poll();
		String bomClass = "bom." + trUtil.getLongShortClazzName(nextMfstClass);
		
		//List<CmsRfcRelation> dependsOnRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(startCiId, null, "DependsOn", bomClass);
		List<Long> targets = new ArrayList<Long>();
		if (dependsOnMap.containsKey(startCiId)) {
			if (dependsOnMap.get(startCiId).containsKey(bomClass)) {
				targets.addAll(dependsOnMap.get(startCiId).get(bomClass));
			}
		}
		if (path.size() ==0) {
			//this should be our target list
			for (long toCiId : targets) {
				//lets check if this guy is related to the right mfstCi
				//TODO this could be not nessesary
				//if (cmRfcMrgProcessor.getToCIRelationsNakedNoAttrs(rel.getToCiId(), null, "RealizedAs", nextMfstClass).size() >0) {
					listOfTargets.add(toCiId);
				//}
			}
		} else {
			for (long toCiId : targets) {
				listOfTargets.addAll(getLeafsByPath(toCiId, new LinkedList<String>(path), targetMfstCiId, dependsOnMap));
			}	
		}
		return listOfTargets;
	}

	private List<String> getDpOnPathBfs(long fromId, long endId) { // implement shortest path search (modified BFS)
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
			List<CmsCIRelation> dependsOnRelations = cmProcessor.getFromCIRelations(current, null, "DependsOn", null);
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

	private List<String> getDpOnPath(long fromId, long endId) {
		List<String> pathClasses = new ArrayList<String>();
		List<CmsCIRelation> dponRels = cmProcessor.getFromCIRelations(fromId, null, "DependsOn", null);
		for (CmsCIRelation dponRel : dponRels) {
			if (dponRel.getToCi().getCiId() == endId) {
				pathClasses.add(dponRel.getToCi().getCiClassName());
				return pathClasses;
			} else {
				List<String> downClasses = getDpOnPath(dponRel.getToCiId(), endId);
				if (downClasses.size() > 0) {
					pathClasses.add(dponRel.getToCi().getCiClassName());
					pathClasses.addAll(downClasses);
					return pathClasses;
				}
				
			}
		}
		return pathClasses;
	}
	
	
	private Map<Long, List<BomRfc>> buildMfstToBomRfcMap(List<BomRfc> boms) {
		Map<Long, List<BomRfc>> map = new HashMap<Long, List<BomRfc>>();
		for (BomRfc bom : boms) {
			if (!map.containsKey(bom.manifestCiId)) {
				map.put(bom.manifestCiId, new ArrayList<BomRfc>()); 
			}
			map.get(bom.manifestCiId).add(bom);
		}
		return map;
	}

	private List<BomRfc> processNode(BomRfc node, CmsCIRelation binding, Map<String, List<BomRfc>> mfstIdEdge2nodeId, Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels, int edgeNum, boolean usePercent, int recursionDepth){
		
		if (recursionDepth >= MAX_RECUSION_DEPTH) {
			String err = "Circular dependency detected, (level - " + recursionDepth + "),\n please check the platform diagram for " + extractPlatformNameFromNsPath(node.mfstCi.getNsPath());
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
		}

		if (edgeNum >= MAX_NUM_OF_EDGES) {
			String err = "Max number of edges is reached - " + edgeNum + "\n please check the platform diagram for " + extractPlatformNameFromNsPath(node.mfstCi.getNsPath());
			logger.error(err);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
		}

		
		
		logger.info("working on " + node.ciName + "; recursion depth - " + recursionDepth);
		
		List<BomRfc> newBoms = new ArrayList<BomRfc>();

		if (node.isProcessed) {
			return newBoms;
		}
		
		List<CmsCIRelation> mfstFromRels = null;
		List<CmsCIRelation> mfstToRels = null;

		if (!manifestDependsOnRels.containsKey(node.manifestCiId)) {
			Map<String,List<CmsCIRelation>> rels = new HashMap<String,List<CmsCIRelation>>();
			rels.put("from",  cmProcessor.getFromCIRelations(node.manifestCiId, "manifest.DependsOn", null));
			rels.put("to", cmProcessor.getToCIRelations(node.manifestCiId, "manifest.DependsOn", null));
			manifestDependsOnRels.put(node.manifestCiId, rels);
		}
		
		mfstFromRels = manifestDependsOnRels.get(node.manifestCiId).get("from");
		mfstToRels = manifestDependsOnRels.get(node.manifestCiId).get("to");;
		
		//logger.info("got " + mfstFromRels.size() + " 'from' relations");
		//logger.info("got " + mfstToRels.size() + " 'to' relations");
		
		for (CmsCIRelation fromRel : mfstFromRels) {
			int numEdges = 0;
			int percent = 100;
			int current = Integer.valueOf(fromRel.getAttribute("current").getDfValue());
			if (fromRel.getAttribute("flex") != null &&  
				Boolean.valueOf(fromRel.getAttribute("flex").getDfValue()) 
				&& binding.getAttributes().containsKey("pct_scale") 
				&& binding.getAttribute("pct_scale") != null) {
				int pctScale = Integer.valueOf(binding.getAttribute("pct_scale").getDjValue());
				current = (int)Math.ceil(current*(pctScale/100.0)) ;
			}
			if (usePercent && fromRel.getAttribute("pct_dpmt") != null) {	
				percent = Integer.valueOf(fromRel.getAttribute("pct_dpmt").getDjValue());
				numEdges = (int)Math.floor(current*(percent/100.0)) ;
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

					if (!mfstIdEdge2nodeId.containsKey(key)) mfstIdEdge2nodeId.put(key, new ArrayList<BomRfc>());
					mfstIdEdge2nodeId.get(key).add(newBom);
					newBoms.addAll(processNode(newBom, binding, mfstIdEdge2nodeId, manifestDependsOnRels, newEdgeNum, usePercent, recursionDepth + 1));
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
				
				mfstIdEdge2nodeId.put(key, new ArrayList<BomRfc>());

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
					newBoms.addAll(processNode(newBom, binding, mfstIdEdge2nodeId, manifestDependsOnRels, edgeNumLocal, usePercent, recursionDepth + 1));
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
	
	private void applyCiToRfc(CmsRfcCI newRfc, BomRfc bom, Map<String, CmsClazzAttribute> mdAttrs, boolean checkExpression) {
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
		
		List<BomLink> fromLinks = new ArrayList<BomLink>();
		List<BomLink> toLinks = new ArrayList<BomLink>();
		
		public List<BomLink> getExisitngFromLinks(long toMfstCiId) {
			List<BomLink> links = new ArrayList<BomLink>();
			for (BomLink link : fromLinks) {
				if (link.toMfstCiId == toMfstCiId) {
					links.add(link);
				}
			}
			return links;
		}

		public List<BomLink> getExisitngToLinks(long fromMfstCiId) {
			List<BomLink> links = new ArrayList<BomLink>();
			for (BomLink link : toLinks) {
				if (link.fromMfstCiId == fromMfstCiId) {
					links.add(link);
				}
			}
			return links;
		}
		public BomLink getExisitngToLinks(String fromNodeId) {
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
		
		protected CmsCIRelation getExistingRel(String relName, long fromCiId, long toCiId) {
			if (existingRels.containsKey(relName)) {
				return existingRels.get(relName).get(fromCiId + ":" + toCiId);
			}
			return null;
		}

		protected Collection<CmsCIRelation> getExistingRel(String relName) {
			if (existingRels.containsKey(relName)) {
				return existingRels.get(relName).values();
			}
			return new ArrayList<CmsCIRelation>(0);
		}
		
		/*
		protected Collection<CmsRfcRelation> getExistingRelRfc(String relName) {
			if (openRelRfcs.containsKey(relName)) {
				return openRelRfcs.get(relName).values();
			}
			return new ArrayList<CmsRfcRelation>(0);
		}
		*/
		
		protected void addRelRfc(CmsRfcRelation relRfc) {
			String localKey = relRfc.getFromCiId() + ":" + relRfc.getToCiId();
			if (!openRelRfcs.containsKey(relRfc.getRelationName())) {
				openRelRfcs.put(relRfc.getRelationName(), new HashMap<String,CmsRfcRelation>());
			}
			openRelRfcs.get(relRfc.getRelationName()).put(localKey, relRfc);
		}
		
		protected CmsRfcRelation getOpenRelRfc(String relName, long fromCiId, long toCiId) {
			if (openRelRfcs.containsKey(relName)) {
				return openRelRfcs.get(relName).get(fromCiId + ":" + toCiId);
			}
			return null;
		}
		
	}
}
