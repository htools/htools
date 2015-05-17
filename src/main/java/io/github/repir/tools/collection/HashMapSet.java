package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMapSet<K, V> extends HashMap<K, HashSet<V>> {

   public static Log log = new Log(HashMapSet.class);
   
   public HashMapSet( ) {
      super();
   }
   
   public void add(K k, V v) {
       HashSet<V> list = getSet(k);
       list.add(v);
   }
   
   public void addIfNotExists(K k, V v) {
       HashSet<V> list = getSet(k);
       if (!list.contains(v))
          list.add(v);
   }
   
   public HashSet<V> getSet(K k) {
       HashSet<V> list = super.get(k);
       if (list == null) {
           list = new HashSet();
           put(k, list);
       }
       return list;
   }
   
   public boolean contains(K k, V v) {
       HashSet<V> list = super.get(k);
       if (list != null) {
           return list.contains(v);
       }
       return false;
   }
   
   public void addAll(HashMapSet<K, V> map) {
       for (Map.Entry<K, HashSet<V>> entry : map.entrySet()) {
           HashSet<V> set = getSet(entry.getKey());
           set.addAll(entry.getValue());
       }
   }
}
