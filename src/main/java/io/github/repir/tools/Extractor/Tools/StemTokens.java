package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Words.englishStemmer;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import java.util.HashMap;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class StemTokens extends ExtractorProcessor {

   private static Log log = new Log(StemTokens.class);
   englishStemmer stemmer = englishStemmer.get();
   static HashMap<String, String> translateStemmed = new HashMap<String, String>();

   public StemTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      EntityChannel attribute = entity.get(attributename);
      for (int c = 0; c < attribute.size(); c++) {
         String chunk = attribute.get(c);
         String stem = translateStemmed.get(chunk);
         if (stem == null) {
            stem = stemmer.stem(chunk);
            translateStemmed.put(chunk, stem);
            //log.info("stem %s %s", chunk, stem);
         }
         attribute.set(c, stem);
      }
   }
}