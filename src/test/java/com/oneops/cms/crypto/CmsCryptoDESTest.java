package com.oneops.cms.crypto;

import com.oneops.cms.crypto.CmsCryptoDES;
import com.oneops.cms.exceptions.CmsException;

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

}


