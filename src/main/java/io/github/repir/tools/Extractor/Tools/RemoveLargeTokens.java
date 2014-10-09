package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import java.util.Iterator;

/**
 * Removes attribute values that have a size over (default=25) characters. The
 * maximum length for the attribute characters can be set in the configuration
 * e.g. extractor.<process>.removelargetokens = 25
 * <p/>
 * @author jeroen
 */
public class RemoveLargeTokens extends ExtractorProcessor {

   private static Log log = new Log(RemoveLargeTokens.class);
   final int maxlength;

   public RemoveLargeTokens(Extractor extractor, String process) {
      super(extractor, process);
      maxlength = extractor.conf.getInt("extractor." + process + ".removelargetokens", 25);
   }

   @Override
   public void process(Entity entity, Entity.Section pos, String attribute) {
      Iterator<String> iter = entity.get(attribute).iterator();
      while (iter.hasNext()) {
         String chunk = iter.next();
         if (chunk.length() > maxlength) {
            iter.remove();
         }
      }
   }
}