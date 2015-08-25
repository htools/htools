package io.github.htools.lib;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

/**
 * Tools that convert time as measured in seconds after the 1970 epoch. 
 * @author jeroen
 */
public enum EpochTools {

    ;
   public static final Long NULLEPOCH = Long.MIN_VALUE;
   public static final Log log = new Log(EpochTools.class);
   
    public static String toString(long epoch) {
        return DateTools.FORMAT.DATETIME.formatEpoch(epoch);
    }

    public static long get(Calendar c) {
        return (c == null)?NULLEPOCH:c.getTimeInMillis() / 1000;
    }

    public static long get(Date d) {
        return (d == null)?NULLEPOCH:d.getTime() / 1000;
    }

    public static Timestamp toTimestamp(long epoch) {
        return new Timestamp(epoch * 1000);
    }

    public static Date toDate(long epoch) {
        return new Date(epoch * 1000);
    }

    public static Calendar toCalendar(long epoch) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1000 * epoch);
        return calendar;
    }
}
