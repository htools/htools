package io.github.repir.tools.Lib;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
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
 * <p/>
 * In the template, normal names are mandatory. Names within [] are optional,
 * but must come last or have a flag. Names within {} are a repeating group, but
 * must come last. Repeating groups are also formed when the flag s used
 * repeatedly, e.g. "-i a -i b". "first {last}" will take 1 or more arguments,
 * first contains the first, last contains the others.
 * <p/>
 * The parsed arguments can be accessed as a String using {@link #getParameter(java.lang.String)
 * }, as an int using {@link #getInt(java.lang.String) } as a Double using
 * {@link #getDouble(java.lang.String) } or as a repeating group using {@link #getRepeatedGroup()
 * } which is always an array of String, and will return an empty array if none
 * is given or an array of a single String if only one is given.
 * <p/>
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
    static final ByteRegex flag = new ByteRegex("\\-\\c\\w*");
    static final ByteRegex optional = new ByteRegex("\\[\\s?\\c\\w*\\s?\\]");
    static final ByteRegex repeat = new ByteRegex("\\{\\s?\\c\\w*\\s?\\}");
    static final ByteRegex term = new ByteRegex("\\c\\S*");
    private final String argumentstring;
    static final ByteRegex combi = ByteRegex.combine(optional, repeat, term, flag);
    private HashMap<String, Parameter> flags = new HashMap();
    private HashMap<String, Parameter> getflags = new HashMap();
    private ArrayList<Parameter> positional = new ArrayList();

    public ArgsParser(String args[], String message) {
        argumentstring = message;
        setFlags(message);
        Iterator<Parameter> iterpos = positional.iterator();
        for (int i = 0; i < args.length; i++) {
            if (flag.match(args[i])) {
                String flagtag = args[i].substring(1);
                Parameter f = flags.get(flagtag);
                if (f == null)
                    log.exit("undefined flag '%s'", args[i]);
                switch (f.type) {
                    case 4:
                        f.values.add(TRUEBOOLEAN);
                        break;
                    case 1:
                        for (; i < args.length - 1 && !flag.match(args[i + 1]); i++) {
                            f.values.add(args[i + 1]);
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
                            do {
                                f.values.add(args[i]);
                            } while (i < args.length - 1 && !flag.match(args[i + 1]) && ++i > 0);
                    }
                    break;
                }
                if (!success) {
                    log.exit("too many arguments for [%s] %s", message, ArrayTools.concat(args));
                }
            }
        }
        for (Parameter f : positional) {
            if (f.type == 2 && f.values.size() < 1) {
                log.exit("run with parameters: %s", message);
            }
        }
    }

    private ArrayList<String> getArgumentNames(byte bmessage[], ArrayList<ByteSearchPosition> findAll) {
        ArrayList<String> names = new ArrayList();
        for (int posi = 0; posi < findAll.size(); posi++) {
            ByteSearchPosition p = findAll.get(posi);
            switch (p.pattern) {
                case 2: // normal argument
                    names.add(p.toString(bmessage));
                    break;
                case 0: // optional argument
                    names.add(new String(bmessage, p.start + 1, p.end - p.start - 2).trim());
                    break;
                case 3: // flag - switch flag for description name
                    names.add(new String(bmessage, p.start + 1, p.end - p.start - 1).trim());
                    break;
                case 1: // repeated group, can only be last in list (or use flags)
                    names.add(new String(bmessage, p.start + 1, p.end - p.start - 2).trim());
            }
        }
        return names;
    }

    public boolean exists(String name) {
        return (getflags.containsKey(name) && getflags.get(name).values.size() > 0);
    }

    public ArrayList<String> getParameter(String name) {
        if (!getflags.containsKey(name)) {
            log.fatal("argument %s not in argument list: %s", name, argumentstring);
        }
        return getflags.get(name).values;
    }

    public String get(String name) {
        ArrayList<String> list = getParameter(name);
        return list.size() == 0?null:list.get(0);
    }

    public int getInt(String name, int def) {
        String v = get(name);
        return v == null?def:Integer.parseInt(v);
    }

    public long getLong(String name, long def) {
        String v = get(name);
        return v == null?def:Long.parseLong(v);
    }

    public double getDouble(String name, double def) {
        String v = get(name);
        return v == null?def:Double.parseDouble(v);
    }

    public boolean getBoolean(String name) {
        String v = get(name);
        return v == null?false:v.equals(TRUEBOOLEAN);
    }

    public String[] getStrings(String name) {
        return getParameter(name).toArray(new String[0]);
    }

    public Collection<Parameter> getParameters() {
        return getflags.values();
    }

    private void setFlags(String message) {
        byte bmessage[] = message.getBytes();
        ArrayList<ByteSearchPosition> argumentpositions = combi.findAllPos(bmessage, 0, bmessage.length);
        ArrayList<String> names = this.getArgumentNames(bmessage, argumentpositions);
        for (int argumentnumber = 0; argumentnumber < argumentpositions.size(); argumentnumber++) {
            ByteSearchPosition argument = argumentpositions.get(argumentnumber);
            if (argument.pattern == 3) {
                if (argumentnumber == argumentpositions.size() - 1 || argumentpositions.get(argumentnumber + 1).pattern == 3) {
                    Parameter f = new Parameter(4, names.get(argumentnumber), names.get(argumentnumber));
                    flags.put(names.get(argumentnumber), f);
                    getflags.put(f.tag, f);
                } else {
                    Parameter f = new Parameter(argumentpositions.get(argumentnumber + 1).pattern, names.get(argumentnumber), names.get(argumentnumber + 1));
                    flags.put(f.tag, f);
                    getflags.put(f.name, f);
                    positional.add(f);
                    argumentnumber++;
                }
            } else {
                if (argument.pattern < 2 && argumentnumber != argumentpositions.size() - 1) {
                    log.fatal("unflagged optional or repeated group can only be in last position");
                }
                Parameter f = new Parameter(argument.pattern, names.get(argumentnumber), names.get(argumentnumber));
                positional.add(f);
                getflags.put(f.name, f);
            }
        }
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
    }
}
