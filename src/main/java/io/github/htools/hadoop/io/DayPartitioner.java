package io.github.htools.hadoop.io;

import io.github.htools.lib.DateTools;
import io.github.htools.lib.Log;
import java.text.ParseException;
import java.util.Date;
import org.apache.hadoop.conf.Configurable;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Partitioner;

/**
 *
 * @author jeroen
 */
public class DayPartitioner extends Partitioner<LongWritable, Object> implements Configurable {

    public static Log log = new Log(DayPartitioner.class);
    private static final String starttimelabel = "daypartitioner.starttime";
    private static final String endtimelabel = "daypartitioner.endtime";
    static Configuration conf;
    static long startdate;
    static private final long msperday = 24 * 60 * 60 * 1000;
    static private final long secperday = 24 * 60 * 60;

    @Override
    public int getPartition(LongWritable key, Object value, int numPartitions) {
        return (int) ((key.get() - startdate) / secperday);
    }

    @Override
    public void setConf(Configuration conf) {
        this.conf = conf;
        startdate = conf.getLong(starttimelabel, 0);
    }

    @Override
    public Configuration getConf() {
        return conf;
    }

    public static int getPartition(Configuration conf, String date) throws ParseException {
        long now = DateTools.FORMAT.Y_M_D.toDate(date).getTime() / 1000;
        if (validDate(conf, now)) {
            startdate = conf.getLong(starttimelabel, 0);
            return (int) ((now - startdate) / secperday);
        } else {
            log.info("invalid date %s", date);
            return -1;
        }
    }
    
    public static void setTime(Configuration conf, String start, String end) throws ParseException {
        setStartTime(conf, start);
        setEndTime(conf, end);
    }

    public static void setStartTime(Configuration conf, String date) throws ParseException {
        Date start = DateTools.FORMAT.Y_M_D.toDate(date);
        conf.setLong(starttimelabel, start.getTime() / 1000);
    }

    public static void setEndTime(Configuration conf, String date) throws ParseException {
        Date end = DateTools.FORMAT.Y_M_D.toDate(date);
        conf.setLong(endtimelabel, end.getTime() / 1000);
    }

    public static int getNumberOfReducers(Configuration conf) throws ParseException {
        long start = conf.getLong(starttimelabel, 0);
        long end = conf.getLong(endtimelabel, 0);
        int days = 1 + (int) ((end - start) / (secperday));
        log.info("%d %d %d", start, end, days);
        return days;
    }
    
    public static boolean validDate(Configuration conf, long date) {
        long start = conf.getLong(starttimelabel, 0);
        long end = conf.getLong(endtimelabel, 0);
        log.info("validDate %d %d %d %b", start, date, end + secperday, (start < date && date < end + secperday));
        return (start < date && date < end + secperday);
    }
    
    public static String getDate(Configuration conf, int reducer) {
        long start = conf.getLong(starttimelabel, 0) + reducer * secperday;
        return DateTools.FORMAT.Y_M_D.formatEpoch(start); 
    }

}
