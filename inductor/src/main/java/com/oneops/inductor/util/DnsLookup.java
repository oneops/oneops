package com.oneops.inductor.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.log4j.Logger;
import org.xbill.DNS.ARecord;
import org.xbill.DNS.CNAMERecord;
import org.xbill.DNS.Cache;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.Resolver;
import org.xbill.DNS.SimpleResolver;
import org.xbill.DNS.Type;

/**
 * Perform DNS queries.
 *
 * @author Suresh
 */
public class DnsLookup {

  private static final Logger log = Logger.getLogger(DnsLookup.class);

  /** Time to wait for a response from DNS server. */
  private static final int DNS_TIMEOUT_IN_SEC = 10;

  /**
   * Retry policy for DNS lookup. Maximum delay for DNS lookup retry policy is 330 (33*10) seconds,
   * which is slightly greater than the Infoblox negative cache TTL of 5 minutes (300 seconds)
   */
  private static final RetryPolicy RETRY_POLICY =
      new RetryPolicy()
          .retryOn(Exception.class)
          .retryWhen(Collections.emptyList())
          .retryIf(Objects::isNull)
          .withDelay(10, TimeUnit.SECONDS)
          .withMaxRetries(33);

  /**
   * Query the DNS server for an A record of fqdn.
   *
   * @param fqdn domain name
   * @param dnsServer dns resolver
   * @return list of A records associated with the domain.
   * @throws Exception throws if any error occurs communicating to DNS server.
   */
  public static List<String> lookupARec(String fqdn, String dnsServer) throws Exception {
    Resolver resolver = new SimpleResolver(dnsServer);
    resolver.setTimeout(DNS_TIMEOUT_IN_SEC);

    Lookup dns = new Lookup(fqdn, Type.A);
    dns.setResolver(resolver);
    dns.setCache(new Cache());
    Record[] recs = dns.run();

    return recs != null
        ? Arrays.stream(recs)
            .map(ARecord.class::cast)
            .map(a -> a.getAddress().getHostAddress())
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  /**
   * Query the DNS server for CNAME records of fqdn.
   *
   * @param fqdn domain name
   * @param dnsServer dns resolver
   * @return list of canonical names associated with the domain.
   * @throws Exception throws if any error occurs communicating to DNS server.
   */
  public static List<String> lookupCNameRec(String fqdn, String dnsServer) throws Exception {
    Resolver resolver = new SimpleResolver(dnsServer);
    resolver.setTimeout(DNS_TIMEOUT_IN_SEC);

    Lookup dns = new Lookup(fqdn, Type.CNAME);
    dns.setResolver(resolver);
    dns.setCache(new Cache());
    Record[] recs = dns.run();

    return recs != null
        ? Arrays.stream(recs)
            .map(CNAMERecord.class::cast)
            .map(c -> c.getTarget().toString())
            .collect(Collectors.toList())
        : Collections.emptyList();
  }

  /**
   * Checks if the domain name is resolvable using all the dns servers. Each dns server will be
   * retried 180 seconds with 10 seconds retry delay before returning failure.
   *
   * @param fqdn domain name
   * @param dnsServers list of dns servers to resolve
   * @param logKey inductor log key
   * @return <code>true</code> if the domain resolves on all the name servers.
   */
  public static boolean isARecResolvable(String fqdn, List<String> dnsServers, String logKey) {
    log.info(logKey + "Resolving A record for '" + fqdn + "' using all name servers.");

    if (dnsServers.isEmpty()) {
      log.error(logKey + "Name server list is empty!");
      return false;
    }

    for (String dnsServer : dnsServers) {
      List<String> addresses =
          Failsafe.with(RETRY_POLICY)
              .onRetry(
                  (res, err, ctx) ->
                      log.warn(
                          logKey
                              + "DNS response from "
                              + dnsServer
                              + ": "
                              + res
                              + ", retrying #"
                              + ctx.getExecutions()))
              .get(() -> lookupARec(fqdn, dnsServer));

      log.info(logKey + "dig +short A @" + dnsServer + " " + fqdn + " -> " + addresses);
      if (addresses.isEmpty()) {
        log.error(logKey + "Dns resolution failed on " + dnsServer);
        return false;
      }
    }

    return true;
  }
}
