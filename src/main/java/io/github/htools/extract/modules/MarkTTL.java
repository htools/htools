package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Marks &lt;ttl&gt; sections, which are used in some news wires to tag titles.
 * <p>
 * @author jbpvuurens
 */
public class MarkTTL extends SectionMarker {

   public static Log log = new Log(MarkTTL.class);
   public ByteSearch endmarker = ByteSearch.create("</ttl>");

   public MarkTTL(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<ttl>");
   }

   @Override
   public ByteSearchSection process(Content content, ByteSearchSection section) {
      ByteSearchPosition end = endmarker.findPos(content.content);
      if (end.found()) {
          return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
      }
      return null;
   }
}
