package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import static io.github.repir.tools.Lib.ByteTools.*;
import io.github.repir.tools.Type.Tuple2Comparable;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;

/**
 * Removes HTML Tags, except for the contents in between open and close tags,
 * and except for contents in the ALT attribute.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlTagsExceptAlt extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlTagsExceptAlt.class);
   public boolean tagname[] = new boolean[256];
   public boolean tag[] = new boolean[256];
   public boolean quote[] = new boolean[256];
   public ByteSearch altattribute = ByteSearch.create("\\salt\\s*=\\s*\\Q");
   public ByteSearch start = ByteSearch.create("<");

   private RemoveHtmlTagsExceptAlt(Extractor extractor, String process) {
      super(extractor, process);
      for (int i = 0; i < 128; i++) {
         tagname[i] = (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z') || i == '/' || i == '!';
         tag[i] = (i >= 'a' && i <= 'z') || (i >= 'A' && i <= 'Z');
         quote[i] = (i == '"' || i == '\'');
      }
   }

   @Override
   public void process(Entity entity, Entity.Section section, String attribute) {
      //log.info("process %d %d", startpos, endpos);
      int startpos = section.open;
      int endpos = section.close;
      for (int pos : start.findAll(entity.content, section.open, section.close - 1)) {
         if (tagname[entity.content[pos + 1] & 0xFF]) { // if it s a valid tag 
            int endtag = findQuoteSafeTagEnd(entity, pos + 1, section.close);
            if (tag[entity.content[pos + 1] & 0xFF]) { // if it is a valid open tag
               ByteSearchPosition altpos = altattribute.findPosQuoteSafe(entity.content, pos, endtag);
               if (altpos.found()) {
                  for (int i = pos; i < altpos.start; i++) {
                     entity.content[i] = 32;
                  }
                  for (int i = altpos.start; quote[entity.content[i] & 0xFF]; i++) {
                     entity.content[i + 1] = 32;
                  }
                  for (int i = altpos.end - 1; i < endtag; i++) {
                     entity.content[i + 1] = 32;
                  }
                  continue;
               }
            }
            for (int i = pos; i <= endtag; i++) {
               entity.content[i] = 32;
            }
         }
      }
      //log.info("afterTagEraser %s", new String(buffer, 0, endpos));
   }
}
