package io.github.repir.tools.lib;

import static io.github.repir.tools.lib.Const.*;
import static io.github.repir.tools.lib.PrintTools.*;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 *
 * @author jeroen
 */
public enum StrTools {

    ;

    public static Log log = new Log(StrTools.class);

    /**
     *
     * @param properties
     * @return
     */
    public static HashMap<String, String> getProperties(String properties) {
        HashMap<String, String> ret = new HashMap<String, String>();
        if (properties != null && properties.length() > 0) {
            String[] property = properties.split("\\s+");
            for (String p : property) {
                String c[] = p.split("=");
                if (c.length == 2) {
                    ret.put(c[0], c[1]);
                }
            }
        }
        return ret;
    }

    /**
     *
     * @param object
     * @param preMatch
     * @param postMatch
     * @return
     */
    public static String getSubstring(String object, String preMatch, String postMatch) {
        int pre = object.indexOf(preMatch) + preMatch.length();
        if (pre > -1) {
            int post = object.indexOf(postMatch, pre);
            if (post > -1) {
                return object.substring(pre, post);
            }
        }
        return null;
    }

    public static String[] splitEscaped(String object, char c) {
        String result[] = new String[2];
        StringBuilder sb = new StringBuilder();
        int pos = 0;
        for (; pos < object.length(); pos++) {
            char p = object.charAt(pos);
            if (p == '\\') {
                sb.append(object.charAt(++pos));
            } else if (p == c) {
                break;
            } else {
                sb.append(p);
            }
        }
        result[0] = sb.toString();
        sb = new StringBuilder();
        for (pos++; pos < object.length(); pos++) {
            char p = object.charAt(pos);
            if (p == '\\') {
                sb.append(object.charAt(++pos));
            } else {
                sb.append(p);
            }
        }
        result[1] = sb.toString();
        return result;
    }

