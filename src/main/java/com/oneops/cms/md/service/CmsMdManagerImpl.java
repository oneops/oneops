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
package com.oneops.cms.md.service;

import java.util.List;

import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;

/**
 * The Class CmsMdManagerImpl.
 */
public class CmsMdManagerImpl implements CmsMdManager {

	private CmsMdProcessor mdProcessor;

	/**
	 * Sets the md processor.
	 *
	 * @param mdProcessor the new md processor
	 */
	public void setMdProcessor(CmsMdProcessor mdProcessor) {
		this.mdProcessor = mdProcessor;
	}

    /**
     * Creates the clazz.
     *
     * @param clazz the clazz
     * @return the cms clazz
     */
    @Override
    public CmsClazz createClazz(CmsClazz clazz) {
        return mdProcessor.createClazz(clazz);
    }

    /**
     * Update clazz.
     *
     * @param clazz the clazz
     * @return the cms clazz
     */
    @Override
    public CmsClazz updateClazz(CmsClazz clazz) {
        return mdProcessor.updateClazz(clazz);
    }

    /**
     * Delete clazz.
     *
     * @param clazzId the clazz id
     * @param deleteAll the delete all
     */
    @Override
    public void deleteClazz(int clazzId, boolean deleteAll) {
        mdProcessor.deleteClazz(clazzId, deleteAll);
    }

    /**
     * Delete clazz.
     *
     * @param clazzName the clazz name
     * @param deleteAll the delete all
     */
    @Override
    public void deleteClazz(String clazzName, boolean deleteAll) {
        CmsClazz clazz = getClazz(clazzName, false);
        mdProcessor.deleteClazz(clazz.getClassId(), deleteAll);
    }

    /**
     * Creates the relation.
     *
     * @param relation the relation
     * @return the cms relation
     */
    @Override
    public CmsRelation createRelation(CmsRelation relation) {
        return mdProcessor.createRelation(relation);
    }

    /**
     * Update relation.
     *
     * @param relation the relation
     * @return the cms relation
     */
    @Override
    public CmsRelation updateRelation(CmsRelation relation) {
        return mdProcessor.updateRelation(relation);
    }

    /**
     * Delete relation.
     *
     * @param relationId the relation id
     * @param deleteAll the delete all
     */
    @Override
    public void deleteRelation(int relationId, boolean deleteAll) {
        mdProcessor.deleteRelation(relationId, deleteAll);
    }

    /**
     * Delete relation.
     *
     * @param relationName the relation name
     * @param deleteAll the delete all
     */
    @Override
    public void deleteRelation(String relationName, boolean deleteAll) {
        CmsRelation relation = getRelation(relationName);
        mdProcessor.deleteRelation(relation.getRelationId(), deleteAll);
    }

    /**
     * Gets the clazzes.
     *
     * @return the clazzes
     */
    @Override
	public List<CmsClazz> getClazzes() {
		return mdProcessor.getClazzes();
	}

	/**
	 * Gets the sub clazzes.
	 *
	 * @param clsName the cls name
	 * @return the sub clazzes
	 */
	@Override
	public List<String> getSubClazzes(String clsName){	
		return mdProcessor.getSubClazzes(clsName);
	}

	/**
	 * Gets the clazz.
	 *
	 * @param clazzId the clazz id
	 * @return the clazz
	 */
	@Override
	public CmsClazz getClazz(int clazzId) {
		return mdProcessor.getClazz(clazzId);
	}

    /**
     * Gets the clazz.
     *
     * @param clazzName the clazz name
     * @param eager the eager
     * @return the clazz
     */
    @Override
    public CmsClazz getClazz(String clazzName, boolean eager) {
        return mdProcessor.getClazz(clazzName, eager);
    }

    /**
     * Gets the clazz.
     *
     * @param clazzId the clazz id
     * @param eager the eager
     * @return the clazz
     */
    @Override
    public CmsClazz getClazz(int clazzId, boolean eager) {
        return mdProcessor.getClazz(clazzId,eager);
    }


    /**
     * Gets the clazz.
     *
     * @param clazzName the clazz name
     * @return the clazz
     */
    @Override
	public CmsClazz getClazz(String clazzName) {
		return mdProcessor.getClazz(clazzName);
	}
	

	/**
	 * Gets the relation.
	 *
	 * @param relationName the relation name
	 * @return the relation
	 */
	@Override
	public CmsRelation getRelation(String relationName) {
		return mdProcessor.getRelation(relationName);
	}

	/**
	 * Gets the relation with targets.
	 *
	 * @param relationName the relation name
	 * @return the relation with targets
	 */
	@Override
	public CmsRelation getRelationWithTargets(String relationName) {
		return mdProcessor.getRelationWithTargets(relationName);
	}

	/**
	 * Gets the relation with targets.
	 *
	 * @param relationName the relation name
	 * @param fromClassId the from class id
	 * @param toClassId the to class id
	 * @return the relation with targets
	 */
	@Override
	public CmsRelation getRelationWithTargets(String relationName, int fromClassId, int toClassId) {
		return mdProcessor.getRelationWithTargets(relationName, fromClassId, toClassId);
	}
	
	
	/**
	 * Gets the all relations.
	 *
	 * @return the all relations
	 */
	@Override
	public List<CmsRelation> getAllRelations() {
		return mdProcessor.getAllRelations();
	}

	/**
	 * Flush cache.
	 */
	@Override
	public void flushCache() {
		mdProcessor.flushCache();		
	}
	
	/**
	 * Gets the clazzes by package.
	 *
	 * @param packagePrefix the package prefix
	 * @return the clazzes by package
	 */
	@Override
	public List<CmsClazz> getClazzesByPackage(String packagePrefix) {
		return mdProcessor.getClazzesByPackage(packagePrefix);
	}

	@Override
	public void invalidateCache() {
		mdProcessor.invalidateCache();
	}
	
}
