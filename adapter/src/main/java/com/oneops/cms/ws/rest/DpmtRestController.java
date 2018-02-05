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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsDpmtApproval;
import com.oneops.cms.dj.domain.CmsDpmtRecord;
import com.oneops.cms.dj.domain.CmsDpmtStateChangeEvent;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsDjManager;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.ws.exceptions.CmsSecurityException;
import com.oneops.cms.ws.rest.util.CmsScopeVerifier;


@Controller
public class DpmtRestController extends AbstractRestController {
	private CmsDjManager djManager;
	private CmsUtil cmsUtil = new CmsUtil();
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
		logger.error("handle DJException", e);
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CIValidationException.class)
	public void handleCIValidationExceptions(CIValidationException e, HttpServletResponse response) throws IOException {
		logger.error("handle CIValidationException", e);
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CmsSecurityException.class)
	public void handleCmsSecurityException(CmsSecurityException e, HttpServletResponse response) throws IOException {
		logger.error("handle CmsSecurityException", e);
		sendError(response,HttpServletResponse.SC_FORBIDDEN,e);
	}
	
	
	@RequestMapping(method=RequestMethod.POST, value="/dj/simple/deployments")
	@ResponseBody
	public CmsDeployment createDeployment(
			@RequestBody CmsDeployment dpmt,
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		try {
			scopeVerifier.verifyScope(scope, dpmt);
			dpmt.setCreatedBy(userId);
			return djManager.createDeployment(dpmt);
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in createDeployment", e);
			e.printStackTrace();
			throw e;
		}	
	}

	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/deployments/{dpmtId}")
	@ResponseBody
	public CmsDeployment updateDeployment(
			@RequestBody CmsDeployment dpmt, 
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId) {
		try {
			scopeVerifier.verifyScope(scope, dpmt);
			dpmt.setDeploymentId(dpmtId);
			dpmt.setUpdatedBy(userId);
			return djManager.updateDeployment(dpmt);
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in updateDeployment", e);
			e.printStackTrace();
			throw e;
		}	
	}

	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/deployments/{dpmtId}/records")
	@ResponseBody
	public CmsDpmtRecord updateDpmtRecord(
			@RequestBody CmsDpmtRecord dpmtRecord, 
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		try {
			if (scope != null) {
				CmsDeployment dpmt = djManager.getDeployment(dpmtId);
				scopeVerifier.verifyScope(scope, dpmt);
			}
			dpmtRecord.setDeploymentId(dpmtId);
			return djManager.updateDpmtRecord(dpmtRecord);
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in updateDpmtRecord", e);
			e.printStackTrace();
			throw e;
		}	
	}

	@RequestMapping(method=RequestMethod.GET, value="/dj/simple/deployments/records/{dpmtRecordId}")
	@ResponseBody
	public CmsDpmtRecord updateDpmtRecord(@PathVariable long dpmtRecordId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		try {
			CmsDpmtRecord dpmtRecord = djManager.getDpmtRecord(dpmtRecordId);
			if (scope != null) {
				CmsDeployment dpmt = djManager.getDeployment(dpmtRecord.getDeploymentId());
				scopeVerifier.verifyScope(scope, dpmt);
			}
			return dpmtRecord;
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in get dpmt record", e);
			e.printStackTrace();
			throw e;
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, value="/dj/simple/deployments/{dpmtId}/records/{dpmtRecordId}")
	@ResponseBody
	public CmsDpmtRecord updateDpmtRecord(
			@PathVariable long dpmtId,
			@PathVariable long dpmtRecordId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		try {
			if (scope != null) {
				CmsDeployment dpmt = djManager.getDeployment(dpmtId);
				scopeVerifier.verifyScope(scope, dpmt);
			}
			return djManager.getDpmtRecord(dpmtRecordId);
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in updateDpmtRecord", e);
			e.printStackTrace();
			throw e;
		}	
	}
	
	@RequestMapping(method=RequestMethod.PUT, value="/dj/simple/deployments/{dpmtId}/records/{dpmtRecordId}")
	@ResponseBody
	public CmsDpmtRecord updateDpmtRecord(
			@RequestBody CmsDpmtRecord dpmtRecord, 
			@PathVariable long dpmtId,
			@PathVariable long dpmtRecordId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope) {
		try {
			if (scope != null) {
				CmsDeployment dpmt = djManager.getDeployment(dpmtId);
				scopeVerifier.verifyScope(scope, dpmt);
			}
			dpmtRecord.setDeploymentId(dpmtId);
			dpmtRecord.setDpmtRecordId(dpmtRecordId);
			return djManager.updateDpmtRecord(dpmtRecord);
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in updateDpmtRecord", e);
			e.printStackTrace();
			throw e;
		}	
	}
	
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsDeployment getDeploymentById(
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		CmsDeployment dpmt = djManager.getDeployment(dpmtId);
		if (dpmt == null) throw new DJException(CmsError.DJ_NO_DEPLOYMENT_WITH_GIVEN_ID_ERROR,
                                "There is no deployment with this id");
		scopeVerifier.verifyScope(scope, dpmt);

		return dpmt;
	}

	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/history", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDpmtStateChangeEvent> getDeploymentStateHist(
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		return djManager.getDeploymentStateHist(dpmtId);
	}
	
	
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/cancel", method = RequestMethod.GET)
	@ResponseBody
	public CmsDeployment cancelDeployment(
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId){
		
		CmsDeployment dpmt = new CmsDeployment();
		dpmt.setDeploymentId(dpmtId);
		dpmt.setUpdatedBy(userId);
		dpmt.setDescription("forced canceled");
		dpmt.setDeploymentState("canceled");
		return djManager.updateDeployment(dpmt);
	}
	
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/workorders", method = RequestMethod.PUT)
	@ResponseBody
	public String completeWorkOrders(@RequestBody CmsWorkOrderSimple wos, @PathVariable long dpmtId){
		try {
			wos.setDeploymentId(dpmtId);
			CmsWorkOrder wo = cmsUtil.custSimple2WorkOrder(wos);
			djManager.completeWorkOrder(wo);
			return "done!";
		} catch (CmsBaseException e) {
			logger.error("CmsBaseException in completeWorkOrders", e);
			e.printStackTrace();
			throw e;
		}	
	}
	
	
	@RequestMapping(value="/dj/simple/deployments", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDeployment> findDeployment(
			@RequestParam(value="nsPath", required = false) String nsPath,  
			@RequestParam(value="releaseId", required = false) Long releaseId,
			@RequestParam(value="deploymentState", required = false) String state,
			@RequestParam(value="latest", required = false) Boolean latest,
			@RequestParam(value="recursive", required = false) Boolean recursive,
			@RequestParam(value="start", required = false) Long start,
			@RequestParam(value="end", required = false) Long end,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (latest == null) latest = false;
		
		List<CmsDeployment> dpmtList = null;
		
		if (nsPath != null) {
			if (start != null || end != null) {
				dpmtList = djManager.findDeploymentsByTimePeriod(nsPath, recursive, start == null ? null : new Date(start), end == null ? null : new Date(end));
			}
			else {
				dpmtList = djManager.findDeployment(nsPath, state, recursive, latest);
			}
		} else if (releaseId != null) {
			dpmtList =  djManager.findDeploymentByReleaseId(releaseId, state, latest);
		}
		
		if (scope != null && dpmtList != null) {
			for (CmsDeployment dpmt : dpmtList) {
				scopeVerifier.verifyScope(scope, dpmt);
			}
		}
	
		return dpmtList;
	}

	@RequestMapping(value="/dj/simple/deployments/count", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Long> countDeployment(
			@RequestParam(value="nsPath", required = true) String nsPath,  
			@RequestParam(value="deploymentState", required = false) String state,
			@RequestParam(value="recursive", required = false) Boolean recursive,
			@RequestParam(value="groupBy", required = false) String groupBy,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (groupBy != null) {
			return djManager.countDeploymentGroupByNsPath(nsPath, state);
		} else{
	 		long count =  djManager.countDeployments(nsPath, state, recursive);
	 		Map<String, Long> result = new HashMap<>();
	 		result.put("Total", count);
	 		return result;
		}
	}
	

	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/cis/list", method = RequestMethod.POST)
	@ResponseBody
	public List<CmsDpmtRecord> getDpmtRecordCis(
			@PathVariable long dpmtId,
			@RequestParam (value="ids") String idString,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (scope != null) {
			CmsDeployment dpmt = djManager.getDeployment(dpmtId);
			scopeVerifier.verifyScope(scope, dpmt);
		}
		List<Long> ids = Stream.of(idString.split(",")).map(Long::parseLong).collect(Collectors.toList());
		return djManager.getDpmtRecordCis(dpmtId, ids);
	}	
		
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/cis", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDpmtRecord> getDpmtRecordCis(
			@PathVariable long dpmtId,
			@RequestParam(value="updatedAfter", required = false) Long updatedAfter,
			@RequestParam(value="state", required = false) String state,
			@RequestParam(value="execorder", required = false) Integer execOrder,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (scope != null) {
			CmsDeployment dpmt = djManager.getDeployment(dpmtId);
			scopeVerifier.verifyScope(scope, dpmt);
		}

		if (updatedAfter != null) {
			return djManager.getDpmtRecordCis(dpmtId, new Date(updatedAfter));
		}
		else if (state == null && execOrder==null) {
			return djManager.getDpmtRecordCis(dpmtId);
		} else {
			return djManager.getDpmtRecordCis(dpmtId, state, execOrder);
		}
	}

	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/cis/count", method = RequestMethod.GET)
	@ResponseBody
	public Long getDpmtRecordCisCount(
			@PathVariable long dpmtId,
			@RequestParam(value="state", required = false) String state,  
			@RequestParam(value="execorder", required = false) Integer execOrder,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (scope != null) {
			CmsDeployment dpmt = djManager.getDeployment(dpmtId);
			scopeVerifier.verifyScope(scope, dpmt);
		}
		return djManager.getDeploymentRecordCount(dpmtId, state, execOrder);
	}
	
	
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/relations", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDpmtRecord> getDpmtRecordRelations(
			@PathVariable long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		if (scope != null) {
			CmsDeployment dpmt = djManager.getDeployment(dpmtId);
			scopeVerifier.verifyScope(scope, dpmt);
		}

		return djManager.getDpmtRecordRelations(dpmtId);
	}

	/*
	@RequestMapping(value="/dj/simple/deployments/{dpmtId}/approve", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDeployment> approveDeployment(
			@PathVariable long dpmtId,
			@RequestParam(value="expires", required = false) Integer expiresIn,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = true)  String userId){
		
		List<CmsDpmtApproval> approvals = djManager.getDeploymentApprovals(dpmtId);
		if (expiresIn == null) {
			expiresIn = 120;
		}
		for (CmsDpmtApproval approval : approvals){
			approval.setUpdatedBy(userId);
			approval.setExpiresIn(expiresIn);
		}
		
		return djManager.dpmtApprove(approvals);
	}
	*/
	
	@RequestMapping(value="/dj/simple/approvals", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsDpmtApproval> getDeploymentApprovals(
			@RequestParam(value="deploymentId", required = true) long dpmtId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId){
		
		CmsDeployment dpmt = djManager.getDeployment(dpmtId);
		if (dpmt == null) throw new DJException(CmsError.DJ_NO_DEPLOYMENT_WITH_GIVEN_ID_ERROR,
                                "There is no deployment with this id");
		scopeVerifier.verifyScope(scope, dpmt);
		
		return djManager.getDeploymentApprovals(dpmtId);
	}
	
	@RequestMapping(value="/dj/simple/approvals/{approvalId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsDpmtApproval getApproval(
			@PathVariable long approvalId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = false)  String userId){
		
		return djManager.getDeploymentApproval(approvalId);
	}

	@RequestMapping(value="/dj/simple/approvals/{approvalId}", method = RequestMethod.PUT)
	@ResponseBody
	public CmsDpmtApproval dpmtApprove(
			@RequestBody CmsDpmtApproval approval, @PathVariable long approvalId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = true)  String userId,
			@RequestHeader(value="X-Approval-Token", required = false) String approvalToken){
		
		approval.setUpdatedBy(userId);
		
	    djManager.updateApprovalList(Arrays.asList(approval), approvalToken);
	    return djManager.getDeploymentApproval(approval.getApprovalId());
	}

	@RequestMapping(value="/dj/simple/approvals", method = RequestMethod.PUT)
	@ResponseBody
	public List<CmsDpmtApproval> dpmtApprove(
			@RequestBody CmsDpmtApproval[] approvals,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope,
			@RequestHeader(value="X-Cms-User", required = true)  String userId,
			@RequestHeader(value="X-Approval-Token", required = false) String approvalToken
	){
		
		List<CmsDpmtApproval> toApprove = new ArrayList<>();
		for (CmsDpmtApproval approval : approvals) {
			approval.setUpdatedBy(userId);
			toApprove.add(approval);
		}
	    return djManager.updateApprovalList(toApprove, approvalToken);
	}

}
