package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;

/**
 * Scans the document's byte contents to mark sections that must be processed in
 * some way. In the configuration, SectionMarkers are configured with 
 * "+extractor.sectionmarker=source_section result_section SectionMarker"
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

   public abstract ByteSearchSection process(Content entity, ByteSearchSection section);

   public int findQuoteSafeTagEnd(ByteSearchSection section) {
      return endtag.findEndQuoteSafe(section);
   }
}
