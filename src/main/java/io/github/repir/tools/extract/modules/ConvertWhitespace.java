package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.BoolTools;

/**
 * Convert all whitespace to a single space, ignoring \0
 *
 * @author jbpvuurens
 */
public class ConvertWhitespace extends ExtractorProcessor {

   public static Log log = new Log(ConvertWhitespace.class);
   boolean whitespace[] = BoolTools.whitespace();

   public ConvertWhitespace(Extractor extractor, String process) {
      super(extractor, process);
      whitespace[32] = false;
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      byte buffer[] = entity.content;
      boolean contiguouswhitespace = false;
      for (int p = section.innerstart; p < section.innerend; p++) {
         if (whitespace[buffer[p] & 0xff]) {
            if (!contiguouswhitespace)
               buffer[p] = ' ';
            else {
                buffer[p] = '\0';
                contiguouswhitespace = true;
            } 
         } else if (buffer[p] != 0) {
            contiguouswhitespace = false;
         }
      }
   }
}
