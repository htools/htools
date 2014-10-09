package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;

/**
 * Replaces all non-ASCII characters by spaces. To prevent characters that surround
 * the non-ASCII chars to be identified as words, "extractor.processname.nonasciiremoveword"
 * can be set to true to erase these characters.
 * @author jer
 */
public class RemoveNonASCII extends ExtractorProcessor {

   public static Log log = new Log(RemoveNonASCII.class);
   public boolean letterdigit[];
   public boolean nonascii[];

   public RemoveNonASCII(Extractor extractor, String process) {
      this(extractor, process, extractor.getConfigurationBoolean(process, "nonasciiremoveword", true));
   }

   public RemoveNonASCII(Extractor extractor, boolean nonasciiremoveword) {
      this(extractor, null, nonasciiremoveword);
   }

   RemoveNonASCII(Extractor extractor, String process, boolean nonasciiremoveword) {
      super(extractor, null);
      nonascii = new boolean[256];
      for (int i = 0; i < 256; i++)
          nonascii[i] = (i > 0 && i < 32) || i > 126;
      if (nonasciiremoveword) {
         letterdigit = BoolTools.combineRanges(
                 BoolTools.createASCIIAcceptRange('A', 'Z'),
                 BoolTools.createASCIIAcceptRange('a', 'z'),
                 BoolTools.createASCIIAcceptRange('0', '9'));
      }
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      if (letterdigit == null) {
         for (int p = section.open; p < section.close; p++) {
            if ( nonascii[entity.content[p] & 0xFF] ) {
               entity.content[p] = 32;
            }
         }
      } else {
         for (int p = section.open; p < section.close; p++) {
            if (entity.content[p] < 0) {
               entity.content[p] = 32;
               for (int i = p - 1; i >= section.open && letterdigit[entity.content[i] & 0xFF]; i--) {
                  entity.content[i] = 32;
               }
               for (; p < section.close && letterdigit[entity.content[p] & 0xFF]; p++) {
                  entity.content[p] = 32;
               }
            } else if ( nonascii[ entity.content[p]] )
                entity.content[p] = 32;
         }
      }
   }
}
