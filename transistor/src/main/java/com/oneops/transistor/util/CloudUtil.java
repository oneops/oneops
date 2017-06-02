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
package com.oneops.transistor.util;

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.util.CmsConstants;
import com.oneops.transistor.exceptions.TransistorException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.oneops.cms.util.CmsConstants.*;
import static com.oneops.cms.util.CmsError.*;
import static java.util.stream.Collectors.toSet;

/**
 * Generic purpose class which can be used for getting cloud related configuration (services).
 */
public class CloudUtil {

    private static final Logger logger = Logger.getLogger(CloudUtil.class);
    private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
    private CmsCmProcessor cmProcessor;

    /**
     * This will check if all required services are configured for clouds in
     * which platform needs to be deployed.
     * @param platformIds to check if all services required for the platform are configured
     * @throws TransistorException if not all services are configured in clouds.
     * */
    public void check4missingServices(Set<Long> platformIds) {

        Set<String> errors = new HashSet<>(platformIds.size());
        Map<String, Set<String>> cloudServices = new HashMap<>();
        for (long manifestPlatformId : platformIds) {
            List<CmsRfcRelation> requiredRelations = getRequired(manifestPlatformId);
            Set<String> requiredServices = getRequiredServices(requiredRelations);
            List<CmsRfcRelation> cloudRelations = getCloudsForPlatform(manifestPlatformId);
            Map<String, TreeSet<String>> cloudsMissingServices = new HashMap<>();
            cloudRelations
                    .forEach(cloudRelation -> {
                        String cloud = cloudRelation.getToRfcCi().getCiName();
                        //have not seen the cloud before
                        if (!cloudServices.containsKey(cloud)) {
                            cloudServices.put(cloud, getCloudServices(cloudRelation));
                        }
                        //check if service is configured
                        cloudsMissingServices.putAll(getMissingCloudServices(cloud,
                                cloudServices.get(cloud),
                                requiredServices));
                    });

            if (!cloudsMissingServices.isEmpty()) {
                // <{c1=[s1]}>
                final String nsPath = requiredRelations.get(0).getNsPath();
                String message = getErrorMessage(cloudsMissingServices, nsPath);
                errors.add(message);
            }
        }
        if (!errors.isEmpty()) {
            throw new TransistorException(TRANSISTOR_MISSING_CLOUD_SERVICES, StringUtils.join(errors, ";\n"));
        }
    }


    private String getErrorMessage(Map<String, TreeSet<String>> cloudsMissingServices, String nsPath) {
        return String.format("All services <%s> required for platform (%s) are not configured for clouds. Please contact your org. admin."
                        , cloudsMissingServices.toString(),
                getNSWithoutManifest(nsPath));
    }

    private Set<String> getRequiredServices(List<CmsRfcRelation> requiredRelations) {
        return requiredRelations.stream()
                .filter(this::needServices)
                .flatMap(this::getServices)
                .collect(toSet());
    }

    private List<CmsRfcRelation> getRequired(long manifestPlatformId) {
        return cmRfcMrgProcessor.getFromCIRelationsNaked(manifestPlatformId, MANIFEST_REQUIRES, null, null);
    }

    private String getNSWithoutManifest(String nsPath) {
        if(nsPath ==null ) return null;
        return nsPath.replaceAll("manifest/","");
    }


    private Map<String, TreeSet<String>> getMissingCloudServices(long manifestPlatCiId, Set<String> requiredServices) {
        Map<String, TreeSet<String>> missingCloud2Services = new TreeMap<>();
        //get clouds
        List<CmsRfcRelation> cloudRelations = getCloudsForPlatform(manifestPlatCiId);
        // get services for all clouds
        cloudRelations
                .forEach(cloudRelation -> {
                    Set<String> cloudServices = getCloudServices(cloudRelation);
                    String cloud = cloudRelation.getToRfcCi().getCiName();
                    //check if service is configured
                    requiredServices.stream()
                            .filter(s -> !cloudServices.contains(s))
                            .forEach(s -> missingCloud2Services
                                    .computeIfAbsent(cloud, k -> new TreeSet<>())
                                    .add(s));
                logger.debug("cloud: " +cloud +" required services:: " + requiredServices.toString() +" missingServices "+ missingCloud2Services.keySet() );
                });

        return missingCloud2Services;
    }

    private Map<String, TreeSet<String>> getMissingCloudServices(String cloud, Set<String> cloudServices, Set<String> requiredServices) {
        Map<String, TreeSet<String>> missingCloud2Services = new TreeMap<>();
        requiredServices.stream()
                .filter(s -> !cloudServices.contains(s))
                .forEach(s -> missingCloud2Services
                        .computeIfAbsent(cloud, k -> new TreeSet<>())
                        .add(s));
        logger.debug("cloud: " + cloud + " required services:: " + requiredServices.toString() + " missingServices " + missingCloud2Services.keySet());

        return missingCloud2Services;
    }

    private Set<String> getCloudServices(CmsRfcRelation rel) {
        return cmProcessor.getFromCIRelationsNaked(rel.getToCiId(), BASE_PROVIDES, null)
                                .stream()
                                .filter(this::isService)
                                .map(cmsCIRelation -> cmsCIRelation.getAttribute("service").getDjValue())
                                .collect(Collectors.toSet());
    }

    private List<CmsRfcRelation> getCloudsForPlatform(long manifestPlatCiId) {
        return cmRfcMrgProcessor.getFromCIRelations(manifestPlatCiId,
                    BASE_CONSUMES, ACCOUNT_CLOUD_CLASS, "dj");
    }

    public Set<String> getMissingServices(long manifestPlatformId){
        Set<String> requiredServices = getServicesForPlatform(manifestPlatformId);
        return getMissingCloudServices(manifestPlatformId,requiredServices).keySet();
    }

    private Set<String> getServicesForPlatform(long manifestPlatformId) {
        List<CmsRfcRelation> rfcRelations = getRequired(manifestPlatformId);
        return getRequiredServices(rfcRelations);
    }

    private Stream<String> getServices(CmsRfcRelation cmsRfcRelation) {
        String[] ciRequiredServices = cmsRfcRelation.getAttribute("services").getNewValue().split(",");
        return Arrays.stream(ciRequiredServices)
                .filter(s -> !s.startsWith("*"));
    }

    private boolean isService(CmsCIRelation rel) {
        CmsCIRelationAttribute serviceAttr = rel.getAttribute("service");
        return serviceAttr != null && serviceAttr.getDjValue() != null;
    }

    private boolean needServices(CmsRfcRelation rfc) {
        CmsRfcAttribute servicesAttr = rfc.getAttribute("services");
        return servicesAttr != null && servicesAttr.getNewValue() != null && StringUtils.isNotBlank(servicesAttr.getNewValue());
    }

    public boolean isCloudActive(CmsCIRelation platformCloudRel) {
        return checkCloudStatus(platformCloudRel, CmsConstants.CLOUD_STATE_ACTIVE);
    }

    public boolean isCloudOffline(CmsCIRelation platformCloudRel) {
        return checkCloudStatus(platformCloudRel, CmsConstants.CLOUD_STATE_OFFLINE);
    }

    private boolean checkCloudStatus(CmsCIRelation platformCloudRel, String cloudState) {
        return platformCloudRel.getAttribute(ATTR_NAME_ADMINSTATUS) != null
                && cloudState.equals(platformCloudRel.getAttribute(ATTR_NAME_ADMINSTATUS).getDjValue());
    }


    public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
        this.cmRfcMrgProcessor = cmRfcMrgProcessor;
    }

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }



}
