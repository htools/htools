package io.github.repir.tools.hadoop.io.buffered;

import io.github.repir.tools.lib.Log;

public class ComparatorLong extends ComparatorSub {

    public static final Log log = new Log(ComparatorLong.class);

    @Override
    public int compare(Comparator r) {
        int i = 0;
        for (; i < 8 && r.byte2[r.start2] != r.byte1[r.start1]; i++, r.start1++, r.start2++);
        if (i == 8)
            return 0;
        if (i == 0) {
            return r.byte1[r.start1] - r.byte2[r.start2];
        } 
        int a = (r.byte1[r.start1] & 0xff);
        int b = (r.byte2[r.start2] & 0xff);
        return a - b;
    }
}
