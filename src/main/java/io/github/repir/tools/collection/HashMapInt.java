package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
/**
 *
 * @author jeroen
 */
public class HashMapInt<K> extends HashMap<K, Integer> {
   public static final Log log = new Log( HashMapInt.class );

   public HashMapInt() {
       super();
   }
   
   public void add(K key, int value) {
       Integer oldvalue = get(key);
       if (oldvalue == null)
           put(key, value);
       else {
           int newvalue = oldvalue + value;
           if (newvalue == 0)
               remove(key);
           else
               put(key, newvalue);
       }
   }
   
   public void add(K key) {
       add(key, 1);
   }
   
   public void addAll(Collection<K> keys) {
       for (K key : keys)
          add(key, 1);
   }
   
   @Override
   public Integer get(Object key) {
       Integer oldvalue = super.get(key);
       return (oldvalue==null)?0:oldvalue;
   }
   
   public K max() {
       Integer maxvalue = Integer.MIN_VALUE;
       K max = null;
       for (Map.Entry<K, Integer> entry : entrySet()) {
           if (entry.getValue() > maxvalue) {
               maxvalue = entry.getValue();
               max = entry.getKey();
           }
       }
       return max;
   }
   
   public K min() {
       Integer minvalue = Integer.MAX_VALUE;
       K min = null;
       for (Map.Entry<K, Integer> entry : entrySet()) {
           if (entry.getValue() < minvalue) {
               minvalue = entry.getValue();
               min = entry.getKey();
           }
       }
       return min;
   }
   
   public void addIfExists(K key, int value) {
       Integer oldvalue = get(key);
       if (oldvalue != null)
          put(key, oldvalue+value);
   }
}
