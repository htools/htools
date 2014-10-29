package io.github.repir.tools.hadoop;

import io.github.repir.tools.Lib.DateTimeTools;
import io.github.repir.tools.Lib.Log;
import java.util.Calendar;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparator;

/**
 * Replaces Text in cases where cases need to be preserved, but for comparison
 * and hashing needs to be case less.
 *
 * @author jeroen
 */
public class Time extends LongWritable {
    public static Log log = new Log(Time.class);

    public Time() {
        super();
    }

    public Time(long time) {
        super(time);
    }

    public static class Comparator extends WritableComparator {

        public Comparator() {
            super(Time.class);
        }

        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return compareBytes(b1, s1, l1, b2, s2, l2);
        }
    }

    static {
        // register this comparator
        WritableComparator.define(Time.class, new Comparator());
    }
    
    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<Time, Object> {

        @Override
        public int getPartition(Time key, Object value, int i) {
            Calendar c = DateTimeTools.toCalendar(key.get());
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            return month * 100 + day;
        }
    }

}
