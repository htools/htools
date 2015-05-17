package io.github.repir.tools.hadoop.io.buffered;

import io.github.repir.tools.lib.Log;

/**
 *
 * @author jeroen
 */
public class ComparatorByte extends ComparatorSub {

    public static final Log log = new Log(ComparatorByte.class);

    @Override
    public int compare(Comparator r) {
        return (r.byte1[r.start1++] & 0xff) - (r.byte2[r.start2++] & 0xff);
    }
}
