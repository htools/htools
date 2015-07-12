package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlAmpersand;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.TokenInvertedWord;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.extract.modules.TokenWordPunct;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
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
