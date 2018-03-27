/*******************************************************************************
 *

 *   Copyright 2018 Walmart, Inc.
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
package com.oneops.crawler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonsUtil {

  private final static Logger log = LoggerFactory.getLogger(CommonsUtil.class);
  
    public static String parseOrganizationNameFromNsPath(String path) {

        if (path != null && !path.isEmpty()) {
            String[] parsedArray = path.split("/");
            return parsedArray[1];
        } else {
            return "";
        }
    }

  public static String getFileContent(String indexMappingsJsonFileName) throws IOException {
    String fileAsString = new String();
      InputStream is = ClassLoader.getSystemResourceAsStream(indexMappingsJsonFileName);
      BufferedReader buf = new BufferedReader(new InputStreamReader(is));
      String line = buf.readLine();
      StringBuilder sb = new StringBuilder();
      while (line != null) {
        sb.append(line).append("\n");
        line = buf.readLine();
      }
      fileAsString = sb.toString();
      log.info("Contents : " + fileAsString);
      buf.close();
      return fileAsString;
  }
}
