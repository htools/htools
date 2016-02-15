package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;

/**
 * Removes metatags T2
 * <p>
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
