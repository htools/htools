package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import java.util.ArrayDeque;

/**
 * Remove mediawiki macro's e.g. "{{cite ...}}" and "{| table |}".
 * <p/>
 * @author jbpvuurens
 */
public class RemoveWikipediaMacrosTables extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaMacrosTables.class);
   ArrayDeque<Integer> open = new ArrayDeque<Integer>();

   public RemoveWikipediaMacrosTables(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      if (open.size() > 0) {
         open = new ArrayDeque<Integer>();
      }
      byte buffer[] = entity.content;
      for (int p = section.innerstart; p < section.innerend; p++) {
         switch (buffer[p]) {
            case '{':
               open.push(p);
               break;
            case '}':
               if (open.size() > 0) {
                  int lastopen = open.poll();
                  if (p < section.innerend && buffer[p + 1] == '}' && open.size() > 0) {
                     int prevopen = open.poll();
                     for (int i = p + 1; i >= prevopen; i--) {
                        buffer[i] = 32;
                     }
                     p++;
                  } else {
                     for (int i = p; i >= lastopen; i--) {
                        buffer[i] = 32;
                     }
                  }
               }
               break;
         }
      }
   }
}
