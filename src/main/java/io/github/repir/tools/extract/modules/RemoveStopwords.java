package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.Words.StopWordsLetter;
import io.github.repir.tools.Words.StopWordsSmart;
import io.github.repir.tools.Words.StopWordsUrl;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveStopwords extends RemoveFilteredWords {

   private static Log log = new Log(RemoveStopwords.class);

   public RemoveStopwords(Extractor extractor, String process) {
      super(extractor, process);
      this.addWords(StopWordsSmart.getUnstemmedFilterSet());
      this.addWords(StopWordsUrl.getUnstemmedFilterSet());
      this.addWords(StopWordsLetter.getUnstemmedFilterSet());
   }
}