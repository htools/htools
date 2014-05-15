package io.github.repir.tools.Lib;

import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.DataTypes.DateExt;
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
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;

/**
 * Yeah, Yeah, I know, why build your own logger... well for one thing, this can
 * Profile spent cpu startTime of classes over Hadoop :)
 *
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
   private boolean profile;
   private HashMap<String, Profile> profiles;
   private Profile currentprofile;
   static boolean profileall = true;

   public Log(Class clazz) {
      this.clazz = clazz;
      this.messageprefix = clazz.getCanonicalName() + ".";
      checkSettings();
      logs.add(this);
      if (profileall)
         profileOn();
   }

   public static void profileAll() {
      profileall = true;
      for (Log log : logs) {
         log.profileOn();
      }
   }

   public static void profileNone() {
      profileall = false;
      for (Log log : logs) {
         log.profileOff();
      }
   }

   public void profileOn() {
      profile = true;
      profiles = new HashMap<String, Profile>();
   }

   public void profileOff() {
      profile = false;
   }

   public void s(String name) {
      if (profile) {
         Profile p = profiles.get(name);
         if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
         }
         p.startTime();
         p.count++;
         currentprofile = p;
      }
   }

   public void e(String name) {
      if (profile) {
         if (currentprofile.name.equals(name)) {
            currentprofile.addTime();
         } else {
            Profile p = profiles.get(name);
            p.addTime();
         }
      }
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

   public final double getTimePassed() {
      return System.currentTimeMillis() - time;
   }

   public final static void reportProfile() {
      for (int i = 0; i < logs.size(); i++) {
         Log log = logs.get(i);
         if (log.profiles != null) {
            for (Profile p : log.profiles.values()) {
                log.printf("%s%s( count=%d sec=%f )", log.messageprefix, p.name, p.count, p.time / 1000.0);
            }
         }
      }
   }

   static public long getMemoryUsage() {
      Runtime runtime = Runtime.getRuntime();
      return runtime.totalMemory() - runtime.freeMemory();
   }

   static public long getFreeMemory() {
      Runtime runtime = Runtime.getRuntime();
      return runtime.freeMemory();
   }

   static public long getTotalMemory() {
      Runtime runtime = Runtime.getRuntime();
      return runtime.totalMemory();
   }

   static public long getTime() {
      return System.currentTimeMillis();
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

   public void startTime() {
      time = System.currentTimeMillis();
   }

   public void reportTime(String message, Object... obj) {
      printf("%.3f %s", (System.currentTimeMillis() - time) / 1000.0,
              sprintf(message, obj));
   }

   /**
    *
    */
   public void compileTime() {
      try {
         String m = "CompileDate of Class %s" + clazz.getSimpleName();
         URL res = clazz.getResource(clazz.getSimpleName() + ".class");
         URLConnection openConnection = res.openConnection();
         DateExt d = new DateExt(openConnection.getLastModified());
         //Attributes atts = mf.getAttributes("Built-DateExt");
         info(m, ConversionTools.toString(d));
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
