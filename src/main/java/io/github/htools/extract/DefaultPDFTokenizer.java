package io.github.htools.extract;

import io.github.htools.extract.modules.ConvertPDFHyphen;
import io.github.htools.extract.modules.TokenPDF;
import io.github.htools.lib.Log;
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
