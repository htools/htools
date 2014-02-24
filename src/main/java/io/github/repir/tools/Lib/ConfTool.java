package io.github.repir.tools.Lib;

import io.github.repir.tools.ByteRegex.ByteRegex;
import io.github.repir.tools.ByteRegex.ByteRegex.Pos;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import io.github.repir.tools.Content.Datafile;
import io.github.repir.tools.DataTypes.Configuration;

public class ConfTool {

   public static Log log = new Log(ConfTool.class);
   HashMap<String, Integer> intkeys = new HashMap<String, Integer>();
   HashMap<String, Long> longkeys = new HashMap<String, Long>();
   HashMap<String, String> stringkeys = new HashMap<String, String>();
   HashMap<String, ArrayList<String>> arraykeys = new HashMap<String, ArrayList<String>>();
   HashMap<String, Boolean> boolkeys = new HashMap<String, Boolean>();
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

   public ConfTool() {
   }

   public ConfTool(Datafile df) {
      read(df);
   }

   public ConfTool(String content) {
      read(content);
   }

   public ConfTool(Configuration conf) {
      toConf(conf);
   }

   public void toConf(Configuration conf) {
      for (Map.Entry<String, Boolean> entry : boolkeys.entrySet()) {
         conf.setBoolean(entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, Integer> entry : intkeys.entrySet()) {
         conf.setInt(entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, Long> entry : longkeys.entrySet()) {
         conf.setLong(entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, String> entry : stringkeys.entrySet()) {
         conf.set(entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, ArrayList<String>> entry : arraykeys.entrySet()) {
         conf.setStrings(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
         //log.info("ArraySet %s %s", entry.getKey(), Lib.StrTools.concat(',', conf.getStrings(entry.getKey())));
      }
   }

   public Configuration toConf() {
      Configuration c = new Configuration();
      toConf(c);
      return c;
   }

   public void write(Datafile df) {
      for (Map.Entry<String, Boolean> entry : boolkeys.entrySet()) {
         df.printf("%s = %s\n", entry.getKey(), entry.getValue() ? "true" : "false");
      }
      for (Map.Entry<String, Integer> entry : intkeys.entrySet()) {
         df.printf("%s = %s\n", entry.getKey(), entry.getValue().toString());
      }
      for (Map.Entry<String, Long> entry : longkeys.entrySet()) {
         df.printf("%s = %s\n", entry.getKey(), entry.getValue().toString() + "l");
      }
      for (Map.Entry<String, String> entry : stringkeys.entrySet()) {
         df.printf("%s = %s\n", entry.getKey(), entry.getValue());
      }
      for (Map.Entry<String, ArrayList<String>> entry : arraykeys.entrySet()) {
         for (String value : entry.getValue()) {
            df.printf("+%s = %s\n", entry.getKey(), value);
         }
      }
      df.close();
   }
   
   public void read(Datafile df) {
      read(df, df.readAsString());
   }
   
   public void read(String content) {
      read(null, content);
   }
   
   public void read(Datafile df, String cont) {
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
            setArray(key, value);
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
                  if (!optional || !longkeys.containsKey(key))
                      longkeys.put(key, Long.parseLong(value.substring(0, value.indexOf('l'))));
                  break;
               case 2: // double is stored as String to avoid precision loss fro converting to float
                  if (!optional || !stringkeys.containsKey(key))
                  stringkeys.put(key, value);
                  break;
               case 3: // int
                  if (!optional || !intkeys.containsKey(key))
                  intkeys.put(key, Integer.parseInt(value));
                  break;
               case 4: // boolean
                  if (!optional || !boolkeys.containsKey(key))
                  boolkeys.put(key, value.equalsIgnoreCase("true"));
                  break;
               case 5: // string
                  if (!optional || !stringkeys.containsKey(key))
                  stringkeys.put(key, value);
            }
         }
      }
   }

   public void store(String key, String value) {
      Pos p = valueregex.findFirst(value);
      value = value.trim();
      switch (p.pattern) {
         case 0: // empty line means delete key
            delete(key);
            break;
         case 1: // long
            longkeys.put(key, Long.parseLong(value.substring(0, value.indexOf('l'))));
            break;
         case 2: // double is stored as String to avoid precision loss fro converting to float
            stringkeys.put(key, value);
            break;
         case 3: // int
            intkeys.put(key, Integer.parseInt(value));
            break;
         case 4: // boolean
            boolkeys.put(key, value.equalsIgnoreCase("true"));
            break;
         case 5: // string
            stringkeys.put(key, value);
      }
   }

   public String getString(String label) {
      return stringkeys.get(label);
   }

   public int getInt(String label) {
      if (intkeys.containsKey(label)) {
         return intkeys.get(label);
      }
      return -1;
   }

   public long getLong(String label) {
      if (longkeys.containsKey(label)) {
         return longkeys.get(label);
      }
      return -1;
   }

   public double getDouble(String label) {
      if (stringkeys.containsKey(label)) {
         return Double.parseDouble(stringkeys.get(label));
      }
      return -1;
   }

   public boolean getBoolean(String label) {
      if (boolkeys.containsKey(label)) {
         return boolkeys.get(label);
      }
      return false;
   }

   public String[] getArray(String label) {
      if (arraykeys.containsKey(label)) {
         return arraykeys.get(label).toArray(new String[arraykeys.get(label).size()]);
      }
      return new String[0];
   }

   public void set(String label, String value) {
      stringkeys.put(label, value);
   }

   public void set(String label, int value) {
      intkeys.put(label, value);
   }

   public void set(String label, long value) {
      longkeys.put(label, value);
   }

   public void set(String label, double value) {
      stringkeys.put(label, Double.toString(value));
   }

   public void set(String label, boolean value) {
      boolkeys.put(label, value);
   }

   public void delete(String label) {
      stringkeys.remove(label);
      intkeys.remove(label);
      longkeys.remove(label);
      boolkeys.remove(label);
      arraykeys.remove(label);
   }

   public void setArray(String label, String value) {
      if (value.length() == 0) {
         arraykeys.remove(label);
      } else if (arraykeys.containsKey(label)) {
         ArrayList<String> values = arraykeys.get(label);
         if (!values.contains(value))
            values.add(value);
      } else {
         ArrayList<String> values = new ArrayList<String>();
         values.add(value);
         arraykeys.put(label, values);
      }
   }
}
