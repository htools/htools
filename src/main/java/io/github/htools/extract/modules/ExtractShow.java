package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

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
