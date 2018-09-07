package com.oneops.cms.ws.rest;

import static com.oneops.cms.cm.ops.domain.OpsProcedureState.active;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.complete;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.pending;
import static com.oneops.cms.util.CmsConstants.GSLB_TYPE_NETSCALER;
import static com.oneops.cms.util.CmsConstants.GSLB_TYPE_TORBIT;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.PlatformFqdn;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureSimple;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.exceptions.CmsException;
import com.oneops.cms.execution.MigrateArg;
import com.oneops.cms.util.CmsError;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class GslbRestController extends AbstractRestController {

  private static final String PACK_SOURCE_ATTRIBUTE = "source";
  private static final String PACK_NAME_ATTRIBUTE = "pack";
  private static final String PACK_VERSION_ATTRIBUTE = "version";
  private static final String PLATFORM_AVAILABILITY_ATTRIBUTE = "availability";

  private static final String FQDN_SHORT_CLASS = "Fqdn";
  private static final String PROCEDURE_DEFN_CLASS = "mgmt.manifest.Procedure";
  private static final String GSLB_MIGRATION_PROC = "gslb-migration";
  private static final String MANIFEST_PLATFORM_CLASS = "manifest.Platform";

  private static final String SERVICE_TYPE_ATTR = "service_type";

  @Autowired OpsManager opsManager;

  @Autowired CmsCmManager cmsCmManager;

  private Gson gson = new Gson();

  @RequestMapping(value = "/gslb/migrate", method = RequestMethod.POST)
  @ResponseBody
  public List<OpsProcedureSimple> gslbMigrate(
      @RequestParam String nsPath,
      @RequestParam String type,
      @RequestParam(required = false) boolean force,
      @RequestParam(required = false) boolean continueOnFailure,
      @RequestParam(defaultValue = "true") boolean waitForCompletion,
      @RequestParam(required = false) boolean excludeCloudVip)
      throws Exception {
    type = type(type);
    List<OpsProcedureSimple> procedureList = new ArrayList<>();
    List<PlatformFqdn> platformNsPaths =
        platformFqdnsThatCanBeMigrated(nsPath, type, force, excludeCloudVip);
    Map<String, CmsCI> procedureCiMap = new HashMap<>();
    for (PlatformFqdn platformFqdn : platformNsPaths) {
      String bomNsPath = platformFqdn.getNsPath();
      CmsCI manifestPlatformCi = getManifestPlatformCi(bomNsPath);

      if (manifestPlatformCi != null) {
        String packNsPath = getPackNsPath(manifestPlatformCi);
        logger.info(
            "gslb migrate :: pack nsPath :" + packNsPath + " for bom nsPath : " + bomNsPath);
        if (StringUtils.isNotBlank(packNsPath)) {
          CmsCI procedureCi =
              procedureCiMap.computeIfAbsent(packNsPath, this::getMigrationProcedure);
          if (procedureCi != null) {
            // submit gslb-migration procedure for this nsPath
            CmsOpsProcedure procedure =
                submitProcedure(bomNsPath, type, procedureCi, manifestPlatformCi);
            if (waitForCompletion) {
              CmsOpsProcedure updatedProc = waitForCompletion(procedure.getProcedureId());
              if (updatedProc == null) updatedProc = procedure;
              procedureList.add(procedure(updatedProc, bomNsPath));
              // continue only if the last procedure was successful
              if (!isSuccessful(updatedProc)) {
                logger.info(
                    "gslb migrate :: procedure [nsPath: "
                        + bomNsPath
                        + ", id: "
                        + procedure.getProcedureId()
                        + "] not successful. continue ? "
                        + continueOnFailure);
                if (!continueOnFailure) break;
              }
            } else {
              procedureList.add(procedure(procedure, bomNsPath));
            }
          } else {
            logger.info("gslb migration procedure not available for this pack ns : " + packNsPath);
          }
        }
      }
    }
    return procedureList;
  }

  private CmsOpsProcedure submitProcedure(
      String bomNsPath, String type, CmsCI procedureCi, CmsCI manifestPlatformCi) {
    // if a procedure is already 'active' with the same type then return that
    List<CmsOpsProcedure> procs =
        opsManager.getCmsOpsProcedureForCi(
            manifestPlatformCi.getCiId(),
            Collections.singletonList(OpsProcedureState.active),
            GSLB_MIGRATION_PROC,
            1);
    if (procs != null && !procs.isEmpty()) {
      String argList = procs.get(0).getArglist();
      if (StringUtils.isNotBlank(argList) && argList.contains(type)) {
        return procs.get(0);
      }
    }
    CmsOpsProcedure procRequest = new CmsOpsProcedure();
    procRequest.setCiId(manifestPlatformCi.getCiId());
    procRequest.setCreatedBy("oneops-system");
    procRequest.setProcedureState(OpsProcedureState.active);
    procRequest.setProcedureCiId(procedureCi.getCiId());
    procRequest.setArglist(procedureArgument(type));
    logger.info(
        "submitting gslb-migration procedure for nsPath "
            + bomNsPath
            + ", proc ciId : "
            + procedureCi.getCiId());

    return opsManager.submitProcedure(procRequest);
  }

  private List<PlatformFqdn> platformFqdnsThatCanBeMigrated(
      String nsPath, String type, boolean force, boolean excludeCloudVip) {
    type = type(type);
    String[] nsElements = nsPath.split("/");
    if (!isAtleastEnvironmentLevelNs(nsElements)) {
      throw new CmsException(
          CmsError.CMS_EXCEPTION, "nsPath should be atleast upto environment ns.");
    }
    if (!isGdnsEnabled(nsElements)) {
      throw new CmsException(
          CmsError.CMS_EXCEPTION, "gdns not enabled for this environment, nothing to migrate");
    }

    logger.info(
        "started migrating gslb for nsPath: " + nsPath + ", force: " + force + ", type: " + type);
    // TODO: get all bom FQDN instances using
    // manifest.EntryPoint -> (manifest Fqdn) -> base.RealizedAs -> (bom Fqdn)
    List<CmsCI> fqdnInstances = cmsCmManager.getCiBy3NsLike(nsPath, FQDN_SHORT_CLASS, null);
    logger.info("gslb migration :: fqdns count: " + fqdnInstances.size());
    Map<String, List<CmsCI>> instanceMap =
        fqdnInstances
            .stream()
            .filter(ci -> ci.getCiClassName().startsWith("bom."))
            .collect(Collectors.groupingBy(CmsCI::getNsPath));

    logger.info("gslb migration :: instanceMap: " + instanceMap.keySet());
    List<PlatformFqdn> nsPaths = new ArrayList<>();
    for (Entry<String, List<CmsCI>> entry : instanceMap.entrySet()) {
      List<CmsCI> bomFqdns = entry.getValue();

      List<CmsCI> eligibleFqdns = new ArrayList<>();
      for (CmsCI fqdn : bomFqdns) {
        if (isEligibleForMigration(fqdn, excludeCloudVip)) {
          eligibleFqdns.add(fqdn);
        }
      }
      if (eligibleFqdns.isEmpty()) {
        logger.info(
            "gslb migrate :: skipping platform "
                + bomFqdns.get(0).getNsPath()
                + " as it does not meet the conditions for gslb migration");
        continue;
      }

      // continue only if there is atleast one bom instance that is not migrated or force is
      // requested
      if (force || hasAnyNonMigratedInstance(eligibleFqdns, type)) {
        PlatformFqdn platformFqdn = new PlatformFqdn();
        platformFqdn.setNsPath(entry.getKey());
        platformFqdn.setEntries(attribute(bomFqdns.get(0), "entries"));
        nsPaths.add(platformFqdn);
      } else {
        logger.info(
            "gslb migration :: no migrated instances available in this platform"
                + bomFqdns.get(0).getNsPath());
      }
    }
    return nsPaths;
  }

  @RequestMapping(value = "/gslb/migrate/platforms", method = RequestMethod.GET)
  @ResponseBody
  public List<PlatformFqdn> getEligiblePlatforms(
      @RequestParam String nsPath,
      @RequestParam String type,
      @RequestParam(required = false) boolean force,
      @RequestParam(required = false) boolean excludeCloudVip)
      throws Exception {
    return platformFqdnsThatCanBeMigrated(nsPath, type, force, excludeCloudVip);
  }

  private boolean isEligibleForMigration(CmsCI fqdn, boolean excludeCloudVip) {
    List<CmsCIRelation> lbRelations =
        cmsCmManager.getFromCIRelations(fqdn.getCiId(), "bom.DependsOn", null, "Lb");
    if (lbRelations != null && lbRelations.size() > 0) {
      CmsCI lb = lbRelations.get(0).getToCi();
      return isNotTrue(attribute(fqdn, "ptr_enabled"))
          && isNotTrue(attribute(lb, "stickiness"))
          && (!excludeCloudVip || isNotTrue(attribute(lb, "create_cloud_level_vips")));
    }
    logger.info("gslb migrate :: no dependsOn lb found " + fqdn.getNsPath());
    return false;
  }

  private boolean isNotTrue(String attrValue) {
    return !("true".equalsIgnoreCase(attrValue));
  }

  private boolean isAtleastEnvironmentLevelNs(String[] nsElements) {
    return nsElements.length >= 4
        && !"public".equals(nsElements[1])
        && !"_clouds".equals(nsElements[2]);
  }

  private boolean isGdnsEnabled(String[] nsElements) {
    String assemblyNs = String.join("/", "", nsElements[1], nsElements[2]);
    List<CmsCI> cis = cmsCmManager.getCiBy3(assemblyNs, "manifest.Environment", nsElements[3]);
    if (cis == null || cis.isEmpty())
      throw new CmsException(CmsError.CMS_EXCEPTION, "No environment ci found @ " + assemblyNs);
    String value = attribute(cis.get(0), "global_dns");
    logger.info(
        "gslb migrate :: assembly ns : "
            + assemblyNs
            + "platform "
            + nsElements[2]
            + " gdns enabled : "
            + value);
    return "true".equalsIgnoreCase(value);
  }

  private OpsProcedureSimple procedure(CmsOpsProcedure procedure, String nsPath) {
    return new OpsProcedureSimple()
        .nsPath(nsPath)
        .procedureId(procedure.getProcedureId())
        .state(procedure.getProcedureState());
  }

  private boolean isSuccessful(CmsOpsProcedure procedure) {
    return (procedure.getProcedureState() == complete);
  }

  private CmsOpsProcedure waitForCompletion(long procedureId) throws Exception {
    CmsOpsProcedure opsProcedure = null;
    long startTime = System.currentTimeMillis();
    while (timeElapsedInSecs(startTime) < 300) {
      opsProcedure = opsManager.getCmsOpsProcedure(procedureId, false);
      if (opsProcedure.getProcedureState() == pending
          || opsProcedure.getProcedureState() == active) {
        logger.info("procedure " + procedureId + " still running, will wait and check");
        Thread.sleep(3_000);
      } else break;
    }
    return opsProcedure;
  }

  private long timeElapsedInSecs(long startTime) {
    return (System.currentTimeMillis() - startTime) / 1000L;
  }

  private boolean hasAnyNonMigratedInstance(List<CmsCI> bomCis, String type) {
    Optional<CmsCI> optinal =
        bomCis.stream().filter(ci -> !type.equals(attribute(ci, SERVICE_TYPE_ATTR))).findFirst();
    return optinal.isPresent();
  }

  private String procedureArgument(String type) {
    return gson.toJson(new MigrateArg(type));
  }

  // get the gslb type, default is torbit
  private String type(String type) {
    if (StringUtils.isNotBlank(type)) {
      type = type.toLowerCase();
      if (GSLB_TYPE_NETSCALER.equals(type) || GSLB_TYPE_TORBIT.equals(type)) {
        return type;
      }
      throw new IllegalArgumentException(
          "Invalid type. type should be one of ["
              + GSLB_TYPE_NETSCALER
              + ","
              + GSLB_TYPE_TORBIT
              + "]");
    }
    return GSLB_TYPE_TORBIT;
  }

  private CmsCI getMigrationProcedure(String packNsPath) {
    CmsCI procCi = null;
    List<CmsCI> cis = cmsCmManager.getCiBy3(packNsPath, PROCEDURE_DEFN_CLASS, GSLB_MIGRATION_PROC);
    if (cis != null && !cis.isEmpty()) {
      procCi = cis.get(0);
    }
    return procCi;
  }

  private CmsCI getManifestPlatformCi(String bomNsPath) {
    String[] elements = bomNsPath.split("/");
    if (elements.length > 6) {
      elements[4] = "manifest";
      String manifestNsPath = String.join("/", elements);
      List<CmsCI> cis = cmsCmManager.getCiBy3(manifestNsPath, MANIFEST_PLATFORM_CLASS, elements[5]);
      if (!cis.isEmpty()) {
        return cis.get(0);
      }
    }
    return null;
  }

  private String getPackNsPath(CmsCI platformCi) {
    return "/public/"
        + attribute(platformCi, PACK_SOURCE_ATTRIBUTE)
        + "/packs/"
        + attribute(platformCi, PACK_NAME_ATTRIBUTE)
        + "/"
        + attribute(platformCi, PACK_VERSION_ATTRIBUTE)
        + "/"
        + attribute(platformCi, PLATFORM_AVAILABILITY_ATTRIBUTE);
  }

  private String attribute(CmsCI ci, String attributeName) {
    CmsCIAttribute attribute = ci.getAttribute(attributeName);
    if (attribute != null) {
      return attribute.getDjValue();
    }
    return null;
  }
}
