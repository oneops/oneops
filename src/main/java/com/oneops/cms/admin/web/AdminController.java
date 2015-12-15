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
package com.oneops.cms.admin.web;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.oneops.cms.admin.service.CmsManager;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.util.domain.CmsStuckDpmtCollection;

@Controller
public class AdminController {

	static Logger logger = Logger.getLogger(AdminController.class);

	@Autowired
    private CmsManager cmsManager;
	
    @RequestMapping(value="/md.do", method = RequestMethod.GET)
	protected ModelAndView handleRequestInternal(HttpServletRequest reqParams) throws Exception {

		if ("clazz".equalsIgnoreCase(reqParams.getParameter("type"))) {
			if (reqParams.getParameter("name")==null){
				return getClassList();
			} else {
				return getClassDetail(reqParams.getParameter("name"));
			}
		} else if ("relation".equalsIgnoreCase(reqParams.getParameter("type"))) {
			return getRelationDetail(reqParams.getParameter("name"));
		}
		else {
			return new ModelAndView("welcomePage");
		}
		
	}

    @RequestMapping(value="/flushCache.do", method = RequestMethod.GET)
	protected ModelAndView handleFlushCache(HttpServletRequest reqParams) throws Exception {
    	cmsManager.flushCache();
		return new ModelAndView("welcomePage");
	}
    
	@RequestMapping(value="/ci.do", method = RequestMethod.GET)
	protected String handleCi(@RequestParam(value="id", required = false) Integer id,
							  @RequestParam(value="nspath", required = false) String nsPath,
							  @RequestParam(value="classname", required = false) String className,
							  @RequestParam(value="ciname", required = false) String ciName,
	                          Model model) throws Exception {
		if(className!=null && className.isEmpty()) {
			className = null;
		}
		if(ciName!=null && ciName.isEmpty()) {
			ciName = null;
		}
		model.addAttribute("nspath",nsPath);
		model.addAttribute("classname",className);
		model.addAttribute("ciname",ciName);
		logger.debug("Search: nspath="+nsPath+" class="+className+" ci="+ciName);
		if(id == null) {
			model.addAttribute("cilist",cmsManager.getCiList(nsPath, className, ciName));
			return "ciList";
		} else {
			model.addAttribute("ci",cmsManager.getCI(id));
			model.addAttribute("attributes",cmsManager.getCIAttributes(id));
			model.addAttribute("fromRelations",cmsManager.getFromRelation(id));
			model.addAttribute("toRelations",cmsManager.getToRelation(id));
			return "ci";
		}
	}

	@RequestMapping(value="/relation.do", method = RequestMethod.GET)
	protected String handleCi(@RequestParam(value="id", required = true) Integer id,
	                          Model model) throws Exception {
		model.addAttribute("relation",cmsManager.getCIRelation(id));
		model.addAttribute("attributes",cmsManager.getCIRelationAttributes(id));
		return "ciRelation";
	}
	
	@RequestMapping(value="/stuckdpmts.do", method = RequestMethod.GET)
	protected ModelAndView getStuckDpmts(HttpServletRequest reqParams) throws Exception {
		CmsStuckDpmtCollection stuckDpmtColl=cmsManager.getStuckDpmts();
		return new ModelAndView("stuckDpmtList","stuckDpmtColl", stuckDpmtColl);
	}
	
	private ModelAndView getClassList() {
		List<CmsClazz> clazzes=cmsManager.getClazzes();
		return new ModelAndView("clazzList","clazzes", clazzes);
	}
	
	private ModelAndView getClassDetail(String clazzName) {
		CmsClazz clazz=cmsManager.getClazz(clazzName);
		return new ModelAndView("clazz","clazz", clazz);
	}

	private ModelAndView getRelationDetail(String relationName) {
		CmsRelation relation=cmsManager.getRelation(relationName);
		return new ModelAndView("relation","relation", relation);
	}
	
	
/*
	public void setCmsManager(CmsManager cmsManager) {
		this.cmsManager = cmsManager;
	}
*/
}
