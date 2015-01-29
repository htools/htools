package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.ByteTools;
import io.github.repir.tools.lib.Log;

/**
 * Stores a literal section. To use, first mark the section with a
 * SectionMarker, then in the configuration assign this class to the section's
 * process, e.g. "+extractor.literaltitle=StoreLiteralSection" will store the
 * section marked as "literaltitle" as {@link EntityChannel} "literaltitle".
 * <p/>
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
