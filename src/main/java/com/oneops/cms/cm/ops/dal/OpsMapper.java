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
    
    List<CmsOpsProcedure> getProcedureForCi(@Param("ciId") long ciId, @Param("state") OpsProcedureState state,
                                            @Param("name") String procedureName, @Param("limit") Integer limit);

    List<CmsOpsProcedure> getProcedureForCiByAction(@Param("ciId") long ciId, @Param("state") OpsProcedureState state,
            @Param("name") String procedureName, @Param("limit") Integer limit);
    
    List<CmsOpsProcedure> getProcedureForNamespace(@Param("nspath") String nsPath, @Param("state") OpsProcedureState state,
                                            @Param("name") String procedureName);
    /**
     * Returns the list of <code>CmsOpsProcedure<code> for specified params passed
      @param nsPath the nspath of the CI.(/assembly/org/)
     * @param state the state in which the procedure is in. 
     * @param limit the number of CmsOpsProcedure returned
     * @param procedureName procedure name
     * @return list of CmsOpsProcedure
     */
    List<CmsOpsProcedure> getProcedureForNamespaceLike(@Param("ns") String ns, @Param("nsLike") String nsLike, @Param("state") OpsProcedureState state,
            @Param("name") String procedureName,@Param("limit") Integer limit);

    List<CmsActionOrder> getActionOrders(@Param("procedureId") long procedureId, @Param("state") OpsProcedureState state, @Param("execOrder") Integer execOrder);
	List<CmsOpsProcedure> getProceduresForCiFromTime(@Param("ciId") long ciId,
            @Param("name") String procedureName, @Param("timestamp") Date timestamp);

}
