package io.github.repir.tools.Extractor.Tools;

import io.github.repir.tools.Extractor.Extractor;
import io.github.repir.tools.Lib.Log;

/**
 * Replaces certain HTML special codes with a byte equivalent. Using Translator
 * the replacements must have the same byte length. If the byte length of the replacement
 * is smaller than the search key, the replacement is pre padded with \0 bytes
 * which are ignored during extraction.
 * <p/>
 * @author jeroen
 */
public class ConvertHtmlSpecialCodes extends Translator {

   public static Log log = new Log(ConvertHtmlSpecialCodes.class);

   public ConvertHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
      add("&quot;", "'");
      //add("&#039;", "'");
      add("&amp;", "&");
      add("&lt;", "<");
      add("&gt;", ">\000\000\000");
      add("&nbsp;", " \000\000\000\000");
      add("&ndash;", "-");
      add("&mdash;", "-");
      add("&ldquo;", "\"");
      add("&rdquo;", "\"");
      add("&lsquo;", "'");
      add("&rsquo;", "'");
   }
}
