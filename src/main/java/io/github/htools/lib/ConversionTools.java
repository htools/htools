package io.github.htools.lib;

import java.sql.Timestamp;
import java.util.StringTokenizer;

/**
 *
 * @author jeroen
 */
public enum ConversionTools {;

   /**
    * @param s the string to parse for the double value
    * @return
    * @throws IllegalArgumentException if s is empty or represents NaN or
    * Infinity
    * @throws NumberFormatException see {@link Double#parseDouble( String )}
    */
   public static double atof(String s) {
      if (s == null || s.length() < 1) {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
      double d = Double.parseDouble(s);
      if (Double.isNaN(d) || Double.isInfinite(d)) {
         throw new IllegalArgumentException("NaN or Infinity in input: " + s);
      }
      return (d);
   }

   /**
    * @param s the string to parse for the integer value
    * @return
    * @throws IllegalArgumentException if s is empty
    * @throws NumberFormatException see {@link Integer#parseInt( String )}
    */
   public static int atoi(String s) throws NumberFormatException {
      if (s == null || s.length() < 1) {
         throw new IllegalArgumentException("Can't convert empty string to integer");
      }
      // Integer.parseInt doesn't accept '+' prefixed strings
      if (s.charAt(0) == '+') {
         s = s.substring(1);
      }
      return Integer.parseInt(s);
   }

   /**
    *
    * @param s
    * @return
    */
   public static int[] convertStringInts(String s) {
      StringTokenizer st = new StringTokenizer(s, " ");
      int[] r = new int[st.countTokens()];
      for (int token = 0; st.hasMoreTokens(); token++) {
         r[token] = atoi(st.nextToken());
      }
      return r;
   }

   /**
    *
    * @param s
    * @return
    */
   public static double[] convertStringDoubles(String s) {
      StringTokenizer st = new StringTokenizer(s, " ");
      double[] r = new double[st.countTokens()];
      for (int token = 0; st.hasMoreTokens(); token++) {
         r[token] = atof(st.nextToken());
      }
      return r;
   }

   /**
    *
    * @param doubles
    * @return
    */
   public static String convertDoublesString(double[] doubles) {
      String r = "";
      for (int d = 0; d < doubles.length; d++) {
         r = r + " " + doubles[d];
      }
      return r.substring(1);
   }

   /**
    *
    * @param ints
    * @return
    */
   public static String convertIntsString(int[] ints) {
      String r = "";
      for (int d = 0; d < ints.length; d++) {
         r = r + " " + ints[d];
      }
      return r.substring(1);
   }

   /**
    *
    * @param s
    * @return
    */
   public static final String objectsToString(Object... s) {
      return objectsToString(", ", s);
   }

   /**
    *
    * @param delimiter
    * @param s
    * @return
    */
   public static final String objectsToString(String delimiter, Object... s) {
      String out = "";
      for (int i = 0; i < s.length; i++) {
         if (s[i] != null) {
            if (s[i] instanceof String) {
               out += delimiter + '"' + s[i] + '"';
            } else {
               out += delimiter + s[i];
            }
         } else {
            out += delimiter + "<null>";
         }
      }
      return (out.length() == 0) ? out : out.substring(delimiter.length());
   }

   /**
    *
    * @param c
    * @param o
    * @return
    */
   public static String objectToString(Class c, Object o) {
      if (o == null) {
         return "";
      } else if (c.getName().equals("java.lang.String") || o instanceof String) {
         return (String) o;
      } else if (c.getName() == "int" || c.getName() == "java.lang.Integer" || c.getName() == "java.lang.Long") {
         return o.toString();
      } else if (c.getName() == "java.sql.Timestamp") {
         return DateTools.toString((Timestamp) o);
      } else if (c.getCanonicalName().equals("byte[]")) {
         byte[] b = (byte[]) o;
         return new String(b);
      }
      return null;
   }
}
