package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <section> </section> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkSection extends SectionMarker {

   public static Log log = new Log(MarkSection.class);
   public ByteSearch endmarker = ByteSearch.create("</section").toSection(ByteSearch.create(">"));

   public MarkSection(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<section>");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
      if (end.found() && end.start > position.end) {
         entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
      }
   }
}
