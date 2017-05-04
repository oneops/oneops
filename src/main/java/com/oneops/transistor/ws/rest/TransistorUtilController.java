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

import java.util.HashMap;
import java.util.Map;

import com.oneops.mybatis.Stats;
import com.oneops.mybatis.StatsPlugin;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.simple.domain.CmsRfcCISimple;
import com.oneops.cms.util.CmsUtil;
import com.oneops.transistor.service.DesignManager;

@Controller
public class TransistorUtilController extends AbstractRestController {
	private DesignManager dManager;
	private CmsUtil util = new CmsUtil();
	private CmsMdManager mdManager;

	public void setMdManager(CmsMdManager mdManager) {
		this.mdManager = mdManager;
	}

	public void setdManager(DesignManager dManager) {
		this.dManager = dManager;
	}

	@RequestMapping(value="/test/assemblies/{assemblyId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Long> generateDesign(
			@PathVariable long assemblyId) {

		String userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		CmsRfcCISimple platRfcSimple = getTestPlat();
		
		CmsRfcCI platRfc = util.custRfcCISimple2RfcCI(platRfcSimple);
		//1170115
		long releaseId = dManager.generatePlatform(platRfc, assemblyId, userId, null);  
		
		Map<String,Long> result = new HashMap<String,Long>(); 
		result.put("releaseId", releaseId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Design - " + tookTime);

		return result;
	}

	@RequestMapping(value="/test/assemblies/{assemblyId}/clone/{fromPlatformId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Long> clonePlatform(
			@PathVariable long assemblyId,
			@PathVariable long fromPlatformId) {

		String userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		CmsRfcCISimple platRfcSimple = getTestPlat();
		
		CmsRfcCI platRfc = util.custRfcCISimple2RfcCI(platRfcSimple);
		//1170115
		long releaseId = dManager.clonePlatform(platRfc, assemblyId, fromPlatformId, userId, null);  
		
		Map<String,Long> result = new HashMap<String,Long>(); 
		result.put("releaseId", releaseId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Design - " + tookTime);

		return result;
	}
	
	@RequestMapping(value="/test/org/{orgId}/clone/{fromAssemblyId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Long> cloneAssembly(
			@PathVariable long orgId,
			@PathVariable long fromAssemblyId) {

		String userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		CmsCISimple assemblySimple = getDummyAssembly();
		
		CmsCI assemblyCi = util.custCISimple2CI(assemblySimple, null);
	
		long assemblyId = dManager.cloneAssembly(assemblyCi, fromAssemblyId, userId, "/oneops");  
		
		Map<String,Long> result = new HashMap<String,Long>(); 
		result.put("assemblyId", assemblyId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Design - " + tookTime);

		return result;
	}

	@RequestMapping(value="/test/org/{orgId}/saveascat/{fromAssemblyId}", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,Long> saveAssemblyAsCatalog(
			@PathVariable long orgId,
			@PathVariable long fromAssemblyId) {

		String userId = "oneops-system";
		
		long startTime = System.currentTimeMillis(); 
		
		CmsCISimple catalogSimple = getDummyCatalog();
		
		CmsCI catalogCi = util.custCISimple2CI(catalogSimple, null);
		long catalogId = dManager.saveAssemblyAsCatalog(catalogCi, fromAssemblyId, userId, "/oneops");
		
		Map<String,Long> result = new HashMap<String,Long>(); 
		result.put("catalogId", catalogId);

		long tookTime = System.currentTimeMillis() - startTime;
		logger.debug("Time to generate Design - " + tookTime);

		return result;
	}
	
	@RequestMapping(value="/cache/md/clear", method = RequestMethod.GET)
	@ResponseBody
	public Map<String,String> invalidateMdCache() {

		Map<String,String> result = new HashMap<String,String>();
		mdManager.invalidateCache();
		result.put("result", "md cache cleared.");

		return result;
	}


	@RequestMapping(value="/mybatis/stats", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Stats> getStats() {
		return StatsPlugin.getStatsMap();
	}

	@RequestMapping(value="/mybatis/stats/clear", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Stats> clearStats() {
		StatsPlugin.getStatsMap().clear();
		return getStats();
	}


	private CmsRfcCISimple getTestPlat() {
		
		CmsRfcCISimple platRfcSimple = new CmsRfcCISimple();
		
		platRfcSimple.setCiName("test-rails-1");
		platRfcSimple.setNsPath("/oneops/rb1");
		platRfcSimple.setCiClassName("catalog.Platform");
		
		platRfcSimple.addCiAttribute("version", "0.4");
		platRfcSimple.addCiAttribute("pack", "rails");
		platRfcSimple.addCiAttribute("major_version", "1");
		platRfcSimple.addCiAttribute("source", "packer");
		platRfcSimple.addCiAttribute("description", "blah");
		
		return platRfcSimple;
	}
	
	private CmsCISimple getDummyAssembly() {
		
		CmsCISimple assemblySimple = new CmsCISimple();
		
		assemblySimple.setCiName("test-assembly-1");
		assemblySimple.setCiClassName("account.Assembly");
		
		assemblySimple.addCiAttribute("description", "blah");
		
		return assemblySimple;
	}

	private CmsCISimple getDummyCatalog() {
		
		CmsCISimple catalogSimple = new CmsCISimple();
		
		catalogSimple.setCiName("test-catalog");
		catalogSimple.setCiClassName("account.Design");
		
		catalogSimple.addCiAttribute("description", "blah");
		
		return catalogSimple;
	}
	
}
