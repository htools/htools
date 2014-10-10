package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Lib.Log;

/**
 * Marks <docurl> </docurl> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkDocURL extends SectionMarker {

   public static Log log = new Log(MarkDocURL.class);
   public ByteSearch endmarker = ByteSearch.create("</docurl>");

   public MarkDocURL(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<docurl>");
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
      if (end.found() && end.start > position.end) {
         return entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
      }
      return null;
   }
}
