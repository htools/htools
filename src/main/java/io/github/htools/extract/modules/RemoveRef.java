package io.github.htools.extract.modules;

import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 * Removes Ref tags, that are in wikipedia pages after html characters are substituted.
 * These tags contain URLs.
 * <p>
 * @author jbpvuurens
 */
public class RemoveRef extends ExtractorProcessor {

   public static Log log = new Log(RemoveRef.class);
   public ByteSection open = new ByteSection("<ref>", "</ref>");

   public RemoveRef(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.innerstart, section.innerend) ) {
         for (int p = pos.start; p < pos.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
