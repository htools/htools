package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.BoolTools;
import java.util.Iterator;

/**
 * Remove tokens that are longer than (default=5) digits. Note, this process can
 * only be used if the tokens only contain ASCII characters. The maximum length
 * can be set in the configuration e.g. extractor.<process>.removelongnumbers =
 * 5
 * <p/>
 * @author jeroen
 */
public class RemoveLongNumbers extends ExtractorProcessor {

   private static Log log = new Log(RemoveLongNumbers.class);
   final int maxlength;
   boolean number[];

   public RemoveLongNumbers(Extractor extractor, String process) {
      super(extractor, process);
      maxlength = extractor.conf.getInt("extractor." + process + ".removelongnumbers", 5);
      number = BoolTools.digit();
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
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