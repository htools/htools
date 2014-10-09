package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Extractor;
import java.util.ArrayList;

/**
 * Tokenizer with splitpeek and lowercase removed.
 *
 * @author jeroen
 */
public class TokenizerQuery extends Tokenizer {

   public static Log log = new Log(TokenizerQuery.class);

   public TokenizerQuery(Extractor extractor, String process) {
      super(extractor, process);
   }

   protected void setSplitNumbers() {
   }

   protected void setMaxTokenLength() {
      maxtokenlength = Integer.MAX_VALUE;
   }

   protected void setCharacterTranslation() {
      for (int i = 0; i < 256; i++) {
         tokenchar[i] = (char) i;
      }
   }
}
