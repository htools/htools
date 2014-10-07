package io.github.repir.tools.Lib;

import io.github.repir.tools.Content.Datafile;
import static io.github.repir.tools.Lib.PrintTools.*;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Date;

/**
 * Yeah, Yeah, I know, why build your own logger... I suppose I was mostly annoyed by
 * conflicts between different versions of existing loggers by third party libs.
 * And on the bright side, this is a convenient place to put extensions needed by
 * any class, such as a simple profiler to compute spent cpu time running Hadoop jobs.
 * <p/>
 * To use this logger, a class should create one as "final public static Log log = new Log(class)"
 * Each class specific logger then has a report level, that can be configured with setLevel
 * using a value TRACE, DEBUG, INFO, WARN, ERROR, FATAL or NONE. A universal minimal report level
 * is controlled by Log.setGeneralLevel(), by default this is set to INFO.
 * <p/>
 * The logger can also collect time used between log.s(label) and log.e(label) markers.
 * The collected results can be dumped with log.reportProfile().
 * <p/>
 * @author jeroen
 */
public class Log {

   final public static int TRACE = 0;
   final public static int DEBUG = 1;
   final public static int INFO = 2;
   final public static int WARN = 3;
   final public static int ERROR = 4;
   final public static int FATAL = 5;
   final public static int NONE = 6;
   final public static int GENERALLEVEL = 7;
   public static Datafile out;
   public static String prefix = "<p>";
   public static PrintStream err = System.err;
   private static HashMap<Class, Integer> hsettings = new HashMap<Class, Integer>();
   private static HashMap<Class, Integer> settings = new HashMap<Class, Integer>();
   private static ArrayList<Log> logs = new ArrayList<Log>();
   private String messageprefix;
   private Class clazz;
   private Integer level = GENERALLEVEL;
   private long time;
   private static int generallevel = INFO;

   public Log(Class clazz) {
      this.clazz = clazz;
      this.messageprefix = clazz.getCanonicalName() + ".";
      checkSettings();
      logs.add(this);
   }
   
   public Class getLoggedClass() {
       return clazz;
   }

   public void sleepRnd(int max) {
      try {
         Thread.sleep(RandomTools.getInt(max));
      } catch (InterruptedException ex) {
         log.exception(ex, "");
      }
   }
   
   /**
    * Waits until #sleep milliseconds have passed
    */
   public void sleep(int sleep) {
      try {
         Thread.sleep(sleep);
      } catch (InterruptedException ex) {
         log.exception(ex, "");
      }
   }
   
   public static void setLogFile(Datafile file) {
      out = file;
   }

   public final void checkSettings() {
      for (Map.Entry< Class, Integer> entry : hsettings.entrySet()) {
         if (entry.getKey().isAssignableFrom(clazz)) {
            level = entry.getValue();
         }
      }
      for (Map.Entry< Class, Integer> entry : settings.entrySet()) {
         if (entry.getKey().equals(clazz)) {
            level = entry.getValue();
         }
      }
   }

   /**
    *
    * @param clazz
    * @param level
    */
   public static void setLevelHierarchy(Class clazz, int level) {
      for (Log log : logs) {
         if (clazz.isAssignableFrom(log.clazz)) {
            log.level = level;
         }
      }
      hsettings.put(clazz, level);
   }

   /**
    *
    * @param clazz
    * @param level
    */
   public static void setLevel(Class clazz, int level) {
      for (Log log : logs) {
         if (clazz.equals(log.clazz)) {
            log.level = level;
         }
      }
      settings.put(clazz, level);
   }

   public static void setGeneralLevel(int level) {
      generallevel = level;
   }

   /**
    *
    * @param level
    */
   public void setLevelHierarchy(int level) {
      setLevelHierarchy(this.clazz, level);
   }

   /**
    *
    * @param level
    */
   public void setLevel(int level) {
      setLevel(this.clazz, level);
   }

   /**
    *
    */
   public void compileTime() {
      try {
         String m = "CompileDate of Class %s" + clazz.getSimpleName();
         URL res = clazz.getResource(clazz.getSimpleName() + ".class");
         URLConnection openConnection = res.openConnection();
         Date d = new Date(openConnection.getLastModified());
         //Attributes atts = mf.getAttributes("Built-DateExt");
         info(m, DateTimeTools.toString(d));
      } catch (IOException ex) {
         Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
      }
   }

   /**
    *
    * @return
    */
   public boolean isDebugEnabled() {
      return level <= DEBUG || (level == GENERALLEVEL && generallevel <= DEBUG);
   }

   /**
    *
    * @return
    */
   public boolean isErrorEnabled() {
      return level <= ERROR || (level == GENERALLEVEL && generallevel <= ERROR);
   }

   /**
    *
    * @return
    */
   public boolean isFatalEnabled() {
      return level <= FATAL || (level == GENERALLEVEL && generallevel <= FATAL);
   }

   /**
    *
    * @return
    */
   public boolean isInfoEnabled() {
      return level <= INFO || (level == GENERALLEVEL && generallevel <= INFO);
   }

