package com.oneops.opamp.service;

import static java.lang.Long.parseLong;
import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oneops.cms.cm.service.CmsCmProcessor;
import com.oneops.cms.md.service.CmsMdProcessor;
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
    //cached for long types
    private LoadingCache<String, Long> varCache;
    private int ttlInSeconds = 30;
    private int maxSize = 100;
    private static final String MD_CACHE_STATUS_VAR = "MD_UPDATE_TIMESTAMP";

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

        this.varCache = CacheBuilder.newBuilder()
            .maximumSize(maxSize)
            .expireAfterWrite(ttlInSeconds, SECONDS)
            .initialCapacity(1)
            .build(new CacheLoader<String, Long>() {
                @Override
                public Long load(String key) throws Exception {
                    CmsVar cacheStatus = cmProcessor.getCmSimpleVar(key);
                    if (cacheStatus != null) {
                        return parseLong(cacheStatus.getValue());
                    }
                    return 0L;
                }
            });
    }

    @Override
    public boolean getBooleanVariable(String key) {
        return variables.getUnchecked(key);
    }

    @Override
    public long getLongVariable(String key) {
        return varCache.getUnchecked(key);
    }
    private long lastUpdatedTs;


    @Override
    public void refreshMdCache() {
        long updateTs = getLongVariable(MD_CACHE_STATUS_VAR);
        if (updateTs > lastUpdatedTs) {
            lastUpdatedTs = updateTs;
            getMdProcessor().invalidateCache();
        }
    }


}
