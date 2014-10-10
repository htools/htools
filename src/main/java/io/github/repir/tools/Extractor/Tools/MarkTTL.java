package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <ttl> </ttl> sections, which are used in some news wires to tag titles.
 * <p/>
 * @author jbpvuurens
 */
public class MarkTTL extends SectionMarker {

   public static Log log = new Log(MarkTTL.class);
   public ByteSearch endmarker = ByteSearch.create("</ttl>");

   public MarkTTL(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<ttl>");
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition start) {
      ByteSearchPosition end = endmarker.findPos(entity.content, start.end, sectionend);
      if (end.found()) {
         return entity.addSectionPos(outputsection, start.start, start.end, end.start, end.end);
      }
      return null;
   }
}
