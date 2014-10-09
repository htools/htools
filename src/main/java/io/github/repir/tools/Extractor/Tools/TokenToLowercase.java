package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.Log;
import io.github.repir.tools.Lib.BoolTools;

/**
 * convert all uppercase characters to lowercase. This processor is not context
 * aware so multi-byte characters such as unicode characters should be converted
 * before running this processor.
 * <p/>
 * @author jbpvuurens
 */
public class TokenToLowercase extends TokenSubProcessor {

   public static Log log = new Log(TokenToLowercase.class);
   boolean capital[];
   
   public TokenToLowercase(TokenizerRegex extractor, String process) {
      super(extractor, process);
      capital = BoolTools.createASCIIAcceptRange('A', 'Z');
   }

   @Override
   public int process(byte[] buffer, int startpos, int endpos) {
      int p;
      for (; startpos < endpos; startpos++) {
         if (buffer[startpos] >= 'A' && buffer[startpos] <= 'Z') {
            buffer[startpos] |= 32;
         }
      }
      return endpos;
   }
}
