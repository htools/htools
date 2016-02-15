package io.github.htools.extract;

import io.github.htools.extract.modules.ConvertHtmlASCIICodes;
import io.github.htools.extract.modules.ConvertHtmlAmpersand;
import io.github.htools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.htools.extract.modules.TokenWordPunct;
import io.github.htools.lib.Log;
/**
 *
 * @author jeroen
 */
public class WordPunctTokenizer extends AbstractTokenizer {
   public static final Log log = new Log( WordPunctTokenizer.class );

   public WordPunctTokenizer() {
       super();
   }
   
    @Override
    protected void buildPreProcess() {
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
    }

    @Override
    protected void buildProcess() {
       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
    }

    @Override
    public Class getTokenMarker() {
        return TokenWordPunct.class;
    }
}
