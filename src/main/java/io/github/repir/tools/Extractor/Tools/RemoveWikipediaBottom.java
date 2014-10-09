package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchSection;
import io.github.repir.tools.ByteSearch.ByteSection;
import io.github.repir.tools.ByteSearch.ByteSectionScanned;
import io.github.repir.tools.Lib.Log;

/**
 * Removes the sections after See Also, References, Further Reading and External Links
 * in a Wikipedia page.
 * <p/>
 * @author jbpvuurens
 */
public class RemoveWikipediaBottom extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaBottom.class);
   public ByteRegex seealso = new ByteRegex("\n==+\\s*See\\s+Also\\s*==+");
   public ByteRegex references = new ByteRegex("\n==+\\s*References\\s*==+");
   public ByteRegex furtherreading = new ByteRegex("\n==+\\s*Further\\s+Reading\\s*==+");
   public ByteRegex externallinks = new ByteRegex("\n==+\\s*External\\s+Links\\s*==+");
   public ByteRegex combined = ByteRegex.combine(seealso, references, furtherreading,externallinks );

   public RemoveWikipediaBottom(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      int find = combined.find(entity.content, section.open, section.close);
      if (find > -1) {
         for (int pos = find; pos < section.close; pos++)
            entity.content[pos] = 32;
      }
   }
}
