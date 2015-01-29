package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.lib.Log;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchSection pos : bsection.findAllSections(entity.content, section.innerstart, section.innerend)) {
         for (int p = pos.start; p < pos.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
