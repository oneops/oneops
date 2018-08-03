package com.oneops.cms.ws.rest;

import static com.oneops.cms.cm.ops.domain.OpsProcedureState.active;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.complete;
import static com.oneops.cms.cm.ops.domain.OpsProcedureState.pending;
import static com.oneops.cms.util.CmsConstants.GSLB_TYPE_NETSCALER;
import static com.oneops.cms.util.CmsConstants.GSLB_TYPE_TORBIT;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureSimple;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.cm.ops.service.OpsManager;
import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.execution.MigrateArg;
import java.util.ArrayList;
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

  @Autowired
  OpsManager opsManager;

  @Autowired
  CmsCmManager cmsCmManager;

  private Gson gson = new Gson();

  @RequestMapping(value = "/gslb/migrate", method = RequestMethod.POST)
  @ResponseBody
  public List<OpsProcedureSimple> gslbMigrate(@RequestParam String nsPath,
      @RequestParam String type,
      @RequestParam(required=false) boolean force) throws Exception {
    type = type(type);
    //TODO validate nspath to be atleast upto environment, should not allow org level migrates
    logger.info("started migrating gslb for nsPath: " +  nsPath + ", force: " + force);
    List<CmsCI> fqdnInstances = cmsCmManager.getCiBy3NsLike(nsPath, FQDN_SHORT_CLASS, null);
    Map<String, CmsCI> procedureCiMap = new HashMap<>();
    Map<String, List<CmsCI>> instanceMap = fqdnInstances.stream()
        .filter(ci -> ci.getCiClassName().startsWith("bom."))
        .collect(Collectors.groupingBy(CmsCI::getNsPath));

    List<OpsProcedureSimple> procedureList = new ArrayList<>();
    for (Entry<String, List<CmsCI>> entry : instanceMap.entrySet()) {
      //continue only if there is atleast one bom instance that is not migrated or force is requested
      if (force || hasAnyNonMigratedInstance(entry.getValue(), type)) {
        String bomNsPath = entry.getKey();
        CmsCI manifestPlatformCi = getManifestPlatformCi(bomNsPath);

        if (manifestPlatformCi != null) {
          String packNsPath = getPackNsPath(manifestPlatformCi);
          logger.info("gslb migrate :: pack nsPath :" + packNsPath + " for bom nsPath : " + bomNsPath);
          if (StringUtils.isNotBlank(packNsPath)) {
            CmsCI procedureCi = procedureCiMap.computeIfAbsent(packNsPath, this::getMigrationProcedure);
            if (procedureCi != null) {
              //submit gslb-migration procedure for this nsPath
              CmsOpsProcedure procRequest = new CmsOpsProcedure();
              procRequest.setCiId(manifestPlatformCi.getCiId());
              procRequest.setCreatedBy("oneops-system");
              procRequest.setProcedureState(OpsProcedureState.active);
              procRequest.setProcedureCiId(procedureCi.getCiId());
              procRequest.setArglist(procedureArgument(type));
              logger.info("submitting gslb-migration procedure for nsPath " +  bomNsPath + ", proc ciId : " + procedureCi.getCiId());

              CmsOpsProcedure procedure = opsManager.submitProcedure(procRequest);
              CmsOpsProcedure updatedProc = waitForCompletion(procedure.getProcedureId());
              if (updatedProc == null)
                updatedProc = procedure;
              procedureList.add(procedure(updatedProc, bomNsPath));
              //continue only if the last procedure was successful
              if (!isSuccessful(updatedProc)) {
                logger.info("gslb migrate :: procedure [nsPath: " + bomNsPath +
                    ", id: " + procedure.getProcedureId() + "] not successful, aborting.");
                break;
              }
            }
          }
        }
      }
    }
    return procedureList;
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
      if (opsProcedure.getProcedureState() == pending ||
          opsProcedure.getProcedureState() == active) {
        logger.info("procedure " + procedureId + " still running, will wait and check");
        Thread.sleep(3_000);
      }
      else
        break;
    }
    return opsProcedure;
  }

  private long timeElapsedInSecs(long startTime) {
    return (System.currentTimeMillis() - startTime) / 1000L;
  }

  private boolean hasAnyNonMigratedInstance(List<CmsCI> bomCis, String type) {
    Optional<CmsCI> optinal = bomCis.stream()
        .filter(ci -> !type.equals(attribute(ci, SERVICE_TYPE_ATTR)))
        .findFirst();
    return optinal.isPresent();
  }

  private String procedureArgument(String type) {
    return gson.toJson(new MigrateArg(type));
  }

  //get the gslb type, default is torbit
  private String type(String type) {
    if (StringUtils.isNotBlank(type)) {
      type = type.toLowerCase();
      if (GSLB_TYPE_NETSCALER.equals(type) || GSLB_TYPE_TORBIT.equals(type)) {
        return type;
      }
      throw new IllegalArgumentException("Invalid type. type should be one of [" + GSLB_TYPE_NETSCALER + "," + GSLB_TYPE_TORBIT + "]");
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
    return "/public/" + attribute(platformCi, PACK_SOURCE_ATTRIBUTE)
        + "/packs/" + attribute(platformCi, PACK_NAME_ATTRIBUTE)
        + "/" + attribute(platformCi, PACK_VERSION_ATTRIBUTE)
        + "/" + attribute(platformCi, PLATFORM_AVAILABILITY_ATTRIBUTE);
  }

  private String attribute(CmsCI ci, String attributeName) {
    CmsCIAttribute attribute = ci.getAttribute(attributeName);
    if (attribute != null) {
      return attribute.getDjValue();
    }
    return null;
  }

}
