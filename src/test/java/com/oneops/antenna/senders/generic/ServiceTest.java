package com.oneops.antenna.senders.generic;

import org.testng.annotations.Test;

import com.oneops.antenna.domain.NotificationMessage;
import com.oneops.antenna.domain.NotificationSeverity;
import com.oneops.antenna.domain.NotificationType;
import com.oneops.antenna.domain.XMPPSubscriber;
import com.oneops.antenna.senders.generic.LoggingMsgService;

public class ServiceTest {

    @Test
    public void simpleLogTest() {
        LoggingMsgService service = new LoggingMsgService();
        XMPPSubscriber subscriber = new XMPPSubscriber();

        NotificationMessage msg = new NotificationMessage();
        msg.setNsPath("/a/b/c/d/e/f");
        msg.setSource("sourceOpen");
        msg.setSubject("subjectOfMsg");
        msg.setText("message-text-xya");
        msg.setType(NotificationType.deployment);
        msg.setSeverity(NotificationSeverity.critical);
        msg.setTemplateName("template12");
        msg.setTimestamp(System.currentTimeMillis());
        boolean res = service.postMessage(msg, subscriber);
        assert (res);
    }
}
