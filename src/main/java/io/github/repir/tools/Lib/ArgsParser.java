package io.github.repir.tools.Lib;

import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
 * The parsed arguments can be accessed as a String using {@link #get(java.lang.String)
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
    static final ByteRegex combi = ByteRegex.combine(optional, repeat, term, flag);
    final String argumentstring;
    public final HashMap<String, String> parsedargs = new HashMap<String, String>();
    public final HashMap<String, String[]> repeatedgroups = new HashMap<String, String[]>();
    HashMap<String, ArrayList<String>> temprepeatedgroups = new HashMap();

    public ArgsParser(String args[], String message) {
        String argumentname;
        this.argumentstring = message;
        byte bmessage[] = message.getBytes();
        args = getFlags(args);
        //log.info("remaining args %s", ArrayTools.concat(args));
        ArrayList<ByteSearchPosition> argumentpositions = combi.findAllPos(bmessage, 0, bmessage.length);
        ArrayList<String> names = this.getArgumentNames(bmessage, argumentpositions);

        int argspos = 0;
        for (int argumentnumber = 0; argumentnumber < argumentpositions.size(); argumentnumber++) {
            ByteSearchPosition argument = argumentpositions.get(argumentnumber);
            //log.info("argument %d pattern %d start %d", argumentnumber, argument.pattern, argument.start);
            switch (argument.pattern) {
                case 2: // normal argument
                    if (argspos >= args.length) {
                        log.fatal("run with parameters: %s", message);
                        break;
                    }
                    parsedargs.put(names.get(argumentnumber), args[argspos++]);
                    break;
                case 0: // optional argument
                    if (argspos < args.length) {
                        parsedargs.put(names.get(argumentnumber), args[argspos++]);
                    } else {
                        parsedargs.put(names.get(argumentnumber), null);
                    }
                    break;
                case 3: // flag - switch flag for description name
                    if (argumentnumber < names.size() - 1 && argumentpositions.get(argumentnumber+1).pattern != 3) {
                        String flagname = names.get(argumentnumber);
                        argumentname = names.get(argumentnumber + 1);
                        if (!flagname.equals(argumentname)) {
                            if (parsedargs.containsKey(flagname)) {
                                parsedargs.put(argumentname, parsedargs.get(flagname));
                                parsedargs.remove(flagname);
                                argumentnumber++;
                            }
                            if (temprepeatedgroups.containsKey(flagname)) {
                                temprepeatedgroups.put(argumentname, temprepeatedgroups.get(flagname));
                                temprepeatedgroups.remove(flagname);
                                argumentnumber++;
                            }
                        }
                    }
                    break;
                case 1: // repeated group, can only be last in list (or use flags)
                    ArrayList<String> group = temprepeatedgroups.get(names.get(argumentnumber));
                    if (group == null) {
                        group = new ArrayList<String>();
                        temprepeatedgroups.put(names.get(argumentnumber), group);
                    }
                    for (int i = argspos; i < args.length; i++) {
                        group.add(args[i]);
                    }
                    argspos = args.length;
            }
        }
        if (argspos < args.length) {
            log.exit("run with parameters: %s", message);
        }

        // convert repeated groups to String arrays
        for (Map.Entry<String, ArrayList<String>> entry : temprepeatedgroups.entrySet()) {
            this.repeatedgroups.put(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
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

    /**
     * Extracts the flags from the arguments. Repeating flags are stored in
     * repeatedgroups as an arraylist. Boolean flags are supported but only when
     * followed by another flag or the end of the args array (otherwise there is
     * no way to identify this as a boolean flag). Boolean flags obtain the
     * value "true". Other flags have single values.
     *
     * @param args String array of arguments, usually passed to main.
     * @return the args array with the flags removed.
     */
    public String[] getFlags(String args[]) {
        ArrayList<String> remainingargs = new ArrayList<String>();
        for (int i = 0; i < args.length; i++) {
            String f = flag.extractMatch(args[i]);
            if (f != null) {
                f = f.substring(1);
                if (i == args.length - 1 || flag.match(args[i + 1])) // boolean flag
                {
                    parsedargs.put(f, TRUEBOOLEAN);
                } else {
                    String exists = parsedargs.get(f);
                    if (exists != null) { // repeating flag
                        ArrayList<String> group = new ArrayList<String>();
                        group.add(exists);
                        group.add(args[i + 1]);
                        temprepeatedgroups.put(f, group);
                        parsedargs.remove(f);
                    } else {
                        ArrayList<String> group = temprepeatedgroups.get(f);
                        if (group != null) { // repeating flag
                            group.add(args[i + 1]);
                        } else { // non repeating flag
                            parsedargs.put(f, args[i + 1]);
                        }
                    }
                    i++;
                }
            } else {
                remainingargs.add(args[i]);
            }
        }
        return remainingargs.toArray(new String[remainingargs.size()]);
    }

    public boolean exists(String name) {
        return (parsedargs.containsKey(name) && parsedargs.get(name) != null)
                || repeatedgroups.containsKey(name);
    }

    public String get(String name) {
        if (!parsedargs.containsKey(name)) {
            log.fatal("argument %s not in argument list: %s", name, argumentstring);
        }
        return parsedargs.get(name);
    }

    public int getInt(String name) {
        int i = Const.NULLINT;
        String arg = get(name);
        if (arg != null) {
            try {
                i = Integer.parseInt(arg);
            } catch (NumberFormatException ex) {
                log.fatal("argument %s is not an Integer (%s) in argument list: %s", name, arg, argumentstring);
            }
        }
        return i;
    }

    public boolean getBoolean(String name) {
        String arg = get(name);
        if (arg != null) {
            return arg.equals(TRUEBOOLEAN);
        }
        return false;
    }

    public double getDouble(String name) {
        double i = Const.NULLINT;
        String arg = get(name);
        if (arg != null) {
            try {
                i = Double.parseDouble(arg);
            } catch (NumberFormatException ex) {
                log.fatal("argument %s is not a Double (%s) in argument list: %s", name, arg, argumentstring);
            }
        }
        return i;
    }

    public String[] getRepeatedGroup(String name) {
        if (!exists(name)) {
            return new String[0];
        }
        if (this.repeatedgroups.containsKey(name)) {
            return repeatedgroups.get(name);
        } else {
            return new String[]{get(name)};
        }
    }

    public Set<String> getRepeatedGroupNames() {
        return repeatedgroups.keySet();
    }
}
