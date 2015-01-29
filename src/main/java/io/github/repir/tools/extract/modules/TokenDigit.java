package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;

/**
 * Matches a sequence of digits in the buffer.
 * @author jeroen
 */
public class TokenDigit extends TokenChar {
   public static final Log log = new Log( TokenDigit.class );
   
   public TokenDigit(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.digit();
    }
}
