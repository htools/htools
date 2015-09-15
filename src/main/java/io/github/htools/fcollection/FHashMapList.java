package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A HashMap with a ArrayList of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapList<K, V> extends Object2ObjectOpenHashMap<K, ObjectArrayList<V>> {

   public static Log log = new Log(FHashMapList.class);
   
   public FHashMapList( ) {
      super();
   }
   
   public FHashMapList( int size ) {
      super(size);
   }
   
   public void add(K k, V v) {
       ObjectArrayList<V> list = getList(k);
       list.add(v);
   }
   
   public ObjectArrayList<V> getList(K k) {
       ObjectArrayList<V> list = super.get(k);
       if (list == null) {
           list = new ObjectArrayList();
           put(k, list);
       }
       return list;
   }
}
