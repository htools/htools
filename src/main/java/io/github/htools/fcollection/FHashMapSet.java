package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;

import java.util.Map;

/**
 * A HashMap with a HashSet of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapSet<K, V> extends 
        Object2ObjectOpenHashMap<K, FHashSet<V>> {

   public static Log log = new Log(FHashMapSet.class);
   
   public FHashMapSet( ) {
      super();
   }
   
   public void add(K k, V v) {
       ObjectOpenHashSet<V> list = getSet(k);
       list.add(v);
   }
   
   public void addIfNotExists(K k, V v) {
       ObjectOpenHashSet<V> list = getSet(k);
       if (!list.contains(v))
          list.add(v);
   }
   
   public ObjectOpenHashSet<V> getSet(K k) {
       FHashSet<V> list = super.get(k);
       if (list == null) {
           list = new FHashSet();
           put(k, list);
       }
       return list;
   }
   
   public boolean contains(K k, V v) {
       ObjectOpenHashSet<V> list = super.get(k);
       if (list != null) {
           return list.contains(v);
       }
       return false;
   }
   
   public ObjectSet<Map.Entry<K, FHashSet<V>>> entryset() {
       return super.entrySet();
   }
   
   public void addAll(FHashMapSet<K, V> map) {
       for (Entry<K, FHashSet<V>> entry : map.object2ObjectEntrySet()) {
           ObjectOpenHashSet<V> set = getSet(entry.getKey());
           set.addAll(entry.getValue());
       }
   }
}
