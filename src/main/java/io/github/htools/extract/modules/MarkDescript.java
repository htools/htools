package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;

/**
 * Marks &lt;descript&gt; sections.
 * <p>
 * @author jbpvuurens
 */
public class MarkDescript extends SectionMarker {

   public static Log log = new Log(MarkDescript.class);
   public ByteSearch endmarker = ByteSearch.create("</descript>");

   public MarkDescript(Extractor extractor, String inputsection, String outputsection) {
      super(extractor, inputsection, outputsection);
   }

   @Override
   public ByteRegex getStartMarker() {
      return new ByteRegex("<descript>");
   }

   @Override
   public ByteSearchSection process(Content content, ByteSearchSection section) {
      ByteSearchPosition end = endmarker.findPos(section);
      if (end.found() && end.start > section.end) {
          return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, end.start, end.end);
      }
      return null;
   }
}
