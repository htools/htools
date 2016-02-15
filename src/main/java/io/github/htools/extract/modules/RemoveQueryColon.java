package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;

/**
 * Remove codes with a colon, e.g. "site:.com", which are sometimes in test 
 * sets, but are not supported by Repir.
 * <p>
 * @author jeroen
 */
public class RemoveQueryColon extends ExtractorProcessor {

   private static Log log = new Log(RemoveQueryColon.class);
   ByteSearch decimal = ByteSearch.create("\\S*:\\S+");

   public RemoveQueryColon(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      for (ByteSearchPosition pos : decimal.findAllPos(entity.content, section.innerstart, section.innerend)) {
         for ( int i = pos.start; i < pos.end; i++)
            entity.content[i] = 32;
      }
   }
}