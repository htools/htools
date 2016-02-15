package io.github.htools.hadoop.io;

import io.github.htools.io.buffer.BufferDelayedWriter;
import io.github.htools.io.buffer.BufferReaderWriter;
import io.github.htools.lib.DateTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Calendar;

/**
 * Pairs an int that indicates the partition number with a of long used for
 * secondary sorting.
 *
 * @author jeroen
 */
public class LongText extends LongWritable {

    public static Log log = new Log(LongText.class);
    private String value2;

    public LongText() {
        super();
    }

    public LongText(long key, String value2) {
        super(key);
        this.value2 = value2;
    }

    public void set(long key, String value2) {
        super.set(key);
        this.value2 = value2;
    }

    public String getValue2() {
        return value2;
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        BufferReaderWriter reader = new BufferReaderWriter(in);
        set(reader.readLong());
        value2 = reader.readString0();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        BufferDelayedWriter writer = new BufferDelayedWriter();
        writer.write(get());
        writer.write0(value2);
        writer.writeBuffer(out);
    }
    
    public static class KeySortComparator extends WritableComparator {

        public KeySortComparator() {
            super(LongText.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return WritableComparator.compareBytes(b1, s1+4, l1-4, b2, s2+4, l2-4);
        }
    }

    public static class KeyComparator extends WritableComparator {

        public KeyComparator() {
            super(LongText.class);
        }

        @Override
        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            int i = 4;
            for (; i < 12 && b1[s1 + i] == b2[s2 + i]; i++);
            if (i == 12)
               for (; b1[s1 + i] != 0 && b1[s1 + i] == b2[s2 + i]; i++);
            return b1[s1 + i] - b2[s2 + i];
        }
    }

    public static class DayPartitioner extends org.apache.hadoop.mapreduce.Partitioner<LongText, Object> {

        @Override
        public int getPartition(LongText key, Object value, int numPartitions) {
            Calendar cal = DateTools.epochToCalendar(key.get());
            return cal.get(Calendar.DAY_OF_MONTH) % numPartitions;
        }
    }
    
}
