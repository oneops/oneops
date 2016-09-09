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
package com.oneops.antenna.ws;

import com.google.common.cache.CacheStats;
import com.oneops.antenna.cache.SinkCache;
import com.oneops.antenna.cache.SinkKey;
import com.oneops.antenna.domain.BasicSubscriber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.web.bind.annotation.RequestMethod.DELETE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * The Class AntennaWsController.
 */
@Controller
public class AntennaWsController {

    /**
     * Sink cache instance
     */
    private final SinkCache cache;

    @Autowired
    public AntennaWsController(SinkCache cache) {
        this.cache = cache;
    }

    /**
     * Get the current sink cache status. Returns the cumulative status of
     * <ul>
     * <li>hitCount
     * <li>missCount;
     * <li>loadSuccessCount;
     * <li>loadExceptionCount;
     * <li>totalLoadTime;
     * <li>evictionCount;
     * </ul>
     *
     * @return cache status map.
     */
    @RequestMapping(value = "/cache/stats", method = GET)
    @ResponseBody
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stat = new LinkedHashMap<>(5);
        stat.put("status", "ok");
        stat.put("maxSize", cache.getMaxSize());
        stat.put("currentSize", cache.instance().size());
        stat.put("timeout", cache.getTimeout());

        CacheStats cs = cache.instance().stats();
        stat.put("hitCount", cs.hitCount());
        stat.put("missCount", cs.missCount());
        stat.put("loadSuccessCount", cs.loadSuccessCount());
        stat.put("totalLoadTime", SECONDS.convert(cs.totalLoadTime(), NANOSECONDS));
        stat.put("loadExceptionCount", cs.loadExceptionCount());
        stat.put("evictionCount", cs.evictionCount());
        return stat;
    }

    /**
     * Clear sink cache.
     *
     * @return cache status map.
     */
    @RequestMapping(value = "/cache/clear", method = DELETE)
    @ResponseBody
    public Map<String, Object> clearCache() {
        Map<String, Object> stat = new LinkedHashMap<>(2);
        // Get the size before invalidating it.
        long size = cache.instance().size();
        cache.instance().invalidateAll();
        cache.instance().cleanUp();
        stat.put("status", "ok");
        stat.put("clearedItems", size);
        return stat;
    }

    /**
     * Get all cache entries. If cache has more than 100 items, it will respond with
     * <b>413 Request Entity Too Large</b> http status code.
     *
     * @return cache entries map.
     */
    @RequestMapping(value = "/cache/entries", method = GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCacheEntries() {
        // Do a cache maintenance first before getting the size
        cache.instance().cleanUp();
        long size = cache.instance().size();
        Map<String, Object> stat = new LinkedHashMap<>(3);
        if (size > 100) {
            stat.put("status", "Too many cache entries (size=" + size + ")");
            return new ResponseEntity<>(stat, PAYLOAD_TOO_LARGE);
        }
        stat.put("status", "ok");
        stat.put("size", size);

        Map<SinkKey, List<BasicSubscriber>> map = cache.instance().asMap();
        Map<String, Object> entries = new HashMap<>(map.size());
        for (SinkKey key : map.keySet()) {
            entries.put(key.getOrg(), map.get(key).toString());
        }
        stat.put("entries", entries);
        return new ResponseEntity<>(stat, OK);
    }

    /**
     * Get the cache entry for specific nsPath. If the API returns an entry
     * doesn't mean that entry was existing in the Cache because the cache loader
     * would fetch and load a non existing entry on demand upon expiry.
     *
     * @param nsPath message nspath
     * @return cache entry map.
     */
    @RequestMapping(value = "/cache/entry", method = GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCacheEntry(@RequestParam(value = "nsPath", required = true) String nsPath) {
        Map<String, Object> stat = new LinkedHashMap<>(2);
        List<BasicSubscriber> entry;
        try {
            entry = cache.instance().get(new SinkKey(nsPath));
        } catch (ExecutionException e) {
            stat.put("status", e.getMessage());
            return new ResponseEntity<>(stat, NOT_FOUND);
        }
        stat.put("status", "ok");
        stat.put("entry", entry.toString());
        return new ResponseEntity<>(stat, OK);
    }

    /**
     * Invalidate the cache entry for specific nspath.
     *
     * @param nsPath message nspath
     * @return cache entry map.
     */
    @RequestMapping(value = "/cache/entry", method = DELETE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCacheEntry(@RequestParam(value = "nsPath", required = true) String nsPath) {
        Map<String, Object> stat = new LinkedHashMap<>(2);
        cache.instance().invalidate(new SinkKey(nsPath));
        stat.put("status", "ok");
        stat.put("entry", nsPath);
        return new ResponseEntity<>(stat, OK);
    }

}
