package io.github.htools.extract;

import io.github.htools.extract.modules.ConvertHtmlASCIICodes;
import io.github.htools.extract.modules.ConvertHtmlAmpersand;
import io.github.htools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.htools.extract.modules.ConvertUnicodeDiacritics;
import io.github.htools.extract.modules.RemoveHtmlComment;
import io.github.htools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.htools.extract.modules.RemoveHtmlTags;
import io.github.htools.extract.modules.RemoveNonASCII;
import io.github.htools.extract.modules.TokenWord;
import io.github.htools.lib.Log;
/**
 *
 * @author jeroen
 */
public class DefaultHTMLTokenizer extends AbstractTokenizer {
   public static final Log log = new Log( DefaultHTMLTokenizer.class );

   public DefaultHTMLTokenizer() {
       super();
   }
   
   @Override
   protected void buildPreProcess() {
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));
   }
   
   @Override
   protected void buildProcess() {
       this.addProcess(ConvertHtmlAmpersand.class);
       this.addProcess(RemoveHtmlTags.class);
       this.addProcess(RemoveHtmlSpecialCodes.class);
   }

    @Override
    public Class getTokenMarker() {
        return TokenWord.class;
    }
}
