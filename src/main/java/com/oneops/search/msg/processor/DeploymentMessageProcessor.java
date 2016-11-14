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
package com.oneops.search.msg.processor;

import com.oneops.cms.dj.domain.CmsDeployment;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsDeploymentSearch;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.processor.es.ESMessageProcessor;
import org.apache.log4j.Logger;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

@Service
public class DeploymentMessageProcessor implements MessageProcessor {
    private static Logger logger = Logger.getLogger(DeploymentMessageProcessor.class);
    private static final String DPMT_STATE_ACTIVE = "active";
    private static final String DPMT_STATE_CANCELED = "canceled";
    private static final String DPMT_STATE_FAILED = "failed";
    private static final String DPMT_STATE_PAUSED = "paused";
    private static final String DPMT_STATE_PENDING = "pending";
    private static final String DPMT_STATE_COMPLETE = "complete";
    private static final String DEPLOYMENT = "deployment";


    @Autowired
    private Client client;

    @Autowired
    Indexer indexer;

    @Override
    public void processMessage(String message, String msgType, String msgId) {
        CmsDeployment od = GSON.fromJson(message, CmsDeployment.class);
        String dpmtEventMessage = GSON_ES.toJson(od);
        indexer.indexEvent(DEPLOYMENT, dpmtEventMessage);
        CmsDeploymentSearch deployment = new CmsDeploymentSearch();
        BeanUtils.copyProperties(od, deployment);
        deployment = processDeploymentMsg(deployment);
        message = GSON_ES.toJson(deployment);
        indexer.index(String.valueOf(deployment.getDeploymentId()), DEPLOYMENT, message);
    }

    /**
     * @param deployment
     * @return
     */
    private CmsDeploymentSearch processDeploymentMsg(CmsDeploymentSearch deployment) {

        CmsDeploymentSearch esDeployment = null;
        try {
            esDeployment = fetchDeploymentRecord(deployment.getDeploymentId());

            String now = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN).format(new Date());
            if (isFinalState(deployment.getDeploymentState())) {

                if (esDeployment != null && DPMT_STATE_CANCELED.equalsIgnoreCase(deployment.getDeploymentState())) {
                    if (DPMT_STATE_PAUSED.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setPausedEndTS(now);
                        esDeployment.setPausedDuration(esDeployment.getPausedDuration()
                                + calcDiff(esDeployment.getPausedEndTS(), esDeployment.getPausedStartTS()));

                    } else if (DPMT_STATE_PENDING.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setPendingEndTS(now);
                        esDeployment.setPendingDuration(esDeployment.getPendingDuration()
                                + calcDiff(esDeployment.getPendingEndTS(), esDeployment.getPendingStartTS()));
                    } else if (DPMT_STATE_FAILED.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setFailedEndTS(now);
                        esDeployment.setFailedDuration(esDeployment.getFailedDuration()
                                + calcDiff(esDeployment.getFailedEndTS(), esDeployment.getFailedStartTS()));
                    }
                    esDeployment.setDeploymentState(deployment.getDeploymentState());
                    esDeployment.setTotalTime(diff(esDeployment.getCreated()));
                } else if (esDeployment != null && DPMT_STATE_COMPLETE.equalsIgnoreCase(deployment.getDeploymentState())) {
                    esDeployment.setActiveEndTS(now);

                    double activeDuration = esDeployment.getActiveDuration() +
                            calcDiff(esDeployment.getActiveEndTS(), esDeployment.getActiveStartTS());
                    esDeployment.setActiveDuration(activeDuration);
                    esDeployment.setDeploymentState(deployment.getDeploymentState());
                    esDeployment.setTotalTime(diff(esDeployment.getCreated()));
                }

            } else if (DPMT_STATE_ACTIVE.equalsIgnoreCase(deployment.getDeploymentState())) {

                if (esDeployment != null) {
                    esDeployment.setActiveStartTS(now);
                    if (DPMT_STATE_FAILED.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setRetryCount(esDeployment.getRetryCount() + 1);
                        esDeployment.setFailedEndTS(now);
                        esDeployment.setFailedDuration(esDeployment.getFailedDuration() +
                                calcDiff(esDeployment.getFailedEndTS(), esDeployment.getFailedStartTS()));
                        esDeployment.setDeploymentState(deployment.getDeploymentState());
                    } else if (DPMT_STATE_PAUSED.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setPausedEndTS(now);

                        double pausedDuration = esDeployment.getPausedDuration() +
                                calcDiff(esDeployment.getPausedEndTS(), esDeployment.getPausedStartTS());
                        esDeployment.setPausedDuration(pausedDuration);
                        esDeployment.setDeploymentState(deployment.getDeploymentState());
                    } else if (DPMT_STATE_PENDING.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setPendingEndTS(now);
                        esDeployment.setPendingDuration(esDeployment.getPendingDuration() +
                                calcDiff(esDeployment.getPendingEndTS(), esDeployment.getPendingStartTS()));
                        esDeployment.setDeploymentState(deployment.getDeploymentState());
                    }
                    updateTotalTime(esDeployment);
                } else {
                    deployment.setActiveStartTS(now);
                }
            } else {
                if (esDeployment == null) {
                    esDeployment = deployment;
                }
                if (DPMT_STATE_PENDING.equalsIgnoreCase(deployment.getDeploymentState())) {
                    esDeployment.setPendingStartTS(now);
                    esDeployment.setDeploymentState(deployment.getDeploymentState());
                } else if (DPMT_STATE_PAUSED.equalsIgnoreCase(deployment.getDeploymentState())) {
                    esDeployment.setPauseCnt(deployment.getPauseCnt() + 1);
                    esDeployment.setPausedStartTS(now);
                    if (DPMT_STATE_ACTIVE.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setActiveEndTS(now);
                        esDeployment.setActiveDuration(esDeployment.getActiveDuration() +
                                calcDiff(esDeployment.getActiveEndTS(), esDeployment.getActiveStartTS()));
                    }
                    esDeployment.setDeploymentState(deployment.getDeploymentState());
                    updateTotalTime(esDeployment);
                } else if (DPMT_STATE_FAILED.equalsIgnoreCase(deployment.getDeploymentState())) {
                    esDeployment.setFailureCnt(esDeployment.getFailureCnt() + 1);
                    esDeployment.setFailedStartTS(now);
                    if (DPMT_STATE_ACTIVE.equalsIgnoreCase(esDeployment.getDeploymentState())) {
                        esDeployment.setActiveEndTS(now);
                        esDeployment.setActiveDuration(esDeployment.getActiveDuration() +
                                calcDiff(esDeployment.getActiveEndTS(), esDeployment.getActiveStartTS()));
                    }
                    esDeployment.setDeploymentState(deployment.getDeploymentState());
                    updateTotalTime(esDeployment);
                }
            }

        } catch (Exception e) {
            logger.error("Error in processing deployment message " + e.getMessage());
            e.printStackTrace();
        }

