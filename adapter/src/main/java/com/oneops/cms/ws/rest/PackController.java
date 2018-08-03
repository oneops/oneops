package com.oneops.cms.ws.rest;

import static com.oneops.cms.util.CmsError.CMS_EXCEPTION;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.CmsException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PackController extends AbstractRestController {

  private static final String MGMT_PROCEDURE_CLASS = "mgmt.manifest.Procedure";
  private static final String MGMT_CONTROLLER_BY_RELATION = "mgmt.manifest.ControlledBy";
  private static final String MGMT_CATALOG_PLATFORM_CLASS = "mgmt.catalog.Platform";
  private static final String MGMT_MANIFEST_PLATFORM_CLASS = "mgmt.manifest.Platform";
  private static final String MGMT_MANIFEST_PAYLOAD_CLASS = "mgmt.manifest.Qpath";
  private static final String MGMT_MANIFEST_PAYLOAD_RELATION = "mgmt.manifest.Payload";

  private Logger logger = Logger.getLogger(this.getClass());

  @Autowired CmsCmProcessor cmProcessor;

  @RequestMapping(value = "/packs/sync/gslb-migrate", method = RequestMethod.POST)
  @ResponseBody
  public Set<String> syncGslbMigrationProcedure(@RequestParam String source) {
    String packBaseNsPath = "/public/" + source + "/packs/";
    List<CmsCIRelation> procedureRelations = getAllGslbMigrationProcedures(packBaseNsPath);
    if (procedureRelations == null || procedureRelations.isEmpty()) {
      throw new CmsException(
          CMS_EXCEPTION, "gslb-migration procedure ControlledBy relations not found");
    }
    Set<String> existingNsPaths =
        procedureRelations.stream().map(CmsCIRelation::getNsPath).collect(Collectors.toSet());
    logger.info("gslb-migration available in these nsPaths " + existingNsPaths);

    CmsCI existingPlatformCi = procedureRelations.get(0).getFromCi();
    CmsCIRelation templateProcRelation = templateRelation(procedureRelations.get(0));
    CmsCIRelation fqdnPayloadRelation = templateRelation(getFqdnPayload(existingPlatformCi));

    Set<String> nsPaths = new HashSet<>();

    // for catalog ns we need to add only the procedure, for manifest ns we need to add the
    // procedure and the payload
    List<CmsCI> catalogPlatforms =
        cmProcessor.getCiBy3NsLike(packBaseNsPath, MGMT_CATALOG_PLATFORM_CLASS, null);
    List<String> addedNsPaths =
        addRelationToNsPath(
            catalogPlatforms, templateProcRelation, Optional.empty(), existingNsPaths);
    nsPaths.addAll(addedNsPaths);

    List<CmsCI> manifestPlatforms =
        cmProcessor.getCiBy3NsLike(packBaseNsPath, MGMT_MANIFEST_PLATFORM_CLASS, null);
    addedNsPaths =
        addRelationToNsPath(
            manifestPlatforms,
            templateProcRelation,
            Optional.of(fqdnPayloadRelation),
            existingNsPaths);
    nsPaths.addAll(addedNsPaths);
    return nsPaths;
  }

  private List<String> addRelationToNsPath(
      List<CmsCI> platforms,
      CmsCIRelation templateProcedureRelation,
      Optional<CmsCIRelation> templateFqdnPayloadRelation,
      Set<String> existingNsPaths) {

    // we only need to create the cm_ci_relation record, ci record for toCi, fromCi should already
    // be present
    List<String> nsPaths = new ArrayList<>();
    List<CmsCI> platformsToBeAdded =
        platforms
            .stream()
            .filter(
                n ->
                    !existingNsPaths.contains(n.getNsPath())
                        && isValidNsForProcedure(n.getNsPath()))
            .collect(Collectors.toList());

    for (CmsCI platform : platformsToBeAdded) {
      String nsPath = platform.getNsPath();
      Optional<CmsCI> fqdnOpt = getFqdnCI(nsPath);
      if (fqdnOpt.isPresent()) {
        logger.info("gslb-migration adding the procedure to nsPath : " + nsPath);
        CmsCIRelation newProcedureRelation =
            copy(templateProcedureRelation, nsPath, platform.getCiId());
        cmProcessor.createRelation(newProcedureRelation);

        // add fqdnclouds payload if not already present
        if (templateFqdnPayloadRelation.isPresent() && !isFqdnPayloadAvailable(nsPath)) {
          logger.info("gslb-migration adding the fqdnclouds payload to nsPath : " + nsPath);
          CmsCIRelation newFqdnPayloadRelation =
              copy(templateFqdnPayloadRelation.get(), nsPath, fqdnOpt.get().getCiId());
          cmProcessor.createRelation(newFqdnPayloadRelation);
        }
        nsPaths.add(nsPath);
      } else {
        logger.info(
            "gslb-migration no fqdn ci present in nsPath, not adding procedure, payload " + nsPath);
      }
    }
    return nsPaths;
  }

  private boolean isFqdnPayloadAvailable(String nsPath) {
    List<CmsCI> cis = cmProcessor.getCiBy3(nsPath, MGMT_MANIFEST_PAYLOAD_CLASS, "fqdnclouds");
    return cis != null && !cis.isEmpty();
  }

  private Optional<CmsCI> getFqdnCI(String nsPath) {
    List<CmsCI> cis = cmProcessor.getCiBy3(nsPath, "Fqdn", "fqdn");
    if (cis != null && !cis.isEmpty()) {
      return Optional.of(cis.get(0));
    }
    return Optional.empty();
  }

  private CmsCIRelation getFqdnPayload(CmsCI platform) {
    logger.info("getting existing fqdnclouds payload from nsPath " + platform.getNsPath());
    List<CmsCIRelation> payloadRelations =
        cmProcessor.getCIRelations(
            platform.getNsPath(), MGMT_MANIFEST_PAYLOAD_RELATION, null, "Fqdn", null);
    Optional<CmsCIRelation> relnOpt =
        payloadRelations
            .stream()
            .filter(r -> "fqdnclouds".equals(r.getToCi().getCiName()))
            .findFirst();
    if (relnOpt.isPresent()) {
      return relnOpt.get();
    } else {
      throw new CmsException(CMS_EXCEPTION, "can't get fqdnclouds payload");
    }
  }

  private CmsCIRelation copy(CmsCIRelation existingRelation, String nsPath, long fromCiId) {
    CmsCI existingCi = existingRelation.getToCi();
    CmsCI newCi = new CmsCI();
    copyProperties(existingCi, newCi);
    newCi.setNsPath(nsPath);
    Map<String, CmsCIAttribute> attributesMap = new HashMap<>();
    newCi.setAttributes(attributesMap);
    for (Entry<String, CmsCIAttribute> entry : existingCi.getAttributes().entrySet()) {
      CmsCIAttribute newAttr = new CmsCIAttribute();
      copyProperties(entry.getValue(), newAttr);
      newAttr.setAttributeName(entry.getKey());
      attributesMap.put(entry.getKey(), newAttr);
    }
    CmsCIRelation newRelation = new CmsCIRelation();
    copyProperties(existingRelation, newRelation);
    newRelation.setToCi(newCi);
    newRelation.setNsPath(nsPath);
    newRelation.setFromCiId(fromCiId);
    return newRelation;
  }

  private boolean isValidNsForProcedure(String nsPath) {
    String[] nsElements = nsPath.split("/");
    return nsElements.length == 6
        || (nsElements.length == 7
            && ("single".equals(nsElements[6]) || "redundant".equals(nsElements[6])));
  }

  private List<CmsCIRelation> getAllGslbMigrationProcedures(String packNsPath) {
    return cmProcessor.getCIRelationsNsLikeNakedNoAttrs(
        packNsPath,
        MGMT_CONTROLLER_BY_RELATION,
        null,
        "Platform",
        MGMT_PROCEDURE_CLASS,
        true,
        true);
  }

  private CmsCIRelation templateRelation(CmsCIRelation procedureRelation) {
    logger.info(
        "gslb sync : template relation name: "
            + procedureRelation.getRelationName()
            + ", ciRelationId: "
            + procedureRelation.getCiRelationId()
            + ", to_ci_id "
            + procedureRelation.getToCi().getCiId());
    procedureRelation.setFromCi(null);
    procedureRelation.setFromCiId(0);
    procedureRelation.setToCiId(0);
    procedureRelation.setNsPath(null);
    procedureRelation.setNsId(0);
    procedureRelation.setCiRelationId(0);
    procedureRelation.setRelationGoid(null);

    CmsCI toCi = procedureRelation.getToCi();
    toCi.setNsPath(null);
    toCi.setNsId(0);
    toCi.setCiId(0);
    toCi.setCiGoid(null);
    for (CmsCIAttribute attr : toCi.getAttributes().values()) {
      attr.setCiId(0);
      attr.setCiAttributeId(0);
    }
    return procedureRelation;
  }
}
