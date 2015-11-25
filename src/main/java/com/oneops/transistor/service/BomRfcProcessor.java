package com.oneops.transistor.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.exceptions.DJException;
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

public class BomRfcProcessor {

	static Logger logger = Logger.getLogger(BomRfcProcessor.class);
	
    private static final Map<String, Integer> priorityMap = new HashMap<String, Integer>();
    static {
    	//priorityMap.put("Compute", 2);
    	//priorityMap.put("Storage", 2);
    	priorityMap.put("Keypair", 1);
    }
    
    private static final int priorityMax = 1;
    
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
		
		logger.info(">>>>>>>>>>>>>Start working on " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName());
		
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
		
		Map<String,String> localVars = cmsUtil.getLocalVars(platformCi);
		
		List<CmsCIRelation> mfstPlatComponents = cmProcessor.getFromCIRelations(platformCi.getCiId(), null, "Requires", null);
		if (mfstPlatComponents.size() > 0) {
		
			String manifestNs = mfstPlatComponents.get(0).getNsPath();
			boolean isPartial = isPartialDeployment(manifestNs);
			
			List<BomRfc> boms = new ArrayList<BomRfc>();
			Map<String, List<String>> mfstId2nodeId = new HashMap<String,List<String>>();
			
			CmsCI startingPoint = mfstPlatComponents.get(0).getToCi(); 
			Map<String, Integer> namesMap = new HashMap<String, Integer>();
			Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels = new HashMap<Long,Map<String,List<CmsCIRelation>>>();
			
			while (startingPoint != null) {
				BomRfc newBom = bootstrapNewBom(startingPoint, namesMap, bindingRel.getToCiId(), 1);
				boms.add(newBom);	
				mfstId2nodeId.put(String.valueOf(newBom.manifestCiId) + "-" + 1, new ArrayList<String>(Arrays.asList(newBom.nodeId)));
				
				boms.addAll(processNode(newBom, namesMap, bindingRel.getToCiId(), mfstId2nodeId, manifestDependsOnRels, 1, usePercent));
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
			logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", creating rfcs");
			maxExecOrder = createBomRfcsAndRels(boms, nsPath, bindingRel, startExecOrder, isPartial, userId);
			
			Map<Long, List<BomRfc>> bomsMap = buildMfstToBomRfcMap(boms);

			logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", processing managed via");
			processManagedViaRels(mfstPlatComponents,bomsMap,nsPath, userId);

			logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", processing secured by");
			processSecuredByRels(mfstPlatComponents,bomsMap,nsPath, userId);
			
			logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", processing entry point");
			processEntryPointRel(platformCi.getCiId(),bomsMap, nsPath, userId);
			
			if (!usePercent || !isPartial) {
				if (maxExecOrder == 0) maxExecOrder++;
				logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", finding obsolete boms");
				findObsolete(boms, bindingRel, nsPath, maxExecOrder);
			}
			for(BomRfc bom : boms) {
				logger.info(bom.ciName + "::" + bom.execOrder);
			}
		}
		
		long timeTook = System.currentTimeMillis() - startingTime;
		logger.info(">>>>>>>>>>>>>>>Done with " + platformCi.getCiName() + ", cloud - " + bindingRel.getToCi().getCiName());
		logger.info("Time to process " + platformCi.getCiName() + " " + timeTook + " ms." );
		
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
		for (BomRfc bom : boms) {
			trUtil.processAllVars(bom.mfstCi, cloudVars, globalVars, localVars);
		}	
	}
	
	private int findObsolete(List<BomRfc> newBoms, CmsCIRelation bindingRel, String nsPath, int startingExecOrder) {	
		int maxExecOrder = startingExecOrder;
		Map<String, BomRfc> bomMap = new HashMap<String, BomRfc>();
		for (BomRfc bom : newBoms) {
			bomMap.put(bom.ciName, bom);
		}

		List<CmsCI> existingCis = new ArrayList<CmsCI>();
		
		//lets find the components of this platform deployed to the Binding cloud
		List<CmsCIRelation> deployedToRels = cmProcessor.getToCIRelationsByNs(bindingRel.getToCiId(), "base.DeployedTo", null, null, nsPath);
		for (CmsCIRelation rel : deployedToRels) {
			existingCis.add(rel.getFromCi());
		}
		
		
		Map<Long, CmsCI> obsoleteCisMap = new HashMap<Long, CmsCI>();
		for (CmsCI ci : existingCis) {
			if (!bomMap.containsKey(ci.getCiName())) {
				logger.info("This ci should be deleted - " + ci.getCiName());
				obsoleteCisMap.put(ci.getCiId(), ci);
			}
		}

		if (obsoleteCisMap.size()>0) { 
			maxExecOrder = processObsolete(obsoleteCisMap, startingExecOrder);
		}
		return maxExecOrder;
	}
	
	private int processObsolete(Map<Long, CmsCI> obsoleteCisMap, int startingExecOrder){
		
		int maxExecOrder = startingExecOrder;
		
		Set<Long> obsoleteToRelations = new HashSet<Long>();
		Map<Long, List<CmsCIRelation>> obsoleteFromRelations = new HashMap<Long, List<CmsCIRelation>>();
		List<CmsCIRelation> dummyUpdateRels = new ArrayList<CmsCIRelation>();

		for (Long ciId : obsoleteCisMap.keySet()) {
			List<CmsCIRelation> toDependsOnRels = cmProcessor.getToCIRelationsNakedNoAttrs(ciId, "bom.DependsOn", null, null);
			for (CmsCIRelation rel : toDependsOnRels) {
				if (obsoleteCisMap.containsKey(rel.getFromCiId())) {
					obsoleteToRelations.add(ciId);
					if (!obsoleteFromRelations.containsKey(rel.getFromCiId())) {
						obsoleteFromRelations.put(rel.getFromCiId(), new ArrayList<CmsCIRelation>());
					}
					obsoleteFromRelations.get(rel.getFromCiId()).add(rel);
				} else {
					dummyUpdateRels.add(rel);
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
			if (priorityMap.containsKey(shortClazzName)) {
				int priorityOrder = priorityMap.get(shortClazzName);
				cmRfcMrgProcessor.requestCiDelete(ciId, "oneops-transistor", startingExecOrder + obsoleteCisMap.size() + priorityMax - priorityOrder + 1);
			} else {
				cmRfcMrgProcessor.requestCiDelete(ciId, "oneops-transistor", ciExecOrder);
			}
			maxExecOrder = (ciExecOrder > maxExecOrder) ? ciExecOrder : maxExecOrder;
		}

		//now lets submit submit dummy update
		if (dummyUpdateRels.size()>0) {
			maxExecOrder++;
			for (CmsCIRelation rel : dummyUpdateRels) {
				cmRfcMrgProcessor.createDummyUpdateRfc(rel.getFromCiId(), null, maxExecOrder, "oneops-transistor");
			}
		}
		
		return 0;
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

	private int createBomRfcsAndRels(List<BomRfc> boms, String nsPath, CmsCIRelation bindingRel, int startExecOrder, boolean isPartial, String userId) {
		
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
				processOrder(bom, bomMap, startExecOrder);
			} else {
				for (BomLink link : bom.fromLinks) {
					links.put(link.fromNodeId + "@" + link.toNodeId, link);
					logger.info(link.fromNodeId + "-" + link.toNodeId);
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
		for (int i=startExecOrder; i<=maxExecOrder; i++) {
			boolean incOrder = false;
			if (orderedMap.containsKey(i)) {
				for (BomRfc bom : orderedMap.get(i)) {
					String shortClazzName = trUtil.getShortClazzName(bom.mfstCi.getCiClassName());
					if (priorityMap.containsKey(shortClazzName)) {
						bom.execOrder = priorityMap.get(shortClazzName);
						boolean rfcCreated = upsertRfcs(bom, nsPath, bindingRel, userId);
						if (rfcCreated && realExecOrder == 1) incOrder = true;
					} else {
						//bom.execOrder = realExecOrder;
						boolean rfcCreated = upsertRfcs(bom, nsPath, bindingRel, userId);
						if (rfcCreated && bom.rfc != null 
								) {
							//if rfc was created, lets check if any propagation is required
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
				}
			}
			if (incOrder) realExecOrder++;
		}
		
		//lets create dependsOn Relations
		//TODO question should we propagate rel attrs
		int maxRfcExecOrder = getMaxRfcExecOrder(boms);
		
		maxExecOrder = (maxRfcExecOrder > 0) ? maxRfcExecOrder : maxExecOrder;
		//execute all dummmy updates in one last step
		//maxExecOrder++;
		List<CmsRfcRelation> existingDependsOnRels = cmRfcMrgProcessor.getDfDjRelations("bom.DependsOn", null, nsPath, null, null, null);
		Set<String> djRelGoids = new HashSet<String>();
		int dummyStep = maxExecOrder + 1;
		boolean increaseMaxOrder = false;
		for (BomLink link : links.values()) {
			if (bomMap.get(link.fromNodeId).rfc != null &&
					bomMap.get(link.toNodeId).rfc != null) {
				long fromCiId = bomMap.get(link.fromNodeId).rfc.getCiId();
				long toCiId = bomMap.get(link.toNodeId).rfc.getCiId();
				CmsRfcRelation dependsOn = bootstrapRelationRfc(fromCiId,toCiId,"bom.DependsOn", nsPath);
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
				dependsOn.setValidated(true);
				
				CmsRfcRelation newRel =	cmRfcMrgProcessor.upsertRfcRelationNoCheck(dependsOn, userId, "dj");
				djRelGoids.add(newRel.getRelationGoid());
				//if we got new relation lets update create dummy update rfcs
				if (newRel.getRfcId()>0) {
					if (bomMap.get(link.fromNodeId).rfc.getRfcId()==0) {
						cmRfcMrgProcessor.createDummyUpdateRfc(fromCiId, null, dummyStep, userId);
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
				}
			}
		}
		
		//Now create dummy updates for all the dependency-propagations needed
		if (propagations.size() > 0) {
				for (BomRfc bom : boms) {
					if (bom.rfc == null) {
						logger.info("rfc null for: " + bom.ciName);
						continue;
					}

					if (propagations.contains(bom.rfc.getCiId())) {
						CmsRfcCI rfc = bootstrapRfc(bom, nsPath);
						rfc.setCreatedBy(userId);
						rfc.setUpdatedBy(userId);
						rfc.setNsId(nsId);
						cmRfcMrgProcessor.createDummyUpdateRfc(rfc.getCiId(), null, bom.execOrder, userId);
					}
				}
		}

		if (!isPartial) {
			for (CmsRfcRelation existingRel : existingDependsOnRels) {
				if (!djRelGoids.contains(existingRel.getRelationGoid())
					&& bomCiIds.contains(existingRel.getFromCiId())	
					&& bomCiIds.contains(existingRel.getToCiId())) {
					cmRfcMrgProcessor.requestRelationDelete(existingRel.getCiRelationId(), userId);
				}
			}
		}
		if (increaseMaxOrder) maxExecOrder++;
		logger.info(">>>>>>> Total time taken by propagation in seconds: " + timeTakenByPropagation/1000);
		return maxExecOrder;
	}
	
	private void propagateUpdate(long bomCiId, long manifestId,
			Map<Long, List<String>> manifestPropagations, String userId, Set<Long> propagations) {
		List<String> targetManifestCiNames = manifestPropagations.get(manifestId);
		List<CmsCIRelation> rels  = cmProcessor.getAllCIRelations(bomCiId);// all bom relations for this bom ci
		
		for (String targetCiName : targetManifestCiNames) {
			for (CmsCIRelation rel : rels) {
				if (! rel.getRelationName().equals("bom.DependsOn")) {
					continue;
				}
				if (rel.getFromCi() != null) {
					String ciName = rel.getFromCi().getCiName();
					if (ciName != null && ciName.startsWith(targetCiName + "-")) {
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
						&&rel.getFromCiId() == manifestCiId && attrib.getDfValue().equalsIgnoreCase("to")) {
					//found 
					targetManifests.add(rel.getToCi().getCiName());
					mapPropagations(rel.getToCiId(), manifestPropagations);
				} else if (rel.getToCiId() > 0 
						&&rel.getToCiId() == manifestCiId && attrib.getDfValue().equalsIgnoreCase("from")) {
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

	private boolean upsertRfcs(BomRfc bom, String nsPath, CmsCIRelation bindingRel, String userId) {
		
		boolean rfcCreated = false;
		long nsId = trUtil.verifyAndCreateNS(nsPath);
		
		if (bom.mfstCi.getCiState().equalsIgnoreCase("pending_deletion")) {
			List<CmsRfcCI> cis2delete = cmRfcMrgProcessor.getDfDjCi(nsPath, "bom." + trUtil.getLongShortClazzName(bom.mfstCi.getCiClassName()), bom.ciName, "dj");
			if (cis2delete.size() > 0) {
				for (CmsRfcCI ci2delete : cis2delete) {
					bom.rfc = cmRfcMrgProcessor.requestCiDelete(ci2delete.getCiId(), userId, bom.execOrder);
					rfcCreated = bom.rfc.getRfcId() > 0;
				}
			} else {
				//if no boms lets see if we have some in other cloud
				if (cmProcessor.getCountFromCIRelationsByNS(bom.mfstCi.getCiId(),  "base.RealizedAs", null, null, nsPath, false) == 0) {
					cmProcessor.deleteCI(bom.mfstCi.getCiId(), true);
				}
			}
		} else {
			CmsRfcCI rfc = bootstrapRfc(bom, nsPath);
			rfc.setCreatedBy(userId);
			rfc.setUpdatedBy(userId);
			rfc.setNsId(nsId);
			bom.rfc = cmRfcMrgProcessor.upsertRfcCINoChecks(rfc, userId, "dj");
			
			rfcCreated = bom.rfc.getRfcId() > 0;
			
			if (bom.rfc.getRfcId() == 0) {
				//lets make sure the manifest object has not changed or we will create dummy update
				List<CmsCIRelation> realizedAsRels = cmProcessor.getFromToCIRelations(bom.mfstCi.getCiId(), "base.RealizedAs", bom.rfc.getCiId());
				if (realizedAsRels.size()>0 && realizedAsRels.get(0).getAttribute("last_manifest_rfc") != null) {
					CmsCIRelation realizedAsRel = realizedAsRels.get(0);
					long deployedManifestRfc = Long.valueOf(realizedAsRel.getAttribute("last_manifest_rfc").getDjValue());
					if (bom.mfstCi.getLastAppliedRfcId() > deployedManifestRfc) {
						bom.rfc = cmRfcMrgProcessor.createDummyUpdateRfc(bom.rfc.getCiId(), null, bom.execOrder, userId);
						rfcCreated = true;
					}
				}
			}

			//lets create RealizedAs relation
			
			Map<String,String> attrs = new HashMap<String,String>();
			attrs.put("last_manifest_rfc", String.valueOf(bom.mfstCi.getLastAppliedRfcId()));
			CmsRfcRelation realizedAs = bootstrapRelationRfcWithAttributes(bom.mfstCi.getCiId(), bom.rfc.getCiId(), "base.RealizedAs", nsPath, attrs);
			if (rfcCreated) {
				realizedAs.setToRfcId(bom.rfc.getRfcId());
			}
			realizedAs.setComments(generateRelComments(bom.mfstCi.getCiName(), bom.mfstCi.getCiClassName(), bom.rfc.getCiName(), bom.rfc.getCiClassName()));
			realizedAs.getAttribute("priority").setNewValue(bindingRel.getAttribute("priority").getDjValue());
			realizedAs.setCreatedBy(userId);
			realizedAs.setUpdatedBy(userId);
			realizedAs.setNsId(nsId);
			validateRelRfc(realizedAs, bom.mfstCi.getCiClassId(), bom.rfc.getCiClassId());
			cmRfcMrgProcessor.upsertRfcRelationNoCheck(realizedAs, userId, "dj");
			
			//lest create relation to the binding
			CmsRfcRelation deployedTo = bootstrapRelationRfc(bom.rfc.getCiId(), bindingRel.getToCiId(), "base.DeployedTo", nsPath);
			deployedTo.setComments(generateRelComments(bom.rfc.getCiName(), bom.rfc.getCiClassName(), bindingRel.getToCi().getCiName(), bindingRel.getToCi().getCiClassName()));
			deployedTo.setCreatedBy(userId);
			deployedTo.setUpdatedBy(userId);
			deployedTo.setNsId(nsId);
			validateRelRfc(deployedTo, bom.rfc.getCiClassId(), bindingRel.getToCi().getCiClassId());
			if (rfcCreated) {
				deployedTo.setFromRfcId(bom.rfc.getRfcId());
			}
			cmRfcMrgProcessor.upsertRfcRelationNoCheck(deployedTo, userId, "dj");
		}
		
		return rfcCreated;
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
				/*
				Map<String, List<String>> mfstId2nodeId = new HashMap<String,List<String>>();
				
				CmsCI startingPoint = mfstPlatComponents.get(0).getToCi(); 
				Map<String, Integer> namesMap = new HashMap<String, Integer>();
				Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels = new HashMap<Long,Map<String,List<CmsCIRelation>>>();
	
				while (startingPoint != null) {
					BomRfc newBom = bootstrapNewBom(startingPoint, namesMap, bindingRel.getToCiId(), 1);
					boms.add(newBom);	
					mfstId2nodeId.put(String.valueOf(newBom.manifestCiId) + "-" + 1, new ArrayList<String>(Arrays.asList(newBom.nodeId)));
					
					boms.addAll(processNode(newBom, namesMap, bindingRel.getToCiId(), mfstId2nodeId,manifestDependsOnRels, 1, false));
					startingPoint = getStartingPoint(mfstPlatComponents, boms);
				}
				maxExecOrder = createBomDeleteRfcsAndRels(boms, platNsPath, bindingRel, platformCi, startExecOrder, userId);
				*/
				logger.info(">>>>>>>>>>>>>" + platformCi.getCiName() + ", finding obsolete boms");
				maxExecOrder = findObsolete(new ArrayList<BomRfc>(), bindingRel, platNsPath, startExecOrder);
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
					cmProcessor.deleteCI(mfstPlatComponentRel.getToCiId(), true);
				}
				cmProcessor.deleteCI(platformCi.getCiId(), true);
				trUtil.deleteNs(platNsPath);
			}
			
		}
		return maxExecOrder;
	}

	private CmsRfcCI bootstrapRfc(BomRfc bom, String nsPath) {
		
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
	    setCiId(newRfc);
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
	private void processOrder(BomRfc bom, Map<String, BomRfc> bomMap, int order) {

		bom.execOrder = (order > bom.execOrder) ? order : bom.execOrder;
		order += 1;
		for (BomLink link : bom.toLinks) {
			BomRfc parentBom = bomMap.get(link.fromNodeId);
			processOrder(parentBom, bomMap, order);
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
	
	private void processEntryPointRel(long platformCiId, Map<Long, List<BomRfc>> bomsMap, String nsPath, String user) {
		List<CmsCIRelation> entryPoints = cmProcessor.getFromCIRelationsNaked(platformCiId, null, "Entrypoint", null);
		for (CmsCIRelation epRel : entryPoints) {
			if (bomsMap.containsKey(epRel.getToCiId())) {
				for (BomRfc bom : bomsMap.get(epRel.getToCiId())) {
					if (bom.rfc != null) {
					CmsRfcRelation entryPoint = bootstrapRelationRfc(platformCiId, bom.rfc.getCiId(),"base.Entrypoint", nsPath);
					cmRfcMrgProcessor.upsertRelationRfc(entryPoint, user, "dj");
					}
				}
			}
		}
	}

	private void processManagedViaRels(List<CmsCIRelation> mfstCiRels, Map<Long, List<BomRfc>> bomsMap, String nsPath, String user) {

		long nsId = trUtil.verifyAndCreateNS(nsPath);
		for (CmsCIRelation mfstCiRel : mfstCiRels) {
			CmsCI mfstCi = mfstCiRel.getToCi();
			//first lets check if we even have an add rfc for this Ci
			//if (newRfcExists(mfstCi.getCiId(), bomsMap)) {
			List<CmsCIRelation> mfstMngViaRels = cmProcessor.getFromCIRelationsNaked(mfstCi.getCiId(), null, "ManagedVia", null);
			for (CmsCIRelation mfstMngViaRel : mfstMngViaRels) {
				// lets find the path 
				//List<String> pathClasses = getTraversalPath(mfstMngViaRel);
				List<String> pathClasses = getDpOnPath(mfstMngViaRel.getFromCiId(), mfstMngViaRel.getToCiId());
				if (pathClasses.size()==0) {
					String err = "Can not traverse ManagedVia relation using DependsOn path from ci " + mfstMngViaRel.getFromCiId() + ", to ci " + mfstMngViaRel.getToCiId();
					logger.error(err);
					throw new TransistorException(CmsError.TRANSISTOR_CANNOT_TRAVERSE, err);
				}
				for (BomRfc bomRfc : bomsMap.get(mfstCi.getCiId())) {
					
					/*
					boolean fromCiAdded = bomRfc.rfc != null && bomRfc.rfc.getRfcId() > 0 && (bomRfc.rfc.getRfcAction().equalsIgnoreCase("add") || bomRfc.rfc.getRfcAction().equalsIgnoreCase("replace"));
					boolean toCiAdded = false;
					for (BomRfc toBomRfc : bomsMap.get(mfstMngViaRel.getToCiId())) {
						toCiAdded = toBomRfc.rfc != null && toBomRfc.rfc.getRfcId() > 0 && (toBomRfc.rfc.getRfcAction().equalsIgnoreCase("add") || toBomRfc.rfc.getRfcAction().equalsIgnoreCase("replace"));
					}
					*/
					
					//if (true) {
					
					//for this rfc we need to traverse by the DependsOn path down to ManagedVia Ci and create the relation\
					//Now this is tricky since it could get resolved as a tree so we need to use recursion
					LinkedList<String> path = new LinkedList<String>();
					path.addAll(pathClasses);
					if (bomRfc.rfc != null) {
						List<Long> targets = getLeafsByPath(bomRfc.rfc.getCiId(), path,mfstMngViaRel.getToCiId());
						for (long managedViaCiId : targets) {
							List<CmsCIRelation> existingRel = cmProcessor.getFromToCIRelationsNaked(bomRfc.rfc.getCiId(), "bom.ManagedVia", managedViaCiId);
							if (existingRel.size() == 0) {		
								CmsRfcRelation managedVia = bootstrapRelationRfc(bomRfc.rfc.getCiId(), managedViaCiId, "bom.ManagedVia", nsPath);
								managedVia.setNsId(nsId);
								managedVia.setReleaseId(bomRfc.rfc.getReleaseId());
								
								CmsRfcCI toCiRfc = cmRfcMrgProcessor.getCiById(managedViaCiId, null);
								
								managedVia.setComments(generateRelComments(bomRfc.rfc.getCiName(), bomRfc.rfc.getCiClassName(), toCiRfc.getCiName(), toCiRfc.getCiClassName()));
								
								if (bomRfc.rfc != null && bomRfc.rfc.getRfcId() > 0) {
									managedVia.setFromRfcId(bomRfc.rfc.getRfcId());
								}
								
								if (toCiRfc.getRfcId() > 0) {
									managedVia.setToRfcId(toCiRfc.getRfcId());
								}
								
								managedVia.setValidated(true);
								cmRfcMrgProcessor.upsertRfcRelationNoCheck(managedVia, user, "dj");
							}
						}
					}
					//}
				}
			}
		}
	};

	private void processSecuredByRels(List<CmsCIRelation> mfstCiRels, Map<Long, List<BomRfc>> bomsMap, String nsPath, String user) {
		
		long nsId = trUtil.verifyAndCreateNS(nsPath);
		
		for (CmsCIRelation mfstCiRel : mfstCiRels) {
			CmsCI mfstCi = mfstCiRel.getToCi();
			List<CmsCIRelation> mfstSecuredByRels = cmProcessor.getFromCIRelationsNaked(mfstCi.getCiId(), null, "SecuredBy", null);
			for (CmsCIRelation mfstSecuredByRel : mfstSecuredByRels) {
				for (BomRfc fromBomRfc : bomsMap.get(mfstCi.getCiId())) {
					for (BomRfc toBomRfc : bomsMap.get(mfstSecuredByRel.getToCiId())) {
						CmsRfcRelation securedBy = bootstrapRelationRfc(fromBomRfc.rfc.getCiId(), toBomRfc.rfc.getCiId(), "bom.SecuredBy", nsPath);
						
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
						cmRfcMrgProcessor.upsertRelationRfc(securedBy, user, "dj");
					}
				}
			}
		}
	};
	
	
	private List<Long> getLeafsByPath(long startCiId, LinkedList<String> path, long targetMfstCiId) {
		List<Long> listOfTargets = new ArrayList<Long>();
		if (path.size() == 0) {
			//we reached end of the path but seems like there are multiple routes, but at this point we are good
			return listOfTargets;
		}
		
		String nextMfstClass = path.poll();
		String bomClass = "bom." + trUtil.getLongShortClazzName(nextMfstClass);
		
		List<CmsRfcRelation> dependsOnRels = cmRfcMrgProcessor.getFromCIRelationsNakedNoAttrs(startCiId, null, "DependsOn", bomClass);
		if (path.size() ==0) {
			//this should be our target list
			for (CmsRfcRelation rel : dependsOnRels) {
				//lets check if this guy is related to the right mfstCi
				//TODO this could be not nessesary
				if (cmRfcMrgProcessor.getToCIRelationsNakedNoAttrs(rel.getToCiId(), null, "RealizedAs", nextMfstClass).size() >0) {
					listOfTargets.add(rel.getToCiId());
				}
			}
		} else {
			for (CmsRfcRelation rel : dependsOnRels) {
				listOfTargets.addAll(getLeafsByPath(rel.getToCiId(), new LinkedList<String>(path), targetMfstCiId));
			}	
		}
		return listOfTargets;
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

	private List<BomRfc> processNode(BomRfc node, Map<String, Integer> namesMap, long bindingId, Map<String, List<String>> mfstIdEdge2nodeId, Map<Long,Map<String,List<CmsCIRelation>>> manifestDependsOnRels, int edgeNum, boolean usePercent){

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
		
		logger.info("working on " + node.ciName);
		
		logger.info("got " + mfstFromRels.size() + " 'from' relations");
		logger.info("got " + mfstToRels.size() + " 'to' relations");
		
		for (CmsCIRelation fromRel : mfstFromRels) {
			int numEdges = 0;
			int percent = 100;
			if (usePercent && fromRel.getAttribute("pct_dpmt") != null) {	
				percent = Integer.valueOf(fromRel.getAttribute("pct_dpmt").getDjValue());
				numEdges = (int)Math.floor(Integer.valueOf(fromRel.getAttribute("current").getDfValue())*(percent/100.0)) ;
			} else {
				numEdges = Integer.valueOf(fromRel.getAttribute("current").getDfValue());
			}
			int edgeNumLocal = edgeNum;
			//special case if the relation marked as converge
			if (fromRel.getAttribute("converge") != null
				&& Boolean.valueOf(fromRel.getAttribute("converge").getDfValue())) {
				edgeNumLocal = 1;
				numEdges = 1;
			}
			String key = String.valueOf(fromRel.getToCi().getCiId()) + "-" + edgeNumLocal;
			
			if (!mfstIdEdge2nodeId.containsKey(key)
				|| 	numEdges > 1) {
				//for (int i=node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size()+1; i<=numEdges; i++) {
				for (int i=node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size() + 1 + ((edgeNumLocal-1) * numEdges); i<=numEdges + ((edgeNumLocal-1) * numEdges); i++) {	
					int newEdgeNum = (i > edgeNumLocal) ? i : edgeNumLocal;
					BomRfc newBom = bootstrapNewBom(fromRel.getToCi(), namesMap, bindingId, newEdgeNum);
					BomLink link = new BomLink();
					link.fromNodeId = node.nodeId;
					link.fromMfstCiId = node.manifestCiId;
					link.toNodeId = newBom.nodeId;
					link.toMfstCiId = newBom.manifestCiId;
					node.fromLinks.add(link);
					newBom.toLinks.add(link);
					newBoms.add(newBom);

					key = String.valueOf(newBom.manifestCiId)+ "-" + newEdgeNum;
					if (!mfstIdEdge2nodeId.containsKey(key)) mfstIdEdge2nodeId.put(key, new ArrayList<String>());
					mfstIdEdge2nodeId.get(key).add(newBom.nodeId);
					newBoms.addAll(processNode(newBom, namesMap, bindingId, mfstIdEdge2nodeId, manifestDependsOnRels, newEdgeNum, usePercent));
				}
			} else {
				for (String toNodeId : mfstIdEdge2nodeId.get(key)) {
					if (node.getExisitngFromLinks(fromRel.getToCi().getCiId()).size() == 0 ) {
						BomLink link = new BomLink();
						link.fromNodeId = node.nodeId;
						link.fromMfstCiId = node.manifestCiId;
						link.toNodeId = toNodeId;
						link.toMfstCiId = fromRel.getToCi().getCiId();
						node.fromLinks.add(link);
					}
				} 
			}
		}

		for (CmsCIRelation toRel : mfstToRels) {
			String key = String.valueOf(toRel.getFromCi().getCiId()) + "-" + edgeNum;
			
			if (!mfstIdEdge2nodeId.containsKey(key)) {
				
				mfstIdEdge2nodeId.put(key, new ArrayList<String>());

				if (node.getExisitngToLinks(toRel.getFromCi().getCiId()).size() == 0 ) {
					BomRfc newBom = bootstrapNewBom(toRel.getFromCi(), namesMap, bindingId, edgeNum);
					BomLink link = new BomLink();
					link.toNodeId = node.nodeId;
					link.toMfstCiId = node.manifestCiId;
					link.fromNodeId = newBom.nodeId;
					link.fromMfstCiId = newBom.manifestCiId;
					node.toLinks.add(link);
					newBom.fromLinks.add(link);
					newBoms.add(newBom);
					mfstIdEdge2nodeId.get(String.valueOf(newBom.manifestCiId)+ "-" + edgeNum).add(newBom.nodeId);
					newBoms.addAll(processNode(newBom, namesMap, bindingId, mfstIdEdge2nodeId, manifestDependsOnRels, edgeNum, usePercent));
				}
			} else {
				for (String fromNodeId : mfstIdEdge2nodeId.get(key)) {
					if (node.getExisitngToLinks(toRel.getFromCi().getCiId()).size() == 0 ) {
						BomLink link = new BomLink();
						link.toNodeId = node.nodeId;
						link.toMfstCiId = node.manifestCiId;
						link.fromNodeId = fromNodeId;
						link.fromMfstCiId = toRel.getFromCi().getCiId();
						node.toLinks.add(link);
					}
				}
			}
		}
		node.isProcessed = true;
		return newBoms;
	}
	
	
	
	private BomRfc bootstrapNewBom(CmsCI ci, Map<String, Integer> namesMap, long bindingId, int edgeNum) {
		BomRfc newBom = new BomRfc();
		newBom.manifestCiId = ci.getCiId();
		newBom.mfstCi = ci;
		newBom.ciName = getName(ci.getCiName(), namesMap, bindingId, edgeNum);
		newBom.nodeId = newBom.manifestCiId + newBom.ciName; 
		return newBom;
	}
	
	private String getName(String base, Map<String, Integer> namesMap, long bindingId, int edgeNum) {
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
	
	private void setCiId(CmsRfcCI rfc) {
		List<CmsRfcCI> existingRfcs = rfcProcessor.getOpenRfcCIByClazzAndNameNoAttrs(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName());
		if (existingRfcs.size()>0) {
			CmsRfcCI existingRfc = existingRfcs.get(0);
			rfc.setCiId(existingRfc.getCiId());
			rfc.setRfcId(existingRfc.getRfcId());
		} else {
			List<CmsCI> existingCis = cmProcessor.getCiBy3Naked(rfc.getNsPath(), rfc.getCiClassName(), rfc.getCiName());
			if (existingCis.size()>0) {
				CmsCI ci = existingCis.get(0);
				rfc.setCiId(ci.getCiId());
				rfc.setCiState(ci.getCiState());
			}
		}
	}
	
	private CmsRfcRelation bootstrapRelationRfc(long fromCiId, long toCiId, String relName, String nsPath) {
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
	    setCiRelationId(newRfc);
		return newRfc;
	}
	
	private CmsRfcRelation bootstrapRelationRfcWithAttributes(long fromCiId, long toCiId, String relName, String nsPath, Map<String,String> attrs) {
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
	    setCiRelationId(newRfc);
		return newRfc;
	}

	
	private void setCiRelationId(CmsRfcRelation rfc) {
		List<CmsRfcRelation> existingRfcs = rfcProcessor.getOpenRfcRelationBy2NoAttrs(rfc.getFromCiId(), rfc.getToCiId(), rfc.getRelationName(), null); 
		if (existingRfcs.size()>0) {
			CmsRfcRelation existingRfc = existingRfcs.get(0);
			rfc.setCiRelationId(existingRfc.getCiRelationId());
			rfc.setRfcId(existingRfc.getRfcId());
		} else {
			List<CmsCIRelation> existingRels = cmProcessor.getFromToCIRelationsNaked(rfc.getFromCiId(),rfc.getRelationName(), rfc.getToCiId());
			if (existingRels.size()>0) {
				CmsCIRelation rel = existingRels.get(0);
				rfc.setCiRelationId(rel.getCiRelationId());
			}
		}
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

}
