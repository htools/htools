package io.github.htools.extract.modules;

import io.github.htools.collection.ArrayMap;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;
import io.github.htools.lib.StrTools;
import io.github.htools.search.ByteSearchSection;
import java.util.ArrayList;
import java.util.Map;

/**
 * Converts unicode characters with diacritics to their corresponding
 * ASCII character.
 * <p>
 * @author jbpvuurens
 */
public class ConvertUnicodeDiacritics extends Translator {

   public static Log log = new Log(ConvertUnicodeDiacritics.class);

   public ConvertUnicodeDiacritics(Extractor extractor, String process) {
      super(extractor, process);
      ArrayMap<byte[], byte[]> b = translations[0xC3];
//      for (Map.Entry<byte[], byte[]> entry : b) {
//          log.printf("%s%s", PrintTools.memoryDump(entry.getKey()), PrintTools.memoryDump(entry.getValue()));
//      }
   }
   
   @Override
    public void process(Content entity, ByteSearchSection section, String attribute) {
        //log.info("Translator %d %d\n%s", section.innerstart, section.innerend, PrintTools.memoryDump(entity.content, section.innerstart, section.innerend));
        super.process(entity, section, attribute);
    }
    
    protected ArrayMap<byte[], byte[]> initSearchReplace() {
      ArrayMap<byte[], byte[]> searchReplace = new ArrayMap();
      for (int i = 0; i < StrTools.asciibyte20.length; i++) {
         byte search[] = new byte[3];
         byte replace[] = new byte[3];
         search[0] = (byte)0xe2;
         search[1] = StrTools.unicodebyte20[i * 3 + 1];
         search[2] = StrTools.unicodebyte20[i * 3 + 2];
         replace[0] = 0;
         replace[1] = 0;
         replace[2] = StrTools.asciibyte20[i];
         searchReplace.add(search, replace);
      }
      for (int i = 0; i < StrTools.unicodebyteC3.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC3;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC3[i];
         replace[1] = StrTools.asciibyteC3[i];
         searchReplace.add(search, replace);
      }
      for (int i = 0; i < StrTools.unicodebyteC32byte.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC3;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC32byte[i];
         replace = ByteTools.toBytes(StrTools.asciibyteC32byte[i]);
         searchReplace.add(search, replace);
      }
      for (int i = 0; i < StrTools.asciiextendedbyte.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC2;
         replace[0] = 0;
         search[1] = StrTools.asciiextendedbyte[i];
         replace[1] = StrTools.asciiextendedbyte[i];
         searchReplace.add(search, replace);
      }
      for (int i = 0; i < StrTools.unicodebyteC5.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC5;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC5[i];
         replace[1] = StrTools.asciibyteC5[i];
         searchReplace.add(search, replace);
      }
      return searchReplace;
   }
}
