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
public class IntIntLongWritable extends IntWritable {

    public static Log log = new Log(IntIntLongWritable.class);
    private int value2;
    private long value3;

    public IntIntLongWritable() {
        super();
    }

    public IntIntLongWritable(int partition, int value2, long value3) {
        super();
        set(partition, value2, value3);
    }

    public void set(int partition, int value2, long value3) {
        super.set(partition);
        this.value2 = value2;
        this.value3 = value3;
    }

    public int getValue2() {
        return value2;
    }
    
    public long getValue3() {
        return value3;
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readInt(), reader.readInt(), reader.readLong());
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
        WritableComparator.define(IntIntLongWritable.class, new TimeSortComparator());
    }

    public static class TimeSortComparator extends WritableComparator {

        public TimeSortComparator() {
            super(IntIntLongWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return WritableComparator.compareBytes(b1, s1+12, l1-4, b2, s2+12, l2-4);
        }
    }

    public static class DayHourPartitioner extends org.apache.hadoop.mapreduce.Partitioner<IntIntLongWritable, Object> {

        @Override
        public int getPartition(IntIntLongWritable key, Object value, int numPartitions) {
            return ((key.get() -1 ) * 24 + key.value2) % numPartitions;
        }

    }
}
