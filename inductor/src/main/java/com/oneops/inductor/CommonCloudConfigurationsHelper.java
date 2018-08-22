package com.oneops.inductor;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class CommonCloudConfigurationsHelper {
    private Logger logger;
    private String logKey;

    public CommonCloudConfigurationsHelper(Logger logger, String logKey) {
        setLogger(logger);
        setLogKey(logKey);
    }

    public Map<String, Object> findClassCiAttributes(Map<String, Object> classesMap, String className) {
        return Optional.ofNullable((Map<String, Object>) classesMap.get(className))
                .orElse(Collections.emptyMap());
    }

    public Map<String, Object> findServiceClasses(Map<String, Object> servicesMap, String serviceName) {
        return Optional.ofNullable((Map<String, Object>) servicesMap.get(serviceName))
                .orElse(Collections.emptyMap());
    }

    public Map<String, Object> findServicesAtCloudLevel(Map<String, Object> cloudsMap, String cloudName) {
        Map<String, Object> servicesMap;
        if (cloudsMap.containsKey(cloudName)) {
            servicesMap = (Map<String, Object>) cloudsMap.get(cloudName);
        } else {
            servicesMap = findServicesFromCloudRegexMatch(cloudsMap, cloudName);
        }
        return servicesMap;
    }

    public Map<String, Object> findServicesAtOrgLevel(Map<String, Object> cloudConfig, String orgName, String cloudName) {
        if (cloudConfig.containsKey(orgName)) {
            Map<String, Object> cloudsMap = (Map<String, Object>) cloudConfig.get(orgName);
            return findServicesAtCloudLevel(cloudsMap, cloudName);
        }
        return Collections.emptyMap();
    }

    public Map<String, Object> findServicesFromCloudRegexMatch(Map<String, Object> cloudsMap, String cloudName) {
        try {
            // Get services list when cloud regex match
            List<Object> servicesList = cloudsMap.entrySet().
                    stream().filter(entry -> {
                if (entry.getKey().contains(".*")) {
                    Pattern p = Pattern.compile(entry.getKey());
                    Matcher m = p.matcher(cloudName);
                    return m.find();
                } else {
                    return false;
                }
            }).map(Map.Entry::getValue).
                    collect(Collectors.toList());

            if (!servicesList.isEmpty()) {
                return (Map<String, Object>) servicesList.get(0);
            }
        } catch (Exception e) {
            logger.error(logKey + e.getMessage());
        }
        return Collections.emptyMap();
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public void setLogKey(String logKey) {
        this.logKey = logKey;
    }
}