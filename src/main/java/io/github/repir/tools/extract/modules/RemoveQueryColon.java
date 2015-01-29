package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.search.ByteSearchSection;
import io.github.repir.tools.lib.Log;

/**
 * Remove codes with a colon, e.g. "site:.com", which are sometimes in test 
 * sets, but are not supported by Repir.
 * <p/>
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