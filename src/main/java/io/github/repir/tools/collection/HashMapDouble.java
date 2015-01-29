package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author jeroen
 */
public class HashMapDouble<K> extends HashMap<K, Double> {
   public static final Log log = new Log( HashMapDouble.class );

   public HashMapDouble() {
       super();
   }
   
   public void add(K key, double value) {
       Double oldvalue = get(key);
       put(key, oldvalue==null?value:oldvalue+value);
   }
   
   /**
    * Normalize the Map, so that the sum of values equals 1.
    */
   public void normalize() {
       double total = 0;
       for (Double d : values())
           total += d;
       for (Map.Entry<K, Double> entry : entrySet())
           entry.setValue(entry.getValue() / total);
   }
}
