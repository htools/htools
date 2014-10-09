package io.github.repir.tools.Extractor;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Tools.SectionMarker;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Helper class for {@link Extractor} to mark {@link Section}s in an
 * {@link Entity}s content.
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
         regex.add(((SectionMarker) p).getStartMarker());
      }
      patternmatcher = ByteRegex.combine(regex.toArray(new ByteRegex[regex.size()]));
   }

   void processSectionMarkers(Entity entity, int sectionstart, int sectionend) {
      ArrayList<ByteSearchPosition> positions = patternmatcher.findAllPos(entity.content, sectionstart, sectionend);
      for (ByteSearchPosition pos : positions) { // find all possible section starts
         markers.get(pos.pattern).process(entity, sectionstart, sectionend, pos);
      }
   }
}
