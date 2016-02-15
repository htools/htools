package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchSection;

/**
 * Removes the sections after See Also, References, Further Reading and External Links
 * in a Wikipedia page.
 * <p>
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
   public void process(Content entity, ByteSearchSection section, String attribute) {
      int find = combined.find(entity.content, section.innerstart, section.innerend);
      if (find > -1) {
         for (int pos = find; pos < section.innerend; pos++)
            entity.content[pos] = 32;
      }
   }
}
