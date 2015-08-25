package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchPosition;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;

/**
 * Convert HTML code <code>&amp;</code> to &amp;
 * @author jbpvuurens
 */
public class ConvertHtmlAmpersand extends ExtractorProcessor {

   public static Log log = new Log(ConvertHtmlAmpersand.class);
   ByteSearch regex = ByteSearch.create("&amp;");

   public ConvertHtmlAmpersand(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchPosition p : regex.findAllPos(entity.content, section.innerstart, section.innerend)) {
         for (int i = p.start + 1; i < p.end; i++) {
            entity.content[i] = 0;
         }
      }
   }
}
