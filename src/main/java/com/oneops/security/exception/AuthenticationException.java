package com.oneops.security.exception;

/**
 * This is exception which will be thrown if the basic auth fails.
 */
public class AuthenticationException extends SecurityException {

    public AuthenticationException() {
        super();
    }

    public AuthenticationException(Throwable cause) {
        super(cause);
    }

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }

}
