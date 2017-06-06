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
package com.oneops.cms.cm.ops.dal;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.cm.ops.domain.*;

import java.util.Date;
import java.util.List;

/**
 * The Interface OpsMapper.
 */
public interface OpsMapper {

    long getNextCmOpsProcedureId();
    void createCmsOpsProcedure(CmsOpsProcedure proc);
    void createCmsOpsAction(CmsOpsAction action);
    void updateCmsOpsProcedureState(@Param("procedureId") long procedureId, @Param("state") OpsProcedureState state);
    void updateCmsOpsActionState(@Param("actionId") long actionId, @Param("state") OpsActionState state);

    CmsOpsProcedure getCmsOpsProcedure(long procedureId);
    CmsOpsProcedure getCmsOpsProcedureWithDefinition(long procedureId);
    boolean isActiveOpsProcedureExistForCi(long ciId);
	boolean isOpenedReleaseExistForCi(long ciId);
	boolean isActiveDeploymentExistForNsPath(String nsPath);
    List<CmsOpsAction> getCmsOpsActions(long procedureId);
    CmsOpsAction getCmsOpsActionById(long actionId);
    List<CmsOpsAction> getCmsOpsActionsForCi(@Param("procedureId") long procedureId, @Param("ciId") long ciId);
    
    List<CmsOpsProcedure> getProcedureForCi(@Param("ciId") long ciId, @Param("stateList") List<OpsProcedureState> stateList,
                                            @Param("name") String procedureName, @Param("limit") Integer limit);

    List<CmsOpsProcedure> getProcedureForCiByAction(@Param("ciId") long ciId, @Param("stateList") List<OpsProcedureState> stateList,
            @Param("name") String procedureName, @Param("limit") Integer limit);
    
    List<CmsOpsProcedure> getProcedureForNamespace(@Param("nspath") String nsPath, @Param("stateList") List<OpsProcedureState> stateList,
                                            @Param("name") String procedureName);
    /**
     * Returns the list of <code>CmsOpsProcedure<code> for specified params passed
      @param ns the nspath of the CI.(/assembly/org/)
     * @param stateList the list of states in which the procedure is in. 
     * @param limit the number of CmsOpsProcedure returned
     * @param procedureName procedure name
     * @return list of CmsOpsProcedure
     */
    List<CmsOpsProcedure> getProcedureForNamespaceLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("stateList") List<OpsProcedureState> stateList,
            @Param("name") String procedureName,@Param("limit") Integer limit);

    List<CmsActionOrder> getActionOrders(@Param("procedureId") long procedureId, @Param("state") OpsProcedureState state, @Param("execOrder") Integer execOrder);
	long getCmsOpsProceduresCountForCiFromTime(@Param("ciId") long ciId,
			@Param("stateList") List<OpsProcedureState> stateList, @Param("name") String procedureName, @Param("timestamp") Date timestamp);

}
