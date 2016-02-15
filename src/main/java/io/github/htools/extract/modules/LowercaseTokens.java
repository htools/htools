package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

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