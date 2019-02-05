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
package com.oneops.cms.dj.service;

import com.google.gson.Gson;
import com.oneops.capacity.CapacityProcessor;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationBasic;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.crypto.CmsCrypto;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.ns.dal.NSMapper;
import com.oneops.cms.util.*;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DuplicateKeyException;

import java.lang.reflect.InvocationTargetException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The Class CmsDpmtProcessor.
 */
public class CmsDpmtProcessor {

  public static final String OPEN_DEPLOYMENT_REGEXP = "active|failed|paused|pending";
  static Logger logger = Logger.getLogger(CmsDpmtProcessor.class);

  private DJMapper djMapper;
  private DJDpmtMapper dpmtMapper;
  private NSMapper nsMapper;
  private CmsCmProcessor cmProcessor;
  private CmsRfcProcessor rfcProcessor;
  private CmsCrypto cmsCrypto;
  private Gson gson = new Gson();
  private CapacityProcessor capacityProcessor;

  public static final String DPMT_STATE_PENDING = "pending";
  public static final String DPMT_STATE_ACTIVE = "active";
  public static final String DPMT_STATE_CANCELED = "canceled";
  public static final String DPMT_STATE_FAILED = "failed";
  public static final String DPMT_STATE_PAUSED = "paused";
  public static final String DPMT_STATE_COMPLETE = "complete";

  public static final String APPROVAL_STATE_PENDING = "pending";
  public static final String APPROVAL_STATE_APPROVED = "approved";
  public static final String APPROVAL_STATE_REJECTED = "rejected";
  //private static final String APPROVAL_STATE_EXPIRED = "expired";


  private static final String DPMT_RECORD_STATE_INPROGRESS = "inprogress";
  private static final String DPMT_RECORD_STATE_PENDING = "pending";
  private static final String RELEASE_STATE_OPEN = "open";
  private static final String MANIFEST_PLATFORM_CLASS = "manifest.Platform";
  private static final String ONEOPS_AUTOREPLACE_USER = "oneops-autoreplace";
  private static final int BOM_RELASE_NSPATH_LENGTH = 5;
  private static final String DEPLOYMENT_APPROVAL_BYPASS_FLAG = "DEPLOYMENT_APPROVAL_BYPASS_FLAG";
  private static final String ZONES_SELECTED = "ZONES_SELECTED";

  /**
   * Sets the cm processor.
   *
   * @param cmProcessor the new cm processor
   */
  public void setCmProcessor(CmsCmProcessor cmProcessor) {
    this.cmProcessor = cmProcessor;
  }

    public void setCmsCrypto(CmsCrypto cmsCrypto) {
        this.cmsCrypto = cmsCrypto;
    }
  /**
   * Sets the dpmt mapper.
   *
   * @param dpmtMapper the new dpmt mapper
   */
  public void setDpmtMapper(DJDpmtMapper dpmtMapper) {
    this.dpmtMapper = dpmtMapper;
  }

  public void setNsMapper(NSMapper nsMapper) {
    this.nsMapper = nsMapper;
  }

  public void setDjMapper(DJMapper djMapper) {
    this.djMapper = djMapper;
  }

