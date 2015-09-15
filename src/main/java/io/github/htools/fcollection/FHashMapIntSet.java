package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/**
 * A HashMap with a HashSet of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapIntSet<V> extends Int2ObjectOpenHashMap<ObjectOpenHashSet<V>> {

   public static Log log = new Log(FHashMapIntSet.class);
   
   public FHashMapIntSet( ) {
      super();
   }
   
   public void add(int k, V v) {
       ObjectOpenHashSet<V> list = getSet(k);
       list.add(v);
   }
   
   public void addIfNotExists(int k, V v) {
       ObjectOpenHashSet<V> list = getSet(k);
       if (!list.contains(v))
          list.add(v);
   }
   
   public ObjectOpenHashSet<V> getSet(int k) {
       ObjectOpenHashSet<V> list = super.get(k);
       if (list == null) {
           list = new ObjectOpenHashSet();
           put(k, list);
       }
       return list;
   }
   
   public boolean contains(int k, V v) {
       ObjectOpenHashSet<V> list = super.get(k);
       if (list != null) {
           return list.contains(v);
       }
       return false;
   }
   
   public void addAll(FHashMapIntSet<V> map) {
       for (Entry<ObjectOpenHashSet<V>> entry : map.int2ObjectEntrySet()) {
           ObjectOpenHashSet<V> set = getSet(entry.getIntKey());
           set.addAll(entry.getValue());
       }
   }
}
