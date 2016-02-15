package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * Removes words shorter than 2 characters, including one letter one digit.
 * <p>
 * @author jbpvuurens
 */
public class RemoveShortWords extends ExtractorProcessor {

   public static Log log = new Log(RemoveShortWords.class);
   boolean[] chars = BoolTools.character();
   boolean[] digit = BoolTools.digit();

   public RemoveShortWords(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int count = 0;
      int start = 0;
      for (int p = section.innerstart; p < section.innerend; p++) {
         if (section.haystack[p] == 0) {
             
         } else if (chars[section.haystack[p] & 0xff]) {
             if (count == 0) {
                 start = p;
             }
             count++;
         } else if (digit[section.haystack[p] & 0xff]) {
             if (count > 0)
                 count++;
         } else {
             if (count > 0) {
                 if (count < 3) {
                     for (int i = start; i < p; i++)
                         section.haystack[p] = 32;
                 }
                 count = 0;
             }
         }
      }
   }
}
