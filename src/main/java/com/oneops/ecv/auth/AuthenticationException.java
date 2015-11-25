package com.oneops.ecv.auth;

/**
 * This is exception which will be thrown if the basic auth fails.
 */
public class AuthenticationException extends RuntimeException {

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
