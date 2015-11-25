package com.oneops.cms.util.dal;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.oneops.cms.util.domain.CmsStuckDpmt;
import com.oneops.cms.util.domain.CmsVar;

public interface UtilMapper {
	boolean acquireLock(@Param("lockName") String lockName, @Param("processId") String processId, @Param("staleTimeout") int staleTimeout);
	void releaseLock(@Param("lockName") String lockName, @Param("processId") String processId);
	void updateCmSimpleVar(@Param("name") String varName,@Param("value") String varValue,@Param("updatedBy") String updatedBy);
	CmsVar getCmSimpleVar(@Param("name") String varName);
	List<CmsStuckDpmt> getCmsStuckDpmts();
	List<CmsStuckDpmt> getInProgressStuckDpmts();
	List<CmsStuckDpmt> getPausedStuckDpmts();
}
