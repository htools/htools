package io.github.htools.hadoop;

import io.github.htools.fcollection.FHashSet;
import io.github.htools.fcollection.FHashSetInt;
import io.github.htools.fcollection.FHashSetLong;
import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import io.github.htools.io.Datafile;
import io.github.htools.io.EOCException;
import io.github.htools.io.FSFileInBuffer;
import io.github.htools.io.FSPath;
import io.github.htools.lib.ArgsParser;
import io.github.htools.lib.ArrayTools;
import io.github.htools.lib.Log;
import static io.github.htools.lib.PrintTools.sprintf;
import io.github.htools.hadoop.io.OutputFormat;
import io.github.htools.io.FSFile;
import io.github.htools.io.HDFSPath;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.io.NullWritable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Extension of Hadoop's Conf that can read/write configurations from flat text
 * files, or from command line args.
 * <p>
 * Valid configuration keys have at least one dot, e.g. retriever.strategy, are
 * in lowercase and can either be assigned a String, int, long (ends with l),
 * double (has a decimal point), boolean (true/false) or array (multiple lines
 * +key.name=...). A minus "-key.name=" can be used to only set keys that have
 * no value yet.
 * <p>
 * From files, "import filename" can be used to read settings from a file in the
 * same folder, and "delete key.name" can be used to delete key.name.
 *
 * @author Jeroen Vuurens
 */
public class Conf extends JobConf {

    public static Log log = new Log(Conf.class);
    private static FileSystem filesystem;
    static ByteRegex configurationkey = new ByteRegex("\\+?\\c\\w*(\\.\\c\\w*)+=\\S*$");
    static ByteRegex jarskey = new ByteRegex("jars=\\S*$");
    static ByteRegex commentregex = new ByteRegex("#[^\\n]*(\\n|$)");
    static ByteRegex importregex = new ByteRegex("[ \\t]*import[ \\t]+");
    static ByteRegex deleteregex = new ByteRegex("[ \\t]*delete[ \\t]+");
    static ByteRegex arraykeyregex = new ByteRegex("\\+[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
    static ByteRegex optionalkeyregex = new ByteRegex("\\-[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
    static ByteRegex keyregex = new ByteRegex("[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
    static ByteRegex emptylineregex = new ByteRegex("[ \\t]*(\\n|$)");
    static ByteRegex otherlineregex = new ByteRegex(".*?(\\n|$)");
    static ByteRegex loadregex = ByteRegex.combine(commentregex, emptylineregex, importregex, otherlineregex);
    static ByteRegex lineregex = ByteRegex.combine(deleteregex, arraykeyregex, optionalkeyregex, keyregex, otherlineregex);
    static ByteRegex doubleregex = new ByteRegex("[ \\t]*\\d+\\.\\d+\\s*(\\n|$)");
    static ByteRegex longregex = new ByteRegex("[ \\t]*\\d+l\\s*(\\n|$)");
    static ByteRegex intregex = new ByteRegex("[ \\t]*\\d+\\s*(\\n|$)");
    static ByteRegex boolregex = new ByteRegex("[ \\t]*(true|false)\\s*(\\n|$)");
    static ByteRegex stringregex = new ByteRegex("[^\\n]+(\\n|$)");
    static ByteRegex valueregex = ByteRegex.combine(emptylineregex, longregex, doubleregex, intregex, boolregex, stringregex);

    public Conf(String args[], String template) {
        this();
        parseArgs(args, template);
    }

    public Conf() {
        super();
        HBaseConfiguration.addHbaseResources(this);
    }

    protected Conf(org.apache.hadoop.conf.Configuration other) {
        super(other);
    }

    public FileSystem getFileSystem() {
        return Conf.getFileSystem(this);
    }

    public void addLibraries(String dir, String libs[]) {
        StringBuilder sb = new StringBuilder();
        for (String lib : libs) {
            sb.append(",").append(dir).append(lib);
        }
        String args[] = new String[]{"-libjars", sb.deleteCharAt(0).toString()};
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
        } catch (IOException ex) {
            log.exception(ex, "Failed to include rr.lib jars: %s", libs);
        }
    }

    public void addLibraries(String dirs[]) {
        StringBuilder sb = new StringBuilder();
        for (String dir : dirs) {
            if (FSPath.isDir(dir)) {
                if (!dir.endsWith("/")) {
                    dir = dir + "/";
                }
                for (String lib : new FSPath(dir).getFilenames()) {
                    if ((lib.endsWith(".jar") || lib.endsWith(".xml")) && FSFile.canRead(dir + lib)) {
                        sb.append(",").append(dir).append(lib);
                    }
                }
            } else if (FSFile.exists(dir) && FSFile.canRead(dir)
                    && (dir.endsWith(".jar") || dir.endsWith(".xml"))) {
                sb.append(",").append(dir);
            }
        }
        String args[] = new String[]{"-libjars", sb.deleteCharAt(0).toString()};
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
        } catch (IOException ex) {
            log.exception(ex, "Failed to include jars=%s", sb);
        }
    }

    public Conf(Datafile df) {
        processScript(readConfigFile(df));
    }

    public Conf(String filename) {
        this(new Datafile(filename));
    }

    public static FileSystem getFileSystem(Configuration conf) {
        if (filesystem == null) {
            try {
                filesystem = FileSystem.get(conf);
            } catch (IOException ex) {
                log.exception(ex, "getFileSystem(%s)", conf);
            }
        }
        return filesystem;
    }

    public void setJobName(Class jobclass, Object... params) {
        this.setJobName(jobclass.getCanonicalName() + " " + ArrayTools.toString(params));
    }

    public void submitJob() throws IOException, InterruptedException, ClassNotFoundException {
        setJarByClass(this.getMapperClass());
        if (getOutputKeyClass() == null) {
            setOutputKeyClass(NullWritable.class);
        }
        if (getOutputValueClass() == null) {
            setOutputValueClass(OutputFormat.getWritableClass());
        }
        Job.getInstance(this).submit();
    }

    // creates a Conf based on a file with settings in a JAR
    public static Conf createFromResource(String resource) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        FSFileInBuffer fi = new FSFileInBuffer(input);
        byte[] readBytes = fi.readBytes();
        Conf conf = new Conf();
        conf.processScript(new String(readBytes, 0, readBytes.length));
        return conf;
    }

