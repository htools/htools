package io.github.htools.extract;

import io.github.htools.extract.modules.*;
import io.github.htools.lib.Log;

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
       return process(content).get("result").getTerms();
   }
}
