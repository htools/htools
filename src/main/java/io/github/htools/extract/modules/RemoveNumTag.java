package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;

/**
 * Removes content tagged as num
 * <p>
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
