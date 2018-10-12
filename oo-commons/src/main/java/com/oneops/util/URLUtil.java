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

import com.oneops.notification.NotificationMessage;
import com.oneops.notification.NotificationType;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.apache.log4j.Logger;

/**
 * URL utility class containing methods for constructing different notification urls. This is mainly
 * used in the Antenna subsystem.
 */
public class URLUtil {

  private static final Logger logger = Logger.getLogger(URLUtil.class);
  private static final String ONEOPS_URL_PROP = "oneops.url";
  public static final String ONEOPS_BASE_URL;

  static {
    String oourl = System.getProperty(ONEOPS_URL_PROP);
    if (oourl == null || !isValidURL(oourl)) {
      throw new IllegalStateException(
          "Can't find/invalid  " + ONEOPS_URL_PROP + " system property. OneOps BaseUrl = " + oourl);
    }
    if (!oourl.endsWith("/")) {
      oourl += "/";
    }
    ONEOPS_BASE_URL = oourl;
  }

  private URLUtil() {}

  /**
   * Checks the given string is valid url conforming to rfc3986.
   *
   * @param url url string
   * @return <code>true</code> if it conforming to the standard, else return <code>false</code>
   */
  @SuppressWarnings("ResultOfMethodCallIgnored")
  public static boolean isValidURL(String url) {
    try {
      URI uri = new URI(url);
      uri.toURL();
      return true;
    } catch (Exception e) {
      logger.warn(url + " is not a valid url, error: " + e.getMessage());
      return false;
    }
  }

  /**
   * Assembles a OneOps URL which is a link to more details for given nspath, and NotificationType.
   * Will return <code>null</code> if parameters cannot be used to make a good URL.
   *
   * <p>* Deployment nsPath logic <br>
   * Form : /{org}/{assembly}/{env}/bom <br>
   * URL :
   * https://oneops.prod.walmart.com/{org}/assemblies/{assembly}/transition/environments/{env}#summary
   *
   * <p>* Procedure nsPath logic <br>
   * From : /{org}/{assembly}/{env}/bom/{platform}/1/{cid} <br>
   * URL : https://oneops.prod.walmart.com/r/ci/{cid}[?org_name={org}]
   *
   * @param msg a NotificationMessage
   * @return URL into OneOps for the nspath and NotificationType
   */
  public static URL getNotificationUrl(NotificationMessage msg) {
    URL url = null;
    String nsPath = msg.getNsPath();
    NotificationType nsType = msg.getType();
    try {
      StringBuilder sb = new StringBuilder();
      sb.append(ONEOPS_BASE_URL);
      switch (nsType) {
        case deployment:
          String[] nsParts = nsPath.split("/");
          if (nsParts.length >= 4) {
            sb.append(nsParts[1])
                .append("/assemblies/")
                .append(nsParts[2])
                .append("/transition/environments/")
                .append(nsParts[3])
                .append("#summary");
          } else {
            logger.error("Not a valid nsPath " + nsPath + " for " + nsType + " notification.");
          }
          break;
        default:
          sb.append("r/ci/").append(msg.getCmsId());
          break;
      }
      url = new URL(sb.toString());
      logger.debug("URL for nsPath: " + nsPath + " and " + nsType + " = " + url);
    } catch (MalformedURLException e) {
      logger.error("Cannot create URL for nsPath: " + nsPath + " and " + nsType, e);
    }
    return url;
  }

  /**
   * Returns OneOps instance redirect url for the cid of the given notification message. Instance
   * redirect url format is, https://oneops.prod.walmart.com/r/instances/{cid}
   *
   * @param msg Notification message
   * @return instance redirect url string
   */
  public static String getInstanceRedirectUrl(NotificationMessage msg) {
    return String.format("%1$sr/instances/%2$d", ONEOPS_BASE_URL, msg.getCmsId());
  }

  /**
   * Returns OneOps instance monitor redirect url for the given notification message. Instance
   * redirect url format is,
   * https://oneops.prod.walmart.com/r/instances/{cid}/monitors/{eventName}/d
   *
   * @param msg Notification message
   * @return instance monitor redirect url string
   */
  public static String getMonitorRedirectUrl(NotificationMessage msg) {
    if (msg.getPayload() != null) {
      String eventName = msg.getPayloadString("eventName");
      return String.format(
          "%1$sr/instances/%2$d/monitors/%3$s/d", ONEOPS_BASE_URL, msg.getCmsId(), eventName);
      // Fallback to instance url if it couldn't find an event.
    } else {
      return getInstanceRedirectUrl(msg);
    }
  }
}
