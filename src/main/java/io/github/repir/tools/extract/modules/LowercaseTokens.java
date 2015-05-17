package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.Words.englishStemmer;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Extractor;
import java.util.HashMap;

/**
 * Processes all tokens in the supplied EntityChannel (i.e. after tokenization)
 */
public class LowercaseTokens extends ExtractorProcessor {

   private static Log log = new Log(LowercaseTokens.class);

   public LowercaseTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection pos, String attributename) {
      //log.fatal("process channel %s %d", channel.channel, channel.size());
      ExtractChannel attribute = entity.get(attributename);
      for (int c = 0; c < attribute.size(); c++) {
         attribute.set(c, attribute.get(c).toLowerCase());
      }
   }
}