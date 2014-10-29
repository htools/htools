package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Words.StopWordsLetter;
import io.github.repir.tools.Words.StopWordsSmart;
import io.github.repir.tools.Words.StopWordsUrl;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class RemoveStopwords extends ExtractorProcessor {

   private static Log log = new Log(RemoveStopwords.class);
   HashSet<String> stopwords = new HashSet<String>();

   public RemoveStopwords(Extractor extractor, String process) {
      super(extractor, process);
      stopwords.addAll(StopWordsSmart.getUnstemmedFilterSet());
      stopwords.addAll(StopWordsUrl.getUnstemmedFilterSet());
      stopwords.addAll(StopWordsLetter.getUnstemmedFilterSet());
   }

   @Override
   public void process(Entity entity, Entity.Section pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      EntityChannel attribute = entity.get(attributename);
      Iterator<String> iter = attribute.iterator();
      while (iter.hasNext()) {
         if (stopwords.contains(iter.next()))
            iter.remove();
      }
   }
}