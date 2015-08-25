package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.RemovedException;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;


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
      if ((p < 0) || (p - pos > 1000)) {
         p = endtag.findNoQuoteSafe(entity.content, pos, end);
      }
      return p;
   }
}
