/*******************************************************************************
 *
 *   Copyright 2017 Walmart, Inc.
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
package com.oneops.inductor.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Helper class for reading file resources.
 *
 * @author Suresh
 */
public class ResourceUtils {

  public static final int DEFAULT_BUFFER_SIZE = 4 * 1024;

  /**
   * Gets the entire content of this resource as a byte array. This method is not recommended on
   * huge files.
   *
   * @param name resource name
   * @return byte array..
   */
  public static byte[] readResourceAsBytes(String name) {
    try (InputStream ins = ResourceUtils.class.getResourceAsStream(name)) {
      ByteArrayOutputStream bos = new ByteArrayOutputStream(ins.available());
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

      int read = ins.read(buffer);
      while (read >= 0) {
        bos.write(buffer, 0, read);
        read = ins.read(buffer);
      }
      return bos.toByteArray();

    } catch (IOException e) {
      throw new IllegalStateException("Error reading resource: " + name, e);
    }
  }

  /**
   * Gets the entire content of this resource as a String using UTF-8 encoding. Returns
   * the {@code defaultValue} if there is any error reading the resource.
   *
   * @param name resource name.
   * @param defaultValue default value.
   */
  public static String readResourceAsString(String name, String defaultValue) {
    try {
      return readResourceAsString(name);
    } catch (Exception e) {
      return defaultValue;
    }
  }

  /**
   * Gets the entire content of this resource as a String using UTF-8 encoding.
   *
   * @param name resource name
   * @return string.
   */
  public static String readResourceAsString(String name) {
    return new String(readResourceAsBytes(name), StandardCharsets.UTF_8);
  }

  public static String readExternalFile(String filePath) {
    String fileContent = "";
    try {
      fileContent = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
    } catch (IOException e) {}
    return fileContent;
  }

}
