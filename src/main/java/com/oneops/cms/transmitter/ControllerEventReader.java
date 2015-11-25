package com.oneops.cms.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

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
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.CmsVar;

public class ControllerEventReader {
	
	private SqlSessionFactory sqlsf;
	private static Logger logger = Logger.getLogger(ControllerEventReader.class);
	private static final int PROCESS_TIMEOUT_SECONDS = 30;
	private static final long LOG_PUB_INACTIVE = 60000;
	private static final String PUB_LOCK  = "PUBLISHER_LOCK";
	private static final String CMS_PUB_STATUS = "IS_CMS_PUB_ACTIVE";
	
    private String processId;
    private Boolean iamActiveProcess = null;
    private long eventTs;

	public void setSessionFactory(SqlSessionFactory sf) {
		this.sqlsf = sf;
	}

    public void init() {
        this.processId = UUID.randomUUID().toString();
        logger.info(">>>>>>>>>>>>>ControllerEventPublisher process id = " + this.processId);
    }

    public void cleanup() {
		SqlSession session = sqlsf.openSession();
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
			eventMapper.removeLock(PUB_LOCK, processId);
			session.commit();
		} finally {
			session.close();
		}
    }
    
    public int getQueueBacklog() {
		SqlSession session = sqlsf.openSession();
		int backLog = 0;
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
			backLog = eventMapper.getQueueBacklog();
		} finally {
			session.close();
		}
		return backLog;
	}
    
	public void removeEvent(long eventId) {
		SqlSession session = sqlsf.openSession();
		
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
		    eventMapper.removeEvent(eventId);
			session.commit();
		} finally {
			session.close();
		}
	}
	
	public List<CMSEvent> getEvents() {
		
		SqlSession session = null;
		List<CMSEvent> cmsEvents = new ArrayList<CMSEvent>();

		try {
		
			session = sqlsf.openSession();
			EventMapper eventMapper = session.getMapper(EventMapper.class);	

            if ( !eventMapper.good2run(PUB_LOCK, processId, PROCESS_TIMEOUT_SECONDS)) {
            	if (iamActiveProcess == null || iamActiveProcess) {
            		logger.info(">>>>>>>>>Other process has a lock "+ PUB_LOCK + ", will wait");
            		iamActiveProcess = false;
            	}
            	session.commit(true);
            	return cmsEvents;
            } else {
            	if (iamActiveProcess == null || !iamActiveProcess) {
            		logger.info(">>>>>>>>>Acquired lock "+ PUB_LOCK + ", I'm the active process now");
            		iamActiveProcess = true;
            	}
            	session.commit(true);
            }
			
    		if (isCmsPubActive()) {
	            for (CMSEventRecord record : eventMapper.getEvents()) {
					CMSEvent event = populateEvent(record);
					if (event != null) {
						cmsEvents.add(event);
					}
				}
    		}
		} catch (Exception e) {
			if (e instanceof PersistenceException) {
				logger.error("Problem connecting to the postgres db, I will try reconnect...");
				logger.error(e.getMessage());
			} else {
				e.printStackTrace();
				throw new RuntimeException(e);
			}
		} finally {
			if (session != null) {
				session.close();
			}
		}
		
		return cmsEvents;
		
	}
	
	private CMSEvent populateEvent(CMSEventRecord record) {
		switch (EventSource.toEventSource(record.getSourceName()))
		{
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
		CMSEvent event =  null;

		SqlSession session = sqlsf.openSession();
		try {
			DJDpmtMapper dpmtMapper = session.getMapper(DJDpmtMapper.class);
			CmsDeployment dpmt = dpmtMapper.getDeployment(record.getSourcePk());
			event = new CMSEvent();
			event.setEventId(record.getEventId());
			event.addHeaders("source", "deployment");
			event.addHeaders("clazzName", "Deployment");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
			event.setPayload(dpmt);
		} finally {
			session.close();
		}
		return event;
	}

	private CMSEvent getRelease(CMSEventRecord record) {
        CMSEvent event =  null;

        SqlSession session = sqlsf.openSession();
        try {
	        DJMapper djMapper = session.getMapper(DJMapper.class);
	        CmsRelease release = djMapper.getReleaseById(record.getSourcePk());
	        if(release!=null){
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
        } finally {
            session.close();
        }
        return event;
	}

    private CMSEvent getOpsProcedure(CMSEventRecord record) {
        CMSEvent event = null;
        SqlSession session = sqlsf.openSession();
        try {
            OpsMapper opsMapper = session.getMapper(OpsMapper.class);
            CmsOpsProcedure procedure = opsMapper.getCmsOpsProcedure(record.getSourcePk());
            event = new CMSEvent();
            event.setEventId(record.getEventId());
            event.addHeaders("source", "opsprocedure");
            event.addHeaders("clazzName", "OpsProcedure");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(procedure);
        } finally {
            session.close();
        }
        return event;
    }

	
	/**
	 * Checks if CMSPublisher is active
	 * @return
	 */
	public boolean isCmsPubActive() {
		SqlSession session = sqlsf.openSession();
		
		try {
			UtilMapper utilMapper = session.getMapper(UtilMapper.class);
			CmsVar pubStatus = utilMapper.getCmSimpleVar(CMS_PUB_STATUS);
			if (pubStatus != null) {
				if (Boolean.FALSE.toString().equals(pubStatus.getValue())) {
					if(eventTs == 0){
						eventTs = System.currentTimeMillis();
					}
					else if((System.currentTimeMillis() - eventTs) > LOG_PUB_INACTIVE){
						logger.warn("CMSPublisher is suspended. No events will be published.");
						eventTs = 0; //reset timer
					}
					return false;
				} else {
					eventTs = 0; //reset timer
					return true;
				}
			}
		} finally {
            session.close();
        }
		return true;
	}

}
