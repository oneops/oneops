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

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.ds.CmsDataHelper;
import com.oneops.cms.ds.ReadOnlyDataAccess;
import com.oneops.cms.exceptions.*;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import com.oneops.cms.ws.exceptions.CmsSecurityException;
import com.oneops.cms.ws.rest.util.CmsScopeVerifier;
import com.oneops.cms.ws.rest.util.RelationParam;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class CmRestController extends AbstractRestController {


	private CmsUtil cmsUtil;
	private CmsCmManager cmManager;
	private OpsManager opsManager;
	private CmsScopeVerifier scopeVerifier;
	private static final boolean ENABLE_FORCE_EXECUTION = Boolean.valueOf(System.getProperty("adapter.proc.forceExecution", "false"));

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

    @ExceptionHandler({OpsException.class})
	public void handleOpsException(OpsException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_NOT_FOUND,e);
	}
	
    @ExceptionHandler({ResourceNotFoundException.class})
	public void handleOpsException(ResourceNotFoundException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_NOT_FOUND,e);
	}


	@RequestMapping(value="/cm/cis/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	@ReadOnlyDataAccess
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
	@ReadOnlyDataAccess
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
	@ReadOnlyDataAccess
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
	@ReadOnlyDataAccess
	public CmsCISimple getCISimpleById(@PathVariable long ciId, 
			@RequestParam(value="value", required = false) String valueType,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestParam(value="includeAltNs", required = false)  String includeAltNs,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		
		CmsCI ci = cmManager.getCiById(ciId);

		if (ci == null) throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
                                        "There is no ci with this id");

		scopeVerifier.verifyScope(scope, ci);

		return cmsUtil.custCI2CISimple(ci, valueType, attrProps, getEncrypted != null, includeAltNs);
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


    @RequestMapping(value = "/cm/simple/cis/list", method = RequestMethod.POST)
    @ResponseBody
		@ReadOnlyDataAccess
    public List<CmsCISimple> getCIcByIds(
            @RequestBody CiListRequest request,
            @RequestHeader(value = "X-Cms-Scope", required = false) String scope) {
        List<CmsCI> ciList = cmManager.getCiByIdList(request.getIds());
        for (CmsCI ci : ciList) {
            scopeVerifier.verifyScope(scope, ci);
        }

        return buildCiSimpleList(ciList, "df", request.attrProps(), false, request.altNsTag());
    }

    @JsonDeserialize(using = CiListRequestDeserializer.class)
    static class CiListRequest {
        private List<Long> ids;
        private String attrProps;
        private String altNsTag;

        CiListRequest(List<Long> ids) {
            this.ids = ids;
        }

        CiListRequest(List<Long> ids, String attrProps, String altNsTag) {
            this.ids = ids;
            this.attrProps = attrProps;
            this.altNsTag = altNsTag;
        }

        List<Long> getIds() {
            return ids;
        }

        String attrProps() {
            return attrProps;
        }

        String altNsTag() {
            return altNsTag;
        }
    }

    static class CiListRequestDeserializer extends JsonDeserializer<CiListRequest> {
        @Override
        public CiListRequest deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            JsonNode node = jsonParser.getCodec().readTree(jsonParser);
            if (node.getNodeType() == JsonNodeType.ARRAY) {
                return new CiListRequest(getIds(node));
            }
            else {
                JsonNode attrProps = node.get("attrProps");
                JsonNode altNsTag = node.get("includeAltNs");
                return new CiListRequest(getIds(node.get("ids")),
                        attrProps == null ? null :attrProps.asText(),
                        altNsTag == null ? null : altNsTag.asText());
            }
        }

        private List<Long> getIds(JsonNode node) {
            List<Long> ids = new ArrayList<>();
            Iterator<JsonNode> idNodes = node.elements();
            while (idNodes.hasNext()) {
                ids.add(idNodes.next().asLong());
            }
            return ids;
        }
    }

	@RequestMapping(value="/cm/simple/cis", method = RequestMethod.GET)
	@ResponseBody
	@ReadOnlyDataAccess
	public List<CmsCISimple> getCISimpleQuery(
			@RequestParam(value="nsPath", required = false) String nsPath,
			@RequestParam(value="ciClassName", required = false) String clazzName, 
			@RequestParam(value="ciName", required = false) String ciName,
			@RequestParam(value="attr", required = false)  String[] attrs,
			@RequestParam(value="ids", required = false)  String ids,
			@RequestParam(value="value", required = false)  String valueType,
			@RequestParam(value="includeAltNs", required = false)  String includeAltNs,
			@RequestParam(value="altNs", required = false)  String altNs,
			@RequestParam(value="altNsTag", required = false)  String altNsTag,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="getEncrypted", required = false) String getEncrypted,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
	
		List<CmsCISimple> ciSimpleList;
		
		if (attrs != null) {
            scopeVerifier.verifyScope(scope, nsPath);
			boolean nsRecursive = recursive != null;
			List<AttrQueryCondition> attrConds = cmsUtil.parseConditions(attrs);
			List<CmsCI> ciList;
			if (altNs != null || altNsTag != null) {
                ciList = cmManager.getCiByAttributes(nsPath, clazzName, attrConds, nsRecursive, altNs, altNsTag);
            } else {
                ciList = cmManager.getCiByAttributes(nsPath, clazzName, attrConds, nsRecursive);
            }
			ciSimpleList = buildCiSimpleList(ciList, valueType, attrProps, getEncrypted != null, altNsTag == null ? includeAltNs : altNsTag);
		} else if (ids != null) {
			String[] ciIdsAr = ids.split(",");
	        List<Long> ciIds = new ArrayList<>();
	        for (String ciId : ciIdsAr) {
	            ciIds.add(Long.valueOf(ciId));
	        }

            List<CmsCI> ciList = cmManager.getCiByIdList(ciIds);
            for (CmsCI ci : ciList) {
                scopeVerifier.verifyScope(scope, ci);
            }

            ciSimpleList = buildCiSimpleList(ciList, valueType, attrProps, getEncrypted != null, includeAltNs);
		} else {
            scopeVerifier.verifyScope(scope, nsPath);
            List<CmsCI> ciList;
            if (altNs != null || altNsTag != null) {
                ciList = cmManager.getCmCIByAltNsAndTag(nsPath, clazzName, altNs, altNsTag, recursive != null);
            } else if (recursive != null && recursive) {
                ciList = cmManager.getCiBy3NsLike(nsPath, clazzName, ciName);
            } else {
                ciList = cmManager.getCiBy3(nsPath, clazzName, ciName);
            }
            ciSimpleList = buildCiSimpleList(ciList, valueType, attrProps, getEncrypted != null, altNsTag == null ? includeAltNs : altNsTag);
        }

		return ciSimpleList;
	}

    private List<CmsCISimple> buildCiSimpleList(List<CmsCI> ciList, String valueType, String attrProps, boolean getEncrypted, String altNsTag) {
        return ciList.stream()
                .map(ci -> cmsUtil.custCI2CISimple(ci, valueType == null ? "df" : valueType, attrProps, getEncrypted, altNsTag))
                .collect(Collectors.toList());
    }

	@RequestMapping(value="/cm/simple/cis/count", method = RequestMethod.GET)
	@ResponseBody
	@ReadOnlyDataAccess
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
			updateAltNs(ci.getCiId(), ciSimple);
			logger.debug(ci.getCiId());
			CmsCISimple cmsCISimple = cmsUtil.custCI2CISimple(ci, valueType);
			cmsCISimple.setAltNs(ciSimple.getAltNs());
			return cmsCISimple;
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
		updateAltNs(ciSimple.getCiId(), ciSimple);
		CmsCISimple cmsCISimple = cmsUtil.custCI2CISimple(ci, valueType);
		cmsCISimple.setAltNs(ciSimple.getAltNs());
		return cmsCISimple;
	}

	@RequestMapping(method=RequestMethod.POST, value="/cm/simple/cis/bulk")
	@ResponseBody
	public List<CmsCISimple> createOrUpdateCISimpleBulk(
			@RequestParam(value="value", required = false)  String valueType,
			@RequestBody CmsCISimple[] cis,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {

		List<CmsCISimple> result = new ArrayList<>();
		for (CmsCISimple ci : cis) {
			long ciId = ci.getCiId();
			result.add(ciId == 0 ? createCISimple(valueType, ci, scope, userId) : updateCISimple(ciId, valueType, ci, scope, userId));
		}
		return result;
	}


	private void updateAltNs(long ciId, CmsCISimple ciSimple) {
		Map<String, Set<String>> altNs = ciSimple.getAltNs();
		if (altNs !=null && altNs.size()!=0){
			cmManager.updateCiAltNs(ciId, altNs);
		}
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
	@ReadOnlyDataAccess
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
		CmsDataHelper<RelationParam, Map<String, Long>> dataHelper = new CmsDataHelper<>();
		RelationParam param = new RelationParam(nsPath, relationName, shortRelationName, targetClazz, recursive);
		param.setCiId(ciId);
		param.setDirection(direction);
		param.setGroupBy(groupBy);
		return dataHelper.execute(this::getRelationsCountsInternal, param, isOrgLevelRecursiveAccess(nsPath, recursive), "getRelationsCounts");
	}

	private Map<String, Long> getRelationsCountsInternal(RelationParam param) {
		if (param.getGroupBy() != null) {
			if ("ciId".equals(param.getGroupBy())) {
				Map<Long, Long> counts;
				if ("from".equals(param.getDirection())) {
					counts = cmManager.getCounCIRelationsGroupByFromCiId(param.getRelationName(), param.getShortRelationName(),
							param.getTargetClazz(), param.getNsPath());
				} else {
					counts = cmManager.getCounCIRelationsGroupByToCiId(param.getRelationName(), param.getShortRelationName(),
							param.getTargetClazz(), param.getNsPath());
				}
				//convert to Map<String,Long>
				Map<String, Long> result = new HashMap<>(1);
				for (Map.Entry<Long, Long> count : counts.entrySet()) {
					result.put(count.getKey().toString(), count.getValue());
				}
				return result;
			} else {
				if ("from".equals(param.getDirection())) {
					return cmManager.getCountFromCIRelationsGroupByNs(param.getCiId(), param.getRelationName(),
							param.getShortRelationName(), param.getTargetClazz(), param.getNsPath());
				} else {
					return cmManager.getCountToCIRelationsGroupByNs(param.getCiId(), param.getRelationName(),
							param.getShortRelationName(), param.getTargetClazz(), param.getNsPath());
				}
			}
		} else {
			Long count;
			if ("from".equals(param.getDirection())) {
				count = cmManager.getCountFromCIRelationsByNS(param.getCiId(), param.getRelationName(),
						param.getShortRelationName(), param.getTargetClazz(), param.getNsPath(), param.isRecursive());
			} else {
				count = cmManager.getCountToCIRelationsByNS(param.getCiId(), param.getRelationName(),
						param.getShortRelationName(), param.getTargetClazz(), param.getNsPath(), param.isRecursive());
			}
			Map<String, Long> result = new HashMap<>(1);
			result.put("count", count);
			return result;
		}
	}

	private boolean isOrgLevelRecursiveAccess(String nsPath, Boolean recursive) {
		if (nsPath != null && nsPath.length() > 1 && nsPath.endsWith("/")) {
			nsPath = nsPath.substring(0, nsPath.length() - 1);
		}
		return recursive && nsPath.split("/").length <= 2;
	}


	@RequestMapping(value="/cm/simple/relations", method = RequestMethod.GET)
	@ResponseBody
	@ReadOnlyDataAccess
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
			@RequestParam(value="attrProps", required = false) String attrProps,
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
				RelationParam param = new RelationParam(nsPath, relationName, shortRelationName, targetClazz, recursive);
				param.setFromClazz(fromClazz);
				relList = getNsLikeRelations(param);

			} else {	
				relList = cmManager.getCIRelations(nsPath, relationName, shortRelationName, fromClazz, targetClazz);
			}
			cmManager.populateRelCis(relList, includeFromCi != null, includeToCi != null);
		} else {
			throw new DJException(CmsError.DJ_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR,
                                            "You must specify either ciId or nsPath ");
		}

		String[] relationAttrProps = null;
		if (attrProps != null) {
			relationAttrProps = attrProps.split(",");
		}

		List<CmsCIRelationSimple> simpleList = new ArrayList<>();
		for (CmsCIRelation rel : relList) {
			scopeVerifier.verifyScope(scope, rel);
			simpleList.add(cmsUtil.custCIRelation2CIRelationSimple(rel, valueType, getEncrypted!=null, relationAttrProps));
		}
		return simpleList;
	}

	private List<CmsCIRelation> getNsLikeRelations(RelationParam param) {
		return cmManager.getCIRelationsNsLike(param.getNsPath(), param.getRelationName(),
				param.getShortRelationName(), param.getFromClazz(), param.getTargetClazz());
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
		String[] attrProps = null;
		if (relSimple.getRelationAttrProps().size() >0) {
			attrProps = relSimple.getRelationAttrProps().keySet().toArray(new String[relSimple.getRelationAttrProps().size()]);
		}
		return cmsUtil.custCIRelation2CIRelationSimple(newRel, valueType,false, attrProps);
	}

	@RequestMapping(method=RequestMethod.POST, value="/cm/simple/relations/bulk")
	@ResponseBody
	public List<CmsCIRelationSimple> createOrUpdateCIRelationBulk(
			@RequestParam(value="value", required = false)  String valueType,
			@RequestBody CmsCIRelationSimple[] relations,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws CIValidationException {
		List<CmsCIRelationSimple> result = new ArrayList<>();
		for (CmsCIRelationSimple relation : relations) {
			long id = relation.getCiRelationId();
			result.add(id == 0 ? createCIRelation(valueType, relation, scope, userId) : updateCIRelation(id, valueType, relation, scope, userId));
		}
		return result;
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
	    	if(ENABLE_FORCE_EXECUTION){
	    		  logger.info("Executing forcefully procedure "+procedure.getProcedureName() +" on ciId " +procedure.getCiId() );
					procedure.setForceExecution(true);
			}
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
			cmManager.updateCmSimpleVar(varName, varValue, null, user!=null?user:"oneops-system");
	        return "cms var '"+ varName + "' updated";
		}
		return "";
		
	}
	
	@RequestMapping(value="/cm/complex/vars", method = RequestMethod.PUT)
	@ResponseBody
	public String updateCmComplexVar(@RequestHeader(value="X-Cms-User", required = false)  String user,
			@RequestBody CmsVar cmsVar){
			cmManager.updateCmSimpleVar(cmsVar.getName(), cmsVar.getValue(),cmsVar.getCriteria(), user!=null?user:"oneops-system");
			return "cms var '"+ cmsVar.getName() + "' updated";
		
	}
	
	@RequestMapping(value="/cm/simple/{varName}/vars", method = RequestMethod.GET)
	@ResponseBody
	public CmsVar getCmSimpleVar(@PathVariable String varName){
		return cmManager.getCmSimpleVar(varName);
	}

	@RequestMapping(value="/cm/simple/vars/{varName}", method = RequestMethod.GET)
	@ResponseBody
	public CmsVar getCmSimpleVar2(@PathVariable String varName,
								 @RequestParam(value = "criteria", required = false) String criteria){
		CmsVar var = criteria == null ? cmManager.getCmSimpleVar(varName) : cmManager.getCmSimpleVar(varName, criteria);
		if (var == null) throw new ResourceNotFoundException();
		return var;
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
									@RequestParam(value = "tag", required = false) String tag,
									@RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {
		if (scope != null) {
			CmsCI baseCi = cmManager.getCiById(ciId);
			scopeVerifier.verifyScope(scope, baseCi);
		}
		return  cmManager.getAltNsByCiAndTag(ciId, tag);
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

	@RequestMapping(value="/cm/vars/criteria", method = RequestMethod.GET)
	@ResponseBody
	public CmsVar getCmVarCriteria(@RequestParam String var,
			@RequestParam String criteria){
		return cmManager.getCmVarByLongestMatchingCriteria(var, criteria);
	}

}
