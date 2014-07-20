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
    public final HashMap<String, ArrayList<String>> parsedargstemp = new HashMap();
    public final HashMap<String, Object> parsedargs = new HashMap();

    public ArgsParser(String args[], String message) {
        String argumentname;
        this.argumentstring = message;
        byte bmessage[] = message.getBytes();
        //args = getFlags(args);
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
                    }
                    if (parsedargstemp.containsKey(names.get(argumentnumber))) {
                        log.fatal("duplicate use of non-repeating parameters: %s in %s", names.get(argumentnumber), message);
                    }
                    put(names.get(argumentnumber), args[argspos++]);
                    break;
                case 0: // optional argument
                    if (parsedargstemp.containsKey(names.get(argumentnumber))) {
                        log.fatal("duplicate use of non-repeating parameters: %s in %s", names.get(argumentnumber), message);
                    }
                    if (argspos < args.length) {
                        put(names.get(argumentnumber), args[argspos++]);
                    }
                    break;
                case 3: // flag - switch flag for description name
                    if (argumentnumber < names.size() - 1) {
                        int nexttype = argumentpositions.get(argumentnumber + 1).pattern;
                        if (nexttype != 3) {
                            argumentname = names.get(++argumentnumber);
                            if (nexttype == 1) {
                                for (; argspos < args.length && !flag.match(args[argspos]); argspos++) {
                                    put(argumentname, args[argspos]);
                                }
                            } else {
                                if (parsedargstemp.containsKey(names.get(argumentnumber))) {
                                    log.fatal("duplicate use of non-repeating parameters: %s in %s", names.get(argumentnumber + 1), message);
                                }
                                put(argumentname, args[argspos++]);
                            }
                        } else {
                            if (parsedargstemp.containsKey(names.get(argumentnumber))) {
                                log.fatal("duplicate use of non-repeating parameters: %s in %s", names.get(argumentnumber), message);
                            }
                            put(names.get(argumentnumber), TRUEBOOLEAN);
                        }
                    }
                    break;
                case 1: // repeated group, can only be last in list (or use flags)
                    for (; argspos < args.length && !flag.match(args[argspos]); argspos++) {
                        put(names.get(argumentnumber), args[argspos]);
                    }
            }
        }
        if (argspos < args.length) {
            log.exit("run with parameters: %s", message);
        }
    }

    public void put(String key, String value) {
        ArrayList<String> list = parsedargstemp.get(key);
        if (list == null) {
            list = new ArrayList();
            parsedargstemp.put(key, list);
        }
        list.add(value);
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
        return (parsedargstemp.containsKey(name) && parsedargstemp.get(name).size() > 0);
    }

    public ArrayList<String> get(String name) {
        if (!parsedargstemp.containsKey(name)) {
            log.fatal("argument %s not in argument list: %s", name, argumentstring);
        }
        return parsedargstemp.get(name);
    }
}