    public static String coalesce(String... s) {
        for (String t : s) {
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public static String getToString(String object, String match) {
        int pos = object.indexOf(match);
        return (pos < 1) ? "" : object.substring(0, pos);
    }

    public static String getFromString(String object, String match) {
        int pos = object.indexOf(match);
        return (pos < 1) ? "" : object.substring(pos + match.length());
    }

    public static String removeOptionalStart(String object, String optionalstart) {
        return (object.startsWith(optionalstart)) ? object.substring(optionalstart.length()) : object;
    }

    /**
     *
     * @param s
     * @return
     */
    public static int strToInt(String s) {
        if (s == null || s.length() == 0) {
            return NULLINT;
        }
        return Integer.parseInt(s);
    }

    /**
     *
     * @param text
     * @param search
     * @return
     */
    public static int countIndexOf(String text, String search) {
        return countIndexOf(text, search, 0, text.length());
    }

    /**
     *
     * @param text
     * @param search
     * @param start
     * @param end
     * @return
     */
    public static int countIndexOf(String text, String search, int start, int end) {
        int count = 0;
        for (int fromIndex = start; fromIndex > -1 && fromIndex <= end; count++) {
            fromIndex = text.indexOf(search, fromIndex + ((count > 0) ? search.length() : 0));
        }
        return count - 1;
    }

    /**
     *
     * @param text
     * @param search
     * @return
     */
    public static int countIndexOf(String text, char search) {
        return countIndexOf(text, search, 0, text.length());
    }

    /**
     *
     * @param text
     * @param search
     * @param start
     * @param end
     * @return
     */
    public static int countIndexOf(String text, char search, int start, int end) {
        int count = 0;
        for (int fromIndex = start; fromIndex > -1 && fromIndex < end; count++) {
            fromIndex = text.indexOf(search, fromIndex + ((count > 0) ? 1 : 0));
        }
        return count - 1;
    }

    /**
     *
     * @param string
     * @return
     */
    public static String toEscapeString(String string) {
        string = string.replaceAll("[']", "''");
        string = string.replaceAll("[%]", "%%");
        return string;
    }
    static char exceptnl = '\n';
    static String needles = ".?!";

    /**
     *
     * @param haystack
     * @return
     */
    public static String stripAllUntil(String haystack) {
        return stripUntil(haystack, needles, exceptnl);
    }

    /**
     *
     * @param haystack
     * @return
     */
    public static String stripAllFrom(String haystack) {
        return stripFrom(haystack, needles, exceptnl);
    }

    /**
     *
     * @param haystack
     * @param needles
     * @param notallowed
     * @return
     */
    public static String stripUntil(String haystack, String needles, char notallowed) {
        int pos;
        for (pos = 0; (pos < haystack.length() && needles.indexOf(haystack.charAt(pos)) == -1)
                || (pos < haystack.length() - 1 && !Character.isWhitespace(haystack.charAt(pos + 1))); pos++) {
            if (haystack.charAt(pos) == '\n') {
                break;
            }
        }
        if (pos < haystack.length() && needles.indexOf(haystack.charAt(pos)) > -1) {
            return haystack.substring(pos + 1).trim();
        }
        return haystack;
    }

    public static String stripUntilLast(String haystack, String needles) {
        int pos = haystack.lastIndexOf(needles);
        return (pos < 0) ? haystack : haystack.substring(pos + needles.length());
    }

    /**
     *
     * @param haystack
     * @param needles
     * @param notallowed
     * @return
     */
    public static String stripFrom(String haystack, String needles, char notallowed) {
        int pos2, pos = 0;
        for (char end : needles.toCharArray()) {
            int p = haystack.lastIndexOf(end);
            if (p + 1 == haystack.length() || Character.isWhitespace(haystack.charAt(p + 1))) {
                pos = java.lang.Math.max(pos, p);
            }
        }
        if (pos == 0) {
            return haystack;
        }
        for (pos2 = pos + 1; pos2 < haystack.length() && haystack.charAt(pos2) != notallowed; pos2++) {
        }
        //printf("pos %d %d\n", pos, pos2);
        if (pos2 == haystack.length()) {
            return haystack.substring(0, pos + 1).trim();
        }
        return haystack;
    }

    public static String[] split(String object, String regex) {
        String r[] = object.split(regex);
        ArrayList<String> rl = new ArrayList<String>();
        for (String s : r) {
            s = s.trim();
            if (s.length() > 0) {
                rl.add(s);
            }
        }
        return rl.toArray(new String[rl.size()]);
    }

    /**
     *
     * @param haystack
     * @param delimiter
     * @param openquote
     * @param closequote
     * @return
     */
    public static TreeMap<String, String> Split(String haystack, char delimiter, char openquote, char closequote) {
        TreeMap<String, String> r = new TreeMap<String, String>();
        //printf("%s\n", haystack);
        int dpos = -1;
        int pos = 0;
        do {
            dpos = haystack.indexOf(delimiter, dpos + 1);
            if (dpos == -1) {
                dpos = haystack.length();
            }
            int opos = haystack.indexOf(openquote, pos);
            int cpos = haystack.indexOf(closequote, opos);
            int brackets = StrTools.countIndexOf(haystack, openquote, pos, dpos) - StrTools.countIndexOf(haystack, closequote, pos, dpos);
            if (cpos == -1 || dpos < opos || brackets == 0) {
                //printf("%d %d %d %d\n", pos, opos, cpos, dpos);
                int is = haystack.indexOf('=', pos);
                String key = (is < dpos) ? haystack.substring(pos, is).trim() : "";
                String value = (is < dpos) ? haystack.substring(is + 1, dpos).trim()
                        : haystack.substring(pos, dpos).trim();
                if (value.charAt(0) == openquote && value.charAt(value.length() - 1) == closequote) {
                    value = value.substring(1, value.length() - 1);
                }
                r.put(key, value);
                //printf("%s=%s\n", key, value);
                pos = dpos + 1;
            }
        } while (dpos < haystack.length());
        return r;
    }

    /**
     * @param str array of Strings to be concatenated
     * @return Concatenated array of Strings
     */
    public static String concat(String... str) {
        StringBuilder sb = new StringBuilder();
        if (str != null) {
            for (String s : str) {
                sb.append(s);
            }
        }
        return sb.toString();
    }

    public static String concat(String str, int count) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(str);
        }
        return sb.toString();
    }

