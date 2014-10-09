package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlTags extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlTags.class);
   public ByteRegex start = new ByteRegex("<[/!]?[A-Za-z][A-Za-z0-9]*");

   private RemoveHtmlTags(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      int endpos = section.close;
      ArrayList<ByteSearchPosition> pos = start.findAllPos(entity.content, startpos, endpos);
      int tagclose = 0;
      for (int i = 0; i < pos.size(); i++) {
         ByteSearchPosition p = pos.get(i);
         if (p.start >= tagclose) { // in case a tag isnt properly closed
            tagclose = findQuoteSafeTagEnd(entity, p.start, endpos) + 1;
            for (int a = p.start; a < tagclose; a++) {
               entity.content[a] = 32;
            }
         }
      }
   }
}
