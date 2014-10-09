package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <audio> </audio> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkAudio extends SectionMarker {

   public static Log log = new Log(MarkAudio.class);
   public ByteSection endmarker = ByteSearch.create("</audio").toSection(ByteSearch.create(">"));

   public MarkAudio(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<audio");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend) + 1;
      if (tagclose > -1) {
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found() && end.start > position.end) {
            entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
   }
}
