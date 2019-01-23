package com.oneops.cms.dj.domain;

public class RfcHint {
    public static String PROPAGATION = "{\"propagation\":\"true\"}";
    public static String TOUCH = "touch";
    public static String MONITOR = "monitor";
    public static String ATTACHMENT = "attachment";
    public static String LOG = "log";
    public static String CLOUD = "cloud";

    private String propagation;

    public RfcHint() {
    }

    public RfcHint(String propagation) {
        this.propagation = propagation;
    }

    public String getPropagation() {
        return propagation;
    }

    public void setPropagation(String propagation) {
        this.propagation = propagation;
    }
}
