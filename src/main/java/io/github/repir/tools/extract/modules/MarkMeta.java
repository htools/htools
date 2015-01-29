package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

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
   public ByteSearchSection process(Content content, ByteSearchSection section) {
      int tagclose = findQuoteSafeTagEnd(section) + 1;
      if (tagclose > -1) {
         return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, tagclose, tagclose);
      }
      return null;
   }
}
