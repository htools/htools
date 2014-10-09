package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearch;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Lib.Log;

/**
 * Matches a regular expression in the buffer. This is slower for mass tokenization, 
 * but easy to configure for complex patterns.
 * 
 * @author jeroen
 */
public class TokenRegex extends TokenProcessor {
   public static final Log log = new Log( TokenRegex.class );
   ByteSearch bytesearch;
   
   public TokenRegex(TokenizerRegex tokenizer, String name) {
       super(tokenizer, name);
       String pattern = tokenizer.extractor.getConfigurationString(name, "regex", "");
       bytesearch = ByteSearch.create(pattern);
   }
   
    @Override
    public boolean[] acceptedFirstChars() {
        return bytesearch.firstAcceptedChar();
    }   
   
    @Override
    public int process(byte[] buffer, int pos, int endpos) {
       ByteSearchPosition matchPos = bytesearch.matchPos(buffer, pos, endpos);
       return (matchPos.found())?matchPos.end:pos;
    }
}
