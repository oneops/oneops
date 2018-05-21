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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class ErrorHandler {
    private Logger logger;
    private Level level;
    private String logKey;
    private ProcessResult result;

    // limit amt of output a cmd can log
    private int rowCount = 0;
    private static int maxRowCount = 2000;

    public ErrorHandler(Logger logger, String logKey, ProcessResult result) {
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

    public void WriteOutputToLogger(String line) {
        if (rowCount < maxRowCount * 2) {

            logger.info(logKey + "cmd error: " + line);
            result.appendStdErr(line + "\n");

        } else if (rowCount == maxRowCount * 2) {
            logger.warn(logKey
                    + " hit max amount of output per process of "
                    + maxRowCount
                    + " lines. Please run the workorder on the box: chef-solo -c /home/oneops/cookbooks/chef.rb -j /opt/oneops/workorder/someworkorder ");
        }
        rowCount++;
    }
}