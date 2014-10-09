package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Marks <meta> </meta> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkMeta extends SectionMarker {

   public static Log log = new Log(MarkMeta.class);

   public MarkMeta(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<meta\\s");
   }

   @Override
   public void process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position) {
      int tagclose = findQuoteSafeTagEnd(entity, position.end, sectionend) + 1;
      if (tagclose > -1) {
         entity.addSectionPos(outputsection, position.start, position.start, tagclose, tagclose);
      }
   }
}
