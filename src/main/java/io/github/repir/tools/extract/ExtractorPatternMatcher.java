package io.github.repir.tools.extract;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.modules.SectionMarker;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class for {@link ExtractorConf} to mark {@link Section}s in an
 * {@link Content}s content.
 *
 * @author jer
 */
public class ExtractorPatternMatcher {

   public static Log log = new Log(ExtractorPatternMatcher.class);
   Extractor extractor;
   String section;
   ArrayList<SectionMarker> markers = new ArrayList<SectionMarker>();
   ByteRegex patternmatcher;

   public ExtractorPatternMatcher(Extractor extractor, String section, ArrayList<SectionMarker> markers) {
      this.extractor = extractor;
      this.section = section;
      ArrayList<ByteRegex> regex = new ArrayList<ByteRegex>();
      this.markers = markers;
      for (SectionMarker p : markers) {
         regex.add(p.getStartMarker());
      }
      patternmatcher = ByteRegex.combine(regex.toArray(new ByteRegex[regex.size()]));
   }

   /**
    * Marks all non-overlapping sections for all markers in the entities raw content. 
    */
   void processSectionMarkers(Content entity, ByteSearchSection content) {
      ByteSearchSection[] lastSection = new ByteSearchSection[markers.size()];
      ArrayList<ByteSearchPosition> positions = patternmatcher.findAllPos(content);
      for (ByteSearchPosition pos : positions) { // find all possible section starts
          if (lastSection[pos.pattern] == null || lastSection[pos.pattern].end < pos.start) {
             ByteSearchSection psection = new ByteSearchSection(content.haystack, pos.start, pos.end, content.innerend, content.end);
             ByteSearchSection section = markers.get(pos.pattern).process(entity, psection);
             if (section != null) {
                 lastSection[pos.pattern] = section;
             }
          }
      }
   }
}
