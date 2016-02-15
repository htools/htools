package io.github.htools.words;

import io.github.htools.lib.Log;

import java.util.HashSet;

/**
 * List of contractions that can be considered stop words.
 */
public class StopWordsContractions {
   public static Log log = new Log( StopWordsContractions.class );
   
   public static String filterarray[] = {
      // removed "us", because we tokenize U.S. as us.
      "ain't", "aren't", "can't", "can't've", "'cause", "could've",
      "couldn't've", "couldn't", "didn't", "doesn't", "don't",
      "hadn't've", "hadn't", "hain't", "hasn't", "haven't", "he'd've",  "he'd",
      "he'll've", "he'll", "he's", "how'd'y", "how'd", 
      "how'll", "how's", "i'd've", "i'd", "i'll", "i'll've", "i'ma", "i'm",
      "i've", "isn't", "it'd've", "it'd", "it'll've", "it'll", "it's",
      "let's", "mayn't", "might've", "mightn't've", "mightn't", 
      "must've", "mustn't've", "mustn't", "needn't've", "needn't", "oughtn't've", 
      "oughtn't", "shalln't", "shan't've", "shan't", "sha'n't", "she'd've", "she'd", 
      "she'll", "she'll've", "she's", "should've", "shouldn't've", "shouldn't",
      "so've", "so's", "that'd've", "that'd", "that's",
      "there'd've", "there'd", "there's", "they'd've", "they'd", "they'll've", 
      "they'll","they're", "they've", "to've", "wasn't", "we'd've", "we'd", 
      "we'll've", "we'll", "we're", "we've", "weren't", "what'll've", "what'll", 
      "what're", "what's", "what've", "when's", "when've", "where'd",
      "where's", "where've", "who'll've", "who'll", "who's", "who've",
      "why's", "why've", "will've", "won't've", "won't", "would've", "wouldn't've", 
      "wouldn't", "y'all'd've", "y'all'd", "y'all're", 
      "y'all've", "y'all", "you'd've", "you'd", "you'll've", "you'll", "you're",
      "you've"
   };

   public static HashSet<String> getUnstemmedFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
         set.add(s);
      }
      return set;
   }
   
   public static HashSet<String> getUnstemmedBrokenFilterSet() {
      HashSet<String> set = new HashSet<String>();
      for (String s : filterarray) {
          set.add(s);
          String part[] = s.split("'");
          for (String p : part)
              set.add(p);
      }
      return set;
   }
}
