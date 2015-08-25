package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      log.info("query %s", new String(entity.content, section.innerstart, section.innerend));
      byte buffer[] = entity.content;
      int p;
      int start = section.innerstart;
      boolean colonseen = false;
      for (p = section.innerstart; p < section.innerend; p++) {
         if (buffer[p] == ':')
            colonseen = true;
         if (whitespace[buffer[p]] || p == section.innerend - 1) {
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
      log.info("querynew %s", new String(entity.content, section.innerstart, section.innerend));
   }
}
