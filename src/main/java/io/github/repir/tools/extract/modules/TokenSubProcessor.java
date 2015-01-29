package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;
import io.github.repir.tools.lib.Log;


/**
 * A TokenSubProcessor can be assigned to a TokenProcessorWithSubs by calling
 * {@link TokenProcessorWithSubs#addSubProcessor(java.lang.Class)} or by 
 * configuring +extractor.[token].process = [subprocessor]
 * <p/>
 * When a TokenProcessorWithSubs has accepted a token, is will sequentially
 * call it's SubProcessor to operate on the identified token in the byte array.
 * A subprocessor may modify the token in place (e.g. lowercase) or contain 
 * validation rules to reject the token (e.g. verify URL validity). To reject
 * a token it should return endpos equal to startpos.
 * 
 * @author jer
 */
public abstract class TokenSubProcessor extends TokenProcessor {
   public static final Log log = new Log(TokenSubProcessor.class);
    
   public TokenSubProcessor(TokenizerRegex tokenizer, String name) {
      super(tokenizer, name);
   }
   
   public boolean[] acceptedFirstChars() {
       log.fatal("Cannot use a TokenSubProcessor as TokenProcessor");
       return null;
   }
}
