package io.github.repir.tools.Lib;

import io.github.repir.tools.Content.Datafile;
import static io.github.repir.tools.Lib.PrintTools.*;
import java.io.PrintStream;
import java.util.HashMap;

/**
 * Tiny profiler.
 * <p/>
 * @author jeroen
 */
public class Profiler {

    public static Datafile out;
    public static PrintStream systemout = System.out;
    private static HashMap<String, Profile> profiles;

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

    public static void s(String name) {
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.startTime();
        p.count++;
    }

    public void e(String name) {
        Profile p = profiles.get(name);
        if (p == null) {
            p = new Profile(name);
            profiles.put(name, p);
        }
        p.addTime();
    }

    public static void setProfileFile(Datafile file) {
        out = file;
    }

    public final static void reportProfile() {
        for (Profile p : profiles.values()) {
            out("%s( count=%d sec=%f )", p.name, p.count, p.time / 1000.0);
        }
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

        void addTime() {
            time += System.currentTimeMillis() - starttime;
        }
    }
}
