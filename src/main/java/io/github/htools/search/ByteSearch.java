package io.github.htools.search;

import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.BoolTools;
import io.github.htools.lib.ByteTools;
import io.github.htools.lib.Log;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Factory for fast search of a byte array needle in a byte array. The input
 * pattern is considered to be a ByteRegex expression. Depending on the pattern,
 * a {@link ByteSearchSingle} is used for single characters (e.g. "&gt;", "a"), a
 * {@link ByteSearchSingleClass} is used for single characters that have several
 * possible matches (e.g. "\\s", "[a-z]"), a {@link ByteSearchString} is used
 * when the pattern contains multiple characters but no decision (e.g. "&lt;link"),
 * and a {@link ByteRegex} is used for more complex needles (e.g. "\\s*\\w").
 *
 * @author Jeroen Vuurens
 */
public abstract class ByteSearch {

    public static Log log = new Log(ByteSearch.class);
    public boolean quotesafe;
    public static ByteSearch WHITESPACE = ByteSearch.create("\\s+");
    public static ByteSearch EMPTY = new ByteSearchEmpty();

    public static ByteSearch create(String pattern) {
        if (pattern.length() == 0) {
            return EMPTY;
        } else if (pattern.length() == 1 && isSingle(pattern.charAt(0))) {
            return new ByteSearchSingle(pattern.getBytes()[0]);
        } else {
            ArrayList<Node> list = new ArrayList<Node>();
            Node root = ByteRegex.parseRegex(pattern, list);
            for (Node n : list) {
                if (n.type != Node.TYPE.CHAR) {
                    return new ByteRegex(pattern, root);
                }
            }
            if (list.size() == 1) {
                int first = 0;
                for (; first < 256 && !root.allowed[first]; first++);
                for (int i = first + 1; i < 256; i++) {
                    if (root.allowed[i]) {
                        return new ByteSearchSingleClass(pattern, root);
                    }
                }
                return new ByteSearchSingle((byte) first);
            } else {
                return new ByteSearchString(pattern, list);
            }
        }
    }

    public static ByteSearch createFilePattern(String pattern) {
        pattern = escape(pattern).replace(".", "\\.");
        pattern = escape(pattern).replace("*", ".*");
        return create(pattern);
    }

    public boolean[] firstAcceptedChar() {
        if (this instanceof ByteRegex) {
            return ArrayTools.clone(((ByteRegex) this).root.allowed);
        }
        if (this instanceof ByteSearchSingleClass) {
            return ((ByteSearchSingleClass) this).getValid();
        }
        if (this instanceof ByteSearchSingle) {
            return BoolTools.createASCIIAccept((char) ((ByteSearchSingle) this).b);
        }
        if (this instanceof ByteSearchString) {
            return ArrayTools.clone(((ByteSearchString) this).pattern[0]);
        }
        return new boolean[256];
    }

    public ByteSearch QuoteSafe() {
        quotesafe = true;
        return this;
    }

    public static boolean isSingle(char c) {
        return (c != '.' && c != '$' && c != '^' && !(c >= 'A' && c <= 'Z') && !(c >= 'a' && c <= 'z'));
    }

    public ByteSection toSection(ByteSearch o) {
        return new ByteSection(this, o);
    }

    public int findQuoteSafe(byte haystack[], int start, int end) {
        return find(haystack, start, end);
    }

    public int findNoQuoteSafe(byte haystack[], int start, int end) {
        return find(haystack, start, end);
    }

    public int find(byte haystack[], int start, int end) {
        return (quotesafe) ? findQuoteSafe(haystack, start, end) : findNoQuoteSafe(haystack, start, end);
    }

    public int find(String text) {
        byte bytes[] = ByteTools.toBytes(text);
        return find(bytes, 0, bytes.length);
    }

    public ByteSearchPosition findPos(String text) {
        byte bytes[] = ByteTools.toBytes(text);
        return findPos(bytes, 0, bytes.length);
    }

