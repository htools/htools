package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Lib.Log;

/**
 * Removes metatags <T2> </T2>
 * <p/>
 * @author jbpvuurens
 */
public class RemoveT2Tag extends ExtractorProcessor {

   public static Log log = new Log(RemoveT2Tag.class);
   public ByteSearch open = ByteSearch.create("<T[0-9]>");
   public ByteSearch close = ByteSearch.create("</T[0-9]>");
   public ByteSection bsection = new ByteSection(open, close);

   public RemoveT2Tag(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Section section, String attribute) {
      for (ByteSearchSection pos : bsection.findAllSections(entity.content, section.open, section.close)) {
         for (int p = pos.start; p < pos.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
