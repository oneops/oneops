package com.oneops.ecv.auth;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;


/**
 * Class to authorize http exception.
 */

public class AuthUtil {

    private static final Logger logger = Logger.getLogger(AuthUtil.class);
    private static final String HTTP_REQUEST_HEADER_AUTH_BASIC_VAL_PREFIX = "Basic";
    private static final String HTTP_REQUEST_HEADER_AUTH_BASIC_VAL_SEPARATOR = ":";

    private String user;
    private String secret;


    public boolean authenticate(String authString) {
        if (authString == null || "".equals(authString.trim())) {
            return false;
        }
        String[] authTokens = authString.split("\\s");
        for (String token : authTokens) {
            if (HTTP_REQUEST_HEADER_AUTH_BASIC_VAL_PREFIX.equals(token)) continue;
            String credential = new String(Base64.decodeBase64(token));
            if (credential.indexOf(HTTP_REQUEST_HEADER_AUTH_BASIC_VAL_SEPARATOR) != -1) {
                String[] credentials = credential.split(":");
                if (credentials.length != 2) return false;
                String requestUserName = credentials[0];
                String requestUserCred = credentials[1];
                if (user.equals(requestUserName) && secret.equals(requestUserCred)) {
                    return true;
                }
            }
        }
        return false;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

}
