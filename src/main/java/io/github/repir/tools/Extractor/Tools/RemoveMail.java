package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveMail extends ExtractorProcessor {

   public static Log log = new Log(RemoveMail.class);
   public ByteRegex start = new ByteRegex("@[\\c]+(\\.\\c)+");
   public boolean letter[] = BoolTools.namedot();

   private RemoveMail(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int startpos = section.open;
      int endpos = section.close;
      ArrayList<ByteSearchPosition> positions = start.findAllPos(entity.content, startpos, endpos);
      for (ByteSearchPosition pos : positions) {
         while (pos.start > section.open && letter[entity.content[pos.start - 1] & 0xFF]) {
            pos.start--;
         }
         if (pos.start > section.open && entity.content[pos.start - 1] == ':') { // remove stuff like mailto:
            while (--pos.start > section.open && letter[entity.content[pos.start - 1] & 0xFF]) {
              pos.start--;
           }
         }
         for (int a = pos.start; a < pos.end; a++) {
            entity.content[a] = 32;
         }
      }
   }
}
