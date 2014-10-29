package io.github.repir.tools.Words;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Words.englishStemmer;
import java.util.HashSet;

/**
 * Stop word that compensate for tokenized url's, which do not work well with PRF 
 */
public class StopWordsUrl {
   public static Log log = new Log( StopWordsUrl.class );
   
   public static String filterarray[] = {
      "www", "http", "com", "html", "org", "net", "ca", "edu", "au", "co", "xhtml", "dtd", "htftp", "hyttp", "main", "htm", "en",  
   };

   public static HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
         set.add(s);
      }
      return set;
   }
}
