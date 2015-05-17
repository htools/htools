package io.github.repir.tools.io.web;

import io.github.repir.tools.lib.ArrayTools;
import io.github.repir.tools.lib.Log;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import javax.ws.rs.core.UriBuilder;

/**
 *
 * @author jeroen
 */
public enum UrlStrTools {

    ;
    public static final Log log = new Log(UrlStrTools.class);
    private static final Pattern splitdir = Pattern.compile("\\/");
    private static final Pattern spliturl = Pattern.compile("(%..)|[\\-\\_\\+\\=]");

    public static String extractMaxSection(String url) {
        url = stripExtension(stripQuery(stripHost(url)));
        String sections[] = splitdir.split(stripHost(url));
        String maxsection = "";
        for (String s : sections) {
            if (s.length() > maxsection.length()) {
                maxsection = s;
            }
        }
        return maxsection;
    }

    public static String[] extractWords(String maxsection) {
        String parts[] = spliturl.split(extractMaxSection(maxsection));
        int end = parts.length;
        if (end > 1 && parts[end - 2].length() == 0) {
            return (String[]) ArrayTools.subArray(parts, 0, end - 2);
        }
        if (end > 0 && parts[end - 1].length() > 4 && parts[end - 1].matches("[a-z]?[0-9]+")) {
            return (String[]) ArrayTools.subArray(parts, 0, end - 1);
        }
        return parts;
    }

    public static String stripHost(String url) {
        int i = url.indexOf(".");
        if (i > 0) {
            i = url.indexOf("/", i);
            if (i > 0) {
                return url.substring(i + 1);
            }
        }
        return url;
    }

    public static String host(String url) {
        int i = url.indexOf(".");
        if (i > 0) {
            i = url.indexOf("/", i);
            if (i > 0) {
                return url.substring(0, i);
            }
        }
        return "";
    }

    public static String folder(String url) {
        int i = url.lastIndexOf("/");
        if (i > 0) {
            return url.substring(0, i);
        }
        return "";
    }

    public static String stripMethod(String url) {
        int i = url.indexOf("://");
        if (i > 0) {
            return url.substring(i + 3);
        }
        return url;
    }

    public static String domain(String url) {
        int i = url.indexOf("://");
        if (i > 0) {
            i = url.indexOf(".", i+4);
            if (i > 0) {
                int j = url.indexOf("/", i);
                if (j > 0)
                    return url.substring(0, j);
            }
        }
        return url;
    }

    public static String stripExtension(String url) {
        int i = url.lastIndexOf(".");
        if (i < 0) {
            return url;
        }
        for (int j = url.length() - 1; j > i; j--) {
            if (!Character.isLetterOrDigit(url.charAt(j))) {
                return url;
            }
        }
        return url.substring(0, i);
    }

    private static String stripQuery(String url) {
        int i = url.indexOf("?");
        return (i > -1) ? url.substring(0, i) : url;
    }

    public final static String ESCAPE_CHARS = "<>&\"\' #[]<>{}|\\^~`";
    public final static List<String> ESCAPE_STRINGS = Collections.unmodifiableList(Arrays.asList(new String[]{
        "&lt;", "&gt;", "&amp;", "&quot;", "&apos;", "%20", "%23", "%5B", "%5D", "%3C", "%3E", "%7B", "%7D", "%7C", "%5C", "%5E", "%7E", "%60"
    }));

    private static String UNICODE_LOW = "" + ((char) 0x20); //space
    private static String UNICODE_HIGH = "" + ((char) 0x7f);

    //should only use for the content of an attribute or tag      
    public static String toEscaped(String content) {
        String result = content;

        if ((content != null) && (content.length() > 0)) {
            boolean modified = false;
            StringBuilder stringBuilder = new StringBuilder(content.length());
            for (int i = 0, count = content.length(); i < count; ++i) {
                String character = content.substring(i, i + 1);
                int pos = ESCAPE_CHARS.indexOf(character);
                if (pos > -1) {
                    stringBuilder.append(ESCAPE_STRINGS.get(pos));
                    modified = true;
                } else {
                    if ((character.compareTo(UNICODE_LOW) > -1)
                            && (character.compareTo(UNICODE_HIGH) < 1)) {
                        stringBuilder.append(character);
                    } else {
                        stringBuilder.append("&%23" + ((int) character.charAt(0)) + ";");
                        modified = true;
                    }
                }
            }
            if (modified) {
                result = stringBuilder.toString();
            }
        }

        return result;
    }

}
