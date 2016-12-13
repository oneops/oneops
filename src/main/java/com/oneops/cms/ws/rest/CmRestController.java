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
package com.oneops.cms.ws.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.domain.CmsRfcCI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.exceptions.OpsException;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.cms.ws.exceptions.CmsSecurityException;
import com.oneops.cms.ws.rest.util.CmsScopeVerifier;

@Controller
public class CmRestController extends AbstractRestController {

	private CmsUtil cmsUtil;
	private CmsCmManager cmManager;
	private OpsManager opsManager;
	private CmsScopeVerifier scopeVerifier; 
	
	@Autowired
    public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}
	
	public void setScopeVerifier(CmsScopeVerifier scopeVerifier) {
		this.scopeVerifier = scopeVerifier;
	}

	public void setOpsManager(OpsManager opsManager) {
		this.opsManager = opsManager;
	}


	public void setCmManager(CmsCmManager cmManager) {
		this.cmManager = cmManager;
	}

	@ExceptionHandler(DJException.class)
	public void handleDJExceptions(DJException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CIValidationException.class)
	public void handleCIValidationExceptions(CIValidationException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}

    @ExceptionHandler(CmsException.class)
    public void handleCmsException(CmsException e, HttpServletResponse response) throws IOException {
        sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
    }

    @ExceptionHandler(CmsSecurityException.class)
	public void handleCmsSecurityException(CmsSecurityException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_FORBIDDEN,e);
	}

    @ExceptionHandler(OpsException.class)
	public void handleOpsException(OpsException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_NOT_FOUND,e);
	}
	
	
	@RequestMapping(value="/cm/cis/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsCI getCIById(
			@PathVariable long ciId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {

		CmsCI ci = cmManager.getCiById(ciId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                                            "There is no ci with this id");

		scopeVerifier.verifyScope(scope, ci);
		
		return ci;
	}

	@RequestMapping(value="/cm/relations/{relId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsCIRelation getRelationById(
			@PathVariable long relId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {

		CmsCIRelation rel = cmManager.getRelationById(relId);

		if (rel == null) throw new CmsException(CmsError.CMS_NO_RELATION_WITH_GIVEN_ID_ERROR,
                                                    "There is no relation with this id");

		scopeVerifier.verifyScope(scope, rel);
		
		return rel;
	}

	
	@RequestMapping(value="/cm/cis", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsCI> getCIBy3(
			@RequestParam("ns") String nsPath,  
			@RequestParam("clazz") String clazzName, 
			@RequestParam("ciname") String ciName,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsCI> ciList = cmManager.getCiBy3(nsPath,clazzName, ciName);
		
		if (scope != null) {
			for (CmsCI ci : ciList) {
				scopeVerifier.verifyScope(scope, ci);
			}
		}
		return ciList;
	}
	

	@RequestMapping(value="/cm/simple/cis/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsCISimple getCISimpleById(@PathVariable long ciId, 
			@RequestParam(value="value", required = false) String valueType,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		
		CmsCI ci = cmManager.getCiById(ciId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                                        "There is no ci with this id");

		scopeVerifier.verifyScope(scope, ci);

		return cmsUtil.custCI2CISimple(ci, valueType, attrProps, getEncrypted != null);
	}

	@RequestMapping(value="/cm/simple/cis/{ciId}/states", method = RequestMethod.GET)
	@ResponseBody
	public String updateCiState(@PathVariable long ciId, 
			@RequestParam(value="newState", required = true) String newState,
			@RequestParam(value="relationName", required = false) String relName,
			@RequestParam(value="direction", required = false) String direction,
			@RequestParam(value="recursive", required = false) Boolean recursive,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) {
		
		CmsCI ci = cmManager.getCiById(ciId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                                        "There is no ci with this id");

		scopeVerifier.verifyScope(scope, ci);

		cmManager.updateCiState(ciId, newState, relName, direction, recursive != null, userId);

		return "{\"updated\"}";	
	
	}
	
	@RequestMapping(value="/cm/simple/cis/{ciId}/states", method = RequestMethod.PUT)
	@ResponseBody
	public String updateCiStatePut(@PathVariable long ciId, 
			@RequestParam(value="newState", required = true) String newState,
			@RequestParam(value="relationName", required = false) String relName,
			@RequestParam(value="direction", required = false) String direction,
			@RequestParam(value="recursive", required = false) Boolean recursive,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) {
		
		CmsCI ci = cmManager.getCiById(ciId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                                        "There is no ci with this id");

		scopeVerifier.verifyScope(scope, ci);

		cmManager.updateCiState(ciId, newState, relName, direction, recursive != null, userId);

		return "{\"updated\"}";	
	
	}

	
	@RequestMapping(value="/cm/simple/cis/states", method = RequestMethod.PUT)
	@ResponseBody
	public String updateCiStateBulk(
			@RequestParam(value="ids", required = true) String ids,
			@RequestParam(value="newState", required = true) String newState,
			@RequestParam(value="relationName", required = false) String relName,
			@RequestParam(value="direction", required = false) String direction,
			@RequestParam(value="recursive", required = false) Boolean recursive,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) {
		
		String[] idsStr = ids.split(",");
	    Long[] ciIds = new Long[idsStr.length];
	    for (int i=0; i<idsStr.length; i++) {
             ciIds[i] = Long.valueOf(idsStr[i]);
	    }
		cmManager.updateCiStateBulk(ciIds, newState, relName, direction, recursive != null, userId);
		return "{\"updated\"}";	
	}


    /**
     * 
     * @param ciId the Id of the ci for which the procedure needs to be found 
     * @param stateList optional single or comma separated list of states procedure is in
     * @param limit optional parameter to limit number of elements in the list. Defaults to 10 is missing.  
     * @return list of CmsOpsProcedure
     */
	@RequestMapping(value="/cm/simple/cis/{ciId}/procedures", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsOpsProcedure> getgetOpsProcedureForCi(@PathVariable long ciId, 
			@RequestParam(value="state", required = false) List<OpsProcedureState> stateList,
			@RequestParam(value="limit", required = false) Integer limit) {
		return opsManager.getCmsOpsProcedureForCi(ciId, stateList, null, limit);
	}


	@RequestMapping(value="/cm/simple/cis/list", method = RequestMethod.POST)
	@ResponseBody
	public List<CmsCISimple> getCIcByIds(
			@RequestBody Long[] ciIdsAr) {
		
		List<Long> ciIds = new ArrayList<>();
        for (Long ciId : ciIdsAr) {
            ciIds.add(Long.valueOf(ciId));
        }
        List<CmsCISimple> ciSimpleList = new ArrayList<>();
		for (CmsCI ci : cmManager.getCiByIdList(ciIds)) {
			ciSimpleList.add(cmsUtil.custCI2CISimple(ci, "df", false));
		}
		return ciSimpleList;
	}

			
	
	@RequestMapping(value="/cm/simple/cis", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsCISimple> getCISimpleQuery(
			@RequestParam(value="nsPath", required = false) String nsPath,
			@RequestParam(value="ciClassName", required = false) String clazzName, 
			@RequestParam(value="ciName", required = false) String ciName,
			@RequestParam(value="attr", required = false)  String[] attrs,
			@RequestParam(value="ids", required = false)  String ids,
			@RequestParam(value="value", required = false)  String valueType,
			@RequestParam(value="altNs", required = false)  String altNs,
			@RequestParam(value="tag", required = false)  String tag,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
	
		List<CmsCISimple> ciSimpleList;
		
		if (attrs != null) {
			boolean nsRecursive = recursive != null;
			ciSimpleList = getCISimpleByAttrs(nsPath, clazzName, attrs, valueType, nsRecursive);
		} else if (ids != null) {
			String[] ciIdsAr = ids.split(",");
	        List<Long> ciIds = new ArrayList<>();
	        for (String ciId : ciIdsAr) {
	            ciIds.add(Long.valueOf(ciId));
	        }
			ciSimpleList = new ArrayList<>();
			for (CmsCI ci : cmManager.getCiByIdList(ciIds)) {
				ciSimpleList.add(cmsUtil.custCI2CISimple(ci, valueType, attrProps, getEncrypted != null));
			}
	        
		} else {
		
			List<CmsCI> ciList;
			if (altNs!=null || tag!=null){
				ciList = cmManager.getCmCIByAltNsAndTag(altNs, tag);
			} else if (recursive != null && recursive) {
				ciList = cmManager.getCiBy3NsLike(nsPath, clazzName, ciName);
			} else {	
				ciList = cmManager.getCiBy3(nsPath, clazzName, ciName);
			}	
			ciSimpleList = new ArrayList<>();
			for (CmsCI ci : ciList) {
				ciSimpleList.add(cmsUtil.custCI2CISimple(ci, valueType, attrProps, getEncrypted != null));
			}
		}
		
		if (scope != null) {
			for (CmsCISimple ci : ciSimpleList) {
				scopeVerifier.verifyScope(scope, ci);
			}
		}
		
		return ciSimpleList;
	}

	@RequestMapping(value="/cm/simple/cis/count", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Long> getCountBy3(
			@RequestParam(value="nsPath", required = true) String nsPath,
			@RequestParam(value="ciClassName", required = false) String clazzName, 
			@RequestParam(value="ciName", required = false) String ciName,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="groupBy", required = false) String groupBy,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (scope != null) {
			scopeVerifier.verifyScope(scope, nsPath);
		}

		if (recursive == null) {
			recursive = false;
		}
		if (groupBy != null) {
			return cmManager.getCountBy3GroupByNs(nsPath, clazzName, ciName);
		} else {
			Long count = cmManager.getCountBy3(nsPath, clazzName, ciName, recursive);
			Map<String, Long> result = new HashMap<>(1);
			result.put("count", count);
			return result;
		}
	}
	
	private List<CmsCISimple> getCISimpleByAttrs(
			String nsPath,  
			String clazzName, 
			String[] attrs, 
			String valueType,
			boolean recursive){
		
		List<AttrQueryCondition> attrConds = cmsUtil.parseConditions(attrs); 

		List<CmsCI> ciList = cmManager.getCiByAttributes(nsPath, clazzName, attrConds, recursive);
		List<CmsCISimple> ciSimpleList = new ArrayList<>();
		for (CmsCI ci : ciList) {
			ciSimpleList.add(cmsUtil.custCI2CISimple(ci, valueType));
		}
		return ciSimpleList;
	}

	
	
	@RequestMapping(method=RequestMethod.POST, value="/cm/simple/cis")
	@ResponseBody
	public CmsCISimple createCISimple(
			@RequestParam(value="value", required = false)  String valueType, 
			@RequestBody CmsCISimple ciSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {	
		
		scopeVerifier.verifyScope(scope, ciSimple);
		
		CmsCI newCi = cmsUtil.custCISimple2CI(ciSimple, valueType);
		newCi.setCiId(0);
		newCi.setCiGoid(null);
		newCi.setCreatedBy(userId);
		try {
			CmsCI ci = cmManager.createCI(newCi);
			logger.debug(ci.getCiId());
			return cmsUtil.custCI2CISimple(ci, valueType);
		} catch (DataIntegrityViolationException dive) {
			if (dive instanceof DuplicateKeyException) {
				throw new CIValidationException(CmsError.CMS_DUPCI_NAME_ERROR, dive.getMessage());
			} else {
				throw new CmsException(CmsError.CMS_EXCEPTION, dive.getMessage());
			}
		}
	}

	
	@RequestMapping(method=RequestMethod.PUT, value="/cm/simple/cis/{ciId}")
	@ResponseBody
	public CmsCISimple updateCISimple(
			@PathVariable long ciId,
			@RequestParam(value="value", required = false)  String valueType, 
			@RequestBody CmsCISimple ciSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {

		scopeVerifier.verifyScope(scope, ciSimple);
		
		logger.debug(ciSimple.getCiName());
		ciSimple.setCiId(ciId);
		ciSimple.setUpdatedBy(userId);
		CmsCI ci = cmManager.updateCI(cmsUtil.custCISimple2CI(ciSimple, valueType));
		return cmsUtil.custCI2CISimple(ci, valueType);

	}

	@RequestMapping(method=RequestMethod.DELETE, value="/cm/simple/cis/{ciId}")
	@ResponseBody
	public String delteCISimple(
			@PathVariable long ciId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) {	

		if (scope != null) {
			CmsCI ci = cmManager.getCiById(ciId);
			scopeVerifier.verifyScope(scope, ci);
		}
		
		cmManager.deleteCI(ciId, userId);
		return "{\"deleted\"}";
	}
	
	@RequestMapping(value="/cm/simple/relations/{ciRelId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsCIRelationSimple getRelationSimpleById(
			@PathVariable long ciRelId, 
			@RequestParam(value="includeFromCi", required = false) String includeFromCi,
			@RequestParam(value="includeToCi", required = false) String includeToCi,
			@RequestParam(value="value", required = false) String valueType,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		CmsCIRelation rel = cmManager.getRelationById(ciRelId);
		
		if (rel == null) throw new CmsException(CmsError.CMS_NO_RELATION_WITH_GIVEN_ID_ERROR,
                                                    "There is no relation with this id");

		scopeVerifier.verifyScope(scope, rel);
		
		if (includeFromCi != null) {
			rel.setFromCi(cmManager.getCiById(rel.getFromCiId()));
		}
		
		if (includeToCi != null) {
			rel.setToCi(cmManager.getCiById(rel.getToCiId()));
		}
		
		return cmsUtil.custCIRelation2CIRelationSimple(rel, valueType, getEncrypted!=null); 
	}

	@RequestMapping(value="/cm/simple/relations/count", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Long> getRelationsCounts(
			@RequestParam(value="nsPath", required = true) String nsPath,
			@RequestParam(value="ciId", required = false) Long ciId,
			@RequestParam(value="direction", required = true) String direction, 
			@RequestParam(value="relationName", required = false) String relationName,
			@RequestParam(value="relationShortName", required = false) String shortRelationName,
			@RequestParam(value="targetClassName", required = false) String targetClazz,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="groupBy", required = false) String groupBy,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (scope != null) {
			scopeVerifier.verifyScope(scope, nsPath);
		}
		if (recursive == null) {
			recursive = false;
		}
		if (groupBy != null) {
			if ("ciId".equals(groupBy)) {
				Map<Long,Long> counts;
				if ("from".equals(direction)) {
					counts = cmManager.getCounCIRelationsGroupByFromCiId(relationName, shortRelationName, targetClazz, nsPath);
				} else {
					counts = cmManager.getCounCIRelationsGroupByToCiId(relationName, shortRelationName, targetClazz, nsPath);
				}
				//convert to Map<String,Long>
				Map<String, Long> result = new HashMap<>(1);
				for (Map.Entry<Long, Long> count : counts.entrySet()) {
					result.put(count.getKey().toString(), count.getValue());
				}
				return result;
			} else {
				if ("from".equals(direction)) {
					return cmManager.getCountFromCIRelationsGroupByNs(ciId, relationName, shortRelationName, targetClazz, nsPath);
				} else {
					return cmManager.getCountToCIRelationsGroupByNs(ciId, relationName, shortRelationName, targetClazz, nsPath);
				}
			}
		} else {
			Long count;
			if ("from".equals(direction)) {
				count = cmManager.getCountFromCIRelationsByNS(ciId, relationName, shortRelationName, targetClazz, nsPath, recursive);
			} else {
				count = cmManager.getCountToCIRelationsByNS(ciId, relationName, shortRelationName, targetClazz, nsPath, recursive);
			}
			Map<String, Long> result = new HashMap<>(1);
			result.put("count", count);
			return result;
		}
	}


	@RequestMapping(value="/cm/simple/relations", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsCIRelationSimple> getCIRelationSimpleQuery(
			@RequestParam(value="ciId", required = false) Long ciId,
			@RequestParam(value="direction", required = false) String direction, 
			@RequestParam(value="nsPath", required = false) String nsPath, 
			@RequestParam(value="relationName", required = false) String relationName,
			@RequestParam(value="relationShortName", required = false) String shortRelationName,
			@RequestParam(value="fromClassName", required = false) String fromClazz,
			@RequestParam(value="targetClassName", required = false) String targetClazz,
			@RequestParam(value="value", required = false)  String valueType,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestParam(value="attr", required = false)  String[] attrs,
			@RequestParam(value="targetIds", required = false)  Long[] targetIds,
			@RequestParam(value="includeFromCi", required = false) String includeFromCi,
			@RequestParam(value="includeToCi", required = false) String includeToCi,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsCIRelation> relList = null;
		if (ciId != null) {
			if (attrs != null) {
				List<AttrQueryCondition> attrConds = cmsUtil.parseConditions(attrs); 
				if ("from".equalsIgnoreCase(direction)) {
					relList = cmManager.getFromCIRelations(ciId, relationName, shortRelationName, targetClazz, attrConds);
				} else if ("to".equalsIgnoreCase(direction)) {
					relList = cmManager.getToCIRelations(ciId, relationName, shortRelationName, targetClazz, attrConds);
				}
			} else if(targetIds != null) {
				if ("from".equalsIgnoreCase(direction)) {
					relList = cmManager.getFromCIRelations(ciId, relationName, shortRelationName, Arrays.asList(targetIds));
				} else if ("to".equalsIgnoreCase(direction)) {
					relList = cmManager.getToCIRelations(ciId, relationName, shortRelationName, Arrays.asList(targetIds));
				}
			} else {	
				if ("from".equalsIgnoreCase(direction)) {
					relList = cmManager.getFromCIRelations(ciId, relationName, shortRelationName, targetClazz);
				} else if ("to".equalsIgnoreCase(direction)) {
					relList = cmManager.getToCIRelations(ciId, relationName, shortRelationName, targetClazz);
				} else {
					relList = cmManager.getAllCIRelations(ciId);
				}
			}
		} else if (nsPath != null) {
			
			if (recursive != null && recursive) {
				relList = cmManager.getCIRelationsNsLike(nsPath, relationName, shortRelationName, fromClazz, targetClazz);
			} else {	
				relList = cmManager.getCIRelations(nsPath, relationName, shortRelationName, fromClazz, targetClazz);
			}
			cmManager.populateRelCis(relList, includeFromCi != null, includeToCi != null);
		} else {
			throw new DJException(CmsError.DJ_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR,
                                            "You must specify either ciId or nsPath ");
		}
		
		List<CmsCIRelationSimple> simpleList = new ArrayList<>();
		for (CmsCIRelation rel : relList) {
			scopeVerifier.verifyScope(scope, rel);
			simpleList.add(cmsUtil.custCIRelation2CIRelationSimple(rel, valueType, getEncrypted!=null));
		}
		return simpleList;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/cm/simple/relations")
	@ResponseBody
	public CmsCIRelationSimple createCIRelation(
			@RequestParam(value="value", required = false)  String valueType, 
			@RequestBody CmsCIRelationSimple relSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {
		
		scopeVerifier.verifyScope(scope, relSimple);
		
		CmsCIRelation rel = cmsUtil.custCIRelationSimple2CIRelation(relSimple, valueType);
		rel.setCreatedBy(userId);
		try {
			CmsCIRelation newRel = cmManager.createRelation(rel);
			return cmsUtil.custCIRelation2CIRelationSimple(newRel, valueType,false);
		} catch (DataIntegrityViolationException dive) {
			if (dive instanceof DuplicateKeyException) {
				throw new CIValidationException(CmsError.CMS_DUPCI_NAME_ERROR, dive.getMessage());
			} else {
				throw new CmsException(CmsError.CMS_EXCEPTION, dive.getMessage());
			}
		}
	}

	@RequestMapping(method=RequestMethod.PUT, value="/cm/simple/relations/{ciRelId}")
	@ResponseBody
	public CmsCIRelationSimple updateCIRelation(
			@PathVariable long ciRelId,
			@RequestParam(value="value", required = false)  String valueType, 
			@RequestBody CmsCIRelationSimple relSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {
		
		scopeVerifier.verifyScope(scope, relSimple);
		
		relSimple.setCiRelationId(ciRelId);
		CmsCIRelation rel = cmsUtil.custCIRelationSimple2CIRelation(relSimple, valueType);
		rel.setUpdatedBy(userId);
		CmsCIRelation newRel = cmManager.updateRelation(rel);
		return cmsUtil.custCIRelation2CIRelationSimple(newRel, valueType,false);
	}
	
	
	@RequestMapping(value="/cm/simple/relations/{ciRelId}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteRelation(
			@PathVariable long ciRelId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (scope != null) {
			CmsCIRelation rel = cmManager.getRelationById(ciRelId);
			scopeVerifier.verifyScope(scope, rel);
		}
		
		cmManager.deleteRelation(ciRelId);
		return "{\"deleted\"}"; 
	}
	
    @RequestMapping(method=RequestMethod.POST, value="/cm/ops/procedures")
    @ResponseBody
    public CmsOpsProcedure createOpsProcedure( 
    		@RequestHeader(value="X-Cms-User", required = false)  String user,
    		@RequestBody CmsOpsProcedure procedure ) {
    	
    	if (procedure.getProcedureCiId() == 0 && procedure.getDefinition() == null) {
    		throw new CIValidationException(CmsError.VALIDATION_PROCEDURE_ID_OR_DEFINITION_SHOULD_BE_ERROR,
                                        "Either one should be specified - ProcedureCiId or definition");
    	}
    	try {
	    	procedure.setCreatedBy(user);
			return opsManager.submitProcedure(procedure);
    	} catch (OpsException oe) {
    		logger.error(oe);
    		//we need to rethrow it so the spring handler will catch it (for some reason it does not if the exception is from down the stack)
    		throw oe;
    	}
    }

    
    @RequestMapping(value="/cm/ops/procedures/{procedureId}", method = RequestMethod.GET)
    @ResponseBody
    public CmsOpsProcedure getOpsProcedureExt(@PathVariable long procedureId, 
    		@RequestParam(value="definition", required = false) Boolean includeDef){
    	boolean incd = false;
    	if (includeDef != null) {
    		incd = includeDef.booleanValue();
    	}
        return opsManager.getCmsOpsProcedure(procedureId,incd);
    }



	/**
     * Returns the list of <code>CmsOpsProcedure<code> for specified params passed
     * If ciId is passed ,then nsPath will be ignored in the search. 
     * If nsPath is passed, search for procedures will happen via CI.
     * if recursive is <code>true</code> then search will use like
     * @param ciId the Id of the ci for which the procedure needs to be found 
     * @param actionCiId the actionId of the step .
     * @param nsPath the nspath of the CI.(/assembly/org/)
     * @param stateList the state (or comma separated list of states) in which the procedure is in. 
     * @param limit the number of CmsOpsProcedure returned, defaults to 10.
     * @param procedureName procedure name
     * @param actions true will populate the actions used with recursive flag
     * @return list of CmsOpsProcedure
     * @see CmsOpsProcedure 
     * @see OpsProcedureState
     */
    @RequestMapping(value="/cm/ops/procedures", method = RequestMethod.GET)
    @ResponseBody
    public List<CmsOpsProcedure> getOpsProcedureForCi(@RequestParam(value="ciId", required = false) Long ciId,
    												  @RequestParam(value="actionCiId", required = false) Long actionCiId,	
                                                      @RequestParam(value="nsPath", required = false)  String nsPath,
                                                      @RequestParam(value="state", required = false)  List<OpsProcedureState> stateList,
                                                      @RequestParam(value="limit", required = false)  Integer limit,
                                                      @RequestParam(value="procedureName", required = false)  String procedureName,
                                                      @RequestParam(value="recursive", required = false)  Boolean recursive,
                                                      @RequestParam(value="actions", required = false)  Boolean actions
                                                      ){
    	long startingTime = System.currentTimeMillis();
    	List<CmsOpsProcedure> opsProcedures;
    	if(ciId != null) {
    		opsProcedures = opsManager.getCmsOpsProcedureForCi(ciId, stateList, procedureName, limit);
        } else if(actionCiId != null) {
        	opsProcedures =opsManager.getCmsOpsProcedureForCiByAction(actionCiId, stateList, procedureName, limit);
        	
		} else if (nsPath != null) {
			if (recursive != null && recursive) {
				opsProcedures= opsManager.getCmsOpsProcedureForNamespaceLike(nsPath,
						stateList, procedureName, limit,actions);
			} else {
				opsProcedures= opsManager.getCmsOpsProcedureForNamespace(nsPath, stateList,
						procedureName);
			}

		}  else {
            throw new OpsException(CmsError.OPS_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR,
                                        "Parameter ciId or nsPath must present.");
        }
    	long timeTook = System.currentTimeMillis() - startingTime;
    	logger.debug("Time taken to get opsproc for ciId:<"+ciId  +":actionCiId:"+actionCiId+":nsPath:"+nsPath+":state(s):"+stateList+":limit:"+limit+":procedureName:"+procedureName+":recursive:"+recursive+" > "+timeTook);
    	return opsProcedures;
    }

    @RequestMapping(value="/cm/ops/procedures/{procedureId}/actionorders", method = RequestMethod.PUT)
    @ResponseBody
    public String completeActionOrders(@RequestBody CmsActionOrderSimple aos, @PathVariable long procedureId){
        aos.setProcedureId(procedureId);
        CmsActionOrder ao = cmsUtil.custSimple2ActionOrder(aos);
        opsManager.completeActionOrder(ao);
        return "done!";
    }

    @RequestMapping(method=RequestMethod.GET, value="/cm/ops/procedures/{procedureId}/updatestate")
    @ResponseBody
    public CmsOpsProcedure updateOpsProcedureState(
    		@RequestParam(value="state", required = true) OpsProcedureState state,
            @PathVariable long procedureId ) throws OpsException {

        return opsManager.updateProcedureState(procedureId, state);
    }

    @RequestMapping(method=RequestMethod.GET, value="/cm/ops/procedures/{procedureId}/retry")
    @ResponseBody
    public CmsOpsProcedure retryOpsProcedure(
            @PathVariable long procedureId ) throws OpsException {
        return opsManager.retryOpsProcedure(procedureId);
    }
    
    
    @RequestMapping(method=RequestMethod.PUT, value="/cm/ops/procedures/{procedureId}")
    @ResponseBody
    public CmsOpsProcedure updateOpsProcedure(
            @RequestBody CmsOpsProcedure proc,
            @PathVariable long procedureId ) throws OpsException {

        proc.setProcedureId(procedureId);
        return opsManager.updateOpsProcedure(proc);
    }
   
    
    @RequestMapping(method=RequestMethod.PUT, value="/cm/ops/procedures/{procedureId}/actions")
    @ResponseBody
    public CmsOpsAction updateOpsAction(
            @RequestBody CmsOpsAction action,
            @PathVariable long procedureId) throws OpsException {

        action.setProcedureId(procedureId);
        return opsManager.updateOpsAction(action);
    }

	@RequestMapping(value="/cm/simple/environments/{envId}/state", method = RequestMethod.GET)
	@ResponseBody
	public Map<Long,List<Long>> getEnvState(
				@PathVariable long envId,
				@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		
		CmsCI ci = cmManager.getCiById(envId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
												"There is no ci with this id");
		scopeVerifier.verifyScope(scope, ci);
		return cmManager.getEnvState(envId);
	}
    
	@RequestMapping(value="/cm/simple/vars", method = RequestMethod.GET)
	@ResponseBody
	public String updateCmSimpleVar(@RequestParam(value = "name" , required = true) String varName,
			@RequestParam(value = "value" , required = true) String varValue,
			@RequestHeader(value="X-Cms-User", required = false)  String user){
		if (varName != null && varValue != null) {
			cmManager.updateCmSimpleVar(varName, varValue, user!=null?user:"oneops-system");
	        return "cms var '"+ varName + "' updated";
		}
		return "";
		
	}
	
	@RequestMapping(value="/cm/complex/vars", method = RequestMethod.PUT)
	@ResponseBody
	public String updateCmComplexVar(@RequestHeader(value="X-Cms-User", required = false)  String user,
			@RequestBody CmsVar cmsVar){
			cmManager.updateCmSimpleVar(cmsVar.getName(), cmsVar.getValue(), user!=null?user:"oneops-system");
			return "cms var '"+ cmsVar.getName() + "' updated";
		
	}
	
	@RequestMapping(value="/cm/simple/{varName}/vars", method = RequestMethod.GET)
	@ResponseBody
	public CmsVar getCmSimpleVar(@PathVariable String varName){
		return cmManager.getCmSimpleVar(varName);
	}


	@RequestMapping(method = RequestMethod.PUT, value = "/cm/simple/ci/{ciId}/altNs")
	@ResponseBody
	public void tagRfc(@PathVariable long ciId,
					   @RequestParam(value = "tag", required = false) String tag,
					   @RequestParam(value = "altNsPath", required = false) String altNsPath,
					   @RequestParam(value = "altNsId", required = false) Long altNsId,
					   @RequestHeader(value = "X-Cms-User", required = false) String userId,
					   @RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {

		CmsCI ci = cmManager.getCiById(ciId);
		scopeVerifier.verifyScope(scope, ci);
		CmsAltNs cmsAltNs = new CmsAltNs();
		cmsAltNs.setNsPath(altNsPath);
		if (altNsId != null) {
			cmsAltNs.setNsId(altNsId);
		}
		cmsAltNs.setTag(tag);
		cmManager.createAltNs(cmsAltNs, ci);
	}


	@RequestMapping(method = RequestMethod.GET, value = "/cm/simple/ci/{ciId}/altNs")
	@ResponseBody
	public List<CmsAltNs> getCiTags(@PathVariable long ciId,
									@RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {

		CmsCI baseCi = cmManager.getCiById(ciId);
		scopeVerifier.verifyScope(scope, baseCi);
		return  cmManager.getAltNsBy(ciId);
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/cm/simple/ci/{ciId}/ns/{nsId}/altNs")
	@ResponseBody
	public String getCiTags(@PathVariable long ciId, @PathVariable long nsId, 
									@RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {
		CmsCI baseCi = cmManager.getCiById(ciId);
		scopeVerifier.verifyScope(scope, baseCi);
		cmManager.deleteAltNs(ciId, nsId);
		return  "";
	}


}
