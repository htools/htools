package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Extractor;
import java.util.Map;

/**
 * Shows the current assigned attributes for debugging
 * <p/>
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
