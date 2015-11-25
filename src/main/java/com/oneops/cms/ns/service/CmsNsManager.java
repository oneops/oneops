package com.oneops.cms.ns.service;

import java.util.List;

import com.oneops.cms.ns.domain.CmsNamespace;

/**
 * The Interface CmsNsManager.
 */
public interface CmsNsManager {
	CmsNamespace createNs(CmsNamespace ns);
	CmsNamespace getNs(String nsPath);
	List<CmsNamespace> getNsLike(String nsPath);
	CmsNamespace getNsById(long nsId);
	void deleteNs(String nsPath);
	void deleteNsById(long nsId);
	void lockNs(String nsPath);
}
