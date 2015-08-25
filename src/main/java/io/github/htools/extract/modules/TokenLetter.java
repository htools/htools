package io.github.htools.extract.modules;

import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Matches a sequence of letters in the buffer.
 * @author jeroen
 */
public class TokenLetter extends TokenChar {
   public static final Log log = new Log( TokenLetter.class );
   
   public TokenLetter(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.letter();
    }

}
