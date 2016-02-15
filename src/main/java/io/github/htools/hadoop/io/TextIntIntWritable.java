package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class TextIntIntWritable extends Text {

    public static Log log = new Log(TextIntIntWritable.class);
    private int value2;
    private int value3;

    public TextIntIntWritable() {
        super();
    }

    public TextIntIntWritable(String key, int value2, int value3) {
        super(key);
        this.value2 = value2;
        this.value3 = value3;
    }

    public void set(String key, int value2, int value3) {
        super.set(key);
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readString0());
        value2 = reader.readInt();
        value3 = reader.readInt();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write0(toString());
        writer.write(value2);
        writer.write(value3);
        writer.writeBuffer(out);
    }

    static {
        // register this comparator
        WritableComparator.define(TextIntIntWritable.class, new Text.Comparator());
    }

//    public static class SortComparator extends WritableComparator {
//
//        @Override
//        public int compare(byte[] b1, int s1, int l1,
//                byte[] b2, int s2, int l2) {
//            return compareBytes(b1, s1 + 4, l1 - 4, b2, s2 + 4, l2 - 4);
//        }
//    }
//
//    public static class GroupComparator extends WritableComparator {
//
//        @Override
//        public int compare(byte[] b1, int s1, int l1,
//                byte[] b2, int s2, int l2) {
//            int i = s1 + 4;
//            int j = s2 + 4;
//            for (; b1[i] != 0 && b2[j] != 0; i++, j++) {
//                int a = (b1[i] & 0xff);
//                int b = (b2[j] & 0xff);
//                if (a != b) {
//                    return a - b;
//                }
//            }
//            return 0;
//        }
//    }
//
//    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<TextIntIntWritable, Object> {
//
//        @Override
//        public int getPartition(TextIntIntWritable key, Object value, int numPartitions) {
//            return MathTools.mod(key.toString().hashCode(), numPartitions);
//        }
//
//    }
}
