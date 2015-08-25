package io.github.htools.extract.modules;

import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveHtmlArticle extends RemoveNestedTags {

   public static Log log = new Log(RemoveHtmlArticle.class);

   private RemoveHtmlArticle(Extractor extractor, String process) {
      super(extractor, process, "<article(\\s|>)", "<\\s*/\\s*article\\s*>");
   }
}
