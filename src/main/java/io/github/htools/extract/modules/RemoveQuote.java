package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * Removes numbers
 * <p>
 * @author jbpvuurens
 */
public class RemoveQuote extends ExtractorProcessor {

   public static Log log = new Log(RemoveQuote.class);

   public RemoveQuote(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (int p = section.innerstart; p < section.innerend; p++) {
         if (section.haystack[p] == '\'') {
             section.haystack[p] = 0;
         } 
      }
   }
}
