package io.github.htools.collection;

import io.github.htools.lib.Log;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A HashMap with a ArrayList of values, that supports adding values to existing keys
 * @author jeroen
 */
public class HashMapList<K, V> extends HashMap<K, ArrayList<V>> {

   public static Log log = new Log(HashMapList.class);
   
   public HashMapList( ) {
      super();
   }
   
   public HashMapList( int size ) {
      super( size );
   }
   
   public void add(K k, V v) {
       ArrayList<V> list = getList(k);
       list.add(v);
   }
   
   public ArrayList<V> getList(K k) {
       ArrayList<V> list = super.get(k);
       if (list == null) {
           list = new ArrayList();
           put(k, list);
       }
       return list;
   }
}
