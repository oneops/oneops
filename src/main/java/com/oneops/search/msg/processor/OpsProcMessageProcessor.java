package com.oneops.search.msg.processor;

import com.oneops.cms.cm.ops.domain.CmsOpsProcedure;
import com.oneops.cms.cm.ops.domain.OpsProcedureState;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsOpsProcedureSearch;
import com.oneops.search.msg.index.Indexer;
import org.apache.log4j.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class OpsProcMessageProcessor implements MessageProcessor {

    private static final String OPSPROCEDURE = "opsprocedure";
    private static Logger logger = Logger.getLogger(OpsProcMessageProcessor.class);
    @Autowired
    private Indexer indexer;


    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsOpsProcedure op = GSON.fromJson(message, CmsOpsProcedure.class);
        String procEventMessage = GSON_ES.toJson(op);
        indexer.indexEvent(OPSPROCEDURE, procEventMessage);
        CmsOpsProcedureSearch procedure = new CmsOpsProcedureSearch();
        BeanUtils.copyProperties(op, procedure);
        procedure = processOpsProcMsg(procedure);

        message = GSON_ES.toJson(procedure);
        indexer.index(String.valueOf(procedure.getProcedureId()), OPSPROCEDURE, message);
    }

    /**
     * @param procedure
     * @return
     */
    private CmsOpsProcedureSearch processOpsProcMsg(CmsOpsProcedureSearch procedure) {

        CmsOpsProcedureSearch esProcedure = null;
        try {
            esProcedure = fetchprocedureRecord(procedure.getProcedureId());


            String now = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN).format(new Date());
            if (isFinalState(procedure.getProcedureState().getName())) {

                if (esProcedure != null && OpsProcedureState.canceled.getName().equalsIgnoreCase(procedure.getProcedureState().getName())) {
                    if (OpsProcedureState.failed.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())) {
                        esProcedure.setFailedEndTS(now);
                        esProcedure.setFailedDuration(esProcedure.getFailedDuration() +
                                diff(esProcedure.getFailedEndTS(), esProcedure.getFailedStartTS()));
                    }
                    esProcedure.setProcedureState(procedure.getProcedureState());
                    esProcedure.setTotalTime(diff(esProcedure.getCreated()));
                } else if (esProcedure != null && OpsProcedureState.complete.getName().equalsIgnoreCase(procedure.getProcedureState().getName())) {
                    if (OpsProcedureState.active.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())) {
                        esProcedure.setActiveEndTS(now);
                        esProcedure.setActiveDuration(esProcedure.getActiveDuration() +
                                diff(esProcedure.getActiveEndTS(), esProcedure.getActiveStartTS()));
                    }
                    esProcedure.setProcedureState(procedure.getProcedureState());
                    esProcedure.setTotalTime(diff(esProcedure.getCreated()));
                }

            } else if (OpsProcedureState.active.getName().equalsIgnoreCase(procedure.getProcedureState().getName())) {

                if (esProcedure != null) {
                    esProcedure.setActiveStartTS(now);
                    if (OpsProcedureState.failed.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())) {
                        esProcedure.setRetryCount(esProcedure.getRetryCount() + 1);
                        esProcedure.setFailedEndTS(now);
                        esProcedure.setFailedDuration(esProcedure.getFailedDuration() +
                                diff(esProcedure.getFailedEndTS(), esProcedure.getFailedStartTS()));
                        esProcedure.setProcedureState(procedure.getProcedureState());
                    }
                } else {
                    procedure.setActiveStartTS(now);
                }
            } else if (esProcedure != null && OpsProcedureState.failed.getName().equalsIgnoreCase(procedure.getProcedureState().getName())) {
                esProcedure.setFailureCnt(esProcedure.getFailureCnt() + 1);
                esProcedure.setFailedStartTS(now);
                if (OpsProcedureState.active.getName().equalsIgnoreCase(esProcedure.getProcedureState().getName())) {
                    esProcedure.setActiveEndTS(now);

                    double activeDuration = esProcedure.getActiveDuration() +
                            diff(esProcedure.getActiveEndTS(), esProcedure.getActiveStartTS());
                    esProcedure.setActiveDuration(activeDuration);
                }
                esProcedure.setProcedureState(procedure.getProcedureState());
            }
        } catch (Exception e) {
            logger.error("Error in processing ops-procedure message " + e.getMessage());
        }

        return esProcedure != null ? esProcedure : procedure;
    }

    private double diff(Date date) {
        return ((System.currentTimeMillis()) - (date.getTime())) / 1000.0;
    }


    private double diff(String failedEndTS, String failedStartTS) {
        try {
            SimpleDateFormat format = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
            return (format.parse(failedEndTS).getTime() - format.parse(failedStartTS).getTime()) / 1000.0;
        } catch (ParseException ignore) {
            return 0;
        }
    }


    private CmsOpsProcedureSearch fetchprocedureRecord(long procedureId) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("cms-2*")
                .withTypes(OPSPROCEDURE).withQuery(queryStringQuery(String.valueOf(procedureId)).field("procedureId"))
                .build();

        List<CmsOpsProcedureSearch> esProcedureList = indexer.getTemplate().queryForList(searchQuery, CmsOpsProcedureSearch.class);
        return !esProcedureList.isEmpty() ? esProcedureList.get(0) : null;
    }


    private boolean isFinalState(String state) {
        return OpsProcedureState.complete.getName().equalsIgnoreCase(state) || OpsProcedureState.canceled.getName().equalsIgnoreCase(state);
    }
}
