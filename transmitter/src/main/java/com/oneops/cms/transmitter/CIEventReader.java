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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.oneops.cms.cm.dal.CIMapper;
import com.oneops.cms.cm.domain.CmsCI;
import com.oneops.cms.cm.domain.CmsCIAttribute;
import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.cm.domain.CmsCIRelationAttribute;
import com.oneops.cms.dj.dal.DJMapper;
import com.oneops.cms.dj.domain.CmsRfcCI;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import com.oneops.cms.simple.domain.CmsCISimpleWithTags;
import com.oneops.cms.transmitter.dal.EventMapper;
import com.oneops.cms.transmitter.domain.CMSEvent;
import com.oneops.cms.transmitter.domain.CMSEventRecord;
import com.oneops.cms.transmitter.domain.EventSource;
import com.oneops.cms.util.CmsUtil;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang.StringUtils;
import org.apache.ibatis.session.SqlSession;

public class CIEventReader extends BaseEventReader {

    private static final String CI_EVENT_PUB_LOCK = "CI_EVENT_PUBLISHER_LOCK";
    private static final String EVENT_TYPE_DELETE = "delete";
    private static final String CI_CLASS_ORG = "account.Organization";
    private static final String ATTR_KEY_TAGS = "tags";

    private CmsUtil cmsUtil = new CmsUtil();
    private Gson gson = new Gson();

    private long orgCacheTtlInMins = 30;
    private long orgCacheMaxSize = 1000;

    private Cache<String, Map<String, String>> orgCache;

    public void init() {
        super.init();
        orgCache = CacheBuilder.newBuilder()
            .maximumSize(orgCacheMaxSize)
            .expireAfterWrite(orgCacheTtlInMins, TimeUnit.MINUTES)
            .build();
    }

    @Override
    public int getBacklog(EventMapper eventMapper) {
        return eventMapper.getCiEventsQueueBacklog();
    }


    @Override
    public void removeEvent(long eventId, EventMapper eventMapper) {
        eventMapper.removeCiEvent(eventId);
    }

    @Override
    public List<CMSEventRecord> getEvents(EventMapper eventMapper) {
        return eventMapper.getCiEvents();
    }

    @Override
    public String getLockName() {
        return CI_EVENT_PUB_LOCK;
    }