    public ByteSearchPosition findPos(byte[] haystack) {
        return findPos(haystack, 0, haystack.length);
    }

    public int findEnd(byte haystack[], int start, int end) {
        return (quotesafe) ? findEndQuoteSafe(haystack, start, end) : findEndQuoteSafe(haystack, start, end);
    }

    public int findEnd(ByteSearchSection section) {
        return findEnd(section.haystack, section.innerstart, section.innerend);
    }

    public int findEndQuoteSafe(byte haystack[], int start, int end) {
        return findEnd(haystack, start, end);
    }

    public int findEndQuoteSafe(ByteSearchSection section) {
        return findEndQuoteSafe(section.haystack, section.innerstart, section.innerend);
    }

    public int findEndNoQuoteSafe(byte haystack[], int start, int end) {
        return findEnd(haystack, start, end);
    }

    public abstract boolean match(byte haystack[], int position, int end);

    public boolean match(String haystack) {
        byte b[] = ByteTools.toBytes(haystack);
        return match(b, 0, b.length);
    }

    public boolean match(byte[] haystack) {
        return match(haystack, 0, haystack.length);
    }

    public boolean match(ByteSearchSection section) {
        return match(section.haystack, section.innerstart, section.innerend);
    }

    public boolean exists(ByteSearchSection section) {
        return exists(section.haystack, section.innerstart, section.innerend);
    }

    public ByteSearchPosition findPos(ByteSearchSection section) {
        return findPos(section.haystack, section.innerstart, section.innerend);
    }

    public ByteSearchPosition findPos(ByteSearchSection section, int from) {
        return findPos(section.haystack, from, section.innerend);
    }

    public boolean matchOuter(ByteSearchSection section) {
        return match(section.haystack, section.start, section.end);
    }

    public boolean existsOuter(ByteSearchSection section) {
        return exists(section.haystack, section.start, section.end);
    }

    public ByteSearchPosition findPosOuter(ByteSearchSection section) {
        return findPos(section.haystack, section.start, section.end);
    }

    public abstract int matchEnd(byte haystack[], int position, int end);

    public abstract ByteSearchPosition matchPos(byte b[], int start, int end);

    public String replace(String s, String replace) {
        ByteSearchPosition findPos = findPos(s);
        if (findPos.found()) {
            return s.substring(0, findPos.start) + replace + s.substring(findPos.end);
        }
        return s;
    }

    public String replaceAll(String s, String replace) {
        try {
            byte[] haystack = s.getBytes("UTF-8");
            ArrayList<ByteSearchPosition> allpos = this.findAllPos(haystack);
            if (allpos.size() > 0) {
                StringBuilder sb = new StringBuilder();
                int oldpos = 0;
                for (ByteSearchPosition pos : allpos) {
                    if (pos.start > oldpos) {
                        sb.append(ByteTools.toString(haystack, oldpos, pos.start)).append(replace);
                    }
                    oldpos = pos.end;
                }
                if (oldpos < haystack.length) {
                    sb.append(ByteTools.toString(haystack, oldpos, haystack.length));
                }
                return sb.toString();
            }
        } catch (UnsupportedEncodingException ex) {
            log.fatalexception(ex, "replaceAll %s %s", s, replace);
        }
        return s;
    }

    public void replaceAll(byte[] haystack, int start, int end, String replace) {
        try {
            byte[] replacement = null;
            for (ByteSearchPosition pos : findAllPos(haystack, start, end)) {
                if (replacement == null) {
                    replacement = replace.getBytes("UTF-8");
                }
                for (int i = 0; i < replacement.length && pos.start + i < pos.end; i++) {
                    haystack[pos.start + i] = replacement[i];
                }
                for (int i = pos.start + replacement.length; i < pos.end; i++) {
                    haystack[i] = 0;
                }
            }
        } catch (UnsupportedEncodingException ex) {
            log.fatalexception(ex, "replaceAll %s %s", ByteTools.toString(haystack), replace);
        }
    }

    public void replaceAll(ByteSearchSection section, String replace) {
        replaceAll(section.haystack, section.innerstart, section.innerend, replace);
    }

