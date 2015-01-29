package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlAmpersand;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.ConvertUnicodeDiacritics;
import io.github.repir.tools.extract.modules.RemoveHtmlComment;
import io.github.repir.tools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.RemoveHtmlTags;
import io.github.repir.tools.extract.modules.RemoveNonASCII;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.lib.Log;
/**
 *
 * @author jeroen
 */
public class DefaultHTMLTokenizer extends AbstractTokenizer {
   public static final Log log = new Log( DefaultHTMLTokenizer.class );

   public DefaultHTMLTokenizer() {
       super(TokenWord.class);
   }
   
   @Override
   protected void preProcess() {
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));
   }
   
   @Override
   protected void process() {
       this.addProcess(ConvertHtmlAmpersand.class);
       this.addProcess(RemoveHtmlTags.class);
       this.addProcess(RemoveHtmlSpecialCodes.class);
   }
   
   @Override
   protected void postProcess() {
       this.addProcess("tokenize", tokenizer);
   }
}
