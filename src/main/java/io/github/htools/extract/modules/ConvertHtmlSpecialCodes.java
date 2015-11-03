package io.github.htools.extract.modules;

import io.github.htools.collection.ArrayMap;
import io.github.htools.extract.Extractor;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;

/**
 * Replaces certain HTML special codes with a byte equivalent. Using Translator
 * the replacements must have the same byte length. If the byte length of the replacement
 * is smaller than the search key, the replacement is pre padded with \0 bytes
 * which are ignored during extraction.
 * <p>
 * @author jeroen
 */
public class ConvertHtmlSpecialCodes extends Translator {

   public static Log log = new Log(ConvertHtmlSpecialCodes.class);
   
   
   public ConvertHtmlSpecialCodes(Extractor extractor, String process) {
      super(extractor, process);
   }
   
   protected ArrayMap<byte[], byte[]> initSearchReplace() {
       ArrayMap<byte[], byte[]> sr = new ArrayMap();
      add(sr, "&quot;", "\"");
      add(sr, "&#039;", "'");
      add(sr, "&amp;", "&");
      add(sr, "&lt;", "<");
      add(sr, "&gt;", ">\000\000\000");
      add(sr, "&nbsp;", " \000\000\000\000\000");
      add(sr, "&#160;", " \000\000\000\000\000");
      add(sr, "&ndash;", "-");
      add(sr, "&Agrave;", "A");
      add(sr, "&Aacute;", "A");
      add(sr, "&Acirc;", "A");
      add(sr, "&Atilde;", "A");
      add(sr, "&Auml;", "A");
      add(sr, "&Aring;", "A");
      add(sr, "&AElig;", "AE");
      add(sr, "&Ccedil;", "C");
      add(sr, "&Egrave;", "E");
      add(sr, "&Eacute;", "E");
      add(sr, "&Ecirc;", "E");
      add(sr, "&Euml;", "E");
      add(sr, "&Igrave;", "I");
      add(sr, "&Iacute;", "I");
      add(sr, "&Icirc;", "I");
      add(sr, "&Iuml;", "I");
      add(sr, "&Ntilde;", "N");
      add(sr, "&Ograve;", "O");
      add(sr, "&Oacute;", "O");
      add(sr, "&Ocirc;", "O");
      add(sr, "&Otilde;", "O");
      add(sr, "&Ouml;", "O");
      add(sr, "&Ugrave;", "U");
      add(sr, "&Uacute;", "U");
      add(sr, "&Ucirc;", "U");
      add(sr, "&Uuml;", "U");
      add(sr, "&Yacute;", "Y");
      add(sr, "&agrave;", "a");
      add(sr, "&aacute;", "a");
      add(sr, "&acirc;", "a");
      add(sr, "&atilde;", "a");
      add(sr, "&auml;", "a");
      add(sr, "&aring;", "a");
      add(sr, "&aelig;", "ae");
      add(sr, "&ccedil;", "c");
      add(sr, "&egrave;", "e");
      add(sr, "&eacute;", "e");
      add(sr, "&ecirc;", "e");
      add(sr, "&euml;", "e");
      add(sr, "&igrave;", "i");
      add(sr, "&iacute;", "i");
      add(sr, "&icirc;", "i");
      add(sr, "&iuml;", "i");
      add(sr, "&ntilde;", "n");
      add(sr, "&ograve;", "o");
      add(sr, "&oacute;", "o");
      add(sr, "&ocirc;", "o");
      add(sr, "&otilde;", "o");
      add(sr, "&ouml;", "o");
      add(sr, "&ugrave;", "u");
      add(sr, "&uacute;", "u");
      add(sr, "&ucirc;", "u");
      add(sr, "&uuml;", "u");
      add(sr, "&yacute;", "y");
      add(sr, "&mdash;", "-");
      add(sr, "&ldquo;", "\"");
      add(sr, "&quot;", "\"");
      add(sr, "&rdquo;", "\"");
      add(sr, "&lsquo;", "'");
      add(sr, "&rsquo;", "'");
      add(sr, "&#8220;", "\"");
      add(sr, "&#8221;", "\"");
      add(sr, "&#8222;", "\"");
      add(sr, "&#8216;", "\'");
      add(sr, "&#8217;", "\'");
      add(sr, "&#8218;", "\'");
      add(sr, "&#8211;", "-");
      add(sr, "&#8212;", "-");
      add(sr, "&#180;", "'");
      add(sr, "&#96;", "'");
      return sr;
   }
   
   private void add(ArrayMap<byte[], byte[]> searchReplace, String search, String replace) {
       searchReplace.add(ByteTools.toBytes(search), ByteTools.toBytes(replace));
   }
}
