package io.github.repir.tools.collection;

import io.github.repir.tools.lib.Log;
import io.github.repir.tools.type.Tuple2;
import java.util.HashMap;

/**
 * A TreeSet containing non-unique integers that are sorted descending
 * <p/>
 * @author jeroen
 */
public class HashMapMaxDouble<K, V> extends HashMap<K, Tuple2<Double, V>> {

   public static Log log = new Log(HashMapMaxDouble.class);
   
   public HashMapMaxDouble( ) {
      super();
   }
   
   public void add(K k, Double d, V v) {
       Tuple2<Double, V> entry = get(k);
       if (entry == null || entry.key < d)
           put(k, new Tuple2<Double, V>(d, v));
   }
   
   public V getValue(K k) {
       Tuple2<Double, V> list = super.get(k);
       if (list != null) {
           return list.value;
       }
       return null;
   }
   
   public Double getScore(K k) {
       Tuple2<Double, V> list = super.get(k);
       if (list != null) {
           return list.key;
       }
       return null;
   }
}
