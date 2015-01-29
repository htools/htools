package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      for (ByteSearchPosition pos : open.findAllPos(entity.content, section.innerstart, section.innerend)) {
            ByteSearchPosition c = close.findPos(entity.content, pos.end, Math.min(section.innerend, pos.end + 40));
            if (c.found()) {
               for (int p = pos.start; p < c.end; p++) {
                  entity.content[p] = 32;
               }
            }
      }
   }
}
