package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;

/**
 * Matches a sequence of digits in the buffer.
 * @author jeroen
 */
public class TokenDigit extends TokenChar {
   public static final Log log = new Log( TokenDigit.class );
   
   public TokenDigit(TokenizerRegex tokenizer, String name) {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.digit();
    }
}
