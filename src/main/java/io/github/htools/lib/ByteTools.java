package io.github.htools.lib;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public enum ByteTools {

    ;

   public static Log log = new Log(ByteTools.class);
    public static final byte[] EMPTYARRAY = new byte[0];
    private static final boolean quotes[] = getQuotes();
    private static final byte lowercase[] = getLC();
    private static final boolean identifier[] = getIdentifier();
    private static final boolean whitespace[] = getByteArray(" \n\t\r");
    private static final boolean whitespacezero[] = getByteArray(" \n\t\r\0");

    /**
     * returns a String constructed with the content that is marked by start
     * (inclusive) and end (exclusive) in the byte array, omitting \0 bytes.
     */
    public static String toString(byte array[], int start, int end) {
       try {
           byte[] not0 = toBytesShallow(array, start, end);
           return new String(not0, "UTF-8");
       } catch (UnsupportedEncodingException ex) {
           log.fatalexception(ex, "toString()");
           return null;
       }
    }

    private static int countnot0(byte array[], int start, int end) {
        int realchars = 0;
        for (int p = start; p < end; p++) {
            if (array[p] != 0) {
                realchars++;
            }
        }
        return realchars;
    }

    /**
     * @param array
     * @param start
     * @param end
     * @return the a copy of the array between start and end, omitting any zero
     * bytes.
     */
    public static byte[] toBytes(byte array[], int start, int end) {
        int bytes = countnot0(array, start, end);
        byte[] c = new byte[bytes];
        if (bytes > 0) {
            for (int cnr = 0, p = start; p < end; p++) {
                if (array[p] != 0) {
                    c[cnr++] = array[p];
                }
            }
        }
        return c;
    }

    /**
     * @param array
     * @param start
     * @param end
     * @return the a copy of the array between start and end, omitting any zero
     * bytes.
     */
    public static byte[] toBytesShallow(byte array[], int start, int end) {
        int realchars = countnot0(array, start, end);
        if (realchars > 0 || start != 0 || end != array.length) {
            byte[] c = new byte[realchars];
            for (int cnr = 0, p = start; p < end; p++) {
                if (array[p] != 0) {
                    c[cnr++] = array[p];
                }
            }
            return c;
        }
        return array;
    }

    public static String toTrimmedString(byte b[], int pos, int end) {
       try {
           return new String(toTrimmed(b, pos, end), "UTF-8");
       } catch (UnsupportedEncodingException ex) {
           ex.printStackTrace();
           return null;
       }
    }

    public static byte[] toTrimmed(byte b[], int pos, int end) {
        byte c[];
        for (; pos < end && whitespacezero[b[pos] & 0xFF]; pos++);
        for (; end > pos && whitespacezero[b[end - 1] & 0xFF]; end--);
        int realchars = countnot0(b, pos, end);
        if (realchars > 0) {
            c = new byte[realchars];
            for (int cnr = 0, p = pos; p < end; p++) {
                if (b[p] != 0) {
                    c[cnr++] = b[p];
                }
            }
            return c;
        }
        return EMPTYARRAY;
    }

    /**
     * @param b
     * @param pos
     * @param end
     * @return
     */
    public static String toFullTrimmedString(byte b[], int pos, int end) {
       try {
           return new String(toFullTrimmed(b, pos, end), "UTF-8");
       } catch (UnsupportedEncodingException ex) {
           ex.printStackTrace();
           return null;
       }
    }

    public static String toFullTrimmedString(byte b[]) {
       return toFullTrimmedString(b, 0, b.length);
    }

    /**
     * @param b
     * @param pos
     * @param end
     * @return a byte array from which \0 bytes 
     */
    public static byte[] toFullTrimmed(byte b[], int pos, int end) {
        byte c[];
        for (; pos < end && whitespacezero[b[pos] & 0xFF]; pos++);
        for (; end > pos && whitespacezero[b[end - 1] & 0xFF]; end--);
        int realchars = 0;
        for (int p = pos; p < end; p++) {
            if (whitespace[b[p] & 0xFF]) {
                realchars++;
                for (; p + 1 < end && whitespacezero[b[p + 1] & 0xFF]; p++);
            } else if (b[p] != 0) {
                realchars++;
            }
        }
        if (realchars > 0) {
            c = new byte[realchars];
            for (int cnr = 0, p = pos; p < end; p++) {
                if (b[p] != 0) {
                    if (whitespace[b[p] & 0xFF]) {
                        c[cnr++] = 32;
                        for (; p + 1 < end && whitespacezero[b[p + 1] & 0xFF]; p++);
                    } else {
                        c[cnr++] = b[p];
                    }
                }
            }
            return c;
        }
        return EMPTYARRAY;
    }

    public static byte[] toFullTrimmed(byte[] content) {
        return toFullTrimmed(content, 0, content.length);
    }
    
    public static String toString(byte b[]) {
        return toString(b, 0, b.length);
    }

    /**
     * Converts a string to a UTF-8 byte array.
     *
     * @param s string
     * @return the byte array
     */
    public static byte[] toBytes(String s) {
       try {
           return s.getBytes("UTF-8");
       } catch (UnsupportedEncodingException ex) {
           ex.printStackTrace();
           return null;
       }
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

    public static int bytesToInt(byte[] buffer, int pos) {
        int ch1 = buffer[pos] & 0xFF;
        int ch2 = buffer[pos + 1] & 0xFF;
        int ch3 = buffer[pos + 2] & 0xFF;
        int ch4 = buffer[pos + 3] & 0xFF;
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4));
    }

    public static byte[] toLowerCase() {
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
        byte array[] = new byte[256];
        for (int i = 0; i < 256; i++) {
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
            if (io.github.htools.lib.ByteTools.matchString(b, eof, i)) {
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

    public static int string0HashCode(byte buffer[], int pos, int bufferend) {
        int h = 0;
        for (int i = pos; i < bufferend && buffer[i] != 0; i++) {
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

    public static int findQuoteSafe(byte haystack[], byte needle[], int start, int end) {
        boolean inSingle = false;
        boolean inDouble = false;
        int nextcheckquotes = start;
        int match = 0;
        for (int i = start; i < end; i++) {
            if (nextcheckquotes == i) {
                nextcheckquotes = i + 1;
                switch (haystack[i]) {
                    case '\\':
                        nextcheckquotes = i + 2;
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
                        break;
                }
            }

            if (!(inSingle || inDouble) && haystack[i] == needle[i + match]) {
                if (++match == needle.length) {
                    return i - needle.length + 1;
                }
            } else {
                match = 0;
            }
        }
        return -1;
    }

    public static int findQuoteSafe(byte haystack[], byte needle, int start, int end) {
        boolean inSingle = false;
        boolean inDouble = false;
        int nextcheckquotes = start;
        for (int i = start; i < end; i++) {
            if (nextcheckquotes == i) {
                nextcheckquotes = i + 1;
                switch (haystack[i]) {
                    case '\\':
                        nextcheckquotes = i + 2;
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
                        break;
                }
            }
            //log.info("pos %d %s %s %b %b", i, needle, haystack[i], inSingle, inDouble);
            if (!(inSingle || inDouble) && haystack[i] == needle) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
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
                return ByteTools.toString(haystack, startpos, endpos);
            }
        }
        return "";
    }

    public static boolean[] getByteArray(String s) {
        boolean c[] = new boolean[256];
        Arrays.fill(c, false);
        byte bytes[] = ByteTools.toBytes(s);
        for (byte b : bytes) {
            if (b >= 0) {
                c[b] = true;
            }
        }
        return c;
    }

    public static byte[] concatenate(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }
        byte c[] = new byte[length];
        for (int pos = 0, i = 0; i < arrays.length; i++) {
            System.arraycopy(arrays[i], 0, c, pos, arrays[i].length);
            pos += arrays[i].length;
        }
        return c;
    }

    public static int print(byte[] haystack, int startpos, String... strings) {
        for (String s : strings) {
            byte[] b = ByteTools.toBytes(s);
            for (int i = 0; i < b.length && startpos < haystack.length;) {
                haystack[startpos++] = b[i++];
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

    /**
     * Compares the byte contents in two byte arrays, until the first \0 byte,
     * therefore suitable to compare \0 terminated Strings. Similar to Hadoop's
     * WritableComparator.
     */
    public static int compareBytes0(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        int end1 = s1 + l1;
        int end2 = s2 + l2;
        for (; s1 < end1 && b1[s1] != 0 && s2 < end2; s1++, s2++) {
            int a = (b1[s1] & 0xff);
            int b = (b2[s2] & 0xff);
            if (a != b) {
                return a - b;
            }
        }
        return 0;
    }

    public static byte[] toArray(ByteBuffer buffer) {
        byte array[] = new byte[buffer.remaining()];
        buffer.get(array);
        return array;
    }

    public static boolean isWhiteSpaceZero(byte b) {
        return whitespacezero[b & 0xff];
    }

    public static boolean isWhiteSpace(byte b) {
        return whitespace[b & 0xff];
    }

    public static boolean isQuote(byte b) {
        return quotes[b & 0xff];
    }

    public static boolean isIdentifier(byte b) {
        return quotes[b & 0xff];
    }
}
