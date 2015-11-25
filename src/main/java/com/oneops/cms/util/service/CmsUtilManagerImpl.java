package com.oneops.cms.util.service;

/**
 * The Class CmsUtilManagerImpl implements CmsUtilManager.
 */
public class CmsUtilManagerImpl implements CmsUtilManager {

	private CmsUtilProcessor uProcessor;
	
	public void setuProcessor(CmsUtilProcessor uProcessor) {
		this.uProcessor = uProcessor;
	}
	
	@Override
	public boolean acquireLock(String lockName, String processId,
			int staleTimeout) {
		return uProcessor.acquireLock(lockName, processId, staleTimeout);
	}

	@Override
	public void releaseLock(String lockName, String processId) {
		uProcessor.releaseLock(lockName, processId);
	}

	@Override
	public boolean refreshLock(String lockName, String processId) {
		return uProcessor.refreshLock(lockName, processId);
	}
}
