package com.oneops.cms.ws.rest;

import static com.oneops.cms.util.CmsError.CMS_EXCEPTION;
import static org.springframework.beans.BeanUtils.copyProperties;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.exceptions.CmsException;
import java.util.ArrayList;
import java.util.Collections;
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
  public Set<String> syncGslbMigrationProcedure(
      @RequestParam String source, @RequestParam(required = false) String pack) {
    if (pack == null) {
      pack = "";
    }
    String packBaseNsPath = "/public/" + source + "/packs/";
    List<CmsCIRelation> procedureRelations = getAllPlatformProcedures(packBaseNsPath);
    procedureRelations =
        procedureRelations
            .stream()
            .filter(r -> "gslb-migration".equals(r.getToCi().getCiName()))
            .collect(Collectors.toList());
    if (procedureRelations.isEmpty()) {
      throw new CmsException(
          CMS_EXCEPTION, "gslb-migration procedure ControlledBy relations not found");
    }
    Set<String> existingNsPaths =
        procedureRelations.stream().map(CmsCIRelation::getNsPath).collect(Collectors.toSet());
    logger.info("gslb-migration procedure available in these nsPaths " + existingNsPaths);

    CmsCIRelation existingRelation = findProcedureRelationForRedundant(procedureRelations);
    CmsCI existingPlatformCi = existingRelation.getFromCi();
    CmsCIRelation templateProcRelation = templateRelation(procedureRelations.get(0));
    CmsCIRelation fqdnPayloadRelation = templateRelation(getFqdnPayload(existingPlatformCi));

    Set<String> nsPaths = new HashSet<>();

    // for catalog ns we need to add only the procedure, for manifest ns we need to add the
    // procedure and the payload
    String packsNsPath = packBaseNsPath + pack;
    List<CmsCI> catalogPlatforms =
        cmProcessor.getCiBy3NsLike(packsNsPath, MGMT_CATALOG_PLATFORM_CLASS, null);
    List<String> addedNsPaths =
        addRelationToNsPath(
            catalogPlatforms, templateProcRelation, Optional.empty(), existingNsPaths);
    nsPaths.addAll(addedNsPaths);

    List<CmsCI> manifestPlatforms =
        cmProcessor.getCiBy3NsLike(packsNsPath, MGMT_MANIFEST_PLATFORM_CLASS, null);
    addedNsPaths =
        addRelationToNsPath(
            manifestPlatforms,
            templateProcRelation,
            Optional.of(fqdnPayloadRelation),
            existingNsPaths);
    nsPaths.addAll(addedNsPaths);
    return nsPaths;
  }

  private CmsCIRelation findProcedureRelationForRedundant(List<CmsCIRelation> relations) {
    Optional<CmsCIRelation> relationOpt =
        relations.stream().filter(r -> r.getNsPath().endsWith("/redundant")).findFirst();
    if (relationOpt.isPresent()) return relationOpt.get();
    throw new CmsException(
        CMS_EXCEPTION, "Existing procedure relation for redundant nsPath not found");
  }

  /**
   * creates the gslb-migration procedure in the pack nsPath, creates the fqdnclouds payload (for
   * fqdn component) required for migration/torbit deployments updates the services attribute of
   * requires relation (from:platform to:fqdn) to include *torbit if needed
   */
  private List<String> addRelationToNsPath(
      List<CmsCI> platforms,
      CmsCIRelation templateProcedureRelation,
      Optional<CmsCIRelation> templateFqdnPayloadRelation,
      Set<String> existingNsPaths) {

    // we only need to create the cm_ci_relation record, ci record for toCi.
    // fromCi should already be present
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
      Optional<CmsCIRelation> fqdnRelnOpt = getFqdnRequireRelation(platform);
      if (fqdnRelnOpt.isPresent()) {
        logger.info("gslb-migration-sync adding the procedure to nsPath : " + nsPath);
        CmsCIRelation newProcedureRelation =
            copy(templateProcedureRelation, nsPath, platform.getCiId());
        cmProcessor.createRelation(newProcedureRelation);

        CmsCIRelation fqdnRequiresRelation = fqdnRelnOpt.get();

        // add fqdnclouds payload if not already present
        if (templateFqdnPayloadRelation.isPresent() && !isFqdnPayloadAvailable(nsPath)) {
          logger.info("gslb-migration-sync adding the fqdnclouds payload to nsPath : " + nsPath);
          CmsCIRelation newFqdnPayloadRelation =
              copy(
                  templateFqdnPayloadRelation.get(),
                  nsPath,
                  fqdnRequiresRelation.getToCi().getCiId());
          cmProcessor.createRelation(newFqdnPayloadRelation);
        }

        // add torbit to services attribute of Requires relation if needed
        CmsCIRelationAttribute servicesAttr = fqdnRequiresRelation.getAttribute("services");
        if (servicesAttr != null) {
          if (updateServicesAttrIfNeeded(servicesAttr)) {
            logger.info("gslb-migration-sync updating services attribute for : " + nsPath);
            cmProcessor.updateRelation(fqdnRequiresRelation);
          }
        }

        nsPaths.add(nsPath);
      } else {
        logger.info(
            "gslb-migration-sync no fqdn ci present in nsPath, not adding procedure, payload " + nsPath);
      }
    }
    return nsPaths;
  }

  private boolean updateServicesAttrIfNeeded(CmsCIRelationAttribute servicesAttr) {
    boolean isUpdated = false;
    String dfValue = servicesAttr.getDfValue();
    if (!dfValue.contains("torbit")) {
      servicesAttr.setDfValue(dfValue + ",*torbit");
      isUpdated = true;
    }
    String djValue = servicesAttr.getDjValue();
    if (!djValue.contains("torbit")) {
      servicesAttr.setDjValue(djValue + ",*torbit");
      isUpdated = true;
    }
    return isUpdated;
  }

  private boolean isFqdnPayloadAvailable(String nsPath) {
    List<CmsCI> cis = cmProcessor.getCiBy3(nsPath, MGMT_MANIFEST_PAYLOAD_CLASS, "fqdnclouds");
    return cis != null && !cis.isEmpty();
  }

  private Optional<CmsCIRelation> getFqdnRequireRelation(CmsCI platform) {
    List<CmsCIRelation> requiresRelations =
        cmProcessor.getFromCIRelations(platform.getCiId(), "mgmt.Requires", null, "Fqdn");
    if (requiresRelations != null) {
      Optional<CmsCIRelation> requiresRelnOpt =
          requiresRelations
              .stream()
              .filter(r -> "fqdn".equals(r.getToCi().getCiName()))
              .findFirst();
      return requiresRelnOpt;
    }
    return Optional.empty();
  }

  private CmsCIRelation getFqdnPayload(CmsCI platform) {
    logger.info("getting existing fqdnclouds payload from nsPath " + platform.getNsPath());
    //payload relation mgmt.manifest.Payload from:fqdn to:payload
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

  private List<CmsCIRelation> getAllPlatformProcedures(String packNsPath) {
    //platform procedure relation mgmt.manifest.ControlledBy (from:platform  to:procedure)
    List<CmsCIRelation> list =
        cmProcessor.getCIRelationsNsLikeNakedNoAttrs(
            packNsPath,
            MGMT_CONTROLLER_BY_RELATION,
            null,
            "Platform",
            MGMT_PROCEDURE_CLASS,
            true,
            true);
    return list != null ? list : Collections.emptyList();
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
