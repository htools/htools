package io.github.htools.search;

import io.github.htools.lib.Log;
import io.github.htools.lib.PrintTools;

import java.util.ArrayList;

/**
 * Fast string search in Byte Array.
 *
 * @author Jeroen Vuurens
 */
public class ByteSearchString extends ByteSearch {

    public static Log log = new Log(ByteSearchString.class);
    public String originalpattern;
    protected final boolean pattern[][];
    private int laststart;
    private int lastend;

    private ByteSearchString(int length) {
        pattern = new boolean[length][256];
    }

    protected ByteSearchString(String originalpattern, ArrayList<Node> list) {
        this.originalpattern = originalpattern;
        pattern = new boolean[list.size()][256];
        for (int i = 0; i < pattern.length; i++) {
            pattern[i] = list.get(i).allowed;
        }
    }

    public String toString() {
        return PrintTools.sprintf("ByteSearchString( %s )", originalpattern);
    }

    @Override
    public int findQuoteSafe(byte haystack[], int start, int end) {
        int match = 0;
        laststart = start;
        LOOP:
        for (lastend = start; lastend < end; lastend++) {
            if (haystack[lastend] != 0) { // skip \0 bytes
                switch (haystack[start]) {
                    case '"':
                        for (start++; start < end; start++) {
                            if (haystack[start] == '\\') {
                                start++;
                            } else if (haystack[start] == '"') {
                                continue LOOP;
                            }
                        }
                        break LOOP;
                    case '\'':
                        for (start++; start < end; start++) {
                            if (haystack[start] == '\\') {
                                start++;
                            } else if (haystack[start] == '\'') {
                                continue LOOP;
                            }
                        }
                        break LOOP;
                }

                if (pattern[match][haystack[lastend] & 0xFF]) {
                    if (++match == pattern.length) {
                        lastend++;
                        return laststart;
                    }
                } else {
                    match = 0;
                    laststart = lastend + 1;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public int findNoQuoteSafe(byte haystack[], int start, int end) {
        int match = 0;
        laststart = start;
        for (lastend = start; lastend < end; lastend++) {
            if (haystack[lastend] != 0) { // skip \0 bytes
                if (lastend > haystack.length - 1) {
                    log.info("%d %d %s %d %d", start, end, new String(haystack), match, pattern.length);
                }
                if (pattern[match][haystack[lastend] & 0xFF]) {
                    if (++match == pattern.length) {
                        lastend++;
                        return laststart;
                    }
                } else {
                    match = 0;
                    laststart = lastend + 1;
                }
            }
        }
        return Integer.MIN_VALUE;
    }

    @Override
    public int findEnd(byte haystack[], int start, int end) {
        if (find(haystack, start, end) > -1) {
            return lastend;
        } else {
            return Integer.MIN_VALUE;
        }
    }

    @Override
    public boolean match(byte[] haystack, int position, int end) {
        return matchPos(haystack, position, end).found();
    }

    @Override
    public int matchEnd(byte[] haystack, int position, int end) {
        ByteSearchPosition pos = matchPos(haystack, position, end);
        return pos.found() ? pos.end : Integer.MIN_VALUE;
    }

    /**
     * Is NOT Thread safe
     */
    @Override
    public ByteSearchPosition findPos(byte[] haystack, int start, int end) {
        int pos = find(haystack, start, end);
        if (pos < 0) {
            ByteSearchPosition p = new ByteSearchPosition(haystack, laststart, Integer.MIN_VALUE);
            p.endreached = true;
            return p;
        } else {
            ByteSearchPosition p = new ByteSearchPosition(haystack, pos, lastend);
            return p;
        }
    }

    @Override
    public ByteSearchPosition matchPos(byte[] haystack, int position, int end) {
        int lastend = position;
        for (int match = 0; lastend < end; lastend++) {
            if (haystack[lastend] != 0) { // skip \0 bytes
                if (pattern[match][haystack[lastend] & 0xFF]) {
                    if (++match == pattern.length) {
                        return new ByteSearchPosition(haystack, position, ++lastend, true);
                    }
                } else {
                    return new ByteSearchPosition(haystack, position, Integer.MIN_VALUE, false);
                }
            }
        }
        return new ByteSearchPosition(haystack, position, lastend, false);
    }

    public static void main(String[] args) {
        ByteSearchString a = (ByteSearchString) ByteSearch.create("Eintein");
        log.info("%b", a.exists("this Albert einstein and this too"));
    }
}
