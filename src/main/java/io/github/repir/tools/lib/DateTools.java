package io.github.repir.tools.lib;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static java.util.regex.Pattern.CASE_INSENSITIVE;

/**
 *
 * @author jeroen
 */
public enum DateTools {
    

    ;
   public static final Log log = new Log(DateTools.class);
   public enum FORMAT {
       YMD("yyyyMMdd", "^\\d{8}$"),
       DATETIME("yyyy-MM-dd HH:mm:ss", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}$"),
       DATETIMET("yyyy-MM-dd'T'HH:mm:ss'Z'", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}Z$"),
       DATETIMETSZ("yyyy-MM-dd'T'HH:mm:ss.SSSZ", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}\\.\\d{3}Z$"),
       DATETIMETSZ6("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}T\\d{1,2}:\\d{2}:\\d{2}\\.\\d{6}Z$"),
       DATETIMEWIKIPEDIA("HH:mm, dd MMMM yyyy", "^\\d{1,2}:\\d{2}(\\s|,\\s|,)\\d{1,2}(-|/|\\s)[a-z]{4,}(-|/|\\s)\\d{4}$"),
       Y_M_D("yyyy-MM-dd", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}$"),
       Y_M_D_H("yyyy-MM-dd-HH", "^\\d{4}-\\d{1,2}-\\d{1,2}-\\d{1,2}$"),
       D_M_Y("dd-MM-yyyy", "^\\d{1,2}(-|\\s)\\d{1,2}(-|\\s)\\d{4}$"),
       M_D_Y("MM/dd/yyyy", "^\\d{1,2}/\\d{1,2}/\\d{4}$"),
       D_MON_Y("dd MMM yyyy", "^\\d{1,2}(-|/|\\s)[a-z]{3}(-|/|\\s)\\d{4}$"),
       D_MONTH_Y("dd MMMM yyyy", "^\\d{1,2}(-|/|\\s)[a-z]{4,}(-|/|\\s)\\d{4}$"),
       YMDHM("yyyyMMddHHmm", "^\\d{12}$"),
       HMSM("HH:mm:ss.SSS", "^\\d{12}:\\d{2}:\\d{2}\\.:\\d{3}$"),
       YMD_HM("yyyyMMdd HHmm", "^\\d{8}\\s\\d{4}$"),
       YMD_H_M("yyyyMMdd HH:mm", "^\\d{8}\\s\\d{1,2}:\\d{2}$"),
       DATEHM("dd-MM-yyyy HH:mm", "^\\d{1,2}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{4}\\s\\d{1,2}:\\d{2}$"),
       Y_M_D_H_M("yyyy-MM-dd HH:mm", "^\\d{4}(-|/|\\s)\\d{1,2}(-|/|\\s)\\d{1,2}\\s\\d{1,2}:\\d{2}$"),
       M_D_Y_H_M("MM/dd/yyyy HH:mm", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}$"),
       D_MON_Y_H_M("dd MMM yyyy HH:mm", "^\\d{1,2}(-|/|\\s)[a-z]{3}(-|/|\\s)\\d{4}\\s\\d{1,2}:\\d{2}$"),
       D_MONTH_Y_H_M("dd MMMM yyyy HH:mm", "^\\d{1,2}(-|/|\\s)[a-z]{4,}(-|/|\\s)\\d{4}\\s\\d{1,2}:\\d{2}$"),
       YMDHMS("yyyyMMddHHmmss", "^\\d{14}$"),
       YMD_HMS("yyyyMMdd HHmmss", "^\\d{8}\\s\\d{6}$"),
       D_M_Y_H_M_S("dd-MM-yyyy HH:mm:ss", "^\\d{1,2}(-|\\s)\\d{1,2}(-|\\s)\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"),
       M_D_Y_H_M_S("MM/dd/yyyy HH:mm:ss", "^\\d{1,2}/\\d{1,2}/\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"),
       D_MON_Y_H_M_S("dd MMM yyyy HH:mm:ss", "^\\d{1,2}(-|\\s|/)[a-z]{3}(-|\\s|/)\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"),
       D_MONTH_Y_H_M_S("dd MMMM yyyy HH:mm:ss", "^\\d{1,2}(-|\\s|/)[a-z]{4,}(-|\\s|/)\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}$"),
       E_MON_D_H_M_S_T_Y("EEE MMM dd HH:mm:ss z yyyy", "^[a-z]{3}\\s[a-z]{3}\\s\\d{1,2}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[a-z]{3}\\s\\d{4}$"),
       E_D_MON_Y_H_M_S_Z("EEE, dd MMM yyyy HH:mm:ss z", "^[a-z]{3},\\s\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s(\\-|\\+)?\\d{3,4}$"),
       E_D_MON_Y_H_M_S_UT("EEE, dd MMM yyyy HH:mm:ss", "^[a-z]{3},\\s\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\sUT$"),
       E_D_MON_Y_H_M_S_SAST("EEE, dd MMM yyyy HH:mm:ss", "^[a-z]{3},\\s\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\sSAST$", "GMT+2"),
       E_D_MON_Y_H_M_S_T("EEE, dd MMM yyyy HH:mm:ss zzz", "^[a-z]{3},\\s\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}:\\d{2}\\s[a-z]{3}$"),
       E_D_MON_Y_H_M_T("EEE, dd MMM yyyy HH:mm zzz", "^[a-z]{3},\\s\\d{1,2}\\s[a-z]{3}\\s\\d{4}\\s\\d{1,2}:\\d{2}\\s[a-z]{3}$");

