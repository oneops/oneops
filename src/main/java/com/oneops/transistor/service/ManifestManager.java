/*******************************************************************************
 * Copyright 2015 Walmart, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.oneops.transistor.service;

import com.oneops.cms.cm.domain.CmsCIRelation;
import com.oneops.cms.dj.domain.CmsRfcRelation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Transactional
public interface ManifestManager {
    long generateEnvManifest(long envId, String userId, Map<String, String> platModes);

    long activatePlatform(long platId, String userId);

    long disablePlatforms(Set<Long> platId, String userId);

    long enablePlatforms(Set<Long> platId, String userId);

    void updateCloudAdminStatus(long cloudId, long envId, String adminstatus, String userId);

    long updateEnvClouds(long envId, List<CmsCIRelation> cloudRels, String userId);

    void updatePlatformCloud(CmsRfcRelation cloudRel, String userId);

    default String getProcessingThreadName(String threadName, long envId) {
        return threadName + "-env-" + envId;
    }

}
