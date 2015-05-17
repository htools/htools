package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.StrTools;

/**
 * Converts unicode characters with diacritics to their corresponding
 * ASCII character.
 * <p/>
 * @author jbpvuurens
 */
public class ConvertUnicodeDiacritics extends Translator {

   public static Log log = new Log(ConvertUnicodeDiacritics.class);

   public ConvertUnicodeDiacritics(Extractor extractor, String process) {
      super(extractor, process);
      int i;
      for (i = 0; i < StrTools.asciibyte20.length; i++) {
         byte search[] = new byte[3];
         byte replace[] = new byte[3];
         search[0] = (byte)0xe2;
         search[1] = (byte)0x80;
         search[2] = StrTools.unicodebyte20[i * 3 + 2];
         replace[0] = 0;
         replace[1] = 0;
         replace[2] = StrTools.asciibyte20[i];
         this.add(search, replace);
      }
      for (i = 0; i < StrTools.unicodebyteC3.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC3;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC3[i];
         replace[1] = StrTools.asciibyteC3[i];
         this.add(search, replace);
      }
      for (i = 0; i < StrTools.unicodebyteC2.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC2;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC2[i];
         replace[1] = StrTools.asciibyteC2[i];
         this.add(search, replace);
      }
      for (i = 0; i < StrTools.unicodebyteC5.length; i++) {
         byte search[] = new byte[2];
         byte replace[] = new byte[2];
         search[0] = (byte) 0xC5;
         replace[0] = 0;
         search[1] = StrTools.unicodebyteC5[i];
         replace[1] = StrTools.asciibyteC5[i];
         this.add(search, replace);
      }
      for (i = 0; i < StrTools.asciiextendedbyte.length; i++) {
         byte search[] = new byte[1];
         byte replace[] = new byte[1];
         search[0] = StrTools.asciiextendedbyte[i];
         replace[0] = StrTools.asciibyte[i];
         this.add(search, replace);
      }
   }
}