       public SimpleDateFormat format;
       private String regex;
       private Pattern pattern; 
       
       public String format(Date date) {
           return (date == null)?null:format.format(date);
       }
       
       public String format(Calendar calendar) {
           return (calendar == null)?null:format.format(calendar);
       }
       
       public String formatEpoch(long epoch) {
           return formatMS(epoch * 1000);
       }
       
       public String formatMS(long ms) {
           return format.format(new Date(ms));
       }
       
       public Date toDate(String timestring) throws ParseException {
           return format.parse(timestring);
       }
       
       public long toEpoch(String timestring) throws ParseException {
           return format.parse(timestring).getTime() / 1000;
       }
       
       public long toMS(String timestring) throws ParseException {
           return format.parse(timestring).getTime();
       }
       
       public boolean isValid(String timestring) {
           try {
               format.parse(timestring);
           } catch (ParseException ex) {
               return false;
           }
           return true;
       }
       
       private Pattern getPattern() {
           if (pattern == null)
               pattern = Pattern.compile(regex, CASE_INSENSITIVE);
           return pattern;
       }
       
       FORMAT(String format, String regex) {
           this(format, regex, "GMT");
       }
       
       FORMAT(String format, String regex, String timezone) {
           this.format = new SimpleDateFormat(format);
           this.format.setTimeZone(TimeZone.getTimeZone(timezone));
           this.regex = regex;
       }
   }
   
    public static FORMAT determineDateFormat(String dateString) {
        for (FORMAT format : FORMAT.values()) {
            Matcher matcher = format.getPattern().matcher(dateString);
            if (matcher.matches()) {
                return format;
            }
        }
        return null; // Unknown format.
    }
   
    public static Date parse(String dateString) throws ParseException {
        FORMAT dateFormat = determineDateFormat(dateString);
        if (dateFormat == null) {
            throw new ParseException("Unknown date format " + dateString, 0);
        }
        return dateFormat.toDate(dateString);
    }
    
    public static long parseToEpoch(String dateString) throws ParseException {
        FORMAT dateFormat = determineDateFormat(dateString);
        if (dateFormat == null) {
            throw new ParseException("Unknown date format.", 0);
        }
        return dateFormat.toEpoch(dateString);
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

    public static String msToString(long unixtime) {
        return FORMAT.HMSM.formatMS(unixtime);
    }

    public static String msToString() {
        return msToString(System.currentTimeMillis());
    }

    public static Timestamp toTimestamp(String time) {
        return Timestamp.valueOf(time);
    }

    public static String toString(Timestamp c) {
        return (c == null) ? null : FORMAT.DATETIME.formatMS(c.getTime());
    }

    public static String epochToString(long epoch) {
        return FORMAT.DATETIME.format(epochToDate(epoch));
    }

    public static long epoch() {
        return System.currentTimeMillis() / 1000;
    }

    public static Date epochToDate(long epoch) {
        return new Date(1000 * epoch);
    }

    public static Calendar epochToCalendar(long epoch) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(1000 * epoch);
        return calendar;
    }

    public static Calendar toCalendar(Timestamp t) {
        try {
            Calendar cal = Calendar.getInstance();
            String ts = toString(t);
            if (ts != null) {
                cal.setTime(FORMAT.DATETIME.toDate(ts));
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
    
    public static Date yearsBefore(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.YEAR, -i);
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
