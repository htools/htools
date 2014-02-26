package io.github.repir.tools.DataTypes;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import io.github.repir.tools.Content.Datafile;
import java.util.ArrayList;
import java.util.Arrays;
import io.github.repir.tools.Lib.Log;

/**
 * Extension of Hadoop's Configuration that allows substitution of String values
 * and get/set using ArrayLists.
 *
 * @author Jeroen Vuurens
 */
public class Configuration extends org.apache.hadoop.conf.Configuration {

   public static Log log = new Log(Configuration.class);
   static ByteRegex commentregex = new ByteRegex("#[^\\n]*\\n");
   static ByteRegex importregex = new ByteRegex("[ \\t]*import[ \\t]+");
   static ByteRegex arraykeyregex = new ByteRegex("\\+[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex optionalkeyregex = new ByteRegex("\\-[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex keyregex = new ByteRegex("[ \\t]*\\c\\w*(\\.\\w+)*[ \\t]*=");
   static ByteRegex emptylineregex = new ByteRegex("[ \\t]*\\n");
   static ByteRegex junklineregex = new ByteRegex(".*?\\n");
   static ByteRegex lineregex = new ByteRegex(commentregex, emptylineregex, importregex, arraykeyregex, optionalkeyregex, keyregex, junklineregex);
   static ByteRegex doubleregex = new ByteRegex("[ \\t]*\\d+\\.\\d+\\s*\\n");
   static ByteRegex longregex = new ByteRegex("[ \\t]*\\d+l\\s*\\n");
   static ByteRegex intregex = new ByteRegex("[ \\t]*\\d+\\s*\\n");
   static ByteRegex boolregex = new ByteRegex("[ \\t]*(true|false)\\s*\\n");
   static ByteRegex stringregex = new ByteRegex("[^\\n]+\\n");
   static ByteRegex valueregex = new ByteRegex(emptylineregex, longregex, doubleregex, intregex, boolregex, stringregex);
   private Datafile df;

   public Configuration() {
      super();
   }

   private Configuration(org.apache.hadoop.conf.Configuration other) {
      super(other);
   }

   public Configuration(Datafile df) {
      read(df);
   }

   public Configuration(String content) {
      read(content);
   }
   
   public void writeBoolean(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %s\n", key, getBoolean(key, false) ? "true" : "false");
   }
   
   public void writeInt(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
      df.printf("%s = %d\n", key, getInt(key, -1));
   }
   
   public void writeLong(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %dl\n", key, getLong(key, -1));
   }
   
   public void writeDouble(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
      df.printf("%s = %fl\n", key, getDouble(key, -1));
   }
   
   public void writeString(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         df.printf("%s = %s\n", key, get(key));
   }
   
   public void writeStrings(Datafile df, String key) {
      if (!containsKey(key))
         df.printf("%s =\n", key);
      else
         for (String value : getStrings(key))
            df.printf("+%s = %s\n", key, value);
   }
   
   public void read(Datafile df) {
      read(df, df.readAsString());
   }
   
   public void read(String content) {
      if (content != null)
         read(null, content);
   }
   
   private void read(Datafile df, String cont) {
      byte content[];
      if (cont.endsWith("\n")) {
         content = cont.getBytes();
      } else {
         content = new StringBuilder(cont).append('\n').toString().getBytes();
      }
      int pos = 0;
      while (pos < content.length) {
         boolean array = false;
         boolean optional = false;
         Pos p = lineregex.findFirst(content, pos, content.length);
         if (!p.found()) {
            break;
         }
         pos = p.end;
         switch (p.pattern) {
            case 0: // line is comment
            case 1: // line is empty
               pos = p.end;
               continue;
            case 2: // line is import
               if (df != null) {
                  p = stringregex.findFirst(content, p.end, content.length);
                  pos = p.end;
                  String file = new String(content, p.start, p.end - p.start).trim();
                  Datafile subfile = new Datafile(df.getDir().getFilename(file));
                  subfile.setFileSystem(df.getFileSystem());
                  String c = subfile.readAsString();
                  read(df, c);
               } else {
                  log.fatal("Cannot read import from string");
               }
               continue;
            case 3: // line is array
               array = true;
               p.start++;
               break;
            case 4: // optionalkey
               optional = true;
               p.start++;
            case 5: // line is no array
               break;
            default:
               log.info("unreadable line in configuration : %s", new String(content, p.start, p.end - p.start));
               continue;
         }
         String key = new String(content, p.start, p.end - p.start - 1).trim();
         if (array) {
            p = stringregex.findFirst(content, p.end, content.length);
            pos = p.end;
            String value = new String(content, p.start, p.end - p.start - 1).trim();
            addArray(key, value);
         } else {
            p = valueregex.findFirst(content, p.end, content.length);
            pos = p.end;

            String value = new String(content, p.start, p.end - p.start).trim();
            //log.info("conf key %s pattern %d content %s", key, p.pattern, value);
            switch (p.pattern) {
               case 0: // empty line means delete key
                  delete(key);
                  break;
               case 1: // long
                  if (!optional || !containsKey(key))
                      setLong(key, Long.parseLong(value.substring(0, value.indexOf('l'))));
                  break;
               case 2: // double is stored as String to avoid precision loss fro converting to float
                  if (!optional || !containsKey(key))
                     set(key, value);
                  break;
               case 3: // int
                  if (!optional || !containsKey(key))
                     setInt(key, Integer.parseInt(value));
                  break;
               case 4: // boolean
                  if (!optional || !containsKey(key))
                     setBoolean(key, value.equalsIgnoreCase("true"));
                  break;
               case 5: // string
                  if (!optional || !containsKey(key))
                     set(key, value);
            }
         }
      }
   }
   
   public boolean containsKey(String key) {
      return (get(key) != null && get(key).length() > 0);
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
         if (!values.contains(value))
            values.add(value);
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
    * If a String value contains ${correct.name} then that part will be replaced
    * by the value of correct.name. This way general configuration settings can be
    * reused such as location of the repository. Therefore the use of 
    * {@link #getSubString(java.lang.String) } is recommended over {@link #get(java.lang.String)}
    * @param key
    * @return value of key, in which other key references have been substituted.
    */
   public String getSubString(String key) {
      return getSubString(key, "");
   }

   public String getSubString(String key, String defaultvalue) {
      String value = get(key, defaultvalue);
      return substituteString(value);
   }

   public String[] getSubStrings(String key) {
      String values[] = getStrings(key);
      if (values == null) {
         return new String[0];
      }
      for (int i = 0; i < values.length; i++) {
         values[i] = substituteString(values[i]);
      }
      return values;
   }

   public ArrayList<String> getStringList(String key) {
      ArrayList<String> values = new ArrayList<String>();
      String value[] = getStrings(key);
      if (values != null) {
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
    * Note: Hadoop 0.20 does not support double, so these are stored as strings,
    * if the value is not empty or a valid double a fatal exception is the
    * result
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
}
