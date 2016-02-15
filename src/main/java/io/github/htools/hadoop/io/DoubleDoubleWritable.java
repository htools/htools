package io.github.htools.hadoop.io;

import io.github.htools.lib.Log;
import io.github.htools.lib.MathTools;
import org.apache.hadoop.io.DoubleWritable;
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
public class DoubleDoubleWritable extends DoubleWritable {

    public static Log log = new Log(DoubleDoubleWritable.class);
    private double value2;

    public DoubleDoubleWritable() {
        super();
    }

    public DoubleDoubleWritable(double value1, double value2) {
        super(value1);
        this.value2 = value2;
    }

    public void setValue2(double value2) {
        this.value2 = value2;
    }

    public double getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readInt());
        value2 = in.readDouble();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeDouble(get());
        out.writeDouble(value2);
    }

    static {
        // register this comparator
        WritableComparator.define(DoubleDoubleWritable.class, new SortComparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(DoubleDoubleWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1,
                byte[] b2, int s2, int l2) {
            double thisValue = readDouble(b1, s1);
            double thatValue = readDouble(b2, s2);
            if (thisValue < thatValue) {
                return -1;
            }
            if (thisValue > thatValue) {
                return 1;
            }
            double thisValue2 = readDouble(b1, s1 + 4);
            double thatValue2 = readDouble(b2, s2 + 4);
            return (thisValue2 < thatValue2 ? -1 : (thisValue2 == thatValue2 ? 0 : 1));
        }
    }
    
    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<DoubleDoubleWritable, Object> {

        @Override
        public int getPartition(DoubleDoubleWritable key, Object value, int numPartitions) {
            return MathTools.mod((int)Double.doubleToLongBits(key.get()), numPartitions);
        }
        
    }
}