    /**
     * @param haystack
     * @return ByteSearchPosition for ByteSearch pattern at pos 0 of haystack
     */
    public ByteSearchPosition matchPos(String haystack) {
        byte b[] = ByteTools.toBytes(haystack);
        return matchPos(b, 0, b.length);
    }

    public String extractMatch(String s) {
        if (s == null) {
            return null;
        }
        byte b[] = ByteTools.toBytes(s);
        ByteSearchPosition matchPos = matchPos(b, 0, b.length);
        return matchPos.found() ? matchPos.toString() : null;
    }

    public String extractMatch(ByteSearchSection section) {
        ByteSearchPosition matchPos = matchPos(section.haystack, section.innerstart, section.innerend);
        return matchPos.found() ? matchPos.toString() : null;
    }

    public String extractMatchOuter(ByteSearchSection section) {
        ByteSearchPosition matchPos = findPos(section.haystack, section.start, section.end);
        return matchPos.found() ? matchPos.toString() : null;
    }

    /**
     * @param section
     * @return first String that matches the ByteSearch pattern
     */
    public String extract(ByteSearchSection section) {
        ByteSearchPosition findPos = findPos(section.haystack, section.innerstart, section.innerend);
        return findPos.found() ? findPos.toString() : null;
    }

    public byte[] extractBytes(ByteSearchSection section) {
        ByteSearchPosition findPos = findPos(section.haystack, section.innerstart, section.innerend);
        return findPos.found() ? findPos.toBytes() : null;
    }

    public String extractOuter(ByteSearchSection section) {
        ByteSearchPosition findPos = findPos(section.haystack, section.start, section.end);
        return findPos.found() ? findPos.toString() : null;
    }

    public String extract(String s) {
        if (s == null) {
            return null;
        }
        byte b[] = ByteTools.toBytes(s);
        ByteSearchPosition findPos = findPos(b, 0, b.length);
        return findPos.found() ? findPos.toString() : null;
    }

    public String extract(byte haystack[]) {
        ByteSearchPosition findPos = findPos(haystack, 0, haystack.length);
        return findPos.found() ? findPos.toString() : null;
    }

    public byte[] extractBytes(byte haystack[]) {
        ByteSearchPosition findPos = findPos(haystack, 0, haystack.length);
        return findPos.found() ? findPos.toBytes() : null;
    }

    public boolean exists(byte haystack[], int start, int end) {
        return find(haystack, start, end) > -1;
    }

    public boolean exists(byte haystack[]) {
        return exists(haystack, 0, haystack.length);
    }

    public boolean exists(String s) {
        byte a[] = ByteTools.toBytes(s);
        return exists(a, 0, a.length);
    }

    public boolean startsWith(String s) {
        byte a[] = ByteTools.toBytes(s);
        return match(a, 0, a.length);
    }

    public String findAsString(byte haystack[], int start, int end) {
        ByteSearchPosition pos = findPos(haystack, start, end);
        if (pos.found()) {
            return ByteTools.toString(haystack, pos.start, pos.end);
        }
        return null;
    }

    public String findAsTrimmedString(byte haystack[], int start, int end) {
        ByteSearchPosition pos = findPos(haystack, start, end);
        if (pos.found()) {
            return ByteTools.toTrimmedString(haystack, pos.start, pos.end);
        }
        return null;
    }

    public String findAsFullTrimmedString(byte haystack[], int start, int end) {
        ByteSearchPosition pos = findPos(haystack, start, end);
        if (pos.found()) {
            return ByteTools.toFullTrimmedString(haystack, pos.start, pos.end);
        }
        return null;
    }

    public ByteSearchPosition findPos(byte haystack[], int start, int end) {
        return (quotesafe) ? findPosQuoteSafe(haystack, start, end) : findPosNoQuoteSafe(haystack, start, end);
    }

    public ByteSearchPosition findPosQuoteSafe(byte haystack[], int start, int end) {
        return findPos(haystack, start, end);
    }

