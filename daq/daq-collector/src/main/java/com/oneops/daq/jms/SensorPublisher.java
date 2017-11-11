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

package com.oneops.daq.jms;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.oneops.sensor.events.BasicEvent;
import com.oneops.sensor.events.PerfEvent;
import com.oneops.sensor.thresholds.Threshold;
import com.oneops.sensor.thresholds.ThresholdsDao;
import com.oneops.util.AMQConnectorURI;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.apache.log4j.Logger;
import org.springframework.jms.JmsException;
import org.springframework.jms.core.JmsTemplate;

import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

//import org.springframework.jms.connection.CachingConnectionFactory;

/**
 * The Class SensorPublisher.
 */
public class SensorPublisher {
    private static final Logger logger = Logger.getLogger(SensorPublisher.class);
    private static final int MINUTE = 60 * 1000;

    private String user = ActiveMQConnection.DEFAULT_USER;
    private String password = ActiveMQConnection.DEFAULT_PASSWORD;
    private String url = ActiveMQConnection.DEFAULT_BROKER_URL + "?connectionTimeout=1000";
    private String queueBase = "perf-in-q";
    private ConcurrentHashMap<Long, Long> manifestCache = new ConcurrentHashMap<>();

    private final AtomicLong eventCounter = new AtomicLong();
    private final AtomicLong missingManifestCounter = new AtomicLong();
    private final AtomicLong failedThresholdLoadCounter = new AtomicLong();
    private final AtomicLong publishedCounter = new AtomicLong();
    private int amqConnectionPoolSize = Integer.parseInt(System.getProperty("daq.amq.connection.pool.size", "8"));

    private static final Threshold NO_OP_THRESHOLD = new Threshold();

    private class ThresholdHolderWithExpiration {
        private long expiration;
        private Threshold threshold;


        ThresholdHolderWithExpiration(Threshold threshold) {
            this(threshold, -1);
        }

        ThresholdHolderWithExpiration(Threshold threshold, long expiration) {
            this.threshold = threshold;
            this.expiration = (expiration == -1) ? -1 : (System.currentTimeMillis() + expiration);
        }

        Threshold getThreshold() {
            return threshold;
        }

        boolean isExpired() {
            return expiration > 0 && System.currentTimeMillis() > expiration;
        }

    }


    private static int ttlForNoThresholdCache = Integer.parseInt(System.getProperty("no_threshold_cache_ttl", "2"));
    
    private CacheLoader<String, ThresholdHolderWithExpiration> loader = new CacheLoader<String, ThresholdHolderWithExpiration>() {
        @Override
        public ThresholdHolderWithExpiration load(String key) throws Exception {
            String[] keyParts = key.split(":");
            Long manifestId = Long.parseLong(keyParts[0]);
            Threshold threshold = thresholdsDao.getThreshold(manifestId, keyParts[1]);
            logger.debug("loading: " + manifestId.toString() + " " + keyParts[1]);
            if (threshold == null || (threshold.getThresholdJson().equals("n") && !threshold.isHeartbeat())) {
                return new ThresholdHolderWithExpiration(NO_OP_THRESHOLD, ttlForNoThresholdCache*MINUTE);
            }
            return new ThresholdHolderWithExpiration(threshold);
        }
    };

    private static int thresholdTTL = Integer.parseInt(System.getProperty("threshold_cache_ttl", "15"));
    private static String mqConnectionTimeout = System.getProperty("mqTimeout", "1000");  // timeout message send after 1 second
    private static String mqConnectionStartupRetries = System.getProperty("mqStartupRetries", "5");  // only reconnect 5 times on startup (to avoid publisher being stuck if MQ is down on startup
    private static int mqConnectionThreshold = Integer.parseInt(System.getProperty("mqRetryTimeout", "10000"));  // discard all the published messages for mqRetryTimeout milliseconds before attempting to send message again
    private static Long manifestIdLookupThreshold = Long.parseLong(System.getProperty("manifestIdLookupThreshold", "20"));

