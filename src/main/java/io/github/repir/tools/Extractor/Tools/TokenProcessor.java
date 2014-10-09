package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.RemovedException;
import io.github.repir.tools.Extractor.EntityChannel;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchString;
import io.github.repir.tools.Lib.ByteTools;
import java.util.ArrayList;


/**
 * Implementations can perform a conversion or extraction on an {@link Entity},
 * either modifying their raw byte contents, extracting an {@link EntityChannel}, or
 * modifying an {@link EntityChannel}.
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