    public ByteSearchPosition findPosDoubleQuoteSafe(byte haystack[], int start, int end) {
        return findPos(haystack, start, end);
    }

    public ByteSearchPosition findPosNoQuoteSafe(byte haystack[], int start, int end) {
        return findPos(haystack, start, end);
    }

    public ByteSearchPosition findLastPos(String haystack) {
        byte bytes[] = ByteTools.toBytes(haystack);
        return findLastPos(bytes, 0, bytes.length);
    }

    public ByteSearchPosition findLastPos(ByteSearchSection section) {
        return findLastPos(section.haystack, section.innerstart, section.innerend);
    }

    public ByteSearchPosition findLastPos(ByteSearchSection section, int from) {
        return findLastPos(section.haystack, from, section.innerend);
    }

    public ByteSearchPosition findLastPos(byte haystack[], int start, int end) {
        if (this instanceof ByteSearchEmpty) {
            return new ByteSearchPosition(haystack, end, end, true);
        }
        for (int pos = end - 1; pos >= start; pos--) {
            ByteSearchPosition p = matchPos(haystack, pos, end);
            if (p.found()) {
                return p;
            }
        }
        return new ByteSearchPosition(haystack, end, -1, false);
    }

    /**
     * @return all matching positions, without overlap, so "\w+" used on "word"
     * will return 1 exists.
     */
    public ArrayList<ByteSearchPosition> findAllPos(byte b[], int start, int end) {
        ArrayList<ByteSearchPosition> list = new ArrayList<ByteSearchPosition>();
        while (start <= end) {
            ByteSearchPosition p = findPos(b, start, end);
            if (p.found()) {
                if (p.length() > 0)
                   list.add(p);
                if (start == p.end) {
                    start++;
                } else {
                    start = p.end;
                }
            } else {
                break;
            }
        }
        return list;
    }

    /**
     * @return all matching positions, without overlap, so "\w+" used on "word"
     * will return 1 exists.
     */
    public ArrayList<ByteSearchPosition> findPos(byte b[], int start, int end, int count) {
        ArrayList<ByteSearchPosition> list = new ArrayList<ByteSearchPosition>();
        while (start <= end && list.size() < count) {
            ByteSearchPosition p = findPos(b, start, end);
            if (p.found()) {
                list.add(p);
                if (start == p.end) {
                    start++;
                } else {
                    start = p.end;
                }
            } else {
                break;
            }
        }
        return list;
    }

    public ArrayList<Integer> findAll(byte b[], int start, int end) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        while (start <= end) {
            int p = find(b, start, end);
            if (p > -1) {
                list.add(p);
                start = (start == p) ? start + 1 : p;
            } else {
                break;
            }
        }
        return list;
    }

    /**
     * @return all matching positions, without overlap, so "\w+" used on "word"
     * will return 1 exists.
     */
    public ArrayList<ByteSearchPosition> findAllPos(String s) {
        return findAllPos(ByteTools.toBytes(s));
    }

    public ArrayList<ByteSearchPosition> findAllPos(byte haystack[]) {
        return findAllPos(haystack, 0, haystack.length);
    }

    public ArrayList<ByteSearchPosition> findAllPos(ByteSearchSection s) {
        return findAllPos(s.haystack, s.innerstart, s.innerend);
    }

    /**
     * @return all matching positions, allowing overlap, so "\w+" used on "word"
     * will result in matches "word", "ord", "rd" and "d".
     */
    public ArrayList<ByteSearchPosition> findAllPosOverlap(byte b[], int start, int end) {
        ArrayList<ByteSearchPosition> list = new ArrayList<ByteSearchPosition>();
        while (start < end) {
            ByteSearchPosition p = findPos(b, start, end);
            if (p.found()) {
                list.add(p);
                start = p.start + 1;
            } else {
                break;
            }
        }
        return list;
    }

    @Override
    public abstract String toString();

    public static String escape(String regex) {
        return regex.replace(".", "\\.").replace("-", "\\-");
    }
}
