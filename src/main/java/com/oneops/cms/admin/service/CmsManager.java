package com.oneops.cms.admin.service;


import java.util.List;

import org.springframework.transaction.annotation.Transactional;

import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.md.domain.CmsClazz;
import com.oneops.cms.md.domain.CmsRelation;
import com.oneops.cms.util.domain.CmsStuckDpmtCollection;

@Transactional
public interface CmsManager {
	List<CmsClazz> getClazzes();
	CmsClazz getClazz(String clazzName);
	CmsRelation getRelation(String relationName);
	List<CmsCI> getCiList(String nsPath, String className, String ciName);
	CmsCI getCI(long ciId);
	List<CmsCIAttribute> getCIAttributes(long ciId);
	List<CmsCIRelation> getFromRelation(long ciId);
	List<CmsCIRelation> getToRelation(long ciId);
	CmsCIRelation getCIRelation(long relId);
	List<CmsCIRelationAttribute> getCIRelationAttributes(long relId);
	void flushCache();
	CmsStuckDpmtCollection getStuckDpmts();
//	List<CmsStuckDpmt> getInProgressStuckDpmts();
//	List<CmsStuckDpmt> getPausedStuckDpmts();
	
}
