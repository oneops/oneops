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

import com.oneops.cms.md.domain.*;

/**
 * The Interface ClazzMapper.
 */
public interface ClazzMapper {
	
	void flushCache();
    int getNextClassId();
	int createClazz(CmsClazz clazz);
    int updateClazz(CmsClazz clazz);
    void deleteClazz(@Param("clazzId")int clazzId, @Param("deleteAll") boolean deleteAll);

	int addClazzAttribute(CmsClazzAttribute attr);
    void updateClazzAttribute(CmsClazzAttribute attr);
    void deleteClazzAttribute(@Param("attrId")int attrId,@Param("deleteAll") boolean deleteAll);

    int addClazzAction(CmsClazzAction action);
    void updateClazzAction(CmsClazzAction action);
    void deleteClazzAction(int actId);

	List<CmsClazz> getClazzes();
	List<CmsClazz> getClazzesByPackage(String packagePrefix);
	CmsClazz getClazz(String clazzName);
	CmsClazz getClazzById(int clazzId);
	List<CmsClazzAttribute> getClazzAttrs(int clazzId);
	List<CmsClazzAttribute> getInheritableClazzAttrs(int clazzId);
	List<CmsClazzRelation> getFromClazzRelations(int classId);
	List<CmsClazzRelation> getToClazzRelations(int classId);
	List<String> getSubClazzes(String clsName);
    long getCountCiOfClazz(int clazzId);
    List<CmsClazzAction> getClazzActions(int clazzId);
    List<CmsClazzAction> getInheritableClazzActions(int clazzId);
}
