package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.innerstart, section.innerend)) {
         if (pos.end < section.innerend - 5) {
            ByteSearchPosition c = close.findPos(entity.content, pos.end, Math.min(section.innerend, pos.end + 40));
            if (c.found()) {
               for (int p = pos.start; p < c.end; p++) {
                  entity.content[p] = 32;
               }
            }
         }
      }
   }
}
