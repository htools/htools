package io.github.htools.collection;

import io.github.htools.lib.Log;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

/**
 * A HashMap with a ArrayList of values, that supports adding values to existing keys
 * @author jeroen
 */
public class TreeMapList<K, V> extends TreeMap<K, ArrayList<V>> {

   public static Log log = new Log(TreeMapList.class);
   
   public TreeMapList( ) {
      super();
   }
   
   public TreeMapList( Map<K, ArrayList<V>> map) {
      super(map);
   }
   
   public Map<ArrayList<V>, K> invert() {
       return new InvertedMap(this);
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
