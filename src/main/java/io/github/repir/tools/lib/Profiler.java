package io.github.repir.tools.lib;

import io.github.repir.tools.io.Datafile;
import static io.github.repir.tools.lib.PrintTools.*;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * Tiny profiler.
 * <p/>
 * @author jeroen
 */
public enum Profiler {;

    public static Datafile out;
    public static PrintStream systemout = System.out;
    private static boolean trace;
    private static HashMap<String, Profile> profiles = new HashMap();
    

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

    public static void startTime(String name) {
        if (trace) {
           out("startTime %s", name);
        }
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.startTime();
        p.count++;
    }

    public static void addTime(String name) {
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.addTime();
        if (trace)
            reportProfile();
    }

    public static void addCounter(String name) {
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.addCount(1);
    }

    public static void addCounter(String name, int count) {
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.addCount(count);
    }

    public static long timePassed(String name) {
        Profile p = profiles.get(name);
        if (p != null) {
            return p.timePassed();
        }
        return 0;
    }

    public static long getCount(String name) {
        Profile p = profiles.get(name);
        if (p != null) {
            return p.getCount();
        }
        return 0;
    }

    public static void setProfileFile(Datafile file) {
        out = file;
    }

    public final static void reportProfile() {
        for (Profile p : profiles.values()) {
            out("%s( count=%d sec=%f )", p.name, p.count, p.time / 1000.0);
        }
    }

    public final static String reportProfileString() {
        StringBuilder sb = new StringBuilder();
        for (Profile p : profiles.values()) {
            sb.append(sprintf("%s( count=%d sec=%f )\n", p.name, p.count, p.time / 1000.0));
        }
        return sb.toString();
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

    static class Profile {

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

        void addCount(int a) {
            count += a;
        }

        int getCount() {
            return count;
        }

        void addTime() {
            time += System.currentTimeMillis() - starttime;
        }
        
        long timePassed() {
            return System.currentTimeMillis() - starttime;
        }
    }
}
