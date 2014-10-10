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
 * Marks <text> </text> sections, which are used in some news wires to tag the
 * body of the document.
 * <p/>
 * @author jbpvuurens
 */
public class MarkText extends SectionMarker {

   public static Log log = new Log(MarkText.class);
   public ByteSearch endmarker = ByteSearch.create("</text>");
   public ByteRegex startmarker = new ByteRegex("<text");

   public MarkText(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return startmarker;
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend) + 1;
      if (tagclose > -1) {
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found()) {
            return entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
      return null;
   }
}
