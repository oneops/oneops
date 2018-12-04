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
package com.oneops.controller.cms;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsActionOrder;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.collections.CollectionProcessor;
import com.oneops.cms.collections.def.CollectionLinkDefinition;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.domain.CmsWorkOrder;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcUtil;
import com.oneops.cms.domain.CmsWorkOrderBase;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.exceptions.DJException;
import com.oneops.cms.simple.domain.CmsActionOrderSimple;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.cms.util.CmsError;
import com.oneops.cms.util.CmsUtil;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import static com.oneops.cms.util.CmsConstants.*;
import static java.util.stream.Collectors.*;


/**
 * The Class CmsWoProvider.
 */
public class CmsWoProvider {

  private static final Logger logger = Logger.getLogger(CmsWoProvider.class);
  private static final String REQUIRES_COMPUTES_PAYLOAD_NAME = "RequiresComputes";
  private static final String REQUIRES_COMPUTES = "REQUIRES_COMPUTES";
  private static final String OFFERING = "offerings";
  private static final String IS_PLATFORM_ENABLED_ATTR = "is_platform_enabled";
  private static final String IS_PLATFORM_ENABLED_REL_ATTR = "enabled";
  private static final String EXTRA_RUNLIST_PAYLOAD_NAME = "ExtraRunList";
  private static final boolean OFFERING_ENABLED = "true"
      .equals(System.getProperty("controller.offerings.on", "true"));
  private DJDpmtMapper dpmtMapper;
  private OpsMapper opsMapper;
  private CmsCmRfcMrgProcessor cmrfcProcessor;
  private CmsCmProcessor cmProcessor;
  private DJMapper djMapper;
  private CmsRfcUtil rfcUtil;
  private CollectionProcessor colProcessor;
  private Gson gson = new Gson();
  private CmsUtil cmsUtil;
  private OfferingsMatcher offeringMatcher;
  private ExpressionEvaluator expressionEvaluator;
  private ControllerCache controllerCache;

  private static final String SERVICE_TYPE_ATTR = "service_type";
  private static final String PAYLOAD_FQDN_CLOUDS = "fqdnclouds";
  private static final String CONSUMES_ATTRIBUTE = "base.Consumes.priority";
  private static final String GSLB_WEIGHTS_ENABLED_VAR = "GSLB_WEIGHTS_ENABLED";

  private boolean isGslbWeightsEnabled;

  private boolean requiresComputesCheckEnabled;

  /**
   * Sets the cms util.
   *
   * @param cmsUtil the new cms util
   */
  public void setCmsUtil(CmsUtil cmsUtil) {
    this.cmsUtil = cmsUtil;
  }

  /**
   * Sets the dj mapper.
   *
   * @param djMapper the new dj mapper
   */
  public void setDjMapper(DJMapper djMapper) {
    this.djMapper = djMapper;
  }

  /**
   * Sets the cm processor.
   *
   * @param cmProcessor the new cm processor
   */
  public void setCmProcessor(CmsCmProcessor cmProcessor) {
    this.cmProcessor = cmProcessor;
  }

  /**
   * Sets the cmrfc processor.
   *
   * @param cmrfcProcessor the new cmrfc processor
   */
  public void setCmrfcProcessor(CmsCmRfcMrgProcessor cmrfcProcessor) {
    this.cmrfcProcessor = cmrfcProcessor;
  }

  /**
   * Sets the dpmt mapper.
   *
   * @param dpmtMapper the new dpmt mapper
   */
  public void setDpmtMapper(DJDpmtMapper dpmtMapper) {
    this.dpmtMapper = dpmtMapper;
  }

  /**
   * Sets the rfc util.
   *
   * @param rfcUtil the new rfc util
   */
  public void setRfcUtil(CmsRfcUtil rfcUtil) {
    this.rfcUtil = rfcUtil;
  }

  /**
   * Sets the ops mapper.
   *
   * @param opsMapper the new ops mapper
   */
  public void setOpsMapper(OpsMapper opsMapper) {
    this.opsMapper = opsMapper;
  }

  /**
   * Sets the col processor.
   *
   * @param colProcessor the new col processor
   */
  public void setColProcessor(CollectionProcessor colProcessor) {
    this.colProcessor = colProcessor;
  }

  public List<CmsActionOrderSimple> getActionOrdersSimple(long procedureId, OpsProcedureState state,
      Integer execOrder) {
    List<CmsActionOrder> aorders = getActionOrders(procedureId, state, execOrder);
    List<CmsActionOrderSimple> aosSimple = new ArrayList<>();
    for (CmsActionOrder ao : aorders) {
      aosSimple.add(cmsUtil.custActionOrder2Simple(ao));
    }
    return aosSimple;
  }

  public CmsActionOrderSimple getAssembledAo(CmsActionOrder ao,
      Map<Long, CmsCI> manifestToTemplateMap) {
    assembleAo(ao, manifestToTemplateMap);
    return cmsUtil.custActionOrder2Simple(ao);
  }

  private boolean isCloudServiceAction(CmsActionOrder ao) {
    return ao.getCi().getCiClassName().startsWith(CLOUDSERVICEPREFIX);
  }

  public List<CmsActionOrder> getBaseActionOrders(long procedureId, OpsProcedureState state,
      Integer execOrder) {
    checkControllerCache();
    List<CmsActionOrder> aorders = opsMapper.getActionOrders(procedureId, state, execOrder);
    List<CmsActionOrder> actionOrders = Collections.EMPTY_LIST;
    if (!aorders.isEmpty()) {
      aorders.forEach(ao -> {
        CmsCI ci = cmProcessor.getCiById(ao.getCiId());
        ao.setCi(ci);
      });
      populateWoBase(aorders);
      // this is a special case for the cloud.Service usecase
      actionOrders = aorders.stream().
          filter(ao -> !isCloudServiceAction(ao)).collect(Collectors.toList());
      List<CmsActionOrder> cloudServiceOrders = aorders.stream().
          filter(ao -> isCloudServiceAction(ao)).collect(Collectors.toList());

      if (!actionOrders.isEmpty()) {
        List<CmsCI> envs = Collections
            .singletonList(getEnvAndPopulatePlatEnable(actionOrders.get(0).getBox()));
        actionOrders.forEach(ao -> ao.putPayLoadEntry("Environment", envs));
      }
      actionOrders.addAll(cloudServiceOrders);
    }
    return actionOrders;
  }

  /**
   * Gets the action orders.
   *
   * @param procedureId the procedure id
   * @param state the state
   * @param execOrder the exec order
   * @return the action orders
   */
  public List<CmsActionOrder> getActionOrders(long procedureId, OpsProcedureState state,
      Integer execOrder) {
    checkControllerCache();
    List<CmsActionOrder> aorders = getBaseActionOrders(procedureId, state, execOrder);
    Map<Long, CmsCI> manifestToTemplateMap = new HashMap<>();
    aorders.stream().filter(ao -> !isCloudServiceAction(ao))
        .forEach(ao -> assembleAo(ao, manifestToTemplateMap));
    return aorders;
  }

