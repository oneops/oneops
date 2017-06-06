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

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smackx.muc.MultiUserChat;

/**
 * Holds a Session with a chat server and a MultiUserChat entry which
 * was opened by that Session. Essentially a tuple class we can use
 * to hold the objects, and close the Session when needed.
 */
class ChatRoomConnection {

    private final MultiUserChat multiUserChat;
    private final Connection connection;

    /**
     * constructor for all fields
     *
     * @param multiUserChat chat room object
     * @param connection    xmpp connection
     */
    ChatRoomConnection(MultiUserChat multiUserChat, Connection connection) {
        this.multiUserChat = multiUserChat;
        this.connection = connection;
    }

    /**
     * @return the multiUserChat
     */
    MultiUserChat getMultiUserChat() {
        return multiUserChat;
    }

    /**
     * @return the connection
     */
    Connection getConnection() {
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
