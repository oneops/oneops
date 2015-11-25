package com.oneops.antenna.domain;

/**
 * Jabber sink subscriber
 */
public class XMPPSubscriber extends BasicSubscriber {

    private String chatServer;
    private int chatPort;
    private String chatRoom;
    private String chatConference;
    private String chatUser;
    private String chatPassword;

    /**
     * @return the chatServer
     */
    public String getChatServer() {
        return chatServer;
    }

    /**
     * @param chatServer the chatServer to set
     */
    public void setChatServer(String chatServer) {
        this.chatServer = chatServer;
    }

    /**
     * @return the chatPort
     */
    public int getChatPort() {
        return chatPort;
    }

    /**
     * @param chatPort the chatPort to set
     */
    public void setChatPort(int chatPort) {
        this.chatPort = chatPort;
    }

    /**
     * @return the chatRoom
     */
    public String getChatRoom() {
        return chatRoom;
    }

    /**
     * @param chatRoom the chatRoom to set
     */
    public void setChatRoom(String chatRoom) {
        this.chatRoom = chatRoom;
    }

    /**
     * @return the chatConference
     */
    public String getChatConference() {
        return chatConference;
    }

    /**
     * @param chatConference the chatConference to set
     */
    public void setChatConference(String chatConference) {
        this.chatConference = chatConference;
    }

    /**
     * @return the chatUser
     */
    public String getChatUser() {
        return chatUser;
    }

    /**
     * @param chatUser the chatUser to set
     */
    public void setChatUser(String chatUser) {
        this.chatUser = chatUser;
    }

    /**
     * @return the chatPassword
     */
    public String getChatPassword() {
        return chatPassword;
    }

    /**
     * @param chatPassword the chatPassword to set
     */
    public void setChatPassword(String chatPassword) {
        this.chatPassword = chatPassword;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("XMPPSubscriber{ ");
        sb.append("name='").append(getName()).append('\'');
        sb.append(", chatServer='").append(chatServer).append('\'');
        sb.append(", chatPort=").append(chatPort);
        sb.append(", chatRoom='").append(chatRoom).append('\'');
        sb.append(", chatConference='").append(chatConference).append('\'');
        sb.append(", chatUser='").append(chatUser).append('\'');
        sb.append(", chatPassword='").append("*no*").append('\'');
        sb.append('}');
        return sb.toString();
    }
}
