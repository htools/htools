package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSection;

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
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.open, section.close) ) {
         for (int p = pos.start; p < pos.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
