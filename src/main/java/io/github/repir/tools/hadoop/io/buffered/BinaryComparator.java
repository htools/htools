package io.github.repir.tools.hadoop.io.buffered;

import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.WritableComparator;
/**
 *
 * @author jeroen
 */
public class BinaryComparator extends WritableComparator {

    @Override
    public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        return compareBytes(b1, s1 + 4, l1 - 4, b2, s2 + 4, l2 - 4);
    }

}
