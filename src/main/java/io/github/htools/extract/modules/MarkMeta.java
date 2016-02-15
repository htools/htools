package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchSection;

/**
 * Marks &lt;meta&gt; sections.
 * <p>
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
      int tagclose = findQuoteSafeTagEnd(section);
      if (tagclose > -1) {
         return content.addSectionPos(outputsection, content.content, section.start, section.innerstart, tagclose, tagclose);
      }
      return null;
   }
}
