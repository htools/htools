package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;

/**
 * Query specific converter to lowercase, which leaves all names preceding a
 * colon (:) in orginal case, because this is a Java Class name in RR syntax.
 * @author jbpvuurens
 */
public class ConvertToLowercaseQuery extends ConvertToLowercase {

   public static Log log = new Log(ConvertToLowercaseQuery.class);
   public boolean whitespace[] = new boolean[128];
   public boolean uppercase[] = new boolean[128];

   public ConvertToLowercaseQuery(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         whitespace[i] = (i == ' ' || i == '\t' || i == '\n' || i == '\r');
      }
      for (int i = 0; i < 128; i++) {
         uppercase[i] = (i >= 'A' && i <= 'Z');
      }
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      log.info("query %s", new String(entity.content, section.open, section.close));
      byte buffer[] = entity.content;
      int p;
      int start = section.open;
      boolean colonseen = false;
      for (p = section.open; p < section.close; p++) {
         if (buffer[p] == ':')
            colonseen = true;
         if (whitespace[buffer[p]] || p == section.close - 1) {
            if (colonseen) {
               colonseen = false;
            } else {
               for (int i = start; i <= p; i++) 
                  if ( buffer[i] >= 'A' && buffer[i] <= 'Z' )
                     buffer[i] |= 32;
            }
            start = p+1;
         }
      }
      log.info("querynew %s", new String(entity.content, section.open, section.close));
   }
}
