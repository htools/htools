package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import java.util.ArrayDeque;

/**
 * Remove mediawiki macro's e.g. "{{cite ...}}" and "{| table |}".
 * <p/>
 * @author jbpvuurens
 */
public class RemoveWikipediaMacros extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaMacros.class);
   ArrayDeque<Integer> open = new ArrayDeque<Integer>();

   public RemoveWikipediaMacros(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      if (open.size() > 0) {
         open = new ArrayDeque<Integer>();
      }
      byte buffer[] = entity.content;
      for (int p = section.open; p < section.close; p++) {
         switch (buffer[p]) {
            case '{':
               open.push(p);
               break;
            case '}':
               if (open.size() > 0) {
                  int lastopen = open.poll();
                  if (p < section.close && buffer[p + 1] == '}' && open.size() > 0) {
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
