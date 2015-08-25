package io.github.htools.extract.modules;

import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Matches a sequence of letters/digits in the buffer.
 * @author jeroen
 */
public class TokenInvertedWord extends TokenChar {
   public static final Log log = new Log( TokenInvertedWord.class );
   
   public TokenInvertedWord(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.firstAcceptedCharFromRegex("\\W");
    }
}
