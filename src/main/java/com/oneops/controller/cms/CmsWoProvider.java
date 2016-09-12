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
package com.oneops.controller.cms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.collections.CollectionProcessor;
import com.oneops.cms.collections.def.CollectionLinkDefinition;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.domain.CmsWorkOrderBase;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.es.offerings.percolator.OfferingsMatcher;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * The Class CmsWoProvider.
 */
public class CmsWoProvider {
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	private DJDpmtMapper dpmtMapper;
    private OpsMapper opsMapper;
	private CmsCmRfcMrgProcessor cmrfcProcessor;
	private CmsCmProcessor cmProcessor;
	private DJMapper djMapper;
	private CmsRfcUtil rfcUtil;
	private CollectionProcessor colProcessor;
	private Gson gson = new Gson();
	private CmsUtil cmsUtil;
	private OfferingsMatcher offeringMatcher;
	private ExpressionEvaluator expressionEvaluator;
	
	private static final String CLOUDSERVICEPREFIX = "cloud.service";
	private static final String MANAGED_VIA_PAYLOAD_NAME = "ManagedVia";
	private static final String DEPENDS_ON_PAYLOAD_NAME = "DependsOn";
	private static final String ENTRYPOINT_PAYLOAD_NAME = "Entrypoint";
	private static final String SECURED_BY_PAYLOAD_NAME = "SecuredBy";
	private static final String SERVICED_BY_PAYLOAD_NAME = "ServicedBy";
	private static final String REQUIRES_COMPUTES_PAYLOAD_NAME = "RequiresComputes";
	private static final String OFFERING = "offerings";
	private static final String EXTRA_RUNLIST_PAYLOAD_NAME = "ExtraRunList";
	private static final boolean OFFERING_ENABLED = "true".equals(System.getProperty("controller.offerings.on", "true"));
	/**
	 * Sets the cms util.
	 *
	 * @param cmsUtil the new cms util
	 */
	public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}

	/**
	 * Sets the dj mapper.
	 *
	 * @param djMapper the new dj mapper
	 */
	public void setDjMapper(DJMapper djMapper) {
		this.djMapper = djMapper;
	}
	
	/**
	 * Sets the cm processor.
	 *
	 * @param cmProcessor the new cm processor
	 */
	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	/**
	 * Sets the cmrfc processor.
	 *
	 * @param cmrfcProcessor the new cmrfc processor
	 */
	public void setCmrfcProcessor(CmsCmRfcMrgProcessor cmrfcProcessor) {
		this.cmrfcProcessor = cmrfcProcessor;
	}

	/**
	 * Sets the dpmt mapper.
	 *
	 * @param dpmtMapper the new dpmt mapper
	 */
	public void setDpmtMapper(DJDpmtMapper dpmtMapper) {
		this.dpmtMapper = dpmtMapper;
	}

	/**
	 * Sets the rfc util.
	 *
	 * @param rfcUtil the new rfc util
	 */
	public void setRfcUtil(CmsRfcUtil rfcUtil){
		this.rfcUtil = rfcUtil;
	}

    /**
     * Sets the ops mapper.
     *
     * @param opsMapper the new ops mapper
     */
    public void setOpsMapper(OpsMapper opsMapper) {
        this.opsMapper = opsMapper;
    }
 	
    /**
     * Sets the col processor.
     *
     * @param colProcessor the new col processor
     */
    public void setColProcessor(CollectionProcessor colProcessor) {
		this.colProcessor = colProcessor;
	}

	public List<CmsActionOrderSimple> getActionOrdersSimple(long procedureId, OpsProcedureState state, Integer execOrder) {
		
		List<CmsActionOrder> aorders = getActionOrders(procedureId, state, execOrder);
		List<CmsActionOrderSimple> aosSimple = new ArrayList<CmsActionOrderSimple>();
		for (CmsActionOrder ao : aorders) {
			aosSimple.add(cmsUtil.custActionOrder2Simple(ao));
		}
		return aosSimple;
	}
	/**
	 * Gets the action orders.
	 *
	 * @param procedureId the procedure id
	 * @param state the state
	 * @param execOrder the exec order
	 * @return the action orders
	 */
	public List<CmsActionOrder> getActionOrders(long procedureId, OpsProcedureState state, Integer execOrder) {
        List<CmsActionOrder> aorders = opsMapper.getActionOrders(procedureId, state, execOrder);

        for(CmsActionOrder ao: aorders) {
            CmsCI ci = cmProcessor.getCiById(ao.getCiId());
            ao.setCi(ci);
        }
        populateWoBase(aorders);

        CmsCI env = null;
        List<CmsCI> envs = null;
        Map<Long, CmsCI> manifestToTemplateMap = new HashMap<Long, CmsCI>();
        for(CmsActionOrder ao: aorders) {
        	// this is a special case for the coud.Service usecase
        	if (ao.getCi().getCiClassName().startsWith(CLOUDSERVICEPREFIX)) {
        		continue;
        	}
        	if (env == null) {
        		env = getEnv(ao.getCiId());
        		envs = new ArrayList<CmsCI>();
        		envs.add(env);
        	};
        	
        	//put Environment
        	ao.putPayLoadEntry("Environment",envs);
    		//put proxy
    		ao.putPayLoadEntry("ManagedVia",getCIRelatives(ao.getCiId(),"bom.ManagedVia","from", null)); 
    		//put depends on
    		ao.putPayLoadEntry("DependsOn",getCIRelatives(ao.getCiId(),"bom.DependsOn","from", null));
    		//put realized as
    		ao.putPayLoadEntry("RealizedAs",getCIRelatives(ao.getCiId(),"base.RealizedAs","to", null));
			//put key pairs SecuredBy
			ao.putPayLoadEntry("SecuredBy", getKeyPairs(ao.getCi(), ao.getPayLoad().get("ManagedVia")));
    		
    		// if this is custom action from attachment - get the attachment
    		if (ao.getActionName().equals("user-custom-attachment")) {
    			CmsCI attachment = null;
    			// for the global, cloud and local vars to evaluate for attachment
    			Map<String, String> globalVars = cmsUtil.getGlobalVars(env);
    			Map<String, String> cloudVars = cmsUtil.getCloudVars(ao.getCloud());
    			Map<String, String> localVars = cmsUtil.getLocalVars(ao.getBox());
    			
    			if (ao.getExtraInfo() != null) {
    				long attachmentId = Long.valueOf(ao.getExtraInfo());
    				attachment = cmProcessor.getCiById(attachmentId);
    			}
    			
    			if (attachment != null) {
    				cmsUtil.processAllVars(attachment, cloudVars, globalVars, localVars);
    				ao.putPayLoadEntry("EscortedBy", new ArrayList<CmsCI>(Arrays.asList(attachment)));
    			} else {
    				throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR, "Can not find the attachment by id = " + ao.getExtraInfo());
    			}	
    		} else {	
    			//lets get the payload def from the template
    			long manifestCiId = getRealizedAs(ao.getCiId());
				if (!manifestToTemplateMap.containsKey(manifestCiId)) { 
					CmsCI manifestCi = cmProcessor.getCiById(manifestCiId);
					CmsCI templObj = cmProcessor.getTemplateObjForManifestObj(manifestCi, env);
					if (templObj == null) {
						logger.error("Can not find manifest template object for manifest ci id = " + manifestCi.getCiId() + " ciName" + manifestCi.getCiName());
					} else {
						manifestToTemplateMap.put(manifestCi.getCiId(), templObj);
					}
				}
				
				if (!manifestToTemplateMap.containsKey(manifestCiId)) {
					throw new DJException(CmsError.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR,
	                        "Can not find pack template for manifest component id=" + manifestCiId);
				}
				processPayLoadDef(ao, manifestToTemplateMap.get(manifestCiId), null, null, null);
				String actionPayLoad = ao.getPayLoadDef();
				processPayLoadDef(ao, actionPayLoad);
    		}

    		ao.putPayLoadEntry(EXTRA_RUNLIST_PAYLOAD_NAME, getMatchingCloudCompliance(ao));
        }
        return aorders;
    }

	public List<CmsWorkOrderSimple> getWorkOrderIdsSimple(long deploymentId, String state, Integer execOrder, Integer limit) {
		List<CmsWorkOrderSimple> wosList = new ArrayList<CmsWorkOrderSimple>();
		List<CmsWorkOrder> woList = getWorkOrderIds(deploymentId, state, execOrder, limit);
		
		for (CmsWorkOrder wo : woList) {
			wosList.add(cmsUtil.custWorkOrder2Simple(wo));
		}
			
		return wosList;

	}
	public List<CmsWorkOrder> getWorkOrderIds(long deploymentId, String state, Integer execOrder, Integer limit) {
		
		List<CmsWorkOrder> workOrders = limit != null ? dpmtMapper.getWorkOrdersLimited(deploymentId, state, execOrder, limit) 
													  : dpmtMapper.getWorkOrders(deploymentId, state, execOrder);
		return workOrders;
	}

	public CmsWorkOrderSimple getWorkOrderSimple(long dpmtRecordId, String state, Integer execOrder) {
		CmsWorkOrder wo = getWorkOrder(dpmtRecordId, state, execOrder);
		if (wo != null) {
			return cmsUtil.custWorkOrder2Simple(wo);
		} else {
			return null;
		}
	}	
	public CmsWorkOrder getWorkOrder(long dpmtRecordId, String state, Integer execOrder) {
		
		CmsWorkOrder workOrder = dpmtMapper.getWorkOrder(dpmtRecordId, state, execOrder);

		if (workOrder == null) {
			return null;
		}
		
		CmsRfcCI rfcNaked = djMapper.getRfcCIById(workOrder.getRfcId()); 
		CmsRfcCI rfcCimerged = cmrfcProcessor.getCiById(rfcNaked.getCiId(), "df");
		
		workOrder.setRfcCi(rfcCimerged);
		populateWoBase(workOrder);
		
		
        Map<Long, CmsCI> manifestToTemplateMap = new HashMap<Long, CmsCI>();
		
        CmsCI env = getEnv(workOrder.getRfcCi().getCiId());

        Map<String, String> globalVars = cmsUtil.getGlobalVars(env);
		Map<String, String> cloudVars = cmsUtil.getCloudVars(workOrder.getCloud());
		Map<String, String> localVars = cmsUtil.getLocalVars(workOrder.getBox());
		
		workOrder.putPayLoadEntry(CmsUtil.CLOUD_VARS_PAYLOAD_NAME, cmsUtil.getCloudVarsRfcs(workOrder.getCloud()));
		workOrder.putPayLoadEntry(CmsUtil.GLOBAL_VARS_PAYLOAD_NAME, cmsUtil.getGlobalVarsRfcs(env));
		workOrder.putPayLoadEntry(CmsUtil.LOCAL_VARS_PAYLOAD_NAME, cmsUtil.getLocalVarsRfcs(workOrder.getBox()));
		
		//basic staff
		//put realized as
		workOrder.putPayLoadEntry("RealizedAs",getRfcCIRelatives(workOrder.getRfcCi(),"base.RealizedAs","to", null, "df"));
		
		//put env
		List<CmsRfcCI>	envs = getRfcCIRelatives(workOrder.getBox().getCiId(),"manifest.ComposedOf","to", null, "df");
		workOrder.putPayLoadEntry("Environment", envs);
		
		//put assembly
		List<CmsRfcCI> assemblys = getRfcCIRelatives(workOrder.getPayLoad().get("Environment").get(0),"base.RealizedIn","to", null, "df");
		workOrder.putPayLoadEntry("Assembly", assemblys);
		
		//put Organization
		List<CmsRfcCI> orgs = getRfcCIRelatives(workOrder.getPayLoad().get("Assembly").get(0),"base.Manages","to", null, "df");
		workOrder.putPayLoadEntry("Organization", orgs);

		//put watchedBy and loggedBy
		if (workOrder.getPayLoad().get("RealizedAs").size()>0) {
			workOrder.putPayLoadEntry("WatchedBy",getWatchedByBy(workOrder.getPayLoad().get("RealizedAs").get(0), cloudVars, globalVars, localVars));
			workOrder.putPayLoadEntry("LoggedBy",getLoggedBy(workOrder.getPayLoad().get("RealizedAs").get(0)));
			workOrder.putPayLoadEntry("EscortedBy",getEscortedBy(workOrder.getPayLoad().get("RealizedAs").get(0),workOrder.getRfcCi().getRfcAction(), cloudVars, globalVars, localVars));
		}
		
		// now lets process the custom payloads and this will override the default ones as well

		//lets get the payload def from the template
		long manifestCiId = workOrder.getPayLoad().get("RealizedAs").get(0).getCiId(); 
		if (!manifestToTemplateMap.containsKey(manifestCiId)) { 
			CmsCI manifestCi = cmProcessor.getCiById(manifestCiId);
			CmsCI templObj = cmProcessor.getTemplateObjForManifestObj(manifestCi, env);
			if (templObj == null) {
				logger.error("Can not find manifest template object for manifest ci id = " + manifestCi.getCiId() + " ciName" + manifestCi.getCiName());
			} else {
				manifestToTemplateMap.put(manifestCi.getCiId(), templObj);
			}
		}
		
		if (!manifestToTemplateMap.containsKey(manifestCiId)) {
			throw new DJException(CmsError.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR,
                    "Can not find pack template for manifest component id=" + manifestCiId + "; name - " + workOrder.getPayLoad().get("RealizedAs").get(0).getCiName());
		}
		
		processPayLoadDef(workOrder, manifestToTemplateMap.get(manifestCiId), cloudVars, globalVars, localVars);

		
		//from here all payloads are default ones unless overriden by the custom payload definitions
		//put proxy
		if (!workOrder.getPayLoad().containsKey(MANAGED_VIA_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(MANAGED_VIA_PAYLOAD_NAME,getRfcCIRelatives(workOrder.getRfcCi(),"bom.ManagedVia","from", null, "df"));
		}
	
		//put depends on
		if (!workOrder.getPayLoad().containsKey(DEPENDS_ON_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(DEPENDS_ON_PAYLOAD_NAME,getRfcCIRelatives(workOrder.getRfcCi(),"bom.DependsOn","from", null, "df"));
		}

		//put Entrypoint
		if (!workOrder.getPayLoad().containsKey(ENTRYPOINT_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(ENTRYPOINT_PAYLOAD_NAME,getRfcCIRelatives(workOrder.getRfcCi(),"base.Entrypoint","to", null, "df"));
		}
		//put mgmt key pairs
		if (!workOrder.getPayLoad().containsKey(SECURED_BY_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(SECURED_BY_PAYLOAD_NAME, getKeyPairsRfc(workOrder.getRfcCi(), workOrder.getPayLoad().get("ManagedVia")));
		}
		//put serviecedBy
		if (!workOrder.getPayLoad().containsKey(SERVICED_BY_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(SERVICED_BY_PAYLOAD_NAME,getServicedBy(workOrder.getRfcCi()));
		}
		//put RequiresComputes
		if (!workOrder.getPayLoad().containsKey(REQUIRES_COMPUTES_PAYLOAD_NAME)) {
			workOrder.putPayLoadEntry(REQUIRES_COMPUTES_PAYLOAD_NAME,getRequiresComputes(workOrder.getRfcCi()));
		}
		
		//fetch and update offerings
		List<CmsRfcCI> offerings = new ArrayList<CmsRfcCI>();
		try {
			if(!"delete".equals(workOrder.getRfcCi().getRfcAction())){
				offerings = getRequiredOfferings(workOrder);
			}
		} catch (Exception e) {
			logger.error("Error in fetching offerings" , e);
		}
		
		workOrder.putPayLoadEntry(OFFERING , offerings);

		//add matching compliance objects
		workOrder.putPayLoadEntry(EXTRA_RUNLIST_PAYLOAD_NAME, getMatchingCloudCompliance(workOrder));

		return workOrder;
	}
	
	
	private List<CmsRfcCI> getRequiredOfferings(CmsWorkOrder workOrder) {
		
		List<CmsRfcCI> reqOfferings = new ArrayList<CmsRfcCI>();
		if (OFFERING_ENABLED) {
			for(Entry<String, Map<String, CmsCI>> serviceEntry : workOrder.getServices().entrySet()){
	           for(CmsCI serviceCI:serviceEntry.getValue().values()){
	        	   String offeringNS = serviceCI.getNsPath()+"/"+serviceCI.getCiClassName()+"/"+serviceCI.getCiName();
	        	   if(cmProcessor.getCountFromCIRelationsByNS(serviceCI.getCiId(), "base.Offers", null, null, offeringNS, false) > 0){
	        		  List<String> offeringIds = offeringMatcher.getEligbleOfferings(cmsUtil.custRfcCI2RfcCISimple(workOrder.getRfcCi()), offeringNS);
	        		  if(!offeringIds.isEmpty()){
	      				CmsRfcCI offeringRfc = rfcUtil.mergeRfcAndCi(null,getLowestCostOffering(offeringIds) ,"df");
	      				CmsRfcAttribute serviceTypeAttr = new CmsRfcAttribute();
	        			serviceTypeAttr.setAttributeName("service_type");
	        			serviceTypeAttr.setNewValue(serviceEntry.getKey());
	        			offeringRfc.addAttribute(serviceTypeAttr);
	      				reqOfferings.add(offeringRfc);
	      			}
	        	   }
	           }
			}
		}
		return reqOfferings;
	}

	private CmsCI getLowestCostOffering(List<String> offeringIds) {
		
		if(offeringIds.size() == 1){
			return cmProcessor.getCiById(Long.valueOf(offeringIds.get(0)));
		}
		
		CmsCI lowestOffering = null;
		for(String offId:offeringIds){
			CmsCI offering = cmProcessor.getCiById(Long.valueOf(offId));
			if (offering != null) {
				if(lowestOffering == null){
					lowestOffering = offering;
					continue;
				}else{
					Double costValue = Double.valueOf(offering.getAttribute("cost_rate").getDfValue());
					if(costValue < Double.valueOf(lowestOffering.getAttribute("cost_rate").getDfValue())){
						lowestOffering = offering;
					}
				}
			}
			else {
				logger.warn("offering not found, offId : " + offId);
			}
		}
		return lowestOffering;
	}

	List<CmsRfcCI> getMatchingCloudCompliance(CmsWorkOrder wo) {
		CmsCI platformCi = wo.getBox();
		CmsCIAttribute autoComplyAttr = platformCi.getAttribute(CmsConstants.ATTR_NAME_AUTO_COMPLY);
		if (!Boolean.valueOf(autoComplyAttr.getDfValue())) {
			return Collections.emptyList();
		}

		List<CmsCIRelation> complianceRelations = getComplianceRelations(wo);
		List<CmsRfcCI> list = complianceRelations.stream()
			.map(complianceRel -> complianceRel.getToCi())
			.filter(complianceCi -> (isComplianceEnabled(complianceCi)) && expressionEvaluator.isExpressionMatching(complianceCi, wo))
			.map(complianceCi -> rfcUtil.mergeRfcAndCi(null, complianceCi, CmsConstants.ATTR_VALUE_TYPE_DF))
			.collect(Collectors.toList());
		return list;
	}

	List<CmsCI> getMatchingCloudCompliance(CmsActionOrder ao) {
		List<CmsCIRelation> complianceRelations = getComplianceRelations(ao);
		List<CmsCI> list = complianceRelations.stream()
			.map(complianceRel -> complianceRel.getToCi())
			.filter(complianceCi -> (isComplianceEnabled(complianceCi)) && expressionEvaluator.isExpressionMatching(complianceCi, ao))
			.collect(Collectors.toList());

		return list;
	}

	private List<CmsCIRelation> getComplianceRelations(CmsWorkOrderBase wo) {
		List<CmsCIRelation> relations = cmProcessor.getFromCIRelations(wo.getCloud().getCiId(), CmsConstants.BASE_COMPLIES_WITH, null);
		return relations;
	}

	private boolean isComplianceEnabled(CmsCI compliance) {
		CmsCIAttribute attribute = compliance.getAttribute(CmsConstants.ATTR_NAME_ENABLED);
		return ((attribute != null) && Boolean.valueOf(attribute.getDjValue()));
	}

    private void processPayLoadDef(CmsWorkOrderBase wo, CmsCI templateCi, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {

    	List<CmsCIRelation> payloadRels = cmProcessor.getFromCIRelations(templateCi.getCiId(), "mgmt.manifest.Payload", "mgmt.manifest.Qpath");
    	for (CmsCIRelation payloadRel : payloadRels) {
    		processPayLoadQPath(wo, payloadRel.getToCi().getCiName(), payloadRel.getToCi().getAttribute("definition").getDfValue(), cloudVars, globalVars, localVars);
    	}
    }

    private void processPayLoadQPath(CmsWorkOrderBase wo, String key, String qPath, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
    	if (qPath == null ) return;
    	CollectionLinkDefinition payloadDef = gson.fromJson(qPath, CollectionLinkDefinition.class);
		if (wo instanceof CmsWorkOrder) {
			List<CmsRfcCI> payload = colProcessor.getFlatCollectionRfc(((CmsWorkOrder) wo).getRfcCi().getCiId(), payloadDef);
			for (CmsRfcCI cmsRfcCI : payload) {
				cmsUtil.processAllVars(cmsRfcCI, cloudVars, globalVars, localVars);
			}
			((CmsWorkOrder) wo).putPayLoadEntry(key, payload);
		} else if (wo instanceof CmsActionOrder) {
			List<CmsCI> payload = colProcessor.getFlatCollection(((CmsActionOrder) wo).getCiId(), payloadDef);
			((CmsActionOrder) wo).putPayLoadEntry(key, payload);
		}
    }

    private void processPayLoadDef(CmsWorkOrderBase wo, String payloadDefStr) {
    	if (payloadDefStr == null ) return;
    	Map<String, CollectionLinkDefinition> payloadDef = gson.fromJson(payloadDefStr, new TypeToken<HashMap<String, CollectionLinkDefinition>>() {}.getType());
		for (String key : payloadDef.keySet()) {
	    	if (wo instanceof CmsWorkOrder) {
				List<CmsRfcCI> payload = colProcessor.getFlatCollectionRfc(((CmsWorkOrder) wo).getRfcCi().getCiId(), payloadDef.get(key));
				((CmsWorkOrder) wo).putPayLoadEntry(key, payload);
			} else if (wo instanceof CmsActionOrder) {
				List<CmsCI> payload = colProcessor.getFlatCollection(((CmsActionOrder) wo).getCiId(), payloadDef.get(key));
				((CmsActionOrder) wo).putPayLoadEntry(key, payload);
			}
		}
    }
    
    
	private void populateWoBase(List<? extends CmsWorkOrderBase> wos) {

		for (CmsWorkOrderBase wo : wos) {
			populateWoBase(wo);
		}
	}

	private void populateWoBase(CmsWorkOrderBase wo) {
		long anchorCiId = 0;
		String targetClassName = null;
		if (wo instanceof CmsWorkOrder) {
			anchorCiId = ((CmsWorkOrder)wo).getRfcCi().getCiId();
			targetClassName = ((CmsWorkOrder)wo).getRfcCi().getCiClassName();
		} else if (wo instanceof CmsActionOrder) {
			anchorCiId = ((CmsActionOrder)wo).getCiId();
			targetClassName = ((CmsActionOrder)wo).getCi().getCiClassName();
		} else {
			throw new CmsException(CmsError.CMS_BAD_WO_CLASS_ERROR, "Bad wo class");
		}
		
		if (targetClassName != null && targetClassName.startsWith(CLOUDSERVICEPREFIX)) {
			wo.setCloud(getCloudForCloudService(anchorCiId));
		} else {
 			wo.setBox(getBox(anchorCiId));
			wo.setCloud(getCloud(anchorCiId));
			wo.setServices(getServices(anchorCiId, wo.getCloud()));
		}
	}

	
	private Map<String,Map<String, CmsCI>> getServices(long ciId, CmsCI cloud) {
		
		Map<String,Map<String, CmsCI>> services = new HashMap<String,Map<String, CmsCI>>();
		List<CmsRfcRelation> realizedAsRels = cmrfcProcessor.getToCIRelationsNaked(ciId, "base.RealizedAs", null, null);
		
		if (realizedAsRels.size()>0) {
			CmsRfcRelation realizedRel = realizedAsRels.get(0);
			List<CmsCIRelation> requiresList = cmProcessor.getToCIRelationsNaked(realizedRel.getFromCiId(), "manifest.Requires", null);
			if (requiresList.size()>0) {
				CmsCIRelation requiresRel = requiresList.get(0);
				CmsCIRelationAttribute servicesAttr = requiresRel.getAttribute("services");
				
				if (servicesAttr != null && servicesAttr.getDjValue() != null && servicesAttr.getDjValue().length() > 0) {
					String[] requiredServices = servicesAttr.getDjValue().split(",");
					for (String requredServiceFull : requiredServices) {
						String requredService = null;
						if (requredServiceFull.startsWith("*")) { //optional service
							requredService = requredServiceFull.replace("*", "");
						} else {
							requredService = requredServiceFull;
						}
						List<AttrQueryCondition> attrsQuery = new ArrayList<AttrQueryCondition>();
						AttrQueryCondition attrCondition = new AttrQueryCondition();
						attrCondition.setAttributeName("service");
						attrCondition.setAvalue(requredService);
						attrCondition.setCondition("eq");
						attrsQuery.add(attrCondition);
						List<CmsCIRelation> cloudServiceRels =  cmProcessor.getFromCIRelationsByAttrs(cloud.getCiId(), "base.Provides", null, null, attrsQuery );
						if (cloudServiceRels.size()>0) {
							CmsCI cloudService = cloudServiceRels.get(0).getToCi();
							if (!services.containsKey(requredService)) {
								services.put(requredService, new HashMap<String, CmsCI>());
							}
							services.get(requredService).put(cloud.getCiName(), cloudService);
						}
					}
				}
			} else {
				throw new CmsException(CmsError.CMS_CANT_FIND_REQUIRES_FOR_CI_ERROR,
	                    "can't find Requires for manifest ci with ciId=" + realizedRel.getFromCiId());
			}
		} else {
			throw new CmsException(CmsError.CMS_CANT_FIND_REALIZEDAS_FOR_BOMC_ERROR,
                    "can't find realaziedAs for with ciId=" + ciId);
		}
		
		return services;
	}
	
	
	
	private CmsCI getBox(long ciId) {
		
		CmsCI box = null;
		List<CmsRfcRelation> realizedAsRels = cmrfcProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs", null, null);
		if (realizedAsRels.size()>0) {
			List<CmsCIRelation> boxList = cmProcessor.getToCIRelations(realizedAsRels.get(0).getFromCiId(), "manifest.Requires", null);
			if (boxList.size()>0) {
				box = boxList.get(0).getFromCi();
			}
		}
		
		return box;
	}

	private List<CmsRfcCI>  getRequiresComputes(CmsRfcCI rfc) {

		List<CmsRfcCI> computes = new ArrayList<CmsRfcCI>();
		CmsCI platform = getBox(rfc.getCiId());

		List<CmsCIRelation> manifestComputeList = cmProcessor.getFromCIRelationsNakedNoAttrs(platform.getCiId(), "manifest.Requires", null, "Compute");

		for (CmsCIRelation rel : manifestComputeList) {
			List<CmsRfcRelation> bomComputeRels = cmrfcProcessor.getFromCIRelations(rel.getToCiId(), "base.RealizedAs", null, "df");
			for (CmsRfcRelation realized : bomComputeRels) {
				computes.add(realized.getToRfcCi());
			}
		}
		return computes;
	}
	
	
	
	private List<CmsRfcCI>  getServicedBy(CmsRfcCI rfc) {
		
		List<CmsRfcCI> iaases = new ArrayList<CmsRfcCI>();
		
		CmsCI box = getBox(rfc.getCiId());
		
		List<CmsCIRelation> iaasList = cmProcessor.getFromCIRelations(box.getCiId(), "manifest.ServicedBy", "manifest.Iaas");
		for (CmsCIRelation rel : iaasList) {
			CmsRfcCI iaas = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
			iaas.getAttribute("services").setNewValue(rel.getAttribute("services").getDjValue());
			List<CmsCIRelation> keypairs = cmProcessor.getFromCIRelations(iaas.getCiId(), "manifest.Requires", "manifest.Keypair");
			if (keypairs.size()>0) {
				CmsRfcAttribute prKeyAttr = new CmsRfcAttribute();
				prKeyAttr.setAttributeName("private_key");
				prKeyAttr.setNewValue(keypairs.get(0).getToCi().getAttribute("private").getDjValue());
				iaas.addAttribute(prKeyAttr);
			}
			//this is total HACK for Netscaler needs to be generalized
			List<CmsCIRelation> netscaler = cmProcessor.getFromCIRelations(iaas.getCiId(), "manifest.Requires", "manifest.Netscaler");
			if (netscaler.size()>0) {
				for (Map.Entry<String,CmsCIAttribute> attrEntry : netscaler.get(0).getToCi().getAttributes().entrySet()) {
					CmsCIAttribute nsAttr = attrEntry.getValue();
					CmsRfcAttribute iaasNsAttr = new CmsRfcAttribute();
					iaasNsAttr.setAttributeName(nsAttr.getAttributeName());
					iaasNsAttr.setNewValue(nsAttr.getDjValue());
					iaas.addAttribute(iaasNsAttr);
				}
			}
			iaases.add(iaas);
		}
		return iaases;
	}

    private List<CmsRfcCI>  getWatchedByBy(CmsRfcCI realizedAs, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
		
		List<CmsRfcCI> monitors = new ArrayList<CmsRfcCI>();
		
		List<CmsCIRelation> monitorList = cmProcessor.getFromCIRelations(realizedAs.getCiId(), "manifest.WatchedBy", "manifest.Monitor");
		for (CmsCIRelation rel : monitorList) {
			cmsUtil.processAllVars(rel.getToCi(), cloudVars, globalVars, localVars);
			CmsRfcCI monitor = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
			if (!CmsConstants.CI_STATE_PENDING_DELETION.equals(monitor.getCiState())) {
				monitors.add(monitor);
			}
		}
		return monitors;
	}
	
	private List<CmsRfcCI>  getLoggedBy(CmsRfcCI realizedAs) {
		
		List<CmsRfcCI> logs = new ArrayList<CmsRfcCI>();
		
		List<CmsCIRelation> logList = cmProcessor.getFromCIRelations(realizedAs.getCiId(), "manifest.LoggedBy", "manifest.Log");
		for (CmsCIRelation rel : logList) {
			CmsRfcCI log = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
			logs.add(log);
		}
		return logs;
	}

	private List<CmsRfcCI>  getEscortedBy(CmsRfcCI realizedAs, String action, Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars) {
		
		List<CmsRfcCI> attachments = new ArrayList<CmsRfcCI>();
		
		List<CmsCIRelation> attachmentList = cmProcessor.getFromCIRelations(realizedAs.getCiId(), "manifest.EscortedBy", "manifest.Attachment");
		for (CmsCIRelation rel : attachmentList) {
			CmsCI attachment = rel.getToCi();
			if (attachment.getAttribute("run_on") != null && attachment.getAttribute("run_on").getDjValue().contains(action)) {
				cmsUtil.processAllVars(attachment, cloudVars, globalVars, localVars);
				CmsRfcCI attachmentRfc = rfcUtil.mergeRfcAndCi(null, attachment, "dj");
				attachments.add(attachmentRfc);
			}
		}
		return attachments;
	}
	
	
	private List<CmsRfcCI> getKeyPairsRfc(CmsRfcCI rfc, List<CmsRfcCI> managedVia) {
		
		List<CmsRfcCI> keys = new ArrayList<CmsRfcCI>();
		List<CmsRfcRelation> secRels = null;
		if (managedVia != null && managedVia.size() >0) {
			secRels = cmrfcProcessor.getFromCIRelations(managedVia.get(0).getCiId(), "bom.SecuredBy", null, "df");
		} else {
			secRels = cmrfcProcessor.getFromCIRelations(rfc.getCiId(), "bom.SecuredBy", null, "df");
		}	
		for (CmsRfcRelation rel : secRels) {
			keys.add(rel.getToRfcCi());
		}
		return keys;
	}

	private List<CmsCI> getKeyPairs(CmsCI ci, List<CmsCI> managedVia) {
		List<CmsCI> keys = new ArrayList<CmsCI>();
		List<CmsCIRelation> secRels = null;

		if (managedVia != null && managedVia.size() >0) {
			secRels = cmProcessor.getFromCIRelations(managedVia.get(0).getCiId(), "bom.SecuredBy", null, null);
		} else {
			secRels = cmProcessor.getFromCIRelations(ci.getCiId(), "bom.SecuredBy", null, null);
		}	
		for (CmsCIRelation rel : secRels) {
			keys.add(rel.getToCi());
		}
		return keys;
	}

	private CmsCI getEnv(long ciId) {
		CmsCI box = getBox(ciId);
		if (box != null) {
			List<CmsCIRelation> envRels = cmProcessor.getToCIRelations(box.getCiId(), "manifest.ComposedOf", "manifest.Environment");
			if (envRels.size() >0) {
				return envRels.get(0).getFromCi(); 
			}
		}
		return null;
	}
	
	private List<CmsRfcCI> getRfcCIRelatives(CmsRfcCI rfc, String relName, String direction, String className, String attrValue) {
		return getRfcCIRelatives(rfc.getCiId(), relName, direction, className, attrValue);
	}

	private List<CmsRfcCI> getRfcCIRelatives(long ciId, String relName, String direction, String className, String attrValue) {
		List<CmsRfcCI> relatives = new ArrayList<CmsRfcCI>();
		if ("from".equalsIgnoreCase(direction)) {
			List<CmsRfcRelation> rels = cmrfcProcessor.getFromCIRelations(ciId, relName, className, attrValue);
			for (CmsRfcRelation rel : rels) {
				relatives.add(rel.getToRfcCi());
			}
		} else {
			List<CmsRfcRelation> rels = cmrfcProcessor.getToCIRelations(ciId, relName, className, attrValue);
			for (CmsRfcRelation rel : rels) {
				relatives.add(rel.getFromRfcCi());
			}
		}
		return relatives;
	}
	
	
	private List<CmsCI> getCIRelatives(long ciId, String relName, String direction, String className) {
		List<CmsCI> relatives = new ArrayList<CmsCI>();
		if ("from".equalsIgnoreCase(direction)) {
			List<CmsCIRelation> rels = cmProcessor.getFromCIRelations(ciId, relName, null, className);
			for (CmsCIRelation rel : rels) {
				relatives.add(rel.getToCi());
			}
		} else {
			List<CmsCIRelation> rels = cmProcessor.getToCIRelations(ciId, relName, null, className);
			for (CmsCIRelation rel : rels) {
				relatives.add(rel.getFromCi());
			}
		}
		return relatives;
	}


	private CmsCI getCloud(long ciId) {
		List<CmsRfcRelation> cloudRels = cmrfcProcessor.getFromCIRelationsNakedNoAttrs(ciId, "base.DeployedTo", null, "account.Cloud");
		if (cloudRels.size()>0) {
			CmsCI cloud = cmProcessor.getCiById(cloudRels.get(0).getToCiId());

			List<CmsRfcRelation> realizedAsRels = cmrfcProcessor.getToCIRelationsNaked(ciId, "base.RealizedAs", null, null);
			if (realizedAsRels.size() >0 && realizedAsRels.get(0).getAttribute("priority") != null) {
				String priority = realizedAsRels.get(0).getAttribute("priority").getNewValue();
				CmsCIAttribute prAttr = new CmsCIAttribute();
				prAttr.setAttributeName("priority");
				prAttr.setDfValue(priority);
				prAttr.setDjValue(priority);
				cloud.addAttribute(prAttr);
			} 
			return cloud;
		}
		return null;
	}

	private CmsCI getCloudForCloudService(long ciId) {
		List<CmsRfcRelation> cloudRels = cmrfcProcessor.getToCIRelationsNakedNoAttrs(ciId, "base.Provides", null, "account.Cloud");
		if (cloudRels.size()>0) {
			CmsCI cloud = cmProcessor.getCiById(cloudRels.get(0).getFromCiId());
			return cloud;
		}
		return null;
	}

	private long getRealizedAs(long bomCiId) {
		List<CmsCIRelation> manifestList = cmProcessor.getToCIRelationsNakedNoAttrs(bomCiId, "base.RealizedAs", null, null);
		if (manifestList.size()>0) {
			return manifestList.get(0).getFromCiId();
		} else {
			throw new CmsException(CmsError.CMS_CANT_FIND_REALIZEDAS_FOR_BOMC_ERROR,
                                            "Can not find RealizedAs for bomc ciId - " + bomCiId);
		}
	}

	public OfferingsMatcher getOfferingMatcher() {
		return offeringMatcher;
	}

	public void setOfferingMatcher(OfferingsMatcher offeringMatcher) {
		this.offeringMatcher = offeringMatcher;
	}

	public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
		this.expressionEvaluator = expressionEvaluator;
	}

}