  public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
    this.rfcProcessor = rfcProcessor;
  }

  public void setCapacityProcessor(CapacityProcessor capacityProcessor) {
    this.capacityProcessor = capacityProcessor;
  }

  /**
   * Deploy release.
   *
   * @param dpmt the dpmt
   * @return the cms deployment
   */
  public CmsDeployment deployRelease(CmsDeployment dpmt) {

    validateReleaseForDpmt(dpmt.getReleaseId());

    List<CmsDeployment> existingDpmts = dpmtMapper
        .findLatestDeploymentByReleaseId(dpmt.getReleaseId(), null);

    for (CmsDeployment existingDpmt : existingDpmts) {
      if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_FAILED
          .equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PENDING
          .equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PAUSED
          .equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        String errMsg = "There is an " + existingDpmt.getDeploymentState()
            + " deployment already, returning this. dpmtId = " + existingDpmt.getDeploymentId();
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_STATE_ALREADY_DEPLOYMENT_ERROR, errMsg);
      }
    }

    dpmt.setDeploymentId(cmProcessor.getNextDjId());
    capacityProcessor.reserveCapacityForDeployment(dpmt);

    updateAutoDeployExecOrders(dpmt);
    if (ONEOPS_AUTOREPLACE_USER.equals(dpmt.getCreatedBy())) {
      dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
      createDeployment(dpmt);

    } else {
      if (needApprovalForNewDpmt(dpmt) && !getBooleanVariable(DEPLOYMENT_APPROVAL_BYPASS_FLAG)) {
        dpmt.setDeploymentState(DPMT_STATE_PENDING);

        createDeployment(dpmt);
        existingDpmts = dpmtMapper.findLatestDeploymentByReleaseId(dpmt.getReleaseId(), null);
        if (existingDpmts.size() == 1) {
          dpmt = existingDpmts.get(0);
        }
        needApproval(dpmt); // to cerate approval for this deployment
      } else {
        dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
        createDeployment(dpmt);
      }
    }

    // retrieve deployment from the db to fetch deploymentID
    existingDpmts = dpmtMapper.findLatestDeploymentByReleaseId(dpmt.getReleaseId(), null);
    if (existingDpmts.size() == 1) {
      dpmt = existingDpmts.get(0);
    }

    logger.info("Created new deployment, dpmtId = " + dpmt.getDeploymentId());
    return dpmt;
  }

  private void createDeployment(CmsDeployment dpmt) {
    try {
      dpmtMapper.createDeployment(dpmt);
    } catch (DuplicateKeyException e) {
      logger.error("DuplicateKeyException while creating deployment", e);
      String errMsg = "There is a deployment already in one of pending/active/paused/failed state, returning this";
      throw new DJException(CmsError.DJ_STATE_ALREADY_DEPLOYMENT_ERROR, errMsg);
    }
  }

  private void validateReleaseForDpmt(long releaseId) {
    CmsRelease release = rfcProcessor.getReleaseById(releaseId);
    if (!release.getReleaseState().equals(RELEASE_STATE_OPEN)) {
      String errMsg =
          "The release with id " + releaseId + " is not open, Can't create deployment for it!";
      logger.error(errMsg);
      throw new DJException(CmsError.DJ_RFC_RELEASE_NOT_OPEN_ERROR, errMsg);
    }
    String[] nsParts = release.getNsPath().split("/");
    if (nsParts.length < BOM_RELASE_NSPATH_LENGTH || !"bom".equals(nsParts[nsParts.length - 1])) {
      String errMsg = "The release with id " + releaseId
          + " is not a BOM release, Can't create deployment for it!";
      logger.error(errMsg);
      throw new DJException(CmsError.DJ_OPEN_RELEASE_FOR_NAMESPACE_ERROR, errMsg);
    }
  }

  private void updateAutoDeployExecOrders(CmsDeployment dpmt) {
    Set<Integer> autoPauseExecOrders = dpmt.getAutoPauseExecOrders();
    if (autoPauseExecOrders != null) {
      dpmt.setAutoPauseExecOrdersVal(StringUtils.join(autoPauseExecOrders, ","), false);
    }
  }

    public List<CmsDpmtApproval> updateApprovalList(List<CmsDpmtApproval> approvals, String approvalToken) {

    List<CmsDpmtApproval> result = new ArrayList<>();
    ListUtils<Long> lu = new ListUtils<>();
    try {
      Map<Long, List<CmsDpmtApproval>> approvalMap = lu.toMapOfList(approvals, "deploymentId");
      for (Map.Entry<Long, List<CmsDpmtApproval>> dpmtApprovals : approvalMap.entrySet()) {
        long dpmtId = dpmtApprovals.getKey();
        String userId = null;
        String rejectComments = null;
        boolean rejected = false;
        for (CmsDpmtApproval approval : dpmtApprovals.getValue()) {
                    checkIfAthorized(approvalToken, approval.getApprovalId());
          userId = approval.getUpdatedBy();
          if (approval.getExpiresIn() == 0) {
            approval.setExpiresIn(1);
          }
          dpmtMapper.updDpmtApproval(approval);
          result.add(dpmtMapper.getDpmtApproval(approval.getApprovalId()));
          if (approval.getState().equals(APPROVAL_STATE_REJECTED)) {
            rejected = true;
            rejectComments = approval.getComments();
          }
        }
        CmsDeployment dpmt = new CmsDeployment();
        dpmt.setDeploymentId(dpmtId);
        dpmt.setUpdatedBy(userId);
        if (rejected) {
          dpmt.setDeploymentState(DPMT_STATE_CANCELED);
          dpmt.setComments(rejectComments);
        } else {
          dpmt.setDeploymentState(DPMT_STATE_ACTIVE);
        }
        updateDeployment(dpmt);
      }
      return result;
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | GeneralSecurityException e) {
      e.printStackTrace();
      throw new DJException(CmsError.DJ_EXCEPTION, e.getMessage());
    }
  }

    private void checkIfAthorized(String approvalToken, long approvalId) throws IllegalAccessException, GeneralSecurityException {
        if (cmsCrypto != null) {
            CmsDpmtApproval approval = dpmtMapper.getDpmtApproval(approvalId);
            CmsCI support = cmProcessor.getCiById(approval.getGovernCiId());
            CmsCIAttribute approvalTokenAttribute = support.getAttribute("approval_token");
            CmsCIAttribute approvalAuthTypeAttribute = support.getAttribute("approval_auth_type");
            if (approvalAuthTypeAttribute != null && approvalTokenAttribute != null && "token".equals(support.getAttribute("approval_auth_type").getDfValue())) {
                if (!cmsCrypto.decrypt(approvalTokenAttribute.getDjValue()).equals(approvalToken)) {
                    throw new IllegalAccessException("Not authorized to approve this record");
                }
            }
        }
    }

  private boolean needApproval(CmsDeployment dpmt) {
    logger.info("creating deployment record");
    boolean needApproval = false;

    List<Long> cloudIds = dpmtMapper
        .getToCiIdsForDeployment(dpmt.getDeploymentId(), "pending", "base.DeployedTo");

    List<CmsDpmtApproval> approvals = dpmtMapper.getDpmtApprovals(dpmt.getDeploymentId());
    //convert to map
    Map<Long, List<CmsDpmtApproval>> approvalMap = new HashMap<>();
    for (CmsDpmtApproval approval : approvals) {
      if (!approvalMap.containsKey(approval.getGovernCiId())) {
        approvalMap.put(approval.getGovernCiId(), new ArrayList<>());
      }
      approvalMap.get(approval.getGovernCiId()).add(approval);
    }

    List<CmsCI> governCIs = new ArrayList<>();
    for (long cloudId : cloudIds) {
      for (CmsCIRelation supportRel : cmProcessor
          .getFromCIRelations(cloudId, "base.SupportedBy", null)) {
        governCIs.add(supportRel.getToCi());
      }
    }
    for (long cloudId : cloudIds) {
      for (CmsCIRelation complianceRel : cmProcessor
          .getFromCIRelations(cloudId, "base.CompliesWith", null)) {
        governCIs.add(complianceRel.getToCi());
      }
    }

    for (CmsCI governCi : governCIs) {
      if (governCi.getAttribute("enabled") != null && Boolean.TRUE.toString()
          .equals(governCi.getAttribute("enabled").getDjValue())
          && governCi.getAttribute("approval") != null && Boolean.TRUE.toString()
          .equals(governCi.getAttribute("approval").getDjValue())) {
        if (!approvalMap.containsKey(governCi.getCiId())) {
          createDpmtApproval(dpmt.getDeploymentId(), governCi);
          needApproval = true;
        } else {
          CmsDpmtApproval approval = getLatestApproval(approvalMap.get(governCi.getCiId()));
          if (approval == null) {
            createDpmtApproval(dpmt.getDeploymentId(), governCi);
            needApproval = true;
          } else if (approval.getState().equals(APPROVAL_STATE_PENDING)) {
            needApproval = true;
          } else if (approval.getIsExpired() && approval.getState()
              .equals(APPROVAL_STATE_APPROVED)) {
            //approval.setState(APPROVAL_STATE_EXPIRED);
            //dpmtMapper.updDpmtApproval(approval);
            createDpmtApproval(dpmt.getDeploymentId(), governCi);
            needApproval = true;
          }

        }
      }
    }
    return needApproval;
  }

  private CmsDpmtApproval getLatestApproval(List<CmsDpmtApproval> approvals) {
    if (approvals.size() == 0) {
      return null;
    } else {
      Collections.sort(approvals, (a1, a2) -> {
        //Sorts by 'ApprovalId' property desc
        return (int) (a2.getApprovalId() - a1.getApprovalId());
      });
      return approvals.get(0);
    }
  }

  private boolean needApprovalForNewDpmt(CmsDeployment dpmt) {
    List<Long> cloudIds = dpmtMapper
        .getToCiIdsForReleasePending(dpmt.getReleaseId(), "base.DeployedTo");
    List<CmsCI> governCIs = new ArrayList<>();
    for (long cloudId : cloudIds) {
      for (CmsCIRelation supportRel : cmProcessor
          .getFromCIRelations(cloudId, "base.SupportedBy", null)) {
        governCIs.add(supportRel.getToCi());
      }
    }
    for (long cloudId : cloudIds) {
      for (CmsCIRelation complianceRel : cmProcessor
          .getFromCIRelations(cloudId, "base.CompliesWith", null)) {
        governCIs.add(complianceRel.getToCi());
      }
    }

    for (CmsCI governCi : governCIs) {
      if (governCi.getAttribute("enabled") != null && Boolean.TRUE.toString()
          .equals(governCi.getAttribute("enabled").getDjValue())
          && governCi.getAttribute("approval") != null && Boolean.TRUE.toString()
          .equals(governCi.getAttribute("approval").getDjValue())) {
        return true; //this domt need approval
      }
    }
    return false;
  }


  private void createDpmtApproval(long dpmtId, CmsCI governCi) {
    CmsDpmtApproval approval = new CmsDpmtApproval();
    approval.setDeploymentId(dpmtId);
    approval.setGovernCiId(governCi.getCiId());
    approval.setGovernCiJson(gson.toJson(governCi));
    dpmtMapper.createDpmtApproval(approval);
    logger.info("Created approval for dpmtId = " + dpmtId + " : " + approval.getGovernCiJson());
  }

  /**
   * Update deployment.
   *
   * @param dpmt the dpmt
   * @return the cms deployment
   */
  public CmsDeployment updateDeployment(CmsDeployment dpmt) {
    CmsDeployment existingDpmt = dpmtMapper.getDeploymentSimple(dpmt.getDeploymentId());

    updateAutoDeployExecOrders(dpmt);

    //in case the new state = old state just update the comments and timestamp CMS_ALL event will not be generated
    if (existingDpmt.getDeploymentState().equals(dpmt.getDeploymentState())) {
      String currentState = dpmt.getDeploymentState();
      dpmt.setDeploymentState(null);
      dpmtMapper.updDeployment(dpmt);
      logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " kept the old state ");
      dpmt.setDeploymentState(currentState);
    } else if (DPMT_STATE_CANCELED.equalsIgnoreCase(dpmt.getDeploymentState())) {
      if (dpmtMapper
          .getDeploymentRecordsCountByState(dpmt.getDeploymentId(), DPMT_RECORD_STATE_INPROGRESS,
              null) > 0) {
        String errMsg = "The deployment still have active work orders!";
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_FAILED_ERROR, errMsg);
      }
      if (DPMT_STATE_FAILED.equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PAUSED
          .equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PENDING
          .equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        dpmtMapper.cancelDeployment(dpmt);
        logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt.getDeploymentState());

        // Also automatically cancel open deployment release.
        long releaseId = existingDpmt.getReleaseId();
        CmsRelease bomRelease = rfcProcessor.getReleaseById(releaseId);
        bomRelease.setReleaseState("canceled");
        rfcProcessor.updateRelease(bomRelease);
        logger.info("Canceled deployment release releaseId " + releaseId);

        existingDpmt = dpmtMapper.getDeployment(dpmt.getDeploymentId());
        capacityProcessor.discardCapacityForDeployment(existingDpmt);
        return existingDpmt;
      } else {
        String errMsg =
            "The deployment is not in failed/active state - " + existingDpmt.getDeploymentState();
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_FAILED_ERROR, errMsg);
      }
    } else if (DPMT_STATE_ACTIVE.equalsIgnoreCase(dpmt.getDeploymentState())) {
      if (DPMT_STATE_PENDING.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        if (!needApproval(dpmt)) {
          dpmtMapper.updDeployment(dpmt);
          logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
              .getDeploymentState());
        }
      } else if (DPMT_STATE_FAILED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        dpmtMapper.resetFailedRecords(dpmt);
        if (needApproval(dpmt)) {
          dpmt.setDeploymentState(DPMT_STATE_PENDING);
          dpmtMapper.updDeployment(dpmt);
          logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
              .getDeploymentState());
        } else {
          dpmtMapper.retryDeployment(dpmt);
        }
      } else if (DPMT_STATE_PAUSED.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        if (needApproval(dpmt)) {
          dpmt.setDeploymentState(DPMT_STATE_PENDING);
        }
        dpmtMapper.updDeployment(dpmt);
        logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
            .getDeploymentState());
      } else {
        String errMsg =
            "The deployment is not in failed state - " + existingDpmt.getDeploymentState();
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_FAILED_ERROR, errMsg);
      }
    } else if (DPMT_STATE_COMPLETE.equalsIgnoreCase(dpmt.getDeploymentState())) {
      if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PAUSED
          .equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        //dpmtMapper.completeDeployment(dpmt);
        completeDeployment(dpmt);
        logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
            .getDeploymentState());
      } else {
        String errMsg =
            "The deployment is not in active state - " + existingDpmt.getDeploymentState();
        logger.warn(errMsg);
        //throw new DJException(errMsg);
      }
    } else if (DPMT_STATE_FAILED.equalsIgnoreCase(dpmt.getDeploymentState())) {
      if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState()) || DPMT_STATE_PAUSED
          .equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        dpmtMapper.updDeployment(dpmt);
        logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
            .getDeploymentState());
      } else {
        String errMsg =
            "The deployment is not in active state - " + existingDpmt.getDeploymentState();
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_ERROR, errMsg);
      }
    } else if (DPMT_STATE_PAUSED.equalsIgnoreCase(dpmt.getDeploymentState())) {
      if (DPMT_STATE_ACTIVE.equalsIgnoreCase(existingDpmt.getDeploymentState())) {
        dpmtMapper.updDeployment(dpmt);
        logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
            .getDeploymentState());
      } else {
        String errMsg =
            "The deployment is not in active state - " + existingDpmt.getDeploymentState();
        logger.error(errMsg);
        throw new DJException(CmsError.DJ_DEPLOYMENT_NOT_ACTIVE_ERROR, errMsg);
      }
    } else if (dpmt.getDeploymentState() == null) {
      dpmtMapper.updDeployment(dpmt);
      logger.info("Updated dpmtId = " + dpmt.getDeploymentId() + " with new state  " + dpmt
          .getDeploymentState());
    } else {
      String errMsg = "This state is not supported - " + dpmt.getDeploymentState();
      logger.error(errMsg);
      throw new DJException(CmsError.DJ_NOT_SUPPORTED_STATE_ERROR, errMsg);
    }

    return dpmtMapper.getDeployment(dpmt.getDeploymentId());
  }

  private void completeDeployment(CmsDeployment dpmt) {
    dpmtMapper.completeDeployment(dpmt);
    CmsRelease bomRelease = rfcProcessor.getReleaseById(dpmt.getReleaseId());
    CmsRelease manifestRelease = rfcProcessor.getReleaseById(bomRelease.getParentReleaseId());
    List<CmsCI> platforms = cmProcessor
        .getCiBy3NsLike(manifestRelease.getNsPath(), MANIFEST_PLATFORM_CLASS, null);
    for (CmsCI plat : platforms) {
      boolean vacuumAllowed = true;
      List<CmsCI> monitorCiList = new ArrayList<>();
      Set<Long> monitors4DeletedComponents = new HashSet<>();

      List<CmsCIRelation> platformCloudRels = cmProcessor
          .getFromCIRelationsNaked(plat.getCiId(), "base.Consumes", "account.Cloud");
      for (CmsCIRelation platformCloudRel : platformCloudRels) {
        if (platformCloudRel.getAttribute("adminstatus") != null && !CmsConstants.CLOUD_STATE_ACTIVE
            .equals(platformCloudRel.getAttribute("adminstatus").getDjValue())) {
          String platBomNs = plat.getNsPath().replace("/manifest/", "/bom/");
          List<CmsCIRelation> deployedToRels = cmProcessor
              .getToCIRelationsByNsNaked(platformCloudRel.getToCiId(), "base.DeployedTo", null,
                  null, platBomNs);
          if (deployedToRels.size() > 0) {
            vacuumAllowed = false;
            break;
          }
        } else {
          List<CmsCI> componentsToDelet = cmProcessor
              .getCiByNsLikeByStateNaked(plat.getNsPath(), null, "pending_deletion");
          for (CmsCI component : componentsToDelet) {
            long ciId = component.getCiId();
            if (CmsConstants.MONITOR_CLASS.equals(component.getCiClassName())) {
              monitorCiList.add(component);
              continue;
            }

            List<CmsCIRelation> realizedAsRels = cmProcessor
                .getFromCIRelationsNakedNoAttrs(ciId, "base.RealizedAs", null, null);
            if (realizedAsRels.size() > 0) {
              vacuumAllowed = false;
              break;
            }

            List<CmsCIRelation> monitorRels = cmProcessor
                .getFromCIRelationsNakedNoAttrs(ciId, CmsConstants.MANIFEST_WATCHED_BY, null,
                    CmsConstants.MONITOR_CLASS);
            if (monitorRels.size() > 0) {
              monitors4DeletedComponents.addAll(
                  monitorRels.stream().map(CmsCIRelationBasic::getToCiId)
                      .collect(Collectors.toList()));
            }
          }
          if (!vacuumAllowed) {
            break;
          }
        }
      }
      if (vacuumAllowed) {

        //if there are monitors to be deleted, then make sure it satisfies one of these two
        // 1. the monitors have corresponding parent CIs that are also marked pending_deletion, these would be in monitors4DeletedComponents
        // 2. all the bom CIs for this manifest are updated for this manifest release
        monitorCiList.removeIf(monitor -> monitors4DeletedComponents.contains(monitor.getCiId()));
        List<CmsCI> monitorsEligible4Del = getMonitorsEligible4Del(monitorCiList);

        if (monitorsEligible4Del.size() == monitorCiList.size()) {
          nsMapper.vacuumNamespace(plat.getNsId(), dpmt.getCreatedBy());
        } else {
          monitorsEligible4Del
              .forEach(monitor -> cmProcessor.deleteCI(monitor.getCiId(), dpmt.getCreatedBy()));
        }
      }
    }

    deleteGlobalVars(manifestRelease.getNsPath(), dpmt.getCreatedBy());
  }

  /**
   * Delete global variables in pending_delete state
   */
  private void deleteGlobalVars(String envNsPath, String userId) {
    List<CmsCI> toDelComponents = cmProcessor
        .getCiByNsLikeByStateNaked(envNsPath + "/", null, "pending_deletion");
    //if no components are marked for deletion then delete the global variables in pending_deletion state
    if (toDelComponents.isEmpty()) {
      //delete global vars in pending_delete state
      for (CmsCI globalVar : cmProcessor
          .getCiByNsLikeByStateNaked(envNsPath, "manifest.Globalvar", "pending_deletion")) {
        cmProcessor.deleteCI(globalVar.getCiId(), true, userId);
      }
    }
  }

  private List<CmsCI> getMonitorsEligible4Del(List<CmsCI> monitorCiList) {

    List<CmsCI> monitorsEligible4Del = new ArrayList<>();
    if (!monitorCiList.isEmpty()) {
      monitorsEligible4Del.addAll(monitorCiList.stream().
          filter(monitor -> {
            long monitorRfcId = monitor.getLastAppliedRfcId();
            logger.info(
                "monitor ci : " + monitor.getCiId() + ", last applied rfc id : " + monitorRfcId);
            List<CmsCIRelation> parentRelation = cmProcessor
                .getToCIRelationsNakedNoAttrs(monitor.getCiId(), CmsConstants.MANIFEST_WATCHED_BY,
                    null, null);
            if (parentRelation.size() > 0) {
              long baseRfcId = monitorRfcId;
              long parentCiId = parentRelation.get(0).getFromCiId();

              //get the parent ci rfcId from the same release and use that to compare with bom instances
              CmsRfcCI monitorRfc = rfcProcessor.getRfcCIById(monitorRfcId);
              List<CmsRfcCI> parentRfcs = rfcProcessor
                  .getRfcCIBy3(monitorRfc.getReleaseId(), true, parentCiId);
              if (!parentRfcs.isEmpty()) {
                baseRfcId = parentRfcs.get(0).getRfcId();
                logger.info("component ci rfc id : " + baseRfcId);
              }

              //get count of boms that are not updated by this manifest rfc
              long count = rfcProcessor
                  .getCiCountNotUpdatedByRfc(parentCiId, CmsConstants.BASE_REALIZED_AS, null,
                      baseRfcId);
              logger.info("ci not deployed count " + count);
              if (count > 0) {
                //there are some bom CIs that are not deployed, so this monitor cannot be removed
                return false;
              }
            }
            return true;
          }).
          collect(Collectors.toList()));
    }
    return monitorsEligible4Del;
  }


  /**
   * Update dpmt record.
   *
   * @param rec the rec
   * @return the cms dpmt record
   */
  public CmsDpmtRecord updateDpmtRecord(CmsDpmtRecord rec) {
    dpmtMapper.updDpmtRecordState(rec);
    return dpmtMapper.getDeploymentRecord(rec.getDpmtRecordId());
  }

  public void updateDpmtRecordSimple(CmsDpmtRecord rec) {
    dpmtMapper.updDpmtRecordState(rec);
  }

  /**
   * Complete work order.
   *
   * @param wo the wo
   */
  public void completeWorkOrder(CmsWorkOrder wo) {
    try {
        capacityProcessor.adjustCapacity(wo);
    } catch (Exception e) {
      logger.error("Error while processing capacity for WO with rfcId=" + wo.getRfcId() + "(" + wo.getRfcCi().getNsPath() +
                           ", proceeding anyway.");
    }

    dpmtMapper.updDpmtRecordState(wo);
    if (!wo.getRfcCi().getRfcAction().equalsIgnoreCase("delete") && wo.getResultCi() != null) {
      wo.getResultCi().setUpdatedBy(wo.getRfcCi().getCreatedBy() + ":controller");
      cmProcessor.updateCI(wo.getResultCi());
    }

    processAdditionalInfo(wo);
  }

  private void processAdditionalInfo(CmsWorkOrder wo) {
    Map<String, String> additionalInfo = wo.getAdditionalInfo();
    if (additionalInfo != null && !additionalInfo.isEmpty()) {

      additionalInfo.entrySet().forEach(entry -> {
        String key = entry.getKey();
        if (ZONES_SELECTED.equalsIgnoreCase(key)) {
          processSelectedZones(wo, entry);
        }
      });
    }
  }

  private void processSelectedZones(CmsWorkOrder wo, Entry<String, String> entry) {
    List<CmsCIRelation> existingRelations = cmProcessor
        .getFromCIRelations(wo.getResultCi().getCiId(), CmsConstants.BASE_PLACED_IN,
            CmsConstants.ZONE_CLASS);
    Map<String, CmsCIRelation> relationsMap = existingRelations.stream().
        collect(Collectors.toMap(relation -> relation.getToCi().getCiName(), Function.identity()));
    String values = entry.getValue();
    if (values != null) {
      String[] zones = values.split(",");
      logger.info("selected zones for this ciId [" + wo.getResultCi().getCiId() + "] : " + values);
      logger.info("existing zones : " + relationsMap.keySet());

      Set<String> selectedZones = Collections.emptySet();
      if (zones.length > 0) {
        selectedZones = Stream.of(zones).map(String::trim).collect(Collectors.toSet());
        createPlacedInRelations(selectedZones, relationsMap, wo);
      }
      removeOldRelations(selectedZones, relationsMap);
    }
  }

  private void createPlacedInRelations(Set<String> zones, Map<String, CmsCIRelation> relationsMap,
      CmsWorkOrder wo) {
    List<CmsCIRelation> deployedToRels = cmProcessor
        .getFromCIRelations(wo.getResultCi().getCiId(), CmsConstants.DEPLOYED_TO,
            CmsConstants.CLOUD_CLASS);
    if (deployedToRels != null && !deployedToRels.isEmpty()) {

      zones.stream().
          filter(zone -> (zone.length() > 0 && !(relationsMap.containsKey(zone)))).
          forEach(zone -> {
            if (logger.isDebugEnabled()) {
              logger.debug("creating base.PlacedIn relation to zone : " + zone);
            }
            CmsCI cloudCi = deployedToRels.get(0).getToCi();
            String zoneNs = formZoneNsPath(cloudCi.getNsPath(), cloudCi.getCiName());
            List<CmsCI> cis = cmProcessor.getCiBy3(zoneNs, CmsConstants.ZONE_CLASS, zone);
            if (cis == null || cis.isEmpty()) {
              logger.error("zone ci not found " + zone + ", nsPath : " + zoneNs);
            } else {
              CmsCI zoneCi = cis.get(0);
              CmsCI resultCi = wo.getResultCi();
              CmsRfcCI rfcCi = wo.getRfcCi();
              resultCi.setCiName(rfcCi.getCiName());
              resultCi.setCiClassName(rfcCi.getCiClassName());
              CmsCIRelation placedInRel = cmProcessor
                  .bootstrapRelation(resultCi, zoneCi, CmsConstants.BASE_PLACED_IN,
                      rfcCi.getNsPath(), rfcCi.getCreatedBy(), rfcCi.getCreated());
              cmProcessor.createRelation(placedInRel);
            }
          });
    } else {
      logger.error("no base.DeployedTo relation found for ci " + wo.getResultCi().getCiId());
    }
  }

  private void removeOldRelations(Set<String> zones, Map<String, CmsCIRelation> relationsMap) {
    relationsMap.entrySet().stream().filter(entry -> !zones.contains(entry.getKey()))
        .forEach(entry -> {
          if (logger.isDebugEnabled()) {
            logger.debug("removing base.PlacedIn relation to zone : " + entry.getKey());
          }
          cmProcessor.deleteRelation(entry.getValue().getCiRelationId(), true);
        });
  }

  private String formZoneNsPath(String cloudNsPath, String cloudName) {
    return cloudNsPath + "/" + cloudName;
  }

  /**
   * Gets the deployment.
   *
   * @param dpmtId the dpmt id
   * @return the deployment
   */
  public CmsDeployment getDeployment(long dpmtId) {
    return dpmtMapper.getDeployment(dpmtId);
  }

  /**
   * Find deployment.
   *
   * @param nsPath the ns path
   * @param state the state
   * @param recursive the recursive
   * @return the list
   */
  public List<CmsDeployment> findDeployment(String nsPath, String state, Boolean recursive) {
    if (recursive != null && recursive) {
      String nsLike = CmsUtil.likefyNsPath(nsPath);
      return dpmtMapper.findDeploymentRecursive(nsPath, nsLike, state);
    } else {
      return dpmtMapper.findDeployment(nsPath, state);
    }
  }

  /**
   * Find latest deployment.
   *
   * @param nsPath the ns path
   * @param state the state
   * @param recursive the recursive
   * @return the list
   */
  public List<CmsDeployment> findLatestDeployment(String nsPath, String state, Boolean recursive) {
    if (recursive != null && recursive) {
      String nsLike = CmsUtil.likefyNsPath(nsPath);
      return dpmtMapper.findLatestDeploymentRecursive(nsPath, nsLike, state);
    } else {
      return dpmtMapper.findLatestDeployment(nsPath, state);
    }
  }

  /**
   * Returns a list of deployments for a given namespace and time period.
   *
   * @param nsPath namespace
   * @param recursive find deployments recursively for a given nsPath
   * @param start start timestamp
   * @param end end timestamp
   * @return list of deployments
   */
  public List<CmsDeployment> findDeploymentsByTimePeriod(String nsPath, Boolean recursive, Date start, Date end) {
    String nsLike = null;
    if (recursive != null && recursive) {
      nsLike = CmsUtil.likefyNsPath(nsPath);
    }
    return dpmtMapper.findDeploymentsByTimePeriod(nsPath, nsLike, start, end);
  }

    /**
     * Count deployment.
     *
     * @param nsPath the ns path
     * @param state the state
     * @param recursive the recursive
     * @return the long
     */
  public long countDeployment(String nsPath, String state, Boolean recursive) {
    if (recursive != null && recursive) {
      String nsLike = CmsUtil.likefyNsPath(nsPath);
      return dpmtMapper.countDeploymentRecursive(nsPath, nsLike, state);
    } else {
      return dpmtMapper.countDeployment(nsPath, state);
    }
  }

  /**
   * Count deployment group by.
   *
   * @param nsPath the ns path
   * @param state the state
   * @return the map
   */
  public Map<String, Long> countDeploymentGroupBy(String nsPath, String state) {
    Map<String, Long> result = new HashMap<>();
    String nsLike = CmsUtil.likefyNsPath(nsPath);
    List<Map<String, Object>> stats = dpmtMapper.countDeploymentGroupByNs(nsPath, nsLike, state);
    for (Map<String, Object> row : stats) {
      String nspath = (String) row.get("path");
      Long cnt = (Long) row.get("cnt");
      result.put(nspath, cnt);
    }

    return result;
  }


  /**
   * Find deployment by release id.
   *
   * @param releaseId the release id
   * @param state the state
   * @return the list
   */
  public List<CmsDeployment> findDeploymentByReleaseId(long releaseId, String state) {
    return dpmtMapper.findDeploymentByReleaseId(releaseId, state);
  }

  /**
   * Find latest deployment by release id.
   *
   * @param releaseId the release id
   * @param state the state
   * @return the list
   */
  public List<CmsDeployment> findLatestDeploymentByReleaseId(long releaseId, String state) {
    return dpmtMapper.findLatestDeploymentByReleaseId(releaseId, state);
  }

  /**
   * Gets the deployment records.
   *
   * @param dpmtId the dpmt id
   * @return the deployment records
   */
  public List<CmsDpmtRecord> getDeploymentRecords(long dpmtId) {
    return dpmtMapper.getDeploymentRecords(dpmtId);
  }

  /**
   * Gets the deployment approvals.
   *
   * @param dpmtId the dpmt id
   * @return the deployment approvals
   */
  public List<CmsDpmtApproval> getDeploymentApprovals(long dpmtId) {
    return dpmtMapper.getDpmtApprovals(dpmtId);
  }

  /**
   * Gets the deployment approval.
   *
   * @param approvalId the approval id
   * @return the deployment approval
   */
  public CmsDpmtApproval getDeploymentApproval(long approvalId) {
    return dpmtMapper.getDpmtApproval(approvalId);
  }

  /**
   * Gets the deployment record by dpmtRfcId.
   *
   * @param dpmtRecordId the dpmtRfcId
   * @return the deployment records
   */
  public CmsDpmtRecord getDeploymentRecord(long dpmtRecordId) {
    return dpmtMapper.getDeploymentRecord(dpmtRecordId);
  }

  /**
   * Gets the deployment record cis.
   *
   * @param dpmtId the dpmt id
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordCis(long dpmtId) {
    return dpmtMapper.getDeploymentRecordCis(dpmtId);
  }

  /**
   * Gets the deployment record cis.
   *
   * @param deploymentRecordIds of deployment record ids
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordCis(long dpmtId, List<Long> deploymentRecordIds) {
    return dpmtMapper.getDeploymentRecordCisByListOfIds(dpmtId, deploymentRecordIds);
  }

  /**
   * Gets the deployment record cis.
   *
   * @param rfcIds of rfc ids
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordCisByRfcIds(long dpmtId, List<Long> rfcIds) {
    return dpmtMapper.getDeploymentRecordCisByRfcIds(dpmtId, rfcIds);
  }

  /**
   * Gets the deployment record cis.
   *
   * @param ciId the ciId
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordByCiId(long ciId, String state) {
    return dpmtMapper.getDeploymentRecordsByCiId(ciId, state);
  }

  /**
   * Gets the deployment record cis by state.
   *
   * @param dpmtId the dpmt id
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordCisByState(long dpmtId, String state,
      Integer execOrder) {
    return dpmtMapper.getDeploymentRecordsByState(dpmtId, state, execOrder);
  }

  /**
   * Gets the deployment record cis updated after a given timestamp.
   *
   * @param dpmtId the dpmt id
   * @param timestamp udpated timestamp
   * @return the deployment record cis
   */
  public List<CmsDpmtRecord> getDeploymentRecordsUpdatedAfter(long dpmtId, Date timestamp) {
    return dpmtMapper.getDeploymentRecordsUpdatedAfter(dpmtId, timestamp);
  }

  /**
   * Gets the deployment record relations.
   *
   * @param dpmtId the dpmt id
   * @return the deployment record relations
   */
  public List<CmsDpmtRecord> getDeploymentRecordRelations(long dpmtId) {
    return dpmtMapper.getDeploymentRecordRelations(dpmtId);
  }

  public long getDeploymentRecordCount(long dpmtId, String state, Integer execOrder) {
    return dpmtMapper.getDeploymentRecordsCountByState(dpmtId, state, execOrder);
  }

  public List<CmsDpmtStateChangeEvent> getDeploymentStateHist(long deploymentId) {
    return dpmtMapper.getDeploymentStateHist(deploymentId);
  }

  /**
   * Gets the current deployments in progress for environment.
   *
   * @param nsPath The path to search for open deployments .
   * @return will return *Null*  if no deployment is found in |active|failed|paused| ,else return
   * first deployment found in matching state
   */
  public CmsDeployment getOpenDeployments(String nsPath) {
    List<CmsDeployment> currentDeployments = findLatestDeployment(nsPath, null, false);
    List<CmsDeployment> openDeployments = currentDeployments.stream()
        .filter(cmsDeployment -> cmsDeployment.getDeploymentState().matches(OPEN_DEPLOYMENT_REGEXP))
        .collect(Collectors.toList());
    if (openDeployments != null) {
      if (openDeployments.size() > 0) {
        return openDeployments.get(0);
      }
    }
    return null;
  }

  public List<TimelineDeployment> getDeploymentsByFilter(TimelineQueryParam queryParam) {
    List<TimelineDeployment> deployments;

    String filter = queryParam.getWildcardFilter();
    if (StringUtils.isBlank(filter)) {
      queryParam.setWildcardFilter(null);   // In case it was blank
      deployments = dpmtMapper.getDeploymentsByNsPath(queryParam);
    } else {
      queryParam.setRfcNsFilter(CmsUtil.likefyNsPathWithFilter(queryParam.getNsPath(), CmsConstants.BOM, filter));
      deployments = dpmtMapper.getDeploymentsByFilter(queryParam);
    }
    return deployments;
  }

  public Map<String, Integer> getWorkordersCountByState(long deploymentId, int execOrder) {
    List<Map<String,Object>> list = dpmtMapper.getDeploymentRecordsCountByStates(deploymentId, execOrder);
    Map<String, Integer> woCountMap = new HashMap<>();
    if (list != null) {
      list.stream().forEach(m -> {
        woCountMap.put((String) m.get("state"), ((Long) m.get("count")).intValue());
      });
    }
    return woCountMap;
  }

  public void updateDeploymentStep(CmsDeployment deployment) {
    dpmtMapper.updateDeploymentCurrentStep(deployment);
  }

  public void updateDeploymentExecInfo(CmsDeployment deployment) {
    dpmtMapper.updateDeploymentExecInfo(deployment);
  }

  public void getDpmtLock(long deploymentId) {
    dpmtMapper.getDpmtLock(deploymentId);
  }

  public void createDeploymentExec(long deploymentId, int step, String state) {
    dpmtMapper.createDeploymentExec(deploymentId, step, state);
  }

  public int getAndUpdateStepState(long deploymentId, int step, String newState) {
    return dpmtMapper.getAndUpdateStepState(deploymentId, step, newState);
  }


  public boolean getBooleanVariable(String varName) {
    boolean cmVarValue = false;
    CmsVar cmSimpleVar = cmProcessor.getCmSimpleVar(varName);
    if (cmSimpleVar != null) {
      cmVarValue = BooleanUtils.toBoolean(cmSimpleVar.getValue());
    }

    return cmVarValue;
  }

  public List<Integer> getDeploymentDistinctStepsTotal(long deploymentId){
    return djMapper.getDeploymentDistinctStepsTotal(deploymentId);
  }
}