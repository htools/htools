package io.github.htools.extract.modules;

import io.github.htools.extract.Content;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.Log;
import io.github.htools.search.ByteSearchSection;

/**
 * Extract and restore HTML title. This is done to allow erasing the head 
 * section of a HTML document, and then put back the title for tokenization.
 * <p>
 * @author jbpvuurens
 */
public class ExtractRestoreHtmlTitle extends ExtractRestore {

   public static Log log = new Log(ExtractRestoreHtmlTitle.class);

   public ExtractRestoreHtmlTitle(Extractor extractor, String process) {
      super(extractor, process);
   }

   @Override
   public void process(Content entity, ByteSearchSection section, String attribute) {
      add(entity, section.innerstart, section.innerend);
   }
}