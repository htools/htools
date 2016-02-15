package io.github.htools.search;

import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

import java.util.ArrayList;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchSingleClass extends ByteSearch {

    public static Log log = new Log(ByteSearchSingleClass.class);
    boolean valid[];
    String pattern;

    protected ByteSearchSingleClass(String pattern, Node node) {
        this.pattern = pattern;
        valid = node.allowed;
    }

    public static ByteSearchSingleClass create(String pattern) {
        ArrayList<Node> list = new ArrayList<Node>();
        Node root = ByteRegex.parseRegex(pattern, list);
        if (list.size() == 1 && list.get(0).type == Node.TYPE.CHAR) {
            return new ByteSearchSingleClass(pattern, root);
        } else {
            log.info("Invalid pattern for ByteSearchSingleClass is no char or more than 1 char '%s'", pattern);
        }
        return null;
    }
    
    public boolean[] getValid() {
        return ArrayTools.clone(valid);
    }

    @Override
    public int findQuoteSafe(byte haystack[], int start, int end) {
        LOOP:
        for (int i = start; i < end; i++) {
            switch (haystack[i]) {
                case '"':
                    for (i++; i < end; i++) {
                        if (haystack[i] == '\\') {
                            i++;
                        } else if (haystack[i] == '"') {
                            continue LOOP;
                        }
                    }
                    break LOOP;
                case '\'':
                    for (i++; i < end; i++) {
                        if (haystack[i] == '\\') {
                            i++;
                        } else if (haystack[i] == '\'') {
                            continue LOOP;
                        }
                    }
                    break LOOP;
            }
            if (valid[haystack[i] & 0xFF]) {
                return i;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public int findNoQuoteSafe(byte haystack[], int start, int end) {
        for (; start < end; start++) {
            if (valid[haystack[start] & 0xFF]) {
                return start;
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public int findEnd(byte haystack[], int start, int end) {
        int pos = find(haystack, start, end);
        return (pos > -1) ? pos + 1 : Integer.MIN_VALUE;
    }

    @Override
    public boolean match(byte[] haystack, int position, int end) {
        return valid[haystack[position] & 0xFF];
    }

    @Override
    public int matchEnd(byte[] haystack, int position, int end) {
        return valid[haystack[position] & 0xFF] ? position + 1 : Integer.MIN_VALUE;
    }

    @Override
    public ByteSearchPosition matchPos(byte[] haystack, int position, int end) {
        ByteSearchPosition p = new ByteSearchPosition(haystack, position,
        (valid[haystack[position]]) ? position + 1 : Integer.MIN_VALUE);
        return p;
    }

    @Override
    public ByteSearchPosition findPos(byte[] haystack, int start, int end) {
        ByteSearchPosition p;
        int pos = find(haystack, start, end);
        if (pos < 0) {
            p = new ByteSearchPosition(haystack, end, Integer.MIN_VALUE);
            p.endreached = true;
        } else {
            p = new ByteSearchPosition(haystack, pos, pos + 1);
        }
        return p;
    }

    public String toString() {
        return PrintTools.sprintf("ByteSearchSingleClass(%s)", pattern);
    }
}
