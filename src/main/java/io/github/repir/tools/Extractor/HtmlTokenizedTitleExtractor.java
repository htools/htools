package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlAmpersand;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.ConvertUnicodeDiacritics;
import io.github.repir.tools.Extractor.Tools.MarkHead;
import io.github.repir.tools.Extractor.Tools.MarkTitle;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlComment;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlTags;
import io.github.repir.tools.Extractor.Tools.RemoveNonASCII;
import io.github.repir.tools.Extractor.Tools.StoreLiteralSection;
import io.github.repir.tools.Extractor.Tools.TokenInvertedWord;
import io.github.repir.tools.Extractor.Tools.TokenWord;
import io.github.repir.tools.Extractor.Tools.TokenizerRegex;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Extracts the literal title from a HTML page.
 * @author jeroen
 */
public class HtmlTokenizedTitleExtractor extends Extractor {
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
   
   private TokenizerRegex getTokenizer() {
       TokenizerRegex tokenizerRegex = new TokenizerRegex(this, "");
        tokenizerRegex.setupTokenProcessor("word", TokenWord.class);
        //tokenizerRegex.setupTokenProcessor("space", TokenInvertedWord.class);
        return tokenizerRegex;
   }
   
   public ArrayList<String> extract(byte content[]) {
       Entity entity = new Entity();
       entity.setContent(content);
       this.process(entity);
       return entity.get("result");
   }
   
   
   public ArrayList<String> extract(String text) {
       return extract(text.getBytes());
   }
}
