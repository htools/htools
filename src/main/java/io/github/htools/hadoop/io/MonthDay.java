package io.github.htools.hadoop.io;

import io.github.htools.lib.DateTools;
import io.github.htools.lib.Log;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.WritableComparator;

import java.util.Calendar;

/**
 * Replaces Text in cases where cases need to be preserved, but for comparison
 * and hashing needs to be case less.
 *
 * @author jeroen
 */
public class MonthDay extends LongWritable {
    public static Log log = new Log(MonthDay.class);

    public MonthDay() {
        super();
    }

    public MonthDay(long time) {
        super(time);
    }

    public static class Comparator extends WritableComparator {

        public Comparator() {
            super(MonthDay.class);
        }

        public int compare(byte[] b1, int s1, int l1, byte[] b2, int s2, int l2) {
            return compareBytes(b1, s1, l1, b2, s2, l2);
        }
    }

    static {
        // register this comparator
        WritableComparator.define(MonthDay.class, new Comparator());
    }
    
    public static class Partitioner extends org.apache.hadoop.mapreduce.Partitioner<MonthDay, Object> {

        @Override
        public int getPartition(MonthDay key, Object value, int i) {
            Calendar c = DateTools.epochToCalendar(key.get());
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            return month * 100 + day;
        }
    }

}
