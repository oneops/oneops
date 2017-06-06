package com.oneops.sensor.util;

import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ReplacedInstances {
	
    private Cache<Long, Boolean> instanceCache;
    
    public ReplacedInstances(int ttlInMins) {
    	instanceCache = CacheBuilder.newBuilder().expireAfterWrite(ttlInMins, TimeUnit.MINUTES).build();
    }
    
    public void add(long ciId) {
    	instanceCache.put(ciId, true);
    }
    
    public void remove(long ciId) {
    	instanceCache.invalidate(ciId);
    }
    
    public boolean isReplaced(long ciId) {
    	Boolean isReplaced = instanceCache.getIfPresent(ciId);
    	return isReplaced != null ? true : false;
    }

}