  private void assembleAo(CmsActionOrder ao, Map<Long, CmsCI> manifestToTemplateMap) {
    CmsCI env = getEnvFromAo(ao);
    //put all the variables in payload
    try {
      Map<String, List<CmsCI>> resolvedVariableCIs = cmsUtil
          .getResolvedVariableCIs(ao.getCloud(), env, ao.getBox());
      ao.getPayLoad().putAll(resolvedVariableCIs);
    } catch (Exception e) {

      logger.error(
          "Error in generating action order while resolving variables for env: " + env.getNsPath()
              + "/" + env.getCiName() + ", action name: " + ao.getActionName() + ", for CiId: " + ao
              .getCiId());
      //do not throw again because action-procedures may not need variables.. and if they do,
      //and the variable is not defined or badly encrypted, the recipe would fail anyway
    }

    List<CmsCI> managedVia = getCIRelatives(ao.getCiId(), "bom.ManagedVia", "from", null);
    //put proxy
    ao.putPayLoadEntry("ManagedVia", managedVia);
    //put depends on
    ao.putPayLoadEntry("DependsOn", getCIRelatives(ao.getCiId(), "bom.DependsOn", "from", null));
    //put realized as
    ao.putPayLoadEntry("RealizedAs", getCIRelatives(ao.getCiId(), "base.RealizedAs", "to", null));
    //put key pairs SecuredBy
    ao.putPayLoadEntry("SecuredBy", getKeyPairs(ao.getCi(), ao.getPayLoad().get("ManagedVia")));

    // if this is custom action from attachment - get the attachment
    if (ao.getActionName().equals("user-custom-attachment")) {
      setAttachment(env, ao);
    } else {
      //lets get the payload def from the template
      long manifestCiId = getRealizedAs(ao.getCiId());

      //
      List<CmsCI> attachments = getAttachments(ao, manifestCiId, env);
      if (!attachments.isEmpty()) {
        ao.putPayLoadEntry(ESCORTED_BY, attachments);
      }

      manifestToTemplateMap.computeIfAbsent(manifestCiId, (manifestId) -> {
        CmsCI manifestCi = cmProcessor.getCiById(manifestId);
        //process Escorted by relation
        CmsCI templObj = cmProcessor.getTemplateObjForManifestObj(manifestCi, env);
        if (templObj == null) {
          logger.error(
              "Can not find manifest template object for manifest ci id = " + manifestCi.getCiId()
                  + " ciName" + manifestCi.getCiName());
        }
        return templObj;
      });
      CmsCI managedViaTemplateCi = null;
      long managedViaBomCid = 0;
      if (managedVia != null && managedVia.size() > 0) {
        CmsCI managedViaEntity = managedVia.get(0);
        managedViaBomCid = managedViaEntity.getCiId();
        CmsCI managedViaManifestCi = new CmsCI();
        long managedViaManifestCiId = getRealizedAs(managedViaEntity.getCiId());
        managedViaManifestCi.setCiId(managedViaManifestCiId);
        managedViaManifestCi
            .setCiClassName(managedViaEntity.getCiClassName().replace("bom", "manifest"));
        managedViaTemplateCi = cmProcessor.getTemplateObjForManifestObj(managedViaManifestCi, env);
      }

      if (!manifestToTemplateMap.containsKey(manifestCiId)) {
        throw new DJException(CmsError.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR,
            "Can not find pack template for manifest component id=" + manifestCiId);
      }
      processPayLoadDef(ao, manifestToTemplateMap.get(manifestCiId), managedViaTemplateCi,
          managedViaBomCid, null, null, null);
      String actionPayLoad = ao.getPayLoadDef();
      processPayLoadDef(ao, actionPayLoad);
    }

    ao.putPayLoadEntry(EXTRA_RUNLIST_PAYLOAD_NAME, getMatchingCloudCompliance(ao));
    addVarsForConfig(ao);
  }

  private CmsCI getEnvFromAo(CmsActionOrder ao) {
    return ao.getPayLoad().get("Environment").get(0);
  }

  private void setAttachment(CmsCI env, CmsActionOrder ao) {
    CmsCI attachment = null;
    // for the global, cloud and local vars to evaluate for attachment
    Map<String, String> globalVars = cmsUtil.getGlobalVars(env);
    Map<String, String> cloudVars = cmsUtil.getCloudVars(ao.getCloud());
    Map<String, String> localVars = cmsUtil.getLocalVars(ao.getBox());

    if (ao.getExtraInfo() != null) {
      long attachmentId = Long.valueOf(ao.getExtraInfo());
      attachment = cmProcessor.getCiById(attachmentId);
    }

    if (attachment != null) {
      cmsUtil.processAllVars(attachment, cloudVars, globalVars, localVars);
      ao.putPayLoadEntry("EscortedBy", Collections.singletonList(attachment));
    } else {
      throw new CmsException(CmsError.CMS_NO_CI_WITH_GIVEN_ID_ERROR,
          "Can not find the attachment by id = " + ao.getExtraInfo());
    }
  }

  private List<CmsCI> getAttachments(CmsActionOrder ao, long manifestCiId, final CmsCI env) {
    List<CmsCI> attachments = getAttachments(manifestCiId).stream()
        .filter(rel -> isEligible(ao.getActionName(), rel.getToCi(), ATTR_RUN_ON_ACTION))
        .map(rel -> getAttachment(rel, env, ao)).collect(Collectors.toList());
    return attachments;
  }

  private List<CmsCIRelation> getAttachments(long manifestCiId) {
    return cmProcessor
        .getFromCIRelations(manifestCiId, "manifest.EscortedBy", "manifest.Attachment");
  }

  //interpolates the var and returns the attachment
  private CmsCI getAttachment(CmsCIRelation attachmentRelation, CmsCI env, CmsActionOrder ao) {
    CmsCI attachment = attachmentRelation.getToCi();
    cmsUtil.processAllVars(attachment, env, ao.getCloud(), ao.getBox());
    return attachment;
  }

  //interpolates the var and returns the attachment
  private CmsRfcCI getAttachmentRfc(CmsCIRelation attachmentRelation, Map<String, String> cloudVars,
      Map<String, String> globalVars, Map<String, String> localVars) {
    CmsCI attachment = attachmentRelation.getToCi();
    cmsUtil.processAllVars(attachment, cloudVars, globalVars, localVars);
    return rfcUtil.mergeRfcAndCi(null, attachment, "dj");
  }

  private List<CmsRfcCI> getEscortedBy(CmsRfcCI realizedAs, String action,
      Map<String, String> cloudVars, Map<String, String> globalVars,
      Map<String, String> localVars) {
    List<CmsRfcCI> attachments = getAttachments(realizedAs.getCiId()).stream()
        .filter(rel -> isEligible(action, rel.getToCi(), ATTR_RUN_ON))
        .map(rel -> getAttachmentRfc(rel, cloudVars, globalVars, localVars))
        .collect(Collectors.toList());
    return attachments;
  }


  private boolean isEligible(String action, CmsCI attachment, String attributeName) {
    return attachment.getAttribute(attributeName) != null
        && attachment.getAttribute(attributeName).getDjValue() != null && attachment
        .getAttribute(attributeName).getDjValue().contains(action);
  }

