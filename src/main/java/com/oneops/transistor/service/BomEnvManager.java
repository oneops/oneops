package com.oneops.transistor.service;

import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface BomEnvManager {
	public void takeEnvSnapshot(long envId);
	public void cleanEnvBom(long envId);
	public long discardEnvBom(long envId);
	public long discardEnvManifest(long envId);
}
