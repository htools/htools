package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;

/**
 * Removes Ref tags, that are in wikipedia pages after html characters are substituted.
 * These tags contain URLs.
 * <p/>
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
