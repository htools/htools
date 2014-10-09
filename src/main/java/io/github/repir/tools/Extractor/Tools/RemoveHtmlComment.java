package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSection;

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
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.open, section.close) ) {
         for (int p = pos.start; p < pos.end; p++) {
            entity.content[p] = 32;
         }
      }
   }
}
