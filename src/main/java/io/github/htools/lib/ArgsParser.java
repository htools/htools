package io.github.htools.lib;

import io.github.htools.search.ByteRegex;
import io.github.htools.search.ByteSearchPosition;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Parses the argument array args[] that was passed to a main() method against a
 * template String that describes the parameters. ArgsParser can work with flags
 * (-i inputfile -o outputfile), without flags (inputfile outputfile) or mixed
 * (inputfile -o outputfile). In any case parameters are parsed to the
 * description names (in the example: inputfile and outputfile), and not to the
 * flagnames. Using flags allows to modify the order, so "-o outfile infile"
 * works for the last template.
 * <p>
 * In the template, normal names are mandatory. Names within [] are optional,
 * but must come last or have a flag. Names within {} are a repeating group, but
 * must come last. Repeating groups are also formed when the flag s used
 * repeatedly, e.g. "-i a -i b". "first {last}" will take 1 or more arguments,
 * first contains the first, last contains the others. Boolean switches can be
 * added with a double hyphen, e.g. --verbose.
 * <p>
 * The parsed arguments can be accessed as a String using {@link #getParameter(java.lang.String)
 * }, as an int using {@link #getInt(java.lang.String) } as a Double using
 * {@link #getDouble(java.lang.String) }.
 * <p>
 * A descriptive fatal exception is thrown if the arguments cannot be parsed
 * using the template, if an unknown parameter is requested using a get method,
 * or if a numeric type is requested and the String cannot be parsed into a
 * number.
 *
 * @author jeroen
 */
public class ArgsParser {

    public static Log log = new Log(ArgsParser.class);
    static final String TRUEBOOLEAN = "true";
    static final ByteRegex flag = new ByteRegex("\\-\\c+\\w*");
    static final ByteRegex booleanflag = new ByteRegex("--\\w+");
    static final ByteRegex optional = new ByteRegex("\\[\\s?\\c\\w*\\s?\\]");
    static final ByteRegex repeat = new ByteRegex("\\{\\s?\\c\\w*\\s?\\}");
    static final ByteRegex term = new ByteRegex("\\c\\S*");
    private final String argumentstring;
    static final ByteRegex combi = ByteRegex.combine(optional, repeat, term, flag, booleanflag);
    private HashMap<String, Parameter> flags = new HashMap();
    private HashMap<String, Parameter> getflags = new HashMap();
    private ArrayList<Parameter> positional = new ArrayList();

    public ArgsParser(String args[], String message) {
        argumentstring = message;
        setFlags(message);
        Iterator<Parameter> iterpos = positional.iterator();
        for (int i = 0; i < args.length; i++) {
            if (flag.match(args[i]) || booleanflag.match(args[i])) {
                Parameter f = flags.get(args[i]);
                if (f == null) {
                    log.exit("undefined flag '%s'", args[i]);
                }
                //log.info("args %s %s %d %s", f.name, f.tag, f.type, f.values);
                switch (f.type) {
                    case 4:
                        f.values.add(TRUEBOOLEAN);
                        break;
                    case 1:
                        while (i < args.length - 1 && !flag.match(args[i + 1])) {
                            log.info("repeated %s", args[i + 1]);
                            f.values.add(args[i + 1]);
                            i++;
                            if (i < args.length - 1 && args[i + 1].contains("=")) {
                                break;
                            }
                        }
                        break;
                    case 0:
                        if (i == args.length - 1 || flag.match(args[i + 1])) {
                            break;
                        }
                    case 2:
                        if (i < args.length - 1) {
                            f.values.add(args[++i]);
                            break;
                        } else {
                            log.exit("Non boolean flag %s with value", f.tag);
                        }
                }
            } else {
                int equals = args[i].indexOf('=');
                if (equals > 0) {
                    String name = args[i].substring(0, equals);
                    String value = args[i].substring(equals + 1);
                    Parameter f = getflags.get(name);
                    if (f != null) {
                        f.values.add(value);
                    } else {
                        f = addFlag(2, name, name);
                        f.values.add(value);
                    }
                } else {
                    boolean success = false;
                    while (iterpos.hasNext()) {
                        Parameter f = iterpos.next();
                        if (f.values.size() > 0) {
                            continue;
                        }
                        success = true;
                        switch (f.type) {
                            case 0:
                            case 2:
                                f.values.add(args[i]);
                                break;
                            case 1:
                                while (i < args.length) {
                                    f.values.add(args[i]);
                                    i++;
                                    if (i < args.length && (flag.match(args[i]) || args[i].contains("="))) {
                                        i--;
                                        break;
                                    }
                                } 
                        }
                        break;
                    }
                    if (!success) {
                        log.exit("too many arguments for [%s] %s [%s]", message, ArrayTools.toString(args), this.flags);
                    }
                }
            }
        }
        for (Parameter f : positional) {
            if (f.type == 2 && f.values.size() < 1) {
                log.exit("run with parameters: %s", message);
            }
        }
    }

    public void dump() {
        for (Parameter p : getflags.values()) {
            log.info("Arsparser %s=%s", p.name, p.values);
        }
    }

    private ArrayList<String> getArgumentNames(byte bmessage[], ArrayList<ByteSearchPosition> findAll) {
        ArrayList<String> names = new ArrayList();
        for (int posi = 0; posi < findAll.size(); posi++) {
            ByteSearchPosition p = findAll.get(posi);
            switch (p.pattern) {
                case 2: // normal argument
                    names.add(p.toString().trim());
                    break;
                case 0: // optional argument
                    names.add(ByteTools.toTrimmedString(bmessage, p.start + 1, p.end - 1));
                    break;
                case 3: // flag - switch flag for description name
                    names.add(p.toString().trim());
                    break;
                case 4: // boolean flag - switch flag for description name
                    names.add(p.toString().trim());
                    break;
                case 1: // repeated group, can only be last in list (or use flags)
                    names.add(ByteTools.toTrimmedString(bmessage, p.start + 1, p.end - 1));
            }
        }
        return names;
    }

    public boolean exists(String name) {
        return (getflags.containsKey(name) && getflags.get(name).values.size() > 0);
    }

    public ArrayList<String> getParameter(String name) {
        if (!getflags.containsKey(name)) {
            return null;
        }
        return getflags.get(name).values;
    }

    public String get(String name) {
        ArrayList<String> list = getParameter(name);
        return (list == null || list.size() == 0) ? null : list.get(0);
    }

    public String get(String name, String defaultValue) {
        if (exists(name)) {
            return getParameter(name).get(0);
        }
        return defaultValue;
    }

    public int getInt(String name, int def) {
        String v = get(name, null);
        try {
            return v == null ? def : Integer.parseInt(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Integer %s", name, v);
            return 0; // unreachable
        }
    }

    public long getLong(String name, long def) {
        String v = get(name, null);
        try {
            return v == null ? def : Long.parseLong(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Long %s", name, v);
            return 0; // unreachable
        }
    }

    public double getDouble(String name, double def) {
        String v = get(name, null);
        try {
            return v == null ? def : Double.parseDouble(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Double %s", name, v);
            return 0.0; // unreachable
        }
    }

    public double getDouble(String name) {
        String v = get(name);
        try {
            return Double.parseDouble(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Double %s", name, v);
            return 0.0; // unreachable
        }
    }

    public int getInt(String name) {
        String v = get(name);
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Integer %s", name, v);
            return 0; // unreachable
        }
    }

    public long getLong(String name) {
        String v = get(name);
        try {
            return Long.parseLong(v);
        } catch (NumberFormatException | NullPointerException ex) {
            log.exit("flag %s does not contain a valid Long %s", name, v);
            return 0; // unreachable
        }
    }

    public boolean getBoolean(String name) {
        String v = get(name);
        return v == null ? false : v.equals(TRUEBOOLEAN);
    }

    public String[] getStrings(String name) {
        return getParameter(name).toArray(new String[0]);
    }

    public Collection<Parameter> getParameters() {
        return getflags.values();
    }

    private void setFlags(String message) {
        byte bmessage[] = ByteTools.toBytes(message);
        ArrayList<ByteSearchPosition> argumentpositions = combi.findAllPos(bmessage);
        ArrayList<String> names = this.getArgumentNames(bmessage, argumentpositions);
        for (int argumentnumber = 0; argumentnumber < argumentpositions.size(); argumentnumber++) {
            ByteSearchPosition argument = argumentpositions.get(argumentnumber);
            if (argument.pattern == 3) { // flag
                ByteSearchPosition nextArgument = argumentpositions.get(argumentnumber + 1);
                Parameter f = addFlag(nextArgument.pattern, names.get(argumentnumber), names.get(argumentnumber + 1));
                flags.put(f.tag, f);
                if (nextArgument.pattern != 0) // optional arguments can only be used with flags
                {
                    positional.add(f);
                }
                argumentnumber++;
            } else if (argument.pattern == 4) { // boolean flag
                Parameter f = addFlag(argument.pattern, names.get(argumentnumber), names.get(argumentnumber).substring(2));
                flags.put(f.tag, f);
            } else {
                if (argument.pattern < 2 && argumentnumber != argumentpositions.size() - 1) {
                    log.exit("unflagged optional or repeated group can only be in last position");
                }
                Parameter f = addFlag(argument.pattern, names.get(argumentnumber), names.get(argumentnumber));
                positional.add(f);
            }
        }
        //log.info("%s", flags);
    }

    public Parameter addFlag(int pattern, String flagname, String argumentname) {
        Parameter f = new Parameter(pattern, flagname, argumentname);
        getflags.put(f.name, f);
        return f;
    }

    public class Parameter {

        int type;
        String tag;
        String name;
        ArrayList<String> values = new ArrayList();

        protected Parameter(int type, String tag, String name) {
            this.type = type;
            this.tag = tag;
            this.name = name;
        }

        public ArrayList<String> getValues() {
            return values;
        }

        public String getName() {
            return name;
        }

        public String toString() {
            return values.toString();
        }
    }
}
