package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;

/**
 * Stores a literal section. To use, first mark the section with a
 * SectionMarker, then in the configuration assign this class to the section's
 * process, e.g. "+extractor.literaltitle=StoreLiteralSection" will store the
 * section marked as "literaltitle" as channel "literaltitle".
 * <p>
 * The white spaces in the literal sections are transformed to single spaces,
 * and trimmed of the beginning and end.
 *
 * @author jer
 */
public class StoreLiteralSection extends ExtractorProcessor {

   public static Log log = new Log(StoreLiteralSection.class);

   public StoreLiteralSection(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      entity.get(attribute).add(ByteTools.toFullTrimmedString(entity.content, section.innerstart, section.innerend));
   }
}
