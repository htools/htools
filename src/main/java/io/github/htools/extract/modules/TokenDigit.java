package io.github.htools.extract.modules;

import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

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
