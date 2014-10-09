package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <title> </title> sections, which is use in some news wires and in HTML
 * documents to mark the title.
 * <p/>
 * @author jbpvuurens
 */
public class MarkTitle extends SectionMarker {

   public static Log log = new Log(MarkTitle.class);
   public ByteSearch endmarker = new ByteSection("</title", ">");

   public MarkTitle(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<title");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend);
      if (tagclose > -1) {
         position.end = tagclose;
         ByteSearchPosition end = endmarker.findPos(entity.content, tagclose, sectionend);
         if (end.found()) {
            entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
   }
}
