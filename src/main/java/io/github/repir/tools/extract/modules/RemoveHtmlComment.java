package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;

/**
 * Removes HTML comment that is marked with <!-- -->
 * <p/>
 * @author jbpvuurens
 */
public class RemoveHtmlComment extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlComment.class);
   public ByteSection open = new ByteSection("<!--", "-->");

   public RemoveHtmlComment(Extractor extractor, String process) {
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
