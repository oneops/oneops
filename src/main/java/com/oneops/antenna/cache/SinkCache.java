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
package com.oneops.antenna.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;
import com.oneops.antenna.domain.BasicSubscriber;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A loading cache implementation for sink subscribers.
 *
 * @author <a href="mailto:sgopal1@walmartlabs.com">Suresh G</a>
 */
@Service
@Qualifier("sinkCache")
public class SinkCache {

    private static Logger logger = Logger.getLogger(SinkCache.class);

    /**
     * Cache max size. Defaults to 1000.
     */
    @Value("${oo.antenna.cache.max_size:1000}")
    private int maxSize;

    /**
     * Cache timeout. Defaults to 1 hour (3600 secs).
     */
    @Value("${oo.antenna.cache.timeout:3600}")
    private int timeout;

    /**
     * Sink subscriber removal listener
     */
    private final SinkRemovalListener removalListener;

    /**
     * Cache loader
     */
    private final SinkSubscriberLoader cacheLoader;

    /**
     * Loading Cache instance
     */
    private LoadingCache<SinkKey, List<BasicSubscriber>> cache;

    @Autowired
    public SinkCache(SinkRemovalListener removalListener, SinkSubscriberLoader cacheLoader) {
        this.removalListener = removalListener;
        this.cacheLoader = cacheLoader;
    }

    /**
     * Initialize the cache
     */
    @PostConstruct
    public void init() {
        logger.info("***** Initializing the Sink Cache with timeout="
                + TimeUnit.SECONDS.toHours(timeout)
                + " hour(s), maxSize=" + maxSize);
        cache = CacheBuilder.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(timeout, TimeUnit.SECONDS)
                .initialCapacity(15)
                .recordStats()
                .removalListener(removalListener)
                .build(cacheLoader);
    }

    /**
     * Get the cache max size configuration value
     *
     * @return max size configuration
     */
    public int getMaxSize() {
        return maxSize;
    }

    /**
     * Get the cache timeout value
     *
     * @return timeout value in sec
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Returns the loading cache instance
     *
     * @return cache.
     */
    public LoadingCache<SinkKey, List<BasicSubscriber>> instance() {
        return cache;
    }

}
