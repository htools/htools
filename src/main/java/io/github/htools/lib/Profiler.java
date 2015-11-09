package io.github.htools.lib;

import io.github.htools.io.Datafile;
import static io.github.htools.lib.PrintTools.*;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * Tiny profiler.
 * <p>
 * @author jeroen
 */
public class Profiler {

    public static Datafile out;
    public static PrintStream systemout = System.out;
    private static boolean trace;
    private static HashMap<String, Profiler> profiles = new HashMap();
    private static long startTime = System.currentTimeMillis();
    

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
    
    public static void setTraceOn() {
        trace = true;
    }

    public static void setTraceOff() {
        trace = false;
    }

    public static Profiler getProfiler(String name) {
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        return p;
    }
    
    public static void startTime(String name) {
        if (trace) {
           out("startTime %s", name);
        }
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        p.startTime();
        p.count++;
    }

    public static void addTime(String name) {
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        p.addTime();
        if (trace)
            reportProfile();
    }

    public static void addAvgTime(String name) {
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        p.addAvgTime();
        if (trace)
            reportProfile();
    }

    public static void addCounter(String name) {
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        p.addCount(1);
    }

    public static void addCounter(String name, int count) {
        Profiler p = profiles.get(name);
        if (p == null) {
            p = new Profiler(name);
            profiles.put(name, p);
        }
        p.addCount(count);
    }

    public static double totalTimeSeconds(String name) {
        Profiler p = profiles.get(name);
        if (p != null) {
            return p.getTotalTimeSeconds();
        }
        return 0;
    }

    public static long totalTimeMs(String name) {
        Profiler p = profiles.get(name);
        if (p != null) {
            return p.getTotalTimeMs();
        }
        return 0;
    }

    public static double totalAvgTimeSeconds(String name) {
        Profiler p = profiles.get(name);
        if (p != null) {
            return p.getAvgTimeSeconds();
        }
        return 0;
    }

    public static long getCount(String name) {
        Profiler p = profiles.get(name);
        if (p != null) {
            return p.getCount();
        }
        return 0;
    }

    public static void setProfileFile(Datafile file) {
        out = file;
    }

    public final static void reportProfile() {
        for (Profiler p : profiles.values()) {
            out("%s( count=%d sec=%f )", p.name, p.count, p.time / 1000.0);
        }
    }

    public final static String reportProfileString() {
        StringBuilder sb = new StringBuilder();
        for (Profiler p : profiles.values()) {
            sb.append(sprintf("%s( count=%d sec=%f )\n", p.name, p.count, p.time / 1000.0));
        }
        return sb.toString();
    }

    protected static void out(String s, Object... args) {
        if (out == null) {
            systemout.println(sprintf(s, args));
        } else {
            try {
                out.printf(s + "\n", args);
                out.flush();
            } catch (IOException ex) {
                log.exception(ex, "out " + s, args);
            }
        }
    }

    public static void print(String s) {
        if (out == null) {
            systemout.println(s);
        } else {
            try {
                out.printf(s + "\n");
                out.flush();
            } catch (IOException ex) {
                log.exception(ex, "printf %s", s);
            }
        }
    }

    public static void printf(String message, Object... args) {
        print(System.currentTimeMillis() - startTime + " " + sprintf(message, args));
    }

        int count = 0;
        long time = 0;
        long starttime = 0;
        final String name;

        Profiler(String name) {
            this.name = name;
        }

        public void startTime() {
            starttime = System.currentTimeMillis();
        }

        public void addCount(int a) {
            count += a;
        }

        public int getCount() {
            return count;
        }

        public void addTime() {
            time += System.currentTimeMillis() - starttime;
        }
        
        public void addAvgTime() {
            time += System.currentTimeMillis() - starttime;
            count++;
        }
        
        public double getTotalTimeSeconds() {
            return time / 1000.0;
        }
        
        public long getTotalTimeMs() {
            return time;
        }
        
        public double getAvgTimeSeconds() {
            return time / (count * 1000.0);
        }
        
        public long timePassed() {
            return System.currentTimeMillis() - starttime;
        }
}
