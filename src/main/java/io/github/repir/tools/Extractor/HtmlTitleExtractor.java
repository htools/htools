package io.github.repir.tools.Extractor;

import io.github.repir.tools.Extractor.Tools.ConvertHtmlASCIICodes;
import io.github.repir.tools.Extractor.Tools.ConvertHtmlSpecialCodes;
import io.github.repir.tools.Extractor.Tools.ConvertUnicodeDiacritics;
import io.github.repir.tools.Extractor.Tools.MarkHead;
import io.github.repir.tools.Extractor.Tools.MarkTitle;
import io.github.repir.tools.Extractor.Tools.RemoveHtmlComment;
import io.github.repir.tools.Extractor.Tools.RemoveNonASCII;
import io.github.repir.tools.Extractor.Tools.StoreLiteralSection;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Extracts the literal title from a HTML page.
 * @author jeroen
 */
public class HtmlTitleExtractor extends Extractor {
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

       this.addProcess("store", StoreLiteralSection.class);
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
