package io.github.repir.tools.Lib;

import static io.github.repir.tools.Lib.Const.*;
import java.io.*;
import java.util.Formatter;
import java.text.SimpleDateFormat;

/**
 * Abstract class for making lazy C-like printf() statements. Put <i>import
 * static Lib.PrintTools.*;</i> in your script and you can printf() whenever you
 * need it.
 * <p/>
 * @author jbpvuurens
 */
public abstract class PrintTools {

   static Log log = new Log(PrintTools.class);
   public static PrintStream out = System.out;

   /**
    * Formats a c-like printf() string with arguments to System,out. See
    * {@link java.util.Formatter#format(java.lang.String, java.lang.Object[])}
    * <p/>
    * @param formatter
    * @param format
    * @param args
    */
   public static void printf(Formatter formatter, String format, Object... args) {
      formatter.format(format, args);
   }

   /**
    * Formats a c-like printf() string with arguments to System,out. See
    * {@link PrintStream#format(java.lang.String, java.lang.Object[])}
    * <p/>
    * @param format
    * @param args
    */
   public static void printf(String format, Object... args) {
      System.out.format(format, args);
   }

   /**
    * prints an array of doubles separated by spaces to System.out
    * <p/>
    * @param d
    */
   public static void printf(double[] d) {
      for (int i = 0; i < d.length; i++) {
         System.out.println(d[i] + " ");
      }
      System.out.println("\n");
   }

   /**
    * See {@link PrintTools#printf(java.lang.String, java.lang.Object[])}.
    * Instead of printing to System.out this returns the formatted String.
    * <p/>
    * @param format
    * @param args
    * @return formatted string
    */
   public static String sprintf(String format, Object... args) {
      return String.format(format, args);
   }
   public static final String NULL = "NULL";
   public static final String PRINTFCODE = "bBhHsScCdoxXeEgGfaAtT%nwN";
   public static final String PRINTFDATETIMEMODIFIER = "HIklMSLNpzZsQbBhaACyYjmderRTDFc";

