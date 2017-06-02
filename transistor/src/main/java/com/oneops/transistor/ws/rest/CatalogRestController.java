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
package com.oneops.transistor.ws.rest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsCIRelationSimple;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.domain.CatalogExport;
import com.oneops.transistor.domain.CatalogExportSimple;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.service.DesignManager;

@Controller
public class CatalogRestController extends AbstractRestController {
	static Logger logger = Logger.getLogger(CatalogRestController.class);
	
	private DesignManager dManager;
	private CmsUtil util = new CmsUtil();

	public void setdManager(DesignManager dManager) {
		this.dManager = dManager;
	}
	
	@ExceptionHandler(TransistorException.class)
	@ResponseBody
	public void handleExceptions(TransistorException e, HttpServletResponse response) throws IOException {
		logger.error(e);
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(DJException.class)
	public void handleDJExceptions(DJException e, HttpServletResponse response) throws IOException {
		logger.error(e);
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CIValidationException.class)
	public void handleCIValidationExceptions(CIValidationException e, HttpServletResponse response) throws IOException {
		logger.error(e);
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@RequestMapping(value="/catalogs/{catalogId}/export", method = RequestMethod.GET)
	@ResponseBody
	public CatalogExportSimple exportCatalog(
			@PathVariable long catalogId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){
		
		CatalogExport catEx = dManager.exportCatalog(catalogId, scope);
		
		CatalogExportSimple catExSimple = new CatalogExportSimple();
		catExSimple.setCatalogName(catEx.getCatalogName());
		catExSimple.setDescription(catEx.getDescription());
		catExSimple.setCatalogId(catEx.getCatalogId());
		List<CmsCISimple> ciSimples = new ArrayList<CmsCISimple>();
		for (CmsCI ci : catEx.getCis()) {
			ciSimples.add(util.custCI2CISimple(ci, "df", true));
		}
		catExSimple.setCis(ciSimples);

		List<CmsCIRelationSimple> ciRelSimples = new ArrayList<CmsCIRelationSimple>();
		for (CmsCIRelation rel : catEx.getRelations()) {
			ciRelSimples.add(util.custCIRelation2CIRelationSimple(rel, "df", true));
		}
		
		catExSimple.setRelations(ciRelSimples);
		
		return catExSimple;
	}
	
	@RequestMapping(value="/catalogs/import", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Long> importCatalog(
			@RequestBody CatalogExportSimple catExpSimple,
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (userId == null) userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		CatalogExport catExp = new CatalogExport();
		
		catExp.setCatalogId(catExpSimple.getCatalogId());
		catExp.setCatalogName(catExpSimple.getCatalogName());
		catExp.setDescription(catExpSimple.getDescription());
		
		List<CmsCI> cis = new ArrayList<CmsCI>();
		for (CmsCISimple ciSimple : catExpSimple.getCis()) {
			cis.add(util.custCISimple2CI(ciSimple, null));
		}
		catExp.setCis(cis);

		List<CmsCIRelation> ciRels = new ArrayList<CmsCIRelation>();
		for (CmsCIRelationSimple rel : catExpSimple.getRelations()) {
			ciRels.add(util.custCIRelationSimple2CIRelation(rel, null));
		}
		
		catExp.setRelations(ciRels);
		
		long resultCiId = dManager.importCatalog(catExp, userId, scope);
		
		Map<String,Long> result = new HashMap<String,Long>(1); 
		result.put("catalogCiId", resultCiId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Assembly/Catalog - " + tookTime);

		return result;
	}
	
	@RequestMapping(value="/catalogs/{catalogId}/assemblies", method = RequestMethod.POST)
	@ResponseBody
	public Map<String,Long> createAssemblyFromCatalog(
			@PathVariable long catalogId,
			@RequestBody CmsCISimple targetCISimple,
			@RequestHeader(value="X-Cms-User", required = false)  String userId,
			@RequestHeader(value="X-Cms-Scope", required = false)  String scope){

		if (userId == null) userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		if (targetCISimple.getCiAttributes().get("description") == null) {
			targetCISimple.addCiAttribute("description", null);
		}
		
		CmsCI targetCI = util.custCISimple2CI(targetCISimple, null);
		
		long resultCiId = dManager.cloneAssembly(targetCI, catalogId, userId, scope);
		Map<String,Long> result = new HashMap<String,Long>(1); 
		result.put("resultCiId", resultCiId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Assembly/Catalog - " + tookTime);

		return result;
	}	
	
}
