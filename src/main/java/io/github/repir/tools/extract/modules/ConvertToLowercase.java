package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.BoolTools;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      byte buffer[] = entity.content;
      int p;
      for (p = section.innerstart; p < section.innerend; p++) {
         if (buffer[p] >= 'A' && buffer[p] <= 'Z') {
            buffer[p] = (byte) (buffer[p] | 32);
         }
      }
   }
}
