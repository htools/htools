package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.Map;

/**
 * A HashMap containing a nested HashMap as values
 * @author jeroen
 */
public class FHashMapMap<K, K2, V> extends Object2ObjectOpenHashMap<K, Map<K2, V>> {

   public static Log log = new Log(FHashMapMap.class);
   
   public FHashMapMap( ) {
      super();
   }
   
   public void put(K k, K2 k2, V v) {
       Map<K2, V> list = getHashMap(k);
       list.put(k2, v);
   }
   
   public Map<K2, V> getHashMap(K k) {
       Map<K2, V> map = super.get(k);
       if (map == null) {
           map = new Object2ObjectOpenHashMap();
           put(k, map);
       }
       return map;
   }
}
