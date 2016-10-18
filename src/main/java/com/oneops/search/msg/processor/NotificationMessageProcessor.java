package com.oneops.search.msg.processor;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsCISearch;
import com.oneops.search.domain.CmsNotificationSearch;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

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

@Service
public class NotificationMessageProcessor implements MessageProcessor {
    private Logger logger = Logger.getLogger(this.getClass());
    private Indexer indexer;


    @Autowired
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }


    @Override
    public void processMessage(String message, String msgType, String msgId) {
        NotificationMessage notification = GSON.fromJson(message, NotificationMessage.class);
        CmsNotificationSearch notificationSearch = new CmsNotificationSearch();
        BeanUtils.copyProperties(notification, notificationSearch);
        notificationSearch.setPayload(notification.getPayload());
        String notificationTS = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN).format(new Date(notification.getTimestamp()));
        notificationSearch.setTs(notificationTS);
        if ("ops".equals(notification.getSource())) {
            processNotificationMsg(notificationSearch);
        }
        String notificationMsg = GSON_ES.toJson(notificationSearch);
        indexer.index(null, "notification", notificationMsg);
    }

    /**
     * Update ops notifications to CIs
     *
     * @param notificationMsg notification message to process
     */
    private void processNotificationMsg(CmsNotificationSearch notificationMsg) {
        String id = String.valueOf(notificationMsg.getCmsId());

        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices(indexer.getIndexName())
                .withTypes("ci").withQuery(queryStringQuery(id).field("ciId"))
                .build();

        List<CmsCISearch> ciList = indexer.getTemplate().queryForList(searchQuery, CmsCISearch.class);

        if (!ciList.isEmpty()) {
            CmsCISearch ciSearch = ciList.get(0);
            if ("bom.Compute".equals(ciSearch.getCiClassName())) {
                String hypervisor = ciSearch.getCiAttributes().get("hypervisor");
                if (hypervisor != null) {
                    notificationMsg.setHypervisor(hypervisor);
                }
            }
            ciSearch.setOps(notificationMsg);
            indexer.index(id, "ci", GSON_ES.toJson(ciSearch));
            logger.info("updated ops notification for ci id::" + id);
        } else {
            logger.warn("ci record not found for id::" + id);
        }
    }


}
