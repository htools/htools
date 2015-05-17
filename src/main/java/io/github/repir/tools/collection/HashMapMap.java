package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.HashMap;
import java.util.Map;

/**
 * A HashMap containing a nested HashMap as values
 * <p/>
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
   
   public Map<K2, V> getHashMap(K k) {
       Map<K2, V> map = super.get(k);
       if (map == null) {
           map = new HashMap();
           put(k, map);
       }
       return map;
   }
}
