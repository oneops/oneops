package com.oneops.ecv.config;

/**
 * Created by glall on 3/1/15.
 */
public class ConfigException extends RuntimeException {

    public ConfigException() {
        super();
    }

    public ConfigException(Throwable cause) {
        super(cause);
    }

    public ConfigException(String message) {
        super(message);
    }

    public ConfigException(String message, Throwable cause) {
        super(message, cause);
    }

}
