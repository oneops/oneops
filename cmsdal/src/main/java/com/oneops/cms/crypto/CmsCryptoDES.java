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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.crypto.KeyGenerationParameters;
import org.bouncycastle.crypto.engines.DESedeEngine;
import org.bouncycastle.crypto.generators.DESedeKeyGenerator;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.DESedeParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.Security;

/**
 * The Class CmsCryptoDES.
 */
public class CmsCryptoDES implements CmsCrypto {

    private String secretKeyFile;
    private static Logger logger = Logger.getLogger(CmsCryptoDES.class);
    private static final int MIN_DES_FILE_LENGTH = 10;
    private KeyParameter keyParameter = null;


    /**
     * Encrypt.
     *
     * @param instr the instr
     * @return the string
     * @throws java.security.GeneralSecurityException the general security exception
     */
    @Override
    public String encrypt(String instr) throws GeneralSecurityException {
        long t1 = System.currentTimeMillis();
        byte[] in = instr.getBytes();
        PaddedBufferedBlockCipher encryptor = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new DESedeEngine()));
        encryptor.init(true, keyParameter);
        byte[] cipherText = new byte[encryptor.getOutputSize(in.length)];
        int outputLen = encryptor.processBytes(in, 0, in.length, cipherText, 0);
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            encryptor.doFinal(cipherText, outputLen);
            Hex.encode(cipherText, os);
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralSecurityException(e);
        }
        long t2 = System.currentTimeMillis();
        logger.debug("Time taken to encrypt(millis) :" + (t2 - t1));
        return ENC_PREFIX + os.toString();
    }

    /**
     * Decrypt.
     *
     * @param instr the instr
     * @return the string
     * @throws java.security.GeneralSecurityException the general security exception
     */
    @Override
    public String decrypt(String instr) throws GeneralSecurityException {
        if (instr.startsWith(ENC_PREFIX)) {
            instr = instr.substring(ENC_PREFIX.length());
        }
        return decryptStr(instr);
    }
    
    /**
     * DecryptVars. Parse the str, extract encrypted parts and decrypt
     *
     * @param instr the instr
     * @return the decrypted string
     * @throws java.security.GeneralSecurityException the general security exception
     */

    @Override
	public String decryptVars(String instr) throws GeneralSecurityException {
    	StringBuffer sb = new StringBuffer();
    	int startingPoint = 0;
    	while (instr.contains(ENC_VAR_PREFIX)) {
    		sb.append(instr.substring(startingPoint, instr.indexOf(ENC_VAR_PREFIX)));
    		String strToDecrypt = instr.substring(instr.indexOf(ENC_VAR_PREFIX) + ENC_VAR_PREFIX.length(),instr.indexOf(ENC_VAR_SUFFIX));
    		sb.append(decryptStr(strToDecrypt));
    		instr = instr.substring(instr.indexOf(ENC_VAR_SUFFIX) + ENC_VAR_SUFFIX.length());
    	}
		sb.append(instr);
    	return sb.toString();
	}


    private String decryptStr(String instr) throws GeneralSecurityException {
        if(StringUtils.isEmpty(instr)){
            return instr;
        }
        long t1 = System.currentTimeMillis();
        PaddedBufferedBlockCipher decryptor = new PaddedBufferedBlockCipher(
                new CBCBlockCipher(new DESedeEngine()));
        decryptor.init(false, keyParameter);
        byte[] in = null;
        byte[] cipherText = null;

        try {
        	in = Hex.decode(instr);
        	cipherText = new byte[decryptor.getOutputSize(in.length)];

	        int outputLen = decryptor.processBytes(in, 0, in.length, cipherText, 0);
            decryptor.doFinal(cipherText, outputLen);
        } catch (Exception e) {
            throw new GeneralSecurityException(e);
        }
        long t2 = System.currentTimeMillis();
        logger.debug("Time taken to decrypt(millis) : " + (t2 - t1));
        return (new String(cipherText)).replaceAll("\\u0000+$", "");
    }



    /**
     * Inits the.
     *
     * @throws java.io.IOException              Signals that an I/O exception has occurred.
     * @throws java.security.GeneralSecurityException the general security exception
     */
    public void init() throws IOException, GeneralSecurityException {
        Security.addProvider(new BouncyCastleProvider());
        this.secretKeyFile = System.getenv("CMS_DES_PEM");
        if (this.secretKeyFile == null) {
            this.secretKeyFile = System.getProperty("com.kloopz.crypto.cms_des_pem");
        }

        if (this.secretKeyFile == null) {
            logger.error(">>>>>>>>>>>>>>Failed to init DES Encryptor/Decryptor no key faile is set, use CMS_DES_PEM env var to set location!");
            throw new FileNotFoundException("Failed to init DES Encryptor/Decryptor no key faile is set, use CMS_DES_PEM env var to set location!");
        }
        initEncryptorDecryptor();
    }


    private void initEncryptorDecryptor() throws GeneralSecurityException, IOException {
        if (this.secretKeyFile != null) {
            this.keyParameter = getSecretKeyFromFile();
            logger.info(">>>>>>>>>>>>>Successfully read the key file.");
        }
    }


    private KeyParameter getSecretKeyFromFile() throws IOException, GeneralSecurityException {
        BufferedInputStream keystream =
                new BufferedInputStream(new FileInputStream(secretKeyFile));
        int len = keystream.available();
        if (len < MIN_DES_FILE_LENGTH) {
            keystream.close();
            throw new EOFException(">>>> Bad DES file length = " + len);
        }
        byte[] keyhex = new byte[len];
        keystream.read(keyhex, 0, len);
        keystream.close();
        return new KeyParameter(Hex.decode(keyhex));
    }

    /**
     * Generate des key.
     *
     * @param file the file
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     */
    public static void generateDESKey(String file) throws IOException {
        DESedeKeyGenerator kg = new DESedeKeyGenerator();
        KeyGenerationParameters kgp = new KeyGenerationParameters(
                new SecureRandom(),
                DESedeParameters.DES_EDE_KEY_LENGTH * 8);
        kg.init(kgp);
        byte[] key = kg.generateKey();
        BufferedOutputStream keystream =
                new BufferedOutputStream(new FileOutputStream(file));
        byte[] keyhex = Hex.encode(key);
        keystream.write(keyhex, 0, keyhex.length);
        keystream.flush();
        keystream.close();
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Usage: java " + CmsCryptoDES.class.getName() + " keyfile");
            System.exit(1);
        }

        System.out.println("Generate DES key file");

        Security.addProvider(new BouncyCastleProvider());
        try {
            CmsCryptoDES.generateDESKey(args[0]);
            System.out.println("DES key generated successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    /**
     * Sets the secret key file.
     *
     * @param secretKeyFile the new secret key file
     */
    public void setSecretKeyFile(String secretKeyFile) {
        this.secretKeyFile = secretKeyFile;
    }

    /**
     * Used for testing.
     *
     * @param secretKeyFile the secret key file
     * @throws java.security.GeneralSecurityException the general security exception
     * @throws java.io.IOException Signals that an I/O exception has occurred.
     */
    public void init(String secretKeyFile) throws GeneralSecurityException, IOException {
        this.secretKeyFile = secretKeyFile;
        initEncryptorDecryptor();
    }

}
