package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;

/**
 * For debug purposed only, to print a section to the log.
 * @author jer
 */
public class ExtractShow extends ExtractorProcessor {

   public static Log log = new Log(ExtractShow.class);

   public ExtractShow(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      log.print(new String(entity.content, section.innerstart, section.innerend - section.innerstart));
   }
}
