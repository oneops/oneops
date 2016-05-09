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
package com.oneops.cms.ns.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.ns.domain.CmsNamespace;


/**
 * The Interface NSMapper.
 */
public interface NSMapper {

	void createNamespace(CmsNamespace ns);
	CmsNamespace getNamespace(String nsPath);
	List<CmsNamespace> getNamespaceLike(String nsPath);
	CmsNamespace getNamespaceById(long nsId);
	void deleteNamespace(String nsPath);
	void lockNamespace(String nsPath);
	void vacuumNamespace(@Param("nsId") long nsId, @Param("userId") String userId);

}
