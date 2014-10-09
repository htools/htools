package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;

/**
 * convert all uppercase characters to lowercase. This processor is not context
 * aware so multi-byte characters such as unicode characters should be converted
 * before running this processor.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertToLowercase extends ExtractorProcessor {

   public static Log log = new Log(ConvertToLowercase.class);
   boolean capital[];
   
   public ConvertToLowercase(Extractor extractor, String process) {
      super(extractor, process);
      capital = BoolTools.createASCIIAcceptRange('A', 'Z');
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      byte buffer[] = entity.content;
      int p;
      for (p = section.open; p < section.close; p++) {
         if (buffer[p] >= 'A' && buffer[p] <= 'Z') {
            buffer[p] = (byte) (buffer[p] | 32);
         }
      }
   }

   public String process(String s) {
      Entity e = new Entity();
      e.content = s.getBytes();
      Entity.Section pos = new Entity.Section();
      pos.open = 0;
      pos.close = e.content.length;
      process(e, pos, "section");
      return new String(e.content, 0, e.content.length);
   }
}
