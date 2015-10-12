package io.github.htools.fcollection;

import io.github.htools.collection.*;
import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A HashMap with a HashSet of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapSetCount<K, V> extends Object2ObjectOpenHashMap<K, FHashMapObjectInt<V>> {

   public static Log log = new Log(FHashMapSetCount.class);
   
   public FHashMapSetCount( ) {
      super();
   }
   
   public void add(K k, V v) {
       FHashMapObjectInt<V> list = getMap(k);
       list.add(v);
   }
   
   public void addIfNotExists(K k, V v) {
       FHashMapObjectInt<V> list = getMap(k);
       
       if (!list.containsKey(v))
          list.add(v);
   }
   
   public FHashMapObjectInt<V> getMap(K k) {
       FHashMapObjectInt<V> list = super.get(k);
       if (list == null) {
           list = new FHashMapObjectInt();
           put(k, list);
       }
       return list;
   }
   
   public boolean contains(K k, V v) {
       FHashMapObjectInt<V> list = super.get(k);
       if (list != null) {
           return list.containsKey(v);
       }
       return false;
   }
   
   public void addAll(FHashMapSetCount<K, V> map) {
       for (Map.Entry<K, FHashMapObjectInt<V>> entry : map.entrySet()) {
           FHashMapObjectInt<V> set = getMap(entry.getKey());
           set.add(entry.getValue());
       }
   }
}
