package com.oneops.opamp.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.util.domain.CmsVar;

import java.util.concurrent.TimeUnit;

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
public class CachedEnvPropsProcessor extends EnvPropsProcessor {
    private LoadingCache<String, Boolean> variables;
    private int ttlInSeconds = 30;
    private int maxSize = 100;

    public void setTtlInSeconds(int ttlInSeconds) {
        this.ttlInSeconds = ttlInSeconds;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public void setCmProcessor(CmsCmProcessor cmProcessor) {
        super.setCmProcessor(cmProcessor);
        variables = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttlInSeconds, TimeUnit.SECONDS)
                .build(
                        new CacheLoader<String, Boolean>() {
                            public Boolean load(String key) {
                                CmsVar repairStatus = cmProcessor.getCmSimpleVar(key);
                                return repairStatus != null && Boolean.TRUE.toString().equals(repairStatus.getValue());
                            }
                        });
    }

    @Override
    public boolean getBooleanVariable(String key) {
        return variables.getUnchecked(key);
    }
}
