package io.github.htools.extract.modules;

import io.github.htools.lib.BoolTools;
import io.github.htools.lib.Log;

/**
 * Matches a sequence of letters in the buffer.
 * @author jeroen
 */
public abstract class TokenChar extends TokenProcessorWithSubs {
   public static final Log log = new Log( TokenChar.class );
   boolean valid[];
   
   public TokenChar(TokenizerRegex tokenizer, String name) throws ClassNotFoundException {
       super(tokenizer, name);
       valid = setValidChars();
       valid[0] = true; // by default!
   }
   
   public abstract boolean[] setValidChars();
   
   @Override
   public boolean[] acceptedFirstChars() {
       return valid;
   }
   
    @Override
    public int endOfToken(byte[] buffer, int pos, int endpos) {
        for (; pos < endpos && valid[buffer[pos] & 0xff]; pos++);
        return pos;
    }

}