  public List<CmsWorkOrderSimple> getWorkOrderIdsSimple(long deploymentId, String state,
      Integer execOrder, Integer limit) {
    List<CmsWorkOrderSimple> wosList = new ArrayList<>();
    List<CmsWorkOrder> woList = getWorkOrderIds(deploymentId, state, execOrder, limit);
    woToWoSimple(woList, wosList);
    return wosList;
  }

  private void woToWoSimple(List<CmsWorkOrder> woList, List<CmsWorkOrderSimple> wosList) {
    for (CmsWorkOrder wo : woList) {
      wosList.add(cmsUtil.custWorkOrder2Simple(wo));
    }
  }

  public List<CmsWorkOrder> getWorkOrderIds(long deploymentId, String state, Integer execOrder,
      Integer limit) {
    List<CmsWorkOrder> workOrders = (limit != null && limit > 0) ? dpmtMapper
        .getWorkOrdersLimited(deploymentId, state, execOrder, limit)
        : dpmtMapper.getWorkOrders(deploymentId, state, execOrder);
    return workOrders;
  }

  public CmsWorkOrderSimple getWorkOrderSimple(long dpmtRecordId, String state, Integer execOrder) {
    checkControllerCache();
    CmsWorkOrder wo = getWorkOrder(dpmtRecordId, state, execOrder);
    CmsWorkOrderSimple returnObj = null;
    if (wo != null) {
      return cmsUtil.custWorkOrder2Simple(wo);
    } else {
      return null;
    }
  }

  private void checkControllerCache() {
    if (controllerCache != null) {
      controllerCache.invalidateMdCacheIfRequired();
    }
  }

  public CmsWorkOrder getWorkOrder(long dpmtRecordId, String state, Integer execOrder) {

    CmsWorkOrder workOrder = dpmtMapper.getWorkOrder(dpmtRecordId, state, execOrder);

    if (workOrder == null) {
      return null;
    }

    CmsRfcCI rfcNaked = djMapper.getRfcCIById(workOrder.getRfcId());
    CmsRfcCI rfcCimerged = cmrfcProcessor.getCiById(rfcNaked.getCiId(), "df");

    workOrder.setRfcCi(rfcCimerged);
    populateWoBase(workOrder);

    Map<Long, CmsCI> manifestToTemplateMap = new HashMap<>();

    CmsCI env = getEnvAndPopulatePlatEnable(workOrder.getBox());

    Map<String, String> globalVars = cmsUtil.getGlobalVars(env);
    Map<String, String> cloudVars = cmsUtil.getCloudVars(workOrder.getCloud());
    Map<String, String> localVars = cmsUtil.getLocalVars(workOrder.getBox());

    workOrder.putPayLoadEntry(CmsUtil.CLOUD_VARS_PAYLOAD_NAME,
        cmsUtil.getCloudVarsRfcs(workOrder.getCloud()));
    workOrder.putPayLoadEntry(CmsUtil.GLOBAL_VARS_PAYLOAD_NAME, cmsUtil.getGlobalVarsRfcs(env));
    workOrder.putPayLoadEntry(CmsUtil.LOCAL_VARS_PAYLOAD_NAME,
        cmsUtil.getLocalVarsRfcs(workOrder.getBox()));

    //basic staff
    //put realized as
    workOrder.putPayLoadEntry("RealizedAs",
        getRfcCIRelatives(workOrder.getRfcCi(), "base.RealizedAs", "to", null, "df"));

    //put env
    List<CmsRfcCI> envs = getRfcCIRelatives(workOrder.getBox().getCiId(), "manifest.ComposedOf",
        "to", null, "df");
    workOrder.putPayLoadEntry("Environment", envs);

    //put assembly
    List<CmsRfcCI> assemblys = getRfcCIRelatives(workOrder.getPayLoad().get("Environment").get(0),
        "base.RealizedIn", "to", null, "df");
    workOrder.putPayLoadEntry("Assembly", assemblys);

    //put Organization
    List<CmsRfcCI> orgs = getRfcCIRelatives(workOrder.getPayLoad().get("Assembly").get(0),
        "base.Manages", "to", null, "df");
    workOrder.putPayLoadEntry("Organization", orgs);

    //put watchedBy and loggedBy
    if (workOrder.getPayLoad().get("RealizedAs").size() > 0) {
      workOrder.putPayLoadEntry("WatchedBy",
          getWatchedByBy(workOrder.getPayLoad().get("RealizedAs").get(0), cloudVars, globalVars,
              localVars));
      workOrder.putPayLoadEntry("LoggedBy",
          getLoggedBy(workOrder.getPayLoad().get("RealizedAs").get(0)));
      workOrder.putPayLoadEntry("EscortedBy",
          getEscortedBy(workOrder.getPayLoad().get("RealizedAs").get(0),
              workOrder.getRfcCi().getRfcAction(), cloudVars, globalVars, localVars));
    }

    // now lets process the custom payloads and this will override the default ones as well

    processCustomPayloads(workOrder, manifestToTemplateMap, env, globalVars, localVars, cloudVars);
    //from here all payloads are default ones unless overriden by the custom payload definitions
    //put depends on
    if (!workOrder.getPayLoad().containsKey(DEPENDS_ON)) {
      workOrder.putPayLoadEntry(DEPENDS_ON,
          getRfcCIRelatives(workOrder.getRfcCi(), "bom.DependsOn", "from", null, "df"));
    }

    //put Entrypoint
    if (!workOrder.getPayLoad().containsKey(ENTRYPOINT)) {
      workOrder.putPayLoadEntry(ENTRYPOINT,
          getRfcCIRelatives(workOrder.getRfcCi(), "base.Entrypoint", "to", null, "df"));
    }
    //put mgmt key pairs
    if (!workOrder.getPayLoad().containsKey(SECURED_BY)) {
      workOrder.putPayLoadEntry(SECURED_BY,
          getKeyPairsRfc(workOrder.getRfcCi(), workOrder.getPayLoad().get("ManagedVia")));
    }
    //put serviecedBy
    if (!workOrder.getPayLoad().containsKey(SERVICED_BY)) {
      workOrder.putPayLoadEntry(SERVICED_BY, getServicedBy(workOrder.getRfcCi()));
    }
    //put RequiresComputes
    if (isClassEligibleForRequiresComputes(workOrder.getRfcCi().getCiClassName()) && !workOrder.getPayLoad().containsKey(REQUIRES_COMPUTES_PAYLOAD_NAME)) {
      workOrder.putPayLoadEntry(REQUIRES_COMPUTES_PAYLOAD_NAME,
          getRequiresComputes(workOrder.getRfcCi()));
    }

    //add the managed-via ci's compute cloud service and then read env_vars of that cloud-service. set those env_vars to wo.config
    setEnvVars(workOrder, workOrder.getPayLoad().get(MANAGED_VIA));

    //fetch and update offerings
    List<CmsRfcCI> offerings = new ArrayList<>();
    try {
      if (!"delete".equals(workOrder.getRfcCi().getRfcAction())) {
        offerings = getRequiredOfferings(workOrder);
      }
    } catch (Exception e) {
      logger.error("Error in fetching offerings", e);
    }

    workOrder.putPayLoadEntry(OFFERING, offerings);

    //add matching compliance objects
    workOrder.putPayLoadEntry(EXTRA_RUNLIST_PAYLOAD_NAME, getMatchingCloudCompliance(workOrder));

    addVarsForConfig(workOrder);
    addComponentConfig(workOrder);
    return workOrder;
  }

