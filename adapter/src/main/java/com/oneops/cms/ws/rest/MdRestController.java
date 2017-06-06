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

import com.oneops.cms.exceptions.CIValidationException;
import com.oneops.cms.exceptions.MDException;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.cms.util.CmsError;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


@Controller
public class MdRestController extends AbstractRestController {
	
	private CmsMdManager mdManager;
	
	public void setMdManager(CmsMdManager mdManager) {
		this.mdManager = mdManager;
	}

	@ExceptionHandler(MDException.class)
	public void handleDJExceptions(MDException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@ExceptionHandler(CIValidationException.class)
	public void handleCIValidationExceptions(CIValidationException e, HttpServletResponse response) throws IOException {
		sendError(response,HttpServletResponse.SC_BAD_REQUEST,e);
	}
	
	@RequestMapping(value="/md/classes/id/{clazzId}", method = RequestMethod.GET)
	@ResponseBody
	public CmsClazz getClassById(
			@PathVariable int clazzId,
			@RequestParam(value="eager", required = false) Boolean eager) {
		
		CmsClazz clazz;
		
		if (eager == null) {
			clazz = mdManager.getClazz(clazzId);
		} else {
			clazz = mdManager.getClazz(clazzId, eager);
		}
		if (clazz == null) {
			throw new MDException(CmsError.MD_NO_CLASS_WITH_GIVEN_ID_ERROR,
                                "there is no class with id " + clazzId);
		} 
		//reset flags so they should not be used back on update
		clazz.setFlagsToNull();
		return clazz;
	}

	@RequestMapping(value="/md/classes", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsClazz> getClasses(
			@RequestParam(value="package", required = false) String packagePrefix){
		List<CmsClazz> result;
		if (packagePrefix == null) {
			result = mdManager.getClazzes();
		} else {
			result = mdManager.getClazzesByPackage(packagePrefix);
		}
		for (CmsClazz clazz : result) {
			//reset flags so they should not be used back on update
			clazz.setFlagsToNull();
		}
		return result;
	}
	
	@RequestMapping(value="/md/classes/{clazzName}", method = RequestMethod.GET)
	@ResponseBody
	public CmsClazz getClassByName(@PathVariable String clazzName,
			                       @RequestParam(value="includeActions", required = false) Boolean includeActions){
		
		CmsClazz clazz;
		
		if (includeActions != null && includeActions) { 
			clazz = mdManager.getClazz(clazzName, includeActions);
		} else {	
			clazz = mdManager.getClazz(clazzName);
		}

		if (clazz == null) {
			throw new MDException(CmsError.MD_NO_CLASS_WITH_GIVEN_NAME_ERROR,
                                "there is no class with name " + clazzName);
		} 
		//reset flags so they should not be used back on update
		clazz.setFlagsToNull();
		return clazz;
	}

		
    @RequestMapping(method=RequestMethod.POST, value="/md/classes")
    @ResponseBody
    public CmsClazz createClazz(@RequestBody CmsClazz clazz) {

        CmsClazz createdClazz = mdManager.createClazz(clazz);
        logger.debug(createdClazz.getClassId());
        return createdClazz;
    }

    @RequestMapping(method=RequestMethod.PUT, value="/md/classes/{clazzName}")
    @ResponseBody
    public CmsClazz updateClass(@PathVariable String clazzName, @RequestBody CmsClazz clazz) {
        logger.debug(clazz.getClassName());
		return mdManager.updateClazz(clazz);

    }

    @RequestMapping(method=RequestMethod.DELETE, value="/md/classes/{clazzName}")
    @ResponseBody
    public String deleteClazz(@PathVariable String clazzName) {
        mdManager.deleteClazz(clazzName, false);
        return "{\"deleted\"}";
    }

    @RequestMapping(method=RequestMethod.DELETE, value="/md/classes/{clazzName}/{deleteAll}")
    @ResponseBody
    public String deleteClazz(@PathVariable String clazzName, @PathVariable boolean deleteAll) {
        mdManager.deleteClazz(clazzName, deleteAll);
        return "{\"deleted\"}";
    }

    @RequestMapping(method=RequestMethod.POST, value="/md/relations")
    @ResponseBody
    public CmsRelation createRelation(@RequestBody CmsRelation relation) {
        CmsRelation createdRelation = mdManager.createRelation(relation);
        logger.debug(createdRelation.getRelationId());
        return createdRelation;
    }

    @RequestMapping(method=RequestMethod.PUT, value="/md/relations/{relationName}")
    @ResponseBody
    public CmsRelation updateRelation( @PathVariable String relationName, @RequestBody CmsRelation relation) {
        logger.debug(relation.getRelationName());
		return mdManager.updateRelation(relation);

    }
    
    @RequestMapping(method=RequestMethod.DELETE, value="/md/relations/{relationName}")
    @ResponseBody
    public String deleteRelation(@PathVariable String relationName) {
        mdManager.deleteRelation(relationName, false);
        return "{\"deleted\"}";

    }

    @RequestMapping(method=RequestMethod.DELETE, value="/md/relations/{relationName}/{deleteAll}")
    @ResponseBody
    public String deleteRelationEx(@PathVariable String relationName, @PathVariable boolean deleteAll) {
        mdManager.deleteRelation(relationName, deleteAll);
        return "{\"deleted\"}";
    }

	@RequestMapping(value="/md/relations/{relationName}", method = RequestMethod.GET)
	@ResponseBody
	public CmsRelation getRelationByName(@PathVariable String relationName){
		CmsRelation relation = mdManager.getRelation(relationName);
		if (relation == null) {
			throw new MDException(CmsError.MD_NO_RELATION_WITH_GIVEN_NAME_ERROR,
                                        "there is no relation with name " + relationName);
		} 
		return relation;
	}

	@RequestMapping(value="/md/relations", method = RequestMethod.GET)
	@ResponseBody
	public List<CmsRelation> getAllRelations(){
		return mdManager.getAllRelations();
	}
	
	@RequestMapping(value="/md/cache", method = RequestMethod.GET)
	@ResponseBody
	public String getAllRelations(@RequestParam("flush") String flush){
		if (flush != null) {
			mdManager.flushCache();
			mdManager.invalidateCache();
	        return "{\"deleted\"}";
		}
		return "";
	}

	@RequestMapping(value="/strtest/{str}", method = RequestMethod.GET)
	@ResponseBody
	public String get(@PathVariable String str){
		return str;
	}

}
