package io.github.htools.extract.modules;

import io.github.htools.lib.Log;
import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.extract.ExtractorConf;
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
