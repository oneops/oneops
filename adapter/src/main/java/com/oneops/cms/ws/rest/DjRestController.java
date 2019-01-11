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

import com.oneops.cms.cm.domain.CmsAltNs;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.TimelineBase;
import com.oneops.cms.dj.service.CmsDjManager;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.simple.domain.CmsRfcRelationSimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.QueryOrder;
import com.oneops.cms.util.TimelineQueryParam;
import com.oneops.cms.ws.exceptions.CmsSecurityException;
import com.oneops.cms.ws.rest.util.CmsScopeVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class DjRestController extends AbstractRestController {
	
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
	
	
	@RequestMapping(value="/dj/simple/releases/{releaseId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRelease getReleaseById(
			@PathVariable long releaseId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		CmsRelease release = djManager.getReleaseById(releaseId);
		
		if (release == null) throw new DJException(CmsError.DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR,
                                    "There is no release with this id");
		scopeVerifier.verifyScope(scope, release);
		
		return release;
	}

	@RequestMapping(value="/dj/simple/releases/{releaseId}/commit", method = RequestMethod.GET)
	@ResponseBody
	public CmsRelease commitRelease(@PathVariable long releaseId,
			@RequestParam(value="setDfValue", required = false) Boolean setDfValue, 
			@RequestParam(value="newCiState", required = false) String newCiState,
			@RequestParam(value="desc", required = false) String desc,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId){

		if (setDfValue == null) {
			setDfValue = true;
		}
		
		if (scope != null) {
			CmsRelease release = djManager.getReleaseById(releaseId);
			if (release == null) throw new DJException(CmsError.DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR,
                                                "There is no release with this id");
			scopeVerifier.verifyScope(scope, release);
		}
		
		djManager.commitRelease(releaseId, setDfValue, newCiState, userId, desc);
		return djManager.getReleaseById(releaseId);
	}

	
	
	@RequestMapping(value="/dj/simple/releases", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRelease> getReleaseBy3(
			@RequestParam("nsPath") String nsPath,  
			@RequestParam(value="releaseName", required = false) String releaseName, 
			@RequestParam(value="releaseState", required = false) String releaseState,
			@RequestParam(value="latest", required = false) Boolean latest,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsRelease> relList;
		
		if (latest != null && latest.booleanValue()) {
			relList =  djManager.getLatestRelease(nsPath, releaseState);
		} else {
			relList = djManager.getReleaseBy3(nsPath, releaseName, releaseState);
		}
		
		if (scope != null) {
			for (CmsRelease rel : relList) {
				scopeVerifier.verifyScope(scope, rel);
			}
		}	
		
		return relList;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/releases")
	@ResponseBody
	public CmsRelease createRelease(
			@RequestBody CmsRelease release,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope, 
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {
		
		scopeVerifier.verifyScope(scope, release);
		release.setCreatedBy(userId);
		return djManager.createRelease(release);
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/releases/{releaseId}")
	@ResponseBody
	public CmsRelease updateRelease(
			@PathVariable long releaseId, 
			@RequestBody CmsRelease release,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {
		
		release.setReleaseId(releaseId);
		scopeVerifier.verifyScope(scope, release);
		release.setCommitedBy(userId);
		return djManager.updateRelease(release);
	}
	
	@RequestMapping(value="/dj/simple/releases/{releaseId}", method = RequestMethod.DELETE)
	@ResponseBody
	public String deleteRelease(
			@PathVariable long releaseId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (scope != null) {
			CmsRelease release = djManager.getReleaseById(releaseId);
			if (release == null) throw new DJException(CmsError.DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR,
                                            "There is no release with this id");
			scopeVerifier.verifyScope(scope, release);
		}
	
		long deleted =  djManager.deleteRelease(releaseId);
		return "{\"deleted\":" + deleted + "}";

	}
	
	//DJ CIs

	@RequestMapping(value="/dj/simple/rfc/cis/{rfcId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcCISimple getRfcById(
			@PathVariable long rfcId,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		CmsRfcCI rfc = djManager.getRfcCIById(rfcId);
		if (rfc == null) throw new DJException(CmsError.DJ_NO_RFC_WITH_GIVEN_ID_ERROR,
        										"There is no rfc with this id");
		scopeVerifier.verifyScope(scope, rfc);
		
		return cmsUtil.custRfcCI2RfcCISimple(rfc, attrProps == null ? null : attrProps.split(","));
	}
	
	
	@RequestMapping(value="/dj/rfc/cis/{rfcId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcCI getRfcByIdFull(
			@PathVariable long rfcId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		CmsRfcCI rfc = djManager.getRfcCIById(rfcId);
		if (rfc == null) throw new DJException(CmsError.DJ_NO_RFC_WITH_GIVEN_ID_ERROR,
                                                "There is no rfc with this id");

		scopeVerifier.verifyScope(scope, rfc);
	
		return rfc;
	}
	
	
	@RequestMapping(value="/dj/simple/rfc/cis", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRfcCISimple> getRfcCiBy3(
			@RequestParam(value="releaseId", required = false) Long releaseId,  
			@RequestParam(value="isActive", required = false) Boolean isActive, 
			@RequestParam(value="ciId", required = false) Long ciId,
			@RequestParam(value="nsPath", required = false) String nsPath,
            @RequestParam(value="altNsPath", required = false) String altNsPath, 
            @RequestParam(value="tag", required = false) String tag,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestParam(value="ciClassName", required = false) String ciClassName,
			@RequestParam(value="startDate", required = false) Long startDate,
			@RequestParam(value="endDate", required = false) Long endDate,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsRfcCISimple> rfcSimpleList = new ArrayList<>();
		List<CmsRfcCI> rfcList = null;
		if (isActive == null) {
			isActive = true;
		}
		if (altNsPath!=null || tag!=null){
		    rfcList = djManager.getByAltNsAndTag(altNsPath, tag, releaseId, isActive, ciId);
        } else if (releaseId != null) {
			rfcList = djManager.getRfcCIBy3(releaseId, isActive, ciId);
		} else if (ciId != null) {
			rfcList = djManager.getClosedRfcCIByCiId(ciId);
		} else if (nsPath!=null){
			if(startDate != null || endDate != null || ciClassName != null) {
				rfcList = djManager.getRfcCIByNsPathDateRangeClassName(nsPath, startDate == null ? null : new Date(startDate), endDate == null ? null : new Date(endDate), ciClassName);
			} else {
				rfcList = djManager.getRfcCIByNs(nsPath, isActive);
			}
		}
		
		if (rfcList != null) {
			for (CmsRfcCI rfc : rfcList) {
				scopeVerifier.verifyScope(scope, rfc);
				rfcSimpleList.add(cmsUtil.custRfcCI2RfcCISimple(rfc, attrProps == null ? null : attrProps.split(",")));
			}
		}

		return rfcSimpleList;
	}
	
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/rfc/cis")
	@ResponseBody
	public CmsRfcCISimple createRfcCi(
			@RequestBody CmsRfcCISimple rfcSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {

		scopeVerifier.verifyScope(scope, rfcSimple);
		CmsRfcCI rfc = cmsUtil.custRfcCISimple2RfcCI(rfcSimple);
		rfc.setCreatedBy(userId);
		return cmsUtil.custRfcCI2RfcCISimple(djManager.createRfcCI(rfc));
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/rfc/cis/{rfcId}")
	@ResponseBody
	public CmsRfcCISimple updateRfcCi(
			@PathVariable long rfcId, 
			@RequestBody CmsRfcCISimple rfcSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {
		
		scopeVerifier.verifyScope(scope, rfcSimple);

		rfcSimple.setRfcId(rfcId);
		CmsRfcCI rfc = cmsUtil.custRfcCISimple2RfcCI(rfcSimple);
		rfc.setUpdatedBy(userId);
		return cmsUtil.custRfcCI2RfcCISimple(djManager.updateRfcCI(rfc));
	}
	
	@RequestMapping(value="/dj/simple/rfc/cis/{rfcId}", method = RequestMethod.DELETE)
	@ResponseBody
	public String rmRfcCiFromRelease(
			@PathVariable long rfcId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (scope != null) {
			CmsRfcCI rfc = djManager.getRfcCIById(rfcId);
			if (rfc == null) throw new DJException(CmsError.DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR,
                                                    "There is no release with this id");
			scopeVerifier.verifyScope(scope, rfc);
		}

		long deleted =  djManager.rmRfcCiFromRelease(rfcId);
		return "{\"deleted\":" + deleted + "}";
	}

	// DJ Relations
	
	@RequestMapping(value="/dj/simple/rfc/relations/{rfcId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcRelationSimple getRfcRelationById(
			@PathVariable long rfcId,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		
		CmsRfcRelation rfc = djManager.getRfcRelationById(rfcId);
		
		if (rfc == null) throw new DJException(CmsError.DJ_NO_RFC_WITH_GIVEN_ID_ERROR,
                                                "There is no rfc relation with this id");

		scopeVerifier.verifyScope(scope, rfc);
		
		return cmsUtil.custRfcRel2RfcRelSimple(rfc, attrProps == null ? null : attrProps.split(","));
	}

	@RequestMapping(value="/dj/rfc/relations/{rfcId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRfcRelation getRfcRelationByIdFull(
			@PathVariable long rfcId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		CmsRfcRelation rfc = djManager.getRfcRelationById(rfcId);
		
		if (rfc == null) throw new DJException(CmsError.DJ_NO_RFC_WITH_GIVEN_ID_ERROR,
                                                        "There is no rfc relation with this id");

		scopeVerifier.verifyScope(scope, rfc);
		
		return rfc;
	}
	
	
	@RequestMapping(value="/dj/simple/rfc/relations", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRfcRelationSimple> getRfcRelationBy3(
			@RequestParam(value="releaseId",  required = false) Long releaseId,  
			@RequestParam(value="isActive", required = false) Boolean isActive, 
			@RequestParam(value="fromCiId", required = false) Long fromCiId,
			@RequestParam(value="toCiId", required = false) Long toCiId,
			@RequestParam(value="ciId", required = false) Long ciId,
            @RequestParam(value="nsPath", required = false) String nsPath,
			@RequestParam(value="attrProps", required = false) String attrProps,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		List<CmsRfcRelationSimple> relSimpleList = new ArrayList<>();
		List<CmsRfcRelation> relList = null;

        if (isActive == null) {
            isActive = true;
        }
		if (releaseId != null) {
			relList = djManager.getRfcRelationBy3(releaseId, isActive, fromCiId, toCiId);
		} else if (ciId != null){
			relList = djManager.getClosedRfcRelationByCiId(ciId);
		} else if (nsPath!=null){
            relList = djManager.getRfcRelationByNs(nsPath, isActive);
        }
		
		if (relList != null) {
			for (CmsRfcRelation rel : relList) {
				scopeVerifier.verifyScope(scope, rel);
				relSimpleList.add(cmsUtil.custRfcRel2RfcRelSimple(rel, attrProps == null ? null : attrProps.split(",")));
			}
		}
		
		return relSimpleList;
	}
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/rfc/relations")
	@ResponseBody
	public CmsRfcRelationSimple createRfcRelation(
			@RequestBody CmsRfcRelationSimple relSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {

		scopeVerifier.verifyScope(scope, relSimple);

		CmsRfcRelation rel = cmsUtil.custRfcRelSimple2RfcRel(relSimple);
		rel.setCreatedBy(userId);
		return cmsUtil.custRfcRel2RfcRelSimple(djManager.createRfcRelation(rel));
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/rfc/relations/{rfcId}")
	@ResponseBody
	public CmsRfcRelationSimple updateRfcRelation(
			@PathVariable long rfcId, 
			@RequestBody CmsRfcRelationSimple relSimple,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) throws DJException {
		
		scopeVerifier.verifyScope(scope, relSimple);

		relSimple.setRfcId(rfcId);
		CmsRfcRelation rel = cmsUtil.custRfcRelSimple2RfcRel(relSimple);
		rel.setUpdatedBy(userId);
		return cmsUtil.custRfcRel2RfcRelSimple(djManager.updateRfcRelation(rel));
	}
	
	@RequestMapping(value="/dj/simple/rfc/relations/{rfcId}", method = RequestMethod.DELETE)
	@ResponseBody
	public String rmRfcRelFromRelease(
			@PathVariable long rfcId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
	
		if (scope != null) {
			CmsRfcRelation rfc = djManager.getRfcRelationById(rfcId);
			if (rfc == null) throw new DJException(CmsError.DJ_NO_RELEASE_WITH_GIVEN_ID_ERROR,
                                                            "There is no release with this id");
			scopeVerifier.verifyScope(scope, rfc);
		}
		
		long deleted =  djManager.rmRfcRelationFromRelease(rfcId);
		return "{\"deleted\":" + deleted + "}";
	}

	@RequestMapping(value="/dj/simple/rfcs/count", method = RequestMethod.GET)
	@ResponseBody
	public String getRfcCountForNs(
			@RequestParam(value = "nsPath" , required = true) String nsPath,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {

			scopeVerifier.verifyScope(scope, nsPath);

		return "{\"ci\":" + djManager.getRfcCiCountByNs(nsPath) +
			   ",\"relation\":" + djManager.getRfcRelationCountByNs(nsPath) + "}";
	}

    @RequestMapping(value = "/dj/simple/rfcs", method = RequestMethod.DELETE)
    @ResponseBody
    public String rmRfcs(
            @RequestParam("nsPath") String nsPath,
            @RequestHeader(value = "X-Cms-Scope", required = false) String scope) {


        scopeVerifier.verifyScope(scope, nsPath);

        djManager.rmRfcs(nsPath);
        return "";
    }

    @RequestMapping(value="/dj/simple/timeline", method=RequestMethod.GET)
	@ResponseBody
	public List<TimelineBase> getDjTimeLine(
			@RequestParam(value = "nsPath", required = true) String nsPath,
			@RequestParam(value = "filter", required = false) String filter,
			@RequestParam(value = "type", required = false) String type,
			@RequestParam(value = "limit", required = false) Integer limit,
			@RequestParam(value = "sort", required = false) String sort,
			@RequestParam(value = "releaseOffset", required = false) Long releaseOffset,
			@RequestParam(value = "dpmtOffset", required = false) Long dpmtOffset,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {

		scopeVerifier.verifyScope(scope, nsPath);
		QueryOrder queryOrder = QueryOrder.queryOrder(sort);
		if (limit == null || limit <= 0 || limit > 100) limit = 20;
		TimelineQueryParam queryParam = new TimelineQueryParam(nsPath, filter, type, queryOrder, releaseOffset, dpmtOffset, limit);
		return djManager.getDjTimeLine(queryParam);
	}


    @RequestMapping(method = RequestMethod.PUT, value = "/dj/simple/rfc/{rfcId}/altNs")
    @ResponseBody
    public void tagRfc(@PathVariable long rfcId,
                       @RequestParam(value = "tag", required = false) String tag,
                       @RequestParam(value = "altNsPath", required = false) String altNsPath,
                       @RequestParam(value = "altNsId", required = false) Long altNsId,
                       @RequestHeader(value = "X-Cms-User", required = false) String userId,
                       @RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {

        CmsRfcCI ci = djManager.getRfcCIById(rfcId);
        scopeVerifier.verifyScope(scope, ci);
        CmsAltNs cmsAltNs = new CmsAltNs();
        cmsAltNs.setNsPath(altNsPath);
        if (altNsId != null) {
            cmsAltNs.setNsId(altNsId);
        }
        cmsAltNs.setTag(tag);
        djManager.createAltNs(cmsAltNs, ci);
    }


    @RequestMapping(method = RequestMethod.GET, value = "/dj/simple/rfc/{rfcId}/altNs")
    @ResponseBody
    public List<CmsAltNs> getCiTags(@PathVariable long rfcId,
                                    @RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {

        CmsRfcCI baseCi = djManager.getRfcCIById(rfcId);
        scopeVerifier.verifyScope(scope, baseCi);
        return  djManager.getAltNsBy(rfcId);
    }


	@RequestMapping(method = RequestMethod.DELETE, value = "/dj/simple/rfc/{rfcId}/ns/{nsId}/altNs")
	@ResponseBody
	public String getCiTags(@PathVariable long rfcId, @PathVariable long nsId,
							@RequestHeader(value = "X-Cms-Scope", required = false) String scope) throws DJException {

		CmsRfcCI baseCi = djManager.getRfcCIById(rfcId);
		scopeVerifier.verifyScope(scope, baseCi);		
		djManager.deleteAltNs(rfcId, nsId);
		return  "";
	}
}
