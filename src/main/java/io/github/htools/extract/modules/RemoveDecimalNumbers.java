package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;

/**
 * Remove codes with a decimal dot, e.g. "v1.02", "x.01", "0.05", "0.01a"
 * <p>
 * @author jeroen
 */
public class RemoveDecimalNumbers extends ExtractorProcessor {

   private static Log log = new Log(RemoveDecimalNumbers.class);
   ByteSearch decimal = ByteSearch.create("\\.[0-9]");
   boolean number[] = new boolean[256];

   public RemoveDecimalNumbers(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 256; i++)
         number[i] = (i >= '0' && i <= '9') || (i >= 'A' && i <= 'Z') || (i >= 'a' && i <= 'z');
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchPosition pos : decimal.findAllPos(entity.content, section.innerstart, section.innerend)) {
         for ( ; pos.start > section.innerstart && number[entity.content[pos.start-1] & 0xFF]; pos.start--);
         for ( ; pos.end < section.innerend && number[entity.content[pos.start+1] & 0xFF]; pos.end++);
         for (int i = pos.start; i < pos.end; i++)
            entity.content[i] = 32;
      }
   }
}