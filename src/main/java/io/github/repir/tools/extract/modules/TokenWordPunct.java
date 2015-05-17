package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;

/**
 * Matches a sequence of letters/digits in the buffer.
 * @author jeroen
 */
public class TokenWordPunct extends TokenChar {
   public static final Log log = new Log( TokenWordPunct.class );
   boolean word[] = BoolTools.word0();
   boolean punct[] = BoolTools.invert(BoolTools.word());
   
   public TokenWordPunct(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
   }

    @Override
    public boolean[] setValidChars() {
        return BoolTools.firstAcceptedCharFromRegex(".");
    }
    
    @Override
    public int endOfToken(byte[] buffer, int pos, int endpos) {
        for (; pos < endpos && buffer[pos] == 0; pos++);
        if (pos < endpos) {
            if (word[buffer[pos] & 0xff]) {
               for (; pos < endpos && word[buffer[pos] & 0xff]; pos++);
            } else {
               for (; pos < endpos && punct[buffer[pos] & 0xff]; pos++);
            }
        }
        return pos;
    }
}
