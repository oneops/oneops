package com.oneops.tekton;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang.StringUtils;
import com.oneops.tekton.CloudProviderMapping.*;
import org.apache.log4j.Logger;

import java.util.*;

public class TektonUtils {

    public static final String PROVIDER_MAPPINGS_CMS_VAR_NAME = "CLOUD_PROVIDER_MAPPINGS";
    private List<CloudProviderMapping> cloudProviderMappings;
    private Map<String, String> providers = new HashMap<>();
    private Map<Long, String> cloudSubscriptionIds = new HashMap<>();
    private CmsCmProcessor cmProcessor;
    private static Logger logger = Logger.getLogger(TektonUtils.class);
    private Gson gson = new Gson();

    public int getTotalCores(String size, CloudProviderMapping cloudProviderMapping) {
        List<ComputeMapping> computeMappings = cloudProviderMapping.getComputeMapping();
        for (ComputeMapping computeMapping : computeMappings) {
            if (computeMapping.getSize().equalsIgnoreCase(size)) {
                return computeMapping.getCores();
            }
        }

        throw new RuntimeException("Soft quota error. No mapping found for cores. Provider: "
                        + cloudProviderMapping.getProvider() + " size: " + size);
    }

    public List<CloudProviderMapping> getCloudProviderMappings() {
        if (this.cloudProviderMappings != null) {
            return this.cloudProviderMappings;
        }

        CmsVar cmsVar = cmProcessor.getCmSimpleVar(PROVIDER_MAPPINGS_CMS_VAR_NAME);
        if (cmsVar == null || StringUtils.isEmpty(cmsVar.getValue())) {
            return null;
        }
        String mappingJson = cmsVar.getValue();
        logger.info("Got cloud provider mappings: " + mappingJson);
        CloudProviderMapping[] mappingArray = gson.fromJson(mappingJson, CloudProviderMapping[].class);
        List<CloudProviderMapping> mappingList = Arrays.asList(mappingArray);
        this.cloudProviderMappings = mappingList;
        return mappingList;
    }

    public CloudProviderMapping getCloudProviderMapping(String provider, List<CloudProviderMapping> mappings) {
        if (provider == null) {
            return null;
        }

        if (mappings == null) {
            return null;
        }

        for (CloudProviderMapping mapping : mappings) {
            if (provider.toLowerCase().contains(mapping.getProvider().toLowerCase())) {
                return mapping;
            }
        }
        return null;
    }

    public String findProvider(CmsRfcRelation deployedToRelation) {
        String comments = deployedToRelation.getComments();
        Map<String, String> relationDetails = gson.fromJson(comments, Map.class);
        String cloudName = relationDetails.get("toCiName");
        String provider = null;
        if (providers.get(cloudName) != null) {
            provider = providers.get(cloudName);
        } else {
            long cloudCiId = deployedToRelation.getToCiId();
            CmsCI cloudCi = cmProcessor.getCiById(cloudCiId);
            CmsCIAttribute locationAttribute = cloudCi.getAttribute("location");
            if (locationAttribute != null) {
                String location = locationAttribute.getDfValue();
                String[] tokens = location.split("/");
                if (tokens.length > 0) {
                    provider = tokens[tokens.length - 1];
                    providers.put(cloudName, provider);
                }
            }
        }
        return provider;
    }


    public CmsCmProcessor getCmProcessor() {
        return cmProcessor;
    }

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public String findSubscriptionId(long cloudId) {
        if (cloudSubscriptionIds.get(cloudId) != null) {
            return cloudSubscriptionIds.get(cloudId);
        }
        //get the "provides" relation and get the service type having class = compute service
        //then get the subscription id field out of it
        String subscriptionId = null;

        List<CmsCIRelation> cloudServicesRelations = cmProcessor.getFromCIRelations(cloudId, "base.Provides",
                null);

        if (cloudServicesRelations == null || cloudServicesRelations.size() == 0) {
            logger.error("No services found for cloud id: " + cloudId);
            return null;
        }

        for (CmsCIRelation relation : cloudServicesRelations) {
            CmsCIRelationAttribute serviceTypeAttribute = relation.getAttribute("service");

            if (serviceTypeAttribute != null) {
                String serviceType = serviceTypeAttribute.getDfValue();
                if ("compute".equalsIgnoreCase(serviceType)) {  //found the compute cloud service for this cloud
                    CmsCI cloudService = relation.getToCi();
                    CmsCIAttribute subscriptionAttribute = cloudService.getAttribute("subscription");
                    if (subscriptionAttribute == null) {
                        subscriptionAttribute = cloudService.getAttribute("tenant");
                    }
                    if (subscriptionAttribute != null) {
                        subscriptionId = subscriptionAttribute.getDfValue();
                        cloudSubscriptionIds.put(cloudId, subscriptionId);
                        return subscriptionId;
                    }
                }
            }
        }

        return subscriptionId;
    }
}
