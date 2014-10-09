package io.github.repir.tools.hadoop;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.Content.FSFileInBuffer;
import io.github.repir.tools.Lib.ArgsParser;
import io.github.repir.tools.Lib.ArrayTools;
import io.github.repir.tools.Lib.Log;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map.Entry;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapred.JobPriority;
import org.apache.hadoop.util.GenericOptionsParser;

/**
 * Extension of Hadoop's Configuration, that is also used by {@link Repository}
 * to store its configuration. The extension can read/write configurations from
 * flat text files used to configure a {@link Repository} or a specific
 * {@link Strategy}.
 * <p/>
 * Valid configuration keys have at least one dot, e.g. retriever.strategy, are
 * in lowercase and can either be assigned a String, int, long (ends with l),
 * double (has a decimal point), boolean (true/false) or array (multiple lines
 * +key.name=...). A minus "-key.name=" can be used to only set keys that have
 * no value yet.
 * <p/>
 * From files, "import filename" can be used to read settings from a file in the
 * same folder, and "delete key.name" can be used to delete key.name.
 *
 * @author Jeroen Vuurens
 */
public class Configuration extends org.apache.hadoop.conf.Configuration {

    public static Log log = new Log(Configuration.class);
    static ByteRegex configurationkey = new ByteRegex("\\+?\\c\\w*(\\.\\c\\w*)+=\\S*$");
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

    public Configuration() {
        super();
    }

    protected Configuration(org.apache.hadoop.conf.Configuration other) {
        super(other);
    }

    public Configuration(Datafile df) {
        processScript(readConfigFile(df));
    }

    public Configuration(String filename) {
        this(new Datafile(filename));
    }

    // creates a Configuration based on a file with settings in a JAR
    public static Configuration createFromResource(String resource) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        FSFileInBuffer fi = new FSFileInBuffer(input);
        byte[] readBytes = fi.readBytes();
        Configuration conf = new Configuration();
        conf.processScript(new String(readBytes, 0, readBytes.length));
        return conf;
    }

    public Configuration(String args[], String template) {
        parseArgs(args, template);
    }
    
    /**
     * Parses an args[], probably passed to main, and runs the GenericOptionsParser
     * (e.g. setting libs to include for MR job), filter out key.name= style
     * configuration settings, and maps the remaining input to the labels identified
     * by the template (see {@link ArgsParser}).
     * @param args
     * @param template 
     */
    public void parseArgs(String args[], String template) {
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
            args = p.getRemainingArgs();
        } catch (IOException ex) {
            log.fatalexception(ex, "Configuration(%s, %s)", ArrayTools.concat(args), template);
        }
        args = argsToConf(args);
        ArgsParser argsparser = new ArgsParser(args, template);
        for (ArgsParser.Parameter entry : argsparser.getParameters()) {
            if (entry != null)
                if (entry.getValues().size() > 1) {
                    this.setStringList(entry.getName(), entry.getValues());
                } else if (entry.getValues().size() == 1)
                    set(entry.getName(), entry.getValues().get(0));
        }
    }

    public void parseArgsConfFile(String args[], String template) {
        try {
            GenericOptionsParser p = new GenericOptionsParser(this, args);
            args = p.getRemainingArgs();
            //log.info("ramaining args %s", ArrayTools.concat(args));
        } catch (IOException ex) {
            log.fatalexception(ex, "Configuration(%s, %s)", ArrayTools.concat(args), template);
        }
        processConfigFile(configDatafile(args[0]));
        args = argsToConf(args);
        ArgsParser argsparser = new ArgsParser(args, "configurationfilename " + template);
        for (ArgsParser.Parameter entry : argsparser.getParameters()) {
            if (entry != null)
                if (entry.getValues().size() > 1) {
                    this.setStringList(entry.getName(), entry.getValues());
                } else if (entry.getValues().size() == 1)
                    set(entry.getName(), entry.getValues().get(0));
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
                //log.info("argstoconf %s", args[i]);
                processScript(args[i]);
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

    public void delete(String label) {
        set(label, "");
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
            if (!values.contains(value)) {
                values.add(value);
            }
            setStrings(label, values.toArray(new String[values.size()]));
        }
    }

    public static Configuration convert(org.apache.hadoop.conf.Configuration conf) {
        if (conf instanceof Configuration) {
            return (Configuration) conf;
        }
        return new Configuration(conf);
    }

    /**
     * If a String value contains ${correct.name} then that part will be
     * replaced by the value of correct.name. This way general configuration
     * settings can be reused such as location of the repository. Therefore the
     * use of {@link #getSubString(java.lang.String) } is recommended over
     * {@link #get(java.lang.String)}
     *
     * @param key
     * @return value of key, in which other key references have been
     * substituted.
     */
    public String getSubString(String key) {
        return get(key, "");
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

    @Override
    public String get(String key, String defaultvalue) {
        String value = super.get(key, defaultvalue);
        return value;
        //return substituteString(value);
    }

    public ArrayList<String> getStringList(String key) {
        ArrayList<String> values = new ArrayList<String>();
        String value[] = getStrings(key);
        if (value != null) {
            values.addAll(Arrays.asList(value));
        }
        return values;
    }

    public ArrayList<Integer> getIntList(String key) {
        ArrayList<Integer> values = new ArrayList<Integer>();
        String value[] = getStrings(key, new String[0]);
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                values.add(Integer.parseInt(value[i]));
            }
        }
        return values;
    }

    public ArrayList<Long> getLongList(String key) {
        ArrayList<Long> values = new ArrayList<Long>();
        String value[] = super.getStrings(key, new String[0]);
        if (value != null) {
            for (int i = 0; i < value.length; i++) {
                values.add(Long.parseLong(value[i]));
            }
        }
        return values;
    }

    public void setIntList(String key, ArrayList<Integer> list) {
        ArrayList<String> s = new ArrayList<String>();
        for (Integer i : list) {
            s.add(i.toString());
        }
        setStringList(key, s);
    }

    public void setLongList(String key, ArrayList<Long> list) {
        ArrayList<String> s = new ArrayList<String>();
        for (Long i : list) {
            s.add(i.toString());
        }
        setStringList(key, s);
    }

    public void setStringList(String key, ArrayList<String> list) {
        setStrings(key, list.toArray(new String[list.size()]));
    }

    /**
     * Note: Hadoop 0.20 does not support double, so these are stored as
     * strings, if the value is not empty or a valid double a fatal exception is
     * the result
     * <p/>
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

    /**
     * Substitutes ${key} occurrences with their value in the configuration.
     * Note: there is no check for cyclic references, which are not allowed.
     * <p/>
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
            value = value.substring(0, p1) + getSubString(subkey) + value.substring(p2 + 1);
        }
        return value;
    }

    /**
     * Outputs all keys that begin with <prefix> that are configured. For debug
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

    public FileSystem FS() {
        try {
            return FileSystem.get(this);
        } catch (IOException ex) {
            log.exception(ex, "getFS()");
            return null;
        }
    }

    public static FileSystem getFS() {
        return new Configuration().FS();
    }

    public void softSetConfiguration(String key, String value) {
        if (!containsKey(key)) {
            set(key, value);
        }
    }

    public void setPriorityHigh() {
        softSetConfiguration("mapreduce.job.priority", JobPriority.HIGH.toString());
    }

    public void setPriorityVeryHigh() {
        softSetConfiguration("mapreduce.job.priority", JobPriority.VERY_HIGH.toString());
    }

    public void setPriorityLow() {
        softSetConfiguration("mapreduce.job.priority", JobPriority.LOW.toString());
    }

}
