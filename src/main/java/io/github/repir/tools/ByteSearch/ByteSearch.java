package io.github.repir.tools.ByteSearch;

import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.BoolTools;
import io.github.repir.tools.Lib.ByteTools;
import io.github.repir.tools.Lib.Log;
import java.util.ArrayList;

/**
 * Factory for fast search of a byte array needle in a byte array. The input
 * pattern is considered to be a ByteRegex expression. Depending on the pattern,
 * a {@link ByteSearchSingle} is used for single characters (e.g. ">", "a"), a
 * {@link ByteSearchSingleClass} is used for single characters that have several
 * possible matches (e.g. "\\s", "[a-z]"), a {@link ByteSearchString} is used
 * when the pattern contains multiple characters but no decision (e.g. "<link"),
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
    
    public boolean[] firstAcceptedChar() {
        if (this instanceof ByteRegex)
            return ArrayTools.clone(((ByteRegex)this).root.allowed);
        if (this instanceof ByteSearchSingleClass)
            return ((ByteSearchSingleClass)this).getValid();
        if (this instanceof ByteSearchSingle)
            return BoolTools.createASCIIAccept((char)((ByteSearchSingle)this).b);
        if (this instanceof ByteSearchString)
            return ArrayTools.clone(((ByteSearchString)this).pattern[0]);
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
        byte bytes[] = text.getBytes();
        return find(bytes, 0, bytes.length);
    }

    public ByteSearchPosition findPos(String text) {
        byte bytes[] = text.getBytes();
        return findPos(bytes, 0, bytes.length);
    }

    public int findEnd(byte haystack[], int start, int end) {
        return (quotesafe) ? findEndQuoteSafe(haystack, start, end) : findEndQuoteSafe(haystack, start, end);
    }

    public int findEndQuoteSafe(byte haystack[], int start, int end) {
        return findEnd(haystack, start, end);
    }

    public int findEndNoQuoteSafe(byte haystack[], int start, int end) {
        return findEnd(haystack, start, end);
    }

    public abstract boolean match(byte haystack[], int position, int end);

    public boolean match(String haystack) {
        byte b[] = haystack.getBytes();
        return match(b, 0, b.length);
    }

    public abstract int matchEnd(byte haystack[], int position, int end);

    public abstract ByteSearchPosition matchPos(byte b[], int start, int end);

    /**
     * @param haystack
     * @return ByteSearchPosition for ByteSearch pattern at pos 0 of haystack
     */
    public ByteSearchPosition matchPos(String haystack) {
        byte b[] = haystack.getBytes();
        return matchPos(b, 0, b.length);
    }

    public String extractMatch(String s) {
        if (s == null) {
            return null;
        }
        byte b[] = s.getBytes();
        ByteSearchPosition matchPos = matchPos(b, 0, b.length);
        return matchPos.found() ? matchPos.toString(b) : null;
    }

    public String extractFind(String s) {
        if (s == null) {
            return null;
        }
        byte b[] = s.getBytes();
        ByteSearchPosition findPos = findPos(b, 0, b.length);
        return findPos.found() ? findPos.toString(b) : null;
    }

    public boolean exists(byte haystack[], int start, int end) {
        return find(haystack, start, end) > -1;
    }

    public boolean exists(String s) {
        byte a[] = s.getBytes();
        return exists(a, 0, a.length);
    }

    public boolean startsWith(String s) {
        byte a[] = s.getBytes();
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

    public ByteSearchPosition findPosNoQuoteSafe(byte haystack[], int start, int end) {
        return findPos(haystack, start, end);
    }

    public ByteSearchPosition findLastPos(String haystack) {
        byte bytes[] = haystack.getBytes();
        return findLastPos(bytes, 0, bytes.length);
    }

    public ByteSearchPosition findLastPos(byte haystack[], int start, int end) {
        if (this instanceof ByteSearchEmpty) {
            return new ByteSearchPosition(end, end, true);
        }
        for (int pos = end - 1; pos >= start; pos--) {
            ByteSearchPosition p = matchPos(haystack, pos, end);
            if (p.found()) {
                return p;
            }
        }
        return new ByteSearchPosition(end, -1, false);
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
        byte b[] = s.getBytes();
        return findAllPos(b, 0, b.length);
    }

    public String replaceAll(String s, String replacement) {
        byte b[] = s.getBytes();
        StringBuilder sb = new StringBuilder();
        int currentpos = 0;
        for (ByteSearchPosition pos : findAllPos(b, 0, b.length)) {
            if (currentpos < pos.start) {
                sb.append(ByteTools.toString(b, currentpos, pos.start));
            }
            sb.append(replacement);
            currentpos = pos.end;
        }
        if (currentpos < b.length) {
            sb.append(ByteTools.toString(b, currentpos, b.length));
        }
        return sb.toString();
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
}
