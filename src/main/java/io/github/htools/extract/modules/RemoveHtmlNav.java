package io.github.htools.extract.modules;

import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveHtmlNav extends RemoveNestedTags {

   public static Log log = new Log(RemoveHtmlNav.class);

   private RemoveHtmlNav(Extractor extractor, String process) {
      super(extractor, process, "<nav(\\s|>)", "<\\s*/\\s*nav\\s*>");
   }
}
