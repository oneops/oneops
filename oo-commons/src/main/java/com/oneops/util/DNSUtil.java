/**
 * *****************************************************************************
 *
 * <p>Copyright 2015 Walmart, Inc.
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * <p>*****************************************************************************
 */
package com.oneops.util;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.log4j.Logger;

/**
 * {@code DNSUtil} has some simple utility methods to deal with IP address and DNS of the system.
 *
 * @author Suresh G
 */
public class DNSUtil {

  private static final Logger logger = Logger.getLogger(DNSUtil.class);

  /**
   * Resolves the DNS name to IPV4 addresses using the system default DNS configuration.
   *
   * @param fqdn fully qualified doamin name (eg: VIP name)
   * @return list of host addresses
   */
  public static List<String> resolve(String fqdn) {
    return resolveHosts(Collections.singletonList(fqdn), true, true);
  }

  /**
   * Creates a comma separated list of host addresses by resolving the hostname(s). If the
   * <b>host_resolve</b> property is <code>false</code>, returns the same host list.
   *
   * @param hosts host names
   * @param hostResolve <code>true</code> will enable the dns resolution
   * @param ipv4Only filter IPv4 address only
   * @return list of host addresses
   */
  @SuppressWarnings("Duplicates")
  public static List<String> resolveHosts(
      List<String> hosts, boolean hostResolve, boolean ipv4Only) {
    List<String> ips = new ArrayList<>();
    // Resolve the host addresses if dnsResolve is true
    if (hostResolve) {
      for (String host : hosts) {
        try {
          logger.info("Resolving host name: " + host + "...");
          InetAddress addrs[] = InetAddress.getAllByName(host);
          for (InetAddress addr : addrs) {
            // If hostIPv4 is enabled, skip all except IPv4 (ie, IPv6).
            if (ipv4Only && !(addr instanceof Inet4Address)) {
              logger.warn("Skipping IPv6 address: " + addr.getHostAddress());
              continue;
            }
            ips.add(addr.getHostAddress());
          }
        } catch (UnknownHostException ex) {
          logger.warn("Can't resolve host address: " + host, ex);
        }
      }

      try {
        // Check if the resolved address contains local node's IP.
        // If it's there, add it to front of the list so that it will
        // get used first.
        String localIP = Inet4Address.getLocalHost().getHostAddress();
        if (ips.contains(localIP)) {
          logger.info(
              "Found localhost ip (" + localIP + ") in the list. " + "Moving it to the beginning.");
          ips.remove(localIP);
          ips.add(0, localIP);
        }
      } catch (UnknownHostException ex) {
        logger.warn("Can't find local host address", ex);
      }
    }

    // If ips list is empty, use the original hosts.
    if (ips.isEmpty()) {
      ips.addAll(hosts);
    }

    logger.info("Resolved host address for " + hosts + " is " + ips);
    return ips;
  }
}
