package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlHeader extends RemoveNestedTags {

   public static Log log = new Log(RemoveHtmlHeader.class);

   private RemoveHtmlHeader(Extractor extractor, String process) {
      super(extractor, process, "<header(\\s|>)", "<\\s*/\\s*header\\s*>");
   }
}