    /**
     * @param str array of Strings to be concatenated
     * @return Concatenated array of Strings
     */
    public static String concat(Iterable<String> str) {
        StringBuilder sb = new StringBuilder();
        for (String s : str) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * @param seperator char to place in between consecutive array elements
     * @param str array of Strings to be concatenated
     * @return Concatenated array of Strings, separated by the separator
     */
    public static String concat(char seperator, Iterable<String> str) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : str) {
            if (start) {
                start = false;
            } else {
                sb.append(seperator);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public static ArrayList<Integer> getIntList(String strlist) {
        ArrayList<Integer> result = new ArrayList();
        String[] split = strlist.split(",\\s*");
        for (String s : split) {
            if (s.length() > 0) {
                try {
                    result.add(Integer.parseInt(s));
                } catch (NumberFormatException e) {}
            }
        }
        return result;
    }
    
    /**
     * @param seperator char to place in between consecutive array elements
     * @param str array of Strings to be concatenated
     * @return Concatenated array of Strings, separated by the separator;
     */
    public static String concat(char seperator, String... str) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : str) {
            if (start) {
                start = false;
            } else {
                sb.append(seperator);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * @param seperator String to place in between consecutive array elements
     * @param str array of Strings to be concatenated
     * @return Concatenated array of Strings, separated by the separator;
     */
    public static String concat(String seperator, String... str) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for (String s : str) {
            if (start) {
                start = false;
            } else {
                sb.append(seperator);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     *
     * @param args
     */
    private void createC3C5() throws UnsupportedEncodingException {
        for (byte a = (byte) 0xC3;; a = (byte) 0xC5) {
            String s = sprintf(" byte unicodebyte%s[] = { ", Integer.toHexString(a & 0xFF).toUpperCase());
            String t = sprintf(" byte asciibyte%s[] = { ", Integer.toHexString(a & 0xFF).toUpperCase());
            for (int i = 0; i < UNICODE.length(); i++) {
                String c = UNICODE.substring(i, i + 1);
                byte b[] = c.getBytes("UTF-8");
                if (b[0] == a) {
                    s += sprintf("0x%s, ", Integer.toHexString(b[1] & 0xFF).toUpperCase());
                    t += sprintf("'%s', ", PLAIN_ASCII.charAt(i));
                }
            }
            s = s.substring(0, s.length() - 2) + "};";
            t = t.substring(0, t.length() - 2) + "};";
            printf("%s\n", s);
            printf("%s\n", t);
            if (a == (byte) 0xC5) {
                break;
            }
        }
    }

    public static boolean compare(String[] s1, String s2[]) {
        boolean comp = s1.length == s2.length;
        if (comp) {
            for (int i = 0; i < s1.length && comp; i++) {
                int match = 0;
                for (; match < s2.length && !s1[i].equals(s2[match]); match++);
                if (match == s2.length) {
                    comp = false;
                }
            }
        }
        return comp;
    }

    public static boolean elementOf(String[] s1, String s2) {
        if (s1 == null) {
            return false;
        }
        boolean comp = false;
        for (int i = 0; i < s1.length && !comp; i++) {
            if (s1[i].equals(s2)) {
                comp = true;
            }
        }
        return comp;
    }

    public static String[] addUnique(String[] s1, String s2) {
        if (elementOf(s1, s2)) {
            return s1;
        }
        String s3[] = new String[((s1 != null) ? s1.length : 0) + 1];
        for (int i = 0; i < s3.length - 1; i++) {
            s3[i] = s1[i];
        }
        s3[s3.length - 1] = s2;
        return s3;
    }

    /**
     *
     * @param html
     * @return
     */
    public static String html2String(String html) {
        String result = html.replaceAll("[\\s][\\s]+", " ");
        result = html.replaceAll("[\\n\\r]", " ");
        result = result.replaceAll("<[^>]*[>]", "");
        return result;
    }

    public static String soundex(String s) {
        char[] x = s.toUpperCase().toCharArray();
        char firstLetter = x[0];

        // convert letters to numeric code
        for (int i = 0; i < x.length; i++) {
            switch (x[i]) {
                case 'B':
                case 'F':
                case 'P':
                case 'V': {
                    x[i] = '1';
                    break;
                }

                case 'C':
                case 'G':
                case 'J':
                case 'K':
                case 'Q':
                case 'S':
                case 'X':
                case 'Z': {
                    x[i] = '2';
                    break;
                }

                case 'D':
                case 'T': {
                    x[i] = '3';
                    break;
                }

                case 'L': {
                    x[i] = '4';
                    break;
                }

                case 'M':
                case 'N': {
                    x[i] = '5';
                    break;
                }

                case 'R': {
                    x[i] = '6';
                    break;
                }

                default: {
                    x[i] = '0';
                    break;
                }
            }
        }

        // remove duplicates
        StringBuilder output = new StringBuilder();
        output.append(firstLetter);
        for (int i = 1; i < x.length; i++) {
            if (x[i] != x[i - 1] && x[i] != '0') {
                output.append(x[i]);
            }
        }

        // pad with 0's or truncate
        //output.append("0000");
        return output.toString();
    }

    public static int computeLevenshteinDistance(CharSequence str1,
            CharSequence str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];
        for (int i = 0; i <= str1.length(); i++) {
            distance[i][0] = i;
        }
        for (int j = 1; j <= str2.length(); j++) {
            distance[0][j] = j;
        }
        for (int i = 1; i <= str1.length(); i++) {
            for (int j = 1; j <= str2.length(); j++) {
                distance[i][j] = IntTools.min3(
                        distance[i - 1][j] + 1,
                        distance[i][j - 1] + 1,
                        distance[i - 1][j - 1]
                        + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                : 1));
            }
        }
        return distance[str1.length()][str2.length()];
    }

    /**
     *
     * @param html
     * @return
     */
    public static String html2nlString(String html) {
        String result = html.replaceAll("[\\s][\\s]+", " ");
        result = html.replaceAll("[\\n\\r]", " ");
        result = result.replaceAll("<br(?=[\\s>])[^>]*[>]", "\n");
        result = result.replaceAll("<p(?=[\\s>])[^>]*[>]", "\n");
        result = result.replaceAll("<tr(?=[\\s>])[^>]*[>]", "\n");
        result = result.replaceAll("<[^>]*[>]", "");
        return result;
    }
    private static final String PLAIN_ASCII
            = "AaEeIiOoUu" // grave
            + "AaEeIiOoUuYy" // acute
            + "AaEeIiOoUu" // circumflex
            + "AaOoNn" // tilde
            + "AaEeIiOoUuy" // umlaut
            + "Aa" // ring
            + "Cc" // cedilla
            + "YyYOoUu" // double acute
            ;
    private static final String UNICODE
            = "\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9"
            + "\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD"
            + "\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB"
            + "\u00C3\u00E3\u00D5\u00F5\u00D1\u00F1"
            + "\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u00FF"
            + "\u00C5\u00E5"
            + "\u00C7\u00E7"
            + "\u0176\u0177\u0178\u0150\u0151\u0170\u0171";

    // remove accentued from a string and replace with ascii equivalent
    public static String convertNonAscii(String s) {
        if (s == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        int n = s.length();
        for (int i = 0; i < n; i++) {
            char c = s.charAt(i);
            int pos = UNICODE.indexOf(c);
            if (pos > -1) {
                sb.append(PLAIN_ASCII.charAt(pos));
            } else if (c >= '0' && c <= 'Z') {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    public static byte unicodebyteC3[] = {(byte) 0x80, (byte) 0xA0, (byte) 0x88, (byte) 0xA8, (byte) 0x8C,
        (byte) 0xAC, (byte) 0x92, (byte) 0xB2, (byte) 0x99, (byte) 0xB9,
        (byte) 0x81, (byte) 0xA1, (byte) 0x89, (byte) 0xA9, (byte) 0x8D,
        (byte) 0xAD, (byte) 0x93, (byte) 0xB3, (byte) 0x9A, (byte) 0xBA,
        (byte) 0x9D, (byte) 0xBD, (byte) 0x82, (byte) 0xA2, (byte) 0x8A,
        (byte) 0xAA, (byte) 0x8E, (byte) 0xAE, (byte) 0x94, (byte) 0xB4,
        (byte) 0x9B, (byte) 0xBB, (byte) 0x83, (byte) 0xA3, (byte) 0x95,
        (byte) 0xB5, (byte) 0x91, (byte) 0xB1, (byte) 0x84, (byte) 0xA4,
        (byte) 0x8B, (byte) 0xAB, (byte) 0x8F, (byte) 0xAF, (byte) 0x96,
        (byte) 0xB6, (byte) 0x9C, (byte) 0xBC, (byte) 0xBF, (byte) 0x85,
        (byte) 0xA5, (byte) 0x87, (byte) 0xA7};
    public static byte asciibyteC3[] = {'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'Y', 'y', 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'A', 'a', 'O', 'o', 'N', 'n', 'A', 'a', 'E', 'e', 'I', 'i', 'O', 'o', 'U', 'u', 'y', 'A', 'a', 'C', 'c'};
    public static byte unicodebyteC5[] = {(byte) 0xB6, (byte) 0xB7, (byte) 0xB8, (byte) 0x90, (byte) 0x91, (byte) 0xB0, (byte) 0xB1};
    public static byte asciibyteC5[] = {'Y', 'y', 'Y', 'O', 'o', 'U', 'u'};
    public static byte asciiextendedbyte[] = {(byte) 0xC0, (byte) 0xC1, (byte) 0xC2, (byte) 0xC3, (byte) 0xC4,
        (byte) 0xC7,
        (byte) 0xC8, (byte) 0xC9, (byte) 0xCA, (byte) 0xCB,
        (byte) 0xCC, (byte) 0xCD, (byte) 0xCE, (byte) 0xCF,
        (byte) 0xD1,
        (byte) 0xD2, (byte) 0xD3, (byte) 0xD4, (byte) 0xD5, (byte) 0xD6,
        (byte) 0xD9, (byte) 0xDA, (byte) 0xDB, (byte) 0xDC,
        (byte) 0xDD,
        (byte) 0xE0, (byte) 0xE1, (byte) 0xE2, (byte) 0xE3, (byte) 0xE4, (byte) 0xE5,
        (byte) 0xE7,
        (byte) 0xE8, (byte) 0xE9, (byte) 0xEA, (byte) 0xEB,
        (byte) 0xEC, (byte) 0xED, (byte) 0xEE, (byte) 0xEF,
        (byte) 0xF1,
        (byte) 0xF2, (byte) 0xF3, (byte) 0xF4, (byte) 0xF5, (byte) 0xF6,
        (byte) 0xF9, (byte) 0xFA, (byte) 0xFB, (byte) 0xFC,
        (byte) 0xFD, (byte) 0xFF};
    public static byte asciibyte[] = {'A', 'A', 'A', 'A', 'A',
        'C',
        'E', 'E', 'E', 'E',
        'I', 'I', 'I', 'I',
        'N',
        'O', 'O', 'O', 'O', 'O',
        'U', 'U', 'U', 'U',
        'Y',
        'a', 'a', 'a', 'a', 'a', 'a',
        'c',
        'e', 'e', 'e', 'e',
        'i', 'i', 'i', 'i',
        'n',
        'o', 'o', 'o', 'o', 'o',
        'u', 'u', 'u', 'u',
        'y', 'y'};
}
