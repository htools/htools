package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;

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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      byte buffer[] = entity.content;
      for (ByteSearchPosition p : combine.findAllPos(buffer, section.innerstart, section.innerend)) {
         if (p.pattern == 0) {
            buffer[p.start] = 32;
            buffer[p.start+1] = 32;
         } else if (p.pattern == 1) {
            buffer[p.start] = 0;
         }
      }
   }
}
