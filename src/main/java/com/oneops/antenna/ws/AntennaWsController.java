package com.oneops.antenna.ws;

import com.google.common.cache.CacheStats;
import com.oneops.antenna.cache.SinkCache;
import com.oneops.antenna.cache.SinkKey;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.antenna.service.NotificationMessageDao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * The Class AntennaWsController.
 */
@Controller
public class AntennaWsController {

    @Autowired
	private NotificationMessageDao nmDao;

    /**
     * Sink cache instance
     */
    @Autowired
    private SinkCache cache;
	
	/**
	 * Sets the nm dao.
	 *
	 * @param nmDao the new nm dao
	 */
	public void setNmDao(NotificationMessageDao nmDao) {
		this.nmDao = nmDao;
	}


	/**
	 * Gets the notifications for ns.
	 *
	 * @param nsPath the ns path
	 * @param type the type
	 * @param source the source
	 * @param count the count
	 * @param start the start
	 * @param end the end
	 * @return the notifications for ns
	 */
	@RequestMapping(value="/notifications", method = RequestMethod.GET)
	@ResponseBody
	public List<NotificationMessage> getNotificationsForNS(
			@RequestParam(value="nsPath", required = true) String nsPath,
			@RequestParam(value="type", required = false) NotificationType type,
			@RequestParam(value="severity", required = false) NotificationSeverity severity,
			@RequestParam(value="source", required = false) String source,
			@RequestParam(value="contains", required = false) String textMatch,
			@RequestParam(value="count", required = false) Integer count,
			@RequestParam(value="start", required = false) Long start,
			@RequestParam(value="end", required = false) Long end,
			@RequestParam(value="case_sensitive", required = false) Boolean isCaseSensitive) {
		if (count == null && start == null) {
			return null;
		}
		if (count == null) {count = new Integer(1000);}; 
		
		if (start != null) {
			return nmDao.getNotificationsForNsByRange(nsPath, type, severity, source, start * 1000, end * 1000, textMatch, count, isCaseSensitive);
		} else {
			return nmDao.getLastNotificationsForNs(nsPath, type, severity, source, textMatch, count, isCaseSensitive);
		}
	}	

	/**
	 * Gets the notifications for ci.
	 *
	 * @param ciId the ci id
	 * @param source the source
	 * @param count the count
	 * @param start the start
	 * @param end the end
	 * @return the notifications for ci
	 */
	@RequestMapping(value="/notifications/cis/{ciId}", method = RequestMethod.GET)
	@ResponseBody
	public List<NotificationMessage> getNotificationsForCi(
			@PathVariable long ciId,
			@RequestParam(value="severity", required = false) NotificationSeverity severity,
			@RequestParam(value="source", required = false) String source,
			@RequestParam(value="contains", required = false) String textMatch,
			@RequestParam(value="count", required = false) Integer count,
			@RequestParam(value="start", required = false) Long start,
			@RequestParam(value="end", required = false) Long end,
			@RequestParam(value="case_sensitive", required = false) Boolean isCaseSensitive) {

		return getNotificationForId(ciId, NotificationType.ci, severity, source, textMatch, count, start, end, isCaseSensitive);

	}	

	/**
	 * Gets the notifications for dpmt.
	 *
	 * @param dpmtId the dpmt id
	 * @param source the source
	 * @param count the count
	 * @param start the start
	 * @param end the end
	 * @return the notifications for dpmt
	 */
	@RequestMapping(value="/notifications/deployments/{dpmtId}", method = RequestMethod.GET)
	@ResponseBody
	public List<NotificationMessage> getNotificationsForDpmt(
			@PathVariable long dpmtId,
			@RequestParam(value="severity", required = false) NotificationSeverity severity,
			@RequestParam(value="source", required = false) String source,
			@RequestParam(value="contains", required = false) String textMatch,
			@RequestParam(value="count", required = false) Integer count,
			@RequestParam(value="start", required = false) Long start,
			@RequestParam(value="end", required = false) Long end,
			@RequestParam(value="case_sensitive", required = false) Boolean isCaseSensitive) {

		return getNotificationForId(dpmtId, NotificationType.deployment, severity, source, textMatch, count, start, end, isCaseSensitive);

	}	
	
	/**
	 * Gets the notifications for proc.
	 *
	 * @param procId the proc id
	 * @param source the source
	 * @param count the count
	 * @param start the start
	 * @param end the end
	 * @return the notifications for proc
	 */
	@RequestMapping(value="/notifications/procedures/{procId}", method = RequestMethod.GET)
	@ResponseBody
	public List<NotificationMessage> getNotificationsForProc(
			@PathVariable long procId,
			@RequestParam(value="severity", required = false) NotificationSeverity severity,
			@RequestParam(value="source", required = false) String source,
			@RequestParam(value="contains", required = false) String textMatch,
			@RequestParam(value="count", required = false) Integer count,
			@RequestParam(value="start", required = false) Long start,
			@RequestParam(value="end", required = false) Long end,
			@RequestParam(value="case_sensitive", required = false) Boolean isCaseSensitive) {

		return getNotificationForId(procId, NotificationType.procedure, severity, source, textMatch, count, start, end, isCaseSensitive);

	}

