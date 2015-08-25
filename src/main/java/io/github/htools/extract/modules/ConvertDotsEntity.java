package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import java.util.ArrayList;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -&gt; usa). Other dots are replaced by
 * spaces.
 * <p>
 * @author jbpvuurens
 */
public class ConvertDotsEntity extends ExtractorProcessor {

   public static Log log = new Log(ConvertDotsEntity.class);
   ByteRegex abbrev = new ByteRegex("\\.(?<=[^\\c\\.]\\c\\.)(\\c\\.)+");
   ByteRegex other = new ByteRegex("[\\.]");
   ByteRegex combi = ByteRegex.combine(abbrev, other );

   public ConvertDotsEntity(Extractor extractor, String process) {
      super(extractor, process);
   }

   public void process(Content entity, ByteSearchSection pos, String attribute) {
      ArrayList<ByteSearchPosition> positions = combi.findAllPos(entity.content, pos.innerstart, pos.innerend);
      for (ByteSearchPosition p : positions) {
         switch (p.pattern) {
            case 0: // abbreviation or initials
               for (int i = p.start; i < p.end - 1; i ++)
                  if (entity.content[i] == '.')
                     entity.content[i] = 0;
               //for (int i = p.start - 1; i < p.end - 1; i += 2)
               //   entity.content[i] &= (255 - 32);
               entity.content[p.end-1] = 32;
               break;
            case 1: // other . - +
               entity.content[p.start] = 32;
         }
      }
   }
}
