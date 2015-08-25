package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.search.ByteSectionScanned;
import io.github.htools.lib.Log;

/**
 * Removes headings in Wikipedia documents, which are marked like ==heading==
 * <p>
 * @author jbpvuurens
 */
public class RemoveWikipediaHeadings extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaHeadings.class);
   public ByteSection heading = new ByteSectionScanned("\n==+", "\n", "==+");

   public RemoveWikipediaHeadings(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      for (ByteSearchSection sec : heading.findAllSections(entity.content, section.innerstart, section.innerend)) {
         for (int p = sec.start + 1; p < sec.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
