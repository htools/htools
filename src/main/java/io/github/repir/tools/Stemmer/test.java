package io.github.repir.tools.Stemmer;
import io.github.repir.tools.Lib.Log; 

/**
 *
 * @author Jeroen Vuurens
 */
public class test {
  public static Log log = new Log( test.class ); 

   public static void main(String[] args) {
      englishStemmer stemmer = englishStemmer.get();
      for (String t : args)
         log.info("%s %s", t, stemmer.stem(t));
   }

}
