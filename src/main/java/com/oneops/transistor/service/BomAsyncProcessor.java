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

import java.util.Set;

import org.apache.log4j.Logger;

import com.oneops.cms.exceptions.CmsBaseException;
import com.oneops.cms.util.CmsError;
import com.oneops.transistor.exceptions.TransistorException;

public class BomAsyncProcessor {

	static Logger logger = Logger.getLogger(BomAsyncProcessor.class);
	
	private BomManager bomManager;
	private FlexManager flexManager;
	private EnvSemaphore envSemaphore;
	
	public void setFlexManager(FlexManager flexManager) {
		this.flexManager = flexManager;
	}

	public void setBomManager(BomManager bomManager) {
		this.bomManager = bomManager;
	}

	public void setEnvSemaphore(EnvSemaphore envSemaphore) {
		this.envSemaphore = envSemaphore;
	}

	public void compileEnv(long envId, String userId, Set<Long> excludePlats, String desc, boolean autodeploy, boolean commit) {
		envSemaphore.lockEnv(envId, EnvSemaphore.LOCKED_STATE);
		buildBom(envId, userId, excludePlats, desc, autodeploy, commit);
	}

	public void processFlex(long envId, long flexRelId, int step, boolean scaleUp) {
		envSemaphore.lockEnv(envId, EnvSemaphore.LOCKED_STATE);
		processFlexAsync(envId, flexRelId, step, scaleUp);
	}
	
	
	private void buildBom(final long envId, final String userId, final Set<Long> excludePlats, final String desc, final boolean deploy, final boolean commit) {
        final Runnable bomBuilder = new Runnable() {
                public void run() { 
                	String envMsg = null;
                	try {
                		long startTime = System.currentTimeMillis();
                		if (deploy) {
                			bomManager.generateAndDeployBom(envId, userId, excludePlats, desc, commit);
                		} else {
                			bomManager.generateBom(envId, userId, excludePlats, desc, commit);
                		}
                		long timeTaken = Math.round((System.currentTimeMillis() - startTime )/1000);
                		envMsg = EnvSemaphore.SUCCESS_PREFIX + " Generation time taken: " + timeTaken + " seconds." ;
                	} catch (Exception e) {
                		logger.error("Exception in buildBom", e);
                		e.printStackTrace();
            			envMsg = EnvSemaphore.ERROR_PREFIX + e.getMessage();
            			throw new TransistorException(CmsError.TRANSISTOR_BOM_GENERATION_FAILED, envMsg);
					} finally {
                		envSemaphore.unlockEnv(envId, envMsg);
                	}
                }
            };
        Thread t = new Thread(bomBuilder);
        t.start();
    }

	private void processFlexAsync(final long envId, final long flexRelId, final int step, final boolean scaleUp) {
        final Runnable flexRunner = new Runnable() {
                public void run() { 
                	String envMsg = null;
                	try {
                		flexManager.processFlex(flexRelId, step, scaleUp, envId);
                		envMsg = "";
                	} catch (CmsBaseException e) {
            			logger.error("CmsBaseException in processFlexAsync", e);
            			e.printStackTrace();
            			envMsg = EnvSemaphore.ERROR_PREFIX + e.getMessage();
                	} finally {
                		envSemaphore.unlockEnv(envId, envMsg);
                	}
                }
            };
        Thread t = new Thread(flexRunner);
        t.start();
    }
	
	public void resetEnv(long envId) {
		envSemaphore.resetEnv(envId);
	}
}
