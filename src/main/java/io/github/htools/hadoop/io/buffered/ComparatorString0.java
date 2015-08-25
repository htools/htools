package io.github.htools.hadoop.io.buffered;

import io.github.htools.lib.Log;

/**
 *
 * @author jeroen
 */
public class ComparatorString0 extends ComparatorSub {

    public static final Log log = new Log(ComparatorString0.class);

    @Override
    public int compare(Comparator r) {
        for (; r.byte1[r.start1] != 0 && r.byte2[r.start2] == r.byte1[r.start1]; r.start1++, r.start2++);
        if (r.byte1[r.start1] == r.byte2[r.start2]) {
            r.start1++;
            r.start2++;
            return 0;
        }
        return (r.byte1[r.start1] & 0xff) - (r.byte2[r.start2] & 0xff);
    }
}
