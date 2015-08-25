package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.ExtractChannel;


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
    * @param buffer
    * @param pos
    * @param endpos
    * @return end position of a valid token, or pos if not valid 
    */
   public abstract int process(byte [] buffer, int pos, int endpos) ;
}
