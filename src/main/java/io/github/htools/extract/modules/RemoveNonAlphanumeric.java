package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Remove HTML special codes that are written in between &amp; ;, such as <code>&tilde;</code>
 * the codes are replaced by spaces to prevent words before and after from being linked.
 * <p>
 * @author jeroen
 */
public class RemoveNonAlphanumeric extends ExtractorProcessor {

   public static Log log = new Log(RemoveNonAlphanumeric.class);
   boolean[] allowedCharacters = BoolTools.combineRanges(BoolTools.alphanumeric(), BoolTools.createASCIIAccept('\0'));

   private RemoveNonAlphanumeric(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
         for (int i = section.innerstart; i < section.innerend; i++) {
             if (!allowedCharacters[entity.content[i] & 0xff])
                entity.content[i] = 32;
         }
   }
}
