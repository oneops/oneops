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
package com.oneops.antenna.senders.generic;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.oneops.antenna.domain.BasicSubscriber;
import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.XMPPSubscriber;
import com.oneops.antenna.senders.NotificationSender;

import org.apache.log4j.Logger;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.XHTMLExtension;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.codahale.metrics.MetricRegistry.name;
import static com.oneops.metrics.OneOpsMetrics.ANTENNA;

/**
 * Send Jabber (XMPP) messages using smack api.
 */
public class XMPPMsgService implements NotificationSender {

    private static final Logger logger = Logger.getLogger(XMPPMsgService.class);
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy");
    private ConcurrentMap<ChatPresence, ChatRoomConnection> connCache = new ConcurrentHashMap<>();

    private String botName;
    private int timeout;
    private int timesConnecting = 0;

    // Metrics
    private final MetricRegistry metrics;
    private Meter xmpp;
    private Meter xmppErr;

    @Autowired
    public XMPPMsgService(MetricRegistry metrics) {
        this.metrics = metrics;
    }


    @PostConstruct
    public void init() {
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        xmpp = metrics.meter(name(ANTENNA, "xmpp.count"));
        xmppErr = metrics.meter(name(ANTENNA, "xmpp.error"));
    }

    @Override
    public boolean postMessage(NotificationMessage msg, BasicSubscriber sub) {

        if (msg == null) throw new IllegalArgumentException("XMPP notification msg is null");
        XMPPSubscriber xmppSub = (XMPPSubscriber) sub;

        String[] nsParts = msg.getNsPath().split("/");
        String orgName = nsParts[1];

        ChatPresence presence = new ChatPresence(xmppSub.getChatServer(),
                xmppSub.getChatPort(),
                xmppSub.getChatConference(),
                xmppSub.getChatRoom(),
                xmppSub.getChatUser(),
                orgName);

        MultiUserChat ourRoom = null;
        if (connCache.containsKey(presence)) {
            if (connCache.get(presence).getConnection().isConnected()) {
                if (connCache.get(presence).getMultiUserChat().isJoined()) {
                    ourRoom = connCache.get(presence).getMultiUserChat();
                    logger.debug("Reusing old connection. Using chat room " + presence);
                } else {
                    logger.debug("Old connection is alive but no longer in room. Disconnecting " + presence);
                    connCache.get(presence).getConnection().disconnect();
                    // Assume conn has timed-out, disconnect and throw it away
                    connCache.remove(presence);
                }
            } else {
                logger.debug("Old connection we held no longer connected " + presence);
                connCache.remove(presence);
            }
        } else {
            logger.info("Have no usable saved xmpp connections for " + presence);
        }

        Connection xmppConn = null;
        if (ourRoom == null) {
            try {
                ConnectionConfiguration config = new ConnectionConfiguration(
                        xmppSub.getChatServer(), xmppSub.getChatPort());
                config.setRosterLoadedAtLogin(false);
                config.setSendPresence(false);

                xmppConn = new XMPPConnection(config);
                String identity = orgName + "-" + xmppSub.getName();
                logger.info("Creating new xmpp connection. Identity= "
                        + identity + ": " + ++timesConnecting + ", " + presence);
                xmppConn.connect();
                /*
                 * Simultaneous logins are ok for one account if you pass a
                 * unique 3rd value here typically this for one person with
                 * more than one device like home,work,tablet etc.
                 */
                xmppConn.login(xmppSub.getChatUser(), xmppSub.getChatPassword(), identity);
                ourRoom = new MultiUserChat(xmppConn, xmppSub.getChatRoom()
                        + "@" + xmppSub.getChatConference());
                /*
                 * Default config used here as we want any new room open
                 * to public. No password supplied, and no history wanted.
                 */
                ourRoom.join(botName, null, null, timeout);
                logger.debug("Sending the configuration form now. " +
                        "This should unlock the room for any other users to see.");
                try {
                    ourRoom.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
                } catch (XMPPException e) {
                    logger.debug("Registering new room threw exception, but continuing." +
                            "Another thread may have beat us, its ok", e);
                }

                ChatRoomConnection crs = this.connCache.putIfAbsent(presence,
                        new ChatRoomConnection(ourRoom, xmppConn));
                if (crs == null) {
                    logger.debug("Have set-up and saved conn: " + presence
                            + " using resource name: " + identity
                            + " to this map for reuse: " + this.connCache);
                } else {
                    xmppConn.disconnect();
                    logger.debug("When saving connection it was already there: " + this.connCache);
                }

            } catch (XMPPException xe) {
                xmppErr.mark();
                logger.warn("Could not connect to xmpp chat: " + presence, xe);
                // Disconnect to avoid connection leak.
                if (xmppConn != null) {
                    xmppConn.disconnect();
                }
                return false;
            }
        }

        try {
            // Flavor #1 plain text
            StringBuilder plainMsg = new StringBuilder(msg.getSeverity().getName().toUpperCase());
            String msgTimeStamp = dateFormat.format(new Date(msg.getTimestamp()));
            plainMsg.append(" | ")
                    .append(msg.getType().getName())
                    .append(" | ")
                    .append(msgTimeStamp)
                    .append(" | ")
                    .append(msg.getNsPath())
                    .append("<br>")
                    .append(msg.getSubject())
                    .append("<br>")
                    .append(msg.getText());

            // Flavor #2 XHTML
            StringBuilder fmtMsg = new StringBuilder();
            URL url = msg.getNotificationUrl();
            fmtMsg.append("<body>")
                    .append(msg.getSeverity().getName().toUpperCase())
                    .append(" | ")
                    .append(msg.getType().getName())
                    .append(" | ")
                    .append(msgTimeStamp).append(" | ")
                    .append((url != null ? linkifyUrl(msg.getNsPath(), url) : "_" + msg.getNsPath()))
                    .append("<br/>")
                    .append(msg.getText())
                    .append("</body>");

            Message packetMessage = ourRoom.createMessage();
            packetMessage.setSubject(msg.getSubject());
            packetMessage.setBody(plainMsg.toString());
            // Create a XHTMLExtension Package.
            XHTMLExtension xhtmlExtension = new XHTMLExtension();
            xhtmlExtension.addBody(fmtMsg.toString());
            packetMessage.addExtension(xhtmlExtension);
            ourRoom.sendMessage(packetMessage);
            logger.debug("Message has been sent to chat room: "
                    + xmppSub.getChatRoom() + ", msg: " + plainMsg);
        } catch (XMPPException e) {
            logger.warn("Could not send xmpp message to chat: " + presence, e);
            xmppErr.mark();
            return false;
        }

        xmpp.mark();
        return true;
    }

    /**
     * Creates HTML hyperlink for the url
     *
     * @param hyperlinkText
     * @param url
     * @return anchor tag
     */
    private String linkifyUrl(String hyperlinkText, URL url) {
        return String.format("<a href=\"%1$s\">%2$s</a>", url, hyperlinkText);
    }

    /**
     * @return the botName we use in the room
     */
    public String getBotName() {
        return botName;
    }


    /**
     * @param botName to show in the Chat room
     */
    public void setBotName(String botName) {
        this.botName = botName;
    }

    /**
     * @return the timeout
     */
    public int getTimeout() {
        return timeout;
    }


    /**
     * @param timeout the timeout limit for
     *                when trying to connect to chat
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    /**
     * Clean up connections if any
     */
    @Override
    protected void finalize() throws Throwable {
        for (Entry<ChatPresence, ChatRoomConnection> conn : connCache.entrySet()) {
            try {
                conn.getValue().getConnection().disconnect();
            } catch (Exception e) {
                logger.warn("Exception cleaning up connections", e);
            }
        }
        super.finalize();
    }
}
