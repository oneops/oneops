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
package com.oneops.cms.transmitter;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.ops.dal.OpsMapper;
import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.dj.dal.DJDpmtMapper;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.transmitter.dal.EventMapper;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.CMSEventRecord;
import com.oneops.cms.transmitter.domain.EventSource;
import java.util.List;
import org.apache.ibatis.session.SqlSession;

public class ControllerEventReader extends BaseEventReader {
    private static final String PUB_LOCK = "PUBLISHER_LOCK";


    public int getBacklog(EventMapper eventMapper) {
        return eventMapper.getQueueBacklog();
    }


    public void removeEvent(long eventId, EventMapper eventMapper) {
        eventMapper.removeEvent(eventId);
    }


    public List<CMSEventRecord> getEvents(EventMapper eventMapper) {
        return eventMapper.getEvents();
    }

    public String getLockName() {
        return PUB_LOCK;
    }

    protected CMSEvent populateEvent(CMSEventRecord record) {
        switch (EventSource.toEventSource(record.getSourceName())) {
            case deployment:
                return getDeployment(record);
            case release:
                return getRelease(record);
            case opsprocedure:
                return getOpsProcedure(record);
            default:
                logger.warn("Bad event source " + record.getSourceName());
                return null;
        }
    }

    private CMSEvent getDeployment(CMSEventRecord record) {
        CMSEvent event;

        try (SqlSession session = sqlsf.openSession()) {
            DJDpmtMapper dpmtMapper = session.getMapper(DJDpmtMapper.class);
            CmsDeployment dpmt = dpmtMapper.getDeployment(record.getSourcePk());
            event = new CMSEvent();
            event.setEventId(record.getEventId());
            event.addHeaders("source", "deployment");
            event.addHeaders("clazzName", "Deployment");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(dpmt);
        }
        return event;
    }

    private CMSEvent getRelease(CMSEventRecord record) {
        CMSEvent event;

        try (SqlSession session = sqlsf.openSession()) {
            DJMapper djMapper = session.getMapper(DJMapper.class);
            CmsRelease release = djMapper.getReleaseById(record.getSourcePk());
            if (release != null) {
                long rfcCount = djMapper.countCiRfcByReleaseId(release.getReleaseId());
                release.setCiRfcCount(rfcCount);
            }
            event = new CMSEvent();
            event.setEventId(record.getEventId());
            event.addHeaders("source", "release");
            event.addHeaders("clazzName", "Release");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(release);
        }
        return event;
    }

    private CMSEvent getOpsProcedure(CMSEventRecord record) {
        CMSEvent event;
        try (SqlSession session = sqlsf.openSession()) {
            OpsMapper opsMapper = session.getMapper(OpsMapper.class);
            CmsOpsProcedure procedure = opsMapper.getCmsOpsProcedure(record.getSourcePk());
            if (procedure.getNsPath() == null) {
                if (procedure.getCiId() > 0) {
                    CIMapper ciMapper = session.getMapper(CIMapper.class);
                    CmsCI ci = ciMapper.getCIById(procedure.getCiId());
                    if (ci != null) {
                        procedure.setNsPath(ci.getNsPath());
                    }
                }
            }
            event = new CMSEvent();
            event.setEventId(record.getEventId());
            event.addHeaders("source", "opsprocedure");
            event.addHeaders("clazzName", "OpsProcedure");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(procedure);
        }
        return event;
    }
}
