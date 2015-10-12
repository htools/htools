package io.github.htools.lib;

import io.github.htools.io.Datafile;
import static io.github.htools.lib.PrintTools.*;
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
 * Yeah, Yeah, I know, why build your own logger... I suppose I was mostly
 * annoyed by conflicts between different versions of existing loggers by third
 * party libs. And on the bright side, this is a convenient place to put
 * extensions needed by any class, such as a simple profiler to compute spent
 * cpu time running Hadoop jobs.
 * <p>
 * To use this logger, a class should create one as "final public static Log log
 * = new Log(class)" Each class specific logger then has a report level, that
 * can be configured with setLevel using a value TRACE, DEBUG, INFO, WARN,
 * ERROR, FATAL or NONE. A universal minimal report level is controlled by
 * Log.setGeneralLevel(), by default this is set to INFO.
 * <p>
 * The logger can also collect time used between log.s(label) and log.e(label)
 * markers. The collected results can be dumped with log.reportProfile().
 * <p>
 * @author jeroen
 */
public class Log {

    public static Datafile out;
    public static String prefix = "<p>";
    public static PrintStream err = System.err;
    private static HashMap<Class, LEVEL> hsettings = new HashMap();
    private static HashMap<Class, LEVEL> settings = new HashMap();
    private static ArrayList<Log> logs = new ArrayList<Log>();
    private String messageprefix;
    private Class clazz;
    private LEVEL level = LEVEL.DEFAULT;
    private long time;
    private static LEVEL defaultlevel = LEVEL.INFO;

    public Log(Class clazz) {
        this.clazz = clazz;
        this.messageprefix = clazz.getCanonicalName() + ".";
        checkSettings();
        logs.add(this);
    }

    public static enum LEVEL {

        TRACE,
        DEBUG,
        INFO,
        WARN,
        ERROR,
        FATAL,
        NONE,
        DEFAULT
    }

    public Class getLoggedClass() {
        return clazz;
    }

    public String getName() {
        return clazz.getName();
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
        for (Map.Entry< Class, LEVEL> entry : hsettings.entrySet()) {
            if (entry.getKey().isAssignableFrom(clazz)) {
                level = entry.getValue();
            }
        }
        for (Map.Entry< Class, LEVEL> entry : settings.entrySet()) {
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
    public static void setLevelHierarchy(Class clazz, LEVEL level) {
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
    public static void setLevel(Class clazz, LEVEL level) {
        for (Log log : logs) {
            if (clazz.equals(log.clazz)) {
                log.level = level;
            }
        }
        settings.put(clazz, level);
    }

    public LEVEL getLevel() {
        return level;
    }

    public static void setGeneralLevel(LEVEL level) {
        defaultlevel = level;
    }

    /**
     *
     * @param level
     */
    public void setLevelHierarchy(LEVEL level) {
        setLevelHierarchy(this.clazz, level);
    }

    /**
     *
     * @param level
     */
    public void setLevel(LEVEL level) {
        setLevel(this.clazz, level);
    }

    public void setDefaultLevel() {
        setLevel(this.clazz, LEVEL.DEFAULT);
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
            info(m, DateTools.toString(d));
        } catch (IOException ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     *
     * @return
     */
    public boolean isDebugEnabled() {
        return level.compareTo(LEVEL.DEBUG) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.DEBUG) <= 0);
    }

    /**
     *
     * @return
     */
    public boolean isErrorEnabled() {
        return level.compareTo(LEVEL.ERROR) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.ERROR) <= 0);
    }

