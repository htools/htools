package io.github.htools.extract.modules;

import io.github.htools.words.StopWordsContractions;
import io.github.htools.words.StopWordsInquery;
import io.github.htools.lib.Log;
import io.github.htools.extract.Extractor;
import io.github.htools.words.StopWordsLetter;
import io.github.htools.words.StopWordsSmart;
import io.github.htools.words.StopWordsUrl;

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