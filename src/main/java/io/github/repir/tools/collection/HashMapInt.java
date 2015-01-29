package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.HashMap;
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
       put(key, oldvalue==null?value:oldvalue+value);
   }
   
   @Override
   public Integer get(Object key) {
       Integer oldvalue = super.get(key);
       return (oldvalue==null)?0:oldvalue;
   }
   
   public void addIfExists(K key, int value) {
       Integer oldvalue = get(key);
       if (oldvalue != null)
          put(key, oldvalue+value);
   }
   
}
