package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.search.ByteSearchSection;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * This processor converts tag names to lowercase, for easy processing Note:
 * this has to be done in the raw byte array instead of using regular
 * expressions as the byte array may contain non-ASCII.
 * <p>
 * @author jbpvuurens
 */
public class ConvertTagnamesToLowercase extends ExtractorProcessor {

   public static Log log = new Log(ConvertTagnamesToLowercase.class);
   public ByteRegex tagname = new ByteRegex("</?[A-Za-z]\\w*?[/\\s>]");
   public boolean[] isTagNameEnd = new boolean[128];
   char minbyte = 0;
   char maxbyte = 127;
   byte byte1;

   public ConvertTagnamesToLowercase(Extractor extractor, String process) {
      super(extractor, process);
      BoolTools.setBooleanArray(isTagNameEnd, minbyte, maxbyte, false);
      BoolTools.setBooleanArray(isTagNameEnd, true, '>', ' ', '/', '\t', '\n', '\r');
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      byte buffer[] = entity.content;
      ArrayList<ByteSearchPosition> findAll = tagname.findAllPos(buffer, section.innerstart, section.innerend);
      for (ByteSearchPosition p : findAll) {
         p.end--;
         for (int i = p.start; i < p.end; i++) {
            buffer[i] |= 32;
         }
      }
   }
}
