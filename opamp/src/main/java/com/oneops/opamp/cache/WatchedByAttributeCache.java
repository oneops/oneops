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
package com.oneops.opamp.cache;

import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.LoadingCache;


/**
 * A loading cache implementation for relation attributes that need to be
 * retrieved to make decisions whether to send notifications or not.
 * 
 * @author glall
 *
 */
@Service
@Qualifier("watchedByAttributeCache")
public class WatchedByAttributeCache {

	/**
     * Logger instance
     */
    private static Logger logger = Logger.getLogger(WatchedByAttributeCache.class);

    /**
     * Cache max size. Defaults to 3000. This can be
     * set as -DwatchedByCacheMaxSize system property.
     * Note:Having default as 3000
     */
    @Value("${watchedByCacheMaxSize:3000}")
    private int maxSize;
    
    /**
     * Cache timeout. Defaults to 10 minutes (600 secs). This can be
     * set as -DwatchedBycacheTimeoutsystem property.
     */
    @Value("${watchedBycacheTimeout:600}")
    private int timeout;
    @Autowired
	private
    WatchedByAttributeCacheLoader watchedByCacheLoader;
    @Autowired
	private
    WatchedByCacheAttributeRemovalListener watchedByCacheRemovalListener;

    /**
     * Loading Cache instance
     */
    //TODO : could be extended to store the attribute, keeping it simple.  
    //key:ManifestId:Source:AttributeName
    private LoadingCache<String, String > cache;
    

    /**
     * Initialize the cache
     */
    @PostConstruct
    public void init() {
        logger.info("***** Initializing the WatchedByAttributeCache Cache with timeout="
                + TimeUnit.SECONDS.toHours(getTimeout())
                + " hour(s), maxSize=" + getMaxSize());
		cache = CacheBuilder.newBuilder()
                .maximumSize(getMaxSize())
                .expireAfterWrite(getTimeout(), TimeUnit.SECONDS)
                .initialCapacity(15)
                .recordStats()
                .removalListener(getWatchedByCacheRemovalListener())
                .build(getWatchedByCacheLoader());
    }

  

	public int getMaxSize() {
		return maxSize;
	}


	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}


	public int getTimeout() {
		return timeout;
	}


	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	 /**
     * Returns the loading cache instance
     *
     * @return cache.
     */
    public LoadingCache<String, String > instance() {
        return cache;
    }

   

	public WatchedByAttributeCacheLoader getWatchedByCacheLoader() {
		return watchedByCacheLoader;
	}



	public void setWatchedByCacheLoader(WatchedByAttributeCacheLoader watchedByCacheLoader) {
		this.watchedByCacheLoader = watchedByCacheLoader;
	}



	public WatchedByCacheAttributeRemovalListener getWatchedByCacheRemovalListener() {
		return watchedByCacheRemovalListener;
	}



	public void setWatchedByCacheRemovalListener(WatchedByCacheAttributeRemovalListener watchedByCacheRemovalListener) {
		this.watchedByCacheRemovalListener = watchedByCacheRemovalListener;
	}


}

