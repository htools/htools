package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;

/**
 * Removes content tagged as <num> </num>
 * <p/>
 * @author jbpvuurens
 */
public class RemoveNumTag extends ExtractorProcessor {

   public static Log log = new Log(RemoveNumTag.class);
   public ByteSearch open = ByteSearch.create("<NUM>");
   public ByteSearch close = ByteSearch.create("</NUM>");

   public RemoveNumTag(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.open, section.close)) {
         if (pos.end < section.close - 5) {
            ByteSearchPosition c = close.findPos(entity.content, pos.end, Math.min(section.close, pos.end + 40));
            if (c.found()) {
               for (int p = pos.start; p < c.end; p++) {
                  entity.content[p] = 32;
               }
            }
         }
      }
   }
}
