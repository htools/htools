package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Remove HTML special codes that are written in between &;, such as &tilde;
 * the codes are replaced by spaces to prevent words before and after from being linked.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlSpecialCodes extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlSpecialCodes.class);
   ByteRegex regex = new ByteRegex("&[A-Za-z]+;");

   private RemoveHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      ArrayList<ByteSearchPosition> pos = regex.findAllPos(entity.content, section.open, section.close);
      for (ByteSearchPosition p : pos) {
         for (int i = p.start; i < p.end; i++) {
            entity.content[i] = 32;
         }
      }
   }
}
