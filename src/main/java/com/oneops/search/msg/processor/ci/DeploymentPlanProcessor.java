package com.oneops.search.msg.processor.ci;

import com.oneops.cms.dj.domain.CmsRelease;
import com.oneops.cms.simple.domain.CmsCISimple;
import com.oneops.cms.util.CmsConstants;
import com.oneops.search.domain.CmsDeploymentPlan;
import com.oneops.search.domain.CmsReleaseSearch;
import com.oneops.search.msg.index.Indexer;
import com.oneops.search.msg.processor.ci.CISImpleProcessor;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.oneops.search.msg.processor.MessageProcessor.GSON_ES;

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
public class DeploymentPlanProcessor implements CISImpleProcessor{
    private Logger logger = Logger.getLogger(this.getClass());
    @Autowired
    private Indexer indexer;

    
    public void process(CmsCISimple ci) {
        CmsDeploymentPlan deploymentPlan;

        try {
            int genTime = extractTimeTaken(ci.getComments()).intValue();
            CmsRelease release = fetchReleaseRecord(ci.getNsPath() + "/" + ci.getCiName() + "/bom",
                    ci.getUpdated(), genTime);
            if (release != null) {
                deploymentPlan = new CmsDeploymentPlan();
                deploymentPlan.setPlanGenerationTime(genTime);
                deploymentPlan.setCreatedBy(ci.getCreatedBy());
                deploymentPlan.setCiId(ci.getCiId());
                deploymentPlan.setCreated(release.getCreated());
                deploymentPlan.setCreatedBy(release.getCreatedBy());
                deploymentPlan.setCiClassName(ci.getCiClassName());
                deploymentPlan.setReleaseName(release.getReleaseName());
                deploymentPlan.setReleaseId(release.getReleaseId());
                deploymentPlan.setId(String.valueOf(ci.getCiId()) + String.valueOf(release.getReleaseId()));
                deploymentPlan.setCiRfcCount(release.getCiRfcCount());
                deploymentPlan.setCommitedBy(release.getCommitedBy());
                deploymentPlan.setNsId(release.getNsId());
                deploymentPlan.setNsPath(release.getNsPath());
                indexer.index(deploymentPlan.getId(), "plan", GSON_ES.toJson(deploymentPlan));
            }
        } catch (Exception e) {
            logger.error("Exception in processing deployment message: " + ExceptionUtils.getMessage(e));
        }
    }

    private static Double extractTimeTaken(String comments) {
        try {
            String timeTaken = comments.substring("SUCCESS:Generation time taken: ".length(), comments.indexOf(" seconds.")).trim();
            double planGentime = Double.parseDouble(timeTaken);
            return planGentime == 0 ? 1 : planGentime;
        } catch (Exception e) {
            return 1d; // return 1 second
        }
    }

    private CmsRelease fetchReleaseRecord(String nsPath, Date ts, int genTime) throws InterruptedException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(CmsConstants.SEARCH_TS_PATTERN);
        Thread.sleep(3000);
        SearchQuery latestRelease = new NativeSearchQueryBuilder()
                .withIndices("cms-2*")
                .withTypes("release").withFilter(
                        FilterBuilders.andFilter(
                                FilterBuilders.queryFilter(QueryBuilders.termQuery("nsPath.keyword", nsPath)),
                                FilterBuilders.queryFilter(QueryBuilders.rangeQuery("created").
                                        from(simpleDateFormat.format(DateUtils.addMinutes(ts, -(genTime + 10)))).
                                        to(simpleDateFormat.format(ts))))).
                        withSort(SortBuilders.fieldSort("created").order(SortOrder.DESC)).build();

        List<CmsReleaseSearch> ciList = indexer.getTemplate().queryForList(latestRelease, CmsReleaseSearch.class);
        if (!ciList.isEmpty()) {
            return ciList.get(0);
        }
        throw new RuntimeException("Cant find bom release for deployment plan generation event");
    }
}
