package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;

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
   public void process(Entity entity, Entity.Section section, String attribute) {
      for (ByteSearchPosition pos : decimal.findAllPos(entity.content, section.open, section.close)) {
         for ( int i = pos.start; i < pos.end; i++)
            entity.content[i] = 32;
      }
   }
}