package io.github.repir.tools.extract.modules;

import io.github.repir.tools.extract.Extractor;
import io.github.repir.tools.lib.Log;

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
      add("&quot;", "\"");
      add("&#039;", "'");
      add("&amp;", "&");
      add("&lt;", "<");
      add("&gt;", ">\000\000\000");
      add("&nbsp;", " \000\000\000\000\000");
      add("&#160;", " \000\000\000\000\000");
      add("&ndash;", "-");
      add("&Agrave;", "A");
      add("&Aacute;", "A");
      add("&Acirc;", "A");
      add("&Atilde;", "A");
      add("&Auml;", "A");
      add("&Aring;", "A");
      add("&AElig;", "AE");
      add("&Ccedil;", "C");
      add("&Egrave;", "E");
      add("&Eacute;", "E");
      add("&Ecirc;", "E");
      add("&Euml;", "E");
      add("&Igrave;", "I");
      add("&Iacute;", "I");
      add("&Icirc;", "I");
      add("&Iuml;", "I");
      add("&Ntilde;", "N");
      add("&Ograve;", "O");
      add("&Oacute;", "O");
      add("&Ocirc;", "O");
      add("&Otilde;", "O");
      add("&Ouml;", "O");
      add("&Ugrave;", "U");
      add("&Uacute;", "U");
      add("&Ucirc;", "U");
      add("&Uuml;", "U");
      add("&Yacute;", "Y");
      add("&agrave;", "a");
      add("&aacute;", "a");
      add("&acirc;", "a");
      add("&atilde;", "a");
      add("&auml;", "a");
      add("&aring;", "a");
      add("&aelig;", "ae");
      add("&ccedil;", "c");
      add("&egrave;", "e");
      add("&eacute;", "e");
      add("&ecirc;", "e");
      add("&euml;", "e");
      add("&igrave;", "i");
      add("&iacute;", "i");
      add("&icirc;", "i");
      add("&iuml;", "i");
      add("&ntilde;", "n");
      add("&ograve;", "o");
      add("&oacute;", "o");
      add("&ocirc;", "o");
      add("&otilde;", "o");
      add("&ouml;", "o");
      add("&ugrave;", "u");
      add("&uacute;", "u");
      add("&ucirc;", "u");
      add("&uuml;", "u");
      add("&yacute;", "y");
      add("&mdash;", "-");
      add("&ldquo;", "\"");
      add("&quot;", "\"");
      add("&rdquo;", "\"");
      add("&lsquo;", "'");
      add("&rsquo;", "'");
      add("&#8220;", "\"");
      add("&#8221;", "\"");
      add("&#8222;", "\"");
      add("&#8216;", "\'");
      add("&#8217;", "\'");
      add("&#8218;", "\'");
      add("&#8211;", "-");
      add("&#8212;", "-");
      add("&#180;", "'");
      add("&#96;", "'");
   }
}
