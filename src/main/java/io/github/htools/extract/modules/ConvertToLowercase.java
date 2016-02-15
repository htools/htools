package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * convert all uppercase characters to lowercase. This processor is not context
 * aware so multi-byte characters such as unicode characters should be converted
 * before running this processor.
 * <p>
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
