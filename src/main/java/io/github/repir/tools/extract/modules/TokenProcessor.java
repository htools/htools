package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Content;
import io.github.repir.tools.extract.ExtractChannel;


/**
 * Implementations can perform a conversion or extraction on an {@link Content},
 * either modifying their raw byte contents, extracting an {@link ExtractChannel}, or
 * modifying an {@link ExtractChannel}.
 * @author jer
 */
public abstract class TokenProcessor {
   TokenizerRegex tokenizer;
   String name;
    
   public TokenProcessor(TokenizerRegex tokenizer, String name) {
      this.tokenizer = tokenizer;
      this.name = name;
   }
   
   public abstract boolean[] acceptedFirstChars();
   
   /**
    * @param entity
    * @param pos
    * @return end position of a valid token, or pos if not valid 
    */
   public abstract int process(byte [] buffer, int pos, int endpos) ;
}
