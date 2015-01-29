package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlAmpersand;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.TokenInvertedWord;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.lib.Log;
/**
 *
 * @author jeroen
 */
public class WordPunctTokenizer extends AbstractTokenizer {
   public static final Log log = new Log( WordPunctTokenizer.class );

   public WordPunctTokenizer() {
       super(TokenWord.class);
   }
   
    @Override
    protected void preProcess() {
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
    }

    @Override
    protected void process() {
       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
    }

    @Override
    protected void postProcess() {
        this.getTokenizer().setupTokenProcessor("space", TokenInvertedWord.class);
    }
}
