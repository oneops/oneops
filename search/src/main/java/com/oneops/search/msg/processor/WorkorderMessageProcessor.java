package com.oneops.search.msg.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class WorkorderMessageProcessor implements MessageProcessor {

    private static Logger logger = Logger.getLogger(WorkorderMessageProcessor.class);
    @Autowired
    private Indexer indexer;
    @Autowired
    private Client client;

    @Override
    public void processMessage(String message, String msgType, String msgId) {

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = null;
        try {
            rootNode = mapper.readValue(message, JsonNode.class);

            JsonNode rfcCi = rootNode.get("rfcCi");
            if (rfcCi != null && rfcCi.get("rfcAction") != null && rfcCi.get("rfcAction").asText().equals("delete")) {
                SearchResponse response = client.prepareSearch("cms-2*")
                        .setTypes("workorder")
                        .setQuery(queryStringQuery("rfcCi.ciId:" + rfcCi.get("ciId") + " AND dpmtRecordState:complete"))
                        .addSort("searchTags.responseDequeTS", SortOrder.DESC)
                        .setSize(1)
                        .execute()
                        .actionGet();

                if (response.getHits().getHits().length > 0) {
                    Map<String, Object> map = response.getHits().getHits()[0].getSource();
                    Map<String, Object> payLoad = (Map<String, Object>) map.get("payLoad");
                    if (payLoad != null && payLoad.get("offerings") != null) {
                        JsonNode offeringsContent = mapper.readValue(GSON.toJson(payLoad.get("offerings")), JsonNode.class);
                        ((ObjectNode)rootNode.get("payLoad")).put("offerings", offeringsContent);
                        message = mapper.writeValueAsString(rootNode);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn(e, e);
        }
        indexer.index(msgId, msgType, message);
    }

}