  private boolean isTorbitService(CmsRfcCI rfc) {
    CmsRfcAttribute serviceAttr = rfc.getAttribute(SERVICE_TYPE_ATTR);
    return (serviceAttr != null) && GSLB_TYPE_TORBIT.equals(serviceAttr.getNewValue());
  }

  void addComponentConfig(CmsWorkOrder wo) {
    CmsRfcCI rfc = wo.getRfcCi();
    if (rfc.getCiClassName().matches(BOM_FQDN) && isTorbitService(rfc)) {
      String logKey = "Fqdn workorder " + wo.getDeploymentId() + ":" + rfc.getCiId() + ":: ";
      //send weights only when enabled
      if (isWeightsEnabled(wo.getRfcCi().getNsPath())) {
        logger.info(logKey + "finding weights for clouds");
        List<CmsCIRelation> deployedToRelations = cmProcessor.getCIRelationsWithToCIAndNoAttrs(rfc.getNsPath(),
            DEPLOYED_TO,
            null,
            "Compute",
            null
        );
        Set<String> clouds = getPrimaryClouds(wo);
        logger.info(logKey + "primary clouds " + clouds);
        Map<String, Long> cloudInstancesMap = deployedToRelations.stream()
            .filter(r -> (r.getToCi() != null) && clouds.contains(r.getToCi().getCiName()))
            .collect(groupingBy(r -> r.getToCi().getCiName(), counting()));

        if (cloudInstancesMap.isEmpty()) {
          logger.info(logKey + "no active cloud compute instance found");
          return;
        }

        Long totalComputes = 0L;
        List<CloudWeight> cloudWeights = new ArrayList<>();
        for (Entry<String, Long> entry : cloudInstancesMap.entrySet()) {
          totalComputes =  totalComputes + entry.getValue();
          cloudWeights.add(new CloudWeight(entry.getKey(), entry.getValue()));
        }
        logger.info(logKey + "total active computes " + totalComputes);

        cloudWeights.sort((c1,c2) -> Long.compare(c2.computes, c1.computes));

        if (totalComputes > 0) {
          int totalPercent = 0;
          for (CloudWeight cloudWeight: cloudWeights) {
            cloudWeight.weight = (int) ((cloudWeight.computes * 100.0) / totalComputes);
            totalPercent += cloudWeight.weight;
          }
          //bring the total percent to 100
          int remainingPercent = 100 - totalPercent;
          for (int i = 0 ; i < remainingPercent ; i++) {
            cloudWeights.get(i).weight++;
          }

          Map<String, String> config = configMap(wo);
          Map<String, Integer> weights = cloudWeights.stream()
              .collect(Collectors.toMap(c -> c.name, c -> c.weight));
          logger.info(logKey + "weight map " + weights);
          config.put("weights", gson.toJson(weights));
        }
      }
      else {
        logger.info(logKey + "not using weights for gslb. gslb weights not enabled.");
      }
    }
  }

  private boolean isWeightsEnabled(String nsPath) {
    return isGslbWeightsEnabled &&
        cmProcessor.getVarByMatchingCriteriaBoolean(GSLB_WEIGHTS_ENABLED_VAR, nsPath);
  }

  private Map<String, String> configMap(CmsWorkOrder wo) {
    Map<String, String> configMap = wo.getConfig();
    if (configMap == null) {
      wo.setConfig(new HashMap<>());
    }
    return wo.getConfig();
  }

  private Set<String> getPrimaryClouds(CmsWorkOrder wo) {
    Set<String> clouds = null;
    List<CmsRfcCI> fqdnclouds = wo.getPayLoad().get(PAYLOAD_FQDN_CLOUDS);
    if (fqdnclouds != null) {
      clouds = fqdnclouds.stream()
          .filter(f -> "1".equals(rfcAttribute(f, CONSUMES_ATTRIBUTE)))
          .map(CmsRfcCI::getCiName)
          .collect(toSet());
    }
    if (clouds == null) {
      clouds = Collections.emptySet();
    }
    return clouds;
  }

  private String rfcAttribute(CmsRfcCI rfc, String attributeKey) {
    CmsRfcAttribute attr = rfc.getAttribute(attributeKey);
    String value = null;
    if (attr != null) {
      value = attr.getNewValue();
    }
    return value;
  }

  protected void processCustomPayloads(CmsWorkOrder workOrder,
      Map<Long, CmsCI> manifestToTemplateMap, CmsCI env, Map<String, String> globalVars,
      Map<String, String> localVars, Map<String, String> cloudVars) {
    //lets get the payload def from the template
    long manifestCiId = workOrder.getPayLoad().get("RealizedAs").get(0).getCiId();
    if (!manifestToTemplateMap.containsKey(manifestCiId)) {
      CmsCI manifestCi = cmProcessor.getCiById(manifestCiId);
      CmsCI templObj = cmProcessor.getTemplateObjForManifestObj(manifestCi, env);
      if (templObj == null) {
        logger.error(
            "Can not find manifest template object for manifest ci id = " + manifestCi.getCiId()
                + " ciName" + manifestCi.getCiName());
      } else {
        manifestToTemplateMap.put(manifestCi.getCiId(), templObj);
      }
    }

    List<CmsRfcCI> managedVia = getRfcCIRelatives(workOrder.getRfcCi(), "bom.ManagedVia", "from",
        null, "df");

    if (!manifestToTemplateMap.containsKey(manifestCiId)) {
      //throw new DJException(CmsError.CMS_CANT_FIGURE_OUT_TEMPLATE_FOR_MANIFEST_ERROR,
      //Don't throw an exception in case no pack component could be found.
      //If this is the case - pack component was removed and this should be a delete WO
      logger.warn(
          "Can not find pack template for manifest component id=" + manifestCiId + "; name - "
              + workOrder.getPayLoad().get("RealizedAs").get(0).getCiName());
    } else {
      CmsCI managedViaTemplateCi = null;
      long managedViaBomCid = 0;
      if (managedVia != null && managedVia.size() > 0) {
        CmsRfcCI managedViaEntity = managedVia.get(0);
        managedViaBomCid = managedViaEntity.getCiId();
        CmsCI managedViaManifestCi = new CmsCI();
        long managedViaManifestCiId = getRealizedAs(managedViaEntity.getCiId());
        managedViaManifestCi.setCiId(managedViaManifestCiId);
        managedViaManifestCi
            .setCiClassName(managedViaEntity.getCiClassName().replace("bom.", "manifest."));

        managedViaTemplateCi = cmProcessor.getTemplateObjForManifestObj(managedViaManifestCi, env);
        if (managedViaTemplateCi == null) {
          logger.warn("template ci not found for ci id: " + managedViaManifestCi.getCiId());
        }
      }
      processPayLoadDef(workOrder, manifestToTemplateMap.get(manifestCiId), managedViaTemplateCi,
          managedViaBomCid, cloudVars, globalVars, localVars);
    }

    //put proxy
    if (!workOrder.getPayLoad().containsKey(MANAGED_VIA)) {
      workOrder.putPayLoadEntry(MANAGED_VIA, managedVia);
    }
  }

