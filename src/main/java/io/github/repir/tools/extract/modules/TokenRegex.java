package io.github.repir.tools.extract.modules;

import io.github.repir.tools.search.ByteRegex;
import io.github.repir.tools.search.ByteSearch;
import io.github.repir.tools.search.ByteSearchPosition;
import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.extract.ExtractorConf;
import io.github.repir.tools.lib.Log;

/**
 * Matches a regular expression in the buffer. This is slower for mass tokenization, 
 * but easy to configure for complex patterns.
 * 
 * @author jeroen
 */
public class TokenRegex extends TokenProcessor {
   public static final Log log = new Log( TokenRegex.class );
   ByteSearch bytesearch;
   String tokenchars;
   
   public TokenRegex(TokenizerRegexConf tokenizer, String name) {
       this(tokenizer, name, ((ExtractorConf)tokenizer.extractor).getConfigurationString(name, "regex", ""));
   }
   
   public TokenRegex(TokenizerRegex tokenizer, String name, String tokenchars) {
       super(tokenizer, name);
       bytesearch = ByteSearch.create(tokenchars);
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
