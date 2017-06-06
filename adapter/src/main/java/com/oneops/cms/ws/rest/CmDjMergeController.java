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
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmDjManager;
import com.oneops.cms.dj.service.CmsDjManager;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsRfcRelationSimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.ws.exceptions.CmsSecurityException;
import com.oneops.cms.ws.rest.util.CmsScopeVerifier;

@Controller
public class CmDjMergeController extends AbstractRestController {

	private CmsCmDjManager cmdjManager;
	private CmsDjManager djManager;
	private CmsUtil cmsUtil;
	private CmsScopeVerifier scopeVerifier; 
	
	@Autowired
    public void setCmsUtil(CmsUtil cmsUtil) {
		this.cmsUtil = cmsUtil;
	}
	
	
	public void setScopeVerifier(CmsScopeVerifier scopeVerifier) {
		this.scopeVerifier = scopeVerifier;
	}

	public void setCmdjManager(CmsCmDjManager cmdjManager) {
		this.cmdjManager = cmdjManager;
	}

	public void setDjManager(CmsDjManager djManager) {
		this.djManager = djManager;
	}
	
	@ExceptionHandler(DJException.class)
	public void handleDJExceptions(DJException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CIValidationException.class)
	public void handleCIValidationExceptions(CIValidationException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}

	@ExceptionHandler(CmsSecurityException.class)
	public void handleCmsSecurityException(CmsSecurityException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_FORBIDDEN,e);
	}
	
	
	@RequestMapping(value="/dj/simple/cis/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcCISimple getRfcById(@PathVariable long ciId, 
			@RequestParam(value="releaseId", required = false) Long releaseId,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

        CmsRfcCI rfc = cmdjManager.getCiById(ciId, "df");

        if (rfc == null) throw new DJException(CmsError.DJ_NO_CI_WITH_GIVEN_ID_ERROR,
                "There is no rfc or ci with this id: " + ciId);

        scopeVerifier.verifyScope(scope, rfc);

        return cmsUtil.custRfcCI2RfcCISimple(rfc, attrProps);
	}

	@RequestMapping(value="/dj/simple/cis/{ciId}/history", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRfcCISimple> getCiRfcHistory(
			@PathVariable long ciId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsRfcCI> rfcList = djManager.getClosedRfcCIByCiId(ciId);
		List<CmsRfcCISimple> rfcSimpleList = new ArrayList<>();
		for (CmsRfcCI rfc : rfcList) {
			scopeVerifier.verifyScope(scope, rfc);
			rfcSimpleList.add(cmsUtil.custRfcCI2RfcCISimple(rfc));
		}
		return rfcSimpleList;
	}
	
	@RequestMapping(value="/dj/simple/cis/{ciId}/records", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDpmtRecord> getCiDpmtRecords(
			@PathVariable long ciId,
			@RequestParam(value="state", required = false) String state,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
	
		return djManager.getDeploymentRecordByCiId(ciId, state);
	}
	
	
	@RequestMapping(value="/dj/simple/cis", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRfcCISimple> getRfcQuery(
			@RequestParam(value="nsPath", required = false) String nsPath,
			@RequestParam(value="ciClassName", required = false) String clazzName, 
			@RequestParam(value="ciName", required = false) String ciName,
			@RequestParam(value="value", required = false)  String valueType,
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestParam(value="attr", required = false)  String[] attrs,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsRfcCI> rfcList;
		
		if (attrs != null) {
			List<AttrQueryCondition> attrConds = cmsUtil.parseConditions(attrs); 
			rfcList = cmdjManager.getDfDjCi(nsPath, clazzName, ciName, valueType,attrConds);
		} else {
			if (recursive != null && recursive) {
				rfcList = cmdjManager.getDfDjCiNsLike(nsPath, clazzName, ciName, valueType);
			} else {
				rfcList = cmdjManager.getDfDjCi(nsPath, clazzName, ciName, valueType);
			}
		}
		List<CmsRfcCISimple> rfcListSimple = new ArrayList<>();
		for (CmsRfcCI rfcCi : rfcList) {
			scopeVerifier.verifyScope(scope, rfcCi);
			CmsRfcCISimple rfcSimple = cmsUtil.custRfcCI2RfcCISimple(rfcCi, attrProps);
			rfcListSimple.add(rfcSimple);
		}
		
		return rfcListSimple;
	}
	
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/cis")
	@ResponseBody
	public CmsRfcCISimple createRfcCi(@RequestBody CmsRfcCISimple rfcSimple,
			                          @RequestHeader(value="X-Cms-User", required = false)  String userId,
			                          @RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {
		
		scopeVerifier.verifyScope(scope, rfcSimple);
		
		CmsRfcCI rfc = cmsUtil.custRfcCISimple2RfcCI(rfcSimple);
		String[] attrProps = null; 
		if (rfcSimple.getCiAttrProps().size() >0) {
			attrProps = rfcSimple.getCiAttrProps().keySet().toArray(new String[rfcSimple.getCiAttrProps().size()]);
		}
		rfc.setCreatedBy(userId);
		rfc.setUpdatedBy(userId);
		return cmsUtil.custRfcCI2RfcCISimple(cmdjManager.upsertCiRfc(rfc, userId), attrProps);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/cis/{ciId}")
	@ResponseBody
	public CmsRfcCISimple updateRfcCi(@PathVariable long ciId, 
									  @RequestBody CmsRfcCISimple rfcSimple,
									  @RequestHeader(value="X-Cms-User", required = false)  String userId,
									  @RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		
		scopeVerifier.verifyScope(scope, rfcSimple);
		
		rfcSimple.setCiId(ciId);
		CmsRfcCI rfc = cmsUtil.custRfcCISimple2RfcCI(rfcSimple);
		String[] attrProps = null; 
		if (rfcSimple.getCiAttrProps().size() >0) {
			attrProps = rfcSimple.getCiAttrProps().keySet().toArray(new String[rfcSimple.getCiAttrProps().size()]);
		}
		rfc.setUpdatedBy(userId);
		return cmsUtil.custRfcCI2RfcCISimple(cmdjManager.upsertCiRfc(rfc, userId), attrProps);
	}

	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/cis/{ciId}/touch")
	@ResponseBody
	public CmsRfcCISimple touchCi(@PathVariable long ciId, 
									  @RequestBody CmsRfcCISimple rfcSimple,
									  @RequestHeader(value="X-Cms-User", required = false)  String userId,
									  @RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		CmsRfcCI baseCi = cmdjManager.getCiById(ciId, null);
		scopeVerifier.verifyScope(scope, baseCi);
		
		rfcSimple.setCiId(ciId);
		rfcSimple.setNsPath(baseCi.getNsPath());
		CmsRfcCI rfc = cmsUtil.custRfcCISimple2RfcCI(rfcSimple);
		return cmsUtil.custRfcCI2RfcCISimple(cmdjManager.touchCi(rfc, userId));
	}
	
	
	@RequestMapping(method=RequestMethod.DELETE, value="/dj/simple/cis/{ciId}")
	@ResponseBody
	public void deleteCi(@PathVariable long ciId, 
						 @RequestHeader(value="X-Cms-User", required = false)  String userId,
						 @RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		if (scope != null) {
			CmsRfcCI rfc = cmdjManager.getCiById(ciId, "df");
			scopeVerifier.verifyScope(scope, rfc);
		}
		
		if (userId == null) userId = "oneops";
		cmdjManager.deleteCi(ciId, userId);
		return;
	}

	
	@RequestMapping(value="/dj/simple/relations", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRfcRelationSimple> getCIRelationSimpleQuery(
			@RequestParam(value="ciId", required = false) Long ciId,
			@RequestParam(value="releaseId", required = false) Long releaseId,
			@RequestParam(value="direction", required = false) String direction, 
			@RequestParam(value="nsPath", required = false) String nsPath, 
			@RequestParam(value="recursive", required = false)  Boolean recursive,
			@RequestParam(value="fromClassName", required = false) String fromClazz,
			@RequestParam(value="relationName", required = false) String relationName,
			@RequestParam(value="relationShortName", required = false) String shortRelationName,
			@RequestParam(value="targetClassName", required = false) String targetClazz,
			@RequestParam(value="includeFromCi", required = false) Boolean includeFromCi,
			@RequestParam(value="includeToCi", required = false) Boolean includeToCi,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestParam(value="attr", required = false)  String[] attrs,
			@RequestParam(value="value", required = false)  String valueType,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		

		List<CmsRfcRelationSimple> relSimpleList = new ArrayList<>();
		boolean incFromCi = includeFromCi == null ? false : includeFromCi;
		boolean incToCi = includeToCi == null ? false : includeToCi;
		List<AttrQueryCondition> attrConds = null;
		if (attrs != null) {
			attrConds = cmsUtil.parseConditions(attrs); 
		}
		
		if (ciId != null) {
			if ("from".equalsIgnoreCase(direction)) {
				List<CmsRfcRelation> relList;
				if (attrConds != null) {
					relList = cmdjManager.getFromCIRelations(ciId, relationName, shortRelationName, targetClazz, valueType, attrConds);
				} else {
					relList = cmdjManager.getFromCIRelations(ciId, relationName, shortRelationName, targetClazz, valueType);
				}
				for (CmsRfcRelation rel : relList) {
					CmsRfcRelationSimple relSimple = cmsUtil.custRfcRel2RfcRelSimple(rel, attrProps);
					CmsRfcCISimple rfcCiSimple = cmsUtil.custRfcCI2RfcCISimple(rel.getToRfcCi(), attrProps);
					relSimple.setToCi(rfcCiSimple);
					relSimpleList.add(relSimple);
				}
			} else {
				List<CmsRfcRelation> relList;
				if (attrConds != null) {
					relList = cmdjManager.getToCIRelations(ciId, relationName, shortRelationName, targetClazz, valueType, attrConds);
				} else {
					relList = cmdjManager.getToCIRelations(ciId, relationName, shortRelationName, targetClazz, valueType);
				}
				for (CmsRfcRelation rel : relList) {
					CmsRfcRelationSimple relSimple = cmsUtil.custRfcRel2RfcRelSimple(rel, attrProps);
					CmsRfcCISimple rfcCiSimple = cmsUtil.custRfcCI2RfcCISimple(rel.getFromRfcCi(), attrProps);
					relSimple.setFromCi(rfcCiSimple);
					relSimpleList.add(relSimple);
				}
			}
		} else if (nsPath != null){
			List<CmsRfcRelation> relList;
			if (recursive != null && recursive) {
				relList = cmdjManager.getDfDjRelationsNsLikeWithCIs(relationName, shortRelationName, nsPath, fromClazz, targetClazz, valueType, incFromCi, incToCi, attrConds);
			} else {
				relList = cmdjManager.getDfDjRelationsWithCIs(relationName, shortRelationName, nsPath, fromClazz, targetClazz, valueType, incFromCi, incToCi, attrConds);
			}
			for (CmsRfcRelation rel : relList) {
				CmsRfcRelationSimple relSimple = cmsUtil.custRfcRel2RfcRelSimple(rel, attrProps);
				relSimpleList.add(relSimple);
			}
		} else {
			throw new DJException(CmsError.DJ_MUST_SPECIFY_CI_ID_OR_NSPATH_ERROR,
                                            "You must specify either ciId or nsPath ");
		}
		
		if (scope != null) {
			for (CmsRfcRelationSimple rel : relSimpleList) {
				scopeVerifier.verifyScope(scope, rel);
			}
		}	
		return relSimpleList;
	}

	@RequestMapping(value="/dj/simple/relations/{ciRelationId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcRelationSimple getCIRelationSimpleById(@PathVariable long ciRelationId,
			@RequestParam(value="includeFromCi", required = false) String includeFromCi,
			@RequestParam(value="includeToCi", required = false) String includeToCi,
			@RequestParam(value="releaseId", required = false) Long releaseId,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {

        CmsRfcRelation rel = cmdjManager.getCIRelation(ciRelationId, "df");

        if (rel == null) throw new DJException(CmsError.DJ_NO_RELATION_WITH_GIVEN_ID_ERROR,
                "There is no relation with this id: " + ciRelationId);

        scopeVerifier.verifyScope(scope, rel);

		CmsRfcRelationSimple relSimple = cmsUtil.custRfcRel2RfcRelSimple(rel, attrProps);

		if (includeFromCi != null) {
			CmsRfcCISimple fromCi = cmsUtil.custRfcCI2RfcCISimple(cmdjManager.getCiById(rel.getFromCiId(), "df"),attrProps);
			relSimple.setFromCi(fromCi);
		}
		
		if (includeToCi != null) {
			CmsRfcCISimple toCi = cmsUtil.custRfcCI2RfcCISimple(cmdjManager.getCiById(rel.getToCiId(), "df"), attrProps);
			relSimple.setToCi(toCi);
		}
		
		return relSimple;
	}

	
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/relations")
	@ResponseBody
	public CmsRfcRelationSimple createRfcRelation(
			@RequestBody CmsRfcRelationSimple relSimple,
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		
		scopeVerifier.verifyScope(scope, relSimple);
		
		CmsRfcRelation rel = cmsUtil.custRfcRelSimple2RfcRel(relSimple);
		
		String[] attrProps = null; 
		if (relSimple.getRelationAttrProps().size() >0) {
			attrProps = relSimple.getRelationAttrProps().keySet().toArray(new String[relSimple.getRelationAttrProps().size()]);
		}
		
		CmsRfcCISimple fromRfcCi = relSimple.getFromCi();
		if (fromRfcCi != null) rel.setFromRfcCi(cmsUtil.custRfcCISimple2RfcCI(fromRfcCi));

		CmsRfcCISimple toRfcCi = relSimple.getToCi();
		if (toRfcCi != null) rel.setToRfcCi(cmsUtil.custRfcCISimple2RfcCI(toRfcCi));
		
		rel.setCreatedBy(userId);
		rel.setUpdatedBy(userId);
		CmsRfcRelation newRel = cmdjManager.upsertRelationRfc(rel, userId);
		CmsRfcRelationSimple newRelSimple = cmsUtil.custRfcRel2RfcRelSimple(newRel,attrProps);
		if (newRel.getFromRfcCi() != null) {
			newRelSimple.setFromCi(cmsUtil.custRfcCI2RfcCISimple(newRel.getFromRfcCi(),attrProps));
		}
		
		if (newRel.getToRfcCi() != null) {
			newRelSimple.setToCi(cmsUtil.custRfcCI2RfcCISimple(newRel.getToRfcCi(), attrProps));
		}
		
		return newRelSimple;
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/relations/{ciRelationId}")
	@ResponseBody
	public CmsRfcRelationSimple updateRfcRelation(
			@PathVariable long ciRelationId, 
			@RequestBody CmsRfcRelationSimple relSimple,
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		
		scopeVerifier.verifyScope(scope, relSimple);

		String[] attrProps = null; 
		if (relSimple.getRelationAttrProps().size() >0) {
			attrProps = relSimple.getRelationAttrProps().keySet().toArray(new String[relSimple.getRelationAttrProps().size()]);
		}
		
		relSimple.setCiRelationId(ciRelationId);
		CmsRfcRelation rel = cmsUtil.custRfcRelSimple2RfcRel(relSimple);
		CmsRfcCISimple fromRfcCi = relSimple.getFromCi();
		if (fromRfcCi != null) rel.setFromRfcCi(cmsUtil.custRfcCISimple2RfcCI(fromRfcCi));

		CmsRfcCISimple toRfcCi = relSimple.getToCi();
		if (toRfcCi != null) rel.setToRfcCi(cmsUtil.custRfcCISimple2RfcCI(toRfcCi));

		rel.setCreatedBy(userId);
		rel.setUpdatedBy(userId);
		CmsRfcRelation newRel = cmdjManager.upsertRelationRfc(rel, userId);
		CmsRfcRelationSimple newRelSimple = cmsUtil.custRfcRel2RfcRelSimple(newRel,attrProps);
		if (newRel.getFromRfcCi() != null) {
			newRelSimple.setFromCi(cmsUtil.custRfcCI2RfcCISimple(newRel.getFromRfcCi(),attrProps));
		}
		
		if (newRel.getToRfcCi() != null) {
			newRelSimple.setToCi(cmsUtil.custRfcCI2RfcCISimple(newRel.getToRfcCi(),attrProps));
		}
		
		return newRelSimple;
	}

	@RequestMapping(method=RequestMethod.DELETE, value="/dj/simple/relations/{ciRelationId}")
	@ResponseBody
	public void deleteRelation(
			@PathVariable long ciRelationId, 
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) throws DJException {

		if (scope != null) {
			CmsRfcRelation rfc = cmdjManager.getCIRelation(ciRelationId, "df");
			scopeVerifier.verifyScope(scope, rfc);
		}
		
		if (userId == null) userId = "oneops";
		cmdjManager.deleteRelation(ciRelationId, userId);
		return;
	}
}
