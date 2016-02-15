package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.IntWritable;
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
public class IntLongIntWritable extends IntWritable {

    public static Log log = new Log(IntLongIntWritable.class);
    private long value2;
    private int value3;

    public IntLongIntWritable() {
        super();
    }

    public IntLongIntWritable(int partition, long value2, int value3) {
        super();
        set(partition, value2, value3);
    }

    public void set(int partition, long value2, int value3) {
        super.set(partition);
        this.value2 = value2;
        this.value3 = value3;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readInt(), reader.readLong(), reader.readInt());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write(get());
        writer.write(value2);
        writer.write(value3);
        writer.writeBuffer(out);
    }

    static {                                        // register this comparator
        WritableComparator.define(IntLongIntWritable.class, new SortComparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(IntLongIntWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return WritableComparator.compareBytes(b1, s1+4, l1-4, b2, s2+4, l2-4);
//            s1 += 4;
//            s2 += 4;
//            for (; s1 < l1 && s2 < l2; s1++, s2++) {
//                int c = (b1[s1] & 0xff) - (b2[s2] & 0xff);
//                if (c != 0) {
//                    return c;
//                }
//            }
//            return (l1 - l2);
        }
    }

    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<IntLongIntWritable, Object> {

        @Override
        public int getPartition(IntLongIntWritable key, Object value, int numPartitions) {
            return key.get();
        }

    }
}