    private long lastFailureTimestamp = -1;

    private LoadingCache<String, ThresholdHolderWithExpiration> thresholdCache = CacheBuilder.newBuilder()
            .refreshAfterWrite(thresholdTTL, TimeUnit.MINUTES)
            .build(loader);
    // -Dpoolsize=n
    private static int poolsize = Integer.parseInt(System.getProperty("poolsize", "1"));

    private JmsTemplate[] producers = new JmsTemplate[poolsize];

    private ThresholdsDao thresholdsDao = null;


    /**
     * Sets the threshold dao.
     *
     * @param thresholdDao the new threshold dao
     */
    public void setThresholdDao(ThresholdsDao thresholdDao) {
        this.thresholdsDao = thresholdDao;
    }

    private void showParameters() {
        logger.info("Connecting to URL: " + url);
        logger.info("Base queue name : " + queueBase);
        logger.info("poolsize : " + poolsize);
    }

    /**
     * Inits the.
     *
     * @throws JMSException the jMS exception
     */
    public void init() throws JMSException {
        Properties properties = new Properties();
        try {
            properties.load(this.getClass().getResourceAsStream("/sink.properties"));
        } catch (IOException e) {
            logger.error("got: " + e.getMessage());
        }

        user = properties.getProperty("amq.user");
        password = System.getenv("KLOOPZ_AMQ_PASS");


        if (password == null) {
            throw new JMSException("missing KLOOPZ_AMQ_PASS env var");
        }

        AMQConnectorURI connectStringGenerator = new AMQConnectorURI();
        connectStringGenerator.setHost("opsmq");
        connectStringGenerator.setProtocol("tcp");
        connectStringGenerator.setPort(61616);
        connectStringGenerator.setTransport("failover");
        connectStringGenerator.setDnsResolve(true);
        connectStringGenerator.setKeepAlive(true);
        HashMap<String, String> transportOptions = new HashMap<>();
        transportOptions.put("initialReconnectDelay", "1000");
        transportOptions.put("startupMaxReconnectAttempts", mqConnectionStartupRetries);
        transportOptions.put("timeout", mqConnectionTimeout);
        transportOptions.put("useExponentialBackOff", "false");
        connectStringGenerator.setTransportOptions(transportOptions);
        url = connectStringGenerator.build();

        showParameters();

        // Create the connection.
        ActiveMQConnectionFactory amqConnectionFactory = new ActiveMQConnectionFactory(user, password, url);
        amqConnectionFactory.setUseAsyncSend(true);
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory(amqConnectionFactory);
        pooledConnectionFactory.setMaxConnections(amqConnectionPoolSize);
        pooledConnectionFactory.setIdleTimeout(10000);

        for (int i = 0; i < poolsize; i++) {
            JmsTemplate producerTemplate = new JmsTemplate(pooledConnectionFactory);
            producerTemplate.setSessionTransacted(false);
            int shard = i + 1;
            Destination perfin = new org.apache.activemq.command.ActiveMQQueue(queueBase + "-" + shard);
            producerTemplate.setDefaultDestination(perfin);
            producerTemplate.setDeliveryPersistent(false);
            producers[i] = producerTemplate;
        }


    }

