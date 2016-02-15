package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * convert word connections into \0 so that the tokenizer will see a leading
 * letter followed by a ' or - and a trailing word as one word by erasing the
 * connector. This applies to words like d'Arc, o'Brien, n-gram, 2-way. 
 * Also, 's are erased, which is probably not necessary if a stemmer is used.
 * <p>
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
