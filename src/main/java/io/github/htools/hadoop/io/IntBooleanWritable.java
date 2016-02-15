package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

/**
 * Pairs an int that indicates the partition number with a of long used for secondary sorting.
 *
 * @author jeroen
 */
public class IntBooleanWritable extends IntWritable {

    public static Log log = new Log(IntBooleanWritable.class);
    private boolean value2;

    public IntBooleanWritable() {
        super();
    }

    public IntBooleanWritable(int partition, boolean value2) {
        super(partition);
        this.value2 = value2;
    }

    public void set(int partition, boolean value2) {
        super.set(partition);
        this.value2 = value2;
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readInt());
        value2 = in.readBoolean();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeInt(get());
        out.writeBoolean(value2);
    }

    static {
        // register this comparator
        WritableComparator.define(IntBooleanWritable.class, new IntWritable.Comparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(IntBooleanWritable.class);
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
            int thisValue2 = readInt(b1, s1 + 4);
            int thatValue2 = readInt(b2, s2 + 4);
            return (thisValue2 < thatValue2 ? -1 : (thisValue2 == thatValue2 ? 0 : 1));
        }
    }
    
    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<IntBooleanWritable, Object> {

        @Override
        public int getPartition(IntBooleanWritable key, Object value, int numPartitions) {
            return MathTools.mod(key.get(), numPartitions);
        }
        
    }
}
