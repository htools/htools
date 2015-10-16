package io.github.htools.collection;

import io.github.htools.lib.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * A HashMap containing a nested HashMap as values.
 * @author jeroen
 */
public class HashMapMap<K, K2, V> extends HashMap<K, Map<K2, V>> {

   public static Log log = new Log(HashMapMap.class);
   
   public HashMapMap( ) {
      super();
   }
   
   public void put(K k, K2 k2, V v) {
       Map<K2, V> list = getHashMap(k);
       list.put(k2, v);
   }
   
   /**
    * @param k key
    * @return The value Map<K2, V> for key k, if no map exists for key k an empty
    * Map is created.
    */
   public Map<K2, V> getHashMap(K k) {
       Map<K2, V> map = super.get(k);
       if (map == null) {
           map = new HashMap();
           put(k, map);
       }
       return map;
   }
}
