package io.github.htools.extract.modules;

import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Matches a sequence of letters/digits in the buffer.
 * @author jeroen
 */
public class TokenWord extends TokenChar {
   public static final Log log = new Log( TokenWord.class );
   
   public TokenWord(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.word();
    }

}
