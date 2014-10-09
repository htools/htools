package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;

/**
 * Shows the current raw content buffer for debugging
 * <p/>
 * @author jeroen
 */
public class ShowContent extends ExtractorProcessor {

   public static Log log = new Log(ShowContent.class);

   public ShowContent(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Section section, String attribute) {
       if (entity.get("collectionid").getContentStr().equals("736")) {
         log.info("\n\n--- %s", attribute);
         int lastpos = section.open;
         for (ByteSearchPosition pos : ByteSearch.WHITESPACE.findAllPos(entity.content, section.open, section.close)) {
            if (pos.start - lastpos > 80) {
               log.printf("%s", new String(entity.content, lastpos, pos.start - lastpos));
               lastpos = pos.end;
            }
         }
         if (lastpos < section.close)
            log.printf("%s", new String(entity.content, lastpos, section.close - lastpos));
       }
   }
}
