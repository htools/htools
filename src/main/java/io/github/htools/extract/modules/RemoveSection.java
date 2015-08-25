package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;

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
