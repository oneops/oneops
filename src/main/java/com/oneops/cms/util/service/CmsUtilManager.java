package com.oneops.cms.util.service;

import org.springframework.transaction.annotation.Transactional;

/**
 * The Interface CmsUtilManager.
 */

@Transactional
public interface CmsUtilManager {
	boolean acquireLock(String lockName, String processId, int staleTimeout);
	void releaseLock(String lockName, String processId);
	boolean refreshLock(String lockName, String processId);
}
