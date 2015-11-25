package com.oneops.cms.transmitter;

import com.oneops.cms.transmitter.domain.EventSource;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

/**
 * Notification rule
 */
@Root(name = "notification-rule")
public class NotificationRule {

    @Element(required = true)
    private EventSource source;

    @Element(required = false)
    private String clazz;

    @Element(required = false)
    private String name;

    @Element(required = false)
    private String subject;

    @Element(required = false)
    private String message;

    public EventSource getSource() {
        return source;
    }

    public String getClazz() {
        return clazz;
    }

    public String getName() {
        return name;
    }

    public String getSubject() {
        return subject;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "NotificationRule{" +
                "source=" + source +
                ", clazz='" + clazz + '\'' +
                ", name='" + name + '\'' +
                ", subject='" + subject + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
