package io.github.htools.extract.modules;

import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;

/**
 * Removes HTML tags from the content, leaving the content in between open and
 * close tags.
 * <p>
 * @author jeroen
 */
public class RemoveHtmlTable extends RemoveNestedTags {

   public static Log log = new Log(RemoveHtmlTable.class);

   private RemoveHtmlTable(Extractor extractor, String process) {
      super(extractor, process, "<table(\\s|>)", "<\\s*/\\s*table\\s*>");
   }
}
