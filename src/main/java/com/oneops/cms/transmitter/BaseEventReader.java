package com.oneops.cms.transmitter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oneops.cms.transmitter.dal.EventMapper;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.CMSEventRecord;
import com.oneops.cms.util.dal.UtilMapper;
import com.oneops.cms.util.domain.CmsVar;
import org.apache.ibatis.exceptions.PersistenceException;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.log4j.Logger;


import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/*******************************************************************************
 *
 *   Copyright 2016 Walmart, Inc.
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
public abstract class BaseEventReader {
    private static final long LOG_PUB_INACTIVE = 60000;
    private static final String CMS_PUB_STATUS = "IS_CMS_PUB_ACTIVE";
    private static final int PROCESS_TIMEOUT_SECONDS = 30;
    Logger logger = Logger.getLogger(this.getClass());
    private String processId;
    SqlSessionFactory sqlsf;
    private long eventTs;
    private Boolean iamActiveProcess = null;
    private LoadingCache<String, Boolean> varCache;
    private int varCacheTTLInSeconds = 30;
    private int varCacheMaxSize = 10;


    public void setSessionFactory(SqlSessionFactory sf) {
        this.sqlsf = sf;
        varCache = CacheBuilder.newBuilder()
                .maximumSize(varCacheMaxSize)
                .expireAfterAccess(varCacheTTLInSeconds, TimeUnit.SECONDS)
                .build(
                        new CacheLoader<String, Boolean>() {
                            public Boolean load(String key) {
                                return getBooleanVariableFromDB(key);
                            }
                        });
    }

    public void setVarCacheTTLInSeconds(int varCacheTTLInSeconds) {
        this.varCacheTTLInSeconds = varCacheTTLInSeconds;
    }

    public void setVarCacheMaxSize(int varCacheMaxSize) {
        this.varCacheMaxSize = varCacheMaxSize;
    }

    public void init() {
        this.processId = UUID.randomUUID().toString();
        logger.info(">>>>>>>>>>>>>"+this.getClass().getSimpleName()+" process id = " + this.processId);
    }

    public void cleanup() {
        try (SqlSession session = sqlsf.openSession()) {
            session.getMapper(EventMapper.class).removeLock(getLockName(), processId);
            session.commit();
        }
    }


    public int getQueueBacklog() {
        try (SqlSession session = sqlsf.openSession()) {
            return getBacklog(session.getMapper(EventMapper.class));
        }
    }
    /**
     * Checks if CMSPublisher is active
     *
     * @return true if active
     */
    public boolean isCmsPubActive() {
        if (varCache.getUnchecked(CMS_PUB_STATUS)) {
            eventTs = 0; //reset timer
            return true;
        } else {
            if (eventTs == 0) {
                eventTs = System.currentTimeMillis();
            } else if ((System.currentTimeMillis() - eventTs) > LOG_PUB_INACTIVE) {
                logger.warn("CMSPublisher is suspended. No events will be published.");
                eventTs = 0; //reset timer
            }
            return false;
        }
    }


    private boolean getBooleanVariableFromDB(String varName){
        try (SqlSession session = sqlsf.openSession()) {
            UtilMapper utilMapper = session.getMapper(UtilMapper.class);
            CmsVar pubStatus = utilMapper.getCmSimpleVar(varName);
            return pubStatus==null || Boolean.TRUE.toString().equals(pubStatus.getValue());
        }
    }

    public void removeEvent(long eventId) {

        try (SqlSession session = sqlsf.openSession()) {
            removeEvent(eventId, session.getMapper(EventMapper.class));
            session.commit();
        }
    }

    public abstract void removeEvent(long eventId, EventMapper eventMapper);

    public List<CMSEvent> getEvents() {

        SqlSession session = null;
        List<CMSEvent> cmsEvents = new ArrayList<>();

        try {

            session = sqlsf.openSession();
            EventMapper eventMapper = session.getMapper(EventMapper.class);

            if (!eventMapper.good2run(getLockName(), processId, PROCESS_TIMEOUT_SECONDS)) {
                if (iamActiveProcess == null || iamActiveProcess) {
                    logger.info(">>>>>>>>>Other process has a lock " + getLockName() + ", will wait");
                    iamActiveProcess = false;
                }
                session.commit(true);
                return cmsEvents;
            } else {
                if (iamActiveProcess == null || !iamActiveProcess) {
                    logger.info(">>>>>>>>>Acquired lock " + getLockName() + ", I'm the active process now");
                    iamActiveProcess = true;
                }
                session.commit(true);
            }

            if (isCmsPubActive()) {
                for (CMSEventRecord record : getEvents(eventMapper)) {
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

    public abstract int getBacklog(EventMapper evenMapper);

    public abstract List<CMSEventRecord> getEvents(EventMapper eventMapper);

    public abstract String getLockName();

    protected abstract CMSEvent populateEvent(CMSEventRecord record);
}
