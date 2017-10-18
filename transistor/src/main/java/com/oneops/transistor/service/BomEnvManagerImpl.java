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

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsUtil;
import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.BASE_PROVIDES;


public class BomEnvManagerImpl implements BomEnvManager  {

	static Logger logger = Logger.getLogger(BomEnvManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsNsManager nsManager;
	private CmsRfcProcessor rfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcProcessor;
	private CmsDpmtProcessor dpmtProcessor;
	private CmsRfcUtil rfcUtil;
	private ExpressionParser exprParser;
	private CmsUtil cmsUtil;
	private BomManager bomManager;

	public void setCmRfcProcessor(CmsCmRfcMrgProcessor cmRfcProcessor) {
		this.cmRfcProcessor = cmRfcProcessor;
	}

	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setExprParser(ExpressionParser exprParser) {
		this.exprParser = exprParser;
	}

	public void setRfcUtil(CmsRfcUtil rfcUtil) {
		this.rfcUtil = rfcUtil;
	}

	public void setDpmtProcessor(CmsDpmtProcessor dpmtProcessor) {
		this.dpmtProcessor = dpmtProcessor;
	}
	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setNsManager(CmsNsManager nsManager) {
		this.nsManager = nsManager;
	}

	public void setBomManager(BomManager bomManager) {
		this.bomManager = bomManager;
	}
	
	@Override
	public void cleanEnvBom(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		nsManager.deleteNs(bomNsPath);
		logger.info("cleaned bom nspath: " + bomNsPath);
	}
	
	
	@Override
	public void takeEnvSnapshot(long envId) {
		// TODO Auto-generated method stub
	}

	@Override
	public long discardEnvBom(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		bomManager.check4openDeployment(bomNsPath);
		long bomReleaseId = 0;
		List<CmsRelease> bomReleases = rfcProcessor.getReleaseBy3(bomNsPath, null, "open");
		for (CmsRelease bomRel : bomReleases) {
			bomRel.setReleaseState("canceled");
			rfcProcessor.updateRelease(bomRel);
			bomReleaseId = bomRel.getReleaseId();
		}
		return bomReleaseId;
	}
	
	@Override
	public long discardEnvManifest(long envId, String userId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String manifestNsPath = env.getNsPath() + "/" + env.getCiName() + "/manifest";
		long manifestReleaseId = 0;
		List<CmsRelease> manifestReleases = rfcProcessor.getReleaseBy3(manifestNsPath, null, "open");
		for (CmsRelease manifestRel : manifestReleases) {
			manifestRel.setReleaseState("canceled");
			manifestRel.setCommitedBy(userId);
			rfcProcessor.updateRelease(manifestRel);
			env.setComments("");
			cmProcessor.updateCI(env);
			manifestReleaseId = manifestRel.getReleaseId();
		}
		return manifestReleaseId;
	}

	private class Triplet{
		private CmsCI ci;
		private CmsRfcCI rfc;
		private CmsCI cloud;

		Triplet(CmsCI cloud, CmsCI ci, CmsRfcCI rfc) {
			this.ci = ci;
			this.rfc = rfc;
			this.cloud = cloud;
		}

		Triplet(CmsCI cloud, CmsCI ci) {
			this(cloud, ci, null);
		}
	}
	
	@Override
	public List<CostData> getEstimatedCostData(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";
		
		// rfcs (adds and updates)
		List<CmsRfcRelation> rfcRelations = rfcProcessor.getOpenRfcRelationsNsLikeNakedNoAttrs(null, "DeployedTo", bomNsPath,null, "account.Cloud");
		Set<Long> cloudIds = rfcRelations.stream().map(CmsRfcRelationBasic::getToCiId).collect(Collectors.toSet());
		// existing cis 
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null,"DeployedTo", null, "account.Cloud", true, false);
		cloudIds.addAll(relations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet()));
		
		
		Map<Long, CmsCI> cloudMap = new HashMap<>();  // load all DeployedTo clouds
		for (Long cloudId: cloudIds) {
			cloudMap.put(cloudId, cmProcessor.getCiById(cloudId));
		}

		// load all offerings 
		Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(cloudMap.values()); 
		
		Map<Long, Triplet> deploymentMap = new HashMap<>();
		for (CmsRfcRelation rfcRelation: rfcRelations){
			deploymentMap.put(rfcRelation.getFromCiId(), new Triplet(cloudMap.get(rfcRelation.getToCiId()), cmProcessor.getCiById(rfcRelation.getFromCiId()), rfcProcessor.getRfcCIById(rfcRelation.getFromRfcId()))) ;
		}
		
