package io.github.htools.collection;

import io.github.htools.type.Tuple2;
import io.github.htools.lib.Log;
import io.github.htools.type.KV;
import java.util.HashMap;

/**
 * A HashMap containing non-unique integers that are sorted descending
 * <p>
 * @author jeroen
 */
public class HashPairMap<K1, K2, V> extends HashMap<Tuple2<K1, K2>, V> {

   public static Log log = new Log(HashPairMap.class);
   
   public HashPairMap( ) {
      super();
   }
   
   public void put(K1 k1, K2 k2, V v) {
       super.put( new Tuple2<K1, K2>(k1, k2), v);
   }
   
   public V get(K1 k1, K2 k2) {
       return super.get( new Tuple2<K1, K2>(k1, k2));
   }
   
   public boolean containsKey(K1 k1, K2 k2) {
       return super.containsKey(new Tuple2<K1, K2>(k1, k2));
   }
   
   public V getOrDefault(K1 k1, K2 k2, V v) {
       return super.getOrDefault(new Tuple2<K1, K2>(k1, k2), v);
   }
   
   public V putIfAbsent(K1 k1, K2 k2, V v) {
       return super.putIfAbsent(new Tuple2<K1, K2>(k1, k2), v);
   }
}
