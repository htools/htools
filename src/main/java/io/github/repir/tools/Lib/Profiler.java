package io.github.repir.tools.Lib;

import io.github.repir.tools.Content.Datafile;
import static io.github.repir.tools.Lib.PrintTools.*;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tiny profiler.
 * <p/>
 * @author jeroen
 */
public class Profiler {

   public static Datafile out;
   public static PrintStream systemout = System.out;
   private static ArrayList<Profiler> profilers = new ArrayList<Profiler>();
   private long time;
   private Class clazz;
   private HashMap<String, Profile> profiles;
   private Profile currentprofile;

   public Profiler(Class clazz) {
      this.clazz = clazz;
      profilers.add(this);
   }

   public static int availableProcessor() {
       return Runtime.getRuntime().availableProcessors();
   }

    public static long getFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    static final long maxMemory = Runtime.getRuntime().maxMemory();

    public static long getTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

   
   public void s(String name) {
         Profile p = profiles.get(name);
         if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
         }
         p.startTime();
         p.count++;
         currentprofile = p;
   }

   public void e(String name) {
         if (currentprofile.name.equals(name)) {
            currentprofile.addTime();
         } else {
            Profile p = profiles.get(name);
            p.addTime();
         }
   }
   
   public static void setProfileFile(Datafile file) {
      out = file;
   }


   public final double getTimePassed() {
      return System.currentTimeMillis() - time;
   }

   public final static void reportProfile() {
      for (int i = 0; i < profilers.size(); i++) {
         Profiler log = profilers.get(i);
         if (log.profiles != null) {
            for (Profile p : log.profiles.values()) {
                log.printf("%s( count=%d sec=%f )", p.name, p.count, p.time / 1000.0);
            }
         }
      }
   }

   static public long getTime() {
      return System.currentTimeMillis();
   }

   public void startTime() {
      time = System.currentTimeMillis();
   }

   public void reportTime(String message, Object... obj) {
      printf("%.3f %s", (System.currentTimeMillis() - time) / 1000.0,
              sprintf(message, obj));
   }

   protected static void out(String s, Object... args) {
      if (out == null) {
         systemout.println(sprintf(s, args));
      } else {
         out.printf(s + "\n", args);
         out.flush();
      }
   }

   public static void print(String s) {
      if (out == null) {
         systemout.println(s);
      } else {
         out.printf(s + "\n");
         out.flush();
      }
   }

   public void printf(String message, Object... args) {
      print(sprintf(message, args));
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
