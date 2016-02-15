package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.Iterator;

/**
 * Remove tokens that are longer than (default=5) digits. Note, this process can
 * only be used if the tokens only contain ASCII characters. The maximum length
 * can be set in the configuration e.g. extractor.process.removelongnumbers =
 * 5
 * <p>
 * @author jeroen
 */
public class RemoveLongNumbers extends ExtractorProcessor {

   private static Log log = new Log(RemoveLongNumbers.class);
   final int maxlength;
   boolean number[];

   public RemoveLongNumbers(Extractor extractor, String process) {
      this(extractor, process, getConfiguration( process, extractor));
   }

   public RemoveLongNumbers(Extractor extractor, String process, int maxlength) {
      super(extractor, process);
      this.maxlength = maxlength;
      number = BoolTools.digit();
   }

   public static int getConfiguration(String process, Extractor extractor) {
       if (extractor instanceof ExtractorConf) {
           return ((ExtractorConf)extractor).getConfigurationInt(process, "removelongnumbers", 5);
       }
       return 5;
   }
   
   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int j;
      Iterator<String> iter = entity.get(attribute).iterator();
      while (iter.hasNext()) {
         String chunk = iter.next();
         if (number[chunk.charAt(0)] && chunk.length() > maxlength) {
               iter.remove();
         }
      }
   }
}