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

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsRfcRelationBasic;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.util.CmsUtil;
import org.apache.log4j.Logger;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.service.CmsDpmtProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.ns.service.CmsNsManager;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;


public class BomEnvManagerImpl implements BomEnvManager  {

	static Logger logger = Logger.getLogger(BomEnvManagerImpl.class);

	private CmsCmProcessor cmProcessor;
	private CmsNsManager nsManager;
	private CmsRfcProcessor rfcProcessor;
	private CmsDpmtProcessor dpmtProcessor;
	private CmsRfcUtil rfcUtil;
	private ExpressionParser exprParser;
	private CmsUtil cmsUtil;
	private BomManager bomManager;

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
	public Map<String, BigDecimal> calculateEstimatedCost(long envId) {
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

		// collect all deployedTo cloud namespaces
		Set<String> cloudNs = cloudMap.values().stream().map(BomEnvManagerImpl::getNs).collect(Collectors.toSet());
		
		// load all offerings 
		Map<String, List<CmsCI>> offeringsByNs = getOfferingsForClouds(cloudNs); 
		
		//List<CmsRfcCI> rfcs = rfcProcessor.getOpenRfcCIByNsLike(bomNsPath, null, null);
		
		
		Map<Long, Triplet> deploymentMap = new HashMap<>();
		for (CmsRfcRelation rfcRelation: rfcRelations){
			deploymentMap.put(rfcRelation.getFromCiId(), new Triplet(cloudMap.get(rfcRelation.getToRfcId()), cmProcessor.getCiById(rfcRelation.getToCiId()), rfcProcessor.getRfcCIById(rfcRelation.getFromRfcId()))) ;
		}
		
		for (CmsCIRelation relation:relations){
			long ciId = relation.getFromCiId();
			if (!deploymentMap.containsKey(ciId)){
				deploymentMap.put(ciId, new Triplet(cloudMap.get(relation.getToCiId()), relation.getFromCi(), rfcProcessor.getOpenRfcCIByCiId(ciId))) ;
			}
		}
		return calculateCost(offeringsByNs, deploymentMap.values());
	}

	private Map<String, BigDecimal> calculateCost(Map<String, List<CmsCI>> offeringsByNs, Collection<Triplet> triplets) {
		Map<String, BigDecimal> cost = new HashMap<>();
		for (Triplet triplet: triplets) {
			String costNs = getNs(triplet.cloud);
			for (CmsCI ci : offeringsByNs.get(costNs)) {
				CmsCIAttribute criteriaAttribute = ci.getAttribute("criteria");
				String criteria = criteriaAttribute.getDfValue();
				if (isLikelyElasticExpression(criteria)){
					criteria = convert(criteria);
				}
				Expression expression = exprParser.parseExpression(criteria);
				StandardEvaluationContext context = new StandardEvaluationContext();
				if (triplet.rfc!=null && "delete".equals(triplet.rfc.getRfcAction())) continue; // do not process deletes
				CmsRfcCI rfcCi = rfcUtil.mergeRfcAndCi(triplet.rfc, triplet.ci,null);
				context.setRootObject(cmsUtil.custRfcCI2RfcCISimple(rfcCi));
				boolean match = expression.getValue(context, Boolean.class);
				if (match) {
					String platformNs = rfcCi.getNsPath();
					cost.putIfAbsent(platformNs, BigDecimal.ZERO);
					cost.put(platformNs, cost.get(platformNs).add(new BigDecimal(ci.getAttribute("cost_rate").getDjValue())) );
				}
			}
		}
		return cost;
	}

	private Map<String, List<CmsCI>> getOfferingsForClouds(Set<String> cloudNs) {
		Map<String, List<CmsCI>> offeringsByNs = new HashMap<>();
		for (String ns: cloudNs) {
			offeringsByNs.put(ns, cmProcessor.getCiBy3NsLike(ns, "cloud.Offering", null));
		}
		return offeringsByNs;
	}

	@Override
	public Map<String, BigDecimal> calculateCost(long envId) {
		CmsCI env = cmProcessor.getCiById(envId);
		String bomNsPath = getNs(env) + "/bom";
		List<CmsCIRelation> relations = cmProcessor.getCIRelationsNsLikeNakedNoAttrs(bomNsPath, null,"DeployedTo", null, "account.Cloud", true, true);
		Set<String> cloudNs = new HashSet<>();
		for (CmsCIRelation relation: relations){
			cloudNs.add(getNs(relation.getToCi()));
		}
		Map<String, List<CmsCI>> offeringsByNs = getOfferingsForClouds(cloudNs);
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
