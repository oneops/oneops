package com.oneops.capacity;

import com.google.gson.Gson;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.*;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.domain.AttrQueryCondition;
import com.oneops.cms.util.domain.CmsVar;

import org.apache.log4j.Logger;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public class CapacityProcessor {
	private static final Logger logger = Logger.getLogger(CapacityProcessor.class);
	
    static final String CAPACITY_MANAGEMENT_VAR_NAME = "CAPACITY_MANAGEMENT";
    static final String PROVIDER_MAPPINGS_CMS_VAR_NAME = "CLOUD_PROVIDER_MAPPINGS";

    private CmsCmProcessor cmProcessor;
    private CmsRfcProcessor rfcProcessor;
    private TektonClient tektonClient;
    private ExpressionParser exprParser;

    private Gson gson = new Gson();
    private Map<String, Expression> expressionCache = new HashMap<>();

    private static class DeltaCapacity {
    	private Map<String, Map<String, Integer>> increase;
    	private Map<String, Map<String, Integer>> decrease;
    	
		public DeltaCapacity(Map<String, Map<String, Integer>> increase, Map<String, Map<String, Integer>> decrease) {
			super();
			this.increase = increase;
			this.decrease = decrease;
		}

		public Map<String, Map<String, Integer>> getIncrease() {
			return increase;
		}

		public Map<String, Map<String, Integer>> getDecrease() {
			return decrease;
		}
		
		public boolean isEmpty() {
			return increase.isEmpty() && decrease.isEmpty();
		}

		@Override
		public String toString() {
			return "DeltaCapacity [increase=" + increase + ", decrease=" + decrease + "]";
		}
    }
    
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        this.cmProcessor = cmProcessor;
    }

    public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
        this.rfcProcessor = rfcProcessor;
    }

    public void setTektonClient(TektonClient tektonClient) {
        this.tektonClient = tektonClient;
    }

    public void setExprParser(ExpressionParser exprParser) {
        this.exprParser = exprParser;
    }

    
    
    public boolean isCapacityManagementEnabled(String nsPath) {
        CmsVar softQuotaEnabled = cmProcessor.getCmSimpleVar(CAPACITY_MANAGEMENT_VAR_NAME);
        if (softQuotaEnabled != null) {
            String value = softQuotaEnabled.getValue();
            if (Boolean.TRUE.toString().equalsIgnoreCase(value)) return true;
            if (nsPath != null && !value.isEmpty()) {
                return Arrays.stream(value.split(",")).anyMatch(nsPath::startsWith);
            }
        }
        return false;
    }

    public Map<String, Map<String, Integer>> calculateCapacity(Collection<CmsCI> cis, Collection<CmsCIRelation> deployedToRels) {
        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return null;

        List<CmsRfcCI> rfcCis = cis.parallelStream()
                .map(ci -> new CmsRfcCI(ci,
                                        "oneops-system",
                                        ci.getAttributes().entrySet().stream()
                                                .collect(HashMap::new, (map, e) -> map.put(e.getKey(), e.getValue().getDfValue()), HashMap::putAll)))
                .collect(toList());

        List<CmsRfcRelation> rfcDeployedToRels = deployedToRels.parallelStream()
                .map(ci -> new CmsRfcRelation(ci,"oneops-system"))
                .collect(toList());
        Map<Long, CloudInfo> deployedToCloudInfoMap = getDeployedToCloudInfoMap(rfcDeployedToRels);
        Map<String, Map<String, Integer>> capacity = getCapacityForCis(rfcCis, rfcDeployedToRels, deployedToCloudInfoMap, mappings);
        return capacity;
    }

    

    
    
    public CapacityEstimate estimateCapacity(String nsPath, Collection<CmsRfcCI> cis, Collection<CmsRfcRelation> addDeployedToRels) {
        if (!isCapacityManagementEnabled(nsPath)) return null;

        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return null;

        Map<String, List<CmsRfcCI>> groupedCis = cis.stream().collect(groupingBy(CmsRfcCI::getRfcAction));
        Map<String, Map<String, Integer>> increase = new HashMap<>();
        Map<String, Map<String, Integer>> decrease = new HashMap<>();
       
        List<CmsRfcRelation> deployedToRels = new ArrayList<CmsRfcRelation>();
        if (addDeployedToRels !=  null) {
        	deployedToRels.addAll(addDeployedToRels);
        }		
        
        List<CmsRfcCI> toAdd = groupedCis.remove("add");
        // handle not "add" first
        
        List<CmsRfcCI> updateAndDeleteRfcs = new ArrayList<>();
        List<CmsRfcCI> toDelete = groupedCis.remove("delete");
        if (toDelete != null) {
        	updateAndDeleteRfcs.addAll(toDelete);
        }
        
       groupedCis.values().forEach(l->updateAndDeleteRfcs.addAll(l));
       
       
       if (!updateAndDeleteRfcs.isEmpty()) {
        	// First, load everything needed for all categories: delete and update
        	List<Long> allIds = updateAndDeleteRfcs.stream().map(CmsRfcCI::getCiId).collect(toList());
        	
        	
            deployedToRels.addAll(cmProcessor.getCIRelationsByFromCiIdsNakedNoAttrs("base.DeployedTo", null, allIds).stream()
                    .map(rel -> new CmsRfcRelation(rel, null))
                    .collect(toList()));
       }

       deployedToRels = deployedToRels.stream().filter(distinctByKey(CmsRfcRelation::getFromCiId)).collect(toList());
       Map<Long, CloudInfo> deployedToCloudInfoMap = getDeployedToCloudInfoMap(deployedToRels);
       
       if (!updateAndDeleteRfcs.isEmpty()) {      
           	List<CmsCI> allCis = loadCis(updateAndDeleteRfcs);
        	List<CmsRfcCI> allRfcs = toRfcCis(allCis);
            // ok, all loaded. First process those to delete
            
            int toDeleteSize = toDelete == null ? 0 : toDelete.size();
            
            if (toDeleteSize > 0) {
            	List<CmsRfcCI> deleteCis = allRfcs.subList(0, toDelete.size());
            	decrease.putAll(getCapacityForCis(deleteCis, deployedToRels, deployedToCloudInfoMap, mappings));
            }
            
             int toUpdateSize = allRfcs.size() - toDeleteSize;
            
            if (toUpdateSize > 0) {
            	List<CmsCI> updateCis = allCis.subList(toDeleteSize, allRfcs.size());
            	List<CmsRfcCI> updateRfcs= updateAndDeleteRfcs.subList(toDeleteSize, allRfcs.size());
            	DeltaCapacity deltaCapacity = calculateDeltaCapacity(updateRfcs, updateCis, deployedToRels, deployedToCloudInfoMap, mappings);
            	merge(deltaCapacity.getIncrease(), increase);
            	merge(deltaCapacity.getDecrease(), decrease);
            }
        }
        
        // handle add
 	    Map<String, Map<String, Integer>> toIncrease = getCapacityForCis(toAdd, deployedToRels, deployedToCloudInfoMap, mappings);
	        
	    if (toIncrease != null && !toIncrease.isEmpty()) {
	    	merge(toIncrease, increase);
	    }    	
	    
	    String check = "ok";

	    if (!increase.isEmpty()) {
	    	Map<String, String> info = tektonClient.precheckReservation(increase, getReservationNsPath(nsPath), "oneops-system");
	    	
            if (info == null) {
                check = "failed";
            } 
            else if (info.isEmpty()) {
                check = "ok";
            } 
            else {
                check = capacityShortageMessage(info, deployedToCloudInfoMap, "");
            }
    	}
        

        CapacityEstimate result = new CapacityEstimate (increase, decrease, check);
        logger.debug("estimation result="+result);
        return result;
    }

    public void reserveCapacityForDeployment(CmsDeployment deployment) {
        String nsPath = deployment.getNsPath();
        if (!isCapacityManagementEnabled(nsPath)) return;

        // Just in case dump any reservation "leftovers".
        deleteReservations(getReservationNsPath(nsPath));

        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return;

        // Get capacity increase
        long releaseId = deployment.getReleaseId();
        List<CmsRfcCI> cis = rfcProcessor.getRfcCIBy3(releaseId, true, null);
        List<CmsRfcRelation> deployedToRels = rfcProcessor.getRfcRelationByReleaseAndClassNoAttrs(releaseId, null, "DeployedTo");

        
        List<CmsRfcCI> addCIs = cis.stream().filter(ci -> ci.getRfcAction().equals("add")).collect(toList());
        List<CmsRfcCI> updateRfcs = cis.stream().filter(ci -> !ci.getRfcAction().equals("add") && !ci.getRfcAction().equals("delete")).collect(toList());
        
        if (!updateRfcs.isEmpty()) {
        	List<Long> allIds = updateRfcs.stream().map(CmsRfcCI::getCiId).collect(toList());
            deployedToRels.addAll(cmProcessor.getCIRelationsByFromCiIdsNakedNoAttrs("base.DeployedTo", null, allIds).stream()
                    .map(rel -> new CmsRfcRelation(rel, null))
                    .collect(toList()));
        }    
        
        deployedToRels = deployedToRels.stream().filter(distinctByKey(CmsRfcRelation::getFromCiId)).collect(toList());
        
        Map<Long, CloudInfo> deployedToCloudInfoMap = getDeployedToCloudInfoMap(deployedToRels);
        Map<String, Map<String, Integer>> capacity = getCapacityForCis(addCIs, deployedToRels, deployedToCloudInfoMap, mappings);

        
        if (!updateRfcs.isEmpty()) {
        	DeltaCapacity deltaCapacity = calculateDeltaCapacity(updateRfcs, deployedToRels, deployedToCloudInfoMap, mappings);
        	logger.debug("delta capacity="+deltaCapacity);
        	merge(deltaCapacity.getIncrease(), capacity);
        }        	

    	logger.debug("reserve capacity="+capacity);

        
         if (!capacity.isEmpty()) {
            try {
                tektonClient.reserveQuota(capacity, getReservationNsPath(nsPath), deployment.getCreatedBy());
            } catch (ReservationException exception) {
                Map<String, String> info = exception.getInfo();
                String message = info == null ? exception.getMessage() : capacityShortageMessage(info, deployedToCloudInfoMap, "Failed to reserve capacity:\n");
                throw new RuntimeException(message);
            }
        }
    }

    public void discardCapacityForDeployment(CmsDeployment deployment) {
        String nsPath = deployment.getNsPath();
        if (!isCapacityManagementEnabled(nsPath)) return;

        deleteReservations(getReservationNsPath(nsPath));
    }
    
    
    public void adjustCapacity(CmsWorkOrder wo) {
    	String rfcAction = wo.getRfcCi().getRfcAction();
    	if (rfcAction.equalsIgnoreCase("add")) {
    		commitCapacity(wo);
    	} else if (rfcAction.equalsIgnoreCase("delete")) {
    		releaseCapacity(wo);
    	}
    	else {
    		updateCapacity(wo);
    	}
    }

    private void commitCapacity(CmsWorkOrder workOrder) {
        CmsRfcCI rfcCi = workOrder.getRfcCi();
        String nsPath = getReservationNsPath(rfcCi.getNsPath());
        if (!isCapacityManagementEnabled(nsPath)) return;

        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return;

        List<CmsRfcRelation> deployedTos = rfcProcessor.getOpenRfcRelationBy2NoAttrs(rfcCi.getCiId(), null, "base.DeployedTo", null);
        CloudInfo cloudInfo = getDeployedToCloudInfo(deployedTos.get(0).getToCiId());
        Map<String, Integer> capacity = getCapacityForCi(rfcCi, cloudInfo, mappings);

        if (!capacity.isEmpty()) {
            String subscriptionId = cloudInfo.getSubscriptionId();
            afterCommit(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    tektonClient.commitReservation(capacity, nsPath, subscriptionId);
                }
            });
        }
    }

    private void releaseCapacity(CmsWorkOrder workOrder) {
        CmsRfcCI rfcCi = workOrder.getRfcCi();
        String nsPath = getReservationNsPath(rfcCi.getNsPath());
        if (!isCapacityManagementEnabled(nsPath)) return;

        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return;

        List<CmsCIRelation> deployedTos = cmProcessor.getFromCIRelationsNakedNoAttrs(rfcCi.getCiId(), "base.DeployedTo", null, null);
        CloudInfo cloudInfo = getDeployedToCloudInfo(deployedTos.get(0).getToCiId());
        Map<String, Integer> capacity = getCapacityForCi(rfcCi, cloudInfo, mappings);

        if (!capacity.isEmpty()) {
            String subscriptionId = cloudInfo.getSubscriptionId();
            afterCommit(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                    tektonClient.releaseResources(capacity, nsPath, subscriptionId);
                }
            });
        }
    }

    private void updateCapacity(CmsWorkOrder workOrder) {
        CmsRfcCI rfcCi = workOrder.getRfcCi();
        String nsPath = getReservationNsPath(rfcCi.getNsPath());
        if (!isCapacityManagementEnabled(nsPath)) return;

        Map<String, Object> mappings = getCloudProviderMappings();
        if (mappings == null) return;

        List<CmsRfcRelation> deployedTos = cmProcessor.getFromCIRelationsNakedNoAttrs(rfcCi.getCiId(), "base.DeployedTo", null, null).stream()
                .map(rel -> new CmsRfcRelation(rel, null))
                .collect(toList());
 
        CloudInfo cloudInfo = getDeployedToCloudInfo(deployedTos.get(0).getToCiId());
        DeltaCapacity deltaCapacity = calculateDeltaCapacity(Collections.singletonList(rfcCi), deployedTos, Collections.singletonMap(deployedTos.get(0).getToCiId(), cloudInfo), mappings);
        
        logger.debug("updateCapacity: delta capacity="+deltaCapacity);
        
        if (!deltaCapacity.isEmpty()) {
            String subscriptionId = cloudInfo.getSubscriptionId();
            afterCommit(new TransactionSynchronizationAdapter() {
                @Override
                public void afterCommit() {
                	
                	if (!deltaCapacity.getIncrease().isEmpty()) {
                		tektonClient.commitReservation(deltaCapacity.getIncrease().get(subscriptionId), nsPath, subscriptionId);
                	}
                	
                	if (!deltaCapacity.getDecrease().isEmpty()) {
                		tektonClient.releaseResources(deltaCapacity.getDecrease().get(subscriptionId), nsPath, subscriptionId);
                	}                	
                }
            });
        }
    }
    
    void afterCommit(TransactionSynchronizationAdapter adapter) {
        TransactionSynchronizationManager.registerSynchronization(adapter);
    }

    private String getReservationNsPath(String fullNsPath) {
        String[] tokens = fullNsPath.split("/");
        return "/" + tokens[1] + "/" + tokens[2] + "/" + tokens[3];
    }

    private Map<String, Object> getCloudProviderMappings() {
        Map<String, Object> mappings = null;
        CmsVar cmsVar = cmProcessor.getCmSimpleVar(PROVIDER_MAPPINGS_CMS_VAR_NAME);
        if (cmsVar != null) {
            String json = cmsVar.getValue();
            if (json != null && !json.isEmpty()) {
                mappings = gson.fromJson(cmsVar.getValue(), Map.class);
            }
        }

        if (mappings == null || mappings.size() == 0) {
            logger.warn("Cloud provider mappings is not set.");
            mappings = null;
        }

        return mappings;
    }

    private Map<Long, CloudInfo> getDeployedToCloudInfoMap(Collection<CmsRfcRelation> deployedToRels) {
        return deployedToRels.stream()
                .map(CmsRfcRelation::getToCiId)
                .distinct()
                .collect(toMap(Function.identity(), this::getDeployedToCloudInfo));
    }

    private CloudInfo getDeployedToCloudInfo(long cloudCiId) {
        CmsCI cloud = cmProcessor.getCiById(cloudCiId);
        return getDeployedToCloudInfo(cloud);
    }

    private CloudInfo getDeployedToCloudInfo(CmsCI cloud) {
        long cloudCiId = cloud.getCiId();
        List<AttrQueryCondition> attrConds = new ArrayList<>();
        attrConds.add(new AttrQueryCondition("service", "eq", "compute"));
        List<CmsCIRelation> providesRels = cmProcessor.getFromCIRelationsByAttrs(cloudCiId, null, "Provides", null, attrConds);
        if (providesRels.isEmpty()) {
            logger.warn("Could not find compute services for cloudId: " + cloudCiId);
            return null;
        }
        CmsCI computeService = providesRels.get(0).getToCi();

        String[] classNameTokens = computeService.getCiClassName().split("\\.");
        String provider = classNameTokens[classNameTokens.length - 1].toLowerCase();

        String location = null;
        CmsCIAttribute attr = cloud.getAttribute("location");
        if (attr != null) {
            String[] tokens = attr.getDfValue().split("/");
            location = tokens[tokens.length - 1];
        }
        if (location == null) return null;

        String subscriptionId;
        attr = computeService.getAttribute("subscription");
        if (attr == null) {
            attr = computeService.getAttribute("tenant");
        }
        if (attr == null) {
            logger.warn("Neither subscription nor tenant attributes are present for compute cloud service: " + computeService.getCiName() + ", cloudID: " + cloudCiId);
            return null;
        } else {
            String subscription = attr.getDfValue();
            subscriptionId = location + ":" + subscription;
        }

        return new CloudInfo(cloudCiId, cloud.getCiName(), provider, subscriptionId);
    }

    
    private List<Map<String, Integer>> getCapacityByCi(List<CmsRfcCI> cis,
            Collection<CmsRfcRelation> deployedToRels,
            Map<Long, CloudInfo> cloudInfoMap,
            Map<String, Object> mappings) {
    
    	List<Map<String, Integer>> capacity = new ArrayList<Map<String,Integer>>();

		Map<Long, Long> ciToCloudMap = deployedToRels.stream()
		.collect(toMap(CmsRfcRelation::getFromCiId, CmsRfcRelation::getToCiId));

		for (CmsRfcCI rfcCI : cis) {
			CloudInfo cloudInfo = cloudInfoMap.get(ciToCloudMap.get(rfcCI.getCiId()));
			capacity.add(getCapacityForCi(rfcCI, cloudInfo, mappings));

		}

		return capacity;
}

    
    private Map<String, Map<String, Integer>> getCapacityForCis(Collection<CmsRfcCI> cis,
                                                                Collection<CmsRfcRelation> deployedToRels,
                                                                Map<Long, CloudInfo> cloudInfoMap,
                                                                Map<String, Object> mappings) {
        Map<String, Map<String, Integer>> capacity = new HashMap<>();

        if (cis == null) return capacity;

        Map<Long, Long> ciToCloudMap = deployedToRels.stream()
                .collect(toMap(CmsRfcRelation::getFromCiId, CmsRfcRelation::getToCiId));

        for (CmsRfcCI rfcCI : cis) {
            CloudInfo cloudInfo = cloudInfoMap.get(ciToCloudMap.get(rfcCI.getCiId()));
            Map<String, Integer> ciCapacity = getCapacityForCi(rfcCI, cloudInfo, mappings);
            if (!ciCapacity.isEmpty()) {
                Map<String, Integer> subCapacity = capacity.computeIfAbsent(cloudInfo.getSubscriptionId(), (k) -> new HashMap<>());
                for (String resource : ciCapacity.keySet()) {
                    subCapacity.put(resource, subCapacity.computeIfAbsent(resource, (k) -> 0) + ciCapacity.get(resource));
                }
            }
        }

        return capacity;
    }

    
    
    
    private Map<String, Integer> getCapacityForCi(CmsRfcCI rfcCi, CloudInfo cloudInfo, Map<String, Object> mappings) {
        Map<String, Integer> capacity = new HashMap<>();

        if (cloudInfo == null) {
            logger.warn("Could not determine cloud provider/subscription for rfc rfcId: " + rfcCi.getRfcId());
            return capacity;
        }

        Map<String, Object> providerMappings = (Map<String, Object>) mappings.get(cloudInfo.getProvider());
        if (providerMappings == null) return capacity;

        String[] classNameSplit = rfcCi.getCiClassName().split("\\.");
        Map<String, Object> classMappings = (Map<String, Object>) providerMappings.get(classNameSplit[classNameSplit.length - 1].toLowerCase());
        if (classMappings == null) return capacity;

        StandardEvaluationContext exprContext = null;
        for (String attrName : classMappings.keySet()) {
            Map<String, Object> attrMappings = (Map<String, Object>) classMappings.get(attrName);
            Map<String, Object> resources = null;

            // First try to map resources based on attribute value.
            CmsRfcAttribute attr = rfcCi.getAttribute(attrName);
            String value = (attr == null ? null : attr.getNewValue());
            if (value != null && !value.isEmpty()) {
                resources = (Map<String, Object>) attrMappings.get(value);
            }

            // If attribute is missing or value is not mapped, try to map to "otherwise" resources if they are specified via "*" key.
            if (resources == null) {
                // Try to go by "else" value if it is specified via "*" key.
                resources = (Map<String, Object>) attrMappings.get("*");
            }

            if (resources == null) continue;
            for (String resource : resources.keySet()) {
                Object resourceMapping = resources.get(resource);
                if (resourceMapping instanceof String) {

                    String expressionString = (String) resourceMapping;
                    try {
                        Expression expression = expressionCache.get(expressionString);
                        if (expression == null) {
                            expression = exprParser.parseExpression(expressionString);
                            expressionCache.put(expressionString, expression);
                        }
                        if (exprContext == null) {
                            exprContext = new StandardEvaluationContext(rfcCi);
                        }
                        resourceMapping = expression.getValue(exprContext);
                    } catch (Exception e) {
                        logger.error("Failed to parse/evaluate expression '" + expressionString + "': " + e.getMessage());
                    }
                }

                Integer resourceValue;
                if (resourceMapping instanceof Double) {
                    resourceValue = ((Double) resourceMapping).intValue();
                } else if (resourceMapping instanceof Integer) {
                    resourceValue = (Integer) resourceMapping;
                } else {
                    continue;
                }

                if (resourceValue > 0) {
                    capacity.put(resource, capacity.computeIfAbsent(resource, (k) -> 0) + resourceValue);
                }
            }
        }

        return capacity;
    }

    private void deleteReservations(String nsPath) {
        Set<String> subs = cmProcessor.getRelationCounts("base.Consumes", nsPath, true, null, null, null, null, "toCiId", null)
                .keySet()
                .stream()
                .map(ciId -> getDeployedToCloudInfo(Long.parseLong(ciId)))
                .filter(Objects::nonNull)
                .map(CloudInfo::getSubscriptionId)
                .collect(toSet());
        if (!subs.isEmpty()) {
            tektonClient.deleteReservations(nsPath, subs);
        }
    }

    private String capacityShortageMessage(Map<String, String> info, Map<Long, CloudInfo> cloudInfo, String prefix) {
        String check;
        Map<String, List<String>> cloudsBySubscription = new HashMap<>();
        cloudInfo.values().forEach(i -> cloudsBySubscription.computeIfAbsent(i.getSubscriptionId(), s -> new ArrayList<>()).add(i.getCiName()));
        check = info.entrySet().stream()
                .map(i -> String.join(", ", cloudsBySubscription.get(i.getKey())) + " - " + i.getValue())
                .collect(joining(";\n", prefix, ""));
        return check;
    }


    private class CloudInfo {
        private long ciId;
        private String ciName;
        private String provider;
        private String subscriptionId;

        CloudInfo(long ciId, String ciName, String provider, String subscriptionId) {
            this.ciId = ciId;
            this.ciName = ciName;
            this.provider = provider;
            this.subscriptionId = subscriptionId;
        }

        long getCiId() {
            return ciId;
        }

        String getProvider() {
            return provider;
        }

        String getSubscriptionId() {
            return subscriptionId;
        }

        public String getCiName() {
            return ciName;
        }
    }
    
    
    static List<CmsRfcCI> toRfcCis(List<CmsCI> cis) {
    	return cis.stream().map(ci->{
            CmsRfcCI rfc = new CmsRfcCI(ci, null);
            rfc.setAttributes(ci.getAttributes().values().stream()
                                      .map(ciAttr -> {
                                          CmsRfcAttribute rfcAttr = new CmsRfcAttribute();
                                          rfcAttr.setAttributeId(ciAttr.getAttributeId());
                                          rfcAttr.setAttributeName(ciAttr.getAttributeName());
                                          rfcAttr.setNewValue(ciAttr.getDfValue());
                                          return rfcAttr;
                                      })
                                      .collect(toMap(CmsRfcAttribute::getAttributeName, Function.identity())));
            return rfc;
            }).collect(toList());
    }
    
    static List<CmsRfcCI> createFinalSate(List<CmsRfcCI> rfcCis, List<CmsCI> currentState) {
    	return rfcCis.stream().map(r->{
    		CmsCI original = currentState.stream().filter(c->c.getCiId() == r.getCiId()).findFirst().get();
    		Assert.notNull(original, "Original CI must be present");
    		CmsRfcCI finalRfc = new CmsRfcCI(original, "");
    		
    		original.getAttributes().values().forEach(a->{finalRfc.addOrUpdateAttribute(a.getAttributeName(), a.getDfValue());});
    		r.getAttributes().values().forEach(a->{finalRfc.addOrUpdateAttribute(a.getAttributeName(), a.getNewValue());});
    		
    		return finalRfc;
    	}).collect(toList());
    }
    
    
    List<CmsCI> loadCis (List<CmsRfcCI> rfcCis) {
    	Map<Long, CmsCI> ciMap = cmProcessor.getCiByIdList(rfcCis.stream().map(CmsRfcCI::getCiId).collect(toList()))
    			.stream()
    			.collect(toMap(CmsCI::getCiId, Function.identity()))
    			;
    	
    	List<CmsCI> result = new  ArrayList<CmsCI>(rfcCis.size());
    	
    	rfcCis.forEach(r->{
    		CmsCI ci = ciMap.get(r.getCiId());
    		Assert.notNull("ci for RFC must not be null. cid="+r.getCiId());
    		result.add(ci);
    		
    	});
    	
    	return result;
    }
    
    static List<CmsRfcCI> getUpdateOrReplaceCi(Collection<CmsRfcCI> rfcCis) {
    		if (rfcCis == null) {
    			return null;
    		}
    		
    		return rfcCis.stream().filter(ci->ci.getRfcAction().equals("update") || ci.getRfcAction().equals("replace")).collect(toList());
    }

    private DeltaCapacity calculateDeltaCapacity(List<CmsRfcCI> updateRfcs, List<CmsRfcRelation> deployedToRels, Map<Long, CloudInfo> deployedToCloudInfoMap, Map<String, Object> mappings) {
    	return calculateDeltaCapacity(updateRfcs, loadCis(updateRfcs), deployedToRels, deployedToCloudInfoMap, mappings);
    }

    
    
    private DeltaCapacity calculateDeltaCapacity(List<CmsRfcCI> updateRfcs, List<CmsCI> updateCis, Collection<CmsRfcRelation> deployedToRels, Map<Long, CloudInfo> deployedToCloudInfoMap, Map<String, Object> mappings) {
    	// First, load everything needed for all categories: delete and update
    	
    	List<CmsRfcCI> updateRfcCisOriginal = toRfcCis(updateCis);
       	List<CmsRfcCI> updateRfcCisFinal = createFinalSate(updateRfcs, updateCis);
        
    	List<Map<String, Integer>> originalCapacity = getCapacityByCi(updateRfcCisOriginal,deployedToRels, deployedToCloudInfoMap, mappings);
    	List<Map<String, Integer>> finalCapacity = getCapacityByCi(updateRfcCisFinal, deployedToRels, deployedToCloudInfoMap, mappings);
    	List<Map<String, Integer>> delta = calculateDeltaCapacity(originalCapacity, finalCapacity);
        	
        Map<Long, Long> ciToCloudMap = deployedToRels.stream()
                    .collect(toMap(CmsRfcRelation::getFromCiId, CmsRfcRelation::getToCiId));            	
        
    	 Map<String, Map<String, Integer>> increase = new HashMap<String, Map<String,Integer>>();
    	 Map<String, Map<String, Integer>> decrease = new HashMap<String, Map<String,Integer>>();
        
        	IntStream.range(0, updateRfcs.size()).forEach(i->{
        		CmsRfcCI rfcCI = updateRfcs.get(i);
        		Map<String, Integer> rfcDelta = delta.get(i);
        		CloudInfo cloudInfo = deployedToCloudInfoMap.get(ciToCloudMap.get(rfcCI.getCiId()));
        		
        		rfcDelta.forEach((resource, v)->{
        			if (v > 0) {
        				Map<String, Integer> subCapacity = increase.computeIfAbsent(cloudInfo.getSubscriptionId(), (c) -> new HashMap<>());
                        subCapacity.put(resource, subCapacity.computeIfAbsent(resource, (k) -> 0) + v);
        			}
        			
           			if (v < 0) {
        				Map<String, Integer> subCapacity = decrease.computeIfAbsent(cloudInfo.getSubscriptionId(), (c) -> new HashMap<>());
                        subCapacity.put(resource, subCapacity.computeIfAbsent(resource, (k) -> 0) + -v);
        			}         			
        		});
        		
        	});
        	
        	return new DeltaCapacity(increase, decrease);
    }

    static void merge(Map<String, Map<String, Integer>> from, Map<String, Map<String, Integer>> to) {
    	from.forEach((subscription, resources)->{
    		Map<String, Integer> subCapacity = to.computeIfAbsent(subscription, (c) -> new HashMap<>());
    		resources.forEach((r, v)->{
    			subCapacity.put(r, subCapacity.computeIfAbsent(r, (k) -> 0) + v);
    		});
    	});
    }
    
    static List<Map<String, Integer>> calculateDeltaCapacity(List<Map<String, Integer>> originalCapacity, List<Map<String, Integer>> finalCapacity) {
    	return IntStream.range(0, originalCapacity.size()).mapToObj(i->calculateDeltaCapacity(originalCapacity.get(i), finalCapacity.get(i)))
    			.collect(toList());
    	
 
    }
    
    
    static Map<String, Integer> calculateDeltaCapacity(Map<String, Integer> originalCapacity, Map<String, Integer> finalCapacity) {
    	Map<String, Integer> result = new HashMap<String, Integer>();
    	
    	finalCapacity.forEach((k, v)->{
    		Integer originalValue = originalCapacity.get(k);
    		result.put(k ,originalValue == null ? v : v - originalValue);
    	});
    	
       	originalCapacity.forEach((k, v)->{
    		Integer finalValue = finalCapacity.get(k);
    		if (finalValue == null) {
    			result.put(k ,-v);
    		}
    	});	
       	
       	return result;
    }
    
    
    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
}
