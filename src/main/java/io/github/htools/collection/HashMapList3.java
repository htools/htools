package io.github.htools.collection;

import io.github.htools.lib.Log;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A HashMap with a ArrayList of values, that supports adding values to existing keys
 * @author jeroen
 */
public class HashMapList3<K, V, W> extends HashMap<K, ArrayMap<V, W>> {

   public static Log log = new Log(HashMapList3.class);

   public HashMapList3( ) {
      super();
   }

   public HashMapList3(int size ) {
      super(size);
   }

   public void add(K k, V v, W w) {
       ArrayMap<V, W> list = getList(k);
       list.add(v, w);
   }
   
   public ArrayMap<V, W> getList(K k) {
       ArrayMap<V, W> list = super.get(k);
       if (list == null) {
           list = new ArrayMap();
           put(k, list);
       }
       return list;
   }
}
