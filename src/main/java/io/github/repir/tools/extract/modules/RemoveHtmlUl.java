package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p/>
 * @author jeroen
 */
public class RemoveHtmlUl extends RemoveNestedTags {

   public static Log log = new Log(RemoveHtmlUl.class);

   private RemoveHtmlUl(Extractor extractor, String process) {
      super(extractor, process, "<ul(\\s|>)", "<\\s*/\\s*ul\\s*>");
   }
}
