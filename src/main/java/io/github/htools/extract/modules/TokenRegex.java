package io.github.htools.extract.modules;

import io.github.htools.extract.ExtractorConf;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearch;
import io.github.htools.search.ByteSearchPosition;

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
