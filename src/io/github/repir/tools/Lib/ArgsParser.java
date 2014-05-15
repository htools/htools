package io.github.repir.tools.Lib;

import java.util.ArrayList;
import java.util.HashMap;
import io.github.repir.tools.ByteSearch.ByteRegex;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;
import io.github.repir.tools.ByteSearch.ByteSearchPosition;

/**
 * Parses the argument array args[] that was passed to a main() method against a
 * String that lists the parameters. The parameter string lists the parameters with 
 * descriptive names, separated by spaces and after the mandatory arguments may be followed by 
 * optional parameters within [] and after that a repeated group between {}. 
 * <p/>
 * The parsed arguments can be accessed as a String using {@link #get(java.lang.String) },
 * as an int using {@link #getInt(java.lang.String) } as a Double using
 * {@link #getDouble(java.lang.String) } or as the closing repeated group
 * using {@link #getRepeatedGroup() } which is always an array of String that
 * closes the list of parameters (hence no name is required).
 * <p/>
 * A descriptive fatal exception is thrown if the arguments do not exists the
 * parameter, if an unknown parameter is requested using a get method, or if 
 * a numeric type is requested and the String cannot be parsed into a number.
 * @author jeroen
 */
public class ArgsParser {

   public static Log log = new Log(ArgsParser.class);
   static final ByteRegex optional = new ByteRegex("\\[\\s?\\c\\w*\\s?\\]");
   static final ByteRegex repeat = new ByteRegex("\\{\\s?\\c\\w*\\s?\\}");
   static final ByteRegex term = new ByteRegex("\\c\\S*");
   static final ByteRegex combi = ByteRegex.combine(optional, repeat, term);
   final String argumentstring;
   public final HashMap<String, String> parsedargs = new HashMap<String, String>();
   String repeatedgroupname;
   String repeatedgroup[];

   public ArgsParser(String args[], String message) {
      this.argumentstring = message;
      byte bmessage[] = message.getBytes();
      ArrayList<ByteSearchPosition> findAll = combi.findAllPos(bmessage, 0, bmessage.length);
      int argspos = 0;
      for (int posi = 0; posi < findAll.size(); posi++) {
         ByteSearchPosition p = findAll.get(posi);
         switch (p.pattern) {
            case 2:
               if (argspos >= args.length) {
                  log.fatal("run with parameters: %s", message);
                  break;
               }
               parsedargs.put(p.toString(bmessage), args[argspos++]);
               break;
            case 0:
               if (argspos < args.length) {
                  parsedargs.put(new String(bmessage, p.start + 1, p.end - p.start - 2).trim(), args[argspos++]);
               } else {
                  parsedargs.put(new String(bmessage, p.start + 1, p.end - p.start - 2).trim(), null);
               }
               break;
            case 1:
               if (posi != findAll.size() - 1) {
                  log.exit("parameter string can only end with one repeated group: %s", message);
               }
               repeatedgroupname = new String(bmessage, p.start + 1, p.end - p.start - 2).trim();
               repeatedgroup = new String[ args.length - argspos ];
               System.arraycopy(args, argspos, repeatedgroup, 0, repeatedgroup.length);
               argspos = args.length;
         }
      }
      if (repeatedgroup == null && argspos < args.length) {
         log.exit("run with parameters: %s", message);
      }
   }

   public boolean exists(String name) {
      return parsedargs.containsKey(name) && parsedargs.get(name) != null;
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
   
   public String[] getRepeatedGroup() {
      return repeatedgroup;
   }
   
   public String getRepeatedGroupName() {
      return repeatedgroupname;
   }
}
