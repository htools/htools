package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

/**
 * Removes a marked section in the {@link Content}'s content.
 * @author jer
 */
public class RemoveSection extends ExtractorProcessor {

   public static Log log = new Log(RemoveSection.class);

   public RemoveSection(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      //log.info("process %d %d", section.openlead, section.closetrail);
      for (int i = section.start; i < section.end; i++) {
         entity.content[i] = 32;
      }
   }
}
