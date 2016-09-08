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


/**
 * Represents a presence in a jabber chat room. Useful so as
 * to re-use the connections
 */
class ChatPresence {

    private String chatServer;
    private int chatPort;
    private String chatConference;
    private String chatRoom;
    private String chatUser;
    private String stationName;

    /**
     * Constructor
     *
     * @param chatServer     jabber server host
     * @param chatPort       jabber port
     * @param chatConference eg conference.foo.com
     * @param chatRoom       name of the room we are joining
     * @param chatUser       chat user
     * @param stationName    stattion name
     */
    ChatPresence(String chatServer, int chatPort, String chatConference,
                 String chatRoom, String chatUser, String stationName) {
        super();
        this.chatServer = chatServer;
        this.chatPort = chatPort;
        this.chatConference = chatConference;
        this.chatRoom = chatRoom;
        this.chatUser = chatUser;
        this.stationName = stationName;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((chatConference == null) ? 0 : chatConference.hashCode());
        result = prime * result
                + ((stationName == null) ? 0 : stationName.hashCode());
        result = prime * result + chatPort;
        result = prime * result
                + ((chatRoom == null) ? 0 : chatRoom.hashCode());
        result = prime * result
                + ((chatServer == null) ? 0 : chatServer.hashCode());
        result = prime * result
                + ((chatUser == null) ? 0 : chatUser.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        ChatPresence other = (ChatPresence) obj;
        if (chatConference == null) {
            if (other.chatConference != null) {
                return false;
            }
        } else if (!chatConference.equals(other.chatConference)) {
            return false;
        }

        if (stationName == null) {
            if (other.stationName != null) {
                return false;
            }
        } else if (!stationName.equals(other.stationName)) {
            return false;
        }

        if (chatPort != other.chatPort) {
            return false;
        }
        if (chatRoom == null) {
            if (other.chatRoom != null) {
                return false;
            }
        } else if (!chatRoom.equals(other.chatRoom)) {
            return false;
        }

        if (chatServer == null) {
            if (other.chatServer != null) {
                return false;
            }
        } else if (!chatServer.equals(other.chatServer)) {
            return false;
        }
        if (chatUser == null) {
            if (other.chatUser != null) {
                return false;
            }
        } else if (!chatUser.equals(other.chatUser)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ChatPresence{ ");
        sb.append("chatServer='").append(chatServer).append('\'');
        sb.append(", chatPort=").append(chatPort);
        sb.append(", chatConference='").append(chatConference).append('\'');
        sb.append(", chatRoom='").append(chatRoom).append('\'');
        sb.append(", chatUser='").append(chatUser).append('\'');
        sb.append(", stationName='").append(stationName).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
