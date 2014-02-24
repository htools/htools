package io.github.repir.tools.Lib;

import io.github.repir.tools.Content.BufferReaderWriter;
import io.github.repir.tools.DataTypes.Tuple2;
import io.github.repir.tools.Lib.Log;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import io.github.repir.tools.DataTypes.ByteArrayPos;

public class ByteTools {

   public static Log log = new Log(ByteTools.class);
   public static final boolean quotes[] = getQuotes();
   public static final byte lowercase[] = getLC();
   public static final boolean identifier[] = getIdentifier();
   public static final boolean whitespace[] = getByteArray(" \n\t\r");
   public static final byte sametext[] = getSameText();

   public static String toString(byte b) {
      return new StringBuilder().append((char) b).toString();
   }

   public static String toString(byte b[], int pos, int length) {
      char c[];
      int end = pos + length;
      int realchars = 0;
      for (int p = pos; p < end; p++) {
         if (b[p] != 0) {
            realchars++;
         }
      }
      if (realchars > 0) {
         c = new char[realchars];
         for (int cnr = 0, p = pos; p < end; p++) {
            if (b[p] > 0) {
               c[cnr++] = (char) b[p];
            } else if (b[p] < 0) {
               c[cnr++] = (char) (b[p] & 0xFF);
            }
         }
         return new String(c);
      }
      return "";
   }

   public static String toString(byte b[]) {
      return toString(b, 0, b.length);
   }
   
   public static String listBytesAsString(byte b[]) {
      StringBuilder sb = new StringBuilder();
      for (int i : b) {
         if (i > 0) {
            sb.append(i & 0xFF).append(" ");
         }
      }
      return sb.toString();
   }

   public static int bytesToInt(byte [] buffer, int pos) {
      int ch1 = buffer[pos] & 0xFF;
      int ch2 = buffer[pos+1] & 0xFF;
      int ch3 = buffer[pos+2] & 0xFF;
      int ch4 = buffer[pos+3] & 0xFF;
      return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
   }

   
   public static byte[] getSameText() {
      byte b[] = new byte[128];
      for (int i = 0; i < 32; i++) {
         b[i] = 32;
      }
      for (int i = 33; i < 128; i++) {
         b[i] = (i >= 'A' && i <= 'Z') ? (byte) (i + 32) : (byte) i;
      }
      return b;
   }

   private static boolean[] getQuotes() {
      boolean array[] = new boolean[128];
      for (int i = 0; i < 128; i++) {
         array[i] = (i == '"' || i == '\'');
      }
      return array;
   }

   private static boolean[] getIdentifier() {
      boolean array[] = new boolean[128];
      for (int i = 0; i < 128; i++) {
         array[i] = (i >= '0' && i <= '9') || (i >= 'A' && i <= 'Z')
                 || (i >= 'a' && i <= 'z') || i == '-' || i == '_';
      }
      return array;
   }

   private static byte[] getLC() {
      byte array[] = new byte[128];
      for (int i = 0; i < 128; i++) {
         if (i >= 'A' || i <= 'Z') {
            array[i] = (byte) (i + 32);
         } else if (i < 33 && i > 0) {
            array[i] = 32;
         } else {
            array[i] = (byte) i;
         }
      }
      return array;
   }

   public static ArrayList<Integer> matches(byte b[], byte eof[]) {
      ArrayList<Integer> al = new ArrayList<Integer>();
      for (int i = 0; i < b.length; i++) {
         if (io.github.repir.tools.Lib.ByteTools.matchString(b, eof, i)) {
            al.add(i);
         }
      }
      return al;
   }

   public static int skipIgnoreWS(byte[] haystack, byte[] needle, int startpos, int endpos, boolean ignorecase) {
      int match = 0;
      for (; startpos < endpos;) {
         //log.info("skipIgnoreWS() needle %s startpos %d endpos %d match %d",
         //        new String(needle), startpos, endpos, match);
         if (match == needle.length) {
            return startpos;
         } else if (needle[match] == 32) {
            for (; haystack[startpos] < 33; startpos++);
            match++;
         } else if (ignorecase) {
            if (haystack[startpos] < 0 || lowercase[haystack[startpos]] != lowercase[needle[match]]) {
               return -1;
            }
            match++;
            startpos++;
         } else {
            if (haystack[startpos] != needle[match]) {
               return -1;
            }
            match++;
            startpos++;
         }
      }
      return (match == needle.length) ? startpos : -1;
   }

   public static boolean matchStringWS(byte[] haystack, byte[] needle, int pos) {
      int match = 0;
      for (match = 0; match < needle.length && pos < haystack.length;) {
         if (whitespace[needle[match]] && haystack[pos] >= 0 && whitespace[haystack[pos]]) {
            pos++;
         } else if (whitespace[needle[match]]) {
            match++;
         } else if (haystack[pos] == needle[match]) {
            match++;
            pos++;
         } else {
            break;
         }
      }
      for (; match < needle.length && whitespace[needle[match]]; match++);
      return (match >= needle.length);
   }

   public static boolean matchString(byte[] haystack, byte[] needle, int pos) {
      int match = 0;
      for (match = 0; match < needle.length && pos < haystack.length && haystack[pos] == needle[match]; match++, pos++);
      return (match >= needle.length);
   }

