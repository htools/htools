package io.github.htools.fcollection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * A HashMap with a ArrayList of values, that supports adding values to existing keys
 * @author jeroen
 */
public class FHashMapIntList<V> extends Int2ObjectOpenHashMap<ObjectArrayList<V>> {

   public static Log log = new Log(FHashMapIntList.class);
   
   public FHashMapIntList( ) {
      super();
   }
   
   public FHashMapIntList( int size ) {
      super(size);
   }
   
   public void add(int k, V v) {
       ObjectArrayList<V> list = getList(k);
       list.add(v);
   }
   
   public ObjectArrayList<V> getList(int k) {
       ObjectArrayList<V> list = super.get(k);
       if (list == null) {
           list = new ObjectArrayList();
           put(k, list);
       }
       return list;
   }
}
