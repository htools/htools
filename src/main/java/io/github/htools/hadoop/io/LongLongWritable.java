package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import org.apache.hadoop.io.LongWritable;
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
public class LongLongWritable extends LongWritable {

    public static Log log = new Log(LongLongWritable.class);
    private long value2;

    public LongLongWritable() {
        super();
    }

    public LongLongWritable(long value1, long value2) {
        super(value1);
        this.value2 = value2;
    }

    public void set(long value1, long value2) {
        super.set(value1);
        this.value2 = value2;
    }
    
    public long getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readLong());
        value2 = in.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(get());
        out.writeLong(value2);
    }

    static {
        // register this comparator
        WritableComparator.define(LongLongWritable.class, new LongWritable.Comparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(LongLongWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            long thisValue = readLong(b1, s1);
            long thatValue = readLong(b2, s2);
            if (thisValue < thatValue) {
                return -1;
            }
            if (thisValue > thatValue) {
                return 1;
            }
            long thisValue2 = readLong(b1, s1 + 8);
            long thatValue2 = readLong(b2, s2 + 8);
            return (thisValue2 < thatValue2 ? -1 : (thisValue2 == thatValue2 ? 0 : 1));
        }
    }

    public static class DecreasingComparator extends SortComparator {

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }

    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<LongLongWritable, Object> {

        @Override
        public int getPartition(LongLongWritable key, Object value, int numPartitions) {
            return MathTools.mod((int)key.get(), numPartitions);
        }

    }
}
