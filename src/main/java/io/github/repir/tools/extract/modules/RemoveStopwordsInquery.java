package io.github.repir.tools.extract.modules;

import io.github.repir.tools.Words.StopWordsContractions;
import io.github.repir.tools.Words.StopWordsInquery;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.Words.StopWordsLetter;
import io.github.repir.tools.Words.StopWordsSmart;
import io.github.repir.tools.Words.StopWordsUrl;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveStopwordsInquery extends RemoveFilteredWords {

   private static Log log = new Log(RemoveStopwordsInquery.class);

   public RemoveStopwordsInquery(Extractor extractor, String process) {
      super(extractor, process);
      this.addWords(StopWordsInquery.getUnstemmedFilterSet());
      this.addWords(StopWordsContractions.getUnstemmedBrokenFilterSet());
//      this.words.add("s");
//      this.words.add("t");
//      this.words.add("d");
//      this.words.add("i");
//      this.words.add("m");
   }
}