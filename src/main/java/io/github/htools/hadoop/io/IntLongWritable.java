package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparator;
import static org.apache.hadoop.io.WritableComparator.readLong;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class IntLongWritable extends IntWritable {

    public static Log log = new Log(IntLongWritable.class);
    private long value2;

    public IntLongWritable() {
        super();
    }

    public IntLongWritable(int partition, long value2) {
        super(partition);
        this.value2 = value2;
    }

    public void set(int partition, long value2) {
        super.set(partition);
        this.value2 = value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readInt());
        value2 = in.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(get());
        out.writeLong(value2);
    }

    static {
        // register this comparator
        WritableComparator.define(IntLongWritable.class, new IntWritable.Comparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(IntLongWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            int thisValue = readInt(b1, s1);
            int thatValue = readInt(b2, s2);
            if (thisValue < thatValue) {
                return -1;
            }
            if (thisValue > thatValue) {
                return 1;
            }
            long thisValue2 = readLong(b1, s1 + 4);
            long thatValue2 = readLong(b2, s2 + 4);
            return (thisValue2 < thatValue2 ? -1 : (thisValue2 == thatValue2 ? 0 : 1));
        }
    }

    public static class DecreasingComparator extends SortComparator {

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }

    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<IntLongWritable, Object> {

        @Override
        public int getPartition(IntLongWritable key, Object value, int numPartitions) {
            return MathTools.mod(key.get(), numPartitions);
        }

    }
}
