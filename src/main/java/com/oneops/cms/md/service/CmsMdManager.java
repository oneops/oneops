package com.oneops.cms.md.service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;

/**
 * The Interface CmsMdManager.
 */
@Transactional
public interface CmsMdManager {
	void flushCache();
    CmsClazz createClazz(CmsClazz clazz);
    CmsClazz updateClazz(CmsClazz clazz);
    void deleteClazz(int clazzId, boolean deleteAll);
    void deleteClazz(String clazzName, boolean deleteAll);
    CmsRelation createRelation(CmsRelation relation);
    CmsRelation updateRelation(CmsRelation relation);
    void deleteRelation(int relationId, boolean deleteAll);
    void deleteRelation(String relationName, boolean deleteAll);
	List<CmsClazz> getClazzes();
	List<CmsClazz> getClazzesByPackage(String packagePrefix);
	CmsClazz getClazz(String clazzName);
	CmsClazz getClazz(int clazzId);
    CmsClazz getClazz(String clazzName, boolean eager);
    CmsClazz getClazz(int clazzId, boolean eager);
	CmsRelation getRelation(String relationName);
	CmsRelation getRelationWithTargets(String relationName);
	CmsRelation getRelationWithTargets(String relationName, int fromClassId, int toClassId);
	List<CmsRelation> getAllRelations();
	List<String> getSubClazzes(String clsName);
	void invalidateCache();
}