  private void setEnvVars(CmsWorkOrder workOrder, List<CmsRfcCI> managedVia) {
    if (managedVia != null && managedVia.size() > 0) {
      long managedViaCiId = managedVia.get(0).getCiId();
      if (managedViaCiId > 0) {
        Map<String, Map<String, CmsCI>> cloudServices = getServices(managedViaCiId,
            workOrder.getCloud());
        Map<String, CmsCI> cloudService = cloudServices.get("compute");
        if (cloudService != null) {
          CmsCI cloudServiceCi = cloudService.get(workOrder.getCloud().getCiName());
          if (cloudServiceCi != null) {
            CmsCIAttribute envVarsAttribute = cloudServiceCi.getAttribute("env_vars");
            if (envVarsAttribute != null) {
              Map<String, String> config = workOrder.getConfig();
              if (config == null) {
                config = new HashMap<>();
              }
              config.put("env_vars", envVarsAttribute.getDfValue());
              workOrder.setConfig(config);
            }
          }
        }
      }
    }
  }

  private void addVarsForConfig(CmsWorkOrder workOrder) {
    Map<String, String> varMap =
        getVarsForConfig(workOrder.getRfcCi().getNsPath(), workOrder.getRfcCi().getCiClassName());
    workOrder.setConfig(varMap);
  }

  Map<String, String> getVarsForConfig(String nsPath, String className) {
    final Map<String, String> varMap = new HashMap<>();
    for (String prefix : new String[] {"bom", cmsUtil.getShortClazzName(className)}) {
      List<CmsVar> vars = cmProcessor.getCmVarByLongestMatchingCriteria(prefix + ".%", nsPath);
      if (vars != null && !vars.isEmpty()) {
        vars.forEach(var -> varMap.put(StringUtils.substringAfter(var.getName(), prefix + "."), var.getValue()));
      }
    }
    return varMap.isEmpty() ? null : varMap;
  }

  private void addVarsForConfig(CmsActionOrder ao) {
    Map<String, String> varMap =
        getVarsForConfig(ao.getCi().getNsPath(), ao.getCi().getCiClassName());
    ao.setConfig(varMap);
  }

  private List<CmsRfcCI> getRequiredOfferings(CmsWorkOrder workOrder) {

    List<CmsRfcCI> reqOfferings = new ArrayList<>();
    if (OFFERING_ENABLED) {
      for (Entry<String, Map<String, CmsCI>> serviceEntry : workOrder.getServices().entrySet()) {
        for (CmsCI serviceCI : serviceEntry.getValue().values()) {
          String offeringNS =
              serviceCI.getNsPath() + "/" + serviceCI.getCiClassName() + "/" + serviceCI
                  .getCiName();
          List<CmsCI> offerings = offeringMatcher
              .getEligbleOfferings(cmsUtil.custRfcCI2RfcCISimple(workOrder.getRfcCi()), offeringNS);
          if (!offerings.isEmpty()) {
            CmsRfcCI offeringRfc = rfcUtil
                .mergeRfcAndCi(null, getLowestCostOffering(offerings), "df");
            CmsRfcAttribute serviceTypeAttr = new CmsRfcAttribute();
            serviceTypeAttr.setAttributeName("service_type");
            serviceTypeAttr.setNewValue(serviceEntry.getKey());
            offeringRfc.addAttribute(serviceTypeAttr);
            reqOfferings.add(offeringRfc);
          }
        }
      }
    }
    return reqOfferings;
  }

  private CmsCI getLowestCostOffering(List<CmsCI> offerings) {
    CmsCI lowestOffering = null;
    for (CmsCI offering : offerings) {
      if (lowestOffering == null) {
        lowestOffering = offering;
      } else if (Double.valueOf(offering.getAttribute("cost_rate").getDfValue()) < Double
          .valueOf(lowestOffering.getAttribute("cost_rate").getDfValue())) {
        lowestOffering = offering;
      }
    }
    return lowestOffering;
  }

  List<CmsRfcCI> getMatchingCloudCompliance(CmsWorkOrder wo) {
    CmsCI platformCi = wo.getBox();
    CmsCIAttribute autoComplyAttr = platformCi.getAttribute(ATTR_NAME_AUTO_COMPLY);
    if (!Boolean.valueOf(autoComplyAttr.getDfValue())) {
      return Collections.emptyList();
    }

    List<CmsCIRelation> complianceRelations = getComplianceRelations(wo);
    List<CmsRfcCI> list = complianceRelations.stream().map(complianceRel -> complianceRel.getToCi())
        .filter(complianceCi -> (isComplianceEnabled(complianceCi)) && expressionEvaluator
            .isExpressionMatching(complianceCi, wo))
        .map(complianceCi -> rfcUtil.mergeRfcAndCi(null, complianceCi, ATTR_VALUE_TYPE_DF))
        .collect(Collectors.toList());
    return list;
  }

  List<CmsCI> getMatchingCloudCompliance(CmsActionOrder ao) {
    List<CmsCIRelation> complianceRelations = getComplianceRelations(ao);
    List<CmsCI> list = complianceRelations.stream().map(complianceRel -> complianceRel.getToCi())
        .filter(complianceCi -> expressionEvaluator.isExpressionMatching(complianceCi, ao))
        .collect(Collectors.toList());

    return list;
  }

  private List<CmsCIRelation> getComplianceRelations(CmsWorkOrderBase wo) {
    List<CmsCIRelation> relations = cmProcessor
        .getFromCIRelations(wo.getCloud().getCiId(), BASE_COMPLIES_WITH, null);
    return relations;
  }

  private boolean isComplianceEnabled(CmsCI compliance) {
    CmsCIAttribute attribute = compliance.getAttribute(ATTR_NAME_ENABLED);
    return ((attribute != null) && Boolean.valueOf(attribute.getDjValue()));
  }

  private void processPayLoadDef(CmsWorkOrderBase wo, CmsCI templateCi, CmsCI managedViaTemplateCi,
      long managedViaBomCid, Map<String, String> cloudVars, Map<String, String> globalVars,
      Map<String, String> localVars) {

    List<CmsCIRelation> payloadRels = cmProcessor
        .getFromCIRelations(templateCi.getCiId(), "mgmt.manifest.Payload", "mgmt.manifest.Qpath");
    long anchorId = 0;
    if (wo instanceof CmsWorkOrder) {
      anchorId = ((CmsWorkOrder) wo).getRfcCi().getCiId();
    } else if (wo instanceof CmsActionOrder) {
      anchorId = ((CmsActionOrder) wo).getCiId();
    }
    populatePayloads(wo, payloadRels, anchorId, cloudVars, globalVars, localVars, true);

    if (managedViaTemplateCi != null) {
      List<CmsCIRelation> managedViasPayloadRels = cmProcessor
          .getFromCIRelations(managedViaTemplateCi.getCiId(), "mgmt.manifest.Payload",
              "mgmt.manifest.Qpath");
      populatePayloads(wo, managedViasPayloadRels, managedViaBomCid, cloudVars, globalVars,
          localVars, false);
    } else {
      logger.info("managedViaTemplateCi null for : " + templateCi.getCiClassName());
    }
  }

