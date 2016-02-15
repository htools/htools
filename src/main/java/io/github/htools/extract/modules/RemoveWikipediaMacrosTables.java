package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayDeque;

/**
 * Remove mediawiki macro's e.g. "{{cite ...}}" and "{| table |}".
 * <p>
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