   /**
    *
    * @return
    */
   public boolean isTraceEnabled() {
      return level <= TRACE || (level == GENERALLEVEL && generallevel <= TRACE);
   }

   /**
    *
    * @return
    */
   public boolean isWarnEnabled() {
      return level <= WARN || (level == GENERALLEVEL && generallevel <= WARN);
   }

   protected static void out(String s, Object... args) {
      if (out == null) {
         System.out.println(sprintf(s, args));
      } else {
         out.printf(s + "\n", args);
         out.flush();
      }
   }

   public static void print(String s) {
      if (out == null) {
         System.out.println(s);
      } else {
         out.printf(s + "\n");
         out.flush();
      }
   }

   protected static void err(String s, Object... args) {
      if (out == null) {
         System.err.println(sprintf(s, args));
      } else {
         out.printf(s + "\n", args);
         out.flush();
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void trace(String message, Object... args) {
      trace(sprintf(message, args));
   }

   /**
    *
    * @param message
    */
   public void trace(Object message) {
      if (isTraceEnabled()) {
         print(prefix + "TRACE " + messageprefix + message);
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void debug(String message, Object... args) {
      if (isDebugEnabled()) {
         debug(sprintf(message, args));
      }
   }

   /**
    *
    * @param message
    */
   public void debug(Object message) {
      if (isDebugEnabled()) {
         print(prefix + "DEBUG " + messageprefix + message);
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void info(String message, Object... args) {
      info(sprintf(message, args));
   }

   public void printf(String message, Object... args) {
      print(sprintf(message, args));
   }

   /**
    *
    * @param message
    */
   public void info(Object message) {
      if (isInfoEnabled()) {
         print(prefix + "INFO " + messageprefix + message);
      }
   }

   public void binary(String message, byte[] arg, Object... args) {
      if (isInfoEnabled()) {
         print(prefix + "BINARY " + messageprefix + sprintf(message, args));
         StringBuilder sb = new StringBuilder();
         for (int i = 0; arg != null && i < arg.length; i++) {
            sb.append(sprintf("%s%3d ", ((i % 10) == 0) ? "\n" : "", arg[i] & 0xFF));
         }
         print(sb.toString());
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void warn(String message, Object... args) {
      warn(sprintf(message, args));
   }

   /**
    *
    * @param message
    */
   public void warn(Object message) {
      if (isWarnEnabled()) {
         err(prefix + "WARN " + messageprefix + message);
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void error(String message, Object... args) {
      error(sprintf(message, args));
   }

   /**
    *
    * @param message
    */
   public void error(Object message) {
      if (isErrorEnabled()) {
         err(prefix + "ERROR " + messageprefix + message);
      }
   }

   /**
    *
    * @param message
    * @param args
    */
   public void fatal(String message, Object... args) {
      fatal(sprintf(message, args));
   }

   /**
    *
    * @param message
    */
   public void fatal(Object message) {
      if (isFatalEnabled()) {
         err(prefix + "LOG FATAL ERROR " + messageprefix + message);
         err(getCustomStackTrace(Thread.currentThread().getStackTrace()));
         System.exit(1);
      } else {
         info("FATAL DISABLED: " + messageprefix + message);
      }
   }

   public void printStackTrace() {
      info(getCustomStackTrace(Thread.currentThread().getStackTrace()));
   }

   public static void staticfatal(String message, Object... args) {
      err(sprintf(prefix + "LOG FATAL ERROR " + message, args));
      err(getCustomStackTrace(Thread.currentThread().getStackTrace()));
      if (out != null) {
         out.close();
      }
      System.exit(1);
   }

   public static String getCustomStackTrace(StackTraceElement stack[]) {
      final StringBuilder result = new StringBuilder();
      for (StackTraceElement element : stack) {
         result.append(element);
         result.append("\n");
      }
      return result.toString();
   }

   public void crash() {
      err(getCustomStackTrace(Thread.currentThread().getStackTrace()));
      if (out != null) {
         out.close();
      }
      System.exit(1);
   }

   public void exit(String format, Object... o) {
      this.printf(format, o);
      System.exit(1);
   }

   public void exit() {
      System.exit(1);
   }

   public void crash(String message, Object... params) {
      printf(message, params);
      log.crash();
   }

   public void fatalexception(Exception e, String why, Object... params) {
      if (isFatalEnabled()) {
         exception(e, why, params);
         System.exit(1);
      } else {
         exception(e, "FATAL DISABLED: " + why, params);
      }
   }

   public void exception(Exception e, String why, Object... params) {
      err(why, params);
      err(prefix + "EXCEPTION %s", e.getMessage());
      err(getCustomStackTrace(e.getStackTrace()));
   }

   class Profile {

      int count = 0;
      long time = 0;
      long starttime = 0;
      final String name;

      Profile(String name) {
         this.name = name;
      }

      void startTime() {
         starttime = System.currentTimeMillis();
      }

      void addTime() {
         time += System.currentTimeMillis() - starttime;
      }
   }
}
