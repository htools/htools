package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.search.ByteSection;
import io.github.htools.search.ByteSectionScanned;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchPosition;

/**
 * Removes the following markup from Mediawiki source pages:
 * '' bold/italic ''
 * == headings ==
 * # all bullet and definition list variants (e.g. \n;, \n*)
 * <p>
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
