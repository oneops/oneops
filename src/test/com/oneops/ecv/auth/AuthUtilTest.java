/*******************************************************************************
 *  
 *   Copyright 2015 Walmart, Inc.
 *  
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *  
 *       http://www.apache.org/licenses/LICENSE-2.0
 *  
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *  
 *******************************************************************************/
package com.oneops.ecv.auth;

import org.apache.commons.codec.binary.Base64;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuthUtilTest {


    private static String SECRET = "secret";
    private static String USER = "user";

    @Test
    public void testAuthenticate() throws Exception {
        AuthUtil authUtil = new AuthUtil();
        authUtil.setSecret(SECRET);
        authUtil.setUser(USER);
        String authHeader = "Basic ";
        String userSecret = USER + ":" + SECRET;
        String authString = String.valueOf(Base64.encodeBase64URLSafeString(userSecret.getBytes()));
        Assert.assertTrue(authUtil.authenticate(authHeader + authString));
    }

    @Test
    public void testAuthenticateInvalidCred() throws Exception {
        AuthUtil authUtil = new AuthUtil();
        authUtil.setSecret(SECRET);
        authUtil.setUser(USER);
        String authHeader = "Basic ";
        String userSecret = USER + ":" + SECRET + "1";
        String authString = String.valueOf(Base64.encodeBase64URLSafeString(userSecret.getBytes()));
        Assert.assertFalse(authUtil.authenticate(authHeader + authString));
    }


}