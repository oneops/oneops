/*
 * Copyright 2014-2015 WalmartLabs.
 */
package com.oneops.antenna.senders.generic;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Holds a Session with a chat server and a MultiUserChat entry which
 * was opened by that Session. Essentially a tuple class we can use
 * to hold the objects, and close the Session when needed.
 */
public class ChatRoomConnection {

    private final MultiUserChat multiUserChat;
    private final Connection connection;

    /**
     * constructor for all fields
     *
     * @param multiUserChat chat room object
     * @param connection    xmpp connection
     */
    public ChatRoomConnection(MultiUserChat multiUserChat, Connection connection) {
        this.multiUserChat = multiUserChat;
        this.connection = connection;
    }


    /**
     * @return the multiUserChat
     */
    public MultiUserChat getMultiUserChat() {
        return multiUserChat;
    }

    /**
     * @return the connection
     */
    public Connection getConnection() {
        return connection;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatRoomConnection{");
        sb.append("multiUserChat=").append(multiUserChat);
        sb.append(", connection=").append(connection);
        sb.append('}');
        return sb.toString();
    }
}
