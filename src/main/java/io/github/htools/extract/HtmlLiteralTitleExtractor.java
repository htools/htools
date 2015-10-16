package io.github.htools.extract;

import io.github.htools.extract.modules.MarkHead;
import io.github.htools.extract.modules.MarkTitle;
import io.github.htools.extract.modules.StoreLiteralSection;
import io.github.htools.lib.Log;
import java.util.ArrayList;

/**
 * Extracts the literal title from a HTML page.
 * @author jeroen
 */
public class HtmlLiteralTitleExtractor extends Extractor {
   public static final Log log = new Log( HtmlLiteralTitleExtractor.class );

   public HtmlLiteralTitleExtractor() {
       super();

       this.addSectionMarker(MarkHead.class, "all", "head");
       this.addSectionMarker(MarkTitle.class, "head", "title");
       this.addSectionProcess("title", "store", "result");
       this.addProcess("store", StoreLiteralSection.class);
   }
   
   public String extract(byte content[]) {
       Content entity = new Content();
       entity.setContent(content);
       this.process(entity);
       ArrayList<String> list = process(content).get("result").getTerms();
       return (list.size() > 0)?list.get(0):null;
   }
}
