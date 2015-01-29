package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.RemovedException;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchSection;


/**
 * Implementations can perform a conversion or extraction on an {@link Content},
 * either modifying their raw byte contents, extracting an {@link ExtractChannel}, or
 * modifying an {@link ExtractChannel}.
 * @author jer
 */
public abstract class ExtractorProcessor {
   public String process;
   public static ByteSearch endtag = ByteSearch.create(">").QuoteSafe();
   
   public ExtractorProcessor(Extractor extractor, String process) {
      this.process = process;
   }

   public abstract void process(Content entity, ByteSearchSection section, String entityattribute) throws RemovedException ;

   public int findQuoteSafeTagEnd(Content entity, int pos, int end) {
      int p = endtag.find(entity.content, ++pos, end);
      if ((p < 0) || (p - pos > 50)) {
         p = endtag.findNoQuoteSafe(entity.content, pos, end);
      }
      return p;
   }
}
