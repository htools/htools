package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveURL extends ExtractorProcessor {

   public static Log log = new Log(RemoveURL.class);
   public ByteSearch url = ByteSearch.create("://[\\w/%:@#\\(\\)_\\-\\+=;\\.,\\?\\[\\]\\{\\}\\|~]+");
   public ByteSearch domain = ByteSearch.create("\\.\\c[\\w_\\-]*(\\.\\c[\\w_\\-]*)+");
   public boolean letter[] = BoolTools.letter();
   public boolean name0[] = BoolTools.word0();

   private RemoveURL(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      int endpos = section.innerend;
      for (ByteSearchPosition pos : url.findAllPos(entity.content, startpos, endpos)) {
         while (pos.start > section.innerstart && letter[entity.content[pos.start - 1] & 0xFF]) {
            pos.start--;
         }
         for (int a = pos.start; a < pos.end; a++) {
            entity.content[a] = 32;
         }
      }
      for (ByteSearchPosition pos : domain.findAllPos(entity.content, startpos, endpos)) {
         for (; pos.start > section.innerstart && entity.content[pos.start - 1] == 0; pos.start--);
         if (pos.start > section.innerstart && name0[entity.content[pos.start - 1] & 0xFF]) {
            while (pos.start > section.innerstart && name0[entity.content[pos.start - 1] & 0xFF]) {
               pos.start--;
            }
            for (int a = pos.start; a < pos.end; a++) {
               entity.content[a] = 32;
            }
         }
      }
   }
}
