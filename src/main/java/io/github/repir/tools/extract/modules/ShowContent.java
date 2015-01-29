package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
       if (entity.get("collectionid").getContentStr().equals("736")) {
         log.info("\n\n--- %s", attribute);
         int lastpos = section.innerstart;
         for (ByteSearchPosition pos : ByteSearch.WHITESPACE.findAllPos(entity.content, section.innerstart, section.innerend)) {
            if (pos.start - lastpos > 80) {
               log.printf("%s", new String(entity.content, lastpos, pos.start - lastpos));
               lastpos = pos.end;
            }
         }
         if (lastpos < section.innerend)
            log.printf("%s", new String(entity.content, lastpos, section.innerend - lastpos));
       }
   }
}
