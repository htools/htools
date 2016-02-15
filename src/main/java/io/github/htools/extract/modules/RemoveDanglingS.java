package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * Remove 's
 * <p>
 * @author jeroen
 */
public class RemoveDanglingS extends ExtractorProcessor {

   private static Log log = new Log(RemoveDanglingS.class);
   boolean[] invalid = BoolTools.alphanumeric();

   public RemoveDanglingS(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (int p = section.innerstart; p < section.innerend; p++) {
         if (entity.content[p] == '\'' && p < section.innerend - 1) {
             int e = p + 1;
             for (; e < section.innerend && entity.content[e] == 0; e++);
             if (e < section.innerend && entity.content[e] == 's') {
                for (e++ ; e < section.innerend && entity.content[e] == 0; e++);
                String t = ByteTools.toString(entity.content, p, e);
                if (e == section.innerend || !invalid[entity.content[e] & 0xff]) {
                    for (int i = p; i < e; i++)
                        entity.content[i] = 0;
                }
             }
         }
      }
   }
}