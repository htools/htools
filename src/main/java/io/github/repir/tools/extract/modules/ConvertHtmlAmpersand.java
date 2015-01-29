package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchPosition p : regex.findAllPos(entity.content, section.innerstart, section.innerend)) {
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
