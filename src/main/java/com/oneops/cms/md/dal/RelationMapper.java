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

    void deleteRelation(@Param("relationId") int relationId, @Param("deleteAll") boolean deleteAll);

    int addRelationAttribute(CmsRelationAttribute attr);

    void updateRelationAttribute(CmsRelationAttribute attr);

    void deleteRelationAttribute(@Param("attrId") int attrId, @Param("deleteAll") boolean deleteAll);

    int addRelationTarget(CmsClazzRelation target);

    void deleteRelationTarget(int targetId);

    CmsRelation getRelation(String relationName);

    CmsRelation getRelationById(long relationId);

    List<CmsRelation> getAllRelations();

    List<CmsRelationAttribute> getRelationAttrs(int relationId);

    List<CmsClazzRelation> getTargets(int relationId);

    List<CmsClazzRelation> getAllTargets();

    List<CmsClazzRelation> getTargetsStrict(@Param("relationId") int relationId, @Param("fromClassId") int fromClassId, @Param("toClassId") int toClassId);

    long getCountCiOfRelation(int relationId);
}
