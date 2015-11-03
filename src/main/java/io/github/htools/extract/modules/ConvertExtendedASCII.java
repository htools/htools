package io.github.htools.extract.modules;

import io.github.htools.collection.ArrayMap;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.StrTools;
import java.util.ArrayList;

/**
 * Converts unicode characters with diacritics to their corresponding
 * ASCII character.
 * <p>
 * @author jbpvuurens
 */
public class ConvertExtendedASCII extends Translator {

   public static Log log = new Log(ConvertExtendedASCII.class);

   public ConvertExtendedASCII(Extractor extractor, String process) {
      super(extractor, process);
   }
   
   protected ArrayMap<byte[], byte[]> initSearchReplace() {
      ArrayMap<byte[], byte[]> searchReplace = new ArrayMap();
      for (int i = 0; i < StrTools.asciiextendedbyte.length; i++) {
         byte search[] = new byte[1];
         byte replace[] = new byte[1];
         search[0] = StrTools.asciiextendedbyte[i];
         replace[0] = StrTools.asciibyte[i];
         searchReplace.add(search, replace);
      }
      return searchReplace;
   }
}
