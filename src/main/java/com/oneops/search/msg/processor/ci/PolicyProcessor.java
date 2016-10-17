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
package com.oneops.search.msg.processor.ci;


import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;

import static com.oneops.search.msg.processor.MessageProcessor.GSON_ES;

/**
 * This class processes account.Policy CI messages and registers percolators from them.
 *
 * @author ranand
 */

@Service
public class PolicyProcessor implements CISImpleProcessor {
    private Logger logger = Logger.getLogger(this.getClass());
    private Client client;
    private Indexer indexer;


    @Autowired
    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }


    @Autowired
    public void setClient(Client client) {
        this.client = client;
    }


    public void process(CmsCISimple policyCI) {
        String id = String.valueOf(policyCI.getCiId());
        try {

            client.prepareIndex(indexer.getIndexName(), ".percolator", id)
                    .setSource(getQuery(policyCI.getCiAttributes().get("query"), policyCI))
                    .setRefresh(true) // Needed when the query shall be available immediately
                    .execute().actionGet();
            logger.info("registered percolator for " + policyCI.getCiName() + " ci:" + id);
        } catch (Exception e) {
            logger.error("Error in registering percolator for " + policyCI.getCiName() + " ci:" + id, e);
        }
    }

    private XContentBuilder getQuery(String queryString, CmsCISimple ciSimple) {
        XContentBuilder docBuilder = null;
        try {
            docBuilder = XContentFactory.jsonBuilder().startObject();
            docBuilder.field("query").startObject().field("query_string").startObject().field("query", queryString).endObject().endObject();
            docBuilder.rawField("ci", GSON_ES.toJson(ciSimple).getBytes());
            docBuilder.endObject();

            logger.info(docBuilder.string());
        } catch (IOException e) {
            logger.error("Error in forming percolator query ", e);
        }
        return docBuilder;
    }

}
