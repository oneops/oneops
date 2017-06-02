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
package com.oneops.cms.crypto;

import com.oneops.cms.exceptions.CmsException;

import org.apache.commons.lang3.StringUtils;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;


public class CmsCryptoDESTest {


    private CmsCryptoDES crypto;
    private String rawString = UUID.randomUUID().toString();
    private String encryptedString;

    @BeforeClass
    public void init() throws GeneralSecurityException, IOException {
        crypto = new CmsCryptoDES();
        crypto.init(getClass().getResource("/oo.key").getFile());
        encryptedString = crypto.encrypt(rawString);
    }

    @Test(expectedExceptions = CmsException.class)
    /** make sure we fail in the event env is not set up*/
    public void badDESEnvTest() throws Exception {
        CmsCryptoDES crypto1 = new CmsCryptoDES();
        try {
            crypto1.init();
        } catch (FileNotFoundException e) {

            throw new CmsException(33, "File not found, likely a devbox, throwing CmsException");
        }
    }

    @Test(expectedExceptions = IOException.class)
    /** make sure we fail in the event env is not set up*/
    public void badFileTest() throws Exception {
        CmsCryptoDES crypto1 = new CmsCryptoDES();
        crypto1.init("this-is-not-a-key-file");
    }

    @Test(threadPoolSize = 10, invocationCount = 3, timeOut = 10000)
    public void testEncryptDecrypt() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String encryptedUUID = crypto.encrypt(uuid);
        String decryptedUUID = crypto.decrypt(encryptedUUID);
        Assert.assertTrue(uuid.equals(decryptedUUID));
    }

    @Test(threadPoolSize = 10, invocationCount = 3, timeOut = 10000)
    public void testDecrypt() throws Exception {
        String decryptedUUID = crypto.decrypt(encryptedString);
        Assert.assertTrue(rawString.equals(decryptedUUID));
    }

    @Test
    public void testEmptyString() throws Exception {
        String decryptedText = crypto.decrypt(CmsCrypto.ENC_PREFIX);
        Assert.assertTrue(StringUtils.EMPTY.equals(decryptedText));
    }

}


