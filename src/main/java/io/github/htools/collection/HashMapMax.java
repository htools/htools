package io.github.htools.collection;

import io.github.htools.lib.Log;
import io.github.htools.type.Tuple2;

import java.util.HashMap;
import java.util.Map;

/**
 * A HashMap with a Double value, that supports adding values to existing keys
 * and only retaining the highest value.
 * @author jeroen
 */
public class HashMapMax<K, V> extends HashMap<K, Tuple2<Integer, V>> {

   public static Log log = new Log(HashMapMax.class);
   
   public HashMapMax( ) {
      super();
   }
   
   public Map<V, K> invert() {
       return new InvertedMap(this);
   }
   
   public void add(K k, Integer d, V v) {
       Tuple2<Integer, V> entry = get(k);
       if (entry == null)
           put(k, new Tuple2<Integer, V>(d, v));
       else if (entry.key < d)
           entry.key = d;
   }
   
   public V getValue(K k) {
       Tuple2<Integer, V> list = super.get(k);
       if (list != null) {
           return list.value;
       }
       return null;
   }
}
