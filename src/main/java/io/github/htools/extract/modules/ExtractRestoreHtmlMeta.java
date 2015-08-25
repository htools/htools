package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Extract and restore HTML Metadata for the keywords and description field.
 * This data is also added to the 'all' field.
 * <p>
 * @author jbpvuurens
 */
public class ExtractRestoreHtmlMeta extends ExtractRestore {

   public static Log log = new Log(ExtractRestoreHtmlMeta.class);
   private ByteSearch correcttype = ByteSearch.create("\\sname\\s*=\\s*(keywords|description|'keywords'|'description'|\"keywords\"|\"description\")");
   private ByteSearch content = ByteSearch.create("\\scontent\\s*=\\s*\\Q");
   private ByteSearch quote = ByteSearch.create("['\"]");

   public ExtractRestoreHtmlMeta(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      section.innerstart--;
      if (correcttype.exists(entity.content, section.innerstart, section.innerend)) {
            ByteSearchPosition c = content.findPos(entity.content, section.innerstart, section.innerend);
            if (c.found()) {
               c.start = quote.find(entity.content, c.start+8, c.end) + 1;
               add(entity, c.start, --c.end);
            }
      }
   }
}