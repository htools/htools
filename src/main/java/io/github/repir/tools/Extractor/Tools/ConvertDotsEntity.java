package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import java.util.ArrayList;

/**
 * Converts dots depending on the context. Dots that are recognized as a decimal
 * point are kept. Dots that are recognized as abbreviations are removed in such
 * way the letters are connected (eg u.s.a. -> usa). Other dots are replaced by
 * spaces.
 * <p/>
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

   public void process(Entity entity, Section pos, String attribute) {
      ArrayList<ByteSearchPosition> positions = combi.findAllPos(entity.content, pos.open, pos.close);
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
