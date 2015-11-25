package com.oneops.cms.transmitter.dal;

import java.util.List;

import com.oneops.cms.transmitter.domain.CMSEventRecord;

import org.apache.ibatis.annotations.Param;

public interface EventMapper {
	List<CMSEventRecord> getEvents();
	List<CMSEventRecord> getCiEvents();
	void removeEvent(long eventId);
	void removeCiEvent(long eventId);
	int getQueueBacklog();
	int getCiEventsQueueBacklog();
    boolean good2run(@Param("lockName") String lockName, @Param("processId") String processId, @Param("staleTimeout") int staleTimeout);
	void removeLock(@Param("lockName") String lockName, @Param("processId") String processId);
}
