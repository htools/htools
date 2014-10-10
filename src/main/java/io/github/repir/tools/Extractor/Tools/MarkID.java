package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <title> </title> sections, which is use in some news wires and in HTML
 * documents to mark the title.
 * <p/>
 * @author jbpvuurens
 */
public class MarkID extends SectionMarker {

   public static Log log = new Log(MarkID.class);
   public ByteSearch endmarker = ByteSearch.create("</id>");

   public MarkID(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<id>");
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found()) {
            return entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
         }
         return null;
   }
}
