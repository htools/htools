package io.github.htools.hadoop;

import io.github.htools.lib.Log;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

/**
 * Helper class for {@link InputFormat}, that filters the files. By configuring
 * "entityreader.validfilenamestart" and/or "entityreader.validfilenameend",
 * files will only be used when they meet these requirements, or all files if
 * these are not specified. Files can also be excluded, by configuring
 * "entityreader.invalidfilenamestart" and/or "entityreader.invalidfilenameend",
 * which override the settings for valid filenames.
 * @author jer
 */
public class FileFilter {

   public static final String[] empty = new String[0];
   String[] validFilenameStart;
   String[] validFilenameEnd;
   String[] invalidFilenameStart;
   String[] invalidFilenameEnd;

   public FileFilter(Configuration conf) {
      validFilenameStart = conf.getStrings("entityreader.validfilenamestart", empty);
      validFilenameEnd = conf.getStrings("entityreader.validfilenameend", empty);
      invalidFilenameStart = conf.getStrings("entityreader.invalidfilenamestart", empty);
      invalidFilenameEnd = conf.getStrings("entityreader.invalidfilenameend", empty);
   }

   public boolean acceptFile(Path path) {
      String file = path.getName();
      return startWith(file) && endWith(file) && omitStart(file) && omitEnd(file);
   }

   protected boolean startWith(String file) {
      if (validFilenameStart.length == 0) {
         return true;
      }
      for (String s : validFilenameStart) {
         if (file.startsWith(s)) {
            return true;
         }
      }
      return false;
   }

   protected boolean endWith(String file) {
      if (validFilenameEnd.length == 0) {
         return true;
      }
      for (String s : validFilenameEnd) {
         if (file.endsWith(s)) {
            return true;
         }
      }
      return false;
   }

   protected boolean omitStart(String file) {
      if (invalidFilenameStart.length == 0) {
         return true;
      }
      for (String s : invalidFilenameStart) {
         if (file.startsWith(s)) {
            return false;
         }
      }
      return true;
   }

   protected boolean omitEnd(String file) {
      if (invalidFilenameEnd.length == 0) {
         return true;
      }
      for (String s : invalidFilenameEnd) {
         if (file.endsWith(s)) {
            return false;
         }
      }
      return true;
   }
}