        return esDeployment != null ? esDeployment : deployment;
    }

    private double calcDiff(String failedEndTS, String failedStartTS) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
        try {
            return (simpleDateFormat.parse(failedEndTS).getTime() - simpleDateFormat.parse(failedStartTS).getTime()) / 1000.0;
        } catch (ParseException ignore) {
            return 0;
        }
    }

    private double diff(Date date) {
        return ((System.currentTimeMillis()) - (date.getTime())) / 1000.0;
    }

    private CmsDeploymentSearch fetchDeploymentRecord(long deploymentId) {
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withIndices("cms-2*")
                .withTypes(DEPLOYMENT).withQuery(queryStringQuery(String.valueOf(deploymentId)).field("deploymentId"))
                .build();

        List<CmsDeploymentSearch> esDeploymentList = indexer.getTemplate().queryForList(searchQuery, CmsDeploymentSearch.class);
        if (esDeploymentList.size() > 1) {
            cleanOldDeployment(esDeploymentList.get(0).getDeploymentId());
        }
        return !esDeploymentList.isEmpty() ? esDeploymentList.get(0) : null;
    }


    /**
     * Update the total time taken by the deployment before it reaches a terminal state
     *
     * @param esDeployment
     */
    private void updateTotalTime(CmsDeploymentSearch esDeployment) {
        double tt = esDeployment.getActiveDuration() + esDeployment.getFailedDuration() + esDeployment.getPausedDuration() + esDeployment.getPendingDuration();
        esDeployment.setTotalTime(tt);
    }


    private boolean isFinalState(String state) {
        return DPMT_STATE_COMPLETE.equalsIgnoreCase(state) || DPMT_STATE_CANCELED.equalsIgnoreCase(state);
    }


    /**
     * Handles the edge case condition where deployments can spawn to multiple weeks.
     * Cleans duplicate deployments from deployment indices as cms indices are created weekly.
     *
     * @param deploymentId
     */
    private void cleanOldDeployment(Long deploymentId) {

        SearchResponse response = client.prepareSearch("cms-2*")
                .setTypes(DEPLOYMENT)
                .setQuery(queryStringQuery(String.valueOf(deploymentId)).field("deploymentId"))
                .execute()
                .actionGet();

        //Skip the latest week deployment id and deletes all others
        Arrays.stream(response.getHits().getHits())
                .sorted((hit1, hit2) -> (hit1.getIndex().compareTo(hit2.getIndex())) * -1)
                .skip(1).forEach(hit -> {
            indexer.getTemplate().delete(hit.getIndex(), DEPLOYMENT, hit.getId());
            logger.info("Deleted duplicate deployment " + hit.getId() + " in index " + hit.getIndex());
        });
    }

}
