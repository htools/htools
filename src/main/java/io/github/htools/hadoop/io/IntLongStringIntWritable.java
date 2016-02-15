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
public class IntLongStringIntWritable extends IntWritable {

    public static Log log = new Log(IntLongStringIntWritable.class);
    private long value2;
    private String value3;
    private int value4;

    public IntLongStringIntWritable() {
        super();
    }

    public IntLongStringIntWritable(int partition, long value2, String value3, int value4) {
        super();
        set(partition, value2, value3, value4);
    }

    public void set(int partition, long value2, String value3, int value4) {
        super.set(partition);
        this.value2 = value2;
        this.value3 = value3;
        this.value4 = value4;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readInt(), reader.readLong(), reader.readString0(), reader.readInt());
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write(get());
        writer.write(value2);
        writer.write0(value3);
        writer.write(value4);
        writer.writeBuffer(out);
    }

    static {
        // register this comparator
        WritableComparator.define(IntLongStringIntWritable.class, new IntWritable.Comparator());
    }

  /** A Comparator optimized for IntWritable. */ 
  public static class Comparator extends WritableComparator {
    public Comparator() {
      super(IntLongStringIntWritable.class);
    }
    
    @Override
    public int compare(byte[] b1, int s1, int l1,
                       byte[] b2, int s2, int l2) {
      int thisValue = readInt(b1, s1+4);
      int thatValue = readInt(b2, s2+4);
      return (thisValue<thatValue ? -1 : (thisValue==thatValue ? 0 : 1));
    }
  }

  static {                                        // register this comparator
    WritableComparator.define(IntWritable.class, new Comparator());
  }
    
    
    public static class SortComparator extends WritableComparator {

        public SortComparator() {
            super(IntLongStringIntWritable.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return Comparator.compareBytes(b1, s1 + 4, l1 - 4, b2, s2 + 4, l2 - 4);
        }
    }

    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<IntLongStringIntWritable, Object> {

        @Override
        public int getPartition(IntLongStringIntWritable key, Object value, int numPartitions) {
            return key.get();
        }

    }
}
