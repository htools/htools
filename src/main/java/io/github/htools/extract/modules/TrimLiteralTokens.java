package io.github.htools.extract.modules;

import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;
import io.github.htools.extract.Extractor;

/**
 * Trims Literal tokens, by reducing inter-string multiple whitespace characters
 * to single spaces.
 * <p>
 * @author jeroen
 */
public class TrimLiteralTokens extends ExtractorProcessor {

   private static Log log = new Log(TrimLiteralTokens.class);

   public TrimLiteralTokens(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attributename) {
      ExtractChannel attribute = entity.get(attributename);
      if (attribute.size() > 0) {
         StringBuilder sb = new StringBuilder();
         for (String chunk : attribute) {
            String s = chunk.toString().trim();
            if (s.length() > 0)
               sb.append(" ").append(s);
         }
         attribute.clear();
         if (sb.length() > 0) {
            String first = sb.deleteCharAt(0).toString().replaceAll("\\s{2,}|\\n", " ");
            attribute.add(first);
         } else {
            attribute.add("");
         }
      }
   }
}