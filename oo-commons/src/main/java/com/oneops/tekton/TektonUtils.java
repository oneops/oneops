package com.oneops.tekton;

import com.google.gson.Gson;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.commons.lang.StringUtils;
import com.oneops.tekton.CloudProviderMapping.*;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;

public class TektonUtils {

    public static final String PROVIDER_MAPPINGS_CMS_VAR_NAME = "CLOUD_PROVIDER_MAPPINGS";
    private List<CloudProviderMapping> cloudProviderMappings;
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

    public CloudProviderMapping getCloudProviderMapping(String cloudName, List<CloudProviderMapping> mappings) {
        if (cloudName == null) {
            return null;
        }

        if (mappings == null) {
            return null;
        }

        for (CloudProviderMapping mapping : mappings) {
            if (cloudName.toLowerCase().contains(mapping.getProvider().toLowerCase())) {
                return mapping;
            }
        }
        return null;
    }

    public CmsCmProcessor getCmProcessor() {
        return cmProcessor;
    }

    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }
}
