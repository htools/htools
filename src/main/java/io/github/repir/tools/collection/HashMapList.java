package io.github.repir.tools.collection;

import io.github.repir.tools.type.Tuple2;
import io.github.repir.tools.lib.Log;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMapList<K, V> extends HashMap<K, ArrayList<V>> {

   public static Log log = new Log(HashMapList.class);
   
   public HashMapList( ) {
      super();
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
