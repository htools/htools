package io.github.htools.extract.modules;

import io.github.htools.lib.Log;
import io.github.htools.extract.Extractor;
import io.github.htools.words.StopWordsLetter;
import io.github.htools.words.StopWordsSmart;
import io.github.htools.words.StopWordsWWW;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveStopwords extends RemoveFilteredWords {

   private static Log log = new Log(RemoveStopwords.class);

   public RemoveStopwords(Extractor extractor, String process) {
      super(extractor, process);
      this.addWords(StopWordsSmart.getUnstemmedFilterSet());
      this.addWords(StopWordsWWW.getUnstemmedFilterSet());
      this.addWords(StopWordsLetter.getUnstemmedFilterSet());
   }
}