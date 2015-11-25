package com.oneops.transistor.service;

import java.util.List;
import java.util.Map;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.dj.domain.CmsRfcRelation;

@Transactional
public interface ManifestManager {
	public long generateEnvManifest(long envId, String userId, Map<String, String> platModes);
	public long activatePlatform(long platId, String userId);
	public long disablePlatform(long platId, String userId);
	public long enablePlatform(long platId, String userId);
	public void updateCloudAdminStatus(long cloudId, long envId, String adminstatus, String userId);
	public long updateEnvClouds(long envId, List<CmsCIRelation> cloudRels, String userId);
	public void updatePlatformCloud(CmsRfcRelation cloudRel, String userId);
}