package io.github.htools.collection;

import io.github.htools.lib.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A HashMap with a HashSet of values, that supports adding values to existing keys
 * @author jeroen
 */
public class HashMapSetCount<K, V> extends HashMap<K, HashMapInt<V>> {

   public static Log log = new Log(HashMapSetCount.class);
   
   public HashMapSetCount( ) {
      super();
   }
   
   public void add(K k, V v) {
       HashMapInt<V> list = getMap(k);
       list.add(v);
   }
   
   public void add(K k, V v, int value) {
       HashMapInt<V> list = getMap(k);
       list.add(v, value);
   }
   
   public void addIfNotExists(K k, V v) {
       HashMapInt<V> list = getMap(k);
       
       if (!list.containsKey(v))
          list.add(v);
   }
   
   public HashMapInt<V> getMap(K k) {
       HashMapInt<V> list = super.get(k);
       if (list == null) {
           list = new HashMapInt();
           put(k, list);
       }
       return list;
   }
   
   public boolean contains(K k, V v) {
       HashMapInt<V> list = super.get(k);
       if (list != null) {
           return list.containsKey(v);
       }
       return false;
   }
   
   public void addAll(HashMapSetCount<K, V> map) {
       for (Map.Entry<K, HashMapInt<V>> entry : map.entrySet()) {
           HashMapInt<V> set = getMap(entry.getKey());
           set.add(entry.getValue());
       }
   }
}
