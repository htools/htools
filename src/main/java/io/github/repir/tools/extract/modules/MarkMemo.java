package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

/**
 * Marks <memo> </memo> sections.
 * <p/>
 * @author jbpvuurens
 */
public class MarkMemo extends SectionMarker {

   public static Log log = new Log(MarkMemo.class);
   public ByteSearch endmarker = ByteSearch.create("</memo>");

   public MarkMemo(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<memo>");
   }

   @Override
   public ByteSearchSection process(Content content, ByteSearchSection section) {
      ByteSearchPosition end = endmarker.findPos(section);
        if (end.found() && end.start > section.innerstart) {
            return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
        }
      return null;
   }
}
