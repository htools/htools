package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.HashSet;

/**
 * List of single letters.
 */
public class StopWordsLetter {
   public static Log log = new Log( StopWordsLetter.class );
   
   public static String filterarray[] = {
      "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", 
      "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
   };

   public static HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
         set.add(s);
      }
      return set;
   }
}
