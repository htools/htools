package io.github.repir.tools.DataTypes;

import io.github.repir.tools.Lib.ConversionTools;
import java.util.Calendar;

/**
 *
 * @author jeroen
 */
public class DateExt extends java.util.Date {

   /**
    *
    */
   public DateExt() { // current date-time
      super();
   }

   /**
    *
    * @param timeInMilliSeconds
    */
   public DateExt(long timeInMilliSeconds) {
      super(timeInMilliSeconds);
   }

   /**
    *
    * @param seconds
    * @return
    */
   public DateExt addSeconds(int seconds) {
      Calendar cal = Calendar.getInstance();
      cal.setTime(this);
      cal.add(Calendar.SECOND, seconds);
      return new DateExt(cal.getTimeInMillis());
   }

   /**
    *
    * @param seconds
    * @return
    */
   public boolean after(int seconds) {
      return new DateExt().addSeconds(-seconds).after(this);
   }

   @Override
   public String toString() {
      return ConversionTools.dateformatter.format(this);
   }
}
