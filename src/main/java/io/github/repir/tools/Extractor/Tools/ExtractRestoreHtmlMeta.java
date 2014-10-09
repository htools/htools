package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Extract and restore HTML Metadata for the keywords and description field.
 * This data is also added to the 'all' field.
 * <p/>
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
   public void process(Entity entity, Section section, String attribute) {
      section.open--;
      if (correcttype.exists(entity.content, section.open, section.close)) {
            ByteSearchPosition c = content.findPos(entity.content, section.open, section.close);
            if (c.found()) {
               c.start = quote.find( entity.content, c.start+8, c.end) + 1;
               add(entity, c.start, --c.end);
            }
      }
   }
}