/*
 * Copyright 2014-2015 WalmartLabs.
 */
package com.oneops.util;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * ActiveMQ connection broker url builder class.
 *
 * @author Suresh G
 * @see <a href="http://activemq.apache.org/failover-transport-reference.html">Failover transport</a>
 */
@Component
public class AMQConnectorURI {

    private static final Logger logger = Logger.getLogger(AMQConnectorURI.class);

    /**
     * High Level Protocol URIs (transport methods) used for AMQ connection.
     * Commonly used transport methods are,
     * <li>
     * <li>failover</li>
     * <li>static</li>
     * <li>masterslave</li>
     * </li>
     */
    private String transport;

    /**
     * Protocol to be used. Eg:
     * <li>
     * <li>tcp</li>
     * <li>udp</li>
     * <li>nio</li>
     * <li>ssl</li>
     * <li>nio+ssl</li>
     * <li>ws (WebSocket)</li>
     * <li>mqtt</li>
     * <li>amqp</li>
     * </li>
     */
    private String protocol;

    /**
     * ActiveMQ hostname or FQDN for number of brokers.
     */
    private String host;

    /**
     * ActiveMQ port. Default is 61616.
     */
    private int port;

    /**
     * When {@code true}, enables TCP KeepAlive on the broker connection.
     * Useful to ensure that inactive consumers don't time out.
     */
    private boolean keepAlive;

    /**
     * If {@code true}, resolve the host fqdn to load balance
     * clients over a number of brokers
     */
    private boolean dnsResolve;

    /**
     * Maps holds all the different transport options for connection.
     */
    private Map<String, String> transportOptions;

    @PostConstruct
    public void init() {
        logger.info(toString());
    }

    public void setTransport(String transport) {
        this.transport = transport;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public void setDnsResolve(boolean dnsResolve) {
        this.dnsResolve = dnsResolve;
    }

    public void setTransportOptions(Map<String, String> transportOptions) {
        this.transportOptions = transportOptions;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AMQConnectorURI{");
        sb.append("transport='").append(transport).append('\'');
        sb.append(", protocol='").append(protocol).append('\'');
        sb.append(", host='").append(host).append('\'');
        sb.append(", port=").append(port);
        sb.append(", keepAlive=").append(keepAlive);
        sb.append(", dnsResolve=").append(dnsResolve);
        sb.append(", transportOptions=").append(transportOptions);
        sb.append('}');
        return sb.toString();
    }

    /**
     * Builder method for AMQ connector uri string. The standard format is,
     * <b>failover:(tcp://primary:61616?keepAlive=true,tcp://secondary:61616?keepAlive=true)?randomize=false&amp;...</b>
     *
     * @return AMQ connector uri.
     * @see #build(String, int)
     */
    public String build() {
        return build(this.host, this.port);
    }

    /**
     * Builder method for AMQ connector uri string with given broker host and port. The standard format is,
     * <b>failover:(tcp://primary:61616?keepAlive=true,tcp://secondary:61616?keepAlive=true)?randomize=false&amp;...</b>
     *
     * @param host Message broker host name
     * @param port Message broker port
     * @return AMQ connector uri.
     */
    public String build(String host, int port) {
        List<String> brokers = new ArrayList<String>();
        if (dnsResolve) {
            brokers.addAll(DNSUtil.resolve(host));
        } else {
            brokers.add(host);
        }

        StringBuilder buf = new StringBuilder();
        buf.append(transport);
        buf.append(":");
        buf.append("(");
        Iterator<String> it = brokers.iterator();
        while (it.hasNext()) {
            buf.append(protocol);
            buf.append("://");
            buf.append(it.next());
            buf.append(":");
            buf.append(port);
            buf.append("?keepAlive=");
            buf.append(keepAlive);
            if (it.hasNext()) {
                buf.append(",");
            }
        }
        buf.append(")");

        if (transportOptions != null) {
            Iterator<String> optIt = transportOptions.keySet().iterator();
            if (optIt.hasNext()) {
                buf.append("?");
            }
            while (optIt.hasNext()) {
                String key = optIt.next();
                buf.append(key);
                buf.append("=");
                buf.append(transportOptions.get(key));
                if (optIt.hasNext()) {
                    buf.append("&");
                }
            }
        }
        String uri = buf.toString();
        logger.info("AMQ broker URL for " + host + ": " + uri);
        return uri;
    }
}
