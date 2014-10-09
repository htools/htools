package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <noframes> </noframes> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkNoFrames extends SectionMarker {

   public static Log log = new Log(MarkNoFrames.class);
   public ByteSearch endmarker = ByteSearch.create("</noframes").toSection(ByteSearch.create(">"));

   public MarkNoFrames(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<noframes");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend) + 1;
      if (tagclose > -1) {
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found()) {
            entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
   }
}
