package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.Map;

/**
 * Shows the current assigned attributes for debugging
 * <p>
 * @author jeroen
 */
public class ShowAttributes extends ExtractorProcessor {

   public static Log log = new Log(ShowAttributes.class);

   public ShowAttributes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (Map.Entry<String, ExtractChannel> entry : entity.entrySet()) {
         log.info("%s=%s", entry.getKey(), entry.getValue().getContentStr());
      }
   }
}
