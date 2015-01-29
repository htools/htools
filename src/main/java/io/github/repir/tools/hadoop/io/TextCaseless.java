package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;
import org.apache.hadoop.io.BinaryComparable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

/**
 * Replaces Text in cases where cases need to be preserved, but for comparison
 * and hashing needs to be case less.
 *
 * @author jeroen
 */
public class TextCaseless extends Text {
    public static Log log = new Log(TextCaseless.class);

    public TextCaseless() {
        super();
    }

    public TextCaseless(String string) {
        super(string);
    }

    public TextCaseless(Text utf8) {
        super(utf8);
    }

    public TextCaseless(byte[] utf8) {
        super(utf8);
    }

    public static class Comparator extends WritableComparator {

        public Comparator() {
            super(TextCaseless.class);
        }

        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            int n1 = WritableUtils.decodeVIntSize(b1[s1]);
            int n2 = WritableUtils.decodeVIntSize(b2[s2]);
            return TextCaseless.compareBytes(b1, s1 + n1, l1 - n1, b2, s2 + n2, l2 - n2);
        }
    }

    static {
        // register this comparator
        WritableComparator.define(TextCaseless.class, new Comparator());
    }

    @Override
    public int hashCode() {
        byte bytes[] = getBytes();
        int hash = 1;
        for (int i = 0; i < bytes.length; i++) {
            hash = (31 * hash) + lowercase[bytes[i] & 0xff];
        }
        return hash;
    }

    @Override
    public int compareTo(byte[] other, int off, int len) {
        return compareBytes(getBytes(), 0, getLength(), other, off, len);
    }

    @Override
    public int compareTo(BinaryComparable other) {
        if (this == other) {
            return 0;
        }
        if (other instanceof TextCaseless) {
            return compareBytes(getBytes(), 0, getLength(), other.getBytes(), 0, other.getLength());
        } else {
            return WritableComparator.compareBytes(getBytes(), 0, getLength(), other.getBytes(), 0, other.getLength());
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TextCaseless)) {
            return false;
        }
        TextCaseless that = (TextCaseless) other;
        if (this.getLength() != that.getLength()) {
            return false;
        }
        return compareBytes(getBytes(), 0, getLength(), that.getBytes(), 0, that.getLength()) == 0;
    }

    private static final int[] lowercase = getLC();

    /**
     * Lexicographic order of binary data. Case insensitive
     */
    public static int compareBytes(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
        int end1 = s1 + l1;
        int end2 = s2 + l2;
        for (int i = s1, j = s2; i < end1 && j < end2; i++, j++) {
            int a = (lowercase[b1[i] & 0xff]);
            int b = (lowercase[b2[j] & 0xff]);
            if (a != b) {
                return a - b;
            }
        }
        return l1 - l2;
    }

    private static int[] getLC() {
        int array[] = new int[256];
        for (int i = 0; i < 256; i++) {
            if (i >= 'A' && i <= 'Z') {
                array[i] = (i + 32);
            } else {
                array[i] = i;
            }
        }
        return array;
    }
}
