package com.oneops.search.msg.processor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oneops.cms.simple.domain.CmsWorkOrderSimple;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.elasticsearch.action.get.GetResponse;
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
        try {
            CmsWorkOrderSimple wos = GSON.fromJson(message, CmsWorkOrderSimple.class);
            if ("pending".equalsIgnoreCase(wos.getDpmtRecordState()) || "inprogress".equalsIgnoreCase(wos.getDpmtRecordState())) return;
            // we don't need/want to store inprogress and pending messages in ES, in case it arrived out of order to avoid overwriting complete messages we care about
        } catch (Exception ignore){
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readValue(message, JsonNode.class);

            JsonNode rfcCi = rootNode.get("rfcCi");
            // For delete WOs, try to find and preserve last offering matched, so the proper cost can be calculated and accounted for.
            if (rfcCi != null && rfcCi.get("rfcAction") != null && rfcCi.get("rfcAction").asText().equals("delete") && rfcCi.get("ciClassName").asText().startsWith("bom.")) {
                Map<String, Object> payLoad = null;
                GetResponse response = client.prepareGet(indexer.getIndexName(), "ci", "" + rfcCi.get("ciId")).get();
                if (response.isExists()) {
                    Map<String, Object> map = (Map<String, Object>) response.getSource().get("workorder");
                    if (map != null) {
                        payLoad = (Map<String, Object>) map.get("payLoad");
                    }
                }

                if (payLoad == null) {
                    SearchResponse response2 = client.prepareSearch("cms-2*")
                            .setTypes("workorder")
                            .setQuery(queryStringQuery("rfcCi.ciId:" + rfcCi.get("ciId")))
                            .addSort("searchTags.responseDequeTS", SortOrder.DESC)
                            .setSize(1)
                            .execute()
                            .actionGet();
                    if (response2.getHits().getHits().length > 0) {
                        payLoad = (Map<String, Object>) response2.getHits().getHits()[0].getSource().get("payLoad");
                    }
                }


                if (payLoad != null && payLoad.get("offerings") != null) {
                    JsonNode offeringsContent = mapper.readValue(GSON.toJson(payLoad.get("offerings")), JsonNode.class);
                    ((ObjectNode) rootNode.get("payLoad")).put("offerings", offeringsContent);
                    message = mapper.writeValueAsString(rootNode);
                }
            }
        } catch (Exception e)
        {
            logger.warn(e, e);
        }
        indexer.index(msgId, msgType, message);
    }

}
