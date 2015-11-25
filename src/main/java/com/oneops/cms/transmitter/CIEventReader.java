package com.oneops.cms.transmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;

import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRfcAttribute;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.transmitter.dal.EventMapper;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.CMSEventRecord;
import com.oneops.cms.transmitter.domain.EventSource;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.CmsVar;

public class CIEventReader {
	
	private SqlSessionFactory sqlsf;
	private static Logger logger = Logger.getLogger(CIEventReader.class);
	private static final int PROCESS_TIMEOUT_SECONDS = 30;
	private static final long LOG_PUB_INACTIVE = 60000;
	private static final String CI_EVENT_PUB_LOCK  = "CI_EVENT_PUBLISHER_LOCK";
	private static final String CMS_PUB_STATUS = "IS_CMS_PUB_ACTIVE";
	private static final String EVENT_TYPE_DELETE = "delete";
	
    private String processId;
    private Boolean iamActiveProcess = null;
    private long eventTs;

	public void setSessionFactory(SqlSessionFactory sf) {
		this.sqlsf = sf;
	}

    public void init() {
        this.processId = UUID.randomUUID().toString();
        logger.info(">>>>>>>>>>>>>CIEventPublisher process id = " + this.processId);
    }

    public void cleanup() {
		SqlSession session = sqlsf.openSession();
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
			eventMapper.removeLock(CI_EVENT_PUB_LOCK, processId);
			session.commit();
		} finally {
			session.close();
		}
    }
    
    public int getCiEventsQueueBacklog() {
		SqlSession session = sqlsf.openSession();
		int backLog = 0;
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
			backLog = eventMapper.getCiEventsQueueBacklog();
		} finally {
			session.close();
		}
		return backLog;
	}
	
	
	public void removeEvent(long eventId) {
		SqlSession session = sqlsf.openSession();
		
		try {

			EventMapper eventMapper = session.getMapper(EventMapper.class);	
			eventMapper.removeCiEvent(eventId);
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

            if ( !eventMapper.good2run(CI_EVENT_PUB_LOCK, processId, PROCESS_TIMEOUT_SECONDS)) {
            	if (iamActiveProcess == null || iamActiveProcess) {
            		logger.info(">>>>>>>>>Other process has a lock "+ CI_EVENT_PUB_LOCK + ", will wait");
            		iamActiveProcess = false;
            	}
            	session.commit(true);
            	return cmsEvents;
            } else {
            	if (iamActiveProcess == null || !iamActiveProcess) {
            		logger.info(">>>>>>>>>Acquired lock "+ CI_EVENT_PUB_LOCK + ", I'm the active process now");
            		iamActiveProcess = true;
            	}
            	session.commit(true);
            }
			
    		if (isCmsPubActive()) {
	            for (CMSEventRecord record : eventMapper.getCiEvents()) {
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
            case cm_ci:
                return getCi(record);
            case cm_ci_rel:
                return getCiRel(record);
            case rfc_ci:
            	return getRfcCi(record);
            case rfc_relation:
            	return getRfcRelation(record);
            case namespace:
            	return getNsEvent(record);
            default:
		    	logger.warn("Bad event source " + record.getSourceName());
		    	return null;
		}
	}

	private CMSEvent getNsEvent(CMSEventRecord record) {
		CMSEvent event =  null;

		SqlSession session = sqlsf.openSession();
		try {
			event = new CMSEvent();
			event.setEventId(record.getEventId());
			event.addHeaders("source", EventSource.namespace.toString());
			event.addHeaders("clazzName", "Namespace");
            event.addHeaders("action", record.getEventType());
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
			event.setPayload(null);
		} finally {
			session.close();
		}
		return event;
	}

	
    private CMSEvent getCi(CMSEventRecord record) {
        CMSEvent event = new CMSEvent();
        event.setEventId(record.getEventId());
        event.addHeaders("source", "cm_ci");
        event.addHeaders("action", record.getEventType());
        SqlSession session = sqlsf.openSession();
        try {
            if (record.getEventType().equals(EVENT_TYPE_DELETE)) {
            	//TODO add method to read ci from the log table
	            event.addHeaders("clazzName", "");
	            event.setPayload(null);
	            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            } else {
                CIMapper ciMapper = session.getMapper(CIMapper.class);
                CmsCI ci = ciMapper.getCIById(record.getSourcePk());
                if (ci != null) {
	                List<CmsCIAttribute> attrs = ciMapper.getCIAttrs(ci.getCiId());
	                for (CmsCIAttribute attr : attrs) ci.addAttribute(attr);
	                event.addHeaders("clazzName", ci.getCiClassName());
		            event.setPayload(ci);
                } else {
                	logger.warn("Can not get ci object for id=" + record.getSourcePk());
                }
            }
        } finally {
            session.close();
        }
        return event;
    }

    private CMSEvent getCiRel(CMSEventRecord record) {
        CMSEvent event = new CMSEvent();
        event.setEventId(record.getEventId());
        event.addHeaders("source", "cm_ci_rel");
        event.addHeaders("action", record.getEventType());
        SqlSession session = sqlsf.openSession();
        try {
        	CIMapper ciMapper = session.getMapper(CIMapper.class);
        	CmsCIRelation relation = ciMapper.getCIRelation(record.getSourcePk());
        	if(relation != null){
        		List<CmsCIRelationAttribute> attrs = ciMapper.getCIRelationAttrs(relation.getCiRelationId());
        		for (CmsCIRelationAttribute attr : attrs) relation.addAttribute(attr);
        		event.addHeaders("clazzName", relation.getRelationName());
        	}
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(relation);
        } finally {
            session.close();
        }
        return event;
    }
    
	private CMSEvent getRfcCi(CMSEventRecord record) {
        CMSEvent event =  null;

        SqlSession session = sqlsf.openSession();
        try {
	        DJMapper djMapper = session.getMapper(DJMapper.class);
			CmsRfcCI rfcCi = djMapper.getRfcCIById(record.getSourcePk());
			if (rfcCi != null) {
				populateRfcCIAttributes(rfcCi, djMapper);
			}
            event = new CMSEvent();
	    	event.setEventId(record.getEventId());
		    event.addHeaders("source", "rfc_ci");
		    event.addHeaders("clazzName", "RfcCi");
            event.addHeaders("action", record.getEventType());
		    event.setPayload(rfcCi);
        } finally {
            session.close();
        }
        return event;
	}

	private CMSEvent getRfcRelation(CMSEventRecord record) {
        CMSEvent event =  null;

        SqlSession session = sqlsf.openSession();
        try {
	        DJMapper djMapper = session.getMapper(DJMapper.class);
			CmsRfcRelation relationRfc = djMapper.getRfcRelationById(record.getSourcePk());
			if (relationRfc != null) {
				populateRfcRelationAttributes(relationRfc, djMapper);
			}
            event = new CMSEvent();
	    	event.setEventId(record.getEventId());
		    event.addHeaders("source", "rfc_relation");
		    event.addHeaders("clazzName", "RfcRelation");
            event.addHeaders("action", record.getEventType());
		    event.setPayload(relationRfc);
        } finally {
            session.close();
        }
        return event;
	}
	
    private CmsRfcRelation populateRfcRelationAttributes(CmsRfcRelation relationRfc,
			DJMapper djMapper) {
        if (relationRfc == null) return null;
        for(CmsRfcAttribute attr : djMapper.getRfcRelationAttributes(relationRfc.getRfcId())){
        	relationRfc.addAttribute(attr);
        }
        return relationRfc;
	}

	private CmsRfcCI populateRfcCIAttributes(CmsRfcCI ciRfc, DJMapper djMapper) {
        if (ciRfc == null) return null;
        for(CmsRfcAttribute attr : djMapper.getRfcCIAttributes(ciRfc.getRfcId())){
        	ciRfc.addAttribute(attr);
        }
        return ciRfc;
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
