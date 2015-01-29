package io.github.repir.tools.lib;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeroen
 */
public enum DateTools {
    

    ;
   public static final Log log = new Log(DateTools.class);
   public enum FORMAT {
       DATETIME("yyyy-MM-dd HH:mm:ss"),
       DATETIMET("yyyy-MM-dd'T'HH:mm:ss'Z'"),
       DATETIMETSZ("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
       DATETIMETSZ6("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'"),
       DATETIMEWIKIPEDIA("HH:mm, dd MMMM yyyy"),
       YMD("yyyyMMdd"),
       Y_M_D("yyyy-MM-dd"),
       Y_M_D_H("yyyy-MM-dd-HH");
       
       public SimpleDateFormat format;
       
       public String format(Date date) {
           return (date == null)?null:format.format(date);
       }
       
       public String format(Calendar calendar) {
           return (calendar == null)?null:format.format(calendar);
       }
       
       public String format(long unixtime) {
           return format.format(unixtime);
       }
       
       public Date parse(String timestring) throws ParseException {
           return format.parse(timestring);
       }
       
       public boolean isValid(String timestring) {
           try {
               format.parse(timestring);
           } catch (ParseException ex) {
               return false;
           }
           return true;
       }
       
       FORMAT(String format) {
           this.format = new SimpleDateFormat(format);
           this.format.setTimeZone(TimeZone.getTimeZone("GMT"));
       }
   }
   
    public static String toString(Date d) {
        return (d == null) ? null : FORMAT.DATETIME.format(d);
    }

    public static String toString(Calendar c) {
        return (c == null) ? null : FORMAT.DATETIME.format(c.getTime());
    }

    public static Timestamp toTimestamp(Calendar c) {
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp toTimestamp(String time) {
        return Timestamp.valueOf(time);
    }

    public static String toString(Timestamp c) {
        return (c == null) ? null : FORMAT.DATETIME.format(c.getTime());
    }

    public static String toString(long timestamp) {
        return FORMAT.DATETIME.format(secToDate(timestamp));
    }

    public static Date secToDate(long timeStamp) {
        return new Date(1000 * timeStamp);
    }

    public static Calendar secToCalendar(long unixTime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1000 * unixTime);
        return calendar;
    }

    public static Calendar toCalendar(Timestamp t) {
        try {
            Calendar cal = Calendar.getInstance();
            String ts = toString(t);
            if (ts != null) {
                cal.setTime(FORMAT.DATETIME.parse(ts));
                return cal;
            }
        } catch (ParseException ex) {
            Logger.getLogger(ConversionTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static Date toDate(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, 0, 0, 0);
        return calendar.getTime();
    }

    public static Calendar toCalendar(int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, 0, 0, 0);
        return calendar;
    }

    public static Calendar toCalendar(int year, int month, int day, int hours, int minutes, int seconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day, hours, minutes, seconds);
        return calendar;
    }

    public static Calendar dateToCalendar(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    public static Date daysAfter(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, i);
        return calendar.getTime();
    }
    
    public static Date hoursAfter(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.HOUR_OF_DAY, i);
        return calendar.getTime();
    }
    
    public static Date daysBefore(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, -i);
        return calendar.getTime();
    }
    
    public static Date hoursBefore(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.HOUR_OF_DAY, -i);
        return calendar.getTime();
    }
    
    public static int diffDays(Date date1, Date date2) {
        return (int)((date1.getTime() - date2.getTime() )/ (24 * 60 * 60 * 1000));
    }
    
    public static String toYMD(Date date) {
        return FORMAT.YMD.format(date);
    }
    
    public static String toY_M_D(Date date) {
        return FORMAT.Y_M_D.format(date);
    }
}
