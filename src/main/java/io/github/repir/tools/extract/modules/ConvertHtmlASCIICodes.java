package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import java.util.ArrayList;

/**
 * Convert HTML ASCII code like &#101; to the corresponding byte.
 * <p/>
 * @author jeroen
 */
public class ConvertHtmlASCIICodes extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlASCIICodes.class);
   private ByteSearch regex = ByteSearch.create("&#\\d+;");

   public ConvertHtmlASCIICodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      ArrayList<ByteSearchPosition> pos = regex.findAllPos(entity.content, section.innerstart, section.innerend);
      for (ByteSearchPosition p : pos) {
         int ascii = 0;
         for (int i = p.start + 2; i < p.end - 1; i++) {
            ascii = ascii * 10 + entity.content[i] - '0';
         }
         if (ascii > 31 && ascii < 128) {
            entity.content[p.start] = (ascii > 31 && ascii < 256) ? (byte) ascii : 0;
         }
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
