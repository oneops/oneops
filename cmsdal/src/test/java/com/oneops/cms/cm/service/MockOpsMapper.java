package com.oneops.cms.cm.service;

import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.CmsOpsAction;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsActionState;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockOpsMapper implements OpsMapper {

  Map<Long, CmsOpsProcedure> procsMap = new HashMap<>();
  Map<Long, List<CmsOpsAction>> actionsMap = new HashMap<>();

  @Override
  public long getNextCmOpsProcedureId() {
    return 0;
  }

  @Override
  public void createCmsOpsProcedure(CmsOpsProcedure proc) {
    procsMap.put(proc.getProcedureId(), proc);
  }

  @Override
  public void createCmsOpsAction(CmsOpsAction action) {
    List<CmsOpsAction> list = actionsMap.computeIfAbsent(action.getProcedureId(), (l) -> new ArrayList<>());
    list.add(action);
  }

  @Override
  public void updateCmsOpsProcedureState(long procedureId, OpsProcedureState state) {

  }

  @Override
  public void updateCmsOpsActionState(long actionId, OpsActionState state) {

  }

  @Override
  public CmsOpsProcedure getCmsOpsProcedure(long procedureId) {
    return procsMap.get(procedureId);
  }

  @Override
  public CmsOpsProcedure getCmsOpsProcedureWithDefinition(long procedureId) {
    return null;
  }

  @Override
  public boolean isActiveOpsProcedureExistForCi(long ciId) {
    return false;
  }

  @Override
  public boolean isOpenedReleaseExistForCi(long ciId) {
    return false;
  }

  @Override
  public boolean isActiveDeploymentExistForNsPath(String nsPath) {
    return false;
  }

  @Override
  public List<CmsOpsAction> getCmsOpsActions(long procedureId) {
    return actionsMap.get(procedureId);
  }

  @Override
  public CmsOpsAction getCmsOpsActionById(long actionId) {
    return null;
  }

  @Override
  public List<CmsOpsAction> getCmsOpsActionsForCi(long procedureId, long ciId) {
    return null;
  }

  @Override
  public List<CmsOpsProcedure> getProcedureForCi(long ciId, List<OpsProcedureState> stateList,
      String procedureName, Integer limit) {
    return null;
  }

  @Override
  public List<CmsOpsProcedure> getProcedureForCiByAction(long ciId,
      List<OpsProcedureState> stateList, String procedureName, Integer limit) {
    return null;
  }

  @Override
  public List<CmsOpsProcedure> getProcedureForNamespace(String nsPath,
      List<OpsProcedureState> stateList, String procedureName) {
    return null;
  }

  @Override
  public List<CmsOpsProcedure> getProcedureForNamespaceLike(String ns, String nsLike,
      List<OpsProcedureState> stateList, String procedureName, Integer limit) {
    return null;
  }

  @Override
  public List<CmsActionOrder> getActionOrders(long procedureId, OpsProcedureState state,
      Integer execOrder) {
    return null;
  }

  @Override
  public long getCmsOpsProceduresCountForCiFromTime(long ciId, List<OpsProcedureState> stateList,
      String procedureName, Date timestamp) {
    return 0;
  }

  @Override
  public List<Map<String, Object>> getActionsCountByStates(long procedureId, Integer step) {
    return null;
  }

  @Override
  public void createProcedureExec(long procedureId, int step, String state) {

  }

  @Override
  public int getAndUpdateStepState(long procedureId, int step, String newState) {
    return 0;
  }

  @Override
  public void updateProcedureCurrentStep(CmsOpsProcedure procedure) {

  }
}
