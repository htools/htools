package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;

/**
 * Removes content tagged as <math> </math>
 * <p/>
 * @author jbpvuurens
 */
public class RemoveMathTag extends ExtractorProcessor {

   public static Log log = new Log(RemoveMathTag.class);
   public ByteRegex open = new ByteRegex("<MATH>");
   public ByteRegex close = new ByteRegex("</MATH>");

   public RemoveMathTag(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.open, section.close)) {
            ByteSearchPosition c = close.findPos(entity.content, pos.end, Math.min(section.close, pos.end + 40));
            if (c.found()) {
               for (int p = pos.start; p < c.end; p++) {
                  entity.content[p] = 32;
               }
            }
      }
   }
}
