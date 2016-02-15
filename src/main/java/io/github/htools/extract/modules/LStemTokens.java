package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.words.LancasterStemmer;

import java.util.HashMap;

/**
 * Processes all tokens in the supplied EntityChannel though the snowball
 * (Porter 2) stemmer.
 */
public class LStemTokens extends ExtractorProcessor {

   private static Log log = new Log(LStemTokens.class);
   LancasterStemmer stemmer = LancasterStemmer.get();
   static HashMap<String, String> translateStemmed = new HashMap<String, String>();

   public LStemTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      ExtractChannel attribute = entity.get(attributename);
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