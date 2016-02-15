package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.Iterator;

/**
 * Removes attribute values that have a size over (default=25) characters. The
 * maximum length for the attribute characters can be set in the configuration
 * e.g. extractor.&lt;process&gt;.removelargetokens = 25
 * <p>
 * @author jeroen
 */
public class RemoveLargeTokens extends ExtractorProcessor {

   private static Log log = new Log(RemoveLargeTokens.class);
   final int maxlength;

   public RemoveLargeTokens(Extractor extractor, String process) {
      super(extractor, process);
      maxlength = getConfiguration(process, extractor);
   }

   public static int getConfiguration(String process, Extractor extractor) {
       if (extractor instanceof ExtractorConf) {
           return ((ExtractorConf)extractor).getConfigurationInt(process, "removelargetokens", 25);
       }
       return 25;
   }
   
   @Override
   public void process(Content entity, ByteSearchSection pos, String attribute) {
      Iterator<String> iter = entity.get(attribute).iterator();
      while (iter.hasNext()) {
         String chunk = iter.next();
         if (chunk.length() > maxlength) {
            iter.remove();
         }
      }
   }
}