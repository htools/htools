package io.github.repir.tools.DataTypes;

import java.util.ArrayList;
import java.util.Arrays;
import io.github.repir.tools.Lib.ConfTool;
import io.github.repir.tools.Lib.Log;

/**
 * Extension of Hadoop's Configuration that allows substitution of String values
 * and get/set using ArrayLists.
 *
 * @author Jeroen Vuurens
 */
public class Configuration extends org.apache.hadoop.conf.Configuration {

   public static Log log = new Log(Configuration.class);

   public Configuration() {
      super();
   }

   private Configuration(org.apache.hadoop.conf.Configuration other) {
      super(other);
   }

   public static Configuration convert(org.apache.hadoop.conf.Configuration conf) {
      if (conf instanceof Configuration) {
         return (Configuration) conf;
      }
      return new Configuration(conf);
   }

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
