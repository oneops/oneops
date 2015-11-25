package com.oneops.cms.md.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.md.domain.CmsClazzRelation;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.md.domain.CmsRelationAttribute;

/**
 * The Interface RelationMapper.
 */
public interface RelationMapper {
	
	void flushCache();	
    int getNextRelationId();
	void createRelation(CmsRelation relation);
    void updateRelation(CmsRelation relation);
    void deleteRelation( @Param("relationId") int relationId, @Param("deleteAll")  boolean deleteAll);
    int addRelationAttribute(CmsRelationAttribute attr);
    void updateRelationAttribute(CmsRelationAttribute attr);
    void deleteRelationAttribute( @Param("attrId") int attrId, @Param("deleteAll")  boolean deleteAll);
    int addRelationTarget(CmsClazzRelation target);
    void deleteRelationTarget(int targetId);
    CmsRelation getRelation(String relationName);
    CmsRelation getRelationById(long relationId);
	List<CmsRelation> getAllRelations();
	List<CmsRelationAttribute> getRelationAttrs(int relationId);
	List<CmsClazzRelation> getTargets(int relationId);
	List<CmsClazzRelation> getTargetsStrict(@Param("relationId") int relationId, @Param("fromClassId") int fromClassId, @Param("toClassId") int toClassId);
    long getCountCiOfRelation(int relationId);
}
