package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;

/**
 * Matches a sequence of letters/digits in the buffer.
 * @author jeroen
 */
public class TokenWord extends TokenChar {
   public static final Log log = new Log( TokenWord.class );
   
   public TokenWord(TokenizerRegex tokenizer, String name) {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.word();
    }

}
