package io.github.repir.tools.Lib;

import java.util.regex.Pattern;

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

    public static String stripMethod(String url) {
        int i = url.indexOf("://");
        if (i > 0) {
            return url.substring(i + 3);
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

}
