package io.github.repir.tools.hadoop.io;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.lib.MathTools;
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
public class LongIntWritable extends LongWritable {

    public static Log log = new Log(LongIntWritable.class);
    private int value2;

    public LongIntWritable() {
        super();
    }

    public LongIntWritable(long value1, int value2) {
        super(value1);
        this.value2 = value2;
    }

    public void set(long value1, int value2) {
        super.set(value1);
        this.value2 = value2;
    }
    
    public long getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        set(in.readLong());
        value2 = in.readInt();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        out.writeLong(get());
        out.writeInt(value2);
    }

    static {
        // register this comparator
        WritableComparator.define(LongIntWritable.class, new LongWritable.Comparator());
    }

    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(LongIntWritable.class);
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
            int thisValue2 = readInt(b1, s1 + 8);
            int thatValue2 = readInt(b2, s2 + 8);
            return (thisValue2 < thatValue2 ? -1 : (thisValue2 == thatValue2 ? 0 : 1));
        }
    }

    public static class DecreasingComparator extends SortComparator {

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return -super.compare(b1, s1, l1, b2, s2, l2);
        }
    }
}
