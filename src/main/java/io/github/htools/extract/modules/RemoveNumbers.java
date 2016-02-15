package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * Removes numbers
 * <p>
 * @author jbpvuurens
 */
public class RemoveNumbers extends ExtractorProcessor {

   public static Log log = new Log(RemoveNumbers.class);
   boolean[] chars = BoolTools.character();
   boolean[] digit = BoolTools.digit();

   public RemoveNumbers(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      boolean block = false;
      int start = 0;
      for (int p = section.innerstart; p < section.innerend; p++) {
         if (section.haystack[p] == 0) {
             
         } else if (chars[section.haystack[p] & 0xff]) {
             block = true;
         } else if (digit[section.haystack[p] & 0xff]) {
             if (!block) {
                 section.haystack[p] = 32;
             }
         } else {
             block = false;
         }
      }
   }
}
