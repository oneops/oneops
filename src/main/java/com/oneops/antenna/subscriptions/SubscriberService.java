package com.oneops.antenna.subscriptions;

import com.oneops.antenna.cache.SinkCache;
import com.oneops.antenna.cache.SinkKey;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.URLSubscriber;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;

/**
 * The Class SubscriberService.
 */
public class SubscriberService {

    /**
     * Logger instance
     */
    private static Logger logger = Logger.getLogger(SubscriberService.class);

    /**
     * Default notification subscriber
     */
    @Autowired
    private URLSubscriber defaultSystemSubscriber;

    /**
     * The cache.
     */
    @Autowired
    private SinkCache sinkCache;

    /**
     * Get the subscribers for nsPath. Basically it will look sink cache for the
     * nsPath key and if it couldn't find any cached instance of subscribers, it will
     * automatically load from  cms.
     *
     * @param nsPath the ns path
     * @return the subscribers for ns
     */
    public List<BasicSubscriber> getSubscribersForNs(String nsPath) {
        try {
            return sinkCache.instance().get(new SinkKey(nsPath));
        } catch (Exception e) {
            logger.error("Can't retrieve subscribers for nspath " + nsPath + " from sink cache", e);
            // In case of any error, returns the default subscriber
            return Arrays.asList((BasicSubscriber) defaultSystemSubscriber);
        }
    }

    /**
     * Mutator for default subscriber
     *
     * @param defaultSystemSubscriber
     */
    public void setDefaultSystemSubscriber(URLSubscriber defaultSystemSubscriber) {
        this.defaultSystemSubscriber = defaultSystemSubscriber;
    }

    /**
     * Mutator for sink cache
     *
     * @param sinkCache
     */
    public void setSinkCache(SinkCache sinkCache) {
        this.sinkCache = sinkCache;
    }
}
