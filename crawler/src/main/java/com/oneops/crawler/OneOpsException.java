package com.oneops.crawler;

public class OneOpsException extends Exception {
    public OneOpsException(String errorMessage, Exception e) {
        super(errorMessage, e);
    }
    public OneOpsException(String errorMessage) {
        super(errorMessage);
    }
}
