package io.github.htools.extract;

import io.github.htools.extract.modules.ConvertHtmlASCIICodes;
import io.github.htools.extract.modules.ConvertHtmlSpecialCodes;
import io.github.htools.extract.modules.ConvertUnicodeDiacritics;
import io.github.htools.extract.modules.ConvertWhitespace;
import io.github.htools.extract.modules.MarkHead;
import io.github.htools.extract.modules.MarkTitle;
import io.github.htools.extract.modules.RemoveHtmlComment;
import io.github.htools.extract.modules.RemoveNonASCII;
import io.github.htools.extract.modules.StoreLiteralSection;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Extracts the literal title from a HTML page. 
 * Whitespaces are converted to a single space
 * @author jeroen
 */
public class HtmlTitleExtractor extends ExtractorConf {
   public static final Log log = new Log( HtmlTitleExtractor.class );

   public HtmlTitleExtractor() {
       super();
       this.addPreProcessor(RemoveHtmlComment.class);
       this.addPreProcessor(ConvertHtmlASCIICodes.class);
       this.addPreProcessor(ConvertHtmlSpecialCodes.class);
       this.addPreProcessor(ConvertUnicodeDiacritics.class);
       this.addPreProcessor(new RemoveNonASCII(this, true));

       this.addSectionMarker(MarkHead.class, "all", "head");
       this.addSectionMarker(MarkTitle.class, "head", "title");
       this.addSectionProcess("title", "store", "result");

       this.addProcess("store", ConvertWhitespace.class);
       this.addProcess("store", StoreLiteralSection.class);
   }
   
   public ArrayList<String> extract(byte content[]) {
       return process(content).get("result");
   }
}
