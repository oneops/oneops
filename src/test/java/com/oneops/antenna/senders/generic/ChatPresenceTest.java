package com.oneops.antenna.senders.generic;

import org.testng.annotations.Test;

import com.oneops.antenna.senders.generic.ChatPresence;

import static org.testng.Assert.*;


public class ChatPresenceTest {

    private static final String chatServer = "127.0.0.1";
    private static final int chatPort = Integer.MAX_VALUE;
    private static final String chatConference = "a";
    private static final String chatRoom = "b";
    private static final String chatUser = "c";
    private static final String chatPassword = "terces";

    private static final ChatPresence classCp1 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom, chatUser, chatPassword);


    @Test
    public void equalsTest() {
        ChatPresence c1 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom, chatUser, chatPassword);
        ChatPresence c2 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom, chatUser, chatPassword);
        assertEquals(c1, c2);
    }

    @Test
    public void unequalsTest() {

        ChatPresence localCp1 = new ChatPresence(chatServer + "X", chatPort, chatConference, chatRoom, chatUser, chatPassword);
        assertFalse(classCp1.equals(localCp1));


        localCp1 = new ChatPresence(chatServer, chatPort + 100, chatConference, chatRoom, chatUser, chatPassword);
        assertFalse(classCp1.equals(localCp1));

        localCp1 = new ChatPresence(chatServer, chatPort, chatConference + "XXXX", chatRoom, chatUser, chatPassword);
        assertFalse(classCp1.equals(localCp1));


        localCp1 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom + "yyy", chatUser, chatPassword);
        assertFalse(classCp1.equals(localCp1));


        localCp1 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom, chatUser + "cuser", chatPassword);
        assertFalse(classCp1.equals(localCp1));


        localCp1 = new ChatPresence(chatServer, chatPort, chatConference, chatRoom, chatUser, null);
        assertFalse(classCp1.equals(localCp1));
    }

}
