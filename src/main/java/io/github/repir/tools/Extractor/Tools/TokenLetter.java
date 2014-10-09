package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.Log;

/**
 * Matches a sequence of letters in the buffer.
 * @author jeroen
 */
public class TokenLetter extends TokenChar {
   public static final Log log = new Log( TokenLetter.class );
   
   public TokenLetter(TokenizerRegex tokenizer, String name) {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.letter();
    }

}
