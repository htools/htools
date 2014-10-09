package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Extractor.Entity;
import io.github.repir.tools.Extractor.Entity.Section;
import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Extract and restore HTML title. This is done to allow erasing the head 
 * section of a HTML document, and then put back the title for tokenization.
 * <p/>
 * @author jbpvuurens
 */
public class ExtractRestoreHtmlTitle extends ExtractRestore {

   public static Log log = new Log(ExtractRestoreHtmlTitle.class);

   public ExtractRestoreHtmlTitle(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Entity entity, Section section, String attribute) {
      add(entity, section.open, section.close);
   }
}