    private List<NotificationMessage> getNotificationForId(long ciId, NotificationType type, NotificationSeverity severity, String source, String matchText,
                                                           Integer count, Long start, Long end, Boolean isCaseSensitive) {

        if (count == null && start == null) {
            return null;
        }
        if (count == null) {
            count = new Integer(1000);
        }
        ;

        if (start != null) {
            return nmDao.getNotificationsByRange(ciId, type, severity, source,
                    start * 1000, end * 1000, matchText, count, isCaseSensitive);
        } else {
            return nmDao.getLastNotifications(ciId, type, severity,
                    source, matchText, count, isCaseSensitive);
        }
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
    @RequestMapping(value = "/cache/stats", method = RequestMethod.GET)
    @ResponseBody
    public Map<String, Object> getCacheStats() {
        Map<String, Object> stat = new LinkedHashMap<String, Object>(5);
        stat.put("status", "ok");
        stat.put("maxSize", cache.getMaxSize());
        stat.put("currentSize", cache.instance().size());
        stat.put("timeout", cache.getTimeout());
        CacheStats cs = cache.instance().stats();
        stat.put("hitCount", cs.hitCount());
        stat.put("missCount", cs.missCount());
        stat.put("loadSuccessCount", cs.loadSuccessCount());
        stat.put("totalLoadTime", TimeUnit.SECONDS.convert(cs.totalLoadTime(), TimeUnit.NANOSECONDS));
        stat.put("loadExceptionCount", cs.loadExceptionCount());
        stat.put("evictionCount", cs.evictionCount());
        return stat;
    }

    /**
     * Clear sink cache.
     *
     * @return cache status map.
     */
    @RequestMapping(value = "/cache/clear", method = RequestMethod.DELETE)
    @ResponseBody
    public Map<String, Object> clearCache() {
        Map<String, Object> stat = new LinkedHashMap<String, Object>(2);
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
    @RequestMapping(value = "/cache/entries", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCacheEntries() {
        // Do a cache maintenance first before getting the size
        cache.instance().cleanUp();
        long size = cache.instance().size();
        Map<String, Object> stat = new LinkedHashMap<String, Object>(3);
        if (size > 100) {
            stat.put("status", "Too many cache entries (size=" + size + ")");
            return new ResponseEntity<Map<String, Object>>(stat, HttpStatus.REQUEST_ENTITY_TOO_LARGE);
        }
        stat.put("status", "ok");
        stat.put("size", size);

        Map<SinkKey, List<BasicSubscriber>> map = cache.instance().asMap();
        Map<String, Object> entries = new HashMap<String, Object>(map.size());
        for (SinkKey key : map.keySet()) {
            entries.put(key.getOrg(), map.get(key).toString());
        }
        stat.put("entries", entries);
        return new ResponseEntity<Map<String, Object>>(stat, HttpStatus.OK);
    }

    /**
     * Get the cache entry for specific nsPath. If the API returns an entry
     * doesn't mean that entry was existing in the Cache because the cache loader
     * would fetch and load a non existing entry on demand upon expiry.
     *
     * @param nsPath message nspath
     * @return cache entry map.
     */
    @RequestMapping(value = "/cache/entry", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getCacheEntry(
            @RequestParam(value = "nsPath", required = true) String nsPath) {
        Map<String, Object> stat = new LinkedHashMap<String, Object>(2);
        List<BasicSubscriber> entry;
        try {
            entry = cache.instance().get(new SinkKey(nsPath));
        } catch (ExecutionException e) {
            stat.put("status", e.getMessage());
            return new ResponseEntity<Map<String, Object>>(stat, HttpStatus.NOT_FOUND);
        }
        stat.put("status", "ok");
        stat.put("entry", entry.toString());
        return new ResponseEntity<Map<String, Object>>(stat, HttpStatus.OK);
    }

    /**
     * Invalidate the cache entry for specific nspath.
     *
     * @param nsPath message nspath
     * @return cache entry map.
     */
    @RequestMapping(value = "/cache/entry", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteCacheEntry(
            @RequestParam(value = "nsPath", required = true) String nsPath) {
        Map<String, Object> stat = new LinkedHashMap<String, Object>(2);
        cache.instance().invalidate(new SinkKey(nsPath));
        stat.put("status", "ok");
        stat.put("entry", nsPath);
        return new ResponseEntity<Map<String, Object>>(stat, HttpStatus.OK);
    }


}
