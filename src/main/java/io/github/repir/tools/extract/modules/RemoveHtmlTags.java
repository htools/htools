package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;
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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      int endpos = section.innerend;
      ArrayList<ByteSearchPosition> pos = start.findAllPos(entity.content, startpos, endpos);
      int tagclose = 0;
      for (int i = 0; i < pos.size(); i++) {
         ByteSearchPosition p = pos.get(i);
         if (p.start >= tagclose) { // in case a tag isnt properly closed
            tagclose = findQuoteSafeTagEnd(entity, p.start, endpos) + 1;
            for (int a = p.start; a < tagclose; a++) {
               entity.content[a] = 0;
            }
         }
      }
   }
}
