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
 * Marks <noscript> </nscript> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkNoScript extends SectionMarker {

   public static Log log = new Log(MarkNoScript.class);
   public ByteSearch endmarker = new ByteSection("</noscript", ">");

   public MarkNoScript(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<noscript");
   }

   @Override
   public Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend) + 1;
      if (tagclose > -1) {
         //log.info("content %d %d %d %d %s", 0, entity.content.length, sectionstart, sectionend, new String( entity.content ));
         ByteSearchPosition end = endmarker.findPos(entity.content, position.end, sectionend);
         if (end.found()) {
            return entity.addSectionPos(outputsection, position.start, tagclose, end.start, end.end);
         }
      }
      return null;
   }
}
