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
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.ns.service.CmsNsManager;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.service.peristenceless.BomData;
import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.*;
import java.util.function.Function;
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

	private class Triplet {
		private CmsCI cloud;
		private Long ciId;
		private CmsRfcCI rfc;
		private String services;

		Triplet(CmsCI cloud, Long ciId) {
			this.cloud = cloud;
			this.ciId = ciId;
		}

		Triplet(CmsCI cloud, Long ciId, CmsRfcCI rfc) {
			this(cloud, ciId);
			this.rfc = rfc;
		}
	}

	@Override
	public List getEnvCostData(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null, "DeployedTo", null, "account.Cloud", false, false);

		Set<Long> cloudIds = relations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet());
		Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(new ArrayList<>(cloudIds)).stream()
				.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));

		Collection<Triplet> triplets = relations.stream()
				.map(relation -> new Triplet(cloudMap.get(relation.getToCiId()), relation.getFromCiId()))
				.collect(Collectors.toList());
		
		
		
		Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(cloudMap.values());

		Map<Long, Long> relmap = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, "base.RealizedAs", null, null, null).stream()
				.collect(Collectors.toMap(CmsCIRelation::getToCiId, CmsCIRelation::getFromCiId));

		return calculateCost(prefilter(bomNsPath, triplets, offeringsByNs, relmap), offeringsByNs);
	}


	@Override
	public Map<String, List<CapacityData>> getEnvCapacity(long envId, BomData data) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";


		// rfcs (adds and updates)
		List<CmsRfcRelation> rfcRelations = new ArrayList<>();
		for (CmsRfcRelation rel : data.getRelations()) {
			if (rel.getRelationName().endsWith(".DeployedTo") && rel.getNsPath().startsWith(bomNsPath)) {
				rfcRelations.add(rel);
			}
		}

		Set<Long> cloudIds = rfcRelations.stream().map(CmsRfcRelationBasic::getToCiId).collect(Collectors.toSet());
		// existing cis


		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null, "DeployedTo", null, "account.Cloud", false, false);
		cloudIds.addAll(relations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet()));

		Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(new ArrayList<>(cloudIds)).stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));
		Map<Long, CmsRfcCI> ciIdMap= data.getCis().stream().collect(Collectors.toMap(CmsRfcCI::getRfcId, Function.identity()));

		Map<Long, Triplet> deploymentMap = new HashMap<>();
		for (CmsRfcRelation rfcRelation : rfcRelations) {
			deploymentMap.put(rfcRelation.getFromCiId(), new Triplet(cloudMap.get(rfcRelation.getToCiId()), rfcRelation.getFromCiId(), ciIdMap.get(rfcRelation.getFromRfcId())));
		}

		for (CmsCIRelation relation : relations) {
			long ciId = relation.getFromCiId();
			if (!deploymentMap.containsKey(ciId)) {
				deploymentMap.put(ciId, new Triplet(cloudMap.get(relation.getToCiId()), ciId, null));
			}
		}
		Collection<Triplet> triplets = deploymentMap.values();

		Map<Long, Long> ciMap = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, "base.RealizedAs", null, null, null).stream()
				.collect(Collectors.toMap(CmsCIRelation::getToCiId, CmsCIRelation::getFromCiId));




		List<CmsRfcRelation> openRfcRelationsNsLikeNakedNoAttrs = new ArrayList<>();
		for (CmsRfcRelation rel : data.getRelations()) {
			if (rel.getRelationName().equals("base.RealizedAs") && rel.getNsPath().startsWith(bomNsPath)) {
				openRfcRelationsNsLikeNakedNoAttrs.add(rel);
			}
		}
		Map<Long, Long> relmap = openRfcRelationsNsLikeNakedNoAttrs.stream()
				.collect(Collectors.toMap(CmsRfcRelation::getToCiId, CmsRfcRelation::getFromCiId));

		relmap.putAll(ciMap);

		Map<String, List<CapacityData>> result = new HashMap<>();
		result.put("actual", calculateCapacity(prefilter(bomNsPath, triplets, ciMap)));
		result.put("estimated", calculateCapacity(prefilter(bomNsPath, triplets, relmap)));
		return result;
	}


	private List<CapacityData> calculateCapacity(Collection<Triplet> triplets) {

		List<Long> ciIds = triplets.stream().map(t -> t.ciId).collect(Collectors.toList());
		Map<Long, CmsCI> ciMap = cmProcessor.getCiByIdList(ciIds).stream()
				.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));

		return triplets.stream()
				.filter(triplet -> triplet.services != null)
				.map(triplet -> {
					//Map<String, List<CmsCI>> map = offeringsByNs.get(getNs(triplet.cloud));
					List<CmsCISimple> reqOfferings = new ArrayList<>();
					CmsRfcCI rfcCi = rfcUtil.mergeRfcAndCi(triplet.rfc, ciMap.get(triplet.ciId), null);
					//String[] requiredServices = triplet.services.split("[,\\*]");
					return rfcCi.getCiClassName().endsWith(".Compute") ? new CapacityData(cmsUtil.custRfcCI2RfcCISimple(rfcCi), cmsUtil.custCI2CISimple(triplet.cloud, "dj")) : null;
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}



	private Collection<Triplet> prefilter(String bomNsPath, Collection<Triplet> triplets, Map<Long, Long> relmap) {


		List<CmsCIRelation> ciRealized = cmProcessor.getCIRelationsNsLikeNaked(convertBomNsToManifestNs(bomNsPath), "manifest.Requires", null, null, null);
		Map<Long, CmsCIRelationAttribute> servicesMap = ciRealized.stream().collect(Collectors.toMap(CmsCIRelation::getToCiId, rel -> rel.getAttribute("services")));

		triplets = triplets.stream().filter(t -> {
			if (t.rfc == null || !"delete".equals(t.rfc.getRfcAction())) { // do not process deletes
				Long realizedFrom = relmap.get(t.ciId);
				if (realizedFrom != null) {
					CmsCIRelationAttribute servicesAttr = servicesMap.get(realizedFrom);
					if (servicesAttr != null) {
						String services = servicesAttr.getDjValue();
						if (services != null && services.length() > 0) {
							String[] requiredServices = services.split("[,\\*]");
							for (String service : requiredServices) {
								if (service.equals("compute")) {
									t.services = services;
									return true;
								}
							}
						}
					}
				}
			}
			return false;
		}).collect(Collectors.toList());
		return triplets;
	}

	@Override
    public Map<String, List<CostData>> getEnvEstimatedCostData(long envId, BomData data) {
        CmsCI env = cmProcessor.getCiById(envId);
        String bomNsPath = getNs(env) + "/bom";


        // rfcs (adds and updates)
        //	List<CmsRfcRelation> rfcRelations = rfcProcessor.getOpenRfcRelationsNsLikeNakedNoAttrs(null, "DeployedTo", bomNsPath, null, "account.Cloud");
        List<CmsRfcRelation> rfcRelations = new ArrayList<>();
        for (CmsRfcRelation rel : data.getRelations()) {
            if (rel.getRelationName().endsWith(".DeployedTo") && rel.getNsPath().startsWith(bomNsPath)) {
                rfcRelations.add(rel);
            }
        }
        
        Set<Long> cloudIds = rfcRelations.stream().map(CmsRfcRelationBasic::getToCiId).collect(Collectors.toSet());
        // existing cis


        List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null, "DeployedTo", null, "account.Cloud", false, false);
        cloudIds.addAll(relations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet()));

        Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(new ArrayList<>(cloudIds)).stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));


        // load all offerings 
