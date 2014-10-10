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
 * Marks <head> </head> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkHead extends SectionMarker {

   public static Log log = new Log(MarkHead.class);
   public ByteSearch endmarker = ByteSearch.create("</head");

   public MarkHead(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<head");
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend);
      if (tagclose > -1) {
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found() && end.start > position.end) {
            return entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
      return null;
   }
}
