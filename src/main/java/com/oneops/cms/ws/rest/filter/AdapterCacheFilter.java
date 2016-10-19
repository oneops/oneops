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
package com.oneops.cms.ws.rest.filter;

import com.oneops.cms.cm.service.CmsCmManager;
import com.oneops.cms.md.service.CmsMdManager;
import com.oneops.filter.CacheFilter;

/**
 * Adapter metadata cache filter. Invalidate the cms metadata
 * cache if there is any change in cache update status variable.
 *
 * @author Suresh G
 */
public class AdapterCacheFilter extends CacheFilter {

    private CmsMdManager mdManager;

    public AdapterCacheFilter(boolean cacheEnabled, long ttl, long cacheSize, CmsCmManager cmManager, CmsMdManager mdManager) {
        super(cacheEnabled, ttl, cacheSize, cmManager);
        this.mdManager = mdManager;
    }

    @Override
    public void onUpdate(String varName) {
        mdManager.invalidateCache();
        logger.info("Adapter metadata cache invalidated.");
    }

}