    @Override
    protected CMSEvent populateEvent(CMSEventRecord record) {
        switch (EventSource.toEventSource(record.getSourceName())) {
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
        CMSEvent event = new CMSEvent();
        event.setEventId(record.getEventId());
        event.addHeaders("source", EventSource.namespace.toString());
        event.addHeaders("clazzName", "Namespace");
        event.addHeaders("action", record.getEventType());
        event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
        event.setPayload(null);
        return event;
    }


    private CMSEvent getCi(CMSEventRecord record) {
        CMSEvent event = new CMSEvent();
        event.setEventId(record.getEventId());
        event.addHeaders("action", record.getEventType());
        try (SqlSession session = sqlsf.openSession()) {
            if (EVENT_TYPE_DELETE.equals(record.getEventType())) {
                //TODO add method to read ci from the log table
                event.addHeaders("source", "cm_ci");
                event.addHeaders("clazzName", "");
                event.setPayload(null);
                event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            } else {
                event.addHeaders("source", "cm_ci_new");
                CIMapper ciMapper = session.getMapper(CIMapper.class);
                CmsCI ci = ciMapper.getCIById(record.getSourcePk());
                if (ci != null) {
                    List<CmsCIAttribute> attrs = ciMapper.getCIAttrs(ci.getCiId());
                    attrs.forEach(ci::addAttribute);
                    CmsCISimpleWithTags simpleCI = cmsUtil.cmsCISimpleWithTags(ci, "df");
                    addTags(simpleCI, ciMapper);
                    event.addHeaders("clazzName", ci.getCiClassName());
                    event.setPayload(simpleCI);
                } else {
                    logger.warn("Can not get ci object for id=" + record.getSourcePk());
                }
            }
        }
        return event;
    }

    private void addTags(CmsCISimpleWithTags ci, CIMapper ciMapper) {
        String org = ci.getOrg();
        if (StringUtils.isNotBlank(org)) {
            Map<String, String> tagMap = null;
            try {
                tagMap = orgCache.get(org, () -> getOrgDetails(org, ciMapper));
            } catch (ExecutionException e) {
                logger.error("Exception while getting org tags from cache ", e);
            }
            if (tagMap == null) {
                tagMap = Collections.emptyMap();
            }
            ci.setTags(tagMap);
        }
    }

    private Map<String, String> getOrgDetails(String org, CIMapper ciMapper) {
        Map<String, String> tagMap = null;
        List<CmsCI> ciList = ciMapper.getCIby3("/", CI_CLASS_ORG, null, org);
        if (ciList != null && !ciList.isEmpty()) {
            CmsCI orgCi = ciList.get(0);
            List<CmsCIAttribute> attrs = ciMapper.getCIAttrs(orgCi.getCiId());
            if (attrs != null) {
                Optional<CmsCIAttribute> optional = attrs.stream().filter(a -> ATTR_KEY_TAGS.equals(a.getAttributeName())).findFirst();
                if (optional.isPresent()) {
                    CmsCIAttribute tagAttr = optional.get();
                    String tagValue = tagAttr.getDfValue();
                    if (StringUtils.isNotBlank(tagValue)) {
                        try {
                            tagMap = gson.fromJson(tagValue, Map.class);
                        } catch (JsonSyntaxException e) {
                            logger.error("exception while parsing tag attribute for org " + org, e);
                        }
                    }
                }
            }
        }
        if (tagMap == null) {
            tagMap = Collections.emptyMap();
        }
        return tagMap;
    }

    private CMSEvent getCiRel(CMSEventRecord record) {
        CMSEvent event = new CMSEvent();
        event.setEventId(record.getEventId());
        event.addHeaders("source", "cm_ci_rel");
        event.addHeaders("action", record.getEventType());
        try (SqlSession session = sqlsf.openSession()) {
            CIMapper ciMapper = session.getMapper(CIMapper.class);
            CmsCIRelation relation = ciMapper.getCIRelation(record.getSourcePk());
            if (relation != null) {
                List<CmsCIRelationAttribute> attrs = ciMapper.getCIRelationAttrs(relation.getCiRelationId());
                attrs.forEach(relation::addAttribute);
                event.addHeaders("clazzName", relation.getRelationName());
            }
            event.addHeaders("sourceId", String.valueOf(record.getSourcePk()));
            event.setPayload(relation);
        }
        return event;
    }

    private CMSEvent getRfcCi(CMSEventRecord record) {
        CMSEvent event;

        try (SqlSession session = sqlsf.openSession()) {
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
        }
        return event;
    }

    private CMSEvent getRfcRelation(CMSEventRecord record) {
        CMSEvent event;

        try (SqlSession session = sqlsf.openSession()) {
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
        }
        return event;
    }

    private CmsRfcRelation populateRfcRelationAttributes(CmsRfcRelation relationRfc, DJMapper djMapper) {
        if (relationRfc == null) return null;
        djMapper.getRfcRelationAttributes(relationRfc.getRfcId()).forEach(relationRfc::addAttribute);
        return relationRfc;
    }

    private CmsRfcCI populateRfcCIAttributes(CmsRfcCI ciRfc, DJMapper djMapper) {
        if (ciRfc == null) return null;
        djMapper.getRfcCIAttributes(ciRfc.getRfcId()).forEach(ciRfc::addAttribute);
        return ciRfc;
    }

    public void setOrgCacheTtlInMins(long orgCacheTtlInMins) {
        this.orgCacheTtlInMins = orgCacheTtlInMins;
    }

    public void setOrgCacheMaxSize(long orgCacheMaxSize) {
        this.orgCacheMaxSize = orgCacheMaxSize;
    }
}