  private void populatePayloads(CmsWorkOrderBase wo, List<CmsCIRelation> payloadRels, long anchorId,
      Map<String, String> cloudVars, Map<String, String> globalVars, Map<String, String> localVars,
      boolean overwrite) {
    logger.info("total # of payloads: " + payloadRels.size());
    for (CmsCIRelation payloadRel : payloadRels) {
      String payloadName = payloadRel.getToCi().getCiName();
      String payloadDefinition = payloadRel.getToCi().getAttribute("definition").getDfValue();
      if (wo instanceof CmsWorkOrder) {
        CmsWorkOrder workOrder = (CmsWorkOrder) wo;
        List<CmsRfcCI> payloadValue = processPayLoadQPath(anchorId, payloadDefinition, cloudVars,
            globalVars, localVars);
        Map<String, List<CmsRfcCI>> payload = workOrder.getPayLoad();
        if (overwrite || payload == null || payload.get(payloadName) == null) {
          ((CmsWorkOrder) wo).putPayLoadEntry(payloadName, payloadValue);
        }
      } else if (wo instanceof CmsActionOrder) {
        CmsActionOrder actionOrder = (CmsActionOrder) wo;
        List<CmsCI> payloadValue = processPayLoadQPath(anchorId, payloadDefinition);
        Map<String, List<CmsCI>> payload = actionOrder.getPayLoad();

        if (overwrite || payload == null || payload.get(payloadName) == null) {
          actionOrder.putPayLoadEntry(payloadName, payloadValue);
        }
      }
    }
  }

  private List<CmsRfcCI> processPayLoadQPath(long anchorId, String qPath,
      Map<String, String> cloudVars, Map<String, String> globalVars,
      Map<String, String> localVars) {
    if (qPath == null) {
      return null;
    }
    CollectionLinkDefinition payloadDef = gson.fromJson(qPath, CollectionLinkDefinition.class);
    List<CmsRfcCI> payload = colProcessor.getFlatCollectionRfc(anchorId, payloadDef);
    for (CmsRfcCI cmsRfcCI : payload) {
      cmsUtil.processAllVars(cmsRfcCI, cloudVars, globalVars, localVars);
    }
    return payload;
  }

  private List<CmsCI> processPayLoadQPath(long anchorId, String qPath) {
    if (qPath == null) {
      return null;
    }
    CollectionLinkDefinition payloadDef = gson.fromJson(qPath, CollectionLinkDefinition.class);
    List<CmsCI> payload = colProcessor.getFlatCollection(anchorId, payloadDef);
    return payload;
  }

  private void processPayLoadDef(CmsWorkOrderBase wo, String payloadDefStr) {
    if (payloadDefStr == null) {
      return;
    }
    Map<String, CollectionLinkDefinition> payloadDef = gson
        .fromJson(payloadDefStr, new TypeToken<HashMap<String, CollectionLinkDefinition>>() {
        }.getType());
    for (String key : payloadDef.keySet()) {
      if (wo instanceof CmsWorkOrder) {
        List<CmsRfcCI> payload = colProcessor
            .getFlatCollectionRfc(((CmsWorkOrder) wo).getRfcCi().getCiId(), payloadDef.get(key));
        ((CmsWorkOrder) wo).putPayLoadEntry(key, payload);
      } else if (wo instanceof CmsActionOrder) {
        List<CmsCI> payload = colProcessor
            .getFlatCollection(((CmsActionOrder) wo).getCiId(), payloadDef.get(key));
        ((CmsActionOrder) wo).putPayLoadEntry(key, payload);
      }
    }
  }


  private void populateWoBase(List<? extends CmsWorkOrderBase> wos) {
    for (CmsWorkOrderBase wo : wos) {
      populateWoBase(wo);
    }
  }

  private void populateWoBase(CmsWorkOrderBase wo) {
    long anchorCiId = 0;
    String targetClassName = null;
    if (wo instanceof CmsWorkOrder) {
      anchorCiId = ((CmsWorkOrder) wo).getRfcCi().getCiId();
      targetClassName = ((CmsWorkOrder) wo).getRfcCi().getCiClassName();
    } else if (wo instanceof CmsActionOrder) {
      anchorCiId = ((CmsActionOrder) wo).getCiId();
      targetClassName = ((CmsActionOrder) wo).getCi().getCiClassName();
    } else {
      throw new CmsException(CmsError.CMS_BAD_WO_CLASS_ERROR, "Bad wo class");
    }

    if (targetClassName != null && targetClassName.startsWith(CLOUDSERVICEPREFIX)) {
      wo.setCloud(getCloudForCloudService(anchorCiId));
    } else {
      wo.setBox(getBox(anchorCiId));
      wo.setCloud(getCloud(anchorCiId, wo.getBox()));
      wo.setServices(getServices(anchorCiId, wo.getCloud()));
    }
  }


  private Map<String, Map<String, CmsCI>> getServices(long ciId, CmsCI cloud) {

    Map<String, Map<String, CmsCI>> services = new HashMap<>();
    List<CmsRfcRelation> realizedAsRels = cmrfcProcessor
        .getToCIRelationsNaked(ciId, "base.RealizedAs", null, null);
    List<CmsCI> zones = cmProcessor.getCiBy3NsLike(getCloudNsPath(cloud), ZONE_CLASS, null);

    if (realizedAsRels.size() > 0) {
      CmsRfcRelation realizedRel = realizedAsRels.get(0);
      List<CmsCIRelation> requiresList = cmProcessor
          .getToCIRelationsNaked(realizedRel.getFromCiId(), "manifest.Requires", null);
      if (requiresList.size() > 0) {
        CmsCIRelation requiresRel = requiresList.get(0);
        CmsCIRelationAttribute servicesAttr = requiresRel.getAttribute("services");

        if (servicesAttr != null && servicesAttr.getDjValue() != null
            && servicesAttr.getDjValue().length() > 0) {
          String[] requiredServices = servicesAttr.getDjValue().split(",");
          for (String requredServiceFull : requiredServices) {
            String requredService = null;
            if (requredServiceFull.startsWith("*")) { //optional service
              requredService = requredServiceFull.replace("*", "");
            } else {
              requredService = requredServiceFull;
            }
            List<AttrQueryCondition> attrsQuery = new ArrayList<>();
            AttrQueryCondition attrCondition = new AttrQueryCondition();
            attrCondition.setAttributeName("service");
            attrCondition.setAvalue(requredService);
            attrCondition.setCondition("eq");
            attrsQuery.add(attrCondition);

            //get cloud level service
            List<CmsCIRelation> cloudServiceRels = getServiceRelations(cloud, attrsQuery);
            addToServices(services, requredService, cloud.getCiName(), cloudServiceRels);

            //get zone level service
            for (CmsCI zone : zones) {
              List<CmsCIRelation> zoneServiceRels = getServiceRelations(zone, attrsQuery);
              addToServices(services, requredService, cloud.getCiName() + "/" + zone.getCiName(),
                  zoneServiceRels);
            }
          }
        }
      } else {
        throw new CmsException(CmsError.CMS_CANT_FIND_REQUIRES_FOR_CI_ERROR,
            "can't find Requires for manifest ci with ciId=" + realizedRel.getFromCiId());
      }
    } else {
      throw new CmsException(CmsError.CMS_CANT_FIND_REALIZEDAS_FOR_BOMC_ERROR,
          "can't find realaziedAs for with ciId=" + ciId);
    }

    return services;
  }

