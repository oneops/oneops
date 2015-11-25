package com.oneops.cms.ns.dal;

import java.util.List;

import com.oneops.cms.ns.domain.CmsNamespace;


/**
 * The Interface NSMapper.
 */
public interface NSMapper {

	void createNamespace(CmsNamespace ns);
	CmsNamespace getNamespace(String nsPath);
	List<CmsNamespace> getNamespaceLike(String nsPath);
	CmsNamespace getNamespaceById(long nsId);
	void deleteNamespace(String nsPath);
	void lockNamespace(String nsPath);
	void vacuumNamespace(long nsId);

}
