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
package com.oneops.inductor;

import com.google.gson.Gson;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class OutputHandler {
    private Logger logger;
    private Level level;
    private String logKey;
    private static String REBOOT_FLAG = "***REBOOT_FLAG***";
    private static String RESULT_KEY = "***RESULT:";
    private static String FAULT_KEY = "***FAULT:";
    private static String TAG_KEY = "***TAG:";
    private static String RESULTJSON_KEY = "***RESULTJSON:";
    private static String ADDITIONAL_INFO_KEY = "***ADDITIONAL_INFO:";
    private static String PRIVATE_KEY_TEMP_TAG = "#PRIVATE_KEY#";

    private ProcessResult result;

    final private Gson gson = new Gson();

    // limit amt of output a cmd can log
    private int rowCount = 0;
    private static int maxRowCount = 2000;

    public OutputHandler(Logger logger, String logKey, ProcessResult result) {
        setLogger(logger);
        setLevel(Level.ALL);
        setLogKey(logKey);
        this.result = result;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setLevel(Level level) {
        this.level = level;
    }

    public Level getLevel() {
        return level;
    }

    public void setLogKey(String logKey) {
        this.logKey = logKey;
    }

    public String getLogKey() {
        return logKey;
    }

    private String getPrivateKey(final String line) {
        String beginRSAPrivateKeyTag = "-----BEGIN RSA PRIVATE KEY-----";
        String endRSAPrivateKeyTag = "-----END RSA PRIVATE KEY-----\\n";
        int startIndex = line.indexOf(beginRSAPrivateKeyTag);
        int endIndex = line.indexOf(endRSAPrivateKeyTag) + endRSAPrivateKeyTag.length();
        String privateKey = line.substring(startIndex, endIndex);
        return privateKey;
    }

    public void writeOutputToLogger(String line) {
        if (rowCount < maxRowCount && line.length() > 0) {
            if (!line.contains("PRIVATE KEY")) {
                logger.info(logKey + "cmd out: " + line);
                result.appendStdOut(line + "\n");
            } else {
                String privateKey = getPrivateKey(line);
                line = line.replace(privateKey, PRIVATE_KEY_TEMP_TAG);
                logger.info(logKey + "cmd out: " + line);
                result.appendStdOut(line + "\n");
                line = line.replace(PRIVATE_KEY_TEMP_TAG, privateKey);
            }

            if (line.indexOf(REBOOT_FLAG) > -1)
                result.setRebooting(true);

            int keyIndex = line.indexOf(RESULT_KEY);
            if (keyIndex == 0) {
                String withOutResultKey = line.substring(keyIndex + 10, line.length());
                String k = withOutResultKey.substring(0, withOutResultKey.indexOf("="));
                String v = withOutResultKey.substring(withOutResultKey.indexOf("=") + 1, withOutResultKey.length());
                result.getResultMap().put(k, v);
                logger.debug(logKey + " resultCi " + k + ": " + v);
            }

            keyIndex = line.indexOf(FAULT_KEY);
            if (keyIndex == 0) {
                String withOutResultKey = line.substring(keyIndex + 9, line.length());
                String k = withOutResultKey.substring(0, withOutResultKey.indexOf("="));
                String v = withOutResultKey.substring(withOutResultKey.indexOf("=") + 1, withOutResultKey.length());
                result.getFaultMap().put(k, v);
                logger.info(logKey + " fault: " + k + ": " + v);
            }

            keyIndex = line.indexOf(TAG_KEY);
            if (keyIndex == 0) {
                String withOutResultKey = line.substring(keyIndex + 7, line.length());
                String k = withOutResultKey.substring(0, withOutResultKey.indexOf("="));
                String v = withOutResultKey.substring(withOutResultKey.indexOf("=") + 1, withOutResultKey.length());
                result.getTagMap().put(k, v);
                logger.info(logKey + " tag: " + k + ": " + v);
            }

            // multi-line attributes encoded w/ json
            keyIndex = line.indexOf(RESULTJSON_KEY);
            if (keyIndex == 0) {
                int firstEquals = line.indexOf("=");
                String key = line.substring(keyIndex + 14, firstEquals);
                String val = line.substring(firstEquals + 1, line.length());

                MultiLineValue value = gson.fromJson(val, MultiLineValue.class);

                result.getResultMap().put(key, value.getValue());
                if (!value.getValue().contains("PRIVATE KEY"))
                    logger.debug(logKey + " resultCi " + key + ": " + val);

            }

            keyIndex = line.indexOf(ADDITIONAL_INFO_KEY);
            if (keyIndex > -1) {
                int firstEquals = line.indexOf("=", keyIndex);
                if (firstEquals > -1) {
                    String key = line.substring(keyIndex + ADDITIONAL_INFO_KEY.length(), firstEquals).trim();
                    String value = line.substring(firstEquals + 1);
                    logger.info(ADDITIONAL_INFO_KEY + " key: " + key + ", value: " + value);
                    result.getAdditionInfoMap().put(key, value);
                }
            }

            // set last error to use if faults are empty
            keyIndex = line.indexOf("ERROR:");
            if (keyIndex > -1) {
                result.setLastError(line.substring(keyIndex + 6, line.length()));
            }

        } else if (rowCount == maxRowCount) {
            logger.warn(logKey + " hit max amount of output per process of " + maxRowCount
                    + " lines. Please run the workorder on the box: chef-solo -c /home/oneops/cookbooks/chef.rb -j /opt/oneops/workorder/someworkorder ");
        }
        rowCount++;
    }
}