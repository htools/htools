package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertPDFHyphen;
import io.github.repir.tools.extract.modules.TokenPDF;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.extract.modules.TokenizerRegexConf;
import io.github.repir.tools.lib.Log;
/**
 *
 * @author jeroen
 */
public class DefaultPDFTokenizer extends AbstractTokenizer {
   public static final Log log = new Log( DefaultPDFTokenizer.class );

   public DefaultPDFTokenizer() {
       super();
   }
   
   
   
    @Override
    protected void buildPreProcess() {
      this.addPreProcessor(ConvertPDFHyphen.class);
    }

    @Override
    protected void buildProcess() {
    }

    @Override
    public Class getTokenMarker() {
        return TokenPDF.class;
    }
}
