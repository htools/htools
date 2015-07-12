package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.search.ByteSection;
import io.github.repir.tools.search.ByteSectionScanned;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.search.ByteSearchPosition;

/**
 * Removes the following markup from Mediawiki source pages:
 * '' bold/italic ''
 * == headings ==
 * # all bullet and definition list variants (e.g. \n;, \n*)
 * <p/>
 * @author jbpvuurens
 */
public class RemoveWikipediaMarkup extends ExtractorProcessor {

   public static Log log = new Log(RemoveWikipediaMarkup.class);
   public ByteRegex letters = new ByteRegex("(==+|''+)");
   public ByteRegex bullets = new ByteRegex("\n#*(#+|[\\*]+|:+|;+|\\-\\-+)");

   public RemoveWikipediaMarkup(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchPosition s : letters.findAllPos(section)) {
          for (int p = s.start; p < s.end; p++)
              entity.content[p] = 32;
      }
      for (ByteSearchPosition s : bullets.findAllPos(section)) {
          log.trace("bullet %d", s.start);
          for (int p = s.start + 1; p < s.end; p++)
              entity.content[p] = 32;
      }
   }
}
