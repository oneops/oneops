/*
 * Copyright 2014-2015 WalmartLabs.
 */
package com.oneops.cassandra;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ExhaustedPolicy;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

import static com.oneops.util.DNSUtil.resolveHosts;
import static java.lang.Boolean.getBoolean;
import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static me.prettyprint.cassandra.service.ExhaustedPolicy.WHEN_EXHAUSTED_GROW;
import static org.springframework.util.StringUtils.collectionToCommaDelimitedString;


/**
 * This class is used to bootstrap the cluster client for Cassandra.
 *
 * @author Suresh G
 */
@Component
public class ClusterBootstrap {

    private static final Logger logger = Logger.getLogger(ClusterBootstrap.class);

    // Cassandra Client property prefix
    public static final String CPP = "oo.cassandra.client";

    @Value("#{'${oo.cassandra.client.hosts:daq}'.split(',')}")
    private List<String> clusterHosts;

    @Value("${oo.cassandra.client.port:9160}")
    private int clusterPort;

    @Autowired
    private Environment env;

    @PostConstruct
    private void init() {
        logger.info(this);
        if (clusterHosts.size() < 1) {
            throw new IllegalStateException("Cassandra cluster host can't be empty");
        }
    }

    /**
     * Set the cassandra cluster host and port. This is usually used for
     * initialing the Cluster bootstrap for non spring apps.
     *
     * @param clusterHostPort should be of the form "host:port", eg:  "daq:9160"
     * @return {@code ClusterBootstrap}
     */
    public ClusterBootstrap setHostPort(String clusterHostPort) {
        String[] parts = clusterHostPort.split(":");
        clusterHosts = Arrays.asList(parts[0]);
        clusterPort = 9160;
        if (parts.length > 1)
            clusterPort = Integer.valueOf(parts[1]);
        return this;
    }


    /**
     * Create a hector cluster instance for an existing Cassandra cluster with the given configuration.
     * Default values for {@code CassandraHostConfigurator} are configured in properties.
     *
     * @param clusterName         cluster name. This is an identifying string for the cluster.
     *                            Clusters will be created on demand per each unique clusterName
     * @param activeClients       The maximum number of active clients to allowkey.
     * @param thriftSocketTimeout Cassandra Thrift Socket Timeout (in milliseconds)
     * @return cluster object
     * @see <a href="https://github.com/hector-client/hector/wiki/User-Guide#finer-grained-configuration">Hector config</a>
     */
    public Cluster getCluster(String clusterName, int activeClients, int thriftSocketTimeout) {
        logger.info("Bootstrapping hector cluster client for " + clusterName);
        CassandraHostConfigurator config = new CassandraHostConfigurator();
        String hostStr = collectionToCommaDelimitedString(resolveHosts(clusterHosts,
                getProp("host_resolve", Boolean.class, true),
                getProp("host_ipv4", Boolean.class, true)));
        config.setHosts(hostStr);
        config.setPort(clusterPort);
        config.setUseThriftFramedTransport(getProp("thrift", Boolean.class, true));
        config.setUseSocketKeepalive(getProp("keep_alive", Boolean.class, true));
        config.setCassandraThriftSocketTimeout(thriftSocketTimeout);
        config.setMaxActive(activeClients);
        config.setExhaustedPolicy(ExhaustedPolicy.valueOf(getProp("exhausted_policy", String.class, WHEN_EXHAUSTED_GROW.toString())));
        config.setMaxWaitTimeWhenExhausted(getProp("exhausted_waittime", Integer.class, -1));
        config.setUseHostTimeoutTracker(getProp("ht_tracker", Boolean.class, true));
        config.setHostTimeoutCounter(getProp("ht_count", Integer.class, 3));
        config.setRetryDownedHosts(getProp("retry_hosts", Boolean.class, true));
        config.setRetryDownedHostsDelayInSeconds(getProp("retry_hosts_delay", Integer.class, 60));
        boolean autoDiscovery = getProp("auto_discovery", Boolean.class, false);
        config.setAutoDiscoverHosts(autoDiscovery);
        if (autoDiscovery) {
            config.setRunAutoDiscoveryAtStartup(true);
            config.setAutoDiscoveryDelayInSeconds(60);
        }

        logger.info(config.toString());
        Cluster cluster = HFactory.createCluster(clusterName, config);
        logger.info("Known pool hosts for " + clusterName + " cluster is " + cluster.getKnownPoolHosts(true));
        if (cluster.getKnownPoolHosts(true).size() == 0) {
        	logger.error("shutdown due to empty cluster.getKnownPoolHosts - hector doesnt throw java.net.ConnectException: Connection refused");
        }
        return cluster;
    }


    /**
     * Create a hector cluster instance for an existing Cassandra cluster with default active clients & thrift timeout.
     *
     * @param clusterName cluster name. This is an identifying string for the cluster.
     *                    Clusters will be created on demand per each unique clusterName
     *                    key.
     * @return cluster object
     * @see <a href="https://github.com/hector-client/hector/wiki/User-Guide#finer-grained-configuration">Hector config</a>
     */
    public Cluster getCluster(String clusterName) {
        return getCluster(clusterName, getProp("active_clients", Integer.class, 6), getProp("timeout", Integer.class, 60 * 1000));
    }


    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved. The property
     * resolution flow is
     * <p/>
     * <ol>
     * <li>Get from {@code Environment} if it's running in spring container.
     * It uses property file config and system property as fallback
     * mechanisms</li>
     * <li>Get the value from System Property (-Dprop=value) in non spring env</li>
     * <li>If it can't find anywhere, returns the  {@code defaultValue} </li>
     * <p/>
     * </ol>
     *
     * @param prop         the property name to resolve
     * @param T            the expected type of the property value
     * @param defaultValue the default value to return if no value is found
     */
    private <T> T getProp(String prop, Class<T> targetType, T defaultValue) {

        T val = defaultValue;
        // Fully qualified property name
        final String fqProp = CPP + "." + prop;

        if (env != null) {
            val = env.getProperty(fqProp, targetType, defaultValue);
        } else {
            String spVal = System.getProperty(fqProp);
            if (spVal != null) {
                if (targetType.isAssignableFrom(Boolean.class)) {
                    val = targetType.cast(getBoolean(spVal));
                } else if (targetType.isAssignableFrom(Integer.class)) {
                    val = targetType.cast(parseInt(spVal));
                } else if (targetType.isAssignableFrom(Double.class)) {
                    val = targetType.cast(parseDouble(spVal));
                } else if (targetType.isAssignableFrom(String.class)) {
                    val = targetType.cast(spVal);
                }
            }
        }
        return val;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ClusterBootstrap{ ");
        sb.append("clusterPort=").append(clusterPort);
        sb.append(", clusterHosts=").append(clusterHosts);
        sb.append('}');
        return sb.toString();
    }
}