		for (CmsCIRelation relation:relations){
			long ciId = relation.getFromCiId();
			if (!deploymentMap.containsKey(ciId)){
				deploymentMap.put(ciId, new Triplet(cloudMap.get(relation.getToCiId()), relation.getFromCi(), rfcProcessor.getOpenRfcCIByCiId(ciId))) ;
			}
		}
		return calculateCost(offeringsByNs, deploymentMap.values());
	}

	private List<CostData> calculateCost(Map<String, Map<String, List<CmsCI>>> offeringsByNs, Collection<Triplet> triplets) {
		List<CostData> costList = new ArrayList<>();
		
		for (Triplet triplet: triplets) {
			List<CmsCISimple> reqOfferings = new ArrayList<>();
			String costNs = getNs(triplet.cloud);
			if (triplet.rfc != null && "delete".equals(triplet.rfc.getRfcAction()))
				continue; // do not process deletes
			CmsRfcCI rfcCi = rfcUtil.mergeRfcAndCi(triplet.rfc, triplet.ci, null);
			
			List<CmsRfcRelation> realizedAsRels = cmRfcProcessor.getToCIRelationsNaked(triplet.ci==null?triplet.rfc.getCiId():triplet.ci.getCiId(), "base.RealizedAs", null, null);

			if (realizedAsRels.size() > 0) {
				CmsRfcRelation realizedRel = realizedAsRels.get(0);
				List<CmsCIRelation> requiresList = cmProcessor.getToCIRelationsNaked(realizedRel.getFromCiId(), "manifest.Requires", null);
				if (requiresList.size() > 0) {
					CmsCIRelation requiresRel = requiresList.get(0);
					CmsCIRelationAttribute servicesAttr = requiresRel.getAttribute("services");
					if (servicesAttr != null && servicesAttr.getDjValue() != null && servicesAttr.getDjValue().length() > 0) {
						String[] requiredServices = servicesAttr.getDjValue().split("[,\\*]");
						Map<String, List<CmsCI>> map = offeringsByNs.get(costNs);
						for (String service : requiredServices) {
							
							List<CmsCI> offerings = getEligibleOfferings(rfcCi, map, service);
							
							if (offerings!=null && !offerings.isEmpty()) {
								CmsCI offering = getLowestCostOffering(offerings);
								CmsCISimple ciSimple = cmsUtil.custCI2CISimple(offering, "dj");
								ciSimple.addCiAttribute("service_type", service);
								reqOfferings.add(ciSimple);
							}
						}
					}
				}
			}
			if (!reqOfferings.isEmpty()) {
				costList.add(new CostData(cmsUtil.custRfcCI2RfcCISimple(rfcCi), cmsUtil.custCI2CISimple(triplet.cloud, "dj"), reqOfferings));
			}
		}
		return costList;
	}

	private List<CmsCI> getEligibleOfferings(CmsRfcCI rfcCi, Map<String, List<CmsCI>> map, String service) {
		List<CmsCI> eligibleOfferings = new ArrayList<>();
		List<CmsCI> serviceOfferings = map.get(service);
		if (serviceOfferings==null) return null;
		for (CmsCI offering: serviceOfferings) {
            CmsCIAttribute criteriaAttribute = offering.getAttribute("criteria");
            String criteria = criteriaAttribute.getDfValue();
            if (isLikelyElasticExpression(criteria)) {
                criteria = convert(criteria);
            }
            Expression expression = exprParser.parseExpression(criteria);
            StandardEvaluationContext context = new StandardEvaluationContext();
            
            context.setRootObject(cmsUtil.custRfcCI2RfcCISimple(rfcCi));
            boolean match = expression.getValue(context, Boolean.class);
            if (match) {
                eligibleOfferings.add(offering);
            }
        }
		return eligibleOfferings;
	}


	private CmsCI getLowestCostOffering(List<CmsCI> offerings) {
		CmsCI lowestOffering = null;
		for (CmsCI offering : offerings) {
			if (lowestOffering == null) {
				lowestOffering = offering;
			} else if (Double.valueOf(offering.getAttribute("cost_rate").getDfValue()) < Double.valueOf(lowestOffering.getAttribute("cost_rate").getDfValue())) {
				lowestOffering = offering;
			}
		}
		return lowestOffering;
	}

	/**
	 * Returns map of cloudNs->ServiceName->List of offerings
	 * @param clouds
	 * @return
	 */
	private Map<String, Map<String, List<CmsCI>>> getOfferingsForClouds(Collection<CmsCI> clouds) {
		Map<String, Map<String, List<CmsCI>>> offeringsByNs = new HashMap<>();
		for (CmsCI cloud: clouds) {
			List<CmsCI> offerings =  cmProcessor.getCiBy3NsLike(getNs(cloud), "cloud.Offering", null);
			List<CmsCIRelation> serviceRels = cmProcessor.getFromCIRelations(cloud.getCiId(), BASE_PROVIDES, null);
			for (CmsCIRelation rel : serviceRels){
				CmsCIRelationAttribute attr = rel.getAttribute("service");
				if (attr!=null && attr.getDjValue()!=null){
					offeringsByNs.putIfAbsent(getNs(cloud), new HashMap<>());
					ArrayList<CmsCI> list = new ArrayList<>();
					for (Iterator<CmsCI> iterator = offerings.iterator(); iterator.hasNext(); ) {
						CmsCI offering = iterator.next();
						if (offering.getNsPath().endsWith(rel.getToCi().getCiClassName() + "/" + rel.getToCi().getCiName())) {
							list.add(offering);
							iterator.remove();
						}
					}
					if (!list.isEmpty()) {
						offeringsByNs.get(getNs(cloud)).put(attr.getDjValue(), list);
					}
				}
			}
		}
		return offeringsByNs;
	}

	@Override
	public List getCostData(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null,"DeployedTo", null, "account.Cloud", true, true);
		Set<CmsCI> clouds = relations.stream().map(CmsCIRelation::getToCi).collect(Collectors.toSet());

		Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(clouds);
		List<Triplet> triplets = new ArrayList<>();
		for (CmsCIRelation relation: relations) {
			triplets.add(new Triplet(relation.getToCi(), relation.getFromCi()));
		}
		return calculateCost(offeringsByNs, triplets);
	}

	private static String convert(String elasticExp) {
		return elasticExp.replace(":", "=='").replace("*.[1 TO *]", "[a-zA-Z0-9.]*").replace(".size", "['size']").replaceFirst("ciClassName==", "ciClassName matches ").replace(".Compute", ".Compute'").replace(".*Compute", ".*Compute'")+"'";
	}

	private static boolean isLikelyElasticExpression(String elasticExp) {
		return elasticExp.contains(":") || elasticExp.contains("ciAttribute.size");
	}

	private static String getNs(CmsCI ci) {
		return ci.getNsPath()+"/"+ci.getCiName();
	}
}
