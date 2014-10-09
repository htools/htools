package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <ITAG tagnum=56> </ITAG> sections, which is used in some news wires to
 * tag a title.
 * <p/>
 * @author jbpvuurens
 */
public class MarkITAG56 extends SectionMarker {

   public static Log log = new Log(MarkITAG56.class);
   public ByteSearch endmarker = ByteSearch.create("</ITAG>");

   public MarkITAG56(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<ITAG\\s+tagnum\\s+=\\s+56\\s+>");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
      if (end.found()) {
         entity.addSectionPos(outputsection, position.start, position.end, end.start, end.end);
      }
   }
}
