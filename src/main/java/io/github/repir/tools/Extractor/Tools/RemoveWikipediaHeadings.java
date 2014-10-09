package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.ByteSearch.ByteSectionScanned;
import io.github.repir.tools.Lib.Log;

/**
 * Removes headings in Wikipedia documents, which are marked like ==heading==
 * <p/>
 * @author jbpvuurens
 */
public class RemoveWikipediaHeadings extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaHeadings.class);
   public ByteSection heading = new ByteSectionScanned("\n==+", "\n", "==+");

   public RemoveWikipediaHeadings(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchSection sec : heading.findAllSections(entity.content, section.open, section.close)) {
         for (int p = sec.start + 1; p < sec.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
