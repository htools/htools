package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveMail extends ExtractorProcessor {

   public static Log log = new Log(RemoveMail.class);
   public ByteRegex start = new ByteRegex("@[\\c]+(\\.\\c)+");
   public boolean letter[] = BoolTools.namedot();

   private RemoveMail(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int startpos = section.innerstart;
      int endpos = section.innerend;
      ArrayList<ByteSearchPosition> positions = start.findAllPos(entity.content, startpos, endpos);
      for (ByteSearchPosition pos : positions) {
          int bpos = pos.start;
         while (bpos > section.innerstart && letter[entity.content[bpos - 1] & 0xFF]) {
            bpos--;
         }
         if (bpos > section.innerstart && entity.content[bpos - 1] == ':') { // remove stuff like mailto:
            while (--bpos > section.innerstart && letter[entity.content[bpos - 1] & 0xFF]);
         }
         for (int a = bpos; a < pos.end; a++) {
            entity.content[a] = 32;
         }
      }
   }
}
