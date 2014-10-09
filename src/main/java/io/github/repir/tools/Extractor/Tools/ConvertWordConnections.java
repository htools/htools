package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;

/**
 * convert word connections into \0 so that the tokenizer will see a leading
 * letter followed by a ' or - and a trailing word as one word by erasing the
 * connector. This applies to words like d'Arc, o'Brien, n-gram, 2-way. 
 * Also, 's are erased, which is probably not necessary if a stemmer is used.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertWordConnections extends ExtractorProcessor {

   public static Log log = new Log(ConvertWordConnections.class);
   ByteRegex danglings = new ByteRegex("'s\\W(?<=\\w's\\W)");
   ByteRegex singlechar = new ByteRegex("['\\-]\\c((?<=\\W\\w['\\-]\\c)|(?<=^\\w['\\-]\\c))");
   ByteRegex combine = ByteRegex.combine(danglings, singlechar );

   public ConvertWordConnections(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      byte buffer[] = entity.content;
      for (ByteSearchPosition p : combine.findAllPos(buffer, section.open, section.close)) {
         if (p.pattern == 0) {
            buffer[p.start] = 32;
            buffer[p.start+1] = 32;
         } else if (p.pattern == 1) {
            buffer[p.start] = 0;
         }
      }
   }
}
