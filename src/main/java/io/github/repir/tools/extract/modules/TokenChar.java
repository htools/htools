package io.github.repir.tools.extract.modules;

import io.github.repir.tools.lib.BoolTools;
import io.github.repir.tools.lib.Log;

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
    public int preprocess(byte[] buffer, int pos, int endpos) {
        for (; pos < endpos && valid[buffer[pos] & 0xff]; pos++);
        return pos;
    }

}
