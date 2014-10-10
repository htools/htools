package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchString;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Lib.ByteTools;
import java.util.ArrayList;

/**
 * Scans the document's byte contents to mark sections that must be processed in
 * some way. In the configuration, SectionMarkers are configured with 
 * "+extractor.sectionmarker=<source section> <result section> <SectionMarker>"
 * where the source sections are scanned by a class that implements SectionMarker
 * and the sections identified are marked as result section.
 * @author jer
 */
public abstract class SectionMarker {

   Extractor extractor;
   String inputsection;
   String outputsection;
   public ByteSearch startmarker;
   private static ByteSearch endtag = ByteSearch.create(">");

   public SectionMarker(Extractor extractor, String inputsection, String outputsection) {
      this.extractor = extractor;
      this.inputsection = inputsection;
      this.outputsection = outputsection;
      startmarker = getStartMarker();
   }

   public String getInputSection() {
      return inputsection;
   }

   /**
    * Regex to start section, must be Regex because the SectionMarkers are combined
    */
   public abstract ByteRegex getStartMarker();

   public abstract Section process(Entity entity, int sectionstart, int sectionend, ByteSearchPosition position);

   public int findQuoteSafeTagEnd(Entity entity, int pos, int end) {
      return endtag.findEndQuoteSafe(entity.content, pos, end);
   }
}