    /**
     *
     * @return
     */
    public boolean isFatalEnabled() {
        return level.compareTo(LEVEL.FATAL) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.FATAL) <= 0);
    }

    /**
     *
     * @return
     */
    public boolean isInfoEnabled() {
        return level.compareTo(LEVEL.INFO) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.INFO) <= 0);
    }

    /**
     *
     * @return
     */
    public boolean isTraceEnabled() {
        return level.compareTo(LEVEL.TRACE) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.TRACE) <= 0);
    }

    /**
     *
     * @return
     */
    public boolean isWarnEnabled() {
        return level.compareTo(LEVEL.WARN) <= 0
                || (level == LEVEL.DEFAULT && defaultlevel.compareTo(LEVEL.WARN) <= 0);
    }

    static private int progresslength = 0;

    /**
     * reports progress by reusing a a single line in the console. Works only if
     * the line contains no end-of-line, and my not work in every terminal. The
     * progress method is not thread-safe and only supports one line per
     * application at the same time. If the output is redirected to a file, each
     * call of progress will be a new line.
     *
     * @param reportString
     * @param args
     */
    static public void progress(String reportString, Object... args) {
        if (out == null) {
            for (int i = 0; i < progresslength; i++) {
                System.out.print('\b');
            }
            String progress = sprintf(reportString, args);
            System.out.print(progress);
            progresslength = progress.length();
        } else {
            try {
                out.printf(reportString + "\n", args);
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * outputs the result of sprintf(formatString, args) to the designated
     * output.
     *
     * @param formatString see {@link PrintTools}.
     * @param args
     */
    public static void out(String formatString, Object... args) {
        if (out == null) {
            System.out.println(sprintf(formatString, args));
        } else {
            try {
                out.printf(formatString + "\n", args);
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * outputs the result of sprintf(formatString, args) to the designated
     * output.
     *
     * @param formatString see {@link PrintTools}.
     * @param args
     */
    public void out(String formatString, LEVEL level, Object... args) {
        if (out == null) {
            System.out.println(prefix + level.name() + " " + messageprefix + sprintf(formatString, args));
        } else {
            try {
                out.printf(formatString + "\n", args);
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * outputs message to the designated output, followed by a newline.
     *
     * @param message
     */
    public static void print(String message) {
        if (out == null) {
            System.out.println(message);
        } else {
            try {
                out.printf(message + "\n");
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    /**
     * outputs message to the designated error output, followed by a newline.
     *
     * @param message
     */
    protected static void err(String s) {
        if (out == null) {
            System.err.println(s);
        } else {
            try {
                out.print(s + "\n");
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    protected void err(String s, LEVEL level, Object... args) {
        if (out == null) {
            System.err.println(prefix + level.name() + " " + messageprefix + sprintf(s, args));
        } else {
            try {
                out.printf(s + "\n", args);
                out.flush();
            } catch (IOException ex) {
            }
        }
    }

    /**
     *
     * @param message
     * @param args
     */
    public void trace(String message, Object... args) {
        if (isTraceEnabled()) {
            out(message, LEVEL.TRACE, args);
        }
    }

    /**
     *
     * @param message
     */
    public void trace(Object message) {
        if (isTraceEnabled()) {
            out("%s", LEVEL.TRACE, message.toString());
        }
    }

    /**
     *
     * @param message
     * @param args
     */
    public void debug(String message, Object... args) {
        if (isDebugEnabled()) {
            out(message, LEVEL.DEBUG, args);
        }
    }

    /**
     *
     * @param message
     */
    public void debug(Object message) {
        if (isDebugEnabled()) {
            out("%s", LEVEL.DEBUG, message.toString());
        }
    }

    /**
     *
     * @param message
     * @param args
     */
    public void info(String message, Object... args) {
        if (isInfoEnabled()) {
            out(message, LEVEL.INFO, args);
        }
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
            out("%s", LEVEL.INFO, message.toString());
        }
    }

    public void memoryDump(byte[] arg) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; arg != null && i < arg.length; i += 20) {
            sb.append(sprintf("%08d ", i));
            for (int j = i; j < i + 20 && j < arg.length; j++) {
                sb.append(sprintf("%3d ", arg[j] & 0xFF));
            }
            for (int j = i; j < i + 20 && j < arg.length; j++) {
                sb.append(sprintf("%s", (arg[j] < 32 || arg[j] > 127) ? "." : (char) arg[j]));
            }
            sb.append("\n");
        }
        print(sb.toString());
    }

    /**
     *
     * @param message
     * @param args
     */
    public void warn(String message, Object... args) {
        if (isWarnEnabled()) {
            err(message, LEVEL.WARN, args);
        }
    }

    /**
     *
     * @param message
     */
    public void warn(Object message) {
        if (isWarnEnabled()) {
            err("%s", LEVEL.WARN, message);
        }
    }

    /**
     *
     * @param message
     * @param args
     */
    public void error(String message, Object... args) {
        if (isErrorEnabled()) {
            err(message, LEVEL.ERROR, args);
        }
    }

    /**
     *
     * @param message
     */
    public void error(Object message) {
        if (isErrorEnabled()) {
            err("%s", LEVEL.ERROR, message);
        }
    }

    /**
     *
     * @param message
     * @param args
     */
    public void fatal(String message, Object... args) {
        if (isFatalEnabled()) {
            err(message, LEVEL.FATAL, args);
            err(getCustomStackTrace(Thread.currentThread().getStackTrace()));
            System.exit(1);
        } else {
            err("FATAL DISABLED: " + messageprefix + message);
        }
    }

    /**
     *
     * @param message
     */
    public void fatal(Object message) {
        if (isFatalEnabled()) {
            err("%s", LEVEL.FATAL, message);
            err(getCustomStackTrace(Thread.currentThread().getStackTrace()));
            System.exit(1);
        } else {
            err("FATAL DISABLED: " + messageprefix + message);
        }
    }

    public void printStackTrace() {
        out(getCustomStackTrace(Thread.currentThread().getStackTrace(), 1));
    }

    public void printStackTrace(String message, Object... args) {
        out(message, args);
        out(getCustomStackTrace(Thread.currentThread().getStackTrace(), 1));
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
        return getCustomStackTrace(stack, 0);
    }

    public static String getCustomStackTrace(StackTraceElement stack[], int from) {
        final StringBuilder result = new StringBuilder();
        for (int i = from; i < stack.length; i++) {
            StackTraceElement element = stack[i];
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

    public void fatalexception(Throwable e, String why, Object... params) {
        if (isFatalEnabled()) {
            exception(e, why, params);
            System.exit(1);
        } else {
            exception(e, "FATAL DISABLED: " + why, params);
        }
    }

    public void fatalexception(Throwable e) {
        if (isFatalEnabled()) {
            exception(e);
            System.exit(1);
        } else {
            exception(e, "FATAL DISABLED: ", e.getMessage());
        }
    }

    public void exception(Exception e, String why, Object... params) {
        err(sprintf(why, params));
        err(sprintf(prefix + "EXCEPTION %s", e.getMessage()));
        err(getCustomStackTrace(e.getStackTrace()));
    }

    public void exception(Exception e) {
        err(sprintf(prefix + "EXCEPTION %s", e.getMessage()));
        err(getCustomStackTrace(e.getStackTrace()));
    }

    public void exception(Throwable e, String why, Object... params) {
        err(sprintf(why, params));
        err(sprintf(prefix + "EXCEPTION %s", e.getMessage()));
        err(getCustomStackTrace(e.getStackTrace()));
    }

    public void exception(Throwable e) {
        err(sprintf(prefix + "EXCEPTION %s", e.getMessage()));
        err(getCustomStackTrace(e.getStackTrace()));
    }

}