    /**
     * Enrich and publish.
     *
     * @param event the event
     * @throws JMSException       the jMS exception
     * @throws ExecutionException
     */
    @SuppressWarnings("unused")
    public void enrichAndPublish(PerfEvent event) throws JMSException {

        if (eventCounter.incrementAndGet() % 1000 == 0)
            logger.info("Publish event count: " + eventCounter.get() +
                    " manifest miss: " + missingManifestCounter.get() +
                    " failed threshold load count: " + failedThresholdLoadCounter.get());

        // negative value in manifestId cache represents number of failed attempts to retrieve it from cassandra. We exponentially backoff after manifestIdLookupThreshold 
        Long manifestId = null;
        if (manifestCache.containsKey(event.getCiId()) &&  manifestCache.get(event.getCiId())>0)
            manifestId = manifestCache.get(event.getCiId());
        else if (!manifestCache.containsKey(event.getCiId()) || needToDoALookup(-manifestCache.get(event.getCiId()), manifestIdLookupThreshold)){
            manifestId = thresholdsDao.getManifestId(event.getCiId());
            if (manifestId != null) {
                manifestCache.put(event.getCiId(), manifestId);
            }
        }
        if (manifestId == null) {
            logger.warn("Failed to map ciId: " + event.getCiId() + " to manifestId. Please fix");
            manifestCache.put(event.getCiId(), manifestCache.getOrDefault(event.getCiId(), 0L)-1);
            missingManifestCounter.incrementAndGet();
            return;
        }

        String key = manifestId.toString() + ":" + event.getSource();
        try {
            ThresholdHolderWithExpiration holder = thresholdCache.get(key);
            if (holder.isExpired()) {
                thresholdCache.refresh(key);
                holder = thresholdCache.get(key);   // get it again after cash refresh because it is expired
            }
            Threshold tr = holder.getThreshold();
            if (tr == NO_OP_THRESHOLD) {
                return;
            }
            logger.debug("Threshold: " + tr.getSource() + " " + tr.getThresholdJson());
            event.setManifestId(manifestId);
            event.setChecksum(tr.getCrc());
        } catch (Exception e) {
            logger.warn("Failed threshold load:" + manifestId + "::" + event.getSource(), e);
            failedThresholdLoadCounter.incrementAndGet();
            return;
        }
        publishMessage(event);

    }

    private boolean needToDoALookup(Long counter, Long threshold) {
        long callsAfterThreshold = counter - threshold;
        if (callsAfterThreshold<0) return true; // make every call before the threshold
        long sqrt = (long)(Math.sqrt(callsAfterThreshold));
        return sqrt*sqrt==callsAfterThreshold; // make calls every n^2 times [1,4,9,16,25 ...] 
    }


    /**
     * Publish message.
     *
     * @param event the event
     * @throws JMSException the jMS exception
     */
    public void publishMessage(final BasicEvent event) throws JMSException {
        
        if (System.currentTimeMillis() > lastFailureTimestamp) {
            publishedCounter.incrementAndGet();
            int shard = (int) (event.getManifestId() % poolsize);
            try {
                producers[shard].send(session -> {
                    ObjectMessage message = session.createObjectMessage(event);
                    message.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
                    message.setLongProperty("ciId", event.getCiId());
                    message.setLongProperty("manifestId", event.getManifestId());
                    message.setStringProperty("source", event.getSource());
                    if (logger.isDebugEnabled()) {
                        logger.debug("Published: ciId:" + event.getCiId() + "; source:" + event.getSource());
                    }
                    return message;
                });
                lastFailureTimestamp = -1;
            } catch (JmsException exception) {
                logger.warn("There was an error sending a message. Discarding messages for " + mqConnectionThreshold + " ms");
                lastFailureTimestamp = System.currentTimeMillis() + mqConnectionThreshold;
            }
        }
    }


    void setProducers(JmsTemplate[] producers) {
        this.producers = producers;
    }

    /**
     * Cleanup.
     */
    public void cleanup() {
        logger.info("Closing AMQ connection");
        closeConnection();
    }

    /**
     * Close connection.
     */
    public void closeConnection() {
        for (JmsTemplate jt : producers) {
            ((PooledConnectionFactory) jt.getConnectionFactory()).stop();
        }
        producers = null;
    }

    public long getMissingManifestCounter() {
        return missingManifestCounter.get();
    }

    public long getFailedThresholdLoadCounter() {
        return failedThresholdLoadCounter.get();
    }

    public long getPublishedCounter() {
        return publishedCounter.get();
    }
}