   public static Tuple2<Integer, Integer> find(byte[] haystack, byte[] needlestart, byte[] needleend, int startpos, int endpos, boolean ignorecase, boolean omitquotes) {
      int needlepos = find(haystack, needlestart, startpos, endpos, ignorecase, false);
      if (needlepos > -1) {
         int needlepos2 = find(haystack, needleend, needlepos + needlestart.length, endpos, ignorecase, omitquotes);
         if (needlepos2 > -1) {
            return new Tuple2<Integer, Integer>(needlepos, needlepos2);
         }
      }
      return null;
   }

   public static int string0HashCode(byte buffer[], int pos, int bufferend) {
      int h = 0;
      for (int i = pos; i < bufferend && buffer[ i ] != 0; i++) {
         h = 31 * h + buffer[i];
      }
      return h;
   }
   public static int findEndQuote(byte[] haystack, int startpos, int endpos) {
      boolean escape = false;
      boolean checkedfake = false;
      int quote = haystack[startpos];
      if (!quotes[quote]) {
         return -1;
      }
      for (int p = startpos + 1; p < endpos; p++) {
         if (quote == haystack[p] && !escape) {
            return p;
         } else if (haystack[p] == '\\') {
            escape = !escape;
         } else {
            escape = false;
         }
         if ((haystack[p] == '>' || haystack[p] == '=') && !checkedfake) {
            checkedfake = true;
            int pp = startpos - 1;
            for (; pp > 0 && haystack[pp] < 33; pp--);
            if (pp < 0 || haystack[pp] != '=') {
               return startpos;
            }
            for (; pp > 0 && haystack[pp] < 33; pp--);
            int pp2 = pp;
            for (; pp > 0 && identifier[haystack[pp]]; pp--);
            if (pp2 == pp) {
               return startpos;
            }
         }
      }
      return -1;
   }

   public static int find(byte[] haystack, byte[] needle, int startpos, int endpos, boolean ignorecase, boolean omitquotes) {
      //log.info("find( %s %d )", new String(needle), startpos);
      int match = 0;
      boolean escape = false;
      for (int p = startpos; p < endpos; p++) {
         if (haystack[p] > 0) {
            //if (!omitquotes && haystack[p] == '\'')
            //   log.info("singlequote %s %s", new String(needle), new String(haystack, startpos, 100));
            if (omitquotes && quotes[haystack[p]]) {
               p = findEndQuote(haystack, p, endpos);
               if (p < 0) {
                  return -1;
               }
            } else if (ignorecase && lowercase[haystack[p]] == lowercase[needle[match]]) {
               if (++match == needle.length) {
                  return p - needle.length + 1;
               }
            } else if (!ignorecase && haystack[p] == needle[match]) {
               if (++match == needle.length) {
                  return p - needle.length + 1;
               }
            } else {
               match = 0;
            }
         } else {
            match = 0;
         }
      }
      return -1;
   }

   public static int find(byte haystack[], byte needle, int startpos, int endpos) {
      for (; startpos < endpos; startpos++) {
         if (haystack[startpos] == needle) {
            return startpos;
         }
      }
      return -1;
   }

   public static byte[] clone(byte[] haystack) {
      byte b[] = new byte[haystack.length];
      System.arraycopy(haystack, 0, b, 0, haystack.length);
      return b;
   }

   public static String extract(byte[] haystack, byte[] needle, byte[] needle2, int startpos, int endpos, boolean ignorecase, boolean omitquotes) {
      startpos = find(haystack, needle, startpos, endpos, ignorecase, omitquotes);
      if (startpos > -1) {
         startpos += needle.length;
         endpos = find(haystack, needle2, startpos, endpos, ignorecase, omitquotes);
         if (endpos > -1) {
            return new String(haystack, startpos, endpos - startpos);
         }
      }
      return "";
   }

   public static boolean[] getByteArray(String s) {
      boolean c[] = new boolean[128];
      Arrays.fill(c, false);
      byte bytes[] = s.getBytes();
      for (byte b : bytes) {
         if (b >= 0) {
            c[b] = true;
         }
      }
      return c;
   }

   public static byte[] concatenate(byte a[], byte b[]) {
      byte c[] = new byte[a.length + b.length];
      System.arraycopy(a, 0, c, 0, a.length);
      System.arraycopy(b, 0, c, a.length, b.length);
      return c;
   }

   public static int print(byte[] haystack, int startpos, String... strings) {
      for (String s : strings) {
         byte[] b = s.getBytes();
         for (int i = 0; i < b.length && startpos < haystack.length;) {
            haystack[ startpos++] = b[i++];
         }
      }
      return startpos;
   }

   public static boolean checkQuotes(byte b[], int start, int end) {
      boolean inSingle = false, inDouble = false;
      for (; start < end; start++) {
         switch (b[start]) {
            case '\\':
               start++;
               break;
            case '"':
               if (!inSingle) {
                  inDouble = !inDouble;
               }
               break;
            case '\'':
               if (!inDouble) {
                  inSingle = !inSingle;
               }
         }
      }
      return !(inSingle || inDouble);
   }
}
