package io.github.repir.tools.extract;

import io.github.repir.tools.extract.modules.ConvertHtmlASCIICodes;
import io.github.repir.tools.extract.modules.ConvertHtmlAmpersand;
import io.github.repir.tools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.ConvertUnicodeDiacritics;
import io.github.repir.tools.extract.modules.MarkHead;
import io.github.repir.tools.extract.modules.MarkTitle;
import io.github.repir.tools.extract.modules.RemoveHtmlComment;
import io.github.repir.tools.extract.modules.RemoveHtmlSpecialCodes;
import io.github.repir.tools.extract.modules.RemoveHtmlTags;
import io.github.repir.tools.extract.modules.RemoveNonASCII;
import io.github.repir.tools.extract.modules.StoreLiteralSection;
import io.github.repir.tools.extract.modules.TokenInvertedWord;
import io.github.repir.tools.extract.modules.TokenWord;
import io.github.repir.tools.extract.modules.TokenizerRegexConf;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;

/**
 * Extracts the literal title from a HTML page.
 * @author jeroen
 */
public class HtmlTokenizedTitleExtractor extends ExtractorConf {
   public static final Log log = new Log( HtmlTokenizedTitleExtractor.class );

   public HtmlTokenizedTitleExtractor() {
       super();
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));

       this.addSectionMarker(MarkHead.class, "all", "head");
       this.addSectionMarker(MarkTitle.class, "head", "title");
       this.addSectionProcess("title", "tokenize", "result");

       this.addProcess("tokenize", ConvertHtmlAmpersand.class);
       this.addProcess("tokenize", RemoveHtmlTags.class);
       this.addProcess("tokenize", RemoveHtmlSpecialCodes.class);
       this.addProcess("tokenize", getTokenizer());
   }
   
   private TokenizerRegexConf getTokenizer() {
       TokenizerRegexConf tokenizerRegex = new TokenizerRegexConf(this, "");
        tokenizerRegex.setupTokenProcessor("word", TokenWord.class);
        //tokenizerRegex.setupTokenProcessor("space", TokenInvertedWord.class);
        return tokenizerRegex;
   }
   
   public ArrayList<String> extract(byte content[]) {
       Content entity = new Content();
       entity.setContent(content);
       this.process(entity);
       return process(content).get("result");
   }
}