    /**
     * Parses an args[], probably passed to main, and runs the
     * GenericOptionsParser (e.g. setting libs to include for MR job), filter
     * out key.name= style configuration settings, and maps the remaining input
     * to the labels identified by the template (see {@link ArgsParser}).
     *
     * @param args
     * @param template
     */
    public void parseArgs(String args[], String template) {
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
            args = p.getRemainingArgs();
        } catch (IOException ex) {
            log.fatalexception(ex, "Configuration(%s, %s)", ArrayTools.toString(args), template);
        }
        args = argsToConf(args);
        ArgsParser argsparser = new ArgsParser(args, template);
        for (ArgsParser.Parameter entry : argsparser.getParameters()) {
            if (entry != null) {
                if (entry.getValues().size() > 1) {
                    this.setStringList(entry.getName(), entry.getValues());
                } else if (entry.getValues().size() == 1) {
                    set(entry.getName(), entry.getValues().get(0));
                }
            }
        }
    }

    public void parseArgsConfFile(String args[], String template) {
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
            args = p.getRemainingArgs();
            //log.info("ramaining args %s", ArrayTools.concat(args));
        } catch (IOException ex) {
            log.fatalexception(ex, "Configuration(%s, %s)", ArrayTools.toString(args), template);
        }
        processConfigFile(configDatafile(args[0]));
        args = argsToConf(args);
        ArgsParser argsparser = new ArgsParser(args, "configurationfilename " + template);
        for (ArgsParser.Parameter entry : argsparser.getParameters()) {
            if (entry != null) {
                if (entry.getValues().size() > 1) {
                    this.setStringList(entry.getName(), entry.getValues());
                } else if (entry.getValues().size() == 1) {
                    set(entry.getName(), entry.getValues().get(0));
                }
            }
        }
    }

    /**
     * Filters out configuration assignments from args, of style
     * first.lastname=value. These assignments are set in the configuration and
     * removed from the returned args array. This is similar to using -Dconf.key
     * using the GenericOptionsParser.
     */
    private String[] argsToConf(String args[]) {
        ArrayList<String> ar = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            if (configurationkey.startsWith(args[i])) {
                processScript(args[i]);
            } else if (jarskey.startsWith(args[i])) {
                String dirs[] = args[i].substring(args[i].indexOf('=') + 1).split("[,:]");
                this.addLibraries(dirs);
            } else {
                ar.add(args[i]);
            }
        }
        args = ar.toArray(new String[ar.size()]);
        return args;
    }

    public void processConfigFile(String file) {
        readConfigFile(configDatafile(file));
    }

    public void processConfigFile(Datafile file) {
        processScript(readConfigFile(file));
    }

    public Datafile configDatafile(String filename) {
        return new Datafile(filename);
    }

    private static String readConfigFile(Datafile df) {
        try {
            StringBuilder sb = new StringBuilder();
            byte content[] = df.readFully();
            int pos = 0;
            while (pos < content.length) {
                boolean array = false;
                boolean optional = false;
                ByteSearchPosition p = loadregex.matchPos(content, pos, content.length);
                if (!p.found()) {
                    break;
                }
                pos = p.end;
                switch (p.pattern) {
                    case 0: // line is comment
                    case 1: // line is empty
                        continue;
                    case 2: // line is import
                        if (df != null) {
                            p = stringregex.matchPos(content, p.end, content.length);
                            pos = p.end;
                            String file = new String(content, p.start, p.end - p.start).trim();
                            Datafile subfile = new Datafile(df.getDir().getFilename(file));
                            subfile.setFileSystem(df.getFileSystem());
                            sb.append(readConfigFile(subfile));
                        } else {
                            log.fatal("Cannot read import from string");
                        }
                        continue;
                    default:
                        sb.append(new String(content, p.start, p.end - p.start));
                        continue;
                }
            }
            return sb.toString();
        } catch (EOCException ex) {
            log.exception(ex, "readConfigFile() %s", df.getCanonicalPath());
        }
        return "";
    }

    public void processScript(String contentstring) {
        byte content[] = contentstring.getBytes();
        int pos = 0;
        while (pos < content.length) {
            boolean array = false;
            boolean optional = false;
            ByteSearchPosition p = lineregex.matchPos(content, pos, content.length);
            if (!p.found()) {
                break;
            }
            pos = p.end;
            switch (p.pattern) {
                case 0: // delete entry
                    p = stringregex.matchPos(content, p.end, content.length);
                    pos = p.end;
                    String key = new String(content, p.start, p.end - p.start - 1).trim();
                    delete(key);
                    continue;
                case 1: // line is array
                    array = true;
                    p.start++;
                    break;
                case 2: // optionalkey
                    optional = true;
                    p.start++;
                case 3: // line is no array
                    break;
                default:
                    log.info("unreadable line in configuration : %s", new String(content, p.start, p.end - p.start));
                    continue;
            }
            String key = new String(content, p.start, p.end - p.start - 1).trim();
            if (array) {
                p = stringregex.matchPos(content, p.end, content.length);
                pos = p.end;
                String value = new String(content, p.start, p.end - p.start - 1).trim();
                addArray(key, value);
            } else {
                p = valueregex.matchPos(content, p.end, content.length);
                pos = p.end;

                String value = new String(content, p.start, p.end - p.start).trim();
                //log.info("conf key %s pattern %d content %s", key, p.pattern, value);
                switch (p.pattern) {
                    case 0: // empty line means delete key
                        delete(key);
                        break;
                    case 1: // long
                        if (!optional || !containsKey(key)) {
                            setLong(key, Long.parseLong(value.substring(0, value.indexOf('l'))));
                        }
                        break;
                    case 2: // double is stored as String to avoid precision loss fro converting to float
                        if (!optional || !containsKey(key)) {
                            set(key, value);
                        }
                        break;
                    case 3: // int
                        if (!optional || !containsKey(key)) {
                            setInt(key, Integer.parseInt(value));
                        }
                        break;
                    case 4: // boolean
                        if (!optional || !containsKey(key)) {
                            setBoolean(key, value.equalsIgnoreCase("true"));
                        }
                        break;
                    case 5: // string
                        if (!optional || !containsKey(key)) {
                            set(key, value);
                        }
                }
            }
        }
    }

    public boolean containsKey(String key) {
        String value = super.get(key);
        return (value != null && value.length() > 0);
    }

    public boolean containsKey(Enum<?> key) {
        return containsKey(key.toString());
    }

    public void delete(String label) {
        set(label, "");
    }

    public void delete(Enum<?> key) {
        delete(key.toString());
    }

    public void addArray(String label, String value) {
        if (value.length() == 0) {
            delete(label);
        } else {
            ArrayList<String> values;
            if (containsKey(label)) {
                values = getStringList(label);
            } else {
                values = new ArrayList<String>();
            }
            values.add(value);
            setStrings(label, values.toArray(new String[values.size()]));
        }
    }

    public void addArray(Enum<?> key, String value) {
        addArray(key.toString(), value);
    }

    public static Conf convert(org.apache.hadoop.conf.Configuration conf) {
        if (conf instanceof Conf) {
            return (Conf) conf;
        }
        return new Conf(conf);
    }

    /**
     *
     * @param key
     * @return Array of strings attached configured for the given key. Different
     * from Hadoop's default getStrings(), this method returns an array length 0
     * when the key does not exist, and substitutes nested variables (e.g.
     * ${name}) in the values.
     */
    @Override
    public String[] getStrings(String key) {
        String values[] = super.getStrings(key);
        if (values == null) {
            return new String[0];
        }
        //for (int i = 0; i < values.length; i++) {
        //   values[i] = substituteString(values[i]);
        //}
        return values;
    }

    public String[] getStrings(Enum<?> key) {
        return getStrings(key.toString());
    }

    @Override
    public String get(String key, String defaultvalue) {
        String value = super.get(key, defaultvalue);
        return value;
        //return substituteString(value);
    }

    public String get(Enum<?> key, String defaultvalue) {
        return get(key.toString(), defaultvalue);
    }

    public String get(Enum<?> key) {
        return get(key.toString());
    }

    public boolean getBoolean(Enum<?> key, boolean defaultvalue) {
        return this.getBoolean(key.toString(), defaultvalue);
    }

    /**
     * @param key
     * @return HDFSPath for the pathstring set under the key. Fails when the key
     * is not set. This is short for new HDFSPath(conf, conf.get(key)).
     */
    public HDFSPath getHDFSPath(String key) {
        return new HDFSPath(this, get(key));
    }

    /**
     * @param key
     * @return Datafile for the pathstring set under the key on HDFS. Fails when
     * the key is not set. This is short for new Datafile(conf, conf.get(key)).
     */
    public Datafile getHDFSFile(String key) throws IOException {
        return new Datafile(this, get(key));
    }

    /**
     * @param key
     * @return Datafile for the pathstring set under the key on local FS. Fails
     * when the key is not set. This is short for new Datafile(conf.get(key)).
     */
    public Datafile getFSFile(String key) throws IOException {
        return new Datafile(get(key));
    }

    public void setLong(Enum<?> key, long value) {
        setLong(key.toString(), value);
    }

    public void setInt(Enum<?> key, int value) {
        setInt(key.toString(), value);
    }

    public void set(Enum<?> key, String value) {
        set(key.toString(), value);
    }

    public void setDouble(Enum<?> key, double value) {
        setDouble(key.toString(), value);
    }

    public void setBoolean(Enum<?> key, boolean value) {
        setBoolean(key.toString(), value);
    }

    public ArrayList<String> getStringList(String key) {
        return getStringList(this, key);
    }

    public static ArrayList<String> getStringList(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        ArrayList<String> set = new ArrayList<String>(values.length);
        for (String value : values) {
            set.add(value);
        }
        return set;
    }
    
    public static void addToStringList(Configuration conf, String key, String value) {
        String existingValue = conf.get(key);
        if (existingValue == null || existingValue.length() == 0) {
            conf.set(key, value);
        } else {
            conf.set(key, existingValue + "," + value);
        }
    }

    public static FHashSet<String> getStringSet(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        FHashSet<String> set = new FHashSet<String>(values.length);
        for (String value : values) {
            set.add(value);
        }
        return set;
    }

    public ArrayList<String> getStringList(Enum<?> key) {
        return getStringList(this, key.toString());
    }

    public ArrayList<Integer> getIntList(String key) {
        return getIntList(this, key);
    }

    public ArrayList<Integer> getIntList(Enum<?> key) {
        return getIntList(this, key.toString());
    }

    public static ArrayList<Integer> getIntList(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        ArrayList<Integer> list = new ArrayList<Integer>(values.length);
        try {
            for (String value : values) {
                list.add(Integer.parseInt(value));
            }
        } catch (NumberFormatException ex) {
            log.fatalexception(ex, "getIntList(%s)", key);
        }
        return list;
    }

    public static FHashSetInt getIntSet(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        FHashSetInt set = new FHashSetInt(values.length);
        try {
            for (String value : values) {
                set.add(Integer.parseInt(value));
            }
        } catch (NumberFormatException ex) {
            log.fatalexception(ex, "getIntSet(%s)", key);
        }
        return set;
    }

    public static ArrayList<Long> getLongList(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        ArrayList<Long> list = new ArrayList<Long>(values.length);
        try {
            for (String value : values) {
                list.add(Long.parseLong(value));
            }
        } catch (NumberFormatException ex) {
            log.fatalexception(ex, "getLongList(%s)", key);
        }
        return list;
    }

    public static FHashSetLong getLongSet(Configuration conf, String key) {
        String values[] = conf.getStrings(key);
        FHashSetLong set = new FHashSetLong(values.length);
        try {
            for (String value : values) {
                set.add(Long.parseLong(value));
            }
        } catch (NumberFormatException ex) {
            log.fatalexception(ex, "getLongSet(%s)", key);
        }
        return set;
    }

    public ArrayList<Long> getLongList(Enum<?> key) {
        return getLongList(this, key.toString());
    }

    public ArrayList<Long> getLongList(String key) {
        return getLongList(this, key);
    }

    public void setIntList(String key, Collection<Integer> list) {
        setIntList(this, key, list);
    }

    public static void setIntList(Configuration conf, String key, Collection<Integer> list) {
        ArrayList<String> s = new ArrayList<String>();
        for (Integer i : list) {
            s.add(i.toString());
        }
        setStringList(conf, key, s);
    }

    public void setIntList(Enum<?> key, Collection<Integer> list) {
        setIntList(key.toString(), list);
    }

    public void setLongList(String key, Collection<Long> list) {
        setLongList(this, key, list);
    }

    public static void setLongList(Configuration conf, String key, Collection<Long> list) {
        ArrayList<String> s = new ArrayList<String>();
        for (Long i : list) {
            s.add(i.toString());
        }
        setStringList(conf, key, s);
    }

    public void setLongList(Enum<?> key, Collection<Long> list) {
        setLongList(key.toString(), list);
    }

    public static void setStringList(Configuration conf, String key, Collection<String> list) {
        conf.setStrings(key, list.toArray(new String[list.size()]));
    }

    public void setStringList(String key, Collection<String> list) {
        setStrings(key, list.toArray(new String[list.size()]));
    }

    public void setStringList(Enum<?> key, Collection<String> list) {
        setStringList(key.toString(), list);
    }

    /**
     * Note: Hadoop 0.20 does not support double, so these are stored as
     * strings, if the value is not empty or a valid double a fatal exception is
     * the result
     * <p>
     * @return the double value of the key.
     */
    public double getDouble(String key, double defaultvalue) {
        double d = defaultvalue;
        String value = get(key);
        try {
            if (value != null && value.length() > 0) {
                d = Double.parseDouble(value);
            }
        } catch (NumberFormatException ex) {
            log.fatalexception(ex, "Configuration setting '%s' does not contain a valid double '%s'", key, value);
        }
        return d;
    }

    public double getDouble(Enum<?> key, double defaultvalue) {
        return getDouble(key.toString(), defaultvalue);
    }

    public double getInt(Enum<?> key, int defaultvalue) {
        return getInt(key.toString(), defaultvalue);
    }

    public long getLong(Enum<?> key, long defaultvalue) {
        return getLong(key.toString(), defaultvalue);
    }

    /**
     * Substitutes ${key} occurrences with their value in the configuration.
     * Note: there is no check for cyclic references, which are not allowed.
     * <p>
     * @param conf
     * @param value
     * @return
     */
    private String substituteString(String value) {
        if (value == null) {
            return null;
        }
        for (int p1 = value.indexOf("${"); p1 > -1; p1 = value.indexOf("${")) {
            int p2 = value.indexOf("}", p1);
            String subkey = value.substring(p1 + 2, p2 - p1 - 2);
            value = value.substring(0, p1) + get(subkey) + value.substring(p2 + 1);
        }
        return value;
    }

    /**
     * Outputs all keys that begin with prefix that are configured. For debug
     * purposes.
     *
     * @param prefix
     */
    public void print(String prefix) {
        for (Entry<String, String> e : this) {
            if (prefix == null || prefix.length() == 0 || e.getKey().startsWith(prefix)) {
                log.printf("%s=%s", e.getKey(), e.getValue());
            }
        }
    }

    public void softSetConfiguration(String key, String value) {
        if (!containsKey(key)) {
            set(key, value);
        }
    }

    /**
     * Sets the maximum amount of memory assigned to mappers. Effective it is
     * assigned to 512MB less to allow for system memory.
     *
     * @param mem
     */
    public void setMapMemoryMB(int mem) {
        this.setInt(ConfSetting.MAP_MEMORY_MB, mem);
        this.set(ConfSetting.MAP_JAVA_OPTS, sprintf("-server -Xmx%dm", mem - 512));
    }

    /**
     * Set the output buffer size
     * @param mem 
     */
    public void setSortMB(int mem) {
        this.setInt(ConfSetting.IO_SORT_MB, mem);
    }

    public void setShufflePercent(double percentage) {
        this.setDouble(ConfSetting.SHUFFLE_INPUT_BUFFER_PERCENT, percentage);
    }

    /**
     * Sets the maximum amount of memory assigned to reducers. Effective it is
     * assigned to 512MB less to allow for system memory.
     *
     * @param mem
     */
    public void setReduceMemoryMB(int mem) {
        this.setInt(ConfSetting.REDUCE_MEMORY_MB, mem);
        this.set(ConfSetting.REDUCE_JAVA_OPTS, sprintf("-server -Xmx%dm -XX:NewRatio=8 -Djava.net.preferIPv4Stack=true", mem - 512));
    }

    /**
     * Sets the task timeout to timeout_ms.
     *
     * @param timeout_ms
     */
    public void setTaskTimeout(int timeout_ms) {
        this.setInt(ConfSetting.TASK_TIMEOUT, timeout_ms);
    }

    /**
     * Configures that reducers do not start until maps_completed percentage of
     * mappers have completed.
     *
     * @param maps_completed
     */
    public void setReduceStart(double maps_completed) {
        setDouble(ConfSetting.COMPLETED_MAPS_FOR_REDUCE_SLOWSTART, maps_completed);
    }

    public void setMaxSimultaneousMappers(int value) {
        setInt(ConfSetting.TASKTRACKER_MAP_TASKS_MAXIMUM, value);
    }

    /**
     * Sets the queue for the job
     *
     * @param queue name of queue
     */
    public void setQueue(String queue) {
        set(ConfSetting.QUEUE_NAME, queue);
    }
}
