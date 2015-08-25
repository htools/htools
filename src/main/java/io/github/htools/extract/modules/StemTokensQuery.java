package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.words.englishStemmer;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import java.util.HashMap;

/**
 * Query specific stemmer, that ignores words that precede a colon (:),
 * because this is a Java Class name in RR syntax.
 */
public class StemTokensQuery extends ExtractorProcessor {

   private static Log log = new Log(StemTokensQuery.class);
   englishStemmer stemmer = englishStemmer.get();
   static HashMap<String, String> translateStemmed = new HashMap<String, String>();

   public StemTokensQuery(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      ExtractChannel attribute = entity.get(attributename);
      for (int c = 0; c < attribute.size(); c++) {
         String chunk = attribute.get(c);
         if (!chunk.contains(":")) {
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
}