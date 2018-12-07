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
package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.dj.service.CmsCmRfcMrgProcessor;
import com.oneops.cms.dj.service.CmsRfcProcessor;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.domain.ManifestRfcContainer;
import com.oneops.transistor.domain.ManifestRfcRelationTriplet;
import com.oneops.transistor.domain.ManifestRootRfcContainer;
import com.oneops.transistor.exceptions.TransistorException;
import com.oneops.transistor.util.CloudUtil;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ManifestManagerImpl implements ManifestManager {

	private static final Logger logger = Logger.getLogger(ManifestManagerImpl.class);
	private CmsCmProcessor cmProcessor;
	private CmsRfcProcessor rfcProcessor;
	private CmsCmRfcMrgProcessor cmRfcMrgProcessor;
	private ManifestRfcBulkProcessor manifestRfcProcessor;
	private TransUtil trUtil;
	private CloudUtil cloudUtil;
	private ExecutorService executorService;
	private int timeoutInMilliSeconds;


	private final static String BASE_REALIZED_IN = "base.RealizedIn";
	private final static String MANIFEST_PLATFORM = "manifest.Platform";
	private final static String MANIFEST_COMPOSED_OF = "manifest.ComposedOf";
	private final static String CATALOG_PLATFORM = "catalog.Platform";
	private final static String ACCOUNT_ASSEMBLY = "account.Assembly";
	private final static String BASE_CONSUMES = "base.Consumes";
	private final static String BASE_DEPLOYED_TO = "base.DeployedTo";
	private final static String ACCOUNT_CLOUD = "account.Cloud";
	private final static String RELEASE_STATE_OPEN = "open";


	public void setTimeoutInMilliSeconds(int timeoutInMilliSeconds) {
		this.timeoutInMilliSeconds = timeoutInMilliSeconds;
	}


	public void setCmRfcMrgProcessor(CmsCmRfcMrgProcessor cmRfcMrgProcessor) {
		this.cmRfcMrgProcessor = cmRfcMrgProcessor;
	}

	public void setTrUtil(TransUtil trUtil) {
		this.trUtil = trUtil;
	}
	
	
	public void setRfcProcessor(CmsRfcProcessor rfcProcessor) {
		this.rfcProcessor = rfcProcessor;
	}

	public void setCmProcessor(CmsCmProcessor cmProcessor) {
		this.cmProcessor = cmProcessor;
	}

	public void setManifestRfcProcessor(ManifestRfcBulkProcessor manifestRfcBulkProcessor) {
		this.manifestRfcProcessor = manifestRfcBulkProcessor;
	}


	public ExecutorService getExecutorService() {
		return executorService;
	}

	public void setExecutorService(ExecutorService executorService) {
		this.executorService = executorService;
	}

	@Override
	public long generateEnvManifest(long envId, String userId, Map<String, String> platModes) {
        long t1 = System.currentTimeMillis();
        String oldThreadName = Thread.currentThread().getName();
        Thread.currentThread().setName(getProcessingThreadName(oldThreadName, envId));
        List<CmsCIRelation> assemblyRels = cmProcessor.getToCIRelations(envId, BASE_REALIZED_IN, null, ACCOUNT_ASSEMBLY);
        CmsCI assembly = getAssembly(envId, assemblyRels);
        CmsCI env = getEnv(envId);
        String nsPath = getManifestNsPath(env);
        //check for openRelease
        check4OpenRelease(env, nsPath);

        logger.info("Created nsId " + trUtil.verifyAndCreateNS(nsPath));

        List<CmsCIRelation> designPlatRels = cmProcessor.getFromCIRelations(assembly.getCiId(), null, "ComposedOf", CATALOG_PLATFORM);

        //we need to reset all pending deletions cis just in case there was one added back
        cmProcessor.resetDeletionsByNs(nsPath);

        // check for edge case scenario when there is new design platform with the same name as old one but different pack
        long releaseId = checkPlatformPackCompliance(designPlatRels, env, nsPath, userId);
        if (releaseId > 0) {
            //stop any processing and return new release id
            return releaseId;
        }

        final CountDownLatch latch = new CountDownLatch(designPlatRels.size());
        List<Future<DesignCIManifestRfcTouple>> submittedFutureTasks = new ArrayList<>();

        for (CmsCIRelation platRelation : designPlatRels) {
            String availMode = getPlatformAvailabiltyMode(platModes, platRelation);
            Future<DesignCIManifestRfcTouple> future = executorService.submit(new ManifestRfcProcessorTask(env, nsPath, userId, availMode, latch, platRelation));
            submittedFutureTasks.add(future);
        }

        boolean allPlatsProcessed;
        try {
            allPlatsProcessed = latch.await(timeoutInMilliSeconds, TimeUnit.MILLISECONDS); //wait for all platform processing threads to finish with timeout of 10 mins
            if (!allPlatsProcessed) {
                logger.error("All platforms not processed within timeout duration of " + timeoutInMilliSeconds);
                throw new TransistorException(CmsError.TRANSISTOR_OPEN_MANIFEST_RELEASE, "Failed to pull latest design for all platform within timeout duration of " + timeoutInMilliSeconds + " millis");
            }
        } catch (InterruptedException ie) {
            for (Future<DesignCIManifestRfcTouple> job : submittedFutureTasks) {
                job.cancel(true);
            }
            throw new TransistorException(CmsError.TRANSISTOR_OPEN_MANIFEST_RELEASE, "Design pull process interrupted. ");
        }
        Map<Long, CmsRfcCI> design2manifestPlatMap = new HashMap<>();
        for (Future<DesignCIManifestRfcTouple> task : submittedFutureTasks) {
            DesignCIManifestRfcTouple touple;
            try {
                touple = task.get();
                processPlatformRfcs(touple.manifestPlatformRfcs, userId);
                CmsRfcCI manifestPlatformRfc = touple.manifestPlatformRfcs.getManifestPlatformRfc();
                logger.info("Finished working on =" + manifestPlatformRfc.getNsPath() + " release id = " + manifestPlatformRfc.getReleaseId());
                design2manifestPlatMap.put(touple.designPlatCI, manifestPlatformRfc);
            } catch (Exception e) {
                logger.error("Error in pulling latest design for all platforms ", e);
                throw new TransistorException(CmsError.TRANSISTOR_OPEN_MANIFEST_RELEASE, "Error in pulling latest design for all platforms ");
            }
        }
		long t2 = System.currentTimeMillis();
        check4MissingServices(design2manifestPlatMap);
		logger.info("Check missing services for " + nsPath + " completed in  " + (System.currentTimeMillis() - t2) + " millis ");
        //now we need to process linkedTo relations
		t2 = System.currentTimeMillis();
        manifestRfcProcessor.processLinkedTo(design2manifestPlatMap, nsPath, userId);
		logger.info("Process linked to " + nsPath + " completed in  " + (System.currentTimeMillis() - t2) + " millis ");

        //now lets delete old existing plats that do not exists in new manifest
		t2 = System.currentTimeMillis();
        manifestRfcProcessor.processDeletedPlatforms(design2manifestPlatMap.values(), env, nsPath, userId);
		logger.info("Process deleted platforms " + nsPath + " completed in  " + (System.currentTimeMillis() - t2) + " millis ");

        //process global variables from design
		t2 = System.currentTimeMillis();
        manifestRfcProcessor.processGlobalVars(assembly.getCiId(), env, nsPath, userId);
		logger.info("Process global vars for " + nsPath + " completed in  " + (System.currentTimeMillis() - t2) + " millis ");
        
        long envReleaseId = populateParentRelease(env, nsPath);
        logger.info("Pull design for  " + nsPath + " completed in  " + (System.currentTimeMillis() - t1) + " millis (releaseId " + envReleaseId + ")");

		List<CmsRelease> manifestReleases = rfcProcessor.getLatestRelease(nsPath, "open");
		if (manifestReleases.size()>0) {
			CmsRelease release = manifestReleases.get(0);
			release.setDescription("Pull design completed in "+(System.currentTimeMillis() - t1) + " ms");
			rfcProcessor.updateRelease(release);
		}
        return envReleaseId;
    }

    /**
     *
     * @param design2manifestPlatMap  design manifest platform map.
     */
    private void check4MissingServices(Map<Long, CmsRfcCI> design2manifestPlatMap) throws TransistorException{
        //get CiIds to be checked for missing services
        Set<Long> manifestPlatformIds = design2manifestPlatMap.entrySet().stream().map(t -> t.getValue().getCiId()).collect(Collectors.toSet());
        cloudUtil.check4missingServices(manifestPlatformIds);
    }

    private String getManifestNsPath(CmsCI env) {
        return env.getNsPath() + "/" + env.getCiName() + "/manifest";
    }

    private void check4OpenRelease(CmsCI env, String nsPath) {
        if (hasOpenManifestRelease(nsPath)) {
            String message = "This environment has an open release. It needs to be discarded or committed before the design pull: "
                    + env.getNsPath() + "/" + env.getCiName();
            logger.info(message);
            throw new TransistorException(CmsError.TRANSISTOR_OPEN_MANIFEST_RELEASE, message);
        }
    }

	private CmsCI getAssembly(long envId, List<CmsCIRelation> assemblyRels) {
		CmsCI assembly = null;
		if (assemblyRels.size() > 0) {
			assembly = assemblyRels.get(0).getFromCi();
		}
		if (assembly == null) {
			String error = "Can not get assembly for envId = " + envId;
			logger.error(error);
			throw new TransistorException(CmsError.TRANSISTOR_CANNOT_GET_ASSEMBLY, error);
		}
		return assembly;
	}

    private String getPlatformAvailabiltyMode(Map<String, String> platModes, CmsCIRelation platRelation) {
        String availMode = null;
        if (platModes != null) {
            availMode = platModes.get(String.valueOf(platRelation.getToCiId()));
            if (availMode != null && availMode.length()==0) {
                availMode="default";
            }
        }
        return availMode;
    }


	private void processPlatformRfcs(ManifestRfcContainer manifestPlatformRfcs, String userId) {
		long  t1= System.currentTimeMillis();
		/***** Handle root RFC and relations ******/
		CmsRfcCI rootRfc;
		if (manifestPlatformRfcs.getRootRfcRelTouple().getRfcCI() != null) {
			rootRfc = rfcProcessor.createAndfetchRfcCINoCheck(manifestPlatformRfcs.getRootRfcRelTouple().getRfcCI(), userId);
		} else if (manifestPlatformRfcs.getManifestPlatformRfc().getRfcAction() != null) {
			rootRfc = rfcProcessor.createAndfetchRfcCINoCheck(manifestPlatformRfcs.getManifestPlatformRfc(), userId);
		} else {
			rootRfc = manifestPlatformRfcs.getManifestPlatformRfc();
		}

		if(rootRfc.getCiState() == null){
			rootRfc.setCiState("default");
		}  
		
		Context context = new Context();                                            
		context.user = userId;
		context.nsPath = rootRfc.getReleaseNsPath();
		context.nsId = rootRfc.getNsId();
		
		for(CmsRfcRelation toRfcRelation : manifestPlatformRfcs.getRootRfcRelTouple().getToRfcRelation()){
			toRfcRelation.setToCiId(rootRfc.getCiId());
			toRfcRelation.setToRfcCi(rootRfc);
			toRfcRelation.setToRfcId(safelong2Long(rootRfc.getRfcId()));
			toRfcRelation.setReleaseId(context.ensureReleaseId());
			toRfcRelation.setNsId(context.nsId);
//			toRfcRelation.setValidated(true);
		//	toRfcRelation.setComments(generateComments(toRfcRelation));
			rfcProcessor.createRfcRelationNoCheck(toRfcRelation, userId);
		}
		
		for(CmsRfcRelation fromRfcRelation : manifestPlatformRfcs.getRootRfcRelTouple().getFromRfcRelation()){
			fromRfcRelation.setFromCiId(rootRfc.getCiId());
			fromRfcRelation.setFromRfcCi(rootRfc);
			fromRfcRelation.setFromRfcId(safelong2Long(rootRfc.getRfcId()));
			fromRfcRelation.setReleaseId(context.ensureReleaseId());
			fromRfcRelation.setNsId(context.nsId);
//			fromRfcRelation.setValidated(true);
			rfcProcessor.createRfcRelationNoCheck(fromRfcRelation, userId);
		}
		
		/**Handle DependsOn and other pack relations ***/
		for(ManifestRfcRelationTriplet rfcRelTriplet : manifestPlatformRfcs.getRfcRelTripletList()){
			
			CmsRfcRelation rfcRelation = rfcRelTriplet.getRfcRelation();
			if(rfcRelation.getRfcAction() == null){rfcRelation.setRfcAction("add");}
			
			CmsRfcCI toRfcCI = rfcRelTriplet.getToRfcCI();
			if(toRfcCI != null){
				if(toRfcCI.getRfcAction() == null){toRfcCI.setRfcAction("add");}
				if(toRfcCI.getRfcId() == 0 && toRfcCI.getCiId() == 0){
                    manifestRfcProcessor.setCiId(toRfcCI);
					toRfcCI.setReleaseId(context.ensureReleaseId());
					toRfcCI.setNsId(context.nsId);
					toRfcCI = rfcProcessor.createAndfetchRfcCINoCheck(toRfcCI, userId);
				}
				rfcRelation.setToCiId(toRfcCI.getCiId());
				rfcRelation.setToRfcId(safelong2Long(toRfcCI.getRfcId()));
				rfcRelation.setToRfcCi(toRfcCI);
			}
			
			CmsRfcCI fromRfcCI = rfcRelTriplet.getFromRfcCI();
			if(fromRfcCI != null){
				if(fromRfcCI.getRfcAction() == null){fromRfcCI.setRfcAction("add");}
				
				if(fromRfcCI.getRfcId() == 0 && fromRfcCI.getCiId() == 0){
                    manifestRfcProcessor.setCiId(fromRfcCI);
					fromRfcCI.setReleaseId(context.ensureReleaseId());
					fromRfcCI.setNsId(context.nsId);
					fromRfcCI = rfcProcessor.createAndfetchRfcCINoCheck(fromRfcCI, userId);
				}
				rfcRelation.setFromCiId(fromRfcCI.getCiId());
				rfcRelation.setFromRfcId(safelong2Long(fromRfcCI.getRfcId()));
				rfcRelation.setFromRfcCi(fromRfcCI);
			}
			
			if("manifest.Entrypoint".equals(rfcRelation.getRelationName())){
				rfcRelation.setFromCiId(rootRfc.getCiId());
				rfcRelation.setFromRfcId(safelong2Long(rootRfc.getRfcId()));
				rfcRelation.setFromRfcCi(rootRfc);
			}
			rfcRelation.setReleaseId(context.ensureReleaseId());
//			rfcRelation.setValidated(true);
			rfcRelation.setNsId(context.nsId);

//			manifestRfcProcessor.setCiRelationId(rfcRelation);
			

			rfcProcessor.createRfcRelationNoCheck(rfcRelation, userId);
		}
		

		/**Handle Requires relations ***/
		for(ManifestRootRfcContainer rfcRelTouple:manifestPlatformRfcs.getRfcRelToupleList()){
			CmsRfcCI newRfc;
			if(rfcRelTouple.getRfcCI().getRfcId() == 0){
				rfcRelTouple.getRfcCI().setReleaseId(context.ensureReleaseId());
				rfcRelTouple.getRfcCI().setNsId(context.nsId);
			  newRfc = rfcProcessor.createAndfetchRfcCINoCheck(rfcRelTouple.getRfcCI(), userId);
			}else{
			  newRfc = rfcRelTouple.getRfcCI();
			}
			for(CmsRfcRelation rfcRel : rfcRelTouple.getToRfcRelation()){
				if(rfcRel.getRfcAction() == null){
					rfcRel.setRfcAction("add");
				}
				
				if("manifest.Requires".equals(rfcRel.getRelationName()) && rfcRel.getFromCiId() == 0){
					rfcRel.setFromCiId(rootRfc.getCiId());
					rfcRel.setFromRfcCi(rootRfc);
					rfcRel.setFromRfcId(safelong2Long(rootRfc.getRfcId()));
				}
				rfcRel.setToCiId(newRfc.getCiId());
				rfcRel.setToRfcId(safelong2Long(newRfc.getRfcId()));
				rfcRel.setToRfcCi(newRfc);
				rfcRel.setReleaseId(context.ensureReleaseId());
//				rfcRel.setValidated(true);
				rfcRel.setNsId(context.nsId);
				rfcProcessor.createRfcRelationNoCheck(rfcRel, userId);
			}
			for(CmsRfcRelation rfcRel : rfcRelTouple.getFromRfcRelation()){
				if(rfcRel.getRfcAction() == null){
					rfcRel.setRfcAction("add");
				}
				
				if(rfcRel.getToCiId() == 0){
					rfcRel.setToCiId(rootRfc.getCiId());
					rfcRel.setToRfcCi(rootRfc);
					rfcRel.setToRfcId(safelong2Long(rootRfc.getRfcId()));
				}
				rfcRel.setFromCiId(newRfc.getCiId());
				rfcRel.setFromRfcId(safelong2Long(newRfc.getRfcId()));
				rfcRel.setFromRfcCi(newRfc);
				rfcRel.setReleaseId(context.ensureReleaseId());
//				rfcRel.setValidated(true);
				rfcRel.setNsId(context.nsId);
				rfcProcessor.createRfcRelationNoCheck(rfcRel, userId);
			}
			
		}
		
		for(CmsRfcCI rfc:manifestPlatformRfcs.getRfcList()){
			rfc.setReleaseId(context.ensureReleaseId());
			rfc.setNsId(context.nsId);
			rfcProcessor.createRfcCINoCheck(rfc, userId);
		}
		
		for(CmsRfcRelation rfcRelation:manifestPlatformRfcs.getRfcRelationList()){
			if(rfcRelation.getFromCiId() == 0){
				//if("base.Consumes".equals(rfcRelation.getRelationName())){
					rfcRelation.setFromCiId(rootRfc.getCiId());
					rfcRelation.setFromRfcCi(rootRfc);
					rfcRelation.setFromRfcId(safelong2Long(rootRfc.getRfcId()));
//				}else{
//					rfcRelation.setFromCiId(rootRfc.getCiId());
//					rfcRelation.setFromRfcCi(rootRfc);
//				}
			}
			if(rfcRelation.getRfcAction() == null){
				rfcRelation.setRfcAction("add");
			}
			rfcRelation.setReleaseId(context.ensureReleaseId());
//			rfcRelation.setValidated(true);
			rfcRelation.setNsId(context.nsId);
			rfcProcessor.createRfcRelationNoCheck(rfcRelation, userId);
		}
		
		for(long delCiId:manifestPlatformRfcs.getDeleteCiIdList()){
			ManifestRfcBulkProcessor.Context manifestContext = manifestRfcProcessor.new Context();
			manifestContext.user = userId;
			manifestContext.nsPath = rootRfc.getReleaseNsPath();
			manifestContext.processed = new ArrayList<>();
			manifestRfcProcessor.requestCiDeleteCascadeNoRelsRfcs(delCiId, 0, manifestContext);
		}
		
		for(CmsCIRelation delRelation:manifestPlatformRfcs.getDeleteRelationList()){
			cmRfcMrgProcessor.requestRelationDelete(delRelation.getCiRelationId(), userId);
		}
		long  t2= System.currentTimeMillis();
		logger.info(" processPlatformRfcs  "+ manifestPlatformRfcs.getManifestPlatformRfc().getNsPath() +" completed in  "+(t2-t1)  );
	}

	private Long safelong2Long(long rfcId) {
		return rfcId==0?null:rfcId;
	}

	private long checkPlatformPackCompliance(List<CmsCIRelation> designPlatRels , CmsCI env, String nsPath, String userId) {
		
		List<CmsCIRelation> manifestPlatRels = cmProcessor.getFromCIRelations(env.getCiId(), MANIFEST_COMPOSED_OF,null, MANIFEST_PLATFORM);
		
		Map<String,String> manifestPlatPacks = new HashMap<>(manifestPlatRels.size());
		for (CmsCIRelation manifestRel : manifestPlatRels) {
			CmsCI plat = manifestRel.getToCi();
			String key = getPlatNameAndVersion(plat);
			String value = getSourcePackVersion(plat);
			manifestPlatPacks.put(key, value);
		}
		
		long newReleaseId = 0;
		
		for (CmsCIRelation designRel : designPlatRels) {
			CmsCI dPlat = designRel.getToCi();
			String key = getPlatNameAndVersion(dPlat);
			String value = getSourcePackVersion(dPlat);
			if (manifestPlatPacks.containsKey(key) && !value.equals(manifestPlatPacks.get(key))) {
				String platNsPath = nsPath + "/" + dPlat.getCiName() + "/" + dPlat.getAttribute("major_version").getDfValue();
				List<CmsRfcCI> mPlats = cmRfcMrgProcessor.getDfDjCi(platNsPath, MANIFEST_PLATFORM, dPlat.getCiName(), "dj");
				if (mPlats.size()>0) {
					newReleaseId = manifestRfcProcessor.deleteManifestPlatform(mPlats.get(0), userId);
				}
			}
			
		}
		return newReleaseId;
	}

    private String getPlatNameAndVersion(CmsCI plat) {
        return plat.getCiName() + ":" + plat.getAttribute("major_version").getDjValue();
    }

    private String getSourcePackVersion(CmsCI plat) {
    	// pack version should be in semver format like 1.0.1 but in legacy packs it could be still just single number
    	// if the major version is different - we need to remove the old platform, 
    	// if only minor and patch version are different we can do update inline
    	String[] packVersion = plat.getAttribute("version").getDjValue().split("\\.");
        return plat.getAttribute("source").getDjValue() +
                 ":" + plat.getAttribute("pack").getDjValue() +
                 ":" + packVersion[0];
    }

    private boolean hasOpenManifestRelease(String nsPath) {
		List<CmsRelease> manReleases = rfcProcessor.getLatestRelease(nsPath, null);
		return manReleases.size() > 0 && "open".equals(manReleases.get(0).getReleaseState());
	}

	private long populateParentRelease(CmsCI env, String nsPath) {
		long releaseId = 0;
		//if we got new release lets populate parent releaseid with latest design
		CmsRelease manifestRelease = null;
		List<CmsRelease> manifestReleases = rfcProcessor.getLatestRelease(nsPath, "open");
		if (manifestReleases.size()>0) {
			manifestRelease = manifestReleases.get(0);
		} else {
			List<CmsRelease> closedManifestReleases = rfcProcessor.getLatestRelease(nsPath, "closed");
			if (closedManifestReleases.size()>0) {
				manifestRelease = closedManifestReleases.get(0);
			}	
		}
		
		if (manifestRelease != null) {
			releaseId = manifestRelease.getReleaseId();
			List<CmsRelease> designReleases = rfcProcessor.getLatestRelease(env.getNsPath(), "closed");
			if (designReleases.size() > 0) {
				CmsRelease designRelease = designReleases.get(0);
				manifestRelease.setParentReleaseId(designRelease.getReleaseId());
				rfcProcessor.updateRelease(manifestRelease);
			}
		}
		
		return releaseId;
	}
	
	@Override
	public long activatePlatform(long platId, String userId) {
		return manifestRfcProcessor.setPlatformActive(platId, userId);
	}
	
	private CmsCI getEnv(long envId) {
		return cmProcessor.getCiById(envId);
	}

	@Override
	public void updateCloudAdminStatus(long cloudId, long envId,
			String adminstatus, String userId) {
		manifestRfcProcessor.updateCloudAdminStatus(cloudId, envId, adminstatus, userId);
	}


	@Override
	public long disablePlatforms(Set<Long> platformIds, String userId) {
		long releaseId = 0;
		for (long manifestPlatformId : platformIds) {
			releaseId = manifestRfcProcessor.disablePlatform(manifestPlatformId, userId);
		}
		return releaseId;
	}


	@Override
	public long enablePlatforms(Set<Long> platformIds, String userId) {
		long releaseId = 0;
		cloudUtil.check4missingServices(platformIds);
		for (long manifestPlatformId : platformIds) {
			releaseId = manifestRfcProcessor.enablePlatform(manifestPlatformId, userId);
		}
		return releaseId;
	}


	@Override
	public long updateEnvClouds(long envId, List<CmsCIRelation> cloudRels, String userId) {
		//for now we will handle just new clouds
		List<CmsCIRelation> existingCloudRels = cmProcessor.getFromCIRelationsNaked(envId, BASE_CONSUMES, ACCOUNT_CLOUD);
		Set<Long> existingCloudIds = new HashSet<>();
		for (CmsCIRelation rel : existingCloudRels) {
			existingCloudIds.add(rel.getToCiId());
		}
		
		boolean needUpdate = false;
		for (CmsCIRelation requestRel : cloudRels) {
			if (!existingCloudIds.contains(requestRel.getToCiId())) {
				// this is new cloud lets add env->cloud rel
				cmProcessor.createRelation(requestRel);
				needUpdate = true;
			} else {
				cmProcessor.updateRelation(requestRel);
				existingCloudIds.remove(requestRel.getToCiId());
			}
		}
		if (!existingCloudIds.isEmpty()) {
			//looks like we need to delete some clouds
			//first lets see if we have any open releases
			processCloudDeletions(envId, existingCloudIds);
		}
		
		if (needUpdate) {
			CmsCI env = getEnv(envId);
			String nsPath = getManifestNsPath(env);
			List<CmsRfcRelation> compOfRels = cmRfcMrgProcessor.getFromCIRelations(envId, MANIFEST_COMPOSED_OF, MANIFEST_PLATFORM, "dj");
			for (CmsRfcRelation compOfRel : compOfRels) {
				CmsRfcCI platform = compOfRel.getToRfcCi();
				String platNs = platform.getNsPath();
				manifestRfcProcessor.processClouds(env, platform, platNs, nsPath, userId, null, null,null);
				Set<String> missingSrvs = cloudUtil.getMissingServices(platform.getCiId());
				if (missingSrvs.size() > 0) {
					logger.info(">>>>> Not all services available for platform: " + platform.getCiName() + ", the missing services: " + missingSrvs.toString());
					manifestRfcProcessor.disablePlatform(platform.getCiId(), userId);
				}
				logger.info("Done working on platform " + platform.getCiName());
			}
			return populateParentRelease(env, nsPath);
		} else {
			return 0;
		}
		
	}

	private void processCloudDeletions(long envId, Set<Long> cloudsToRemove) {
		//looks like we need to delete some clouds
		//first lets see if we have any open releases
		CmsCI env = getEnv(envId);
		String manifestNsPath = getManifestNsPath(env);
		String bomNsPath = env.getNsPath() + "/" + env.getCiName() + "/bom";
		List<CmsRelease> manifestOpenReleases = rfcProcessor.getReleaseBy3(manifestNsPath, null, RELEASE_STATE_OPEN);
		if (manifestOpenReleases.size()>0) {
			//throw exception on open release
			throw new TransistorException(CmsError.TRANSISTOR_OPEN_MANIFEST_RELEASE, "There is open release in this environment, you have to commit or discard before deleteing the clouds!");
		}
		List<CmsRelease> bomOpenReleases = rfcProcessor.getReleaseBy3(bomNsPath, null, RELEASE_STATE_OPEN);
		if (bomOpenReleases.size()>0) {
			//throw exception on open release
			throw new TransistorException(CmsError.TRANSISTOR_OPEN_BOM_RELEASE, "There is open BOM release in this environment, you have to deploy or discard before deleteing the clouds!");
		}
		//if we still here lets check if there are any boms deployed to the clouds that user tries to remove
		for (long cloidId : cloudsToRemove) {
			long deployedToRelsCount = cmProcessor.getRelationCounts(BASE_DEPLOYED_TO, bomNsPath, true, null, cloidId, null, null, null, null).get("count");
			if (deployedToRelsCount > 0) {
				//throw exception that there are active boms
				throw new TransistorException(CmsError.TRANSISTOR_BOM_INSTANCES_EXIST, "There are deployed instances in the cloud, please put the cloud in offline mode for every platform and deploy!");
			}
		}
		//if everything cool, let remove Consumes relations from platforms and env
		for (long cloidId : cloudsToRemove) {
			List<CmsCIRelation> platformRels = cmProcessor.getFromCIRelationsNaked(envId, MANIFEST_COMPOSED_OF, MANIFEST_PLATFORM);
			for (CmsCIRelation platRel : platformRels) {
				List<CmsCIRelation> platformCloudRels = cmProcessor.getFromToCIRelations(platRel.getToCiId(), BASE_CONSUMES, cloidId);
				for (CmsCIRelation platCloudRel : platformCloudRels) {
					cmProcessor.deleteRelation(platCloudRel.getCiRelationId());
				}
			}		
			//now lets remove env consumes rel
			List<CmsCIRelation> envCloudRels = cmProcessor.getFromToCIRelations(envId, BASE_CONSUMES, cloidId);
			for (CmsCIRelation cloudRel : envCloudRels) {
				cmProcessor.deleteRelation(cloudRel.getCiRelationId());
			}
		}	
	}
	
	@Override
	public void updatePlatformCloud(CmsRfcRelation cloudRel, String userId) {
		manifestRfcProcessor.updatePlatfomCloudStatus(cloudRel, userId);
	}

	@Override
	public void rollbackOpenRelease(long envId) {
		CmsCI env = getEnv(envId);
		String nsPath = getManifestNsPath(env);
		List<CmsRelease> list = rfcProcessor.getLatestRelease(nsPath, "open");
		if (list.size()>0) {
			rfcProcessor.deleteRelease(list.get(0).getReleaseId());
		}
	}

	private class ManifestRfcProcessorTask implements Callable<DesignCIManifestRfcTouple> {
        private final CmsCI env;
        private final String nsPath;
        private final String userId;
        private final String availMode;
        private final CountDownLatch countDownLatch;
        private CmsCIRelation platRelation;
 
 
        ManifestRfcProcessorTask(CmsCI env, String nsPath, String userId, String availMode,
				CountDownLatch countDownLatch, CmsCIRelation platRelation) {
			super();
			this.env = env;
			this.nsPath = nsPath;
			this.userId = userId;
			this.availMode = availMode;
			this.countDownLatch = countDownLatch;
			this.platRelation = platRelation;
		}

		public DesignCIManifestRfcTouple call() {
			String oldThreadName = Thread.currentThread().getName();
            try {
				Thread.currentThread().setName(getProcessingThreadName(oldThreadName,env.getCiId()));
				ManifestRfcContainer manifestPlatformRfcs =  manifestRfcProcessor.processPlatform(platRelation.getToCi(), env, nsPath, userId, availMode);
            	return new DesignCIManifestRfcTouple(platRelation.getToCi().getCiId(),manifestPlatformRfcs);
            } catch (Exception e){
            	e.printStackTrace();
            	logger.error(e, e);
            	throw e;
			} finally {
				countDownLatch.countDown();
				Thread.currentThread().setName(oldThreadName);
            }
        }
    }
	
	private class DesignCIManifestRfcTouple {
		private Long designPlatCI;
		//private CmsRfcCI manifestRfc;
		private ManifestRfcContainer manifestPlatformRfcs;
		
		public DesignCIManifestRfcTouple(Long designPlatCI, ManifestRfcContainer manifestPlatformRfcs) {
			super();
			this.designPlatCI = designPlatCI;
			this.manifestPlatformRfcs = manifestPlatformRfcs;
		}
		
	}

    class Context {
        String user;
        String nsPath;
        Long releaseId;
        public long nsId;

        private Long ensureReleaseId() {
            if (releaseId == null) {
                releaseId = rfcProcessor.getOpenReleaseIdByNs(nsPath, null, user);
            }
            return releaseId;
        }
    }


    public void setCloudUtil(CloudUtil cloudUtil) {
		this.cloudUtil=cloudUtil;
	}
}