   /**
    * Correctly formats a querystring with given arguments like C printf( ).
    * This function handles Strings and null-arguments so that a correct sql
    * query is constructed. Any NULL-argument is replaced by NULL. The extra %N
    * parameter translates Strings to '%s', except when they have a NULL value.
    * The extra 'w' modifier is for use in where-clauses, so that comparisons to
    * null are correctly replaced with is null or is not null. <br> <br>input:
    * qprintf("select * from %s where id = %d and city = %Nw", "person", 2,
    * "London") <br>output: "select * from person where id = 2 and city =
    * 'London'" <br> <br>input: qprintf("select %s+1 from person where id = %d
    * and city &lt;&gt; %Nw", "age", null, null) <br>output: "select age+1 from
    * person where id is null and city is not null" <br> <br>input:
    * qprintf("update person set id=%d, city=%N, street='%s', name=%N", 2,
    * "London","Baker st", null) <br>output: "update person set id=2,
    * city='London', street='Baker street', name=null" <br>
    * <p/>
    * @param querystring String containing printf-code like %s and %d that are
    * to be replaced with arguments
    * @param args arguments to be used as printf-codes.
    * @return querystring with inserted atrguments.
    */
   public static String qprintf(String querystring, Object... args) {
      int naiveargnr = -1;
      int compareoperatorpos, codeend, codestart = querystring.indexOf('%');
      boolean[] escapeargs = new boolean[(args == null) ? 0 : args.length];

      while (codestart != -1) {
         for (codeend = codestart + 1; codeend < querystring.length() && PRINTFCODE.indexOf(querystring.charAt(codeend)) == -1; codeend++);
         int codecharstart = codeend++;
         if (codeend < querystring.length() && "tT".indexOf(querystring.charAt(codeend - 1)) != -1 && PRINTFDATETIMEMODIFIER.indexOf(querystring.charAt(codeend)) != -1) {
            codeend++;
         }
         if (codeend < querystring.length() && querystring.charAt(codeend) == 'w') // added modifier for = null to IS NULL, etc.
         {
            codeend++;
         }
         String code = querystring.substring(codestart + 1, codeend);
         String querystring_tail = querystring.substring(codeend);
         if (code.charAt(0) != '%') {      // escape %, means no argument but literal %
            int argnr = naiveargnr;
            if (code.indexOf('<') == -1) { // means use same argument as previous
               int directpos = code.indexOf('$');    // indicate direct argument e.g. 1$ is first argument
               if (directpos == -1) {            // if not direct then (naive) next argument in order is used
                  argnr = ++naiveargnr;
               } else {
                  int tracknumber = directpos - 1;
                  if ("0123456789".indexOf(code.charAt(tracknumber)) == -1) {
                     log.fatal("Lib.Print.qprintf( %s ): illegal direct position in querystring", querystring);
                  } else {
                     for (; tracknumber > 0 && "0123456789".indexOf(code.charAt(tracknumber - 1)) != -1; tracknumber--);
                     argnr = Integer.parseInt(code.substring(tracknumber, directpos)) - 1;
                  }
               }
            }
            if (args == null || argnr > args.length - 1) {
               Object[] newargs = new Object[argnr + 1];
               for (int i = 0; args != null && i < args.length; i++) {
                  newargs[i] = args[i];
               }
               args = newargs;
            }
            if (args[argnr] == null
                    || ((args[argnr] instanceof Integer) && ((Integer) args[argnr]) == NULLINT)
                    || ((args[argnr] instanceof Long) && ((Long) args[argnr]) == NULLINT)) {
               String replacecode = (code.indexOf('<') != -1 || code.indexOf('$') != -1) ? "%" + (argnr + 1) + "$s" : "%s";
               if (code.charAt(code.length() - 1) == 'w') { // replace = null with IS NULL and <> null with IS NOT NULL
                  for (compareoperatorpos = codestart; compareoperatorpos - 1 > 0 && "<>= \t\n\r%".indexOf(querystring.charAt(compareoperatorpos - 1)) != -1; compareoperatorpos--);
                  String compareoperator = querystring.substring(compareoperatorpos, codestart);
                  if (compareoperator.indexOf('=') != -1) {
                     querystring = querystring.substring(0, compareoperatorpos) + " IS " + replacecode + querystring_tail;
                  } else if (compareoperator.indexOf('<') != -1 || compareoperator.indexOf('>') != -1) {
                     querystring = querystring.substring(0, compareoperatorpos) + " IS NOT " + replacecode + querystring_tail;
                  } else {
                     querystring = querystring.substring(0, codestart) + replacecode + querystring_tail;
                  }
               } else {
                  querystring = querystring.substring(0, codestart) + replacecode + querystring_tail;
               }
            } else {
               if (code.charAt(0) == 'N') {
                  querystring = querystring.substring(0, codestart) + "'%s'" + querystring_tail;
                  escapeargs[argnr] = true;
               } else if (code.charAt(0) == 's') {
                  querystring = querystring.substring(0, codestart) + "%s" + querystring_tail;
               } else if (code.charAt(code.length() - 1) == 'w') {
                  querystring = querystring.substring(0, codestart) + "%" + code.substring(0, code.length() - 1) + querystring_tail;
               }
            }
         }
         codestart = querystring.indexOf('%', querystring.length() - querystring_tail.length());
      }
      for (naiveargnr = 0; args != null && naiveargnr < args.length; naiveargnr++) {
         if (args[naiveargnr] == null
                 || ((args[naiveargnr] instanceof Integer) && ((Integer) args[naiveargnr]) == NULLINT)) {
            args[naiveargnr] = NULL;
         } else if (escapeargs[naiveargnr]) {
            args[naiveargnr] = args[naiveargnr].toString().replace("'", "''");
         }
      }
      String result = String.format(querystring, args);
      return result;
   }
}
