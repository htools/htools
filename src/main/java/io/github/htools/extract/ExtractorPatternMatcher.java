package io.github.htools.extract;

import io.github.htools.extract.modules.SectionMarker;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

import java.util.ArrayList;

/**
 * Helper class for {@link ExtractorConf} to mark sections in content.
 *
 * @author jeroen
 */
public class ExtractorPatternMatcher {

   public static Log log = new Log(ExtractorPatternMatcher.class);
   Extractor extractor;
   String section;
   ArrayList<SectionMarker> markers = new ArrayList<SectionMarker>();
   private ByteRegex patternmatcher;

   public ExtractorPatternMatcher(Extractor extractor, String section, ArrayList<SectionMarker> markers) {
      this.extractor = extractor;
      this.section = section;
      ArrayList<ByteRegex> regex = new ArrayList<ByteRegex>();
      this.markers = markers;
      for (SectionMarker p : markers) {
         regex.add(p.getStartMarker());
         //log.info("new ExtractorPatternMatcher %s %s", p.getClass().getCanonicalName(), p.startmarker);
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
          if (lastSection[pos.pattern] == null || lastSection[pos.pattern].end <= pos.start) {
             ByteSearchSection psection = new ByteSearchSection(content.haystack, pos.start, pos.end, content.innerend, content.end);
             ByteSearchSection section = markers.get(pos.pattern).process(entity, psection);
             if (section != null) {
                 lastSection[pos.pattern] = section;
             }
          }
      }
   }
}
