package io.github.htools.hadoop.hashjoin;

import io.github.htools.hadoop.io.TextCaseless;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;
import org.apache.hadoop.io.WritableUtils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Adds an int type to a text key, to support secondary sorting the values.
 *
 * @author jeroen
 */
public class TextType extends Text {

    public static Log log = new Log(TextType.class);
    int type;

    public TextType() {
        super();
    }

    public TextType(int type, String string) {
        super(string);
        this.type = type;
    }

    public int getType() {
        return type;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        super.readFields(in);
        type = in.readByte() & 0xff;
    }

    @Override
    public void write(DataOutput out) throws IOException {
        super.write(out);
        out.write(type);
    }

    public void set(int type, String content) {
        this.type = type;
        super.set(content);
    }

    public static class Comparator extends WritableComparator {

        public Comparator() {
            super(TextType.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            int n1 = WritableUtils.decodeVIntSize(b1[s1]);
            int n2 = WritableUtils.decodeVIntSize(b2[s2]);
            return compareBytes(b1, s1 + n1, l1 - n1 - 1, b2, s2 + n2, l2 - n2 - 1);
        }
    }

    public static class SecondarySort extends WritableComparator {

        public SecondarySort() {
            super(TextType.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            int n1 = WritableUtils.decodeVIntSize(b1[s1]);
            int n2 = WritableUtils.decodeVIntSize(b2[s2]);
            return compareBytes(b1, s1 + n1, l1 - n1, b2, s2 + n2, l2 - n2);
        }
    }

    public static class CaselessComparator extends WritableComparator {

        public CaselessComparator() {
            super(TextType.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            int n1 = WritableUtils.decodeVIntSize(b1[s1]);
            int n2 = WritableUtils.decodeVIntSize(b2[s2]);
            return TextCaseless.compareBytes(b1, s1 + n1, l1 - n1 - 1, b2, s2 + n2, l2 - n2 - 1);
        }
    }

    public static class CaselessSecondarySort extends WritableComparator {

        public CaselessSecondarySort() {
            super(TextType.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            int n1 = WritableUtils.decodeVIntSize(b1[s1]);
            int n2 = WritableUtils.decodeVIntSize(b2[s2]);
            return TextCaseless.compareBytes(b1, s1 + n1, l1 - n1, b2, s2 + n2, l2 - n2);
        }
    }

    static {
        // register this comparator
        WritableComparator.define(TextType.class, new Comparator());
    }
}
