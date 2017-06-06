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
package com.oneops.cms.util.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.util.domain.CmsStuckDpmt;
import com.oneops.cms.util.domain.CmsVar;

public interface UtilMapper {
	boolean acquireLock(@Param("lockName") String lockName, @Param("processId") String processId, @Param("staleTimeout") int staleTimeout);
	void releaseLock(@Param("lockName") String lockName, @Param("processId") String processId);
	void updateCmSimpleVar(@Param("name") String varName, @Param("value") String varValue, 
			@Param("criteria") String criteria, @Param("updatedBy") String updatedBy);
	CmsVar getCmSimpleVar(@Param("name") String varName);
	List<CmsVar> getCmVarByLongestMatchingCriteria(@Param("name") String varNameLike, @Param("criteria") String criteria);
	List<CmsStuckDpmt> getCmsStuckDpmts();
	List<CmsStuckDpmt> getInProgressStuckDpmts();
	List<CmsStuckDpmt> getPausedStuckDpmts();
}
