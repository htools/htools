package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;

/**
 * Convert HTML code &amp; to &
 * @author jbpvuurens
 */
public class ConvertHtmlAmpersand extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlAmpersand.class);
   ByteSearch regex = ByteSearch.create("&amp;");

   public ConvertHtmlAmpersand(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      for (ByteSearchPosition p : regex.findAllPos(entity.content, section.open, section.close)) {
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
