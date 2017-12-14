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

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Common path util functions.
 *
 * @author Suresh G
 */
public class PathUtils {

  /**
   * Deletes file/directory representing the path.
   *
   * @param path file/dir path
   * @throws IOException throws if any error deleting the file/dir.
   */
  public static void delete(Path path) throws IOException {
    if (path.toFile().exists()) {
      try (Stream<Path> stream = Files.walk(path)) {
        // Sort the list in reverse order, so the dir comes after the including sub-dirs and files.
        stream.sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .forEach(File::delete);
      }
    }
  }

  /**
   * Copy the src file/dir to the dest path recursively.
   * This will replace the file if it's already exists.
   *
   * @param src source file /dir path
   * @param dest destination path to copy
   * @param globPattern glob patterns to filter out when copying.
   * Note: Glob pattern matching is case sensitive.
   * @throws IOException throws if any error copying the file/dir.
   */
  public static void copy(Path src, Path dest, List<String> globPattern) throws IOException {
    // Make sure the target dir exists.
    dest.toFile().mkdirs();

    // Create path matcher from list of glob pattern strings.
    FileSystem fs = FileSystems.getDefault();
    List<PathMatcher> matchers = globPattern.stream()
        .map(pattern -> fs.getPathMatcher("glob:" + pattern))
        .collect(Collectors.toList());

    try (Stream<Path> stream = Files.walk(src)) {
      // Filter out Glob pattern
      stream.filter(path -> {
        Path name = src.relativize(path);
        return matchers.stream().noneMatch(m -> m.matches(name));
      }).forEach(srcPath -> {
        try {
          Path target = dest.resolve(src.relativize(srcPath));
          // Don't try to copy existing dir entry.
          if (!target.toFile().isDirectory()) {
            Files.copy(srcPath, target, REPLACE_EXISTING);
          }
        } catch (IOException e) {
          throw new IllegalStateException(e);
        }
      });
    }
  }

  /**
   * Copy the src file/dir to the dest path recursively.
   * This will replace the file if it's already exists.
   *
   * @param src source file /dir path
   * @param dest destination path to copy
   * @throws IOException throws if any error copying the file/dir.
   */
  public static void copy(Path src, Path dest) throws IOException {
    copy(src, dest, Collections.emptyList());
  }

}
