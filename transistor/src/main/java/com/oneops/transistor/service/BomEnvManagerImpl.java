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

import com.oneops.capacity.CapacityEstimate;
import com.oneops.capacity.CapacityProcessor;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
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

import static com.oneops.cms.util.CmsConstants.*;
import static java.util.stream.Collectors.*;


public class BomEnvManagerImpl implements BomEnvManager  {

	static Logger logger = Logger.getLogger(BomEnvManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsNsManager nsManager;
	private CmsRfcProcessor rfcProcessor;
	private CmsRfcUtil rfcUtil;
	private ExpressionParser exprParser;
	private CmsUtil cmsUtil;
	private BomManager bomManager;
	private CapacityProcessor capacityProcessor;


	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	public void setExprParser(ExpressionParser exprParser) {
		this.exprParser = exprParser;
	}

	public void setRfcUtil(CmsRfcUtil rfcUtil) {
		this.rfcUtil = rfcUtil;
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

	public void setCapacityProcessor(CapacityProcessor capacityProcessor) {
		this.capacityProcessor = capacityProcessor;
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


	public Map<String, List<CostData>> getEnvCostData(long envId, boolean includeEstimated, BomData bomData) {
		CmsCI env = cmProcessor.getCiById(envId);
		String manifestNsPath = getNs(env) + "/manifest";

		long t = System.currentTimeMillis();
		Set<Long> cloudIds = cmProcessor.getCountCIRelationsGroupByToCiId(BASE_CONSUMES, null, null, manifestNsPath).keySet();
		Map<Long, CmsCI> cloudMap = cmProcessor.getCiByIdList(new ArrayList<>(cloudIds)).stream()
				.collect(toMap(CmsCI::getCiId, Function.identity()));
		logger.info("CloudMap: " + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		Map<String, Map<String, List<CmsCI>>> offeringsByNs = getOfferingsForClouds(cloudMap.values());
		Set<String> servicesWithOfferings = offeringsByNs.values().stream()
				.flatMap(m -> m.keySet().stream())
				.distinct()
				.collect(toSet());
		logger.info("getOfferingsForClouds: " + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		List<CmsCIRelation> requiresRels = cmProcessor.getCIRelationsNsLikeNaked(manifestNsPath, "manifest.Requires", null, null, null);
		Map<Long, List<String>> manifestIdToServicesMap = requiresRels.stream()
				.collect(HashMap::new,
						(map, r) -> {
							CmsCIRelationAttribute servicesAttr = r.getAttribute("services");
							if (servicesAttr != null) {
								String services = servicesAttr.getDjValue();
								if (services != null && services.length() > 0) {
									map.put(r.getToCiId(),
											Arrays.stream(services.split(",\\**"))
													.filter(servicesWithOfferings::contains)
													.collect(toList()));
								}
							}
						},
						HashMap::putAll);

		logger.info("requiresRels (" + manifestNsPath + "):" + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		List<Long> manifestCiIds = new ArrayList<>(manifestIdToServicesMap.keySet());
		Map<Long, Long> bomIdToManifestIdMap = cmProcessor.getCIRelationsByFromCiIdsNakedNoAttrs(BASE_REALIZED_AS, null, manifestCiIds).stream()
				.collect(toMap(CmsCIRelation::getToCiId, CmsCIRelation::getFromCiId));
		logger.info("cmProcessor - realizedAz (" + manifestCiIds.size() + "):" + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();
		if (includeEstimated) {
			if (bomData == null) {
				rfcProcessor.getOpenRfcRelationsByCiIdsNakedNoAttrs(BASE_REALIZED_AS, null, manifestCiIds,null)
						.forEach(r -> bomIdToManifestIdMap.put(r.getToCiId(), r.getFromCiId()));
			} else {
				bomData.getRelations().stream()
						.filter(r -> BASE_REALIZED_AS.equals(r.getRelationName()) && manifestIdToServicesMap.containsKey(r.getFromCiId()))
						.forEach(r -> bomIdToManifestIdMap.put(r.getToCiId(), r.getFromCiId()));
			}
		}
		logger.info("rfcProcessor - realizedAz (" + manifestCiIds.size() + "):" + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();

		List<Long> bomCiIds = new ArrayList<>(bomIdToManifestIdMap.keySet());
		Map<Long, CmsCI> bomCiIdToCloudMap = cmProcessor.getCIRelationsByFromCiIdsNakedNoAttrs(BASE_DEPLOYED_TO, null, bomCiIds).stream()
				.collect(toMap(CmsCIRelation::getFromCiId, r -> cloudMap.get(r.getToCiId())));
		Map<Long, CmsCI> bomCis = cmProcessor.getCiByIdList(bomCiIds).stream()
				.collect(toMap(CmsCI::getCiId, Function.identity()));
		logger.info("cmProcessor - deployedTo + boms (" + bomCiIds.size() + "):" + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();

		Map<Long, CmsRfcCI> bomRfcs;
		if (includeEstimated) {
			if (bomData == null) {
				rfcProcessor.getOpenRfcRelationsByCiIdsNakedNoAttrs(BASE_DEPLOYED_TO, null, bomCiIds, null)
						.forEach(r -> bomCiIdToCloudMap.put(r.getFromCiId(), cloudMap.get(r.getToCiId())));
				bomRfcs = rfcProcessor.getOpenRfcCIByCiIdList(bomCiIds).stream()
						.collect(toMap(CmsRfcCI::getCiId, Function.identity()));
			} else {
				bomData.getRelations().stream()
						.filter(r -> BASE_DEPLOYED_TO.equals(r.getRelationName()) && bomIdToManifestIdMap.containsKey(r.getFromCiId()))
						.forEach(r -> bomCiIdToCloudMap.put(r.getFromCiId(), cloudMap.get(r.getToCiId())));
				bomRfcs = bomData.getCis().stream()
						.collect(toMap(CmsRfcCI::getCiId, Function.identity()));
			}
		} else {
			bomRfcs = new HashMap<>();
		}
		logger.info("rfcProcessor - deployedTo + boms (" + bomCiIds.size() + "):" + (System.currentTimeMillis() - t));
		t = System.currentTimeMillis();

		Map<String, Expression> expressionCache = new HashMap<>();
		List<CostData> actual = new ArrayList<>();
		List<CostData> estimated = new ArrayList<>();
		for (Long ciId : bomCiIds) {
			CmsCI cloud = bomCiIdToCloudMap.get(ciId);
			CmsCI ci = bomCis.get(ciId);
			CmsRfcCI rfc = bomRfcs.get(ciId);
			if (cloud == null || (ci == null && rfc == null)) continue;   // should not happen

			CmsRfcCI actualRfcCi = ci == null ? null : rfcUtil.mergeRfcAndCi(null, ci, "df");
			CmsRfcCI estimatedRfcCi = rfc == null ? null : rfcUtil.mergeRfcAndCi(rfc, ci, "df");

			List<CmsCISimple> actualOfferings = new ArrayList<>();
			List<CmsCISimple> estimatedOfferings = new ArrayList<>();
			Map<String, List<CmsCI>> cloudOfferings = offeringsByNs.get(getNs(cloud));
			for (String service : manifestIdToServicesMap.get(bomIdToManifestIdMap.get(ciId))) {
				CmsCISimple actualOffering = matchOfferings(actualRfcCi, service, cloudOfferings, expressionCache);
				if (actualOffering != null) actualOfferings.add(actualOffering);

				CmsCISimple estimatedOffering = null;
				if (estimatedRfcCi == null) {
					// No pending changes for this bom CI, so estimated will be the same as actual.
					estimatedOffering = actualOffering;
				} else if (!"delete".equals(estimatedRfcCi.getRfcAction())) {
					// Do not process deletes.
					estimatedOffering = matchOfferings(estimatedRfcCi, service, cloudOfferings, expressionCache);
				}
				if (estimatedOffering != null) estimatedOfferings.add(estimatedOffering);
			}

			if (!actualOfferings.isEmpty()) actual.add(new CostData(cmsUtil.custRfcCI2RfcCISimple(actualRfcCi), cmsUtil.custCI2CISimple(cloud, "df"), actualOfferings));
			if (!estimatedOfferings.isEmpty()) estimated.add(new CostData(cmsUtil.custRfcCI2RfcCISimple(estimatedRfcCi == null ? actualRfcCi : estimatedRfcCi), cmsUtil.custCI2CISimple(cloud, "df"), estimatedOfferings));
		}

		Map<String, List<CostData>> result = new HashMap<>();
		result.put("actual", actual);
		if (includeEstimated) {
			result.put("estimated", estimated);
		}
		logger.info("expression running (" + bomCiIds.size() + "):" + (System.currentTimeMillis() - t));
		return result;
	}



	@Override
	public Map<String, Map<String, Integer>> getEnvCapacity(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";

		List<CmsCI> cis = cmProcessor.getCiBy3NsLike(bomNsPath, null, null);
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, BASE_DEPLOYED_TO, null, null, null);
		return capacityProcessor.calculateCapacity(cis, relations);
	}

	@Override
	public Map<String, Integer> getCloudCapacity(long cloudId) {
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsByToCiIdsNakedNoAttrs(BASE_DEPLOYED_TO, null, Collections.singletonList(cloudId));
		List<CmsCI> cis = cmProcessor.getCiByIdList(relations.parallelStream().map(CmsCIRelation::getFromCiId).collect(toList()));
		Map<String, Map<String, Integer>> capacity = capacityProcessor.calculateCapacity(cis, relations);
		return capacity == null || capacity.size() == 0 ? new HashMap<>() : capacity.values().iterator().next();
	}

	@Override
	public CapacityEstimate estimateDeploymentCapacity(BomData bomData) {
		CmsRelease release = bomData.getRelease();
		Collection<CmsRfcCI> cis = bomData.getCis();
		if (release == null || cis == null || cis.isEmpty()) {
			return new CapacityEstimate(null, null, "ok");
		} else {
			Collection<CmsRfcRelation> deployedToRelations = bomData.getRelations().stream().filter(r -> r.getRelationName().equals("base.DeployedTo")).collect(toList());
			return capacityProcessor.estimateCapacity(release.getNsPath(), cis, deployedToRelations);
		}
	}

	private CmsCISimple matchOfferings(CmsRfcCI rfcCi, String service, Map<String, List<CmsCI>> offeringsByService, Map<String, Expression> expressionCache) {
		if (rfcCi == null) return null;
		List<CmsCI> serviceOfferings = offeringsByService.get(service);
		List<CmsCI> matchedOfferings = getEligibleOfferings(rfcCi, serviceOfferings, expressionCache);
		if (matchedOfferings == null || matchedOfferings.isEmpty()) return null;
		CmsCI lowestOffering = matchedOfferings.stream()
				.min(Comparator.comparing(o -> Double.valueOf(o.getAttribute("cost_rate").getDfValue())))
				.orElse(matchedOfferings.get(0));
		CmsCISimple offering = cmsUtil.custCI2CISimple(lowestOffering, "df");
		offering.addCiAttribute("service_type", service);
		return offering;
	}

	private List<CmsCI> getEligibleOfferings(CmsRfcCI rfcCi, List<CmsCI> serviceOfferings, Map<String, Expression> expressionCache) {
		if (serviceOfferings == null || serviceOfferings.isEmpty()) return null;
		List<CmsCI> eligibleOfferings = new ArrayList<>();
		for (CmsCI offering : serviceOfferings) {
			CmsCIAttribute criteriaAttribute = offering.getAttribute("criteria");
			String criteria = criteriaAttribute.getDfValue();
			Expression expression = expressionCache.get(criteria);
			if (expression == null) {
				expression = exprParser.parseExpression(isLikelyElasticExpression(criteria) ? convertExpression(criteria) : criteria);
				expressionCache.put(criteria, expression);
			}

			boolean match = expression.getValue(new StandardEvaluationContext(cmsUtil.custRfcCI2RfcCISimple(rfcCi)), Boolean.class);
			if (match) {
				eligibleOfferings.add(offering);
			}
		}
		return eligibleOfferings;
	}


	private Map<String, Map<String, List<CmsCI>>> getOfferingsForClouds(Collection<CmsCI> clouds) {
		Map<String, Map<String, List<CmsCI>>> offeringsByNs = new HashMap<>();
		for (CmsCI cloud : clouds) {
			String cloudNs = getNs(cloud);
			List<CmsCI> offerings = cmProcessor.getCiBy3NsLike(cloudNs, "cloud.Offering", null);
			List<CmsCIRelation> serviceRels = cmProcessor.getFromCIRelations(cloud.getCiId(), BASE_PROVIDES, null);
			for (CmsCIRelation rel : serviceRels) {
				CmsCIRelationAttribute attr = rel.getAttribute("service");
				if (attr != null && attr.getDfValue() != null) {
					offeringsByNs.putIfAbsent(cloudNs, new HashMap<>());
					ArrayList<CmsCI> list = new ArrayList<>();
					for (Iterator<CmsCI> iterator = offerings.iterator(); iterator.hasNext(); ) {
						CmsCI offering = iterator.next();
						if (offering.getNsPath().endsWith(rel.getToCi().getCiClassName() + "/" + rel.getToCi().getCiName())) {
							list.add(offering);
							iterator.remove();
						}
					}
					if (!list.isEmpty()) {
						offeringsByNs.get(cloudNs).put(attr.getDjValue(), list);
					}
				}
			}
		}
		return offeringsByNs;
	}


	private static String convertExpression(String elasticExp) {
		return elasticExp.replace(":", "=='").replace("*.[1 TO *]", "[a-zA-Z0-9.]*").replace(".size", "['size']").replaceFirst("ciClassName==", "ciClassName matches ").replace(".Compute", ".Compute'").replace(".*Compute", ".*Compute'")+"'";
	}

	private static boolean isLikelyElasticExpression(String elasticExp) {
		return elasticExp.contains(":") || elasticExp.contains("ciAttribute.size");
	}

	private static String getNs(CmsCI ci) {
		return ci.getNsPath()+"/"+ci.getCiName();
	}
}
