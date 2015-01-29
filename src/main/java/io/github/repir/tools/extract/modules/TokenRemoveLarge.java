package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.lib.Log;

/**
 * Removes attribute values that have a size over (default=25) characters. The
 * maximum length for the attribute characters can be set in the configuration
 * e.g. extractor.[process].removelargetokens = 25
 * <p/>
 * @author jeroen
 */
public class TokenRemoveLarge extends TokenSubProcessor {

   private static Log log = new Log(TokenRemoveLarge.class);
   final int maxlength;

   public TokenRemoveLarge(TokenizerRegexConf tokenizer, String process) {
      this(tokenizer, process, ((ExtractorConf)tokenizer.extractor).getConfigurationInt( process, "removelargetokens", 25));
   } 

   public TokenRemoveLarge(TokenizerRegex tokenizer, String process, int tokensize) {
      super(tokenizer, process);
      maxlength = tokensize;
   } 

   @Override
   public int process(byte[] buffer, int startpos, int endpos) {
       if (endpos - startpos > maxlength) {
          int toomany = endpos - startpos - maxlength;
          for (int p = startpos; toomany > 0 && p < endpos; p++) {
              if (buffer[p] == 0)
                  toomany--;
          }
          if (toomany > 0) {
              log.info("remove large %d %d %d", startpos, endpos, maxlength);
              return startpos;
              
          }
       }
       return endpos;
   }
}