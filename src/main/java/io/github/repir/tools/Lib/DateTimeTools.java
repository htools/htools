/*
 * Copyright 2014 jeroen.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.repir.tools.Lib;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jeroen
 */
public enum DateTimeTools {

    ;
   public static final Log log = new Log(DateTimeTools.class);
   public enum FORMATTER {
       DATETIME("yyyy-MM-dd HH:mm:ss"),
       DATETIMET("yyyy-MM-dd'T'HH:mm:ss'Z'"),
       DATETIMETSZ("yyyy-MM-dd'T'HH:mm:ss.SSSZ"),
       YMD("yyyyMMdd"),
       Y_M_D("yyyy-MM-dd");
       
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
       
       FORMATTER(String format) {
           this.format = new SimpleDateFormat(format);
       }
   }

    public static String toString(Date d) {
        return (d == null) ? null : FORMATTER.DATETIME.format(d);
    }

    public static String toString(Calendar c) {
        return (c == null) ? null : FORMATTER.DATETIME.format(c.getTime());
    }

    public static Timestamp toTimestamp(Calendar c) {
        return new Timestamp(c.getTimeInMillis());
    }

    public static Timestamp toTimestamp(String time) {
        return Timestamp.valueOf(time);
    }

    public static String toString(Timestamp c) {
        return (c == null) ? null : FORMATTER.DATETIME.format(c.getTime());
    }

    public static String toString(long timestamp) {
        return FORMATTER.DATETIME.format(toDate(timestamp));
    }

    public static Date toDate(long timeStamp) {
        return new Date(1000 * timeStamp);
    }

    public static Calendar toCalendar(Timestamp t) {
        try {
            Calendar cal = Calendar.getInstance();
            String ts = toString(t);
            if (ts != null) {
                cal.setTime(FORMATTER.DATETIME.parse(ts));
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
    
    public static Date daysBefore(Date date, int i) {
        Calendar calendar = dateToCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, -i);
        return calendar.getTime();
    }
    
    public static String toYMD(Date date) {
        return FORMATTER.YMD.format(date);
    }
    
    public static String toY_M_D(Date date) {
        return FORMATTER.Y_M_D.format(date);
    }
}