//        List<Long> ciIds = rfcRelations.stream().map(CmsRfcRelationBasic::getFromCiId).collect(Collectors.toList());
//        
        
        
        Map<Long, CmsRfcCI> ciIdMap= data.getCis().stream().collect(Collectors.toMap(CmsRfcCI::getRfcId, Function.identity()));
        
        Map<Long, Triplet> deploymentMap = new HashMap<>();
        for (CmsRfcRelation rfcRelation : rfcRelations) {
            deploymentMap.put(rfcRelation.getFromCiId(), new Triplet(cloudMap.get(rfcRelation.getToCiId()), rfcRelation.getFromCiId(), ciIdMap.get(rfcRelation.getFromRfcId())));
        }

        for (CmsCIRelation relation : relations) {
            long ciId = relation.getFromCiId();
            if (!deploymentMap.containsKey(ciId)) {
                deploymentMap.put(ciId, new Triplet(cloudMap.get(relation.getToCiId()), ciId, null));
            }
        }
        Collection<Triplet> triplets = deploymentMap.values();

        Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(cloudMap.values());
        Map<Long, Long> ciMap = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, "base.RealizedAs", null, null, null).stream()
                .collect(Collectors.toMap(CmsCIRelation::getToCiId, CmsCIRelation::getFromCiId));



        
        //List<CmsRfcRelation> openRfcRelationsNsLikeNakedNoAttrs = rfcProcessor.getOpenRfcRelationsNsLikeNakedNoAttrs("base.RealizedAs", null, bomNsPath, null, null);
        List<CmsRfcRelation> openRfcRelationsNsLikeNakedNoAttrs = new ArrayList<>();
        for (CmsRfcRelation rel : data.getRelations()) {
            if (rel.getRelationName().equals("base.RealizedAs") && rel.getNsPath().startsWith(bomNsPath)) {
                openRfcRelationsNsLikeNakedNoAttrs.add(rel);
            }
        }
        Map<Long, Long> relmap = openRfcRelationsNsLikeNakedNoAttrs.stream()
                .collect(Collectors.toMap(CmsRfcRelation::getToCiId, CmsRfcRelation::getFromCiId));

        relmap.putAll(ciMap);

        Map<String, List<CostData>> result = new HashMap<>();
        result.put("actual", calculateCost(prefilter(bomNsPath, triplets, offeringsByNs, ciMap), offeringsByNs));
        result.put("estimated", calculateCost(prefilter(bomNsPath, triplets, offeringsByNs, relmap), offeringsByNs));
        return result;
    }

	@Override
	public Map<String, List<CostData>> getEnvEstimatedCostData(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";

		// rfcs (adds and updates)
		List<CmsRfcRelation> rfcRelations = rfcProcessor.getOpenRfcRelationsNsLikeNakedNoAttrs(null, "DeployedTo", bomNsPath, null, null);
		Set<Long> cloudIds = rfcRelations.stream().map(CmsRfcRelationBasic::getToCiId).collect(Collectors.toSet());
		// existing cis
		
		
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null, "DeployedTo", null, "account.Cloud", false, false);
		cloudIds.addAll(relations.stream().map(CmsCIRelation::getToCiId).collect(Collectors.toSet()));

		Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(new ArrayList<>(cloudIds)).stream().collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));


		// load all offerings 
		List<Long> ciIds = rfcRelations.stream().map(CmsRfcRelationBasic::getFromCiId).collect(Collectors.toList());
		Map<Long, CmsRfcCI> map = rfcProcessor.getOpenRfcCIByCiIdList(ciIds).stream().collect(Collectors.toMap(CmsRfcCI::getRfcId, Function.identity()));

		Map<Long, Triplet> deploymentMap = new HashMap<>();
		for (CmsRfcRelation rfcRelation : rfcRelations) {
			deploymentMap.put(rfcRelation.getFromCiId(), new Triplet(cloudMap.get(rfcRelation.getToCiId()), rfcRelation.getFromCiId(), map.get(rfcRelation.getFromRfcId())));
		}

		for (CmsCIRelation relation : relations) {
			long ciId = relation.getFromCiId();
			if (!deploymentMap.containsKey(ciId)) {
				deploymentMap.put(ciId, new Triplet(cloudMap.get(relation.getToCiId()), ciId, null));
			}
		}
		Collection<Triplet> triplets = deploymentMap.values();

		Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(cloudMap.values());
		Map<Long, Long> ciMap = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, "base.RealizedAs", null, null, null).stream()
				.collect(Collectors.toMap(CmsCIRelation::getToCiId, CmsCIRelation::getFromCiId));


		Map<Long, Long> relmap = rfcProcessor.getOpenRfcRelationsNsLikeNakedNoAttrs("base.RealizedAs", null, bomNsPath, null, null).stream()
				.collect(Collectors.toMap(CmsRfcRelation::getToCiId, CmsRfcRelation::getFromCiId));

		relmap.putAll(ciMap);
		
		Map<String, List<CostData>> result = new HashMap<>();
		result.put("actual", calculateCost(prefilter(bomNsPath, triplets, offeringsByNs, ciMap), offeringsByNs));
		result.put("estimated", calculateCost(prefilter(bomNsPath, triplets, offeringsByNs, relmap), offeringsByNs));
		return result;
	}

	private List<CostData> calculateCost(Collection<Triplet> triplets, Map<String, Map<String, List<CmsCI>>> offeringsByNs) {
		
		List<Long> ciIds = triplets.stream().map(t -> t.ciId).collect(Collectors.toList());
		Map<Long, CmsCI> ciMap = cmProcessor.getCiByIdList(ciIds).stream()
				.collect(Collectors.toMap(CmsCI::getCiId, Function.identity()));

		return triplets.stream()
				.filter(triplet -> triplet.services != null)
				.map(triplet -> {
					Map<String, List<CmsCI>> map = offeringsByNs.get(getNs(triplet.cloud));
					List<CmsCISimple> reqOfferings = new ArrayList<>();
					CmsRfcCI rfcCi = rfcUtil.mergeRfcAndCi(triplet.rfc, ciMap.get(triplet.ciId), null);
					String[] requiredServices = triplet.services.split("[,\\*]");
					for (String service : requiredServices) {
						List<CmsCI> availableOfferings = map.get(service);
						if (availableOfferings == null || availableOfferings.isEmpty())
							continue;
						List<CmsCI> offerings = getEligibleOfferings(rfcCi, availableOfferings);

						if (offerings != null && !offerings.isEmpty()) {
							CmsCI offering = getLowestCostOffering(offerings);
							CmsCISimple ciSimple = cmsUtil.custCI2CISimple(offering, "dj");
							ciSimple.addCiAttribute("service_type", service);
							reqOfferings.add(ciSimple);
						}
					}
					return reqOfferings.isEmpty() ? null : new CostData(cmsUtil.custRfcCI2RfcCISimple(rfcCi), cmsUtil.custCI2CISimple(triplet.cloud, "dj"), reqOfferings);
				}).filter(Objects::nonNull).collect(Collectors.toList());
	}

	private Collection<Triplet> prefilter(String bomNsPath, Collection<Triplet> triplets, Map<String, Map<String, List<CmsCI>>> offeringsByNs, Map<Long, Long> relmap) {
	

		List<CmsCIRelation> ciRealized = cmProcessor.getCIRelationsNsLikeNaked(convertBomNsToManifestNs(bomNsPath), "manifest.Requires", null, null, null);
		Map<Long, CmsCIRelationAttribute> servicesMap = ciRealized.stream().collect(Collectors.toMap(CmsCIRelation::getToCiId, rel -> rel.getAttribute("services")));

		triplets = triplets.stream().filter(t -> {
			if (t.rfc == null || !"delete".equals(t.rfc.getRfcAction())) { // do not process deletes
				Long realizedFrom = relmap.get(t.ciId);
				if (realizedFrom != null) {
					CmsCIRelationAttribute servicesAttr = servicesMap.get(realizedFrom);
					if (servicesAttr != null) {
						String services = servicesAttr.getDjValue();
						if (services != null && services.length() > 0) {
							Map<String, List<CmsCI>> map = offeringsByNs.get(getNs(t.cloud));
							String[] requiredServices = services.split("[,\\*]");
							for (String service : requiredServices) {
								List<CmsCI> availableOfferings = map.get(service);
							    if (availableOfferings != null && availableOfferings.size() > 0)
								t.services = services;
								return true;
							}
						}
					}
				}
			}
			return false;
		}).collect(Collectors.toList());
		return triplets;
	}

	private String convertBomNsToManifestNs(String bomNsPath) {
		return bomNsPath.substring(0, bomNsPath.length() - 4) + "/manifest";
	}

	private List<CmsCI> getEligibleOfferings(CmsRfcCI rfcCi, List<CmsCI> serviceOfferings) {
		if (serviceOfferings == null) return null;
		List<CmsCI> eligibleOfferings = new ArrayList<>();
		for (CmsCI offering : serviceOfferings) {
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
		for (CmsCI cloud : clouds) {
			List<CmsCI> offerings = cmProcessor.getCiBy3NsLike(getNs(cloud), "cloud.Offering", null);
			List<CmsCIRelation> serviceRels = cmProcessor.getFromCIRelations(cloud.getCiId(), BASE_PROVIDES, null);
			for (CmsCIRelation rel : serviceRels) {
				CmsCIRelationAttribute attr = rel.getAttribute("service");
				if (attr != null && attr.getDjValue() != null) {
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