  private String getCloudNsPath(CmsCI cloud) {
    return cloud.getNsPath() + "/" + cloud.getCiName();
  }

  private List<CmsCIRelation> getServiceRelations(CmsCI ci, List<AttrQueryCondition> attrsQuery) {
    List<CmsCIRelation> serviceRels = cmProcessor
        .getFromCIRelationsByAttrs(ci.getCiId(), BASE_PROVIDES, null, null, attrsQuery);
    return serviceRels;
  }

  private void addToServices(Map<String, Map<String, CmsCI>> services, String requredService,
      String ciName, List<CmsCIRelation> serviceRels) {
    if (serviceRels.size() > 0) {
      CmsCI serviceCi = serviceRels.get(0).getToCi();
      if (!services.containsKey(requredService)) {
        services.put(requredService, new LinkedHashMap<>());
      }
      services.get(requredService).put(ciName, serviceCi);
    }
  }

  private CmsCI getBox(long ciId) {

    CmsCI box = null;
    List<CmsRfcRelation> realizedAsRels = cmrfcProcessor
        .getToCIRelationsNakedNoAttrs(ciId, "base.RealizedAs", null, null);
    if (realizedAsRels.size() > 0) {
      List<CmsCIRelation> boxList = cmProcessor
          .getToCIRelations(realizedAsRels.get(0).getFromCiId(), "manifest.Requires", null);
      if (boxList.size() > 0) {
        box = boxList.get(0).getFromCi();
      }
    }

    return box;
  }

  private List<CmsRfcCI> getRequiresComputes(CmsRfcCI rfc) {

    List<CmsRfcCI> computes = new ArrayList<>();
    CmsCI platform = getBox(rfc.getCiId());

    List<CmsCIRelation> manifestComputeList = cmProcessor
        .getFromCIRelationsNakedNoAttrs(platform.getCiId(), "manifest.Requires", null, "Compute");

    for (CmsCIRelation rel : manifestComputeList) {
      List<CmsRfcRelation> bomComputeRels = cmrfcProcessor
          .getFromCIRelations(rel.getToCiId(), "base.RealizedAs", null, "df");
      for (CmsRfcRelation realized : bomComputeRels) {
        computes.add(realized.getToRfcCi());
      }
    }
    return computes;
  }


  private List<CmsRfcCI> getServicedBy(CmsRfcCI rfc) {

    List<CmsRfcCI> iaases = new ArrayList<>();

    CmsCI box = getBox(rfc.getCiId());

    List<CmsCIRelation> iaasList = cmProcessor
        .getFromCIRelations(box.getCiId(), "manifest.ServicedBy", "manifest.Iaas");
    for (CmsCIRelation rel : iaasList) {
      CmsRfcCI iaas = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
      iaas.getAttribute("services").setNewValue(rel.getAttribute("services").getDjValue());
      List<CmsCIRelation> keypairs = cmProcessor
          .getFromCIRelations(iaas.getCiId(), "manifest.Requires", "manifest.Keypair");
      if (keypairs.size() > 0) {
        CmsRfcAttribute prKeyAttr = new CmsRfcAttribute();
        prKeyAttr.setAttributeName("private_key");
        prKeyAttr.setNewValue(keypairs.get(0).getToCi().getAttribute("private").getDjValue());
        iaas.addAttribute(prKeyAttr);
      }
      //this is total HACK for Netscaler needs to be generalized
      List<CmsCIRelation> netscaler = cmProcessor
          .getFromCIRelations(iaas.getCiId(), "manifest.Requires", "manifest.Netscaler");
      if (netscaler.size() > 0) {
        for (Entry<String, CmsCIAttribute> attrEntry : netscaler.get(0).getToCi().getAttributes()
            .entrySet()) {
          CmsCIAttribute nsAttr = attrEntry.getValue();
          CmsRfcAttribute iaasNsAttr = new CmsRfcAttribute();
          iaasNsAttr.setAttributeName(nsAttr.getAttributeName());
          iaasNsAttr.setNewValue(nsAttr.getDjValue());
          iaas.addAttribute(iaasNsAttr);
        }
      }
      iaases.add(iaas);
    }
    return iaases;
  }

  private List<CmsRfcCI> getWatchedByBy(CmsRfcCI realizedAs, Map<String, String> cloudVars,
      Map<String, String> globalVars, Map<String, String> localVars) {

    List<CmsRfcCI> monitors = new ArrayList<>();

    List<CmsCIRelation> monitorList = cmProcessor
        .getFromCIRelations(realizedAs.getCiId(), "manifest.WatchedBy", "manifest.Monitor");
    for (CmsCIRelation rel : monitorList) {
      cmsUtil.processAllVars(rel.getToCi(), cloudVars, globalVars, localVars);
      CmsRfcCI monitor = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
      if (!CI_STATE_PENDING_DELETION.equals(monitor.getCiState())) {
        monitors.add(monitor);
      }
    }
    return monitors;
  }

  private List<CmsRfcCI> getLoggedBy(CmsRfcCI realizedAs) {

    List<CmsRfcCI> logs = new ArrayList<>();

    List<CmsCIRelation> logList = cmProcessor
        .getFromCIRelations(realizedAs.getCiId(), "manifest.LoggedBy", "manifest.Log");
    for (CmsCIRelation rel : logList) {
      CmsRfcCI log = rfcUtil.mergeRfcAndCi(null, rel.getToCi(), "dj");
      logs.add(log);
    }
    return logs;
  }


  private List<CmsRfcCI> getKeyPairsRfc(CmsRfcCI rfc, List<CmsRfcCI> managedVia) {

    List<CmsRfcCI> keys = new ArrayList<>();
    List<CmsRfcRelation> secRels = null;
    if (managedVia != null && managedVia.size() > 0) {
      secRels = cmrfcProcessor
          .getFromCIRelations(managedVia.get(0).getCiId(), "bom.SecuredBy", null, "df");
    } else {
      secRels = cmrfcProcessor.getFromCIRelations(rfc.getCiId(), "bom.SecuredBy", null, "df");
    }
    for (CmsRfcRelation rel : secRels) {
      keys.add(rel.getToRfcCi());
    }
    return keys;
  }

  private List<CmsCI> getKeyPairs(CmsCI ci, List<CmsCI> managedVia) {
    List<CmsCI> keys = new ArrayList<>();
    List<CmsCIRelation> secRels = null;

    if (managedVia != null && managedVia.size() > 0) {
      secRels = cmProcessor
          .getFromCIRelations(managedVia.get(0).getCiId(), "bom.SecuredBy", null, null);
    } else {
      secRels = cmProcessor.getFromCIRelations(ci.getCiId(), "bom.SecuredBy", null, null);
    }
    for (CmsCIRelation rel : secRels) {
      keys.add(rel.getToCi());
    }
    return keys;
  }

