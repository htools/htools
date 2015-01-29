package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.extract.ExtractorConf;
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
