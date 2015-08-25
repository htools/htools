package io.github.htools.extract.modules;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Remove HTML special codes that are written in between &amp; ;, such as <code>&tilde;</code>
 * the codes are replaced by spaces to prevent words before and after from being linked.
 * <p>
 * @author jeroen
 */
public class RemoveHtmlSpecialCodes extends ExtractorProcessor {

   public static Log log = new Log(RemoveHtmlSpecialCodes.class);
   ByteRegex regex = new ByteRegex("&[A-Za-z]+;");

   private RemoveHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      ArrayList<ByteSearchPosition> pos = regex.findAllPos(entity.content, section.innerstart, section.innerend);
      for (ByteSearchPosition p : pos) {
         for (int i = p.start; i < p.end; i++) {
            entity.content[i] = 32;
         }
      }
   }
}