  private CmsCI getEnvAndPopulatePlatEnable(CmsCI box) {
    if (box != null) {
      List<CmsCIRelation> envRels = cmProcessor
          .getToCIRelations(box.getCiId(), "manifest.ComposedOf", "manifest.Environment");
      if (envRels.size() > 0) {
        CmsCIRelation composedOf = envRels.get(0);
        if (composedOf.getAttribute(IS_PLATFORM_ENABLED_REL_ATTR) != null) {
          CmsCIAttribute platEnabledAttr = new CmsCIAttribute();
          platEnabledAttr.setAttributeName(IS_PLATFORM_ENABLED_ATTR);
          platEnabledAttr
              .setDfValue(composedOf.getAttribute(IS_PLATFORM_ENABLED_REL_ATTR).getDfValue());
          platEnabledAttr
              .setDjValue(composedOf.getAttribute(IS_PLATFORM_ENABLED_REL_ATTR).getDfValue());
          box.addAttribute(platEnabledAttr);
        }
        return composedOf.getFromCi();
      }
    }
    return null;
  }

  private List<CmsRfcCI> getRfcCIRelatives(CmsRfcCI rfc, String relName, String direction,
      String className, String attrValue) {
    return getRfcCIRelatives(rfc.getCiId(), relName, direction, className, attrValue);
  }

  List<CmsRfcCI> getRfcCIRelatives(long ciId, String relName, String direction, String className,
      String attrValue) {
    List<CmsRfcCI> relatives = new ArrayList<>();
    if ("from".equalsIgnoreCase(direction)) {
      List<CmsRfcRelation> rels = cmrfcProcessor
          .getFromCIRelations(ciId, relName, className, attrValue);
      for (CmsRfcRelation rel : rels) {
        relatives.add(rel.getToRfcCi());
      }
    } else {
      List<CmsRfcRelation> rels = cmrfcProcessor
          .getToCIRelations(ciId, relName, className, attrValue);
      for (CmsRfcRelation rel : rels) {
        relatives.add(rel.getFromRfcCi());
      }
    }
    return relatives;
  }


  private List<CmsCI> getCIRelatives(long ciId, String relName, String direction,
      String className) {
    List<CmsCI> relatives = new ArrayList<>();
    if ("from".equalsIgnoreCase(direction)) {
      List<CmsCIRelation> rels = cmProcessor.getFromCIRelations(ciId, relName, null, className);
      for (CmsCIRelation rel : rels) {
        relatives.add(rel.getToCi());
      }
    } else {
      List<CmsCIRelation> rels = cmProcessor.getToCIRelations(ciId, relName, null, className);
      for (CmsCIRelation rel : rels) {
        relatives.add(rel.getFromCi());
      }
    }
    return relatives;
  }


  private CmsCI getCloud(long ciId, CmsCI box) {
    List<CmsRfcRelation> cloudRels = cmrfcProcessor
        .getFromCIRelationsNakedNoAttrs(ciId, "base.DeployedTo", null, "account.Cloud");
    //Add other attributes as CI attributes for work order, viz deploymentOrder, percentage

    if (cloudRels.size() > 0) {
      CmsCI cloud = cmProcessor.getCiById(cloudRels.get(0).getToCiId());
      //Get Clouds from
      List<CmsCIRelation> platformCloudRels = cmProcessor
          .getFromCIRelationsByToCiIds(box.getCiId(), "base.Consumes", null,
              Collections.singletonList(cloud.getCiId()));
      List<CmsRfcRelation> realizedAsRels = cmrfcProcessor
          .getToCIRelationsNaked(ciId, "base.RealizedAs", null, null);
      if (realizedAsRels.size() > 0 && realizedAsRels.get(0).getAttribute("priority") != null) {
        String priority = realizedAsRels.get(0).getAttribute("priority").getNewValue();
        CmsCIAttribute prAttr = new CmsCIAttribute();
        prAttr.setAttributeName("priority");
        prAttr.setDfValue(priority);
        prAttr.setDjValue(priority);
        cloud.addAttribute(prAttr);
      }

      if (platformCloudRels.size() > 0) {
        final Map<String, CmsCIRelationAttribute> consumeRelationAttributes = platformCloudRels
            .get(0).getAttributes();
        consumeRelationAttributes.keySet().stream().filter(k -> !"priority".equals(k))
            .forEach(k -> {
              CmsCIAttribute relAttribute = new CmsCIAttribute();
              relAttribute.setAttributeName(k);
              relAttribute.setDfValue(consumeRelationAttributes.get(k).getDfValue());
              relAttribute.setDjValue(consumeRelationAttributes.get(k).getDjValue());
              cloud.addAttribute(relAttribute);
            });

      }
      return cloud;
    }
    return null;
  }

  private CmsCI getCloudForCloudService(long ciId) {
    List<CmsRfcRelation> cloudRels = cmrfcProcessor
        .getToCIRelationsNakedNoAttrs(ciId, "base.Provides", null, "account.Cloud");
    if (cloudRels.size() > 0) {
      CmsCI cloud = cmProcessor.getCiById(cloudRels.get(0).getFromCiId());
      return cloud;
    }
    return null;
  }

  private long getRealizedAs(long bomCiId) {
    List<CmsCIRelation> manifestList = cmProcessor
        .getToCIRelationsNakedNoAttrs(bomCiId, "base.RealizedAs", null, null);
    if (manifestList.size() > 0) {
      return manifestList.get(0).getFromCiId();
    } else {
      throw new CmsException(CmsError.CMS_CANT_FIND_REALIZEDAS_FOR_BOMC_ERROR,
          "Can not find manifest ciID for bomc ciId - " + bomCiId);
    }
  }

  public boolean isClassEligibleForRequiresComputes(String inClass){
    if(!requiresComputesCheckEnabled){
      return true;
    }
    CmsVar var = cmProcessor.getCmSimpleVar(REQUIRES_COMPUTES);
    if(inClass != null && var != null && var.getValue() != null){
      String[] classNames = var.getValue().split(",");
      for(String sT: classNames){
        if(sT.trim().equals(inClass)){
          return true;
        }
      }
    }
    return false;
  }

  public OfferingsMatcher getOfferingMatcher() {
    return offeringMatcher;
  }

  public void setOfferingMatcher(OfferingsMatcher offeringMatcher) {
    this.offeringMatcher = offeringMatcher;
  }

  public void setExpressionEvaluator(ExpressionEvaluator expressionEvaluator) {
    this.expressionEvaluator = expressionEvaluator;
  }

  public void setControllerCache(ControllerCache controllerCache) {
    this.controllerCache = controllerCache;
  }

  public void setGslbWeightsEnabled(boolean gslbWeightsEnabled) {
    isGslbWeightsEnabled = gslbWeightsEnabled;
  }

  public void setRequiresComputesCheckEnabled(boolean requiresComputesCheckEnabled) {
    this.requiresComputesCheckEnabled = requiresComputesCheckEnabled;
  }

  class CloudWeight {
    String name;
    Long computes;
    int weight;

    CloudWeight(String name, Long computes) {
      this.name = name;
      this.computes = computes;
    }

  }

}
