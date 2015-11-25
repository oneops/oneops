package com.oneops.transistor.service;

import java.util.Set;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BomManager {
	public long generateBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit);
	public long generateAndDeployBom(long envId, String userId, Set<Long> excludePlats, String desc, boolean commit);
}
