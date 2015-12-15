/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
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
